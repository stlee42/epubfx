package de.machmireinebook.epubeditor.epublib.domain;

import java.nio.file.Path;

import javafx.scene.image.Image;

import de.machmireinebook.epubeditor.javafx.FXUtils;

/**
 * User: mjungierek
 * Date: 21.08.2014
 * Time: 21:10
 */
public class BookTemplate
{
    public static final BookTemplate MINIMAL_EPUB_2_BOOK = new BookTemplate("Minimales Buch", "Ein minimales Buch im EPUB 2 Flowable Layout",
            2.0, FXUtils.getImage("/images/book_128px.png"));
    private String name;
    private Path path;
    private String description;
    private Image cover;
    private double version;

    public BookTemplate()
    {
    }

    public BookTemplate(String name, Image cover, String description, Path path, double version)
    {

        this.name = name;
        this.cover = cover;
        this.description = description;
        this.path = path;
        this.version = version;
    }

    public BookTemplate(String name, String description, double version, Image cover)
    {
        this.name = name;
        this.description = description;
        this.version = version;
        this.cover = cover;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Path getPath()
    {
        return path;
    }

    public void setPath(Path path)
    {
        this.path = path;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Image getCover()
    {
        return cover;
    }

    public void setCover(Image cover)
    {
        this.cover = cover;
    }

    public double getVersion()
    {
        return version;
    }

    public void setVersion(double version)
    {
        this.version = version;
    }
}
