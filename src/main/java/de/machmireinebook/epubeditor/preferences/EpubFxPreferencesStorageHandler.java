package de.machmireinebook.epubeditor.preferences;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javafx.collections.ObservableList;

import de.machmireinebook.epubeditor.jdom2.AttributeElementFilter;

import org.apache.commons.lang3.math.NumberUtils;

import org.jdom2.CDATA;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.util.IteratorIterable;

import com.dlsc.preferencesfx.util.Constants;
import com.dlsc.preferencesfx.util.StorageHandler;

/**
 * @author Michail Jungierek, CGI
 */
public class EpubFxPreferencesStorageHandler implements StorageHandler
{
    private Element preferencesElement;

    //tag name
    private static final String SELECTED_CATGEGORY_ELEMENT_NAME = "selected-category";
    private static final String DIVIDER_POSITION_ELEMENT_NAME = "selected-category";
    private static final String WINDOW_ELEMENT_NAME = "window";
    private static final String PREFERENCES_VALUES_ELEMENT_NAME = "preferences-values";
    private static final String PREFERENCES_VALUE_ELEMENT_NAME = "preferences-value";

    public EpubFxPreferencesStorageHandler(Element preferencesElement)
    {
        this.preferencesElement = preferencesElement;
    }

    @Override
    public void saveSelectedCategory(String breadcrumb)
    {
        Element selectedCategoryElement = preferencesElement.getChild(SELECTED_CATGEGORY_ELEMENT_NAME);
        if (selectedCategoryElement == null) {
            selectedCategoryElement = new Element(SELECTED_CATGEGORY_ELEMENT_NAME);
            preferencesElement.addContent(selectedCategoryElement);
        }
        selectedCategoryElement.setText(breadcrumb);
    }

    @Override
    public String loadSelectedCategory()
    {
        return preferencesElement.getChildText(SELECTED_CATGEGORY_ELEMENT_NAME);
    }

    @Override
    public void saveDividerPosition(double dividerPosition)
    {
        Element dividerPositionElement = preferencesElement.getChild(DIVIDER_POSITION_ELEMENT_NAME);
        if (dividerPositionElement == null) {
            dividerPositionElement = new Element(DIVIDER_POSITION_ELEMENT_NAME);
            preferencesElement.addContent(dividerPositionElement);
        }
        dividerPositionElement.setText(String.valueOf(dividerPosition));
    }

    @Override
    public double loadDividerPosition()
    {
        Element dividerPositionElement = preferencesElement.getChild(DIVIDER_POSITION_ELEMENT_NAME);
        if (dividerPositionElement != null) {
            return NumberUtils.toDouble(preferencesElement.getChildText(DIVIDER_POSITION_ELEMENT_NAME), Constants.DEFAULT_DIVIDER_POSITION);
        } else {
            return Constants.DEFAULT_DIVIDER_POSITION;
        }
    }

    @Override
    public void saveWindowWidth(double windowWidth)
    {
        Element windowElement = preferencesElement.getChild(WINDOW_ELEMENT_NAME);
        if (windowElement == null) {
            windowElement = new Element(DIVIDER_POSITION_ELEMENT_NAME);
            preferencesElement.addContent(windowElement);
        }
        windowElement.setAttribute("width", String.valueOf(windowElement));
    }

    @Override
    public double loadWindowWidth()
    {
        Element windowElement = preferencesElement.getChild(WINDOW_ELEMENT_NAME);
        if (windowElement != null) {
            return NumberUtils.toDouble(windowElement.getAttributeValue("width"), Constants.DEFAULT_PREFERENCES_WIDTH);
        } else {
            return Constants.DEFAULT_PREFERENCES_WIDTH;
        }
    }

    @Override
    public void saveWindowHeight(double windowHeight)
    {
        Element windowElement = preferencesElement.getChild(WINDOW_ELEMENT_NAME);
        if (windowElement == null) {
            windowElement = new Element(DIVIDER_POSITION_ELEMENT_NAME);
            preferencesElement.addContent(windowElement);
        }
        windowElement.setAttribute("height", String.valueOf(windowElement));
    }

    @Override
    public double loadWindowHeight()
    {
        Element windowElement = preferencesElement.getChild(WINDOW_ELEMENT_NAME);
        if (windowElement != null) {
            return NumberUtils.toDouble(windowElement.getAttributeValue("height"), Constants.DEFAULT_PREFERENCES_HEIGHT);
        } else {
            return Constants.DEFAULT_PREFERENCES_HEIGHT;
        }
    }

