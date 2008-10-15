/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Oct 11, 2008
 * @author Aaron Phillips
 * 
 */
package org.pentaho.platform.engine.core.system.objfac;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;

//FIXME: make this class work, i.e. create objects based on old pentaho.xml format
public class LegacyPentahoObjectFactory implements IPentahoObjectFactory {
  
  private Map<String, IObjectCreator> objectCreators = null;

  public Object getObject(String key, IPentahoSession session) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean objectDefined(String key) {
    // TODO Auto-generated method stub
    return false;
  }

  public void init(String configFile, Object context) {
    File file = new File( configFile );
    String strXml = null;
    try {
      InputStream inputStrm = new FileInputStream( file );
      strXml = IOUtils.toString( inputStrm );
    } catch (IOException e) { 
      throw new RuntimeException(e);
    }
//      throw new ObjectFactoryException( 
//          Messages.getErrorString( "LegacyObjectFactory.ERROR_0001_CONFIGURE_FAILED", configFile), e ); //$NON-NLS-1$
//    }
//    objectFactory = getPentahoObjectFactoryFromXml( strXml);
  }

  public <T> T getObject(Class<T> interfaceClass, IPentahoSession session) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public <T> T get(Class<T> interfaceClass, IPentahoSession session) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public <T> T get(Class<T> interfaceClass, String key, IPentahoSession session) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public Class getImplementingClass(String key) {
    // TODO Auto-generated method stub
    return null;
  }
  
//  public IPentahoObjectFactory getFactory() {
//    return objectFactory;
//  }
//
//  private PentahoObjectFactory getPentahoObjectFactoryFromXml( String strXml ) throws ObjectFactoryException {
//    PentahoConfigParserHandler h = parsePentahoConfigXml( strXml );
//    return h.getObjectFactory();
//  }
//  
//  private PentahoConfigParserHandler parsePentahoConfigXml( String strXml ) throws ObjectFactoryException {
//    PentahoConfigParserHandler handler = null;
//    try {
//      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
//      handler = new PentahoConfigParserHandler();
//      // TODO sbarkdull, need to set encoding
////      String encoding = CleanXmlHelper.getEncoding( strXml );
////      InputStream is = new ByteArrayInputStream( strXml.getBytes( encoding ) );
//      InputStream is = new ByteArrayInputStream( strXml.getBytes( "UTF-8" ) ); //$NON-NLS-1$
//     
//      parser.parse( is, handler );
//    } catch ( Exception e ) {
//      if ( e instanceof RuntimeException ) {
//        throw (RuntimeException)e;
//      }
//      throw new ObjectFactoryException( 
//          Messages.getErrorString( "LegacyObjectFactoryCreator.ERROR_0002_CONFIG_FILE_PARSE_FAILED" ), e ); //$NON-NLS-1$
//    }
//    return handler;
//  }
// 
//  private static class PentahoConfigParserHandler extends DefaultHandler {
//
//    private PentahoObjectFactory objectFactory = null;
//    private boolean foundPentahoSystem = false;
//    private boolean foundObjects = false;
//    
//    private String currentScope = null;
//    private String currentKey = null;
//    private String currentClassName = null;
//    private boolean parseFailed = false;
//    private String parseFailedMsg = null;
//    private Map<String, IObjectCreator> objectMap = new HashMap<String, IObjectCreator>();
//    
//    public PentahoConfigParserHandler() {
//      objectFactory = new PentahoObjectFactory();
//    }
//  
//    public void characters( char[] ch, int startIdx, int length )
//    {
//      currentClassName = String.valueOf( ch, startIdx, length );
//    }
//    // warning is suppressed because Spring may be used to inject his map, and it can't handle generic types
//    @SuppressWarnings("unchecked")
//    public void setObjectCreators( Map objectMap ) {
//      this.objectCreators = objectMap;
//      createRequiredImplementations();
//    }
//    
//    private void createRequiredImplementations() {
//      // force the existence of a IVersionHelper implementation
//      String versionHelperKey = IVersionHelper.class.getSimpleName();
//      if ( !objectCreators.containsKey( versionHelperKey ) ) {
//        IObjectCreator c = new GlobalObjectCreator( VersionHelper.class.getName() );
//        objectCreators.put( versionHelperKey, c );
//      }
//    }
//    public PentahoObjectFactory getObjectFactory() throws ObjectFactoryException {
//      if ( !parseFailed ) {
//        objectFactory.setObjectCreators( objectMap );
//        return objectFactory;
//      } else {
//        throw new ObjectFactoryException(
//            Messages.getErrorString( "LegacyObjectFactory.ERROR_0002_CREATION_FAILED", parseFailedMsg ) ); //$NON-NLS-1$
//      }
//    }
//    
//    public void endElement(String uri, String localName, String qName ) throws SAXException
//    {
//      if ( qName.equals( "pentaho-system" ) ) { //$NON-NLS-1$
//        // we are almost done
//        foundPentahoSystem = false;
//        objectFactory.setObjectCreators( objectMap );
//      } else if ( qName.equals( "objects" ) ) { //$NON-NLS-1$
//        foundObjects = false;
//      } else {
//        if ( foundPentahoSystem && foundObjects && !parseFailed ) {
//          // i am in the objects node
//          IObjectCreator creator = null;
//          try {
//            creator = getObjectCreatorByScope( currentScope, currentClassName );
//            objectMap.put( currentKey, creator );
//          } catch (ObjectFactoryException e) {
//            parseFailed = true;
//            parseFailedMsg = e.getMessage();
//          }
//        }
//        currentScope = null;
//        currentKey = null;
//        currentClassName = null;
//      }
//    }
//
//    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
//    {
//      if ( qName.equals( "pentaho-system" ) ) { //$NON-NLS-1$
//        foundPentahoSystem = true;
//      } else if ( qName.equals( "objects" ) ) { //$NON-NLS-1$
//        foundObjects = true;
//      } else {
//        if ( foundPentahoSystem && foundObjects ) {
//          // i am in the objects node
//          currentScope = attributes.getValue( "scope" ); //$NON-NLS-1$
//          currentKey = qName;
//        } else {
//          currentScope = null;
//          currentKey = null;
//        }
//      }
//    }
//    
//    private IObjectCreator getObjectCreatorByScope( String scope, String className ) throws ObjectFactoryException {
//      if ( "global".equals( scope ) ) { //$NON-NLS-1$
//        return new GlobalObjectCreator( className );
//      } else if ( "session".equals( scope ) ) { //$NON-NLS-1$
//        return new SessionObjectCreator( className );
//      } else if ( "thread".equals( scope ) ) { //$NON-NLS-1$
//        return new ThreadObjectCreator( className );
//      } else if ( "local".equals( scope ) ) { //$NON-NLS-1$
//        return new LocalObjectCreator( className );
//      } else {
//        throw new ObjectFactoryException(
//            Messages.getErrorString( "LegacyObjectFactoryCreator.ERROR_0001_SCOPE_ERROR", scope ) ); //$NON-NLS-1$
//      }
//    }
//  }

}
