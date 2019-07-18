package de.machmireinebook.epubeditor.epublib.domain.epub3;

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
	
	private MetadataProperty role;
	private MetadataProperty fileAs;

	public Author(String id, String name, String language) {
	    super(id, name, language);
	    if (StringUtils.isEmpty(id) && StringUtils.isNotEmpty(name)) {
			setId(Normalizer.normalize(name, Normalizer.Form.NFD)
					.replaceAll("[^\\p{ASCII}]", "")
					.replaceAll(" ", "_")
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
		return StringUtils.equals(getValue(), other.getValue());
	}

	public void setRole(Relator relator)
	{
		if (role != null) {
			getRefinements().remove(role);
		}
		MetadataProperty roleMetadataProperty = new MetadataProperty();
		roleMetadataProperty.setProperty(MetadataPropertyValue.role.getName());
		roleMetadataProperty.setScheme("marc:relators");
		roleMetadataProperty.setRefines("#" + getId());
		roleMetadataProperty.setValue(relator.getCode());
		getRefinements().add(roleMetadataProperty);
		role = roleMetadataProperty;
	}

	public void setRole(MetadataProperty role)
	{
		this.role = role;
	}

	public MetadataProperty getRole()
	{
		return role;
	}

	public void setFileAs(MetadataProperty fileAs)
	{
		this.fileAs = fileAs;
	}

	public MetadataProperty getFileAs()
	{
		return fileAs;
	}

	public void setFileAs(String fileAs)
	{
		if (this.fileAs != null) {
			getRefinements().remove(this.fileAs);
		}
		MetadataProperty fileAsMetadataProperty = new MetadataProperty();
		fileAsMetadataProperty.setProperty(MetadataPropertyValue.file_as.getName());
		fileAsMetadataProperty.setRefines("#" + getId());
		fileAsMetadataProperty.setValue(fileAs);
		getRefinements().add(fileAsMetadataProperty);
		this.fileAs = fileAsMetadataProperty;
	}
}
