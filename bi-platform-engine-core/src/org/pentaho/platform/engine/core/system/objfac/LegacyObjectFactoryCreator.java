package org.pentaho.platform.engine.core.system.objfac;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IObjectFactoryCreator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LegacyObjectFactoryCreator implements IObjectFactoryCreator {

  private IPentahoObjectFactory objectFactory = null;
  
  public LegacyObjectFactoryCreator() {
    
  }
  
  // TODO sbarkdull, provide better error strings in throw exceptions
  public void configure( String configFilePath ) throws ObjectFactoryException {
    File file = new File( configFilePath );
    String strXml = null;
    try {
      InputStream inputStrm = new FileInputStream( file );
      strXml = IOUtils.toString( inputStrm );
    } catch (IOException e) { 
      throw new ObjectFactoryException( 
          Messages.getErrorString( "LegacyObjectFactory.ERROR_0001_CONFIGURE_FAILED", configFilePath), e ); //$NON-NLS-1$
    }
    objectFactory = getPentahoObjectFactoryFromXml( strXml);
  }
  
  public IPentahoObjectFactory getFactory() {
    return objectFactory;
  }

  private PentahoObjectFactory getPentahoObjectFactoryFromXml( String strXml ) throws ObjectFactoryException {
    PentahoConfigParserHandler h = parsePentahoConfigXml( strXml );
    return h.getObjectFactory();
  }
  
  private PentahoConfigParserHandler parsePentahoConfigXml( String strXml ) throws ObjectFactoryException {
    PentahoConfigParserHandler handler = null;
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      handler = new PentahoConfigParserHandler();
      // TODO sbarkdull, need to set encoding
//      String encoding = CleanXmlHelper.getEncoding( strXml );
//      InputStream is = new ByteArrayInputStream( strXml.getBytes( encoding ) );
      InputStream is = new ByteArrayInputStream( strXml.getBytes( "UTF-8" ) ); //$NON-NLS-1$
     
      parser.parse( is, handler );
    } catch ( Exception e ) {
      if ( e instanceof RuntimeException ) {
        throw (RuntimeException)e;
      }
      throw new ObjectFactoryException( 
          Messages.getErrorString( "LegacyObjectFactoryCreator.ERROR_0002_CONFIG_FILE_PARSE_FAILED" ), e ); //$NON-NLS-1$
    }
    return handler;
  }
 
  private static class PentahoConfigParserHandler extends DefaultHandler {

    private PentahoObjectFactory objectFactory = null;
    private boolean foundPentahoSystem = false;
    private boolean foundObjects = false;
    
    private String currentScope = null;
    private String currentKey = null;
    private String currentClassName = null;
    private boolean parseFailed = false;
    private String parseFailedMsg = null;
    private Map<String, IObjectCreator> objectMap = new HashMap<String, IObjectCreator>();
    
    public PentahoConfigParserHandler() {
      objectFactory = new PentahoObjectFactory();
    }
  
    public void characters( char[] ch, int startIdx, int length )
    {
      currentClassName = String.valueOf( ch, startIdx, length );
    }
    
    public PentahoObjectFactory getObjectFactory() throws ObjectFactoryException {
      if ( !parseFailed ) {
        objectFactory.setObjectCreators( objectMap );
        return objectFactory;
      } else {
        throw new ObjectFactoryException(
            Messages.getErrorString( "LegacyObjectFactory.ERROR_0002_CREATION_FAILED", parseFailedMsg ) ); //$NON-NLS-1$
      }
    }
    
    public void endElement(String uri, String localName, String qName ) throws SAXException
    {
      if ( qName.equals( "pentaho-system" ) ) { //$NON-NLS-1$
        // we are almost done
        foundPentahoSystem = false;
        objectFactory.setObjectCreators( objectMap );
      } else if ( qName.equals( "objects" ) ) { //$NON-NLS-1$
        foundObjects = false;
      } else {
        if ( foundPentahoSystem && foundObjects && !parseFailed ) {
          // i am in the objects node
          IObjectCreator creator = null;
          try {
            creator = getObjectCreatorByScope( currentScope, currentClassName );
            objectMap.put( currentKey, creator );
          } catch (ObjectFactoryException e) {
            parseFailed = true;
            parseFailedMsg = e.getMessage();
          }
        }
        currentScope = null;
        currentKey = null;
        currentClassName = null;
      }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
      if ( qName.equals( "pentaho-system" ) ) { //$NON-NLS-1$
        foundPentahoSystem = true;
      } else if ( qName.equals( "objects" ) ) { //$NON-NLS-1$
        foundObjects = true;
      } else {
        if ( foundPentahoSystem && foundObjects ) {
          // i am in the objects node
          currentScope = attributes.getValue( "scope" ); //$NON-NLS-1$
          currentKey = qName;
        } else {
          currentScope = null;
          currentKey = null;
        }
      }
    }
    
    private IObjectCreator getObjectCreatorByScope( String scope, String className ) throws ObjectFactoryException {
      if ( "global".equals( scope ) ) { //$NON-NLS-1$
        return new GlobalObjectCreator( className );
      } else if ( "session".equals( scope ) ) { //$NON-NLS-1$
        return new SessionObjectCreator( className );
      } else if ( "thread".equals( scope ) ) { //$NON-NLS-1$
        return new ThreadObjectCreator( className );
      } else if ( "local".equals( scope ) ) { //$NON-NLS-1$
        return new LocalObjectCreator( className );
      } else {
        throw new ObjectFactoryException(
            Messages.getErrorString( "LegacyObjectFactoryCreator.ERROR_0001_SCOPE_ERROR", scope ) ); //$NON-NLS-1$
      }
    }
  }
  
  public static void main(final String[] args) {
    String path = "C:\\projects\\pentaho.2.trunk\\bi-platform-sample-solution\\system\\testPentaho.xml"; //$NON-NLS-1$
    LegacyObjectFactoryCreator f = new LegacyObjectFactoryCreator();
    try {
      f.configure( path );
    } catch (ObjectFactoryException e) {
      e.printStackTrace();
    }
    IPentahoObjectFactory objFac = f.getFactory();
    System.out.println( "done" ); //$NON-NLS-1$
  }
}
