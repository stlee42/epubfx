package de.machmireinebook.epubeditor.epublib;

/**
 * @author Michail Jungierek, CGI
 */
public class NavNotFoundException extends RuntimeException
{
    public NavNotFoundException(String message)
    {
        super(message);
    }
}
