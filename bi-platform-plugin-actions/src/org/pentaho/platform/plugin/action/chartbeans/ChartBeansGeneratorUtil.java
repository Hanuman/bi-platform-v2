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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.chartbeans;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.chart.model.ChartModel;
import org.pentaho.chart.model.util.ChartSerializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.services.solution.SolutionHelper;

public class ChartBeansGeneratorUtil {

  private ChartBeansGeneratorUtil() {
  }

  public static void createChart(IPentahoSession userSession, String serializedChartDataDefinition,
      String serializedChartModel, int chartWidth, int chartHeight, OutputStream out) throws IOException {
    ChartBeansGeneratorUtil.internalCreateChart(serializedChartDataDefinition, null, null, null, null,
        serializedChartModel, chartWidth, chartHeight, userSession, out);
  }

  public static void createChart(String mqlQueryString, ChartModel chartModel, int chartWidth, int chartHeight,
      IPentahoSession userSession, OutputStream out) throws IOException {
    String serializedChartModel = ChartSerializer.serialize(chartModel);

    ChartBeansGeneratorUtil.internalCreateChart(null, mqlQueryString, null, null, null, serializedChartModel,
        chartWidth, chartHeight, userSession, out);
  }

  public static void createChart(String mqlQueryString, String serializedChartModel, int chartWidth, int chartHeight,
      IPentahoSession userSession, OutputStream out) throws IOException {
    ChartBeansGeneratorUtil.internalCreateChart(null, mqlQueryString, null, null, null, serializedChartModel,
        chartWidth, chartHeight, userSession, out);
  }

  public static InputStream createChart(IPentahoSession userSession, String serializedChartDataDefinition,
      String serializedChartModel, int chartWidth, int chartHeight) throws IOException {
    return ChartBeansGeneratorUtil.internalCreateChart(serializedChartDataDefinition, null, null, null, null,
        serializedChartModel, chartWidth, chartHeight, userSession, null);
  }

  public static InputStream createChart(String mqlQueryString, ChartModel chartModel, int chartWidth, int chartHeight,
      IPentahoSession userSession) throws IOException {
    String serializedChartModel = ChartSerializer.serialize(chartModel);

    return ChartBeansGeneratorUtil.internalCreateChart(null, mqlQueryString, null, null, null, serializedChartModel,
        chartWidth, chartHeight, userSession, null);
  }

  public static InputStream createChart(String mqlQueryString, String serializedChartModel, int chartWidth,
      int chartHeight, IPentahoSession userSession) throws IOException {
    return ChartBeansGeneratorUtil.internalCreateChart(null, mqlQueryString, null, null, null, serializedChartModel,
        chartWidth, chartHeight, userSession, null);
  }

  /**
   * The engine that processes the parameters from the specific interface methods
   * and writes a chart to the output stream or returns an input stream for reading
   * 
   * @param mqlQueryString
   * @param serializedChartModel
   * @param chartWidth
   * @param chartHeight
   * @param userSession
   * @param outputStream
   * @return
   * @throws IOException
   */
  protected static InputStream internalCreateChart(String serializedChartDataDefinition, String mqlQueryString,
      String seriesColumn, String categoryColumn, String valueColumn, String serializedChartModel, int chartWidth,
      int chartHeight, IPentahoSession userSession, OutputStream outputStream) throws IOException {
    InputStream result = null;
    ByteArrayOutputStream resultOutputStream = null;
    OutputStream out = null;

    // Make code more readable by defining the output result
    boolean returnInputStream = outputStream == null ? true : false;

    // If the caller sends a null OutputStream, then we will return an InputStream
    if (returnInputStream) {
      resultOutputStream = new ByteArrayOutputStream();
      out = new BufferedOutputStream(resultOutputStream);
    } else {
      out = outputStream;
    }

    // Setup parameters to be passed to the xaction
    Map<String, Object> params = new HashMap<String, Object>();

    params.put("chart-model", serializedChartModel); //$NON-NLS-1$
    params.put("chart-width", chartWidth); //$NON-NLS-1$
    params.put("chart-height", chartHeight); //$NON-NLS-1$

    // Chart data definition takes precedence over individual parts
    if (serializedChartDataDefinition != null) {
      // De-serialize the chartDataDefintion and extract relevant parts
      ChartDataDefinition chartDataDefinition = ChartSerializer
          .deSerializeDataDefinition(serializedChartDataDefinition);

      if (chartDataDefinition.getQuery() != null) {
        params.put("query", chartDataDefinition.getQuery()); //$NON-NLS-1$
      }

      if (chartDataDefinition.getDomainColumn() != null) {
        params.put("series-column", chartDataDefinition.getDomainColumn()); //$NON-NLS-1$
      }

      if (chartDataDefinition.getCategoryColumn() != null) {
        params.put("category-column", chartDataDefinition.getCategoryColumn()); //$NON-NLS-1$
      }

      if (chartDataDefinition.getRangeColumn() != null) {
        params.put("value-column", chartDataDefinition.getRangeColumn()); //$NON-NLS-1$
      }
    }

    // Setup remaining data portion of chart
    if (!params.containsKey("query")) { //$NON-NLS-1$
      params.put("query", mqlQueryString); //$NON-NLS-1$
    }

    if (!params.containsKey("series-column")) { //$NON-NLS-1$
      if (seriesColumn != null) {
        params.put("series-column", seriesColumn); //$NON-NLS-1$
      }
    }

    if (!params.containsKey("category-column")) { //$NON-NLS-1$
      if (categoryColumn != null) {
        params.put("category-column", categoryColumn); //$NON-NLS-1$
      }
    }

    if (!params.containsKey("value-column")) { //$NON-NLS-1$
      if (valueColumn != null) {
        params.put("value-column", valueColumn); //$NON-NLS-1$
      }
    }

    SolutionHelper.execute("XAction", userSession, "system/chartbeans/chartbeans_mql.xaction", params, out, true); //$NON-NLS-1$ //$NON-NLS-2$

    if (out instanceof BufferedOutputStream) {
      out.flush();
    }

    if (returnInputStream) {
      result = new ByteArrayInputStream(resultOutputStream.toByteArray());

      return (result);
    }

    return null;
  }

}
