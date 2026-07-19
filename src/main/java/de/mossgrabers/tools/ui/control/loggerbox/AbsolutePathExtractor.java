// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui.control.loggerbox;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Utility class for extracting an absolute file path from a free text line. Uses File.exists() to
 * validate candidates, handling paths with spaces.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public final class AbsolutePathExtractor
{
    // Permissive regular expression - captures anything that looks like an absolute path including
    // those with spaces and special characters
    private static final Pattern ABSOLUTE_PATH = Pattern.compile ("(?i)" + // case-insensitive
            "(?:[a-z]:[\\\\/]" + // Windows drive root, e.g. C:\ or C:/
            "|(?:\\\\\\\\|/)" + // UNC (\\) or POSIX (/) root
            ").*" // anything after the root
    );


    /**
     * Constructor. Private due to helper class.
     */
    private AbsolutePathExtractor ()
    {
        // Intentionally empty
    }


    /**
     * Extracts the first absolute file path that exists on the file-system.
     * <p>
     * Algorithm:
     * <ol>
     * <li>Find a candidate absolute path in the text using regular expression</li>
     * <li>If the candidate (or a shortened version) exists on disk, return it</li>
     * <li>Shorten by removing the last path component (everything after the last separator)</li>
     * <li>Repeat until a valid path is found or nothing remains</li>
     * </ol>
     *
     * @param line the text that may contain a path (may be {@code null})
     * @return an {@link Optional} containing the path if one is found, or an empty {@link Optional}
     *         if no absolute path is present
     */
    public static Optional<File> extractAbsolutePath (final String line)
    {
        if (line == null || line.isBlank ())
            return Optional.empty ();

        final Matcher matcher = ABSOLUTE_PATH.matcher (line);
        if (!matcher.find ())
            return Optional.empty ();

        String candidate = matcher.group ();

        // Remove any trailing whitespace that might have been captured
        candidate = stripTrailingQuotes (candidate.trim ());

        // Try progressively shorter paths until we find one that exists
        while (candidate != null && !candidate.isEmpty ())
        {
            // Skip lone drive letters like "C:" - not a valid path
            if (isValidPath (candidate))
            {
                final File file = new File (candidate);
                if (file.exists ())
                    return Optional.of (file);
            }

            // Shorten: remove everything after the last separator (space, slash, or backslash)
            candidate = shortenByOneComponent (candidate);
        }

        return Optional.empty ();
    }


    /**
     * Checks if the candidate looks like a valid absolute path (at least structurally).
     */
    private static boolean isValidPath (final String path)
    {
        if (path == null || path.isEmpty ())
            return false;

        // Must start with a drive letter, UNC, or root slash
        final boolean startsWithDrive = path.matches ("(?i)[a-z]:.*");
        final boolean startsWithUnc = path.startsWith ("\\\\") || path.startsWith ("//");
        final boolean startsWithRoot = path.startsWith ("/") || path.startsWith ("\\");

        if (!startsWithDrive && !startsWithUnc && !startsWithRoot)
            return false;

        // Reject lone drive letters like "C:" or "D:"
        return !path.matches ("(?i)[a-z]:\\s*") && !path.matches ("(?i)[a-z]:$");
    }


    /**
     * Shortens the path by removing the last component. Considers spaces, forward slashes, and
     * backslashes as separators.
     *
     * @return the shortened path, or null if it cannot be shortened further
     */
    private static String shortenByOneComponent (final String path)
    {
        if (path == null || path.isEmpty ())
            return null;

        // Find the last occurrence of any separator (space, slash, backslash)
        int lastSep = -1;
        for (int i = path.length () - 1; i >= 0; i--)
        {
            final char c = path.charAt (i);
            if (c == '/' || c == '\\' || c == ' ')
            {
                lastSep = i;
                break;
            }
        }

        // No separator found - cannot shorten
        if (lastSep <= 0)
            return null;

        // Return everything up to (but not including) the separator
        // Then trim to remove any extra whitespace
        return path.substring (0, lastSep).trim ();
    }


    /**
     * Removes trailing single-quote ({@code '}) and double-quote ({@code "}) characters from the
     * path candidate.
     *
     * @param path The raw candidate
     * @return The candidate with any quotes removed and re-trimmed
     */
    private static String stripTrailingQuotes (final String path)
    {
        String cleanedPath = path;
        int indexOf = path.indexOf ("'");
        if (indexOf >= 0)
            cleanedPath = cleanedPath.substring (0, indexOf);
        indexOf = path.indexOf ("\"");
        if (indexOf >= 0)
            cleanedPath = cleanedPath.substring (0, indexOf);
        return cleanedPath.trim ();
    }
}