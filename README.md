# SmoekerSchriever - EpubFx
## General
SmoekerSchriever (lower german for _book writer_) is an epub editor written in JavaFX.

The layout and the functionality of this application is inspired by the famous editor Sigil. But i want added some more convenient functions for easier and more user friendly epub editing. 

The editor supports ebooks in epub 2 and epub 3 format. 

## Functions
### Current features
This project is in early stage, but some functions are working:

- book browser
- Creating of ebooks by free configurable templates (put only a epub file into a folder, the system find it and the user can choose this epub as template)
- HTML-Editor with syntax highlighting and undo/redo
- CSS-Editor with syntax highlighting and undo/redo
- spell check
- Applying text snippets and text snippets editor  
- inserting images and other media into book using configurable templates 
- inserting tables using configurable templates
- Preview of xhtml file
- basic search function
- creating toc ncx (epub 2) and nav (epub 3)
- creating cover file by image
- splitting files
- enaming files

### Planned feature:
- check of ebook with official epub checker
- opening files in external applications (for example to edit images)
- checking internal and external links
- symbol table 
- europatastatur like inserting of symbols (adaption of key codes of the project https://www.europatastatur.de/)

## Build and Running

Scripto needs Java 11 or higher to run. The application is build by maven. Currently you can the application only running in an ide (like eclipse, intellij) by starting the `EpubEditorStarter` class. 

I plan to include the maven javafx plugin for easier running the app.  

## Editor
### Keys 
- DEL: Delete forward
- BACKSPACE: Delete back
- RETURN, ENTER: insert paragraph
- TAB: insert tab or spaces (configurable)
- CTRL-DEL: delete next word
- CTRL-BACKSPACE: delete previous word
- CTRL-C: Copy selection 
- CTRL-X: Cut out selection
- CTRL-Y: Paste
- CTRL-Z: undo
- CTRL-SHIFT-Z, CTRL-Y: redo

### Mouse
- Mouse double click: select word
- Mouse triple click: select paragraph  
- SHIFT-Mouse Click: Select text from current caret position to mouse click position


