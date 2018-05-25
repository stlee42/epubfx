package de.machmireinebook.epubeditor.epublib.domain.epub3;

import java.util.ArrayList;
import java.util.List;

import de.machmireinebook.epubeditor.epublib.domain.ResourceReference;

/**
 * User: Michail Jungierek
 * Date: 18.05.2018
 * Time: 20:45
 */
public class Landmarks
{
    private List<LandmarkReference> references = new ArrayList<>();

    public ResourceReference addReference(LandmarkReference reference) {
        this.references.add(reference);
        return reference;
    }

    /**
     * A list of all LandmarkReferences that have the given referenceTypeName (ignoring case).
     *
     * @param referenceType
     * @return A list of all referenceType that have the given referenceType (ignoring case).
     */
    public List<LandmarkReference> getLandmarkReferencesByType(LandmarkReference.Semantics referenceType) {
        List<LandmarkReference> result = new ArrayList<>();
        for (LandmarkReference reference: references) {
            if (referenceType.equals(reference.getType())) {
                result.add(reference);
            }
        }
        return result;
    }
}