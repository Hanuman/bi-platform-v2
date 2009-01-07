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
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.util.xml.dom4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.util.messages.Messages;
import org.pentaho.platform.util.xml.XmlHelper;
import org.xml.sax.EntityResolver;

// TODO sbarkdull, exernalize strings, comment methods

/**
 * A set of static methods to help in with: * the construction of XML DOM
 * Documents (org.dom4j.Document) from files, streams, and Strings * in the
 * creation of XML DOM Documents as the result of an XSLT transform * persisting
 * of XML DOM documents to the file system or a <code>Writer</code>. * the
 * encoding of a String of Xml text
 * 
 * Design notes: This class should never have any dependencies (i.e. imports) on
 * anything on org.pentaho or com.pentaho or their decendant packages. In
 * general, methods in the class should not attempt to handle exceptions, but
 * should let the exceptions propogate to the caller to be handled there. Please
 * do not use european-reuse in this class. One of the primary design goals for
 * this class was to construct it in a way that it could be used without change
 * outside of the Pentaho platform. Related XML-helper type code that is
 * dependant on the platform should be moved "up" to XmlHelper.
 */
public class XmlDom4JHelper {

  private static final Log logger = LogFactory.getLog(XmlDom4JHelper.class);

  /**
   * Create a <code>Document</code> from <code>str</code>.
   * 
   * @param str
   *          String containing the XML that will be used to create the Document
   * @param encoding
   *          String specifying the character encoding. The encoding of the xml
   *          String can be discovered by calling CleanXmlHelper.getEncoding().
   * @param resolver EntityResolver an instance of an EntityResolver that will resolve
   * any external URIs. See the docs on EntityResolver. null is an acceptable value.
   * @return <code>Document</code> initialized with the xml in
   *         <code>strXml</code>.
   * @throws DocumentException
   * @throws UnsupportedEncodingException
   */
  public static Document getDocFromString(final String strXml, final String encoding, final EntityResolver resolver) throws XmlParseException{
    byte[] bytes = null;
    Document document = null;
    InputStream inStrm = null;
    try {
      if (null != encoding) {
        bytes = strXml.getBytes(encoding);
      } else {
        // doh, we don't know the encoding, cross your fingers and hope for the best
        bytes = strXml.getBytes();
      }
      inStrm = new ByteArrayInputStream(bytes);
      document = XmlDom4JHelper.getDocFromStream(inStrm, encoding, resolver);
    } catch (DocumentException e) {
      throw  new XmlParseException(Messages.getErrorString("XmlDom4JHelper.ERROR_0001_UNABLE_TO_GET_DOCUMENT_FROM_STRING"), e); //$NON-NLS-1$
    } catch (UnsupportedEncodingException e) {
      throw  new XmlParseException(Messages.getErrorString("XmlDom4JHelper.ERROR_0002_UNSUPPORTED_ENCODING"), e); //$NON-NLS-1$
    } finally {
      XmlDom4JHelper.closeInputStream(inStrm);
    }
    return document;
  }

  /**
   * Create a <code>Document</code> from <code>str</code>.
   * 
   * @param str
   *          String containing the XML that will be used to create the Document
   *          can be discovered by calling CleanXmlHelper.getEncoding().
   * @param resolver EntityResolver an instance of an EntityResolver that will resolve
   * any external URIs. See the docs on EntityResolver. null is an acceptable value.
   * 
   * @return <code>Document</code> initialized with the xml in
   *         <code>strXml</code>.
   * @throws DocumentException
   */
  public static Document getDocFromString(final String strXml, final EntityResolver resolver) throws XmlParseException{
    String encoding = XmlHelper.getEncoding(strXml);
    return XmlDom4JHelper.getDocFromString(strXml, encoding, resolver);
  }

