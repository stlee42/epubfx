package de.machmireinebook.epubeditor.cdi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * User: mjungierek
 * Date: 03.01.2015
 * Time: 02:32
 */
@Qualifier
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD,
        ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface EpubEditorConfigurationProducer
{
}
