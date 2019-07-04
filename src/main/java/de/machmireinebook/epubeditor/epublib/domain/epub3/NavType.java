package de.machmireinebook.epubeditor.epublib.domain.epub3;

/**
 * User: Michail Jungierek
 * Date: 02.07.2019
 * Time: 20:25
 */
public enum NavType
{
    toc("toc"),
    page_list("page-list"),
    landmarks("landmarks");

    private String name;

    NavType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
