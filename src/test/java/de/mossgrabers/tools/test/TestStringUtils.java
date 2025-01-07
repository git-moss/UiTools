// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.mossgrabers.tools.StringUtils;

import org.junit.jupiter.api.Test;


/**
 * Test for padding utilities.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
class TestStringUtils
{
    private static final String PADDED_RESULT = "    123456";


    /**
     * Test correct padding output.
     */
    @Test
    void testPadding ()
    {
        assertEquals (PADDED_RESULT, StringUtils.padLeftSpaces ("123456", 4));
    }
}
