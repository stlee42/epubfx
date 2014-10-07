package de.machmireinebook.epubeditor.epublib.fileset;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.machmireinebook.epubeditor.epublib.Constants;
import de.machmireinebook.epubeditor.epublib.bookprocessor.DefaultBookProcessorPipeline;
import de.machmireinebook.epubeditor.epublib.domain.Book;
import de.machmireinebook.epubeditor.epublib.domain.MediaType;
import de.machmireinebook.epubeditor.epublib.domain.Resource;
import de.machmireinebook.epubeditor.epublib.domain.Resources;
import de.machmireinebook.epubeditor.epublib.domain.Spine;
import de.machmireinebook.epubeditor.epublib.domain.TOCReference;
import de.machmireinebook.epubeditor.epublib.domain.TableOfContents;
import de.machmireinebook.epubeditor.epublib.epub.BookProcessor;
import de.machmireinebook.epubeditor.epublib.util.ResourceUtil;
import de.machmireinebook.epubeditor.epublib.util.VFSUtil;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;

/**
 * Creates a Book from a collection of html and image files.
 * 
 * @author paul
 *
 */
public class FilesetBookCreator {
	
	private static Comparator<FileObject> fileComparator = new Comparator<FileObject>(){
		@Override
		public int compare(FileObject o1, FileObject o2) {
			return o1.getName().getBaseName().compareToIgnoreCase(o2.getName().getBaseName());
		}
	};
	
	private static final BookProcessor bookProcessor = new DefaultBookProcessorPipeline();
	
	public static Book createBookFromDirectory(File rootDirectory) throws IOException {
		return createBookFromDirectory(rootDirectory, Constants.CHARACTER_ENCODING);	
	}
	
	
	public static Book createBookFromDirectory(File rootDirectory, String encoding) throws IOException {
		FileObject rootFileObject = VFS.getManager().resolveFile("file:" + rootDirectory.getCanonicalPath());
		return createBookFromDirectory(rootFileObject, encoding);
	}
	
	public static Book createBookFromDirectory(FileObject rootDirectory) throws IOException {
		return createBookFromDirectory(rootDirectory, Constants.CHARACTER_ENCODING);
	}
	
	/**
	 * Recursively adds all files that are allowed to be part of an epub to the Book.
	 * 
	 * @see nl.siegmann.epublib.domain.MediaTypeService
	 * @param rootDirectory
	 * @return the newly created Book
	 * @throws java.io.IOException
	 */
	public static Book createBookFromDirectory(FileObject rootDirectory, String encoding) throws IOException {
		Book result = new Book();
		List<TOCReference> sections = new ArrayList<>();
		Resources resources = new Resources();
		processDirectory(rootDirectory, rootDirectory, sections, resources, encoding);
		result.setResources(resources);
		TableOfContents tableOfContents = new TableOfContents(sections);
		result.setTableOfContents(tableOfContents);
		result.setSpine(new Spine(tableOfContents));
		
		result = bookProcessor.processBook(result);
		
		return result;
	}

	private static void processDirectory(FileObject rootDir, FileObject directory, List<TOCReference> sections, Resources resources, String inputEncoding) throws IOException {
		FileObject[] files = directory.getChildren();
		Arrays.sort(files, fileComparator);
        for (FileObject file : files)
        {
            if (file.getType() == FileType.FOLDER)
            {
                processSubdirectory(rootDir, file, sections, resources, inputEncoding);
            }
            else if (MediaType.getByFileName(file.getName().getBaseName()) == null)
            {
                continue;
            }
            else
            {
                Resource resource = VFSUtil.createResource(rootDir, file, inputEncoding);
                if (resource == null)
                {
                    continue;
                }
                resources.add(resource);
                if (MediaType.XHTML == resource.getMediaType())
                {
                    TOCReference section = new TOCReference(file.getName().getBaseName(), resource);
                    sections.add(section);
                }
            }
        }
	}

	private static void processSubdirectory(FileObject rootDir, FileObject file,
			List<TOCReference> sections, Resources resources, String inputEncoding)
			throws IOException {
		List<TOCReference> childTOCReferences = new ArrayList<TOCReference>();
		processDirectory(rootDir, file, childTOCReferences, resources, inputEncoding);
		if(! childTOCReferences.isEmpty()) {
			String sectionName = file.getName().getBaseName();
			Resource sectionResource = ResourceUtil.createResource(sectionName, VFSUtil.calculateHref(rootDir,file));
			resources.add(sectionResource);
			TOCReference section = new TOCReference(sectionName, sectionResource);
			section.setChildren(childTOCReferences);
			sections.add(section);
		}
	}

}
