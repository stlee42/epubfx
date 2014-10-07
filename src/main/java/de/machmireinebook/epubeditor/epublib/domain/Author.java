package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents one of the authors of the book
 * 
 * @author paul
 *
 */
public class Author implements Serializable
{
	
	private static final long serialVersionUID = 6663408501416574200L;
	
	private String name;
    private String fileAs;
	private Relator relator = Relator.AUTHOR;
	
	public Author(String name) {
		this.name = name;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(name);
	}
	
	
	public boolean equals(Object authorObject) {
		if(! (authorObject instanceof Author)) {
			return false;
		}
		Author other = (Author) authorObject;
		return StringUtils.equals(name, other.name) && StringUtils.equals(fileAs, other.fileAs);
	}

	public Relator setRole(String code) {
		Relator result = Relator.byCode(code);
		if (result == null) {
			result = Relator.AUTHOR;
		}
		this.relator = result;
		return result;
	}


	public Relator getRelator() {
		return relator;
	}


	public void setRelator(Relator relator) {
		this.relator = relator;
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
