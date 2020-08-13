package de.machmireinebook.epubeditor.xhtml;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.text.translate.CharSequenceTranslator;

public class NumericEntityWithoutSpacesUnescaper  extends CharSequenceTranslator {
    private final List<Integer> specialXhtmlCharacter = Arrays.asList(
            160,  //non breaking space
            0x2002,  //en space
            0x2003, //em space
            0x2004, //Three-Per-Em Space (thick space, 1/3 of em)
            0x2005, //Four-Per-Em Space (mid space, 1/4 of em)
            0x2006, //Six-Per-Em Space
            0x2007, //Figure Space (widht of number)
            0x2008, //Punctuation Space
            0x2009, //Thin Space
            0x200A, //Hair Space
            0x200B, //Zero-Width Space
            8239,  //narrow no-break space
            65279  //zero width no-break space
    );

    /** NumericEntityUnescaper option enum. */
    public enum OPTION { semiColonRequired, semiColonOptional, errorIfNoSemiColon }

    /** EnumSet of OPTIONS, given from the constructor. */
    private final EnumSet<OPTION> options;

    /**
     * Create a UnicodeUnescaper.
     *
     * The constructor takes a list of options, only one type of which is currently
     * available (whether to allow, error or ignore the semi-colon on the end of a
     * numeric entity to being missing).
     *
     * For example, to support numeric entities without a ';':
     *    new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.semiColonOptional)
     * and to throw an IllegalArgumentException when they're missing:
     *    new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.errorIfNoSemiColon)
     *
     * Note that the default behaviour is to ignore them.
     *
     * @param options to apply to this unescaper
     */
    public NumericEntityWithoutSpacesUnescaper(final OPTION... options) {
        if (options.length > 0) {
            this.options = EnumSet.copyOf(Arrays.asList(options));
        } else {
            this.options = EnumSet.copyOf(Collections.singletonList(OPTION.semiColonRequired));
        }
    }

    /**
     * Whether the passed in option is currently set.
     *
     * @param option to check state of
     * @return whether the option is set
     */
    public boolean isSet(final OPTION option) {
        return options != null && options.contains(option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        final int seqEnd = input.length();
        // Uses -2 to ensure there is something after the &#
        if (input.charAt(index) == '&' && index < seqEnd - 2 && input.charAt(index + 1) == '#') {
            int start = index + 2;
            boolean isHex = false;

            final char firstChar = input.charAt(start);
            if (firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;

                // Check there's more than just an x after the &#
                if (start == seqEnd) {
                    return 0;
                }
            }

            int end = start;
            // Note that this supports character codes without a ; on the end
            while (end < seqEnd && (input.charAt(end) >= '0' && input.charAt(end) <= '9'
                    || input.charAt(end) >= 'a' && input.charAt(end) <= 'f'
                    || input.charAt(end) >= 'A' && input.charAt(end) <= 'F')) {
                end++;
            }

            final boolean semiNext = end != seqEnd && input.charAt(end) == ';';

            if (!semiNext) {
                if (isSet(OPTION.semiColonRequired)) {
                    return 0;
                }
                if (isSet(OPTION.errorIfNoSemiColon)) {
                    throw new IllegalArgumentException("Semi-colon required at end of numeric entity");
                }
            }

            int entityValue;
            try {
                if (isHex) {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 16);
                } else {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 10);
                }
            } catch (final NumberFormatException nfe) {
                return 0;
            }

            if (!specialXhtmlCharacter.contains(entityValue)) {
                if (entityValue > 0xFFFF) {
                    final char[] chrs = Character.toChars(entityValue);
                    out.write(chrs[0]);
                    out.write(chrs[1]);
                }
                else {
                    out.write(entityValue);
                }
            } else {
                out.write("&#" + entityValue + ";");
            }

            return 2 + end - start + (isHex ? 1 : 0) + (semiNext ? 1 : 0);
        }
        return 0;
    }

}
