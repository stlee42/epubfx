package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.util.ArrayList;
import java.util.List;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Spine;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;
import de.machmireinebook.epubeditor.epublib.epub.BookProcessor;

import org.apache.commons.lang.StringUtils;

/**
 * Removes Sections from the page flow that differ only from the previous section's href by the '#' in the url.
 * 
 * @author paul
 *
 */
public class SectionHrefSanityCheckBookProcessor implements BookProcessor {

	@Override
	public Book processBook(Book book) {
		book.getSpine().setSpineReferences(checkSpineReferences(book.getSpine()));
		return book;
	}

    @Override
    public Resource processResource(Resource resource)
    {
        return resource;
    }

    private static List<SpineReference> checkSpineReferences(Spine spine) {
		List<SpineReference> result = new ArrayList<SpineReference>(spine.size());
		Resource previousResource = null;
		for(SpineReference spineReference: spine.getSpineReferences()) {
			if(spineReference.getResource() == null
					|| StringUtils.isBlank(spineReference.getResource().getHref())) {
				continue;
			}
			if(previousResource == null
					|| spineReference.getResource() == null
					|| ( ! (spineReference.getResource().getHref().equals(previousResource.getHref())))) {
				result.add(spineReference);
			}
			previousResource = spineReference.getResource();
		}
		return result;
	}
}
