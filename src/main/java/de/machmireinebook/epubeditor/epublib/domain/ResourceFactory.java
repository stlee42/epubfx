package de.machmireinebook.epubeditor.epublib.domain;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 18:39
 */
public interface ResourceFactory
{
    Resource createResource();
    Resource createResource(String href);
    Resource createResource(byte[] data, String href);
    Resource createResource(String id, byte[] data, String href);
    Resource createResource(byte[] data, String href, MediaType mediaType);
}
