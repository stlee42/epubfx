package de.machmireinebook.epubeditor.epublib.domain.epub3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import de.machmireinebook.epubeditor.epublib.resource.ResourceReference;

/**
 * User: Michail Jungierek
 * Date: 18.05.2018
 * Time: 20:45
 */
public class Landmarks implements Iterable<LandmarkReference>
{
    private Map<LandmarkReference.Semantic, LandmarkReference> references = new HashMap<>();
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Collection<LandmarkReference> getReferences() {
        return references.values();
    }

    public ResourceReference addReference(LandmarkReference reference) {
        this.references.put(reference.getType(), reference);
        return reference;
    }

    /**
     * A list of all LandmarkReferences that have the given referenceTypeName (ignoring case).
     *
     * @param referenceType
     * @return A list of all referenceType that have the given referenceType (ignoring case).
     */
    public List<LandmarkReference> getLandmarkReferencesByType(LandmarkReference.Semantic referenceType) {
        List<LandmarkReference> result = new ArrayList<>();
        for (LandmarkReference reference: references.values()) {
            if (referenceType.equals(reference.getType())) {
                result.add(reference);
            }
        }
        return result;
    }

    @NotNull
    @Override
    public Iterator<LandmarkReference> iterator() {
        return references.values().iterator();
    }

    public boolean isEmpty() {
        return references.isEmpty();
    }
}
