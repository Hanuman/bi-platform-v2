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
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Aug 15, 2005 
 * @author James Dixon
 */

package org.pentaho.test.platform.repository;

import java.io.File;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository.content.ContentRepositoryOutputHandler;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class ContentRepositoryOutputHandlerTest extends BaseTest {

	public static final String SOLUTION_PATH = "test-src/solution";
	  private static final String ALT_SOLUTION_PATH = "test-src/solution";
	  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
	  final String SYSTEM_FOLDER = "/system";
//	  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

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

  public void testContentRepositoryOutputHandler() {
    startTest();
    IPentahoSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    ContentRepositoryOutputHandler handler = new ContentRepositoryOutputHandler();
    handler.setActionName("HelloWorld.xaction");
    handler.setHandlerId("1231231");
    handler.setSession(session);
    handler.setSolutionName("reporting");
    handler.setSolutionPath("c:/solutionpath");
    handler.setMimeType("text/html");

    IPentahoSession mySession = handler.getSession();
    String mimeType = handler.getMimeType();
    String handlerId = handler.getHandlerId();
    String solutionName = handler.getSolutionName();
    String solutionPath = handler.getSolutionPath();
    String actionName = handler.getActionName();
    assertEquals(session, mySession);
    assertEquals(mimeType, "text/html");//$NON-NLS-1$
    assertEquals(handlerId, "1231231");//$NON-NLS-1$
    assertEquals(solutionName, "reporting");
    assertEquals(solutionPath, "c:/solutionpath");//$NON-NLS-1$
    assertEquals(actionName, "HelloWorld.xaction");//$NON-NLS-1$
  }

  public static void main(String[] args) {
    ContentRepositoryOutputHandlerTest test = new ContentRepositoryOutputHandlerTest();
    test.setUp();
    test.testContentRepositoryOutputHandler();
    try {

    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
