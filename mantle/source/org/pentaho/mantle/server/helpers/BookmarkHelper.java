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
    org.w3c.dom.Element root = document.createElement("bookmarks");
    document.appendChild(root);
    for (Bookmark bookmark: bookmarks) {
      org.w3c.dom.Element bookmarkElement = document.createElement("bookmark");
      bookmarkElement.setAttribute("title", bookmark.getTitle());
      bookmarkElement.setAttribute("url", bookmark.getUrl());
      bookmarkElement.setAttribute("group", bookmark.getGroup());
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
    org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    NodeList nodeList = document.getElementsByTagName("bookmark");
    for (int i=0;i<nodeList.getLength();i++) {
      Element element = (Element)nodeList.item(i);
      Bookmark bookmark = new Bookmark();
      bookmark.setTitle(element.getAttribute("title"));
      bookmark.setUrl(element.getAttribute("url"));
      bookmark.setGroup(element.getAttribute("group"));
      bookmarks.add(bookmark);
    }
    return bookmarks;
  }
  
}
