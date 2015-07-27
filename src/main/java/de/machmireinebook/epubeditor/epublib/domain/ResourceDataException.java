package de.machmireinebook.epubeditor.epublib.domain;

/**
 * User: mjungierek
 * Date: 21.12.2014
 * Time: 14:43
 */
public class ResourceDataException extends RuntimeException
{
    public ResourceDataException()
    {
    }

    public ResourceDataException(Throwable cause)
    {
        super(cause);
    }

    public ResourceDataException(String message)
    {
        super(message);
    }

    public ResourceDataException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ResourceDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
