// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools;

/**
 * Some helper functions to deal with strings.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class StringUtils
{
    private static final String  COMMA_SPLIT     = ",";
    private static final char [] REMOVABLE_CHARS =
    {
        ' ',
        'e',
        'a',
        'u',
        'i',
        'o'
    };


    /**
     * Constructor. Private due to utility class.
     */
    private StringUtils ()
    {
        // Intentionally empty
    }


    /**
     * Splits a string by all commas.
     *
     * @param text The text to split
     * @return The split parts
     */
    public static String [] splitByComma (final String text)
    {
        return split (text, COMMA_SPLIT);
    }


    /**
     * Splits a string with the String split function but returns an empty list in case of null as
     * well as the given text is an empty text or contains only whitespace.
     *
     * @param text The text to split
     * @param regex The regular expression for splitting
     * @return The split parts
     */
    public static String [] split (final String text, final String regex)
    {
        if (text == null)
            return new String [0];
        return text.split (regex);
    }


    /**
     * Pad a string with spaces at the left.
     *
     * @param text The text to pad
     * @param length The number of spaces to add to the left of the string
     * @return The padded string
     */
    public static String padLeftSpaces (final String text, final int length)
    {
        final String format = String.format ("%%%ds", Integer.valueOf (text.length () + length));
        return String.format (format, text);
    }


    /**
     * Pad a string with spaces at the end up to the given length. Also shortens the string if it is
     * longer than the given length.
     *
     * @param text The text to right pad
     * @param length The length
     * @return The padded string
     */
    public static String rightPadSpaces (final String text, final int length)
    {
        if (text.length () > length)
            return text.substring (0, length);
        final String formatString = "%1$-" + length + "s";
        return String.format (formatString, text);
    }


    /**
     * Shortens a text to the given length.
     *
     * @param text The text to shorten
     * @param length The length to shorten to
     * @return The shortened text
     */
    public static String optimizeName (final String text, final int length)
    {
        if (text == null)
            return "";

        String shortened = text;
        for (final char element: REMOVABLE_CHARS)
        {
            if (shortened.length () <= length)
                return shortened;
            int pos;
            while ((pos = shortened.indexOf (element)) != -1)
            {
                shortened = shortened.substring (0, pos) + shortened.substring (pos + 1, shortened.length ());
                if (shortened.length () <= length)
                    return shortened;
            }
        }
        return shortened.length () <= length ? shortened : shortened.substring (0, length);
    }


    /**
     * Replace umlauts and other non-ASCII characters with alternative writing.
     *
     * @param text The string to check
     * @return The string with replaced characters, might be longer than the original!
     */
    public static String fixASCII (final String text)
    {
        if (text == null)
            return "";
        final StringBuilder str = new StringBuilder ();
        for (int i = 0; i < text.length (); i++)
        {
            final char c = text.charAt (i);
            if (c > 127)
                switch (c)
                {
                    case 'Ä':
                        str.append ("Ae");
                        break;
                    case 'ä':
                        str.append ("ae");
                        break;
                    case 'Ö', '\u0152':
                        str.append ("Oe");
                        break;
                    case 'ö', '\u0153':
                        str.append ("oe");
                        break;
                    case 'Ü':
                        str.append ("Ue");
                        break;
                    case 'ü':
                        str.append ("ue");
                        break;
                    case 'ß':
                        str.append ("ss");
                        break;
                    case 'é':
                        str.append ("e");
                        break;
                    case '→':
                        str.append ("->");
                        break;
                    case '♯':
                        str.append ("#");
                        break;
                    default:
                        str.append ("?");
                        break;
                }
            else
                str.append (c);
        }
        return str.toString ();
    }


    /**
     * Removes characters after the first null byte.
     *
     * @param text The text
     * @return The text before the first null byte
     */
    public static String removeCharactersAfterZero (final String text)
    {
        final String [] split = text.split ("\0");
        return split.length == 0 ? "" : split[0];
    }


    /**
     * Formats a byte array as one line.
     * 
     * @param data The data to format
     * @return The formatted data
     */
    public static String formatArray (final byte [] data)
    {
        if (data.length == 0)
            return "[]";

        final StringBuilder sb = new StringBuilder ("[ ");
        for (int i = 0; i < data.length; i++)
        {
            if (i > 0)
                sb.append (", ");
            sb.append (data[i]);
        }
        return sb.append (" ]").toString ();
    }


    /**
     * Convert the bytes to a hex string
     *
     * @param data The data to convert
     * @return The hex string
     */
    public static String formatHexStr (final byte [] data)
    {
        final StringBuilder sb = new StringBuilder ();
        for (final byte d: data)
        {
            if (!sb.isEmpty ())
                sb.append (' ');
            sb.append (formatHexStr (Byte.toUnsignedInt (d)));
        }
        return sb.toString ();
    }


    /**
     * Convert the byte to a hex string
     *
     * @param number The value to convert
     * @return The hex string
     */
    public static String formatHexStr (final int number)
    {
        return String.format ("%02X", Integer.valueOf (number));
    }


    /**
     * Formats an integer value as decimal and hex.
     * 
     * @param value The value to format
     * @return The formatted value
     */
    public static String formatDataValue (final int value)
    {
        final Integer valueObj = Integer.valueOf (value);
        return String.format ("%d (0x%02X)", valueObj, valueObj);
    }
}
