package org.pentaho.platform.engine.core.system;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PentahoDtdEntityResolver implements EntityResolver {

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
   *      java.lang.String)
   */
  public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
    int idx = systemId.lastIndexOf('/');
    String dtdName = systemId.substring(idx + 1);
    String fullPath = PentahoSystem.getApplicationContext().getSolutionPath("system/dtd/" + dtdName); //$NON-NLS-1$

    try {
      FileInputStream xslIS = new FileInputStream(fullPath);
      InputSource source = new InputSource(xslIS);
      return source;
    } catch (FileNotFoundException e) {

    }
    return null;
  }

}
