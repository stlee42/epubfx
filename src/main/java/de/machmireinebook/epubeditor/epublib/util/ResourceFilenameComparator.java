package de.machmireinebook.epubeditor.epublib.util;

import java.util.Comparator;

import de.machmireinebook.epubeditor.epublib.resource.Resource;

/**
 * User: mjungierek
 * Date: 23.12.2014
 * Time: 02:05
 */
public class ResourceFilenameComparator implements Comparator<Resource>
{
    @Override
    public int compare(Resource res1, Resource res2)
    {
        return res1.getFileName().compareTo(res2.getFileName());
    }
}

