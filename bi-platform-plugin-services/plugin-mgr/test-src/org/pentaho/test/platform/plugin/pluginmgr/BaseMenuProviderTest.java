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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.test.platform.plugin.pluginmgr;

import java.io.File;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IMenuProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.test.platform.engine.core.BaseTest;

public class BaseMenuProviderTest extends BaseTest {
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

  public void testMenu() throws Exception {
	    startTest();
	    
	    String correctXulPath = "system/ui/menubar.xul"; //$NON-NLS-1$
	    String badXulPath = "bogus"; //$NON-NLS-1$
	    
	    IPentahoSession session = new StandaloneSession( "test user" ); //$NON-NLS-1$
	    IMenuProvider menu = PentahoSystem.get( IMenuProvider.class, session ); 
	    assertNotNull( menu );
	    
	    Object obj = menu.getMenuBar("menu", correctXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertTrue( obj.toString().indexOf( "file-menu" ) > 0 ); //$NON-NLS-1$

	    obj = menu.getMenuBar("bogus", correctXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertEquals( "", obj.toString() ); //$NON-NLS-1$

	    obj = menu.getMenuBar("menu", badXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertEquals( "", obj.toString() ); //$NON-NLS-1$

	    obj = menu.getPopupMenu( "testpopup" , correctXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertTrue( obj.toString().indexOf( "test command" ) > 0 ); //$NON-NLS-1$

	    obj = menu.getPopupMenu("bogus", correctXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertEquals( "", obj.toString() ); //$NON-NLS-1$

	    obj = menu.getPopupMenu("testpopup", badXulPath, session); //$NON-NLS-1$
	    assertNotNull( obj );
	    assertEquals( "", obj.toString() ); //$NON-NLS-1$

	    finishTest();
	  }

}