  /**
   * Create a <code>Document</code> from the contents of a file.
   * 
   * @param path
   *          String containing the path to the file containing XML that will be
   *          used to create the Document.
   * @param resolver EntityResolver an instance of an EntityResolver that will resolve
   * any external URIs. See the docs on EntityResolver. null is an acceptable value.
   * @return <code>Document</code> initialized with the xml in
   *         <code>strXml</code>.
   * @throws DocumentException
   *           if the document isn't valid
   * @throws IOException 
   */
  public static Document getDocFromFile(final String path, final EntityResolver resolver) throws DocumentException,
      IOException {
    File file = new File(path);
    return XmlDom4JHelper.getDocFromFile(file, resolver);
  }

  /**
   * Create a <code>Document</code> from the contents of a file.
   * 
   * @param path
   *          String containing the path to the file containing XML that will be
   *          used to create the Document.
   * @param resolver EntityResolver an instance of an EntityResolver that will resolve
   * any external URIs. See the docs on EntityResolver. null is an acceptable value.
   * @return <code>Document</code> initialized with the xml in
   *         <code>strXml</code>.
   * @throws DocumentException
   *           if the document isn't valid
   * @throws IOException 
   *           if the file doesn't exist
   */
  public static Document getDocFromFile(final File file, final EntityResolver resolver) throws DocumentException,
      IOException {

    InputStream fInStrm = null;
    Document document = null;
    try {
      String encoding = XmlHelper.getEncoding(file);
      fInStrm = new FileInputStream(file);
      document = XmlDom4JHelper.getDocFromStream(fInStrm, encoding, resolver);
    } finally {
      XmlDom4JHelper.closeInputStream(fInStrm);
    }
    return document;
  }

  /**
   * Create a <code>Document</code> from the contents of an input stream,
   * where the input stream contains valid XML.
   * 
   * NOTE: the method Document getDocFromStream(InputStream inStream) should
   * be preferred over this method since it examines the XML for the encoding
   * specified in the processing instruction. This relieves the caller of the
   * burden of discovering the encoding, but also assumes that the encoding
   * in the XML is specified properly.
   * 
   * @param inStream InputStream
   *          input stream to read the XML from
   * @param encoding String the character encoding of the bytes in <param>inStream</param>. For instance: UTF-8
   *          Can be null.
   * @param resolver EntityResolver an instance of an EntityResolver that will resolve
   * any external URIs. See the docs on EntityResolver. null is an acceptable value.
   * @return <code>Document</code> initialized with the xml in
   *         <code>strXml</code>.
   * @throws DocumentException
   *           if the document isn't valid
   * @throws FileNotFoundException
   *           if the file doesn't exist
   */
  private static Document getDocFromStream(final InputStream inStream, final String encoding,
      final EntityResolver resolver) throws DocumentException {

    SAXReader rdr = new SAXReader();
    if (null != encoding) {
      rdr.setEncoding(encoding);
    }
    if (null != resolver) {
      rdr.setEntityResolver(resolver);
    }
    Document document = rdr.read(inStream);
    return document;
  }

  /**
   * Create a <code>Document</code> from the contents of an input stream,
   * where the input stream contains valid XML.
   * 
   * @param inStream
   * @return
   * @throws DocumentException
   * @throws IOException
   */
  public static Document getDocFromStream(final InputStream inStream, final EntityResolver resolver)
      throws DocumentException, IOException {

    String encoding = XmlHelper.getEncoding(inStream);
    return XmlDom4JHelper.getDocFromStream(inStream, encoding, resolver);
  }

  /**
   * Create a <code>Document</code> from the contents of an input stream,
   * where the input stream contains valid XML.
   * 
   * @param inStream
   * @return
   * @throws DocumentException
   * @throws IOException
   */
  public static Document getDocFromStream(final InputStream inStream) throws DocumentException, IOException {

    return XmlDom4JHelper.getDocFromStream(inStream, null);
  }

