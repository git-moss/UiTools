// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


/**
 * Brute force implementation to fix tabulator traversal order. The widgets are traversed in the
 * order at which they are added.
 *
 * @author Jürgen Moßgraber
 */
public class TraversalManager
{
    private final List<Node> widgets = new ArrayList<> ();


    /**
     * Register the manager to a JavaFX stage.
     *
     * @param stage The stage to which to register the manager
     */
    public void register (final Stage stage)
    {
        stage.addEventFilter (KeyEvent.KEY_PRESSED, this::handleKey);
    }


    /**
     * Add a widget to the traversal.
     *
     * @param widget The widget to add
     */
    public void add (final Node widget)
    {
        this.widgets.add (widget);
    }


    /**
     * Add all child-widgets of the given parent to the traversal.
     *
     * @param parent The parent
     */
    public void addChildren (final Parent parent)
    {
        if (parent instanceof final ScrollPane scrollPane)
        {
            final Node content = scrollPane.getContent ();
            if (content instanceof final Parent parentContent)
                this.addChildren (parentContent);
            else
                this.add (content);
        }
        else
            for (final Node node: parent.getChildrenUnmodifiable ())
                if (node instanceof ButtonBase || node instanceof TextInputControl || node instanceof ComboBoxBase<?>)
                    this.widgets.add (node);
                else if (node instanceof final Parent childParent)
                    this.addChildren (childParent);
    }


    private void handleKey (final KeyEvent keyEvent)
    {
        if (keyEvent.getCode ().equals (KeyCode.TAB) && keyEvent.getTarget () instanceof final Node node)
        {
            int index = this.widgets.indexOf (node);
            if (index < 0)
            {
                if (!this.widgets.isEmpty ())
                    this.widgets.get (0).requestFocus ();
            }
            else
            {
                final int direction = keyEvent.isShiftDown () ? -1 : 1;
                final int size = this.widgets.size ();
                final int start = index;

                Node widget;
                do
                {
                    index = index + direction;
                    if (index >= size)
                        index = 0;
                    else if (index < 0)
                        index = size - 1;
                    widget = this.widgets.get (index);
                } while ((widget.isDisabled () || isWidgetHidden (widget)) && start != index);

                widget.requestFocus ();
            }

            keyEvent.consume ();
        }
    }


    private static boolean isWidgetHidden (final Node widget)
    {
        // Is visible is only a property in JavaFX and might not be set!
        if (!widget.isVisible ())
            return true;

        // Check if the widget is in a hidden tab
        Parent parent = widget.getParent ();
        while (parent != null)
        {
            if (!parent.isVisible ())
                return true;

            if (parent instanceof final TabPane tabPane)
            {
                // The widget is in a Tab check if it is the selected one
                final Tab selectedTab = tabPane.getSelectionModel ().getSelectedItem ();
                if (selectedTab == null || !findWidget (selectedTab.getContent (), widget))
                    return true;
            }

            parent = parent.getParent ();
        }

        return false;
    }


    private static boolean findWidget (final Node content, final Node widget)
    {
        if (content == widget)
            return true;

        if (content instanceof final Parent parent)
            for (final Node child: parent.getChildrenUnmodifiable ())
                if (findWidget (child, widget))
                    return true;

        return false;
    }
}
