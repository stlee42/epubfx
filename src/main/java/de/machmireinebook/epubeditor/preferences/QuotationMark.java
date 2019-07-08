package de.machmireinebook.epubeditor.preferences;

/**
 * User: Michail Jungierek
 * Date: 07.07.2019
 * Time: 20:26
 */
public enum QuotationMark
{
    ENGLISH("“ ” (English)", "“", "”"),
    GERMAN("„“ (Deutsch)", "„", "“"),
    GERMAN_GUILLEMETS("»« (Deutsch)", "»", "«"),
    FRENCH("«» (Français)", "«", "»"),
            ;

    private String description;
    private String left;
    private String right;

    QuotationMark(String description, String left, String right)
    {
        this.description = description;
        this.left = left;
        this.right = right;
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

    @Override
    public String toString()
    {
        return description;
    }

}