  /**
   * Use the transform specified by xslSrc and transform the document specified
   * by docSrc, and return the resulting document.
   * 
   * @param xslSrc
   *          StreamSrc containing the xsl transform
   * @param docSrc
   *          StreamSrc containing the document to be transformed
   * @param params
   *          Map of properties to set on the transform
   * @param resolver
   *          URIResolver instance to resolve URI's in the output document.
   * 
   * @return StringBuffer containing the XML results of the transform
   * @throws TransformerConfigurationException
   *           if the TransformerFactory fails to create a Transformer.
   * @throws TransformerException
   *           if actual transform fails.
   */
  protected static final StringBuffer transformXml(final StreamSource xslSrc, final StreamSource docSrc,
      final Map params, final URIResolver resolver) throws TransformerConfigurationException, TransformerException {

    StringBuffer sb = null;
    StringWriter writer = new StringWriter();

    TransformerFactory tf = TransformerFactory.newInstance();
    if (null != resolver) {
      tf.setURIResolver(resolver);
    }
    // TODO need to look into compiling the XSLs...
    Transformer t = tf.newTransformer(xslSrc); // can throw
    // TransformerConfigurationException
    // Start the transformation
    if (params != null) {
      Set<?> keys = params.keySet();
      Iterator<?> it = keys.iterator();
      String key, val;
      while (it.hasNext()) {
        key = (String) it.next();
        val = (String) params.get(key);
        if (val != null) {
          t.setParameter(key, val);
        }
      }
    }
    t.transform(docSrc, new StreamResult(writer)); // can throw
    // TransformerException
    sb = writer.getBuffer();

    return sb;
  }

  /**
   * Convert a W3C Document to a String.
   * 
   * Note: if you are working with a dom4j Document, you can use it's asXml()
   * method.
   * 
   * @param doc
   *          org.w3c.dom.Document to be converted to a String.
   * @return String representing the XML document.
   * 
   * @throws TransformerConfigurationException
   *           If unable to get an instance of a Transformer
   * @throws TransformerException
   *           If the attempt to transform the document fails.
   */
  public static final StringBuffer docToString(final org.w3c.dom.Document doc)
      throws TransformerConfigurationException, TransformerException {

    StringBuffer sb = null;
    StringWriter writer = new StringWriter();

    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer(); // can throw
    // TransformerConfigurationException

    Source docSrc = new DOMSource(doc);
    t.transform(docSrc, new StreamResult(writer)); // can throw
    // TransformerException
    sb = writer.getBuffer();

    return sb;
  }

  // TODO sbarkdull, this code is duplicated in LocaleHelper
  /**
   * convert any character in the XML input (<code>rawValue</code>) whose
   * code position is greater than or equal to 0x080 to its Numeric Character
   * Reference. For a description of Numeric Character References see:
   * http://www.w3.org/TR/html4/charset.html#h-5.3.1
   * 
   * @param rawValue
   *          String containing the XML to be encoded.
   * @return String containing the encoded XML
   */
  public static String getXmlEncodedString(final String rawValue) {
    StringBuffer value = new StringBuffer();
    for (int n = 0; n < rawValue.length(); n++) {
      int charValue = rawValue.charAt(n);
      if (charValue >= 0x80) {
        value.append("&#x"); //$NON-NLS-1$
        value.append(Integer.toString(charValue, 0x10));
        value.append(";"); //$NON-NLS-1$
      } else {
        value.append((char) charValue);
      }
    }
    return value.toString();

  }

  /**
   * Write an XML document to a file using the specified character encoding.
   * 
   * @param doc
   *          Document to be written
   * @param filePath
   *          path identifying the File that will be the output of the Document
   * @param encoding
   *          String specifying the character encoding. Can be null, in which
   *          case the default encoding will be used. See
   *          http://java.sun.com/j2se/1.5.0/docs/api/java/io/OutputStreamWriter.html
   * @throws IOException
   *           if unable to obtain a FileWriter on the specified file
   */
  public static void saveDomToFile(final Document doc, final String filePath, final String encoding) throws IOException {
    File file = new File(filePath);
    XmlDom4JHelper.saveDomToFile(doc, file, encoding);
  }

