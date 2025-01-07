// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui.control.loggerbox;

/**
 * A record in the log.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class LogRecord
{
    private final LoggerBoxLevel level;
    private final String         message;
    private final boolean        combineWithPrevious;
    private boolean              markForRemoval = false;


    /**
     * Constructor.
     * 
     * @param level The level of the message
     * @param message The message text
     * @param combineWithPrevious Should this message be combined with the previous record?
     */
    public LogRecord (final LoggerBoxLevel level, final String message, final boolean combineWithPrevious)
    {
        this.level = level;
        this.message = message;
        this.combineWithPrevious = combineWithPrevious;
    }


    /**
     * Get the level of the record.
     * 
     * @return The level
     */
    public LoggerBoxLevel getLevel ()
    {
        return this.level;
    }


    /**
     * Get the message text of the record.
     * 
     * @return The text
     */
    public String getMessage ()
    {
        return this.message;
    }


    /**
     * Should this message be combined with the previous record?
     * 
     * @return True if it should be combined
     */
    public boolean isCombineWithPrevious ()
    {
        return this.combineWithPrevious;
    }


    /**
     * Marks this record to be removed.
     */
    public void markForRemoval ()
    {
        this.markForRemoval = true;
    }


    /**
     * Check if this record should be removed from the log.
     * 
     * @return True if it should be removed
     */
    public boolean isMarkedForRemoval ()
    {
        return this.markForRemoval;
    }
}
