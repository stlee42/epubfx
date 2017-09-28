package de.machmireinebook.epubeditor.epublib.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A Book's collection of Metadata.
 * In the future it should contain all Dublin Core attributes, for now it contains a set of often-used ones.
 *
 * @author paul
 */
public class Metadata implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = -2437262888962149444L;

    public static final String DEFAULT_LANGUAGE = "en";

    private boolean autoGeneratedId = true;
    private List<Author> authors = new ArrayList<>();
    private List<Author> contributors = new ArrayList<>();
    private List<MetadataDate> dates = new ArrayList<>();
    private String language = DEFAULT_LANGUAGE;
    private List<Epub3MetadataProperty> epub3MetaProperties = new ArrayList<>();
    private List<String> rights = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    private List<Identifier> identifiers = new ArrayList<>();
    private List<String> subjects = new ArrayList<>();
    private String format = MediaType.EPUB.getName();
    private List<String> types = new ArrayList<>();
    private List<String> descriptions = new ArrayList<>();
    private List<String> publishers = new ArrayList<>();
    private List<String> coverages = new ArrayList<>();
    private Map<String, String> epub2MetaAttributes = new HashMap<>();

    public Metadata()
    {
        identifiers.add(new Identifier());
        autoGeneratedId = true;
    }

    public boolean isAutoGeneratedId()
    {
        return autoGeneratedId;
    }

    /**
     * Metadata properties not hard-coded like the author, title, etc.
     *
     * @return Metadata properties not hard-coded like the author, title, etc.
     */
    public List<Epub3MetadataProperty> getEpub3MetaProperties()
    {
        return epub3MetaProperties;
    }

    public void setEpub3MetaProperties(List<Epub3MetadataProperty> epub3MetaProperties)
    {
        this.epub3MetaProperties = epub3MetaProperties;
    }

    public MetadataDate addDate(MetadataDate date)
    {
        this.dates.add(date);
        return date;
    }

    public List<MetadataDate> getDates()
    {
        return dates;
    }

    public void setDates(List<MetadataDate> dates)
    {
        this.dates = dates;
    }

    public Author addAuthor(Author author)
    {
        authors.add(author);
        return author;
    }

    public List<Author> getAuthors()
    {
        return authors;
    }

    public List<Author> getAuthorsWithoutFirst()
    {
        if (authors.size() > 1)
        {
            return authors.subList(1, authors.size());
        }
        else
        {
            return new ArrayList<>();
        }
    }

    public void setAuthors(List<Author> authors)
    {
        this.authors = authors;
    }

    public Author getFirstAuthor()
    {
        if (authors == null || authors.isEmpty())
        {
            return null;
        }
        for (Author author : authors)
        {
            if (author != null)
            {
                return author;
            }
        }
        return null;
    }

    public String getFirstAuthorName()
    {
        Author author = getFirstAuthor();
        if (author != null)
        {
            return author.getName();
        }
        return "";
    }

    public Author addContributor(Author contributor)
    {
        contributors.add(contributor);
        return contributor;
    }

    public List<Author> getContributors()
    {
        return contributors;
    }

    public void setContributors(List<Author> contributors)
    {
        this.contributors = contributors;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public List<String> getSubjects()
    {
        return subjects;
    }

    public void setSubjects(List<String> subjects)
    {
        this.subjects = subjects;
    }

    public void setRights(List<String> rights)
    {
        this.rights = rights;
    }

    public List<String> getRights()
    {
        return rights;
    }


    /**
     * Gets the first non-blank title of the book.
     * Will return "" if no title found.
     *
     * @return the first non-blank title of the book.
     */
    public String getFirstTitle()
    {
        if (titles == null || titles.isEmpty())
        {
            return "";
        }
        for (String title : titles)
        {
            if (StringUtils.isNotBlank(title))
            {
                return title;
            }
        }
        return "";
    }


    public String addTitle(String title)
    {
        this.titles.add(title);
        return title;
    }

    public void setTitles(List<String> titles)
    {
        this.titles = titles;
    }

    public List<String> getTitles()
    {
        return titles;
    }

    public String addPublisher(String publisher)
    {
        this.publishers.add(publisher);
        return publisher;
    }

    public void setPublishers(List<String> publishers)
    {
        this.publishers = publishers;
    }

    public List<String> getPublishers()
    {
        return publishers;
    }

    public String addDescription(String description)
    {
        this.descriptions.add(description);
        return description;
    }

    public void setDescriptions(List<String> descriptions)
    {
        this.descriptions = descriptions;
    }

    public List<String> getDescriptions()
    {
        return descriptions;
    }

    public Identifier addIdentifier(Identifier identifier)
    {
        if (autoGeneratedId && (!(identifiers.isEmpty())))
        {
            identifiers.set(0, identifier);
        }
        else
        {
            identifiers.add(identifier);
        }
        autoGeneratedId = false;
        return identifier;
    }

    public void setIdentifiers(List<Identifier> identifiers)
    {
        this.identifiers = identifiers;
        autoGeneratedId = false;
    }

    /**
     * The first identifier for which the bookId is true is made the bookId identifier.
     * If no identifier has bookId == true then the first bookId identifier is written as the primary.
     *
     * @param identifiers
     * @return The first identifier for which the bookId is true is made the bookId identifier.
     */
    public Identifier getBookIdIdentifier()
    {
        if(identifiers == null || identifiers.isEmpty())
        {
            return null;
        }

        Identifier result = null;
        for(Identifier identifier: identifiers)
        {
            if(identifier.isBookId())
            {
                result = identifier;
                break;
            }
        }

        if(result == null)
        {
            result = identifiers.get(0);
        }

        return result;
    }

    public void generateNewUuid()
    {
        Identifier uuid = new Identifier();
        Identifier bookId = getBookIdIdentifier();
        bookId.setScheme("UUID");
        bookId.setValue(uuid.getValue());
    }

    public List<Identifier> getIdentifiers()
    {
        return identifiers;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public String getFormat()
    {
        return format;
    }

    public String addType(String type)
    {
        this.types.add(type);
        return type;
    }

    public List<String> getTypes()
    {
        return types;
    }

    public void setTypes(List<String> types)
    {
        this.types = types;
    }

    public String addCoverage(String coverage)
    {
        this.coverages.add(coverage);
        return coverage;
    }

    public List<String> getCoverages()
    {
        return coverages;
    }

    public void setCoverages(List<String> coverages)
    {
        this.coverages = coverages;
    }

    public String getEpub2MetaAttribute(String name)
    {
        return epub2MetaAttributes.get(name);
    }

    public void setEpub2MetaAttributes(Map<String, String> metaAttributes)
    {
        this.epub2MetaAttributes = metaAttributes;
    }
}
