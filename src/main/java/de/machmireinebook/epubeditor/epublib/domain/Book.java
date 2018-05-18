package de.machmireinebook.epubeditor.epublib.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.util.IteratorIterable;

import de.machmireinebook.epubeditor.epublib.EpubVersion;
import de.machmireinebook.epubeditor.epublib.epub.NCXDocument;
import de.machmireinebook.epubeditor.epublib.epub.PackageDocumentWriter;
import de.machmireinebook.epubeditor.jdom2.AtrributeElementFilter;
import de.machmireinebook.epubeditor.xhtml.XHTMLUtils;


/**
 * Representation of a Book.
 * <p>
 * All resources of a Book (html, css, xml, fonts, images) are represented as Resources. See getResources() for access to these.<br/>
 * A Book as 3 indexes into these Resources, as per the epub specification.<br/>
 * <dl>
 * <dt>Spine</dt>
 * <dd>these are the Resources to be shown when a user reads the book from start to finish.</dd>
 * <dt>Table of Contents<dt>
 * <dd>The table of contents. Table of Contents references may be in a different order and contain different Resources than the spine, and often do.
 * <dt>Guide</dt>
 * <dd>The Guide has references to a set of special Resources like the cover page, the Glossary, the copyright page, etc.
 * </dl>
 * <p>
 * The complication is that these 3 indexes may and usually do point to different pages.
 * A chapter may be split up in 2 pieces to fit it in to memory. Then the spine will contain both pieces, but the Table of Contents only the first.
 * The Content page may be in the Table of Contents, the Guide, but not in the Spine.
 * Etc.
 * <p>
 * <p>
 * <!-- Created with Inkscape (http://www.inkscape.org/) -->
 * <p>
 * <svg id="svg2" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://www.w3.org/2000/svg" height="568.44" width="670.93" version="1.1" xmlns:cc="http://creativecommons.org/ns#" xmlns:dc="http://purl.org/dc/elements/1.1/">
 * <p>
 * <defs id="defs4">
 * <p>
 * <marker id="Arrow1Lend" refY="0" refX="0" orient="auto">
 * <p>
 * <path id="path4761" style="marker-start:none;" d="M0,0,5-5-12.5,0,5,5,0,0z" fill-rule="evenodd" transform="matrix(-0.8,0,0,-0.8,-10,0)" stroke="#000" stroke-width="1pt"/>
 * <p>
 * </marker>
 * <p>
 * </defs>
 * <p>
 * <metadata id="metadata7">
 * <p>
 * <rdf:RDF>
 * <p>
 * <cc:Work rdf:about="">
 * <p>
 * <dc:format>image/svg+xml</dc:format>
 * <p>
 * <dc:type rdf:resource="http://purl.org/dc/dcmitype/StillImage"/>
 * <p>
 * <dc:title/>
 * <p>
 * </cc:Work>
 * <p>
 * </rdf:RDF>
 * <p>
 * </metadata>
 * <p>
 * <g id="layer1" transform="translate(-46.64286,-73.241096)">
 * <p>
 * <path id="path2985" stroke-linejoin="miter" d="m191.18,417.24c-34.136,16.047-57.505,49.066-54.479,77.983,4.5927,43.891,50.795,88.762,106.42,108.46,73.691,26.093,175.45,22.576,247.06-6.2745,42.755-17.226,76.324-53.121,79.818-87.843,3.8921-38.675-21.416-85.828-68.415-105.77-88.899-37.721-224.06-27.142-310.4,13.445z" stroke-dashoffset="0" stroke="#000" stroke-linecap="butt" stroke-miterlimit="4" stroke-dasharray="1.49193191, 2.98386382" stroke-width="0.74596596" fill="none"/>
 * <p>
 * <g id="g3879" stroke="#000" fill="none" transform="matrix(0.50688602,0,0,0.50688602,141.59593,389.57252)">
 * <p>
 * <rect id="rect3759" stroke-dashoffset="0" height="83.406" width="60.182" stroke-dasharray="none" stroke-miterlimit="4" y="126.91" x="70.173" stroke-width="0.60862"/>
 * <p>
 * <path id="path3761" stroke-linejoin="miter" d="m76.437,137.92,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8" stroke-linejoin="miter" d="m76.437,144.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6" stroke-linejoin="miter" d="m76.437,152.82,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8" stroke-linejoin="miter" d="m76.437,159.39,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2" stroke-linejoin="miter" d="m76.437,166.58,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2" stroke-linejoin="miter" d="m76.437,173.15,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6-6" stroke-linejoin="miter" d="m76.437,181.48,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8-7" stroke-linejoin="miter" d="m76.437,188.05,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2-1" stroke-linejoin="miter" d="m76.437,194.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2-0" stroke-linejoin="miter" d="m76.437,201.06,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * </g>
 * <p>
 * <g id="g3879-5" stroke="#000" fill="none" transform="matrix(0.50688602,0,0,0.50688602,220.60629,374.03899)">
 * <p>
 * <rect id="rect3759-7" stroke-dashoffset="0" height="83.406" width="60.182" stroke-dasharray="none" stroke-miterlimit="4" y="126.91" x="70.173" stroke-width="0.60862"/>
 * <p>
 * <path id="path3761-26" stroke-linejoin="miter" d="m76.437,137.92,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-5" stroke-linejoin="miter" d="m76.437,144.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6-1" stroke-linejoin="miter" d="m76.437,152.82,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8-8" stroke-linejoin="miter" d="m76.437,159.39,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2-9" stroke-linejoin="miter" d="m76.437,166.58,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2-2" stroke-linejoin="miter" d="m76.437,173.15,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6-6-8" stroke-linejoin="miter" d="m76.437,181.48,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8-7-0" stroke-linejoin="miter" d="m76.437,188.05,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2-1-4" stroke-linejoin="miter" d="m76.437,194.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2-0-2" stroke-linejoin="miter" d="m76.437,201.06,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * </g>
 * <p>
 * <g id="g3879-75" stroke="#000" fill="none" transform="matrix(0.50688602,0,0,0.50688602,390.60629,376.89613)">
 * <p>
 * <rect id="rect3759-8" stroke-dashoffset="0" height="83.406" width="60.182" stroke-dasharray="none" stroke-miterlimit="4" y="126.91" x="70.173" stroke-width="0.60862"/>
 * <p>
 * <path id="path3761-5" stroke-linejoin="miter" d="m76.437,137.92,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-56" stroke-linejoin="miter" d="m76.437,144.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6-7" stroke-linejoin="miter" d="m76.437,152.82,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8-0" stroke-linejoin="miter" d="m76.437,159.39,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2-7" stroke-linejoin="miter" d="m76.437,166.58,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2-4" stroke-linejoin="miter" d="m76.437,173.15,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6-6-2" stroke-linejoin="miter" d="m76.437,181.48,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8-7-2" stroke-linejoin="miter" d="m76.437,188.05,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2-1-78" stroke-linejoin="miter" d="m76.437,194.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2-0-7" stroke-linejoin="miter" d="m76.437,201.06,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * </g>
 * <p>
 * <g id="g3879-0" stroke="#000" fill="none" transform="matrix(0.50688602,0,0,0.50688602,344.89201,451.18184)">
 * <p>
 * <rect id="rect3759-74" stroke-dashoffset="0" height="83.406" width="60.182" stroke-dasharray="none" stroke-miterlimit="4" y="126.91" x="70.173" stroke-width="0.60862"/>
 * <p>
 * <path id="path3761-7" stroke-linejoin="miter" d="m76.437,137.92,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-3" stroke-linejoin="miter" d="m76.437,144.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6-2" stroke-linejoin="miter" d="m76.437,152.82,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8-52" stroke-linejoin="miter" d="m76.437,159.39,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2-90" stroke-linejoin="miter" d="m76.437,166.58,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2-3" stroke-linejoin="miter" d="m76.437,173.15,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6-6-7" stroke-linejoin="miter" d="m76.437,181.48,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8-7-09" stroke-linejoin="miter" d="m76.437,188.05,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2-1-1" stroke-linejoin="miter" d="m76.437,194.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2-0-4" stroke-linejoin="miter" d="m76.437,201.06,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * </g>
 * <p>
 * <g id="g3879-05" stroke="#000" fill="none" transform="matrix(0.50688602,0,0,0.50688602,447.74915,459.75326)">
 * <p>
 * <rect id="rect3759-69" stroke-dashoffset="0" height="83.406" width="60.182" stroke-dasharray="none" stroke-miterlimit="4" y="126.91" x="70.173" stroke-width="0.60862"/>
 * <p>
 * <path id="path3761-40" stroke-linejoin="miter" d="m76.437,137.92,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-6" stroke-linejoin="miter" d="m76.437,144.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6-3" stroke-linejoin="miter" d="m76.437,152.82,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8-72" stroke-linejoin="miter" d="m76.437,159.39,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2-6" stroke-linejoin="miter" d="m76.437,166.58,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2-9" stroke-linejoin="miter" d="m76.437,173.15,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-6-6-23" stroke-linejoin="miter" d="m76.437,181.48,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-8-7-4" stroke-linejoin="miter" d="m76.437,188.05,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-2-1-36" stroke-linejoin="miter" d="m76.437,194.49,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * <path id="path3761-8-2-0-9" stroke-linejoin="miter" d="m76.437,201.06,47.289,0" stroke-linecap="butt" stroke-width="0.89265037px"/>
 * <p>
 * </g>
 * <p>
 * <g id="g4373" transform="matrix(0.73826572,0,0,0.77895183,-12.385803,230.83289)">
 * <p>
 * <path id="path4359" d="m463.57,320.22,58.571,0c-6.6549-9.2417-17.897-15-29.286-15-11.388,0-22.631,5.7583-29.286,15" fill="#0F0"/>
 * <p>
 * <path id="path4363" d="m500.71,294.15-12.5,7.8571,23.929,0-11.429-7.8571" fill="#F00"/>
 * <p>
 * <rect id="rect4367" height="10.357" width="17.143" y="302.01" x="492.14" fill="#A40"/>
 * <p>
 * <rect id="rect4369" height="18.929" width="3.5714" y="296.65" x="476.43" fill="#520"/>
 * <p>
 * <path id="path4371" d="m490,292.01c0,4.1421-5.3566,7.5-11.964,7.5-6.6077,0-11.964-3.3579-11.964-7.5s5.3566-7.5,11.964-7.5c6.6077,0,11.964,3.3579,11.964,7.5z" transform="matrix(1,0,0,1.2619048,0,-78.441795)" fill="#008000"/>
 * <p>
 * <rect id="rect3759-8-0" stroke-dashoffset="0" transform="matrix(0,1,-1,0,0,0)" height="65.034" width="44.775" stroke="#000" stroke-dasharray="none" stroke-miterlimit="4" y="-525.55" x="275.43" stroke-width="0.46356" fill="none"/>
 * <p>
 * </g>
 * <p>
 * <g id="g4373-9" transform="matrix(0.73826572,0,0,0.77895183,-109.70121,291.80218)">
 * <p>
 * <path id="path4359-7" d="m463.57,320.22,58.571,0c-6.6549-9.2417-17.897-15-29.286-15-11.388,0-22.631,5.7583-29.286,15" fill="#0F0"/>
 * <p>
 * <path id="path4363-7" d="m500.71,294.15-12.5,7.8571,23.929,0-11.429-7.8571" fill="#F00"/>
 * <p>
 * <rect id="rect4367-3" height="10.357" width="17.143" y="302.01" x="492.14" fill="#A40"/>
 * <p>
 * <rect id="rect4369-7" height="18.929" width="3.5714" y="296.65" x="476.43" fill="#520"/>
 * <p>
 * <path id="path4371-9" d="m490,292.01c0,4.1421-5.3566,7.5-11.964,7.5-6.6077,0-11.964-3.3579-11.964-7.5s5.3566-7.5,11.964-7.5c6.6077,0,11.964,3.3579,11.964,7.5z" transform="matrix(1,0,0,1.2619048,0,-78.441795)" fill="#008000"/>
 * <p>
 * <rect id="rect3759-8-0-4" stroke-dashoffset="0" transform="matrix(0,1,-1,0,0,0)" height="65.034" width="44.775" stroke="#000" stroke-dasharray="none" stroke-miterlimit="4" y="-525.55" x="275.43" stroke-width="0.46356" fill="none"/>
 * <p>
 * </g>
 * <p>
 * <rect id="rect4465" height="217.14" width="137.14" stroke="#000" y="139.51" x="67.143" fill="none"/>
 * <p>
 * <rect id="rect4467" stroke-dashoffset="0" height="44.286" width="97.143" stroke="#000" stroke-dasharray="none" stroke-miterlimit="4" y="163.79" x="89.286" stroke-width="1" fill="none"/>
 * <p>
 * <rect id="rect4467-0" stroke-dashoffset="0" height="44.286" width="97.143" stroke="#000" stroke-dasharray="none" stroke-miterlimit="4" y="237.36" x="89.286" stroke-width="1" fill="none"/>
 * <p>
 * <rect id="rect4467-0-7" stroke-dashoffset="0" height="44.286" width="97.143" stroke="#000" stroke-dasharray="none" stroke-miterlimit="4" y="298.79" x="89.286" stroke-width="1" fill="none"/>
 * <p>
 * <text id="text4507" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="40px" font-style="normal" y="122.36219" x="88.571434" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4509" x="88.571434" y="122.36219">Spine</tspan></text>
 * <p>
 * <rect id="rect4465-2" height="147.54" width="137.14" stroke="#000" y="162.39" x="327.14" stroke-width="0.8243" fill="none"/>
 * <p>
 * <rect id="rect4467-8" stroke-dashoffset="0" height="44.286" width="97.143" stroke="#000" stroke-dasharray="none" stroke-miterlimit="4" y="185.24" x="349.29" stroke-width="1" fill="none"/>
 * <p>
 * <rect id="rect4467-0-7-2" stroke-dashoffset="0" height="44.286" width="97.143" stroke="#000" stroke-dasharray="none" stroke-miterlimit="4" y="248.82" x="349.29" stroke-width="1" fill="none"/>
 * <p>
 * <text id="text4507-3" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="40px" font-style="normal" y="142.38702" x="262.85712" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4509-8" x="262.85712" y="142.38702">Table of Contents</tspan></text>
 * <p>
 * <rect id="rect4465-9" height="163.3" width="137.14" stroke="#000" y="225.24" x="560" stroke-width="0.86719" fill="none"/>
 * <p>
 * <rect id="rect4467-4" stroke-dashoffset="0" height="44.286" width="97.143" stroke="#000" stroke-dasharray="none" stroke-miterlimit="4" y="249.53" x="582.14" stroke-width="1" fill="none"/>
 * <p>
 * <rect id="rect4467-0-8" stroke-dashoffset="0" height="44.286" width="97.143" stroke="#000" stroke-dasharray="none" stroke-miterlimit="4" y="323.1" x="582.14" stroke-width="1" fill="none"/>
 * <p>
 * <text id="text4507-5" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="40px" font-style="normal" y="208.1013" x="581.42853" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4509-1" x="581.42853" y="208.1013">Guide</tspan></text>
 * <p>
 * <text id="text4577" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="21.50233269px" font-style="normal" y="188.89537" x="92.349854" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4579" x="92.349854" y="188.89537">Chapter 1</tspan></text>
 * <p>
 * <text id="text4577-0" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="21.50233269px" font-style="normal" y="255.01701" x="92.76873" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4579-5" x="92.76873" y="255.01701">Chapter 1</tspan></text>
 * <p>
 * <text id="text4577-0-3" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="21.50233269px" font-style="normal" y="278.23132" x="108.66158" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4579-5-9" x="108.66158" y="278.23132">Part 2</tspan></text>
 * <p>
 * <text id="text4577-0-6" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="21.50233269px" font-style="normal" y="327.33847" x="90.983017" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4579-5-1" x="90.983017" y="327.33847">Chapter 2</tspan></text>
 * <p>
 * <text id="text4577-6" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="21.50233269px" font-style="normal" y="215.1956" x="351.34015" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4579-7" x="351.34015" y="215.1956">Chapter 1</tspan></text>
 * <p>
 * <text id="text4577-0-6-1" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="21.50233269px" font-style="normal" y="276.62418" x="351.36185" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4579-5-1-0" x="351.36185" y="276.62418">Chapter 2</tspan></text>
 * <p>
 * <text id="text4577-6-5" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="21.50233269px" font-style="normal" y="278.05276" x="598.48297" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4579-7-9" x="598.48297" y="278.05276">Cover</tspan></text>
 * <p>
 * <text id="text4507-1" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="40px" font-style="normal" y="418.66241" x="238.73047" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4509-6" x="238.73047" y="418.66241">Resources</tspan></text>
 * <p>
 * <text id="text4577-6-5-4" style="letter-spacing:0px;word-spacing:0px;" font-weight="normal" xml:space="preserve" font-size="21.50233269px" font-style="normal" y="351.48663" x="594.909" font-family="Sans" line-height="125%" fill="#000000"><tspan id="tspan4749" x="594.909" y="351.48663">Preface</tspan></text>
 * <p>
 * <path id="path5205" stroke-linejoin="miter" style="marker-end:url(#Arrow1Lend);" d="M148.67,208.08,261.11,438.37" stroke="#000" stroke-linecap="butt" stroke-width="1px" fill="none"/>
 * <p>
 * <path id="path5207" stroke-linejoin="miter" style="marker-end:url(#Arrow1Lend);" d="M386.62,229.53,278.57,442.36" stroke="#000" stroke-linecap="butt" stroke-width="1px" fill="none"/>
 * <p>
 * <path id="path5211" stroke-linejoin="miter" style="marker-end:url(#Arrow1Lend);" d="m143.46,281.65,43.605,172.25" stroke="#000" stroke-linecap="butt" stroke-width="1px" fill="none"/>
 * <p>
 * <path id="path5213" stroke-linejoin="miter" style="marker-end:url(#Arrow1Lend);" d="M186.27,343.08,431.43,455.22" stroke="#000" stroke-linecap="butt" stroke-width="1px" fill="none"/>
 * <p>
 * <path id="path5215" stroke-linejoin="miter" style="marker-end:url(#Arrow1Lend);" d="m402.9,293.1,33.719,148.12" stroke="#000" stroke-linecap="butt" stroke-width="1px" fill="none"/>
 * <p>
 * <path id="path5219" stroke-linejoin="miter" style="marker-end:url(#Arrow1Lend);" d="M610.94,293.82,404.29,525.22" stroke="#000" stroke-linecap="butt" stroke-width="1px" fill="none"/>
 * <p>
 * <path id="path5221" stroke-linejoin="miter" style="marker-end:url(#Arrow1Lend);" d="M616.08,367.39,512.54,524.08" stroke="#000" stroke-linecap="butt" stroke-width="1px" fill="none"/>
 * <p>
 * </g>
 * <p>
 * </svg>
 *
 * @author paul
 */
