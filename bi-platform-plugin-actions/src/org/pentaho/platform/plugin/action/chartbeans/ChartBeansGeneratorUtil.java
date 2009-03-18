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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.pentaho.chart.model.ChartModel;
import org.pentaho.chart.model.util.ChartSerializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SolutionHelper;

import com.ibm.icu.text.MessageFormat;

/**
 * Takes all inputs necessary to generate a chart and passes them to the ChartBeans engine (via an xaction).
 */
public class ChartBeansGeneratorUtil {

  private static final String HTML_TEMPLATE = "<html><head><title>Command: doChart</title></head><body>{0}</body></html>"; //$NON-NLS-1$

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

  /**
   * Convenience method that returns a complete HTML document containing the chart. Resource references point back to 
   * the BI Server.
   */
  public static String createChartAsHtml(IPentahoSession userSession, String serializedChartDataDefinition,
      String serializedChartModel, int chartWidth, int chartHeight) throws IOException {

    ChartModel chartModel = ChartSerializer.deSerialize(serializedChartModel);

    String html = null;

    if (chartModel.getChartEngine() == ChartModel.CHART_ENGINE_JFREE) {
      final String SOLUTION_TMP_DIR = "system/tmp/"; //$NON-NLS-1$
      File chartFileOnServer = new File(new File(PentahoSystem.getApplicationContext().getFileOutputPath(
          SOLUTION_TMP_DIR)), java.util.UUID.randomUUID().toString());

      BufferedOutputStream bos = null;
      try {
        bos = new BufferedOutputStream(new FileOutputStream(chartFileOnServer));
        ChartBeansGeneratorUtil.createChart(userSession, serializedChartDataDefinition, serializedChartModel,
            chartWidth, chartHeight, bos);
      } finally {
        IOUtils.closeQuietly(bos);
      }

      final String IMAGE_URL_TEMPLATE = "{0}getImage?image={1}"; //$NON-NLS-1$
      final String imageUrl = MessageFormat.format(IMAGE_URL_TEMPLATE, new String[] {
          PentahoSystem.getApplicationContext().getBaseUrl(), chartFileOnServer.getName() });
      html = ChartBeansGeneratorUtil.mergeStaticImageHtmlTemplate(imageUrl);

    } else if (chartModel.getChartEngine() == ChartModel.CHART_ENGINE_OPENFLASH) {

      ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
      ChartBeansGeneratorUtil.createChart(userSession, serializedChartDataDefinition, serializedChartModel, chartWidth,
          chartHeight, tmpOut);
      final String ENCODING = "UTF-8"; //$NON-NLS-1$
      ByteArrayInputStream in = new ByteArrayInputStream(tmpOut.toByteArray());
      IOUtils.closeQuietly(tmpOut);
      String openFlashChartJson = IOUtils.toString(in, ENCODING);
      IOUtils.closeQuietly(in);

      final String SWF_URL_TEMPLATE = "{0}openflashchart/open-flash-chart-full-embedded-font.swf"; //$NON-NLS-1$
      final String swfUrl = MessageFormat.format(SWF_URL_TEMPLATE, new String[] { PentahoSystem.getApplicationContext()
          .getBaseUrl() });
      html = ChartBeansGeneratorUtil.mergeOpenFlashChartHtmlTemplate(openFlashChartJson, swfUrl);

    } else {
      throw new IllegalArgumentException("unrecognized chart engine");
    }

    return html;
  }

  /**
   * Returns a complete HTML document that references a static image held in a temporary file on the server. 
   * <p>Only exposed for debugging (i.e. hosted mode) purposes.</p>
   */
  public static String mergeStaticImageHtmlTemplate(String imageUrl) {
    final String BODY_TEMPLATE = "<img src=\"{0}\" />"; //$NON-NLS-1$
    final String body = MessageFormat.format(BODY_TEMPLATE, new String[] { imageUrl });
    return MessageFormat.format(HTML_TEMPLATE, new String[] { body });
  }

  /**
   * Returns a complete HTML document that references an Open Flash Chart SWF resource that resides on the server along
   * with the data that should be displayed in the chart (via a JavaScript function that returns a JSON string). 
   * <p>Only exposed for debugging (i.e. hosted mode) purposes.</p>
   */
  public static String mergeOpenFlashChartHtmlTemplate(String openFlashChartJson, String swfUrl) {
    // JavaScript template contains a namespaced function
    // single quotes wrap curly braces so that MessageFormat is happy
    // two consecutive single quotes yields a single single quote in the result
    final String JS_TEMPLATE = "<script type=\"text/javascript\">"
        + "var org='{' '}'; org.pentaho='{' '}'; org.pentaho.chart='{' '}';"
        + "org.pentaho.chart.getChartData = function() '{' return ''{0}''; '}'" + "</script>";
    final String OPEN_FLASH_CHART_TEMPLATE = ""
        + "<object id=\"ofco00b1c87708fe11dea97da1e1ba5b86bc\" height=\"100%\" align=\"middle\" width=\"100%\" "
        + "codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0\" "
        + "classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\">"
        + "<param value=\"sameDomain\" name=\"allowScriptAccess\"/>" + "<param value=\"opaque\" name=\"wmode\"/>"
        + "<param value=\"{0}?get-data=org.pentaho.chart.getChartData\" name=\"movie\"/>"
        + "<param value=\"high\" name=\"quality\"/>" + "<embed id=\"ofce00b1c87708fe11dea97da1e1ba5b86bc\""
        + " height=\"100%\"" + " align=\"middle\"" + " width=\"100%\""
        + " pluginspage=\"http://www.macromedia.com/go/getflashplayer\"" + " type=\"application/x-shockwave-flash\""
        + " allowscriptaccess=\"sameDomain\"" + " bgcolor=\"#FFFFFF\"" + " quality=\"high\"" + " wmode=\"opaque\""
        + " src=\"{0}?get-data=org.pentaho.chart.getChartData\"/>" + "</object>";
    final String BODY_TEMPLATE = "{0}{1}"; //$NON-NLS-1$
    final String js = MessageFormat.format(JS_TEMPLATE, new String[] { openFlashChartJson });
    final String openFlashChartEmbedHtml = MessageFormat.format(OPEN_FLASH_CHART_TEMPLATE, new String[] { swfUrl });
    final String body = MessageFormat.format(BODY_TEMPLATE, new String[] { js, openFlashChartEmbedHtml });
    return MessageFormat.format(HTML_TEMPLATE, new String[] { body });

  }

}
