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
 * @created Aug 24, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.web.portal.chart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.chart.AbstractJFreeChartComponent;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.portal.PentahoPortletSession;
import org.pentaho.platform.web.portal.PortletPreferencesParameterProvider;
import org.pentaho.platform.web.portal.PortletRequestParameterProvider;
import org.pentaho.platform.web.portal.PortletSessionParameterProvider;
import org.pentaho.platform.web.portal.PortletUrlFactory;
import org.pentaho.platform.web.portal.ViewPortlet;
import org.pentaho.platform.web.portal.messages.Messages;

public abstract class AbstractDatasetChartPortlet extends ViewPortlet {

  private static final String CHART = "chart"; //$NON-NLS-1$

  private static final Log portletLogger = LogFactory.getLog(AbstractDatasetChartPortlet.class);

  @Override
  public Log getLogger() {
    return AbstractDatasetChartPortlet.portletLogger;
  }

  @Override
  public void processPortletAction(final ActionRequest request, final ActionResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

  }

  @Override
  public void doPortletView(final RenderRequest request, final RenderResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {
    response.setContentType("text/html"); //$NON-NLS-1$

    PortletUrlFactory urlFactory = new PortletUrlFactory(response, request.getWindowState(), request.getPortletMode());

    PortletRequestParameterProvider requestParameters = new PortletRequestParameterProvider(request);
    PortletSessionParameterProvider sessionParameters = new PortletSessionParameterProvider(userSession);
    PortletPreferences prefs = request.getPreferences();
    PortletPreferencesParameterProvider portletPrefsParameters = new PortletPreferencesParameterProvider(prefs);

    String chartDefinitionStr = getSetting(AbstractDatasetChartPortlet.CHART, null, request, requestParameters);

    if (chartDefinitionStr == null) {
      response.getWriter().print(Messages.getString("AbstractDatasetChartPortlet.ERROR_0001.NO_CHART_DEF")); //$NON-NLS-1$
      return;
    }

    ArrayList messages = new ArrayList();
    AbstractJFreeChartComponent chartComponent = null;
    String urlDrillTemplate = getSetting("drill-url", null, request, null); //$NON-NLS-1$
    String outerParams = getSetting("outer-params", null, request, null); //$NON-NLS-1$
    String innerParam = getSetting("inner-param", null, request, null); //$NON-NLS-1$

    try {
      chartComponent = getNewChartComponent(chartDefinitionStr, urlFactory, messages);
      chartComponent.validate(userSession, null);
      chartComponent.setUrlTemplate(urlDrillTemplate);
      if (outerParams != null){
        StringTokenizer tokenizer = new StringTokenizer(outerParams, ";"); //$NON-NLS-1$
        while (tokenizer.hasMoreTokens()) {
          chartComponent.addOuterParamName(tokenizer.nextToken());
        }
      }
      chartComponent.setParamName(innerParam);

      chartComponent.setDataAction(chartDefinitionStr);

      chartComponent.setParameterProvider(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters);
      chartComponent.setParameterProvider(IParameterProvider.SCOPE_SESSION, sessionParameters);
      chartComponent.setParameterProvider(PortletPreferencesParameterProvider.SCOPE_PORTLET_PREFERENCES,
          portletPrefsParameters);

      String content = chartComponent.getContent("text/html"); //$NON-NLS-1$

      if ((content == null) || content.equals("")) { //$NON-NLS-1$
        content = "&nbsp;"; //$NON-NLS-1$
      }
      response.getWriter().print(content);

      if (content == null) {
        StringBuffer buffer = new StringBuffer();
        PentahoSystem.get(IMessageFormatter.class, userSession)
            .formatErrorMessage(
                "text/html", Messages.getString("AbstractDatasetChartPortlet.ERROR_0002.COULD_NOT_CREATE"), messages, buffer); //$NON-NLS-1$ //$NON-NLS-2$
        content = buffer.toString();
      }
    } finally {
      if (chartComponent != null) {
        chartComponent.dispose();
      }
    }
  }

  protected abstract AbstractJFreeChartComponent getNewChartComponent(String definitionPath,
      IPentahoUrlFactory urlFactory, ArrayList messages);
}
