package de.machmireinebook.epubeditor.epublib.bookprocessor;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;

import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.epub2.BookProcessor;
import de.machmireinebook.epubeditor.epublib.resource.ImageResource;
import de.machmireinebook.epubeditor.epublib.resource.Resource;
import de.machmireinebook.epubeditor.epublib.resource.Resources;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;

/**
 * If the book contains a cover image then this will add a cover page to the book.
 * If the book contains a cover html page it will set that page's first image as the book's cover image.
 * <p>
 * will overwrite any "cover.jpg" or "cover.html" that are already there.
 *
 * @author paul
 */
public class CoverpageBookProcessor implements BookProcessor
{
    public static int MAX_COVER_IMAGE_SIZE = 1000;
    private static final Logger log = Logger.getLogger(CoverpageBookProcessor.class);
    public static final String DEFAULT_COVER_PAGE_ID = "cover";
    public static final String DEFAULT_COVER_PAGE_HREF = "Text/cover.html";
    public static final String DEFAULT_COVER_IMAGE_ID = "cover-image";
    public static final String DEFAULT_COVER_IMAGE_HREF = "Images/cover.jpg";

    @Override
    public Book processBook(Book book)
    {
        if (book.getCoverPage() == null && book.getCoverImage() == null)
        {
            return book;
        }
        Resource coverPage = book.getCoverPage();
        ImageResource coverImage = book.getCoverImage();
        if (coverPage == null)
        {
            if (coverImage != null)
            {
                if (StringUtils.isBlank(coverImage.getHref()))
                {
                    coverImage.setHref(getCoverImageHref(coverImage, book));
                }
                String coverPageHtml = createCoverpageHtml(coverImage.getHref(), coverImage.getWidth(), coverImage.getHeight());
                coverPage = MediaType.XHTML.getResourceFactory().createResource(coverPageHtml.getBytes(), getCoverPageHref(book));
                fixCoverResourceId(book, coverPage, DEFAULT_COVER_PAGE_ID);
            }
        }
        else
        {
            if (book.getCoverImage() == null) //datei vorhanden aber kein Bild wir nehmen das erste was wir finden
            {
                coverImage = getFirstImageSource(coverPage, book.getResources());
                book.setCoverImage(coverImage);
                if (coverImage != null)
                {
                    book.getResources().remove(coverImage.getHref());
                }
            }
            else //ansonsten mit dem gesetzten cover image die seite neu bauen
            {
                if (StringUtils.isBlank(coverImage.getHref()))
                {
                    coverImage.setHref(getCoverImageHref(coverImage, book));
                }
                String coverPageHtml = createCoverpageHtml(coverImage.getHref(), coverImage.getWidth(), coverImage.getHeight());
                coverPage.setData(coverPageHtml.getBytes());
                fixCoverResourceId(book, coverPage, DEFAULT_COVER_PAGE_ID);
            }
        }

        book.setCoverImage(coverImage);
        book.setCoverPage(coverPage);
        setCoverResourceIds(book);
        return book;
    }

    private void setCoverResourceIds(Book book)
    {
        if (book.getCoverImage() != null)
        {
            fixCoverResourceId(book, book.getCoverImage(), DEFAULT_COVER_IMAGE_ID);
        }
        if (book.getCoverPage() != null)
        {
            fixCoverResourceId(book, book.getCoverPage(), DEFAULT_COVER_PAGE_ID);
        }
    }


    private void fixCoverResourceId(Book book, Resource resource, String defaultId)
    {
        if (StringUtils.isBlank(resource.getId()))
        {
            resource.setId(defaultId);
        }
        book.getResources().fixResourceId(resource);
    }

    private String getCoverPageHref(Book book)
    {
        return DEFAULT_COVER_PAGE_HREF;
    }

    private String getCoverImageHref(Resource imageResource, Book book)
    {
        return DEFAULT_COVER_IMAGE_HREF;
    }

    private ImageResource getFirstImageSource(Resource coverPageResource, Resources resources)
    {
        try
        {
            Document titlePageDocument = ResourceUtil.getAsDocument(coverPageResource);
            Filter<Element> imgFilter = new ElementFilter("img");
            List<Element> imageElements = titlePageDocument.getRootElement().getContent(imgFilter);
            for (Element imageElement : imageElements)
            {
                String relativeImageHref = imageElement.getAttributeValue("src");
                String absoluteImageHref = calculateAbsoluteImageHref(relativeImageHref, coverPageResource.getHref());
                ImageResource imageResource = (ImageResource)resources.getByHref(absoluteImageHref);
                if (imageResource != null)
                {
                    return imageResource;
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    // package
    static String calculateAbsoluteImageHref(String relativeImageHref,
                                             String baseHref)
    {
        if (relativeImageHref.startsWith("/"))
        {
            return relativeImageHref;
        }
        String result = FilenameUtils.normalize(baseHref.substring(0, baseHref.lastIndexOf('/') + 1) + relativeImageHref, true);
        return result;
    }

    private String createCoverpageHtml(String imageHref, double width, double height)
    {
        return "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n" +
                "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                "\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "  <title>Cover</title>\n" +
                "  \n" +
                "<style type=\"text/css\">\n" +
                "div.sgc-1 {text-align: center; padding: 0pt; margin: 0pt;}\n" +
                "</style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "  <div class=\"sgc-1\">\n" +
                "    <svg xmlns=\"http://www.w3.org/2000/svg\" height=\"100%\" preserveAspectRatio=\"xMidYMid meet\" version=\"1.1\" viewBox=\"0 0 " + (long)width + " " + (long)height + "\" width=\"100%\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "      <image width=\"" + (long)width + "\" height=\"" + (long)height + "\"  xlink:href=\"/" + StringEscapeUtils.escapeHtml4(imageHref) + "\"></image>\n" +
                "    </svg>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

/*    private Dimension calculateResizeSize(BufferedImage image)
    {
        Dimension result;
        if (image.getWidth() > image.getHeight())
        {
            result = new Dimension(MAX_COVER_IMAGE_SIZE, (int) (((double) MAX_COVER_IMAGE_SIZE / (double) image.getWidth()) * (double) image.getHeight()));
        }
        else
        {
            result = new Dimension((int) (((double) MAX_COVER_IMAGE_SIZE / (double) image.getHeight()) * (double) image.getWidth()), MAX_COVER_IMAGE_SIZE);
        }
        return result;
    }


    @SuppressWarnings("unused")
    private byte[] createThumbnail(byte[] imageData) throws IOException
    {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        Dimension thumbDimension = calculateResizeSize(originalImage);
        BufferedImage thumbnailImage = createResizedCopy(originalImage, (int) thumbDimension.getWidth(), (int) thumbDimension.getHeight(), false);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ImageIO.write(thumbnailImage, "jpg", result);
        return result.toByteArray();

    }

    private BufferedImage createResizedCopy(java.awt.Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha)
    {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha)
        {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }*/

    @Override
    public Resource processResource(Resource resource)
    {
        return resource;
    }
}
