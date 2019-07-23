package de.machmireinebook.epubeditor.preferences;

/**
 * User: Michail Jungierek
 * Date: 07.07.2019
 * Time: 20:26
 */
public enum QuotationMark
{
    ENGLISH("“ ” (English, UK)", "“", "”", "‘", "’"),
    ENGLISH_US("“ ” (English, US)", "‘", "’", "“", "”"),
    GERMAN("„“ (Deutsch)", "„", "“", "‚", "‘"),
    GERMAN_GUILLEMETS("»« (Deutsch)", "»", "«", "›", "‹"),
    FRENCH("«» (Français)", "«", "»",  "‹", "›"),
            ;

    private String description;
    private String left;
    private String right;
    private String singleLeft;
    private String singleRight;

    QuotationMark(String description, String left, String right, String singleLeft, String singleRight)
    {
        this.description = description;
        this.left = left;
        this.right = right;
        this.singleLeft = singleLeft;
        this.singleRight = singleRight;
    }

    public static QuotationMark findByDescription(String description)
    {
        for (QuotationMark value : values())
        {
            if (value.description.equals(description)) {
                return value;
            }
        }
        throw new IllegalArgumentException("no such quotation mark " + description);
    }

    public String getDescription()
    {
        return description;
    }

    public String getLeft()
    {
        return left;
    }

    public String getRight()
    {
        return right;
    }

    public String getSingleLeft() {
        return singleLeft;
    }

    public String getSingleRight() {
        return singleRight;
    }

    @Override
    public String toString()
    {
        return description;
    }

}
