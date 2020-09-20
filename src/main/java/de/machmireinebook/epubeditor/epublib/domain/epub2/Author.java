package de.machmireinebook.epubeditor.epublib.domain.epub2;

import java.io.Serializable;
import java.text.Normalizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.machmireinebook.epubeditor.epublib.domain.Relator;

/**
 * Represents one of the authors of the book
 * 
 * @author paul
 *
 */
public class Author extends DublinCoreMetadataElement implements Serializable
{
	
	private static final long serialVersionUID = 6663408501416574200L;
	
    private String fileAs;
	private Relator role = Relator.AUTHOR;

	public Author(String id, String name) {
	    super(id, name);
	    if (StringUtils.isEmpty(id)) {
			setId(Normalizer.normalize(name, Normalizer.Form.NFD)
					.replaceAll("[^\\p{ASCII}]", "")
					.replace(" ", "_")
					.toLowerCase());
		}
	}
	
	public String getName() {
		return getValue();
	}
	public void setName(String name) {
		setValue(name);
	}
	
	public String toString() {
		return getValue();
	}
	
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(getValue());
	}
	
	public boolean equals(Object authorObject) {
		if(! (authorObject instanceof Author)) {
			return false;
		}
		Author other = (Author) authorObject;
		return StringUtils.equals(getValue(), other.getValue()) && StringUtils.equals(fileAs, other.fileAs);
	}

	public Relator setRole(String code) {
		Relator result = Relator.byCode(code);
		if (result == null) {
			result = Relator.AUTHOR;
		}
		this.role = result;
		return result;
	}

	public Relator getRole() {
		return role;
	}

	public void setRole(Relator role) {
		this.role = role;
	}

    public String getFileAs()
    {
        return fileAs;
    }

    public void setFileAs(String fileAs)
    {
        this.fileAs = fileAs;
    }
}
