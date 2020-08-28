package de.machmireinebook.epubeditor.epublib.domain.epub3;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.machmireinebook.epubeditor.epublib.domain.ManifestProperties;

public enum ManifestItemPropertiesValue implements ManifestProperties
{
	cover_image("cover-image"),
	mathml("mathml"),
	nav("nav"),
	remote_resources("remote-resources"),
	scripted("scripted"),
	svg("svg"),
	epub_switch("switch");
	
	private final String name;
	
	ManifestItemPropertiesValue(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public static List<ManifestItemPropertiesValue> extractFromAttributeValue(String attributeContent) {
		List<ManifestItemPropertiesValue> result = new ArrayList<>();
		String[] splitted = StringUtils.split(attributeContent, " ");
		if (splitted != null) {
			for (String split : splitted) {
				for (ManifestItemPropertiesValue value : values()) {
					if (value.getName().equals(split)) {
						result.add(value);
					}
				}
			}
		}
		return result;
	}
}
