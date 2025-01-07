// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui.control.loggerbox;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * The actual log.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class LoggerBoxLog
{
    private final int                      maximumLogEntries;
    private final BlockingDeque<LogRecord> log;
    private LogRecord                      previousRecord = null;


    /**
     * Constructor.
     *
     * @param maximumLogEntries The maximum number of entries to add to the log. Oldest entries will
     *            be removed if that number is reached.
     */
    public LoggerBoxLog (final int maximumLogEntries)
    {
        this.maximumLogEntries = maximumLogEntries;
        this.log = new LinkedBlockingDeque<> (maximumLogEntries);
    }


    /**
     * Get the maximum number of entries in the log.
     *
     * @return The maximum number
     */
    public int getMaximumLogEntries ()
    {
        return this.maximumLogEntries;
    }


    /**
     * Hands over all current log record to the given collection.
     *
     * @param collection The collection to drain the log records to
     */
    public void drainTo (final Collection<? super LogRecord> collection)
    {
        synchronized (this.log)
        {
            this.log.drainTo (collection);
        }
    }


    /**
     * Adds a record to the log.
     *
     * @param record The record
     */
    public void offer (final LogRecord record)
    {
        synchronized (this.log)
        {
            LogRecord rec = record;
            if (record.isCombineWithPrevious () && this.previousRecord != null)
            {
                // Combine with the last log entry
                final LogRecord combinedRecord = new LogRecord (record.getLevel (), this.previousRecord.getMessage () + record.getMessage (), false);
                this.previousRecord.markForRemoval ();

                // Is the previous record still in the log?
                if (!this.log.isEmpty ())
                    this.log.pollLast ();

                rec = combinedRecord;
            }

            this.log.offer (rec);
            this.previousRecord = rec;
        }
    }
}
