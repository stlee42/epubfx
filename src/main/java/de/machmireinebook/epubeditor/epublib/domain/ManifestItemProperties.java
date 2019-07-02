package de.machmireinebook.epubeditor.epublib.domain;

public enum ManifestItemProperties implements ManifestProperties
{
	cover_image("cover-image"),
	mathml("mathml"),
	nav("nav"),
	remote_resources("remote-resources"),
	scripted("scripted"),
	svg("svg"),
	swïtch("switch");//switch is reserved word, because this enum value written with trema ï ;-)
	
	private String name;
	
	ManifestItemProperties(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
