// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2024
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;


/**
 * File utility functions.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public final class FileUtils
{
    /**
     * Private due to helper class.
     */
    private FileUtils ()
    {
        // Intentionally empty
    }


    /**
     * Tries to convert the file to a canonical file (unique file).
     *
     * @param file The file to make canonical
     * @return The canonical file or the given file if it could not be converted
     */
    public static File makeCanonical (final File file)
    {
        try
        {
            return file.getCanonicalFile ();
        }
        catch (final IOException ex)
        {
            return file;
        }
    }


    /**
     * Gets the name of the file without the ending. E.g. the filename 'aFile.jpeg' will return
     * 'aFile'.
     *
     * @param file The file from which to get the name
     * @return The name of the file without the ending
     */
    public static String getNameWithoutType (final File file)
    {
        return getNameWithoutType (file.getName ());
    }


    /**
     * Gets the name of the file without the ending. E.g. the filename 'aFile.jpeg' will return
     * 'aFile'.
     *
     * @param filename The file name
     * @return The name of the file without the ending
     */
    public static String getNameWithoutType (final String filename)
    {
        final int pos = filename.lastIndexOf ('.');
        return pos == -1 ? filename : filename.substring (0, pos);
    }


    /**
     * Reads a text file in UTF8 encoding into a string.
     *
     * @param file The file to read
     * @return The content of the file
     * @throws IOException Something crashed
     */
    public static String readUTF8 (final File file) throws IOException
    {
        final String text = Files.readString (file.toPath ());

        // UTF-8 BOM might not be automatically removed
        return text.length () > 0 && text.charAt (0) == '\uFEFF' ? text.substring (1) : text;
    }


    /**
     * Creates a DOS file name with a maximum number of 8 characters. Adds numbers to make it unique
     * among the given other file names.
     *
     * @param filename The filename to shorten
     * @param createdNames Prevent conflicts with these file names
     * @return The unique DOS file name
     */
    public static String createDOSFileName (final String filename, final Set<String> createdNames)
    {
        String dosFilename = filename.toUpperCase ().replace (' ', '_');
        if (dosFilename.length () > 8)
            dosFilename = dosFilename.substring (0, 8);

        int counter = 1;
        while (createdNames.contains (dosFilename))
        {
            counter++;
            final String counterStr = Integer.toString (counter);
            dosFilename = dosFilename.substring (0, 8 - counterStr.length ()) + counterStr;
        }

        createdNames.add (dosFilename);

        return dosFilename;
    }
}
