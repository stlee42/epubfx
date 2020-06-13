package de.machmireinebook.epubeditor.editor;

import java.util.function.IntFunction;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveList;

/**
 * Graphic factory that produces small rectangle with the color defined in paragraph.
 * To customize appearance, use {@code .lineno} style class in CSS stylesheets.
 */
public class ColorFactory implements IntFunction<Node> {
    private static final Logger logger = Logger.getLogger(ColorFactory.class);

    private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);

    public static IntFunction<Node> get(
            GenericStyledArea<?, ?, ?> area) {
        return new ColorFactory(area);
    }

    private final LiveList<? extends Paragraph<?, ?, ?>> paragraphs;

    private ColorFactory(
            GenericStyledArea<?, ?, ?> area) {
        paragraphs = area.getParagraphs();
    }

    @Override
    public Node apply(int idx) {
        String lineText = paragraphs.get(idx).getText();
        Rectangle rect = new Rectangle(10 ,10);
        rect.setVisible(false);
        if (StringUtils.isNotEmpty(lineText)) {
            if (StringUtils.containsIgnoreCase(lineText, "color")) {
                rect.setStroke(Color.web("rgb(192,192,192)"));
                String substring = StringUtils.substringAfter(lineText, ":");
                if (StringUtils.isNotEmpty(substring)) {
                    try {
                        rect.setFill(Paint.valueOf(substring.trim()));
                        rect.setVisible(true);
                    } catch (IllegalArgumentException e) {
                        logger.info("css value not a valid color information: " + substring);
                    }
                }
            }
        }
        return rect;
    }

}
