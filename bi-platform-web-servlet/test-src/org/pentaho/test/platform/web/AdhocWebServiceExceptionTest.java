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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.web;


import org.pentaho.platform.web.servlet.AdhocWebServiceException;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.BaseTestCase;

@SuppressWarnings("nls")
public class AdhocWebServiceExceptionTest extends BaseTestCase {
  private static final String SOLUTION_PATH = "test-src/solution";
  public String getSolutionPath() {
      return SOLUTION_PATH;
  }
   
  public void testAdhocWebServiceException1() {
    AdhocWebServiceException awse = new AdhocWebServiceException();
    System.out.println("AdhocWebServiceException :" +awse); //$NON-NLS-1$
    assertTrue(true);
  }
  

  public void testAdhocWebServiceException2() {
    AdhocWebServiceException awse1 = new AdhocWebServiceException("A test AdhocWebService Exception has been thrown"); //$NON-NLS-1$
    System.out.println("AdhocWebServiceException :" +awse1); //$NON-NLS-1$    
    assertTrue(true);
   }

  public void testAdhocWebServiceException3() {
    AdhocWebServiceException awse3 = new AdhocWebServiceException(new Throwable("This is a throwable exception")); //$NON-NLS-1$
    System.out.println("AdhocWebServiceException :" +awse3); //$NON-NLS-1$    
    assertTrue(true);
  }

  public void testAdhocWebServiceException4() {
    AdhocWebServiceException awse4 = new AdhocWebServiceException("A test AdhocWebService Exception has been thrown", new Throwable());//$NON-NLS-1$
    System.out.println("UIException :" +awse4); //$NON-NLS-1$    
    assertTrue(true);
  }

  public static void main(String[] args) {
    AdhocWebServiceExceptionTest test = new AdhocWebServiceExceptionTest();
    try {
      test.testAdhocWebServiceException1();
      test.testAdhocWebServiceException2();
      test.testAdhocWebServiceException3();
      test.testAdhocWebServiceException4();
    } finally {
      BaseTest.shutdown();
    }
  }
}
