package org.pentaho.test.platform.plugin.pluginmgr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.PluginMessageLogger;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.IMenuCustomization.CustomizationType;
import org.pentaho.ui.xul.IMenuCustomization.ItemType;

public class CustomMenuTest extends BaseTest {
  private static final String SOLUTION_PATH = "plugin-mgr/test-res/solution3-menus"; //$NON-NLS-1$
  private static final String ALT_SOLUTION_PATH = "test-res/solution3-menus"; //$NON-NLS-1$
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

  public void testMenus() throws Exception {
	    startTest();
	    
	    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
	    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, session ); 
	    assertNotNull( pluginManager );
	    
	    PluginMessageLogger.clear();
	    boolean result = pluginManager.reload(session);
	    assertFalse( "Plugin update should fail", result ); //$NON-NLS-1$
	    List<?> customs = pluginManager.getMenuCustomizations();
	    assertEquals( "Wrong number of menu items created", 3, customs.size() ); //$NON-NLS-1$
	    
	    // examine the menu customizations to see if they are what we are expecting
	    IMenuCustomization custom = (IMenuCustomization) customs.get(0);
	    assertEquals( custom.getCustomizationType(), CustomizationType.LAST_CHILD );
	    assertEquals( custom.getItemType(), ItemType.MENU_ITEM );
	    assertEquals( custom.getId(), "item1" ); //$NON-NLS-1$
	    assertEquals( custom.getAnchorId(), "admin-submenu" ); //$NON-NLS-1$
	    assertEquals( custom.getLabel(), "Test 1" ); //$NON-NLS-1$
	    assertEquals( custom.getCommand(), "command1" ); //$NON-NLS-1$

	    // now check that the messages are ok
	    String target = Messages.getString("PluginManager.ERROR_0009_MENU_CUSTOMIZATION_ERROR", "bad-item-1", "Bad 1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );

	    target = Messages.getString("PluginManager.USER_UPDATING_PLUGIN", "Plugin 1", "plugin1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );

	    target = Messages.getString("PluginManager.USER_MENU_ITEM_ADDITION", "item1", "Test 1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );

	    target = Messages.getString("PluginManager.USER_MENU_ITEM_DELETE", "item2" ); //$NON-NLS-1$ //$NON-NLS-2$
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );

	    target = Messages.getString("PluginManager.USER_MENU_ITEM_REPLACE", "item3", "Test 2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );

	    target = Messages.getString("PluginManager.USER_PLUGIN_REFRESH_OK", "plugin1"); //$NON-NLS-1$ //$NON-NLS-2$
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );

	    target = Messages.getString("PluginManager.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", "system/plugin3-bad-plugin-xml/plugin.xml"); //$NON-NLS-1$ //$NON-NLS-2$
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );
	    
	    target = Messages.getString("PluginManager.USER_UPDATING_PLUGIN", "Plugin 4", "plugin4-bad-menu-type"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );

	    target = Messages.getString("PluginManager.ERROR_0009_MENU_CUSTOMIZATION_ERROR", "bad-item-1", "Bad 1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );
	    
	    target = Messages.getString("PluginManager.USER_PLUGIN_REFRESH_BAD", "plugin4-bad-menu-type"); //$NON-NLS-1$ //$NON-NLS-2$
	    assertTrue( PluginMessageLogger.getAll().contains( target ) );

	    
	    finishTest();
	  }

}
