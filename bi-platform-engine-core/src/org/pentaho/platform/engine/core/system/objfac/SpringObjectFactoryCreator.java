package org.pentaho.platform.engine.core.system.objfac;

import java.io.File;

import org.pentaho.platform.api.engine.IObjectFactoryCreator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;


public class SpringObjectFactoryCreator implements IObjectFactoryCreator {

  private IPentahoObjectFactory pentahoObjectFactory = null;
  private static final String PENTAHO_OBJECT_FACTORY_BEAN_NAME = "pentahoObjectFactory"; //$NON-NLS-1$
  public SpringObjectFactoryCreator() {
    
  }
  
  public IPentahoObjectFactory getFactory() {
    return pentahoObjectFactory;
  }

  public void configure(String configFilePath) throws ObjectFactoryException {
    //ApplicationContext appCtx = new FileSystemXmlApplicationContext( configFilePath );
    File f = new File( configFilePath );
    FileSystemResource fsr = new FileSystemResource( f );
    GenericApplicationContext appCtx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appCtx);
    xmlReader.loadBeanDefinitions( fsr );

    pentahoObjectFactory = (PentahoObjectFactory)appCtx.getBean( PENTAHO_OBJECT_FACTORY_BEAN_NAME ); //$NON-NLS-1$
  }
  
  
  public static void main(final String[] args) {
    String path = "C:\\projects\\pentaho.2.trunk\\bi-platform-sample-solution\\system\\pentahoObjects.spring.xml"; //$NON-NLS-1$
    SpringObjectFactoryCreator f = new SpringObjectFactoryCreator();
    try {
      f.configure( path );
    } catch (ObjectFactoryException e) {
      e.printStackTrace();
    }
    IPentahoObjectFactory objFac = f.getFactory();
    System.out.println( "done" ); //$NON-NLS-1$
  }
}
