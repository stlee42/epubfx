package de.machmireinebook.epubeditor.epublib;

/**
 * User: Michail Jungierek
 * Date: 17.07.2019
 * Time: 23:38
 */
public class OpfNotReadableException extends RuntimeException {
    public OpfNotReadableException(String message) {
        super(message);
    }
}
