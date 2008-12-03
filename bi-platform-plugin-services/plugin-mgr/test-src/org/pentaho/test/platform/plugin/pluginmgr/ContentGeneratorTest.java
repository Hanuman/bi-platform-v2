package org.pentaho.test.platform.plugin.pluginmgr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.IPluginSettings;
import org.pentaho.platform.api.engine.IXulOverlay;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.GlobalObjectCreator;
import org.pentaho.platform.plugin.services.pluginmgr.PluginSettings;
import org.pentaho.test.platform.engine.core.BaseTest;

public class ContentGeneratorTest extends BaseTest {
  private static final String SOLUTION_PATH = "plugin-mgr/test-res/solution4-content-gen"; //$NON-NLS-1$
  private static final String ALT_SOLUTION_PATH = "test-res/solution4-content-gen"; //$NON-NLS-1$
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml"; //$NON-NLS-1$

  @Override 
  public String getSolutionPath() {
      File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
      if(file.exists()) {
        return SOLUTION_PATH;  
      } else {
        return ALT_SOLUTION_PATH;
      }
  }

  public void testContentGenerators() throws Exception {
	    startTest();
	    
	    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
	    IPluginSettings pluginSettings = PentahoSystem.get( IPluginSettings.class, session ); 
	    assertNotNull( pluginSettings );
	    
	    List<String> messages = new ArrayList<String>();
	    boolean result = pluginSettings.updatePluginSettings(session, messages);
	    assertFalse( "Plugin update should fail", result ); //$NON-NLS-1$
	    
	    assertEquals( "Wrong number of messages created", 19, messages.size() ); //$NON-NLS-1$

	    // check that the content types are ok
	    Set<String> types = pluginSettings.getContentTypes();
	    assertTrue( types.contains( "test-type-1" ) ); //$NON-NLS-1$
	    assertTrue( types.contains( "test-type-2" ) ); //$NON-NLS-1$
	    assertFalse( types.contains( "test-type-bad" ) ); //$NON-NLS-1$
	    
	    // check that the content generators were created ok
	    assertNotNull( pluginSettings.getContentGenerator( "test1" , session) ); //$NON-NLS-1$
	    assertNotNull( pluginSettings.getContentGenerator( "test2" , session) ); //$NON-NLS-1$
	    assertNotNull( pluginSettings.getContentGenerator( "test3" , session) ); //$NON-NLS-1$
	    assertNotNull( pluginSettings.getContentGenerator( "test4" , session) ); //$NON-NLS-1$
	    assertNull( pluginSettings.getContentGenerator( "test5" , session) ); //$NON-NLS-1$

	    // test the first content type
	    List<IObjectCreator> creators = pluginSettings.getContentGeneratorsForType("test-type-1", session); //$NON-NLS-1$
	    assertEquals( 2, creators.size() );
	    IObjectCreator creator = creators.get(0);
	    Object obj = creator.getInstance( "", session); //$NON-NLS-1$
	    assertNotNull( obj );
	    IContentGenerator contentGenerator = (IContentGenerator) obj;
	    assertTrue( contentGenerator instanceof ContentGenerator1 );
	    creator = creators.get(1);
	    obj = creator.getInstance( "", session); //$NON-NLS-1$
	    assertNotNull( obj );
	    contentGenerator = (IContentGenerator) obj;
	    assertTrue( contentGenerator instanceof ContentGenerator2 );

	    // test the second content type
	    creators = pluginSettings.getContentGeneratorsForType("test-type-2", session); //$NON-NLS-1$
	    assertEquals( 2, creators.size() );
	    creator = creators.get(0);
	    obj = creator.getInstance( "", session); //$NON-NLS-1$
	    assertNotNull( obj );
	    contentGenerator = (IContentGenerator) obj;
	    assertTrue( contentGenerator instanceof ContentGenerator2 );
	    creator = creators.get(1);
	    obj = creator.getInstance( "", session); //$NON-NLS-1$
	    assertNotNull( obj );
	    contentGenerator = (IContentGenerator) obj;
	    assertTrue( contentGenerator instanceof ContentGenerator1 );

	    // test a bad content type
	    creators = pluginSettings.getContentGeneratorsForType("test-type-bad", session); //$NON-NLS-1$
	    assertNull( creators );

	    // test the default content generator (first in the list)
	    contentGenerator = pluginSettings.getContentGeneratorForType( "test-type-1", session); //$NON-NLS-1$
	    assertTrue( contentGenerator instanceof ContentGenerator1 );

	    contentGenerator = pluginSettings.getContentGeneratorForType( "test-type-2", session); //$NON-NLS-1$
	    assertTrue( contentGenerator instanceof ContentGenerator2 );

	    contentGenerator = pluginSettings.getContentGeneratorForType( "test-type-bad", session); //$NON-NLS-1$
	    assertNull( contentGenerator );

	    // test the ids
	    assertEquals( "test1", pluginSettings.getContentGeneratorIdForType( "test-type-1", session) ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( "test3", pluginSettings.getContentGeneratorIdForType( "test-type-2", session) ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( null, pluginSettings.getContentGeneratorIdForType( "test-type-bad", session) ); //$NON-NLS-1$ 
	    
	    // test the ids
	    assertEquals( "Test Generator 1", pluginSettings.getContentGeneratorTitleForType("test-type-1", session) ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( "Test Generator 3", pluginSettings.getContentGeneratorTitleForType( "test-type-2", session) ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( null, pluginSettings.getContentGeneratorTitleForType( "test-type-bad", session) ); //$NON-NLS-1$ 
	    
	    // test the urls
	    assertEquals( "", pluginSettings.getContentGeneratorUrlForType("test-type-1", session) ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( "/testurl", pluginSettings.getContentGeneratorUrlForType( "test-type-2", session) ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( null, pluginSettings.getContentGeneratorUrlForType( "test-type-bad", session) ); //$NON-NLS-1$ 

	    finishTest();
	  }

  public void testPluginSettingsStaticMethods() throws Exception {
	    startTest();
	    
	    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
	    
	    PluginSettings.setSession(session);
	    assertEquals( "test1", PluginSettings.getContentGeneratorIdForType("test-type-1") ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( "Test Generator 1", PluginSettings.getContentGeneratorTitleForType("test-type-1") ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( "", PluginSettings.getContentGeneratorUrlForType("test-type-1") ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertNotNull( PluginSettings.getInstance() );
	    assertEquals( "", PluginSettings.getContentGeneratorIdForType("test-type-bad") ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( "", PluginSettings.getContentGeneratorTitleForType("test-type-bad") ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertEquals( "", PluginSettings.getContentGeneratorUrlForType("test-type-bad") ); //$NON-NLS-1$ //$NON-NLS-2$
	    
	    finishTest();
	  }

  public void testContentGeneratorInfo() throws Exception {
	    startTest();
	    
	    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
	    IPluginSettings pluginSettings = PentahoSystem.get( IPluginSettings.class, session ); 
	    assertNotNull( pluginSettings );
	    
	    List<String> messages = new ArrayList<String>();
	    boolean result = pluginSettings.updatePluginSettings(session, messages);
	    assertFalse( "Plugin update should fail", result ); //$NON-NLS-1$
	    
	    IContentGeneratorInfo contentGeneratorInfo = pluginSettings.getContentGeneratorInfo("test1", session); //$NON-NLS-1$
	    assertNotNull( contentGeneratorInfo );
	    assertEquals( "test1", contentGeneratorInfo.getId() ); //$NON-NLS-1$
	    assertEquals( "Test Generator 1", contentGeneratorInfo.getTitle() ); //$NON-NLS-1$
	    assertEquals( "", contentGeneratorInfo.getUrl() ); //$NON-NLS-1$
	    assertTrue( contentGeneratorInfo.getCreator() instanceof GlobalObjectCreator ); 
	    
	    IFileInfoGenerator fileInfoGenerator = contentGeneratorInfo.getFileInfoGenerator();
	    assertNotNull( fileInfoGenerator  );
	    assertTrue( fileInfoGenerator instanceof FileInfoGenerator );
	    
	    finishTest();
	  }

  public void testContentInfo() throws Exception {
      startTest();
      
      IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
      IPluginSettings pluginSettings = PentahoSystem.get( IPluginSettings.class, session ); 
      assertNotNull( pluginSettings );
      
      List<String> messages = new ArrayList<String>();
      boolean result = pluginSettings.updatePluginSettings(session, messages);
      assertFalse( "Plugin update should fail", result ); //$NON-NLS-1$
      
      IContentInfo contentInfo = pluginSettings.getContentInfoFromExtension("test-type-1" , session); //$NON-NLS-1$
      assertNotNull( contentInfo );
      assertEquals( "test mime type", contentInfo.getMimeType() ); //$NON-NLS-1$
      assertEquals( "Test Type 1", contentInfo.getTitle() ); //$NON-NLS-1$
      assertEquals( "test description", contentInfo.getDescription() ); //$NON-NLS-1$
      assertEquals( "test-type-1", contentInfo.getExtension() ); //$NON-NLS-1$
      
      assertEquals( "test url", contentInfo.getIconUrl() ); //$NON-NLS-1$
            
      finishTest();
    }

  public void testContentOperations() throws Exception {
      startTest();
      
      IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
      IPluginSettings pluginSettings = PentahoSystem.get( IPluginSettings.class, session ); 
      assertNotNull( pluginSettings );
      
      List<String> messages = new ArrayList<String>();
      boolean result = pluginSettings.updatePluginSettings(session, messages);
      assertFalse( "Plugin update should fail", result ); //$NON-NLS-1$
      
      IContentInfo contentInfo = pluginSettings.getContentInfoFromExtension("test-type-1" , session); //$NON-NLS-1$
      assertNotNull( contentInfo );
      
      List<IPluginOperation> ops = contentInfo.getOperations();
      assertNotNull( "Operations are null", ops ); //$NON-NLS-1$
      assertEquals( "Wrong number of ops", 2, ops.size() ); //$NON-NLS-1$
      
      assertEquals( "Operation name is wrong", "op 1 name", ops.get(0).getName() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertEquals( "Operation command is wrong", "op 1 command", ops.get(0).getCommand() ); //$NON-NLS-1$ //$NON-NLS-2$

      assertEquals( "Operation name is wrong", "op 2 name", ops.get(1).getName() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertEquals( "Operation command is wrong", "op 2 command", ops.get(1).getCommand() ); //$NON-NLS-1$ //$NON-NLS-2$
      
      finishTest();
    }

  public void testOverlays() throws Exception {
      startTest();
      
      IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
      IPluginSettings pluginSettings = PentahoSystem.get( IPluginSettings.class, session ); 
      assertNotNull( pluginSettings );
      
      List<String> messages = new ArrayList<String>();
      boolean result = pluginSettings.updatePluginSettings(session, messages);
      assertFalse( "Plugin update should fail", result ); //$NON-NLS-1$
      
      IContentInfo contentInfo = pluginSettings.getContentInfoFromExtension("test-type-1" , session); //$NON-NLS-1$
      assertNotNull( contentInfo );

      List<IXulOverlay> overlays = pluginSettings.getOverlays();
      
      assertNotNull( "Overlays is null", overlays ); //$NON-NLS-1$
      assertEquals( "Wrong number of overlays", 2, overlays.size() ); //$NON-NLS-1$
      IXulOverlay overlay = overlays.get(0);
      assertEquals( "Wrong overlay id", "overlay1", overlay.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertEquals( "Wrong overlay resource uri", "uri1", overlay.getResourceBundleUri() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "Wrong overlay content", overlay.getOverlayXml().indexOf( "<node1" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "Wrong overlay content", overlay.getOverlayXml().indexOf( "<node2" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "Wrong overlay content", overlay.getOverlayXml().indexOf( "<node3" ) == -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "Wrong overlay content", overlay.getOverlayXml().indexOf( "<node4" ) == -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNull( "Overlay URI should be null", overlay.getOverlayUri() ); //$NON-NLS-1$
      
      overlay = overlays.get(1);
      
      assertEquals( "Wrong overlay id", "overlay2", overlay.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertEquals( "Wrong overlay resource uri", "uri2", overlay.getResourceBundleUri() ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "Wrong overlay content", overlay.getOverlayXml().indexOf( "<node1" ) == -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "Wrong overlay content", overlay.getOverlayXml().indexOf( "<node2" ) == -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "Wrong overlay content", overlay.getOverlayXml().indexOf( "<node3" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertTrue( "Wrong overlay content", overlay.getOverlayXml().indexOf( "<node4" ) != -1 ); //$NON-NLS-1$ //$NON-NLS-2$
      assertNull( "Overlay URI should be null", overlay.getOverlayUri() ); //$NON-NLS-1$
      
      finishTest();
    }

}
