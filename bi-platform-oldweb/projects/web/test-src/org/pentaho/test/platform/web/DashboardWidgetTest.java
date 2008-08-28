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
 * @created Aug 15, 2005 
 * @author James Dixon
 */

package org.pentaho.test.platform.web;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.BaseRequestHandler;
import org.pentaho.platform.uifoundation.chart.DashboardWidgetComponent;
import org.pentaho.platform.uifoundation.component.xml.WidgetGridComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.test.platform.engine.core.BaseTest;

public class DashboardWidgetTest extends BaseTest {
  
  private static final String SOLUTION_PATH = "projects/web/test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

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

	public void testWidget2() {
		
		SimpleUrlFactory urlFactory = new SimpleUrlFactory( "" ); //$NON-NLS-1$
		ArrayList messages = new ArrayList();

		DashboardWidgetComponent widget = new DashboardWidgetComponent( DashboardWidgetComponent.TYPE_DIAL, getSolutionPath() + "/samples/charts/dashboardwidget1.dial.xml", 200, 200, urlFactory, messages ); //$NON-NLS-1$

		widget.setValue( 49 );
		widget.setTitle( "test widget 1" ); //$NON-NLS-1$
		widget.setUnits( "" ); //$NON-NLS-1$

        StandaloneSession session = new StandaloneSession("BaseTest.DEBUG_JUNIT_SESSION"); //$NON-NLS-1$
		widget.validate( session, null );
		
        SimpleParameterProvider requestParameters = new SimpleParameterProvider();
        SimpleParameterProvider sessionParameters = new SimpleParameterProvider();
		widget.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters ); 
		widget.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters ); 

		String content = widget.getContent( "text/html" ); //$NON-NLS-1$
        	OutputStream outputStream = getOutputStream("DashboardWidgetTest.testWidget1", ".html"); //$NON-NLS-1$//$NON-NLS-2$
        	try {
        		outputStream.write( content.getBytes() );
        	} catch (Exception e) {
        		// content check will test this
        	}
	}
	
    public void testWidget1() {
        startTest();

        SimpleUrlFactory urlFactory = new SimpleUrlFactory("/testurl?"); //$NON-NLS-1$
        ArrayList messages = new ArrayList();

        DashboardWidgetComponent widget = new DashboardWidgetComponent(DashboardWidgetComponent.TYPE_DIAL, getSolutionPath() +"/samples/charts/dashboardwidget1.dial.xml", 300, 300, urlFactory, messages); //$NON-NLS-1$

        widget.setLoggingLevel(getLoggingLevel());
        widget.setValue(72.5);
        widget.setTitle("test widget 1"); //$NON-NLS-1$
        widget.setUnits("$"); //$NON-NLS-1$

        OutputStream outputStream = getOutputStream("DashboardWidgetTest.testWidget1", ".html"); //$NON-NLS-1$//$NON-NLS-2$
        String contentType = "text/html"; //$NON-NLS-1$

        SimpleParameterProvider requestParameters = new SimpleParameterProvider();
        SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

        HashMap parameterProviders = new HashMap();
        parameterProviders.put(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters); 
        parameterProviders.put(HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters); 
        StandaloneSession session = new StandaloneSession("BaseTest.DEBUG_JUNIT_SESSION"); //$NON-NLS-1$

        SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);
        BaseRequestHandler requestHandler = new BaseRequestHandler(session, null, outputHandler, null, urlFactory);

        try {
            widget.validate(session, requestHandler);
            widget.handleRequest(outputStream, requestHandler, contentType, parameterProviders);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finishTest();

    }

    public void testWidgetGrid1() {
        startTest();

        SimpleUrlFactory urlFactory = new SimpleUrlFactory("/testurl?"); //$NON-NLS-1$
        ArrayList messages = new ArrayList();

        int columns = 4;
        int widgetWidth = 150;
        int widgetHeight = 150;
        WidgetGridComponent widgetGrid = new WidgetGridComponent("test/dashboard/budgetvariance.dial.xml", urlFactory, messages); //$NON-NLS-1$

        widgetGrid.setLoggingLevel(getLoggingLevel());
        widgetGrid.setColumns(columns);
        widgetGrid.setWidgetWidth(widgetWidth);
        widgetGrid.setWidgetHeight(widgetHeight);

        widgetGrid.setDataAction("test", "rules", "headcount_variance_by_region.xaction", "rule-result", "REGION", "VARIANCE"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ 
        OutputStream outputStream = getOutputStream("DashboardWidgetTest.testWidgetGrid1", ".html"); //$NON-NLS-1$//$NON-NLS-2$
        String contentType = "text/html"; //$NON-NLS-1$

        SimpleParameterProvider requestParameters = new SimpleParameterProvider();
        SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

        HashMap parameterProviders = new HashMap();
        parameterProviders.put(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters); 
        parameterProviders.put(HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters); 
        StandaloneSession session = new StandaloneSession("BaseTest.DEBUG_JUNIT_SESSION"); //$NON-NLS-1$

        SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);
        BaseRequestHandler requestHandler = new BaseRequestHandler(session, null, outputHandler, null, urlFactory);

        try {
            widgetGrid.validate(session, requestHandler);
            widgetGrid.handleRequest(outputStream, requestHandler, contentType, parameterProviders);
        } catch (IOException e) {
            e.printStackTrace();
        }

        finishTest();
    }

    public static void main(String[] args) {
        DashboardWidgetTest test = new DashboardWidgetTest();
        test.setUp();
        try {
            test.testWidget1();
            test.testWidget2();
            test.testWidgetGrid1();
        } finally {
            test.tearDown();
            BaseTest.shutdown();
        }
    }

}
