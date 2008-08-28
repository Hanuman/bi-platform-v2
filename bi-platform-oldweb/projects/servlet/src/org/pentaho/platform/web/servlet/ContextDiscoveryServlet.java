package org.pentaho.platform.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @deprecated
 * @author Alex Silva
 *
 */
@Deprecated
public class ContextDiscoveryServlet extends HttpServlet {

  private static final long serialVersionUID = -8747147437664663719L;

  private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

  private final TransformerFactory tf = TransformerFactory.newInstance();

  @Override
  public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {

    String path = request.getContextPath();
   
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.newDocument();
      Element epath = document.createElement("application-context-root");
      epath.setTextContent(path);
      document.appendChild(epath);
      Transformer trans = tf.newTransformer();
      trans.transform(new DOMSource(document), new StreamResult(response.getOutputStream()));
    } catch (ParserConfigurationException e) {
      throw new ServletException(e);
    } catch (TransformerConfigurationException e) {
      throw new ServletException(e);
    } catch (TransformerException e) {
      throw new ServletException(e);
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }

}
