/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jul 19, 2005 
 * @author William Seyler
 * 
 */

package org.pentaho.test.platform.scheduler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.BaseRequestHandler;
import org.pentaho.platform.scheduler.SchedulerAdminUIComponent;
import org.pentaho.platform.scheduler.messages.Messages;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

public class SchedulerAdminTest extends BaseTest {
  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put("quartz", "quartz"); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void testScheduler() {
    startTest();
    SimpleUrlFactory urlFactory = new SimpleUrlFactory("/testurl?"); //$NON-NLS-1$
    ArrayList messages = new ArrayList();
    SchedulerAdminUIComponent component = new SchedulerAdminUIComponent(urlFactory, messages);
    component.setLoggingLevel(getLoggingLevel());
    OutputStream outputStream = this.getOutputStream("SchedulerAdminTest.testScheduler", ".html"); //$NON-NLS-1$//$NON-NLS-2$
    String contentType = "text/html"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    requestParameters.setParameter("schedulerAction", SchedulerAdminUIComponent.GET_IS_SCHEDULER_PAUSED_ACTION_STR); //$NON-NLS-1$

    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParameters);
    parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
    StandaloneSession session = new StandaloneSession(Messages.getString("SchedulerTest.DEBUG_SchedulerTest")); //$NON-NLS-1$

    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);
    BaseRequestHandler requestHandler = new BaseRequestHandler(session, null, outputHandler, null, urlFactory);

    try {
      component.handleRequest(outputStream, requestHandler, contentType, parameterProviders);
    } catch (IOException e) {
      e.printStackTrace();
    }
    finishTest();

  }

  public static void main(String[] args) {

    SchedulerAdminTest test = new SchedulerAdminTest();
    test.setUp();
    try {
      test.testScheduler();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