@Named
public class Book implements Serializable
{
    private static final Logger logger = Logger.getLogger(Book.class);
    private static final long serialVersionUID = 2068355170895770100L;

    private Resources resources = new Resources();
    private Metadata metadata;
    private Spine spine = new Spine();
    private TableOfContents tableOfContents = new TableOfContents();
    private Guide guide = new Guide();
    private ObjectProperty<Resource> opfResource = new SimpleObjectProperty<>();
    private Resource ncxResource;
    private ImageResource coverImage;

    // Property
    private EpubVersion version = EpubVersion.VERSION_2;

    private final ReadOnlyObjectWrapper<EpubVersion> versionProperty = new ReadOnlyObjectWrapper<>(version, "version");
    public final ReadOnlyObjectProperty<EpubVersion> versionProperty() {
       return versionProperty.getReadOnlyProperty();
    }
    public final EpubVersion get() {
       return versionProperty.get();
    }

    private boolean isFixedLayout = false;
    private int fixedLayoutWidth;
    private int fixedLayoutHeight;
    private Resource epub3NavResource;
    private Resource appleDisplayOptions;

    private BooleanProperty bookIsChanged = new SimpleBooleanProperty(false);
    private Path physicalFileName;

    public static Book createMinimalBook()
    {
        Book book = new Book();

        try
        {
            Resource ncxResource = NCXDocument.createNCXResource(book);
            book.setNcxResource(ncxResource);
            book.getSpine().setTocResource(ncxResource);
            book.addResource(ncxResource, false);

            Resource opfResource = PackageDocumentWriter.createOPFResource(book);
            book.setOpfResource(opfResource);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }

        Resource textRes = book.addResourceFromTemplate("/epub/template.xhtml", "Text/text-0001.xhtml");
        book.addSection("Start", textRes);

        book.addResourceFromTemplate("/epub/standard-small.css", "Styles/standard.css");

        return book;
    }

