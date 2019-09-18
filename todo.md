## Bugs 
- metadata editor is not working (epub2 and epub3)
- if file is open and will be renamed, preview is not updated and tries to use the file with the old name
- when image is renamed, usages in xhtml is not updated
- sometimes selection of text with mouse is not possible
- EPUB 3: if cover is deleted or renamed, or other file is made to cover the landmarks in nav file are not updated
- EPUB 3: adding semantics are lost after reopening the file and will not be inserted into nav file 
- not expanded tags are not found by findSurroundingTags(), if cursor is inside tag `<img alt="" href="" /> `
- there is no sub tree for script resources
- and a lot more

## Improvements
- make spell check faster
- on opening large files, the initial spell check needs a lot of time (until 1 min) 
- search over more than one file
- if a template is used, ask user for author, title and some other basic metadata and change these data in new ebook
- implement configuration applications (text, image, css) for external editing
- updating preview is flickering and could be faster