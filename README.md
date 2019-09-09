# SmoekerSchriever - EpubFx
## General
SmoekerSchriever (lower german for _book writer_) is an epub editor written in JavaFX.

The layout and the functionality of this application is inspired by the famous editor Sigil. But i want added some more convenient functions for easier and more user friendly epub editing. 

The editor supports ebooks in epub 2 and epub 3 format. 

## Functions
### Current features
This project is in early stage, but some functions are working (with possible bugs):

- book browser
- Creating of ebooks by free configurable templates (put only a epub file into a folder, the system find it and the user can choose this epub as template)
- HTML editor with syntax highlighting and undo/redo
- CSS editor with syntax highlighting and undo/redo
- auto completion of [, {, ( and " with the second part of it
- spell check
- Applying text snippets and text snippets editor  
- inserting images and other media into book using configurable templates 
- inserting tables using configurable templates
- inserting links to internal and external targets
- Preview of xhtml file
- basic search function
- creating toc ncx (epub 2) and nav (epub 3)
- creating cover file by image
- splitting files
- renaming files
- check of ebook with official epub checker (not configurable yet)

### Planned feature:

- opening files in external applications (for example to edit images), currently only configurable by editing configuration file directly
- checking internal and external links
- symbol table 
- europatastatur like inserting of symbols (adaption of key codes of the project https://www.europatastatur.de/)

## Build and Running

*SmoekerSchriever - epubfx* needs Java 11 or higher to run. The application is build by maven. Currently you can the application only running in an ide (like eclipse, intellij) by starting the `EpubEditorStarter` class. 

I plan to include the maven javafx plugin for easier running the app.  

## Usage
### General Keys
- CTRL-S: saves the ebook
- CTRL-F: opens the search panel
- CTRL-O: opens an ebook

### In book browser
#### Keys 
- CTRL-C on file item in tree: copy the file name
- CTRL-A: select all items
- F2: rename file
- DEL: delete file

#### Mouse
- double click: open file in editor (css, xml, xhtml)
- right (secondary button) click: open context menu  

### Editor 
#### Keys 
- DEL: delete forward
- BACKSPACE: delete back
- RETURN, ENTER: insert paragraph (not html paragraph <p></p>)
- TAB: insert tab or spaces (configurable how much spaces or tab character is used)

- CTRL-DEL: delete next word
- CTRL-BACKSPACE: delete previous word
 
- CTRL-I: wrap selected text with i-tag <i></i>

- CTRL-C: copy selection 
- CTRL-X: cut out selection
- CTRL-Y: paste
- CTRL-Z: undo
- CTRL-SHIFT-Z, CTRL-Y: redo

#### Mouse
- double click: select word
- triple click: select paragraph  
- SHIFT-Click: Select text from current caret position to mouse click position
- right (secondary button) click: open context menu

## Credits
The following software and frameworks are used in *SmoekerSchriever - epubfx*:
* Icons by [Icons8](https://icons8.com)
* [RibbonFx](https://pixelduke.com/fxribbon/) as ui copmponent 
* [languagetool](http://languagetool.org) for spell checking
* [htmlcleaner](https://sourceforge.net/projects/htmlcleaner/) for repair and importing (x)html files
* [PreferencesFX](https://github.com/dlsc-software-consulting-gmbh/PreferencesFX) for editing, storing and loading preferences
* [RichTextFx](https://github.com/FXMisc/RichTextFX) for code editing  
* [ControlsFx](https://github.com/controlsfx/controlsfx) for some ui components



