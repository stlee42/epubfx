package de.machmireinebook.epubeditor.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 * User: mjungierek
 * Date: 17.11.13
 * Time: 18:33
 */
public class NumberUtils
{
    private static final DecimalFormat currencyFormat = new DecimalFormat("###,###,##0.00", DecimalFormatSymbols.getInstance(Locale.GERMANY));
    private static final DecimalFormat oneDecimalFormat = new DecimalFormat("##0.#");
    private static final DecimalFormat integerFormat = new DecimalFormat("###,###,##0");
    private static final DecimalFormat doubleFormat = new DecimalFormat("###,###,###.###########", DecimalFormatSymbols.getInstance(Locale.GERMANY));

    public static Integer toIntWithNull(String str, Integer defaultValue)
    {
        if(StringUtils.isEmpty(str))
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException nfe)
        {
            try
            {
                return integerFormat.parse(str).intValue();
            }
            catch (ParseException e)
            {
                return defaultValue;
            }
        }
    }

    public static Long toLongWithSeprator(String str, Long defaultValue)
    {
        str = str.trim();
        if(StringUtils.isEmpty(str))
        {
            return defaultValue;
        }
        try
        {
            return (Long) integerFormat.parse(str);
        }
        catch (ParseException nfe)
        {
            return defaultValue;
        }
    }

    public static String formatAsCurrency(Number value)
    {
        if (value == null)
        {
            return "";
        }
        return currencyFormat.format(value);
    }

    public static String formatWithOneDecimal(Float value)
    {
        if (value == null)
        {
            return "";
        }
        return oneDecimalFormat.format(value);
    }

    public static String formatWithOneDecimal(Double value)
    {
        if (value == null)
        {
            return "";
        }
        return oneDecimalFormat.format(value);
    }

    public static String formatAsInteger(Number value)
    {
        if (value == null)
        {
            return "";
        }
        return integerFormat.format(value);
    }

    public static Double parseCurrency(String value) throws ParseException
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        return currencyFormat.parse(value).doubleValue();
    }

    public static Double parseDouble(String value) throws ParseException
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        return doubleFormat.parse(value).doubleValue();
    }

    public static String formatDouble(Number value)
    {
        if (value == null)
        {
            return "0";
        }
        return doubleFormat.format(value);
    }

    public static double toDouble(BigDecimal bigDecimal)
    {
        double result = 0.0;
        if (bigDecimal != null)
        {
            result = bigDecimal.doubleValue();
        }
        return result;
    }

    public static long toLong(BigInteger bigInteger)
    {
        long result = 0;
        if (bigInteger != null)
        {
            result = bigInteger.longValue();
        }
        return result;
    }

}
