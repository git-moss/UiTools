// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui.control.loggerbox;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A component to provide a log output based on a ListView.<br>
 * Can be style with '.log-view .list-cell' and the pseudo classes 'debug', 'info', 'warn' and
 * 'error' on the list-cell.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class LoggerBox extends ListView<LogRecord>
{
    private final static Map<LoggerBoxLevel, PseudoClass> PSEUDO_CLASSES = new HashMap<> ();
    static
    {
        PSEUDO_CLASSES.put (LoggerBoxLevel.DEBUG, PseudoClass.getPseudoClass ("debug"));
        PSEUDO_CLASSES.put (LoggerBoxLevel.INFO, PseudoClass.getPseudoClass ("info"));
        PSEUDO_CLASSES.put (LoggerBoxLevel.WARN, PseudoClass.getPseudoClass ("warn"));
        PSEUDO_CLASSES.put (LoggerBoxLevel.ERROR, PseudoClass.getPseudoClass ("error"));
    }

    private final ObjectProperty<LoggerBoxLevel> filterLevel      = new SimpleObjectProperty<> (null);
    private final BooleanProperty                autoScrollToTail = new SimpleBooleanProperty (false);
    private final BooleanProperty                paused           = new SimpleBooleanProperty (false);
    private final DoubleProperty                 refreshRate      = new SimpleDoubleProperty (60);
    private final ObservableList<LogRecord>      logItems         = FXCollections.observableArrayList ();
    private final int                            maximumLogEntries;


    /**
     * Constructor.
     *
     * @param logger The logger to use
     */
    public LoggerBox (final LoggerBoxLogger logger)
    {
        this.maximumLogEntries = logger.getLog ().getMaximumLogEntries ();

        this.getSelectionModel ().setSelectionMode (SelectionMode.MULTIPLE);
        this.getStyleClass ().add ("log-view");

        // Use fixed row heights calculated from an example text
        // Otherwise ListView prints errors to the console
        final Text text = new Text ("Sample Text");
        text.getStyleClass ().add ("log-view");
        text.applyCss ();
        final double textHeight = text.getLayoutBounds ().getHeight ();
        this.setFixedCellSize (2 * textHeight);

        final Timeline logTransfer = new Timeline (new KeyFrame (Duration.seconds (1), event -> this.updateFromLog (logger)));
        logTransfer.setCycleCount (Animation.INDEFINITE);
        logTransfer.rateProperty ().bind (this.refreshRateProperty ());

        this.pausedProperty ().addListener ( (observable, oldValue, newValue) -> {
            final boolean booleanValue = newValue.booleanValue ();
            if (booleanValue && logTransfer.getStatus () == Animation.Status.RUNNING)
                logTransfer.pause ();

            if (!booleanValue && logTransfer.getStatus () == Animation.Status.PAUSED && this.getParent () != null)
                logTransfer.play ();
        });

        this.parentProperty ().addListener ( (observable, oldValue, newValue) -> {
            if (newValue == null)
                logTransfer.pause ();
            else if (!this.paused.get ())
                logTransfer.play ();
        });

        this.filterLevel.addListener ( (observable, oldValue, newValue) -> {
            this.setItems (new FilteredList<> (this.logItems, logRecord -> logRecord.getLevel ().ordinal () >= this.filterLevel.get ().ordinal ()));
        });
        this.filterLevel.set (LoggerBoxLevel.DEBUG);

        this.setCellFactory (param -> new ListCell<LogRecord> ()
        {
            /** {@inheritDoc} */
            @Override
            protected void updateItem (final LogRecord item, final boolean empty)
            {
                super.updateItem (item, empty);

                for (final PseudoClass clazz: PSEUDO_CLASSES.values ())
                    this.pseudoClassStateChanged (clazz, false);

                if (item == null || empty)
                {
                    this.setText (null);
                    return;
                }

                this.setText (item.getMessage ());
                this.pseudoClassStateChanged (PSEUDO_CLASSES.get (item.getLevel ()), true);
            }
        });

        this.createContextMenu ();
    }


    /**
     * Removes all messages.
     */
    public void clear ()
    {
        this.logItems.clear ();
    }


    /**
     * Property to for the filtering by the level.
     *
     * @return The property
     */
    public ObjectProperty<LoggerBoxLevel> filterLevelProperty ()
    {
        return this.filterLevel;
    }


    /**
     * Property to control to automatically scroll to the end of the log.
     *
     * @return The property
     */
    public BooleanProperty autoScrollToTailProperty ()
    {
        return this.autoScrollToTail;
    }


    /**
     * Property to pause updating the output.
     *
     * @return The property
     */
    public BooleanProperty pausedProperty ()
    {
        return this.paused;
    }


    /**
     * Property to control the refresh rate of the output.
     *
     * @return The property
     */
    public DoubleProperty refreshRateProperty ()
    {
        return this.refreshRate;
    }


    private void updateFromLog (final LoggerBoxLogger logger)
    {
        // If the last item does not end with a return, remove it since it gets updated
        if (!this.logItems.isEmpty () && this.logItems.getLast ().isMarkedForRemoval ())
            this.logItems.removeLast ();

        final List<LogRecord> newItems = new ArrayList<> ();
        logger.getLog ().drainTo (newItems);

        final int size = newItems.size ();
        for (int i = 0; i < size; i++)
        {
            final LogRecord logRecord = newItems.get (i);
            if (!logRecord.isMarkedForRemoval () || i == size - 1)
                this.logItems.add (logRecord);
        }

        if (this.logItems.size () > this.maximumLogEntries)
            this.logItems.remove (0, this.logItems.size () - this.maximumLogEntries);

        if (this.autoScrollToTail.get ())
            this.scrollTo (this.logItems.size ());
    }


    private void createContextMenu ()
    {
        // Create the context menu
        final ContextMenu contextMenu = new ContextMenu ();
        final MenuItem copyItem = new MenuItem ("Copy selected rows to clipboard");
        contextMenu.getItems ().add (copyItem);

        copyItem.setOnAction (event -> {

            final StringBuilder concatenatedText = new StringBuilder ();
            this.getSelectionModel ().getSelectedItems ().forEach (item -> concatenatedText.append (item.getMessage ()).append ("\n"));
            final Clipboard clipboard = Clipboard.getSystemClipboard ();
            final ClipboardContent content = new ClipboardContent ();
            content.putString (concatenatedText.toString ().trim ());
            clipboard.setContent (content);

        });

        // Show context menu on right click
        this.setOnMouseClicked (event -> {
            if (event.getButton () == MouseButton.SECONDARY)
                contextMenu.show (this, event.getScreenX (), event.getScreenY ());
        });
    }
}
