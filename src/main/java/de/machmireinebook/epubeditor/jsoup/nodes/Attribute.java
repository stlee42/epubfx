package de.machmireinebook.epubeditor.jsoup.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.machmireinebook.epubeditor.jsoup.helper.Validate;

import org.apache.log4j.Logger;

/**
 A single key + value attribute. Keys are trimmed and normalised to lower-case.

 @author Jonathan Hedley, jonathan@hedley.net */
public class Attribute implements Map.Entry<String, String>, Cloneable  {
    private static final Logger logger = Logger.getLogger(Attribute.class);
    private String key;
    private String value;

    private static final List<String> SVG_ATTRIBUTES = new ArrayList<>();

    static
    {
        SVG_ATTRIBUTES.addAll(Arrays.asList("allowReorder", "attributeName", "attributeType", "autoReverse", "baseFrequency", "baseProfile",
                "calcMode", "clipPathUnits", "contentScriptType", "contentStyleType", "diffuseConstant", "externalResourcesRequired", "filterRes", "filterUnits",
                "glyphRef", "gradientTransform", "gradientUnits", "kernelMatrix", "kernelUnitLength", "keyPoints", "keySplines", "keyTimes",
                "lengthAdjust", "limitingConeAngle", "markerHeight", "markerUnits", "markerWidth", "maskContentUnits",
                "maskUnits", "numOctaves", "pathLength", "patternContentUnits", "patternTransform", "patternUnits", "pointsAtX", "pointsAtY", "pointsAtZ", "preserveAlpha",
                "preserveAspectRatio", "primitiveUnits", "refX", "refY", "repeatCount", "repeatDur", "requiredExtensions", "requiredFeatures", "specularConstant", "specularExponent",
                "spreadMethod", "startOffset", "stdDeviation", "stitchTiles", "surfaceScale", "systemLanguage", "tableValues", "targetX", "targetY", "textLength",
                "viewBox", "viewTarget", "xChannelSelector", "yChannelSelector", "zoomAndPan"));
    }

    /**
     * Create a new attribute from unencoded (raw) key and value.
     * @param key attribute key
     * @param value attribute value
     * @see #createFromEncoded
     */
    public Attribute(String key, String value) {
        Validate.notEmpty(key);
        Validate.notNull(value);
        if (!isSvgKey(key))
        {
            this.key = key.trim().toLowerCase();
        }
        else
        {
            this.key = key.trim();
        }
        this.value = value;
    }

    /**
     Get the attribute key.
     @return the attribute key
     */
    public String getKey() {
        return key;
    }

    /**
     Set the attribute key. Gets normalised as per the constructor method.
     @param key the new key; must not be null
     */
    public void setKey(String key) {
        Validate.notEmpty(key);
        if (!isSvgKey(key))
        {
            this.key = key.trim().toLowerCase();
        }
        else
        {
            this.key = key.trim();
        }
    }

    private boolean isSvgKey(String key)
    {
        return SVG_ATTRIBUTES.contains(key);
    }

    /**
     Get the attribute value.
     @return the attribute value
     */
    public String getValue() {
        return value;
    }

    /**
     Set the attribute value.
     @param value the new attribute value; must not be null
     */
    public String setValue(String value) {
        Validate.notNull(value);
        String old = this.value;
        this.value = value;
        return old;
    }

    /**
     Get the HTML representation of this attribute; e.g. {@code href="index.html"}.
     @return HTML
     */
    public String html() {
        return key + "=\"" + Entities.escape(value, (new Document("")).outputSettings()) + "\"";
    }
    
    protected void html(StringBuilder accum, Document.OutputSettings out) {
        accum
            .append(key)
            .append("=\"")
            .append(Entities.escape(value, out))
            .append("\"");
    }

    /**
     Get the string representation of this attribute, implemented as {@link #html()}.
     @return string
     */
    public String toString() {
        return html();
    }

    /**
     * Create a new Attribute from an unencoded key and a HTML attribute encoded value.
     * @param unencodedKey assumes the key is not encoded, as can be only run of simple \w chars.
     * @param encodedValue HTML attribute encoded value
     * @return attribute
     */
    public static Attribute createFromEncoded(String unencodedKey, String encodedValue) {
        String value = Entities.unescape(encodedValue, true);
        return new Attribute(unencodedKey, value);
    }

    protected boolean isDataAttribute() {
        return key.startsWith(Attributes.dataPrefix) && key.length() > Attributes.dataPrefix.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;

        Attribute attribute = (Attribute) o;

        return !(key != null ? !key.equals(attribute.key) : attribute.key != null)
                && !(value != null ? !value.equals(attribute.value) : attribute.value != null);

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public Attribute clone() {
        try {
            return (Attribute) super.clone(); // only fields are immutable strings key and value, so no more deep copy required
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
