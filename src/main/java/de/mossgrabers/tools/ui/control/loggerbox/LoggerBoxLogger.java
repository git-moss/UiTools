// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui.control.loggerbox;

/**
 * A logger to be used with the LoggerBox.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class LoggerBoxLogger
{
    private final LoggerBoxLog log;


    /**
     * Convenient constructor which also creates the log.
     * 
     * @param maximumLogEntries The maximum number of entries to add to the log. Oldest entries will
     *            be removed if that number is reached.
     */
    public LoggerBoxLogger (final int maximumLogEntries)
    {
        this.log = new LoggerBoxLog (maximumLogEntries);
    }


    /**
     * Constructor.
     * 
     * @param log The log to log to
     */
    public LoggerBoxLogger (final LoggerBoxLog log)
    {
        this.log = log;
    }


    /**
     * Log a message with debug level.
     * 
     * @param message The message to log
     */
    public void debug (final String message)
    {
        this.log (LoggerBoxLevel.DEBUG, message, false);
    }


    /**
     * Log a message with info level.
     * 
     * @param message The message to log
     * @param combineWithPrevious Combines the message of the previous record with the new one if
     *            true
     */
    public void info (final String message, final boolean combineWithPrevious)
    {
        this.log (LoggerBoxLevel.INFO, message, combineWithPrevious);
    }


    /**
     * Log a message with warning level.
     * 
     * @param message The message to log
     */
    public void warn (final String message)
    {
        this.log (LoggerBoxLevel.WARN, message, false);
    }


    /**
     * Log a message with error level.
     * 
     * @param message The message to log
     */
    public void error (final String message)
    {
        this.log (LoggerBoxLevel.ERROR, message, false);
    }


    /**
     * Log a record.
     * 
     * @param level The level of the message
     * @param message The message text
     * @param combineWithPrevious Should this message be combined with the previous record?
     */
    public void log (final LoggerBoxLevel level, final String message, final boolean combineWithPrevious)
    {
        if (combineWithPrevious)
            this.log.offer (new LogRecord (level, message, combineWithPrevious));
        else
        {
            // Create separate records for multi-line text
            final String [] lines = message.split ("\n");
            for (int i = 0; i < lines.length; i++)
                this.log.offer (new LogRecord (level, lines[i], false));
        }
    }


    /**
     * Get the log.
     * 
     * @return The log
     */
    public LoggerBoxLog getLog ()
    {
        return this.log;
    }
}
