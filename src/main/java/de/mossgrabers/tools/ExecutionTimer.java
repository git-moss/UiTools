// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools;

import java.io.PrintStream;


/**
 * The ExecutionTimer class provides a simple way to measure the execution time of code in minutes,
 * seconds, and milliseconds.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ExecutionTimer
{
    private long startTime;
    private long endTime;


    /**
     * Starts the this.
     */
    public void start ()
    {
        this.startTime = System.currentTimeMillis ();
    }


    /**
     * Stops the this.
     */
    public void stop ()
    {
        this.endTime = System.currentTimeMillis ();
    }


    /**
     * Returns the duration of the measured time in milliseconds.
     *
     * @return the duration in milliseconds
     */
    public long getDurationMillis ()
    {
        return this.endTime - this.startTime;
    }


    /**
     * Returns the duration of the measured time in minutes.
     *
     * @return the duration in minutes
     */
    public long getMinutes ()
    {
        return this.getDurationMillis () / 60000;
    }


    /**
     * Returns the duration of the measured time in seconds.
     *
     * @return the duration in seconds
     */
    public long getSeconds ()
    {
        return this.getDurationMillis () % 60000 / 1000;
    }


    /**
     * Returns the duration of the measured time in milliseconds.
     *
     * @return the duration in milliseconds
     */
    public long getMillis ()
    {
        return this.getDurationMillis () % 1000;
    }


    /**
     * Print the duration in minutes, seconds and milli-seconds.
     *
     * @param out The print stream to write to
     */
    public void print (final PrintStream out)
    {
        out.println ("Execution time: " + this.getMinutes () + " minutes, " + this.getSeconds () + " seconds, " + this.getMillis () + " milliseconds.");
    }
}