  /**
   * Write an XML document to a file using the specified character encoding.
   * 
   * @param doc
   *          Document to be written
   * @param file
   *          File that will be the output of the Document
   * @param encoding
   *          String specifying the character encoding. Can be null, in which
   *          case the default encoding will be used. See
   *          http://java.sun.com/j2se/1.5.0/docs/api/java/io/OutputStreamWriter.html
   * @throws IOException
   *           if unable to obtain a FileWriter on the specified file
   */
  public static void saveDomToFile(final Document doc, final File file, final String encoding) throws IOException {
    Writer fWriter = null;
    if (null != encoding) {
      fWriter = new OutputStreamWriter(new FileOutputStream(file), encoding);
    } else {
      fWriter = new OutputStreamWriter(new FileOutputStream(file));
    }
    XmlDom4JHelper.saveDomToWriter(doc, fWriter);
  }

  public static void saveDomToWriter(final Document doc, final Writer writer) throws IOException {
    writer.write(doc.asXML());
  }

  // TODO sbarkdull, move to junit test class
  public static void main(final String[] args) {

    String strXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><b>first</b><b>second</b></root>"; //$NON-NLS-1$
    try {
      Document d = XmlDom4JHelper.getDocFromString(strXml, null);
      String enc = d.getXMLEncoding();
      System.out.println("encoding: " + enc); //$NON-NLS-1$
    } catch (Exception e3) {
      // TODO Auto-generated catch block
      e3.printStackTrace();
    }

    String defaultEncoding = (new OutputStreamWriter(new ByteArrayOutputStream())).getEncoding();
    Charset cs = Charset.defaultCharset();
    System.out.println("default Char set: " + cs.name() + " " + defaultEncoding); //$NON-NLS-1$//$NON-NLS-2$

    ByteArrayInputStream s = new ByteArrayInputStream(strXml.getBytes());
    try {
      // must be repeatable
      for (int ii = 0; ii < 5; ++ii) {
        String pi = XmlHelper.readEncodingProcessingInstruction(s);
        String encoding = XmlHelper.getEncoding(pi);
        System.out.println("encoding: " + encoding); //$NON-NLS-1$
      }
      s.close();
    } catch (IOException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }

    try {
      Document doc = XmlDom4JHelper.getDocFromString(strXml, null);
      Node n = doc.selectSingleNode("/root/b[text()='first']"); //$NON-NLS-1$
      System.out.println(n.getText());

    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      System.exit(1);
    }

    /*
     char[] cbuf = new char[ BUFF_SIZE ];
     File f = null;
     Reader rdr = null;
     try
     {
     f = new File( "C:\\projects\\pentaho1.6\\pentaho-solutions\\system\\pentaho.xml" );
     rdr = new FileReader( f );
     rdr.read(cbuf);
     } catch (FileNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     } catch (IOException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }
     finally
     {
     try {
     rdr.close();
     }catch( Exception ignore){}
     }
     String strEnc = String.valueOf( cbuf );
     //strEnc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<pentaho-system></pentaho-system>";
     //strEnc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
     String re = ".*";
     re = "<\\?xml.*encoding=('|\")([^'\"]*)\\1.*\\?>.*";
     boolean b = strEnc.matches( re );
     
     Pattern p = Pattern.compile( re, Pattern.DOTALL );
     Matcher m = p.matcher(strEnc);
     boolean matches = m.matches();
     for ( int ii=0; ii<=m.groupCount(); ++ii )
     {
     System.out.println( m.group( ii ) );
     }
     String enc = null;
     try {
     enc = getEncoding( f );
     } catch (IOException e1) {
     // TODO Auto-generated catch block
     e1.printStackTrace();
     }
     */

    String[] xmls = { "", //$NON-NLS-1$
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<pentaho-system></pentaho-system>", //$NON-NLS-1$
        "<?xml version=\"1.0\" encoding=\"windows-1252\"?>\n<root></root>", //$NON-NLS-1$
        "<?xml encoding=\"UTF-8\" version=\"1.0\"?><root></root>", //$NON-NLS-1$
        "<?xml encoding=\"UTF-8\" version='1.0'?><root></root>", //$NON-NLS-1$
        "<?xml encoding='UTF-8' version=\"1.0\"?><root></root>", //$NON-NLS-1$
        "<?xml encoding='UTF-8' version='1.0'?><root></root>", //$NON-NLS-1$
        "<?xml encoding='UTF-8\" version='1.0'?><root></root>", //$NON-NLS-1$
        "<?xml version=\"1.0\"?><root></root>", //$NON-NLS-1$
        "bart simpson was here", //$NON-NLS-1$
        "<root>encoding=bad</root>" //$NON-NLS-1$
    };

    for (String element : xmls) {
      String enc = XmlHelper.getEncoding(element);
      System.out.println("2xml: {0} enc: {1}" + element + " enc: " + enc); //$NON-NLS-1$ //$NON-NLS-2$
      enc = ""; //$NON-NLS-1$
    }

    // performance test
    final int numTries = 10000;
    // big file
    String nm = "C:\\projects\\pentaho\\pentaho-reportwizard\\samples\\data\\ClassicCars.xml"; //$NON-NLS-1$
    nm = "C:\\projects\\pentaho1.6\\pentaho-solutions\\system\\pentaho.xml";//$NON-NLS-1$ 
    // small file
    // String nm = "C:\\projects\\pentaho\\my-solutions\\index.xml";
    String xml = null;
    try {
      xml = XmlDom4JHelper.getDocFromFile(nm, null).asXML();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // let's run a test to see if getEncoding is reasonably fast
    long start0 = System.currentTimeMillis();
    for (int ii = 0; ii < numTries; ++ii) {
      XmlHelper.getEncoding(xml);
    }
    long end0 = System.currentTimeMillis();
    System.out.println("time: " + (end0 - start0)); //$NON-NLS-1$

    long start1 = System.currentTimeMillis();
    for (int ii = 0; ii < numTries; ++ii) {
      for (int jj = 0; jj < xml.length(); ++jj) {
        xml.charAt(jj);
      }
    }
    long end1 = System.currentTimeMillis();
    System.out.println("time: " + (end1 - start1)); //$NON-NLS-1$
    // end reasonably fast test
  }

  /**
   * Convenience method to close an input stream and handle (log and throw away)
   * any exceptions. Helps keep code uncluttered.
   * 
   * @param strm
   *          InputStream to be closed
   */
  protected static void closeInputStream(final InputStream strm) {
    if (null != strm) {
      try {
        strm.close();
      } catch (IOException e) {
        XmlDom4JHelper.logger.warn("Failed to close InputStream.", e); //$NON-NLS-1$
      }
    }
  }

  public static String getNodeText(final String xpath, final Node rootNode) {
    return (XmlDom4JHelper.getNodeText(xpath, rootNode, null));
  }

  public static long getNodeText(final String xpath, final Node rootNode, final long defaultValue) {
    String valueStr = XmlDom4JHelper.getNodeText(xpath, rootNode, Long.toString(defaultValue));
    try {
      return Long.parseLong(valueStr);
    } catch (Exception ignored) {
    }
    return defaultValue;
  }

  public static double getNodeText(final String xpath, final Node rootNode, final double defaultValue) {
    String valueStr = XmlDom4JHelper.getNodeText(xpath, rootNode, null);
    if (valueStr == null) {
      return defaultValue;
    }
    try {
      return Double.parseDouble(valueStr);
    } catch (Exception ignored) {
    }
    return defaultValue;
  }

  public static String getNodeText(final String xpath, final Node rootNode, final String defaultValue) {
    if (rootNode == null) {
      return (defaultValue);
    }
    Node node = rootNode.selectSingleNode(xpath);
    if (node == null) {
      return defaultValue;
    }
    return node.getText();
  }

}
