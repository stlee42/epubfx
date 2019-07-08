package de.machmireinebook.epubeditor.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * User: mjungierek
 * Date: 17.11.13
 * Time: 18:33
 */
public class EpubFxNumberUtils
{
    private static final DecimalFormat integerFormat = new DecimalFormat("###,###,##0");
    private static final DecimalFormat doubleFormat = new DecimalFormat("###,###,###.###########", DecimalFormatSymbols.getInstance(Locale.GERMANY));

    public static String formatAsInteger(Number value)
    {
        if (value == null)
        {
            return "";
        }
        return integerFormat.format(value);
    }

    public static String formatDouble(Number value)
    {
        if (value == null)
        {
            return "0";
        }
        return doubleFormat.format(value);
    }
}
