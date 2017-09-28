package de.machmireinebook.epubeditor.manager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Singleton;

import com.eaio.stringsearch.BoyerMooreHorspool;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * User: mjungierek
 * Date: 06.01.2015
 * Time: 01:53
 */
@Singleton
public class SearchManager
{
    private static final Logger logger = Logger.getLogger(SearchManager.class);

    private BoyerMooreHorspool stringSearch;

    private ObjectProperty<Book> currentBook = new SimpleObjectProperty<>();

    public static class SearchResult
    {
        private int begin;
        private int end;
        private Resource resource;

        public SearchResult(int begin, int end, Resource resource)
        {
            this.begin = begin;
            this.end = end;
            this.resource = resource;
        }

        public int getBegin()
        {
            return begin;
        }

        public int getEnd()
        {
            return end;
        }

        public Resource getResource()
        {
            return resource;
        }
    }

    public enum SearchMode
    {
        NORMAL,
        CASE_SENSITIVE,
        REGEX;
    }

    public enum SearchRegion
    {
        CURRENT_RESOURCE,
        ALL_RESOURCES,
        ALL_XHTML_REOURCES;
    }

    public static class SearchParams
    {
        private boolean dotAll;
        private boolean minimalMatch;
        private SearchMode mode;
        private SearchRegion region;

        public SearchParams(boolean dotAll, boolean minimalMatch, SearchMode mode, SearchRegion region)
        {
            this.dotAll = dotAll;
            this.minimalMatch = minimalMatch;
            this.mode = mode;
            this.region = region;
        }

        public boolean isDotAll()
        {
            return dotAll;
        }

        public boolean isMinimalMatch()
        {
            return minimalMatch;
        }

        public SearchMode getMode()
        {
            return mode;
        }

        public SearchRegion getRegion()
        {
            return region;
        }
    }

    private void init()
    {
        stringSearch = new BoyerMooreHorspool();
    }

    public ObjectProperty<Book> currentBookProperty()
    {
        return currentBook;
    }

    public Optional<SearchResult> findNext(String queryString, Resource currentResource, int fromIndex, SearchParams params)
    {
        logger.info("fromIndex " + fromIndex);
        Optional<SearchResult> result;
        int position = -1;
        if (StringUtils.isEmpty(queryString))
        {
            return Optional.empty();
        }
        if (params.getMode().equals(SearchMode.CASE_SENSITIVE))
        {
            try
            {
                String text = new String(currentResource.getData(), currentResource.getInputEncoding());
                position = stringSearch.searchString(text, fromIndex, queryString);
                logger.info("position " + position);
            }
            catch (UnsupportedEncodingException e)
            {
                logger.error("", e);
            }
        }
        else if (params.getMode().equals(SearchMode.NORMAL))
        {
            try
            {
                String text = new String(currentResource.getData(), currentResource.getInputEncoding());
                text = text.toLowerCase(Locale.GERMANY);
                position = stringSearch.searchString(text, fromIndex, queryString.toLowerCase(Locale.GERMANY));
//                position = text.indexOf(queryString.toLowerCase(Locale.GERMANY), fromIndex);
            }
            catch (UnsupportedEncodingException e)
            {
                logger.error("", e);
            }
        }
        if (position > -1)
        {
            int length = queryString.length();
            result = Optional.of(new SearchResult(position, position + length, currentResource));
        }
        else
        {
            result = Optional.empty();
        }
        return result;
    }

    public List<SearchResult> findAll(String queryString, Resource currentResource, SearchParams params)
    {
        List<SearchResult> result = new ArrayList<>();
        int position = 0;
        int length = queryString.length();

        try
        {
            String text = new String(currentResource.getData(), currentResource.getInputEncoding());
            if (params.getMode().equals(SearchMode.NORMAL))
            {
                text = text.toLowerCase(Locale.GERMANY);
                queryString = queryString.toLowerCase(Locale.GERMANY);
            }
            while(true)
            {
                position = stringSearch.searchString(text, position, queryString);
                logger.info("current position " + position);
                if (position == -1)
                {
                    break;
                }
                result.add(0, new SearchResult(position, position + length, currentResource));
                position = position + length;
            }
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error("", e);
        }
        return result;
    }

}