    public Resource addResourceFromTemplate(String templateFileName, String href)
    {
        File file = new File(Book.class.getResource(templateFileName).getFile());
        Resource res = createResourceFromFile(file, href, MediaType.getByFileName(href));
        addResource(res);
        if (MediaType.XHTML.equals(res.getMediaType()))
        {
            try
            {
                String content = new String(res.getData(), res.getInputEncoding());
                content = content.replace("${title}", getTitle());
                if (isEpub3() && isFixedLayout())
                {
                    content = content.replace("${width}", String.valueOf(getFixedLayoutWidth()));
                    content = content.replace("${height}", String.valueOf(getFixedLayoutHeight()));
                }
                res.setData(content.getBytes(res.getInputEncoding()));
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
        }
        return res;
    }

    public Resource addResourceFromFile(File file, String href, MediaType mediaType)
    {
        Resource res = createResourceFromFile(file, href, mediaType);
        addResource(res);
        return res;
    }

    public Resource addSpineResourceFromFile(File file, String href, MediaType mediaType) throws IOException
    {
        Resource res = createResourceFromFile(file, href, mediaType);
        res = XHTMLUtils.fromHtml(res);
        addSpineResource(res);
        return res;
    }

    public Resource createResourceFromFile(File file, String href, MediaType mediaType)
    {
        Resource res = mediaType.getResourceFactory().createResource(href);
        logger.info("reading file " + file.getName() + " for adding as resource");
        byte[] content = null;
        InputStream is = null;
        try
        {
            is = new FileInputStream(file);
            content = IOUtils.toByteArray(is);
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        finally
        {
            try
            {
                if (is != null)
                {
                    is.close();
                }
            }
            catch (IOException e)
            {
                logger.error("", e);
            }
        }
        res.setData(content);
        res.setMediaType(mediaType);
        return res;
    }

    /**
     * Adds the resource to the table of contents of the book as a child section of the given parentSection
     *
     * @param parentSection
     * @param sectionTitle
     * @param resource
     * @return The table of contents
     */
    public TocEntry addSection(TocEntry parentSection, String sectionTitle,
                               Resource resource)
    {
        getResources().add(resource);
        if (spine.findFirstResourceById(resource.getId()) < 0)
        {
            spine.addSpineReference(new SpineReference(resource), null);
        }
        return parentSection.addChildSection(new TocEntry(sectionTitle, resource));
    }

    public void generateSpineFromTableOfContents()
    {
        Spine spine = new Spine(tableOfContents);

        // in case the tocResource was already found and assigned
        spine.setTocResource(this.spine.getTocResource());

        this.spine = spine;
    }

    /**
     * Adds a resource to the book's set of resources, table of contents and if there is no resource with the id in the spine also adds it to the spine.
     *
     * @param title
     * @param resource
     * @return The table of contents
     */
    public TocEntry addSection(String title, Resource resource)
    {
        getResources().add(resource);
        TocEntry tocReference = tableOfContents.addTOCReference(new TocEntry(title, resource));
        if (spine.findFirstResourceById(resource.getId()) < 0)
        {
            spine.addSpineReference(new SpineReference(resource), null);
        }
        return tocReference;
    }

    public SpineReference addSpineResource(Resource resource)
    {
        return addSpineResource(resource, null);
    }

    public SpineReference addSpineResource(Resource resource, Integer index)
    {
        getResources().add(resource);
        SpineReference ref = null;
        if (spine.findFirstResourceById(resource.getId()) < 0)
        {
            ref = spine.addSpineReference(new SpineReference(resource), index);
        }
        refreshOpfResource();
        return ref;
    }

    public void removeResource(Resource resource)
    {
        getResources().remove(resource);
        if (resource.getMediaType().equals(MediaType.CSS))
        {
            String cssFileName = resource.getFileName();
            //aus allen XHTML-Dateien entfernen
            List<Resource> xhtmlResources = getResources().getResourcesByMediaType(MediaType.XHTML);
            for (Resource xhtmlResource : xhtmlResources)
            {
                Document document = ((XHTMLResource) xhtmlResource).asNativeFormat();
                if (document != null)
                {
                    Element root = document.getRootElement();
                    if (root != null)
                    {
                        Element headElement = root.getChild("head");
                        if (headElement != null)
                        {
                            List<Element> linkElements = headElement.getChildren("link");
                            Element toRemove = null;
                            for (Element linkElement : linkElements)
                            {
                                if ("stylesheet".equals(linkElement.getAttributeValue("rel"))
                                        && linkElement.getAttributeValue("href").contains(cssFileName))
                                {
                                    toRemove = linkElement;
                                    break;
                                }
                            }
                            if (toRemove != null)
                            {
                                headElement.removeContent(toRemove);
                            }
                        }
                    }
                }
            }
        }
        refreshOpfResource();
    }


    public SpineReference removeSpineResource(Resource resource)
    {
        getResources().remove(resource);
        int index = spine.findFirstResourceById(resource.getId());
        SpineReference ref = null;
        if (index >= 0)
        {
            ref = spine.getSpineReferences().remove(index);
        }
        refreshOpfResource();
        return ref;
    }

    public void refreshOpfResource()
    {
        opfResource.get().setData(PackageDocumentWriter.createOPFContent(this));
    }

    public String getNextStandardFileName(MediaType mediaType)
    {
        int lastNumber = 0;
        for (Resource resource : getResources().getResourcesByMediaType(mediaType))
        {
            String fileName = resource.getFileName();
            if (fileName.startsWith(mediaType.getFileNamePrefix()))
            {
                String[] splitted = fileName.split("-");
                if (splitted.length > 1)
                {
                    String numberPart = splitted[1];
                    numberPart = numberPart.replace(mediaType.getDefaultExtension(), "");
                    int number = Integer.parseInt(numberPart);
                    if (number > lastNumber)
                    {
                        lastNumber = number;
                    }
                }
            }
        }
        return mediaType.getFileNamePrefix() + "-" + StringUtils.leftPad(String.valueOf(lastNumber + 1), 4, "0")
                + mediaType.getDefaultExtension();
    }

    /**
     * The Book's metadata (titles, authors, etc)
     *
     * @return The Book's metadata (titles, authors, etc)
     */
    public Metadata getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Metadata metadata)
    {
        this.metadata = metadata;
    }


    public void setResources(Resources resources)
    {
        resources.getResourcesMap().values().forEach(resource -> resource.hrefProperty().addListener((observable, oldValue, newValue) ->
        {
            renameResource(resource, oldValue, newValue);
        }));
        this.resources = resources;

    }


    public void addResource(Resource resource)
    {
        addResource(resource, true);
    }

    public void addResource(Resource resource, boolean refreshOpf)
    {
        resource.hrefProperty().addListener((observable, oldValue, newValue) -> renameResource(resource, oldValue, newValue));

        if (resource instanceof ImageResource)
        {
            ImageResource imageResource = (ImageResource) resource;
            imageResource.coverProperty().addListener((observable, oldValue, newValue) ->
            {
                if (newValue)
                {
                    this.coverImage = imageResource;
                    refreshOpfResource();
                }
                if (oldValue && coverImage == imageResource)  //wenn das alte nur ausgeschaltet wird, aber vorher kein neues gesetzt wurde
                {
                    this.coverImage = null;
                    refreshOpfResource();
                }
            });
        }
        resources.add(resource);
        if (refreshOpf)
        {
            refreshOpfResource();
        }
    }

    /**
     * The collection of all images, chapters, sections, xhtml files, stylesheets, etc that make up the book.
     *
     * @return The collection of all images, chapters, sections, xhtml files, stylesheets, etc that make up the book.
     */
    public Resources getResources()
    {
        return resources;
    }


    /**
     * The sections of the book that should be shown if a user reads the book from start to finish.
     *
     * @return The Spine
     */
    public Spine getSpine()
    {
        return spine;
    }


    public void setSpine(Spine spine)
    {
        this.spine = spine;
    }


    /**
     * The Table of Contents of the book.
     *
     * @return The Table of Contents of the book.
     */
    public TableOfContents getTableOfContents()
    {
        return tableOfContents;
    }


    public void setTableOfContents(TableOfContents tableOfContents)
    {
        this.tableOfContents = tableOfContents;
    }

    /**
     * The book's cover page as a Resource.
     * An XHTML document containing a link to the cover image.
     *
     * @return The book's cover page as a Resource
     */
    public Resource getCoverPage()
    {
        return guide.getCoverPage();
    }


    public void setCoverPage(Resource coverPage)
    {
        if (coverPage == null)
        {
            return;
        }
        if (!resources.containsByHref(coverPage.getHref()))
        {
            resources.add(coverPage);
        }
        guide.setCoverPage(coverPage);
    }

    /**
     * Gets the first non-blank title from the book's metadata.
     *
     * @return the first non-blank title from the book's metadata.
     */
    public String getTitle()
    {
        return getMetadata().getFirstTitle();
    }


    /**
     * The book's cover image.
     *
     * @return The book's cover image.
     */
    public ImageResource getCoverImage()
    {
        return coverImage;
    }

    public void setCoverImage(ImageResource coverImage)
    {
        if (coverImage == null)
        {
            return;
        }
        if (!resources.containsByHref(coverImage.getHref()))
        {
            resources.add(coverImage);
        }
        this.coverImage = coverImage;
    }

    /**
     * The guide; contains references to special sections of the book like colophon, glossary, etc.
     *
     * @return The guide; contains references to special sections of the book like colophon, glossary, etc.
     */
    public Guide getGuide()
    {
        return guide;
    }

    /**
     * All Resources of the Book that can be reached via the Spine, the TableOfContents or the Guide.
     * <p>
     * Consists of a list of "reachable" resources:
     * <ul>
     * <li>The coverpage</li>
     * <li>The resources of the Spine that are not already in the result</li>
     * <li>The resources of the Table of Contents that are not already in the result</li>
     * <li>The resources of the Guide that are not already in the result</li>
     * </ul>
     * To get all html files that make up the epub file use {@link #getResources()}
     *
     * @return All Resources of the Book that can be reached via the Spine, the TableOfContents or the Guide.
     */
    public List<Resource> getContents()
    {
        Map<String, Resource> result = new LinkedHashMap<>();
        addToContentsResult(getCoverPage(), result);

        for (SpineReference spineReference : getSpine().getSpineReferences())
        {
            addToContentsResult(spineReference.getResource(), result);
        }

        for (Resource resource : getTableOfContents().getAllUniqueResources())
        {
            addToContentsResult(resource, result);
        }

        for (GuideReference guideReference : getGuide().getReferences())
        {
            addToContentsResult(guideReference.getResource(), result);
        }

        return new ArrayList<>(result.values());
    }

    /**
     * All Resources of the Book that can be reached via the Spine or the TableOfContents.
     * <p>
     * Consists of a list of "readable" resources, excludes the meta pages of the book like toc, cover page, impressum and so on:
     * <ul>
     * <li>The resources of the Spine that are not already in the result</li>
     * <li>The resources of the Table of Contents that are not already in the result</li>
     * </ul>
     * To get all html files that make up the epub file use {@link #getResources()}
     *
     * @return All Resources of the Book that can be reached via the Spine, the TableOfContents.
     */
    public List<Resource> getReadableContents()
    {
        Map<String, Resource> result = new LinkedHashMap<>();

        for (SpineReference spineReference : getSpine().getSpineReferences())
        {
            addToContentsResult(spineReference.getResource(), result);
        }

        for (Resource resource : getTableOfContents().getAllUniqueResources())
        {
            addToContentsResult(resource, result);
        }

        if (isEpub3() && getEpub3NavResource() != null)
        {
            result.remove(getEpub3NavResource().getHref());
        }
        else if (getGuide() != null) //EPUB 2
        {
            if (getGuide().getCoverPage() != null)
            {
                result.remove(getGuide().getCoverPage().getHref());
            }
            removeGuideEntries(result, GuideReference.Semantics.COPYRIGHT_PAGE);
            removeGuideEntries(result, GuideReference.Semantics.COVER);
            removeGuideEntries(result, GuideReference.Semantics.TITLE_PAGE);
            removeGuideEntries(result, GuideReference.Semantics.TOC);
        }

        return new ArrayList<>(result.values());
    }

    private void removeGuideEntries(Map<String, Resource> result, GuideReference.Semantics semantic)
    {
        List<GuideReference> copyrightPageReferences = getGuide().getGuideReferencesByType(semantic);
        if (!copyrightPageReferences.isEmpty())
        {
            for (GuideReference copyrightPageReference : copyrightPageReferences)
            {
                result.remove(copyrightPageReference.getResource().getHref());
            }
        }
    }

    private static void addToContentsResult(Resource resource, Map<String, Resource> allReachableResources)
    {
        if (resource != null && (!allReachableResources.containsKey(resource.getHref())))
        {
            allReachableResources.put(resource.getHref(), resource);
        }
    }

    public void setNcxResource(Resource ncxResource)
    {
        this.ncxResource = ncxResource;
    }

    public Resource getNcxResource()
    {
        return ncxResource;
    }

    public EpubVersion getVersion()
    {
        return version;
    }

    public void setVersion(EpubVersion version)
    {
        this.version = version;
    }

    public boolean isFixedLayout()
    {
        return (version == EpubVersion.VERSION_3 || version == EpubVersion.VERSION_3_1) && isFixedLayout;
    }

    public boolean isEpub3()
    {
        return (version == EpubVersion.VERSION_3 || version == EpubVersion.VERSION_3_1);
    }

    public void setFixedLayout(boolean isFixedLayout)
    {
        this.isFixedLayout = isFixedLayout;
    }

    public Resource getEpub3NavResource()
    {
        return epub3NavResource;
    }

    public void setEpub3NavResource(Resource epub3NavResource)
    {
        this.epub3NavResource = epub3NavResource;
    }

    public Resource getAppleDisplayOptions()
    {
        return appleDisplayOptions;
    }

    public void setAppleDisplayOptions(Resource appleDisplayOptions)
    {
        this.appleDisplayOptions = appleDisplayOptions;
    }

    public boolean getBookIsChanged()
    {
        return bookIsChanged.get();
    }

    public BooleanProperty bookIsChangedProperty()
    {
        return bookIsChanged;
    }

    public void setBookIsChanged(boolean bookIsChanged)
    {
        this.bookIsChanged.set(bookIsChanged);
    }

    public Resource getOpfResource()
    {
        return opfResource.get();
    }

    public ObjectProperty<Resource> opfResourceProperty()
    {
        return opfResource;
    }

    public void setOpfResource(Resource opfResource)
    {
        this.opfResource.set(opfResource);
    }

    public Path getPhysicalFileName()
    {
        return physicalFileName;
    }

    public void setPhysicalFileName(Path physicalFileName)
    {
        this.physicalFileName = physicalFileName;
    }

    public int getFixedLayoutWidth()
    {
        return fixedLayoutWidth;
    }

    public void setFixedLayoutWidth(int fixedLayoutWidth)
    {
        this.fixedLayoutWidth = fixedLayoutWidth;
    }

    public int getFixedLayoutHeight()
    {
        return fixedLayoutHeight;
    }

    public void setFixedLayoutHeight(int fixedLayoutHeight)
    {
        this.fixedLayoutHeight = fixedLayoutHeight;
    }

    public void renameResource(Resource resource, String oldValue, String newValue)
    {
        resources.remove(oldValue); //unter altem namen löschen
        resources.add(resource); //unter neuem wieder hinzufügen

        if (MediaType.CSS.equals(resource.getMediaType()))
        {
            //css umbenannt, erstmal alle XHTMLs durchsuchen
            List<Resource> xhtmlResources = resources.getResourcesByMediaType(MediaType.XHTML);
            Path resourcePath = resource.getHrefAsPath();
            int index = StringUtils.lastIndexOf(oldValue, "/");
            String oldFileName = oldValue;
            if (index > -1)
            {
                oldFileName = oldValue.substring(index + 1);
            }

            index = StringUtils.lastIndexOf(newValue, "/");
            String newFileName = newValue;
            if (index > -1)
            {
                newFileName = newValue.substring(index + 1);
            }

            for (Resource xhtmlResource : xhtmlResources)
            {
                Document document = ((XHTMLResource)xhtmlResource).asNativeFormat();
                Path relativePath = xhtmlResource.getHrefAsPath().relativize(resourcePath);
                AtrributeElementFilter hrefFilter = new AtrributeElementFilter("href", relativePath + "/" + oldFileName);
                IteratorIterable<Element> descendants = document.getDescendants(hrefFilter);
                for (Element descendant : descendants)
                {
                    logger.info("found element with attribut href in resource " + xhtmlResource);
                    descendant.setAttribute("href", relativePath + "/" + newFileName);
                }
                //nach noch mehr Elementen suchen
                //zB src-Attribut
                xhtmlResource.setData(XHTMLUtils.outputXHTMLDocument(document));
            }

            //weiter nach import etc. in anderen css dateien suchen
        }
        else if(MediaType.XHTML.equals(resource.getMediaType()))
        {
            //nach href
        }
        else if(resource.getMediaType().isBitmapImage())
        {
            //nach href und src
        }
        refreshOpfResource();
    }
}

