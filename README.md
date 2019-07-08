# Scripto - EpubFx
## General
Scripto is an epub editor written in JavaFX.

The layout and the functionality of this application is inspired by the famous editor Sigil. But i want added some more convenient functions for easier and more user friendly epub editing. 

The editor supports ebooks in epub 2 and epub 3 format. 

## Functions
### Current features
This project is in early stage, but some functions are working:

- book browser
- Creating of ebooks by free configurable templates (put only a epub file into a folder, the system find it and the user can choose this epub as template)
- HTML-Editor with syntax highlighting and undo/redo
- CSS-Editor with syntax highlighting and undo/redo
- Applying text snippets and text snippets editor  
- inserting images and ohter media into book using configurable templates 
- Preview of xhtml file
- basic search function
- creating toc ncx (epub 2) and nav (epub 3)
- creating cover file by image
- splitting files

### Planned feature:
- inserting tables using configurable templates
- check of ebook with official epub checker
- spell check
- openeing files in external applications (for example to edit images)

## Build and Running

Scripto needs Java 11 or higher to run. The application is build by maven. Currently you can the application only running in an ide (like eclipse, intellij) by starting the `EpubEditorStarter` class. 

I plan to include the maven javafx plugin for easier running the app.   


  