package org.pentaho.test.platform.engine.core;

import java.io.File;

import junit.framework.TestCase;

import org.pentaho.platform.api.engine.IObjectFactoryCreator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.engine.core.solution.CustomSettingsParameterProvider;
import org.pentaho.platform.engine.core.solution.SystemSettingsParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.objfac.SpringObjectFactoryCreator;

public class SettingsParameterProviderTest extends TestCase {


	public static final String SOLUTION_PATH = "projects/core/test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
  final String SYSTEM_FOLDER = "/system";
  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

	  public String getSolutionPath() {
	      File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
	      if(file.exists()) {
	        System.out.println("File exist returning " + SOLUTION_PATH);
	        return SOLUTION_PATH;  
	      } else {
	        System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);      
	        return ALT_SOLUTION_PATH;
	      }
	  }

	  private void init() {
		  if( !PentahoSystem.getInitializedOK() ) {
		        StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(getSolutionPath(), ""); //$NON-NLS-1$
		        IObjectFactoryCreator facCreator;
		        String objectFactoryCreatorCfgFile = getSolutionPath() + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME; //$NON-NLS-1$
		        try {
		      	  facCreator = new SpringObjectFactoryCreator();  
		      	  facCreator.configure( objectFactoryCreatorCfgFile );
		        } catch (Exception e) {
		          //Logger.fatal( SolutionContextListener.class.getName(), e.getMessage() );
		          throw new RuntimeException( "Failed to configure the Pentaho Object Factory.", e );
		        }
		        IPentahoObjectFactory pentahoObjectFactory = facCreator.getFactory();
		        PentahoSystem.setObjectFactory( pentahoObjectFactory );
		        PentahoSystem.init(applicationContext );
		  }
	  }
	  
	  public void testSystemSettingsParameterProvider1() {

		  init();
		  // pull a string from pentaho.xml
		  String value = SystemSettingsParameterProvider.getSystemSetting( "pentaho.xml{pentaho-system/log-file}" );
	        
		  assertEquals( "Could not get setting from pentaho.xml", "server.log", value );
	  }
	  
	  public void testSystemSettingsParameterProvider2() {

		  init();
		  // pull a string from pentaho.xml
		  SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();
		  
		  String value = provider.getStringParameter( "pentaho.xml{pentaho-system/log-file}", null);
	        
		  assertEquals( "Could not get setting from pentaho.xml", "server.log", value );
	  }
	  
	  public void testSystemSettingsParameterProvider3() {

		  init();
		  // pull a string from pentaho.xml
		  SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();
		  
		  String value = provider.getStringParameter( "bogus.xml{pentaho-system/log-file}", null);
	        
		  assertEquals( "Expected null result", null, value );
	  }
	  
	  public void testSystemSettingsParameterProvider4() {

		  init();
		  // pull a string from pentaho.xml
		  SystemSettingsParameterProvider provider = new SystemSettingsParameterProvider();
		  
		  String value = provider.getStringParameter( "pentaho.xml{bogus}", null);
	        
		  assertEquals( "Expected null result", null, value );
	  }
	  
	  public void testCustomSettingsParameterProvider() {

		  init();
		  // pull a string from pentaho.xml
		  CustomSettingsParameterProvider provider = new CustomSettingsParameterProvider();
		  
		  String value = provider.getStringParameter( "settings.xml{settings/setting1}", null);
	        
		  assertEquals( "Could not get setting from pentaho.xml", "value1", value );
	  }
	  
}
