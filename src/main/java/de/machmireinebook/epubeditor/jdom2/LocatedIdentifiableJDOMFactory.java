package de.machmireinebook.epubeditor.jdom2;

/*--

 Copyright (C) 2012 Jason Hunter & Brett McLaughlin.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows
    these conditions in the documentation and/or other materials
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.

 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.

 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many
 individuals on behalf of the JDOM Project and was originally
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.

 */

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;

import de.machmireinebook.epubeditor.EpubEditorConfiguration;

/**
 * Extended LocatedJDOMFactory. On all elements a new attribute class with the line is added, to identify the line in
 * webview per javascript and enable the functionality to scroll to this element
 *
 */
public class LocatedIdentifiableJDOMFactory extends LocatedJDOMFactory {

    @Override
    public Element element(int line, int col, String name, Namespace namespace) {
        final LocatedElement ret = new LocatedElement(name, namespace);
        setLocationClassName(line, ret);
        ret.setLine(line);
        ret.setColumn(col);
        return ret;
    }

    @Override
    public Element element(int line, int col, String name) {
        final LocatedElement ret = new LocatedElement(name);
        setLocationClassName(line, ret);
        ret.setLine(line);
        ret.setColumn(col);
        return ret;
    }

    @Override
    public Element element(int line, int col, String name, String uri) {
        final LocatedElement ret = new LocatedElement(name, uri);
        setLocationClassName(line, ret);
        ret.setLine(line);
        ret.setColumn(col);
        return ret;
    }

    @Override
    public Element element(int line, int col, String name, String prefix,
                           String uri) {
        final LocatedElement ret = new LocatedElement(name, prefix, uri);
        setLocationClassName(line, ret);
        ret.setLine(line);
        ret.setColumn(col);
        return ret;
    }

    @Override
    public void setAttribute(Element parent, Attribute a) {
        if (parent instanceof LocatedElement && a.getName().equals("class") && !a.getValue().contains(EpubEditorConfiguration.LOCATION_CLASS_PREFIX)) {
            int line = ((LocatedElement)parent).getLine();
            a.setValue(a.getValue() + " " + EpubEditorConfiguration.LOCATION_CLASS_PREFIX + line);
        }
        parent.setAttribute(a);
    }

    private void setLocationClassName(int line, LocatedElement ret) {
        ret.setAttribute("class", EpubEditorConfiguration.LOCATION_CLASS_PREFIX + line);
    }
}

