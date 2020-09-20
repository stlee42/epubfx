package de.machmireinebook.epubeditor.manager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.eaio.stringsearch.BoyerMooreHorspool;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.SpineReference;
import de.machmireinebook.epubeditor.epublib.resource.Resource;

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

    private final ObjectProperty<Book> currentBook = new SimpleObjectProperty<>();

    public static class SearchResult
    {
        private final int begin;
        private final int end;
        private final Resource<?> resource;

        public SearchResult(int begin, int end, Resource<?> resource)
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

        public Resource<?> getResource()
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
        private final boolean dotAll;
        private final boolean minimalMatch;
        private final SearchMode mode;
        private final SearchRegion region;

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

    @PostConstruct
    private void init()
    {
        stringSearch = new BoyerMooreHorspool();
    }

    public ObjectProperty<Book> currentBookProperty()
    {
        return currentBook;
    }

    public Optional<SearchResult> findNext(String queryString, Resource<?> currentResource, int fromIndex, SearchParams params)
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
                text = text.replace("\r\n", "\n");
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
                text = text.replaceAll("\r\n", "\n");
                text = text.toLowerCase(Locale.GERMANY);
                position = stringSearch.searchString(text, fromIndex, queryString.toLowerCase(Locale.GERMANY));
//                position = text.indexOf(queryString.toLowerCase(Locale.GERMANY), fromIndex);
                logger.info("position " + position);
            }
            catch (UnsupportedEncodingException e)
            {
                logger.error("", e);
            }
        }

        if (position > -1) {
            int length = queryString.length();
            result = Optional.of(new SearchResult(position, position + length, currentResource));
        }
        else if (params.getRegion() == SearchRegion.ALL_RESOURCES) {
            Book book = currentBook.getValue();
            Optional<Resource<?>> resourceOptional = book.getResources().getAll().stream()
                    .dropWhile(resource -> resource != currentResource) //drop all resources before current resource
                    .filter(resource -> resource != currentResource) //remain all resources after the current resource
                    .findFirst();

            if (resourceOptional.isPresent()) {
                result = findNext(queryString, resourceOptional.get(), 0, params);
            } else {
                result = Optional.empty();
            }
        }
        else if (params.getRegion() == SearchRegion.ALL_XHTML_REOURCES) {
            Book book = currentBook.getValue();
            int index = book.getSpine().getResourceIndex(currentResource);
            if (index + 1 < book.getSpine().getSpineReferences().size()) {
                SpineReference reference = book.getSpine().getSpineReferences().get(index + 1);
                Resource<?> resource = reference.getResource();
                result = findNext(queryString, resource, 0, params);
            } else {
                result = Optional.empty();
            }
        }
        else {
            result = Optional.empty();
        }
        return result;
    }

    public List<SearchResult> findAll(String queryString, Resource<?> currentResource, SearchParams params)
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
                text = text.replace("\r\n", "\n");
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
