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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Apr 17, 2006 
 * @author James Dixon
 */

package org.pentaho.platform.uifoundation.chart;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.web.SimpleUrlFactory;

/**
 * This class provides wrapper functions to make it easier to generate flash 
 * charts within a dashboard environment.
 */
public class FlashChartHelper {

  /**
   * doFlashChart generates a flash media player chart.  It provides a simple
   * wrapper around the org.pentaho.platform.chart.FlashChartComponent
   * 
   * @param solutionName the solution name
   * @param actionPath the action path
   * @param chartName the xml file describing the chart
   * @param parameterProvider the collection of parameters to customize the chart
   * @param outputStream the output string buffer for the content
   * @param userSession the user session object
   * @param messages a collection to store error and logging messages
   * @param logger logging object
   * 
   * @return true if successful
   */
  public static boolean doFlashChart(final String solutionName, final String actionPath, final String chartName,
      final IParameterProvider parameterProvider, final StringBuffer outputStream, final IPentahoSession userSession,
      final ArrayList messages, final ILogger logger) {

    boolean result = true;
    String outerParams = parameterProvider.getStringParameter("outer-params", null); //$NON-NLS-1$
    String innerParam = parameterProvider.getStringParameter("inner-param", null); //$NON-NLS-1$

    String urlDrillTemplate = parameterProvider.getStringParameter("drill-url", null); //$NON-NLS-1$
    String imageUrl = parameterProvider.getStringParameter("image-url", null); //$NON-NLS-1$
    if (imageUrl == null) {
      imageUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    }

    if (urlDrillTemplate == null) {
      urlDrillTemplate = ""; //$NON-NLS-1$
    }

    int width = (int) parameterProvider.getLongParameter("image-width", 150); //$NON-NLS-1$
    int height = (int) parameterProvider.getLongParameter("image-height", 150); //$NON-NLS-1$
    int topX = (int) parameterProvider.getLongParameter("topX", -1); //$NON-NLS-1$

    SimpleUrlFactory urlFactory = new SimpleUrlFactory(urlDrillTemplate);

    FlashChartComponent chartComponent = null;
    try {
      String chartDefinitionStr = solutionName + File.separator + actionPath + File.separator + chartName;
      chartComponent = new FlashChartComponent(chartDefinitionStr, width, height, urlFactory, messages);
      if (logger != null) {
        chartComponent.setLoggingLevel(logger.getLoggingLevel());
      }
      chartComponent.validate(userSession, null);
      chartComponent.setUrlTemplate(urlDrillTemplate);
      if (outerParams != null) {
        StringTokenizer tokenizer = new StringTokenizer(outerParams, ";"); //$NON-NLS-1$
        while (tokenizer.hasMoreTokens()) {
          chartComponent.addOuterParamName(tokenizer.nextToken());
        }
      }
      chartComponent.setParamName(innerParam);
      chartComponent.setTopX(topX);

      chartComponent.setDataAction(chartDefinitionStr);

      chartComponent.setParameterProvider(IParameterProvider.SCOPE_REQUEST, parameterProvider);

      String content = chartComponent.getContent("text/html"); //$NON-NLS-1$

      if ((content == null) || content.equals("")) { //$NON-NLS-1$
        content = "&nbsp;"; //$NON-NLS-1$
      }
      if (content == null) {
        StringBuffer buffer = new StringBuffer();
        PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage(
            "text/html", Messages.getInstance().getString("Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET"), messages, buffer); //$NON-NLS-1$ //$NON-NLS-2$
        content = buffer.toString();
        result = false;
      }

      outputStream.append(content);

    } finally {
      if (chartComponent != null) {
        chartComponent.dispose();
      }
    }
    return result;

  }

  /**
   * doFlashDial generates a flash media player dial.  It provides a simple
   * wrapper around the org.pentaho.platform.chart.FlashChartComponent
   * 
   * @param solutionName the solution name
   * @param actionPath the action path
   * @param chartName the xml file describing the chart
   * @param parameterProvider the collection of parameters to customize the chart
   * @param outputStream the output string buffer for the content
   * @param userSession the user session object
   * @param messages a collection to store error and logging messages
   * @param logger logging object
   * 
   * @return true if successful
   */
  public static boolean doFlashDial(final String solutionName, final String actionPath, final String actionName,
      final IParameterProvider parameterProvider, final StringBuffer outputStream, final IPentahoSession userSession,
      final ArrayList messages, final ILogger logger) {

    boolean result = true;
    String urlDrillTemplate = parameterProvider.getStringParameter("drill-url", null); //$NON-NLS-1$
    String imageUrl = parameterProvider.getStringParameter("image-url", null); //$NON-NLS-1$
    if (imageUrl == null) {
      imageUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    }

    if (urlDrillTemplate == null) {
      urlDrillTemplate = ""; //$NON-NLS-1$
    }

    int width = (int) parameterProvider.getLongParameter("image-width", 150); //$NON-NLS-1$
    int height = (int) parameterProvider.getLongParameter("image-height", 150); //$NON-NLS-1$
    int topX = (int) parameterProvider.getLongParameter("topX", -1); //$NON-NLS-1$

    SimpleUrlFactory urlFactory = new SimpleUrlFactory(urlDrillTemplate);

    FlashChartComponent chartComponent = null;
    try {
      String chartDefinitionStr = solutionName + File.separator + actionPath + File.separator + actionName;
      chartComponent = new FlashChartComponent(chartDefinitionStr, width, height, urlFactory, messages);
      if (logger != null) {
        chartComponent.setLoggingLevel(logger.getLoggingLevel());
      }
      chartComponent.validate(userSession, null);
      chartComponent.setUrlTemplate(urlDrillTemplate);
      chartComponent.setTopX(topX);

      chartComponent.setDataAction(chartDefinitionStr);

      chartComponent.setParameterProvider(IParameterProvider.SCOPE_REQUEST, parameterProvider);

      String content = chartComponent.getDialContent("text/html"); //$NON-NLS-1$

      if ((content == null) || content.equals("")) { //$NON-NLS-1$
        content = "&nbsp;"; //$NON-NLS-1$
      }

      if (content == null) {
        StringBuffer buffer = new StringBuffer();
        PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage(
            "text/html", Messages.getInstance().getString("Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET"), messages, buffer); //$NON-NLS-1$ //$NON-NLS-2$
        content = buffer.toString();
        result = false;
      }

      outputStream.append(content);

    } finally {
      if (chartComponent != null) {
        chartComponent.dispose();
      }
    }
    return result;
  }
}
