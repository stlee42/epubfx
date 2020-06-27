package de.machmireinebook.epubeditor.epublib.resource;

import de.machmireinebook.epubeditor.epublib.domain.MediaType;

/**
 * User: mjungierek
 * Date: 01.09.2014
 * Time: 18:39
 */
public interface ResourceFactory<T extends Resource<S>, S>
{
    T createResource();
    T createResource(String href);
    T createResource(byte[] data, String href);
    T createResource(String id, byte[] data, String href);
    T createResource(String id, byte[] data, String href, MediaType mediaType);
    T createResource(byte[] data, String href, MediaType mediaType);
}
