package de.machmireinebook.epubeditor.preferences;

import javafx.beans.property.SimpleObjectProperty;

/**
 * Created by Michail Jungierek
 */
public class SettingEnumObjectProperty<T> extends SimpleObjectProperty<T>
{
    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";

    private Class enumClass;
    private T initialValue;

    /**
     * The constructor of {@code SettingEnumObjectProperty}
     *
     * @param initialValue
     *            the initial value of the wrapped value
     */
    public SettingEnumObjectProperty(T initialValue, Class<T> enumClass) {
        super(DEFAULT_BEAN, DEFAULT_NAME, initialValue);
        this.initialValue = initialValue;
        this.enumClass = enumClass;
    }

    /**
     * It's a little bit tricky because to runtime T will be infered to Object, so that passing a String is correct.
     * Here the false T will be converted to a correct enum value.
     */
    @SuppressWarnings("unchecked")
    public void setValue(T value) {
        Object rawValue = value;
        if (rawValue instanceof String)
        {
            T enumValue;
            try
            {
                enumValue = (T) Enum.valueOf(enumClass, (String) rawValue);
            } catch (IllegalArgumentException e) {
                enumValue = initialValue;
            }
            set(enumValue);
        }
    }
}
