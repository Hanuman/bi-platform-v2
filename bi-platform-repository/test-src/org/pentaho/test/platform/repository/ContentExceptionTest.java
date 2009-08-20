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
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.test.platform.repository;

import java.io.File;

import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class ContentExceptionTest extends BaseTest {

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

  public void testContentException2() {
    startTest();
    info("Expected: Exception will be caught and thrown as a Content Exception"); //$NON-NLS-1$
    ContentException ce1 = new ContentException("A test Content Exception has been thrown"); //$NON-NLS-1$
    System.out.println("ContentException :" + ce1); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();
  }

  public void testContentException3() {
    startTest();
    info("Expected: A Content Exception will be created with Throwable as a parameter"); //$NON-NLS-1$
    ContentException ce2 = new ContentException(new Throwable("This is a throwable exception")); //$NON-NLS-1$
    System.out.println("ContentException" + ce2); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();

  }

  public void testContentException4() {
    startTest();
    info("Expected: Exception will be caught and thrown as a Content Exception"); //$NON-NLS-1$
    ContentException ce3 = new ContentException("A test UI Exception has been thrown", new Throwable());//$NON-NLS-1$
    System.out.println("ContentException :" + ce3); //$NON-NLS-1$    
    assertTrue(true);
    finishTest();

  }

  public static void main(String[] args) {
    ContentExceptionTest test = new ContentExceptionTest();
    try {
      test.setUp();
      test.testContentException2();
      test.testContentException3();
      test.testContentException4();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
