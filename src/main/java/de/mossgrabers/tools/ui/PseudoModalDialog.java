// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;


/**
 * A pseudo-modal dialog that overlays the owner's scene instead of opening a second native window.
 * Avoids the macOS "modal flash" issue entirely.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class PseudoModalDialog
{
    protected final Stage                    owner;
    protected final Pane                     content;
    protected final TraversalManager         traversalManager = new TraversalManager ();

    private StackPane                        overlay;
    private Parent                           originalRoot;
    private Pane                             injectedRoot;
    private final ObservableList<ButtonType> buttons          = FXCollections.observableArrayList ();
    private final Map<ButtonType, Button>    buttonNodes      = new WeakHashMap<> ();

    private ButtonType                       ok;
    private ButtonType                       cancel;
    private CompletableFuture<Boolean>       future;


    /**
     * Constructor.
     *
     * @param owner The owner stage of the pseudo dialog
     * @param titleID The ID for the title of the dialog
     */
    protected PseudoModalDialog (final Stage owner, final String titleID)
    {
        this.owner = Objects.requireNonNull (owner);

        this.buttons.addListener ((ListChangeListener<ButtonType>) c -> {
            while (c.next ())
            {
                if (c.wasRemoved ())
                    for (final ButtonType cmd: c.getRemoved ())
                        this.buttonNodes.remove (cmd);
                if (c.wasAdded ())
                    for (final ButtonType cmd: c.getAddedSubList ())
                        if (!this.buttonNodes.containsKey (cmd))
                            this.buttonNodes.put (cmd, createButton (cmd));
            }
        });

        this.content = Objects.requireNonNull (this.init ());

        // Make the content look like a dialog panel if it doesn't already
        if (this.content instanceof final Region r)
        {
            r.setStyle (r.getStyle () + ";-fx-background-color:-fx-background;-fx-background-radius:8;-fx-padding:16;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0.2, 0, 4)");
            r.setMaxWidth (Region.USE_PREF_SIZE);
            r.setMaxHeight (Region.USE_PREF_SIZE);
        }

        this.content.getChildren ().add (0, createTitleBar (Functions.getText (titleID)));

        final ButtonBar buttonBar = this.createButtonBar ();
        buttonBar.setPadding (new Insets (16, 0, 0, 0));
        this.content.getChildren ().add (buttonBar);

        this.set ();
    }


    /**
     * Sets the OK button.
     *
     * @param okString The button to set as the OK (default) button
     */
    public void setButtons (final String okString)
    {
        this.setButtons (okString, null);
    }


    /**
     * Sets the OK and cancel button.
     *
     * @param okString The button to set as the OK (default) button
     * @param cancelString The button to set as the CANCEL (Esc) button
     */
    public void setButtons (final String okString, final String cancelString)
    {
        if (okString != null)
        {
            this.ok = new ButtonType (Functions.getText (okString), ButtonData.OK_DONE);
            this.buttons.add (this.ok);
            this.getOkButton ().addEventFilter (ActionEvent.ACTION, event -> {
                if (this.onOk ())
                    this.close (Boolean.TRUE);
                else
                    event.consume ();
            });
        }

        if (cancelString != null)
        {
            this.cancel = new ButtonType (Functions.getText (cancelString), ButtonData.CANCEL_CLOSE);
            this.buttons.add (this.cancel);
            this.getCancelButton ().addEventFilter (ActionEvent.ACTION, event -> {
                if (this.onCancel ())
                    this.close (Boolean.FALSE);
                else
                    event.consume ();
            });
        }
    }


    /**
     * Get the OK button.
     *
     * @return The OK button or null if not set
     */
    protected final Button getOkButton ()
    {
        return this.buttonNodes.get (this.ok);
    }


    /**
     * Get the Cancel button.
     *
     * @return The Cancel button or null if not set
     */
    protected Button getCancelButton ()
    {
        return this.buttonNodes.get (this.cancel);
    }


    /**
     * Overwrite this function to create and add the widgets of the dialog.
     *
     * @return The panel that should be set as the content-pane
     */
    protected abstract Pane init ();


    /**
     * Show the dialog. Returns a future that completes when close() is called.
     *
     * @return The future to wait for the dialog completion
     */
    public CompletableFuture<Boolean> display ()
    {
        Platform.runLater (this::show);
        this.future = new CompletableFuture<> ();
        return this.future;
    }


    /**
     * Overwrite this function to set the widgets of the dialog to the correct values.
     */
    protected void set ()
    {
        // Intentionally empty
    }


    /**
     * Overwrite this function to read the data from the widgets.
     *
     * @return If true the dialog is closed
     */
    protected boolean onOk ()
    {
        return true;
    }


    /**
     * Overwrite this function to do additional things if dialog is aborted.
     *
     * @return If true the dialog is closed
     */
    protected boolean onCancel ()
    {
        return true;
    }


    /**
     * Close the dialog with a result.
     *
     * @param result The result to set FALSE if cancelled, TRUE if confirmed
     */
    private void close (final Boolean result)
    {
        Platform.runLater (() -> {
            this.removeOverlay ();
            this.future.complete (result);
        });
    }


    private void show ()
    {
        final Scene scene = this.owner.getScene ();
        if (scene == null)
            throw new IllegalStateException ("Owner has no scene");

        final Parent root = scene.getRoot ();

        // Ensure we have a StackPane at the top so we can layer an overlay.
        StackPane host;
        if (root instanceof final StackPane sp)
            host = sp;
        else
        {
            this.originalRoot = root;
            host = new StackPane (root);
            this.injectedRoot = host;
            scene.setRoot (host);
        }

        // Dim + click-blocking layer
        this.overlay = new StackPane (this.content);
        this.overlay.setStyle ("-fx-background-color: rgba(0,0,0,0.35);");
        this.overlay.prefWidthProperty ().bind (host.widthProperty ());
        this.overlay.prefHeightProperty ().bind (host.heightProperty ());

        // Swallow all mouse/scroll input directed at the dim area
        this.overlay.addEventFilter (MouseEvent.ANY, e -> {
            if (e.getTarget () == this.overlay)
                e.consume ();
        });

        // ESC cancels
        this.overlay.addEventFilter (KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode () == KeyCode.ESCAPE && this.onCancel ())
            {
                this.close (Boolean.FALSE);
                e.consume ();
            }
        });

        host.getChildren ().add (this.overlay);
        this.content.requestFocus ();
    }


    private void removeOverlay ()
    {
        if (this.overlay == null)
            return;

        if (this.overlay.getParent () instanceof final Pane host)
            host.getChildren ().remove (this.overlay);

        // Restore original root if we had wrapped it
        if (this.injectedRoot != null && this.originalRoot != null)
        {
            this.injectedRoot.getChildren ().clear ();
            this.owner.getScene ().setRoot (this.originalRoot);
            this.injectedRoot = null;
            this.originalRoot = null;
        }
        this.overlay = null;
    }


    private static Button createButton (final ButtonType buttonType)
    {
        final Button button = new Button (buttonType.getText ());
        final ButtonData buttonData = buttonType.getButtonData ();
        ButtonBar.setButtonData (button, buttonData);
        button.setDefaultButton (buttonData.isDefaultButton ());
        button.setCancelButton (buttonData.isCancelButton ());
        return button;
    }


    private static Label createTitleBar (final String title)
    {
        final Label titleLabel = new Label (title);
        titleLabel.setMaxWidth (Double.MAX_VALUE);
        // Use the platform's default system font (weight bold, slightly larger) and a subtle
        // divider line below. No move cursor, no drag handlers.
        titleLabel.setStyle ("-fx-font-family: '-fx-system';-fx-font-size: 1.15em;-fx-font-weight: bold;-fx-text-fill: -fx-text-base-color;-fx-padding: 0 0 8 0;-fx-border-color: transparent transparent derive(-fx-background, -15%) transparent;-fx-border-width: 0 0 1 0;");
        titleLabel.setCursor (javafx.scene.Cursor.DEFAULT);
        VBox.setMargin (titleLabel, new Insets (0, 0, 12, 0));
        return titleLabel;
    }


    private ButtonBar createButtonBar ()
    {
        final ButtonBar buttonBar = new ButtonBar ();
        buttonBar.setMaxWidth (Double.MAX_VALUE);
        this.updateButtons (buttonBar);
        return buttonBar;
    }


    private void updateButtons (final ButtonBar buttonBar)
    {
        buttonBar.getButtons ().clear ();

        boolean hasDefault = false;
        for (final ButtonType cmd: this.buttons)
        {
            final Button button = this.buttonNodes.get (cmd);
            if (button == null)
                continue;

            // Keep only first default button
            final ButtonData buttonType = cmd.getButtonData ();
            button.setDefaultButton (!hasDefault && buttonType != null && buttonType.isDefaultButton ());
            button.setCancelButton (buttonType != null && buttonType.isCancelButton ());
            hasDefault |= buttonType != null && buttonType.isDefaultButton ();
            buttonBar.getButtons ().add (button);
        }
    }
}