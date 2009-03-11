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
 *
 * @created Jul 19, 2005 
 * @author William Seyler
 * 
 */

package org.pentaho.test.platform.scheduler;

import java.util.Map;

import org.pentaho.test.platform.engine.core.BaseTest;

public class SchedulerAdminTest extends BaseTest {
  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put("quartz", "quartz"); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

//  TODO: Get this working!
//  public void testScheduler() {
//    startTest();
//    SimpleUrlFactory urlFactory = new SimpleUrlFactory("/testurl?"); //$NON-NLS-1$
//    ArrayList messages = new ArrayList();
//    SchedulerAdminUIComponent component = new SchedulerAdminUIComponent(urlFactory, messages);
//    component.setLoggingLevel(getLoggingLevel());
//    OutputStream outputStream = this.getOutputStream("SchedulerAdminTest.testScheduler", ".html"); //$NON-NLS-1$//$NON-NLS-2$
//    String contentType = "text/html"; //$NON-NLS-1$
//
//    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
//    requestParameters.setParameter("schedulerAction", SchedulerAdminUIComponent.GET_IS_SCHEDULER_PAUSED_ACTION_STR); //$NON-NLS-1$
//
//    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();
//
//    HashMap parameterProviders = new HashMap();
//    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParameters);
//    parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
//    StandaloneSession session = new StandaloneSession(Messages.getString("SchedulerTest.DEBUG_SchedulerTest")); //$NON-NLS-1$
//
//    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);
//    BaseRequestHandler requestHandler = new BaseRequestHandler(session, null, outputHandler, null, urlFactory);
//
//    try {
//      component.handleRequest(outputStream, requestHandler, contentType, parameterProviders);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    finishTest();
//
//  }

  public void setUp() {
    // do nothing, get the above test to pass!
  }
  
  public void testDummyTest() {
    // do nothing, get the above test to pass!
  }
  
  public static void main(String[] args) {

    SchedulerAdminTest test = new SchedulerAdminTest();
    test.setUp();
    try {
//      test.testScheduler();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
