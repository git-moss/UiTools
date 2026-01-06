// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Dialog;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * Manages program-settings. Adds some useful properties that are normally needed in a GUI program.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class BasicConfig
{
    private static final String MAIN        = "Main";
    private static final String WINDOW      = "Window";
    private static final char   H           = 'H';
    private static final char   W           = 'W';
    private static final char   Y           = 'Y';
    private static final char   X           = 'X';
    private static final String MAXIMIZED   = "Maximized";
    private static final String ACTIVE_PATH = "ActivePath";

    private final Preferences   preferences;


    /**
     * Constructor.
     *
     * @param path The top path to use for storing the configuration properties.
     */
    public BasicConfig (final String path)
    {
        this.preferences = Preferences.userRoot ().node (path);
    }


    /**
     * Stores the window position, dimension and state of an applications mainframe. Only stores the
     * information if the frame is visible.
     *
     * @param stage The main stage of an application
     */
    public void storeStagePlacement (final Stage stage)
    {
        this.putWindowPlacement (MAIN, -1, stage);
        this.preferences.putBoolean (MAIN + MAXIMIZED, stage.isMaximized ());
    }


    /**
     * Restores the window position and dimension of an applications main stage.
     *
     * @param stage The main stage of an application
     */
    public void restoreStagePlacement (final Stage stage)
    {
        this.restoreWindowPlacement (MAIN, stage);
        stage.setMaximized (this.preferences.getBoolean (MAIN + MAXIMIZED, false));
    }


    /**
     * Restores the window position and dimension.
     *
     * @param name The name of the window
     * @param window The window to restore
     */
    public void restoreWindowPlacement (final String name, final Window window)
    {
        this.restoreWindowPlacement (name, -1, window);
    }


    /**
     * Restores the window position and dimension.
     *
     * @param name The name of the window
     * @param id An identifier for the window
     * @param window The window to restore
     */
    public void restoreWindowPlacement (final String name, final int id, final Window window)
    {
        final Rectangle2D bounds = this.getWindowPlacement (name, id);
        if (bounds == null)
            return;

        double x = bounds.getMinX ();
        double y = bounds.getMinY ();
        double w = bounds.getWidth ();
        double h = bounds.getHeight ();

        // Check if it intersects at least 25% with any current screen, if not reset to primary
        // screen center
        final boolean visible = Screen.getScreens ().stream ().map (Screen::getVisualBounds).anyMatch (screen -> isMostlyVisible (bounds, screen, 0.25));
        if (!visible)
        {
            final Rectangle2D primaryBounds = Screen.getPrimary ().getVisualBounds ();
            w = Math.min (w, primaryBounds.getWidth ());
            h = Math.min (h, primaryBounds.getHeight ());
            x = primaryBounds.getMinX () + (primaryBounds.getWidth () - w) / 2;
            y = primaryBounds.getMinY () + (primaryBounds.getHeight () - h) / 2;
        }

        window.setX (x);
        window.setY (y);
        window.setWidth (w);
        window.setHeight (h);
    }


    private static boolean isMostlyVisible (final Rectangle2D window, final Rectangle2D screen, final double minVisibleRatio)
    {
        final double xOverlap = Math.max (0, Math.min (window.getMaxX (), screen.getMaxX ()) - Math.max (window.getMinX (), screen.getMinX ()));
        final double yOverlap = Math.max (0, Math.min (window.getMaxY (), screen.getMaxY ()) - Math.max (window.getMinY (), screen.getMinY ()));
        final double overlapArea = xOverlap * yOverlap;
        final double windowArea = window.getWidth () * window.getHeight ();
        return overlapArea / windowArea >= minVisibleRatio;
    }


    /**
     * Restores the dialog position and dimension.
     *
     * @param name The name of the dialog
     * @param dialog The dialog to restore
     */
    public void restoreDialogPlacement (final String name, final Dialog<?> dialog)
    {
        final Rectangle2D bounds = this.getWindowPlacement (name, -1);
        if (bounds == null)
            return;
        dialog.setX (bounds.getMinX ());
        dialog.setY (bounds.getMinY ());
        dialog.setWidth (bounds.getWidth ());
        dialog.setHeight (bounds.getHeight ());
    }


    /**
     * Get the size of the main window.
     *
     * @param name The name of the window
     * @param id An additional index for the window, e.g. to count child windows
     * @return The size of the window
     */
    public Rectangle2D getWindowPlacement (final String name, final int id)
    {
        final String prop = buildPropertyName (name, id);
        final double width = this.preferences.getDouble (prop + W, 0);
        final double height = this.preferences.getDouble (prop + H, 0);
        return width <= 0 || height <= 0 ? null : new Rectangle2D (this.preferences.getDouble (prop + X, 0), this.preferences.getDouble (prop + Y, 0), width, height);
    }


    /**
     * Store the position and dimensions of a window.
     *
     * @param name The name of the window
     * @param window The window for which to store the dimensions
     */
    public void putWindowPlacement (final String name, final Window window)
    {
        this.putWindowPlacement (name, -1, window);
    }


    /**
     * Store the position and dimensions of a window.
     *
     * @param name The name of the window
     * @param id An identifier for the window
     * @param window The window for which to store the dimensions
     */
    public void putWindowPlacement (final String name, final int id, final Window window)
    {
        final String prop = buildPropertyName (name, id);
        this.preferences.putDouble (prop + X, window.getX ());
        this.preferences.putDouble (prop + Y, window.getY ());
        this.preferences.putDouble (prop + W, window.getWidth ());
        this.preferences.putDouble (prop + H, window.getHeight ());
    }


    /**
     * Builds a window property name from the given name and id.
     *
     * @param name A name
     * @param id An id
     * @return The property name
     */
    private static String buildPropertyName (final String name, final int id)
    {
        final StringBuilder propStr = new StringBuilder (name);
        if (id != -1)
            propStr.append (id);
        return propStr.append (WINDOW).toString ();
    }


    /**
     * Gets the current path of ChangeIt!.
     *
     * @return The current path
     */
    public String getActivePath ()
    {
        return this.preferences.get (ACTIVE_PATH, "");
    }


    /**
     * Set the current path.
     *
     * @param activePath The current path
     */
    public void setActivePath (final String activePath)
    {
        if (activePath != null)
            this.preferences.put (ACTIVE_PATH, activePath);
    }


    /**
     * Set the current path.
     *
     * @param activePath The current path
     */
    public void setActivePath (final File activePath)
    {
        if (activePath != null)
            this.setActivePath (activePath.getAbsolutePath ());
    }


    /**
     * Get a string preference.
     *
     * @param name The name of the preference
     * @return The value or null if not present
     */
    public String getProperty (final String name)
    {
        return this.preferences.get (name, null);
    }


    /**
     * Get a string preference.
     *
     * @param name The name of the preference
     * @param defaultValue The value to return if the preference is not present
     * @return The value or the default value if not present
     */
    public String getProperty (final String name, final String defaultValue)
    {
        return this.preferences.get (name, defaultValue);
    }


    /**
     * Set a string preference.
     *
     * @param name The name of the preference
     * @param value The value to store
     */
    public void setProperty (final String name, final String value)
    {
        this.preferences.put (name, value);
    }


    /**
     * Get a boolean preference.
     *
     * @param name The name of the preference
     * @return The value or false if not present
     */
    public boolean getBoolean (final String name)
    {
        return this.preferences.getBoolean (name, false);
    }


    /**
     * Get a boolean preference.
     *
     * @param name The name of the preference
     * @param defaultValue The value to return if the preference is not present
     * @return The value or the default value if not present
     */
    public boolean getBoolean (final String name, final boolean defaultValue)
    {
        return this.preferences.getBoolean (name, defaultValue);
    }


    /**
     * Set a boolean value.
     *
     * @param name The name of the preference
     * @param value The value to set
     */
    public void setBoolean (final String name, final boolean value)
    {
        this.preferences.putBoolean (name, value);
    }


    /**
     * Get an integer preference.
     *
     * @param name The name of the preference
     * @param defaultValue The value to return if the preference is not present
     * @return The value or the default value if not present
     */
    public int getInteger (final String name, final int defaultValue)
    {
        return this.preferences.getInt (name, defaultValue);
    }


    /**
     * Set an integer preference.
     *
     * @param name The name of the preference
     * @param value The value to set
     */
    public void setInteger (final String name, final int value)
    {
        this.preferences.putInt (name, value);
    }


    /**
     * Store the settings.
     *
     * @throws IOException Error during store
     */
    public void save () throws IOException
    {
        try
        {
            this.preferences.flush ();
        }
        catch (final BackingStoreException ex)
        {
            throw new IOException (ex);
        }
    }
}
