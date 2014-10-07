package de.machmireinebook.epubeditor.cdi;

/**
 * User: mjungierek
 * Date: 19.08.2014
 * Time: 20:16
 */

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@Qualifier
public @interface CurrentBook
{
}

