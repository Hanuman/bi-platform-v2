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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Locale;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.services.SoapHelper;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.request.HttpWebServiceRequestHandler;
import org.pentaho.test.platform.engine.core.BaseTest;

public class SoapUtilTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  public void testSoapUtil() {
    startTest();

    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
    UserSession session = new UserSession("Joe", Locale.US, true, parameterProvider); //$NON-NLS-1$

    OutputStream contentStream = new ByteArrayOutputStream();
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(contentStream, false);
    OutputStream outputStream = getOutputStream("SoapTest", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$

    HttpWebServiceRequestHandler requestHandler = new HttpWebServiceRequestHandler(session, null, outputHandler,
        parameterProvider, null);

    IRuntimeContext runtime = null;
    IRuntimeContext runtime1 = null;
    try {
      outputStream.write(SoapHelper.getSoapHeader().getBytes(LocaleHelper.getSystemEncoding()));

      runtime = run(
          "test", "rules", "XQueryTest.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      SoapHelper.openSoapResponse();
      outputStream.write(SoapHelper.getSoapHeader().getBytes(LocaleHelper.getSystemEncoding()));
      SoapHelper
          .generateSoapResponse(runtime, outputStream, outputHandler, contentStream, requestHandler.getMessages());
      runtime1 = run(
          "test", "dashboard", "departments.rule.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      SoapHelper.generateSoapResponse(runtime1, outputStream, outputHandler, contentStream, requestHandler
          .getMessages());
      SoapHelper.generateSoapError(new StringBuffer(), requestHandler.getMessages());
      SoapHelper.generateSoapResponse(runtime, outputHandler, contentStream, new StringBuffer(), requestHandler
          .getMessages());
      outputStream.write(SoapHelper.getSoapFooter().getBytes(LocaleHelper.getSystemEncoding()));
      SoapHelper.closeSoapResponse();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (runtime != null) {
        runtime.dispose();
      }
    }
    assertTrue(true);
    finishTest();
  }

  public static void main(String[] args) {
    SoapUtilTest test = new SoapUtilTest();
    test.setUp();
    try {
      test.testSoapUtil();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