    @Override
    public void saveWindowPosX(double windowPosX)
    {
        Element windowElement = preferencesElement.getChild(WINDOW_ELEMENT_NAME);
        if (windowElement == null) {
            windowElement = new Element(DIVIDER_POSITION_ELEMENT_NAME);
            preferencesElement.addContent(windowElement);
        }
        windowElement.setAttribute("position-x", String.valueOf(windowElement));
    }

    @Override
    public double loadWindowPosX()
    {
        Element windowElement = preferencesElement.getChild(WINDOW_ELEMENT_NAME);
        if (windowElement != null) {
            return NumberUtils.toDouble(windowElement.getAttributeValue("position-x"), Constants.DEFAULT_PREFERENCES_HEIGHT);
        } else {
            return Constants.DEFAULT_PREFERENCES_POS_X;
        }
    }

    @Override
    public void saveWindowPosY(double windowPosY)
    {
        Element windowElement = preferencesElement.getChild(WINDOW_ELEMENT_NAME);
        if (windowElement == null) {
            windowElement = new Element(DIVIDER_POSITION_ELEMENT_NAME);
            preferencesElement.addContent(windowElement);
        }
        windowElement.setAttribute("position-y", String.valueOf(windowElement));
    }

    @Override
    public double loadWindowPosY()
    {
        Element windowElement = preferencesElement.getChild(WINDOW_ELEMENT_NAME);
        if (windowElement != null) {
            return NumberUtils.toDouble(windowElement.getAttributeValue("position-y"), Constants.DEFAULT_PREFERENCES_HEIGHT);
        } else {
            return Constants.DEFAULT_PREFERENCES_POS_Y;
        }
    }

    @Override
    public void saveObject(String breadcrumb, Object object)
    {
        Element preferenceValuesElement = preferencesElement.getChild(PREFERENCES_VALUES_ELEMENT_NAME);
        if (preferenceValuesElement == null) {
            preferenceValuesElement = new Element(PREFERENCES_VALUES_ELEMENT_NAME);
            preferencesElement.addContent(preferenceValuesElement);
        }
        Element preferenceValueElement;
        AttributeElementFilter filter = new AttributeElementFilter(PREFERENCES_VALUE_ELEMENT_NAME, "breadcrumb", breadcrumb);
        IteratorIterable<Element> values = preferenceValuesElement.getDescendants(filter);
        if (values.hasNext()) { //we take the first element if more than one are existing
            preferenceValueElement = values.next();
        } else {
            preferenceValueElement = new Element(PREFERENCES_VALUE_ELEMENT_NAME);
            preferenceValueElement.setAttribute("breadcrumb", breadcrumb);
            preferenceValuesElement.addContent(preferenceValueElement);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        java.beans.XMLEncoder xe1 = new java.beans.XMLEncoder(bos);
        xe1.writeObject(object);
        xe1.close();
        preferenceValueElement.setContent(new CDATA(bos.toString(StandardCharsets.UTF_8)));
    }

    @Override
    public Object loadObject(String breadcrumb, Object defaultObject)
    {
        Element preferenceValuesElement = preferencesElement.getChild(PREFERENCES_VALUES_ELEMENT_NAME);
        if (preferenceValuesElement == null) {
            return defaultObject;
        }
        Element preferenceValueElement;
        AttributeElementFilter filter = new AttributeElementFilter(PREFERENCES_VALUE_ELEMENT_NAME, "breadcrumb", breadcrumb);
        IteratorIterable<Element> values = preferenceValuesElement.getDescendants(filter);
        if (values.hasNext()) { //we take the first element if more than one are existing
            preferenceValueElement = values.next();
            AtomicReference<Object> result = new AtomicReference<>();
            preferenceValueElement.getContent().stream()
                    .filter(content -> content.getCType() == Content.CType.CDATA)
                    .map(content -> (CDATA)content)
                    .findFirst()
                    .ifPresent(cdata -> {
                        ByteArrayInputStream bis = new ByteArrayInputStream(cdata.getText().getBytes(StandardCharsets.UTF_8));
                        XMLDecoder decoder = new XMLDecoder(bis);
                        result.set(decoder.readObject());
                    });
            return result.get();
        }
        return defaultObject;
    }

    @Override
    public ObservableList loadObservableList(String breadcrumb, ObservableList defaultObservableList)
    {
        return (ObservableList)loadObject(breadcrumb, defaultObservableList);
    }

    @Override
    public boolean clearPreferences()
    {
        preferencesElement.removeContent();
        return true;
    }

    @Override
    public Preferences getPreferences()
    {
        return null;
    }

    public Element getPreferencesElement() {
        return preferencesElement;
    }

    public void setPreferencesElement(Element preferencesElement) {
        this.preferencesElement = preferencesElement;
    }
}
