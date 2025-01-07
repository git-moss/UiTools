// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2024
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui.control;

import de.mossgrabers.tools.ui.Functions;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;


/**
 * A titled separator. This is a text followed by a horizontal separator line.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class TitledSeparator extends BorderPane
{
    private final Label     label     = new Label ();
    private final Separator separator = new Separator (Orientation.HORIZONTAL);


    /**
     * Constructor.
     *
     * @param title The text to display as a title
     */
    public TitledSeparator (final String title)
    {
        this.setCenter (this.separator);
        this.setLeft (this.label);
        this.label.setText (Functions.getText (title));
        this.label.getStyleClass ().add ("titled-separator");
        this.label.setMnemonicParsing (true);
    }


    /**
     * Set the label for the given node.
     * 
     * @param node The node
     */
    public void setLabelFor (final Node node)
    {
        this.label.setLabelFor (node);
    }
}
