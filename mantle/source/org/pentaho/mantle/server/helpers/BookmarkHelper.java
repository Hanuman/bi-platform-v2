/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.server.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pentaho.mantle.client.objects.Bookmark;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BookmarkHelper {

  public static String toXML(List<Bookmark> bookmarks) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
    org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    org.w3c.dom.Element root = document.createElement("bookmarks"); //$NON-NLS-1$
    document.appendChild(root);
    for (Bookmark bookmark: bookmarks) {
      org.w3c.dom.Element bookmarkElement = document.createElement("bookmark"); //$NON-NLS-1$
      bookmarkElement.setAttribute("title", bookmark.getTitle()); //$NON-NLS-1$
      bookmarkElement.setAttribute("url", bookmark.getUrl()); //$NON-NLS-1$
      bookmarkElement.setAttribute("group", bookmark.getGroup()); //$NON-NLS-1$
      root.appendChild(bookmarkElement);
    }
    DOMSource source = new DOMSource(document);
    StreamResult result = new StreamResult(new StringWriter());
    TransformerFactory.newInstance().newTransformer().transform(source, result);
    String theXML = result.getWriter().toString();
    return theXML;
  }
  
  public static List<Bookmark> fromXML(String xml) throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
    List<Bookmark> bookmarks = new ArrayList<Bookmark>();
    org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8"))); //$NON-NLS-1$
    NodeList nodeList = document.getElementsByTagName("bookmark"); //$NON-NLS-1$
    for (int i=0;i<nodeList.getLength();i++) {
      Element element = (Element)nodeList.item(i);
      Bookmark bookmark = new Bookmark();
      bookmark.setTitle(element.getAttribute("title")); //$NON-NLS-1$
      bookmark.setUrl(element.getAttribute("url")); //$NON-NLS-1$
      bookmark.setGroup(element.getAttribute("group")); //$NON-NLS-1$
      bookmarks.add(bookmark);
    }
    return bookmarks;
  }
  
}
