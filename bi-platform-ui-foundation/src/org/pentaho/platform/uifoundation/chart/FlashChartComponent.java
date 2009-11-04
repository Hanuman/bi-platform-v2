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
 * @created May 1, 2006 
 * @author James Dixon
 */

package org.pentaho.platform.uifoundation.chart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

public class FlashChartComponent extends AbstractChartComponent {

  private static final long serialVersionUID = 925147871232129020L;

  protected Node chartNode;

  protected Node chartTemplate;

  protected String backgroundColor;

  protected String chartType = "bar"; //$NON-NLS-1$

  private String valueItem = null;

  private String nameItem = null;

  private int topX = -1;

  public FlashChartComponent(final String definitionPath, final int width, final int height,
      final IPentahoUrlFactory urlFactory, final List messages) {
    super(urlFactory, messages);
    this.definitionPath = definitionPath;
    this.width = width;
    this.height = height;
    ActionInfo info = ActionInfo.parseActionString(definitionPath);
    if (info != null) {
      setSourcePath(info.getSolutionName() + File.separator + info.getPath());
    }
    AbstractChartComponent.logger = LogFactory.getLog(this.getClass());
  }

  @Override
  public Log getLogger() {
    return AbstractChartComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public Document getXmlContent() {

    return null;

  }

  public void setTopX(final int topX) {
    this.topX = topX;
  }

  @Override
  public boolean setDataAction(final String chartDefinition) {
    IActionSequenceResource resource = new ActionSequenceResource(
        "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$ //$NON-NLS-2$
        chartDefinition);
    try {
      Document dataActionDocument = PentahoSystem.get(ISolutionRepository.class, getSession()).getResourceAsDocument(resource, ISolutionRepository.ACTION_EXECUTE);
      if (dataActionDocument == null) {
        return false;
      }
      Node dataNode = dataActionDocument.selectSingleNode("chart/data"); //$NON-NLS-1$
      chartNode = dataActionDocument.selectSingleNode("chart"); //$NON-NLS-1$
      title = XmlDom4JHelper.getNodeText("title", chartNode); //$NON-NLS-1$
      chartType = XmlDom4JHelper.getNodeText("chart_type", chartNode, "bar"); //$NON-NLS-1$ //$NON-NLS-2$
      boolean isGauge = chartType.toLowerCase().startsWith("dial"); //$NON-NLS-1$
      chartTemplate = chartNode.selectSingleNode(isGauge ? "template/gauge" : "template/chart"); //$NON-NLS-1$ //$NON-NLS-2$
      if (chartTemplate == null) {
        String templatePath = XmlDom4JHelper.getNodeText("template-file", chartNode); //$NON-NLS-1$
        if (templatePath != null) {
          // we have a reference to a document to look up
          IActionSequenceResource templateResource = new ActionSequenceResource(
              "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$ //$NON-NLS-2$
              templatePath);
          try {
            Document templateDocument = PentahoSystem.get(ISolutionRepository.class, getSession()).getResourceAsDocument(
                templateResource, ISolutionRepository.ACTION_EXECUTE);
            chartTemplate = templateDocument.getRootElement();
          } catch (Exception e) {
            // the chart template document is not valid
            error(Messages.getInstance().getErrorString("FlashChartComponent.ERROR_0001_CHART_TEMPLATE_INVALID"), e); //$NON-NLS-1$
          }
        }
      }
      solution = XmlDom4JHelper.getNodeText("data-solution", dataNode); //$NON-NLS-1$
      actionPath = XmlDom4JHelper.getNodeText("data-path", dataNode); //$NON-NLS-1$
      actionName = XmlDom4JHelper.getNodeText("data-action", dataNode); //$NON-NLS-1$
      actionOutput = XmlDom4JHelper.getNodeText("data-output", dataNode); //$NON-NLS-1$
      valueItem = XmlDom4JHelper.getNodeText("data-value", dataNode); //$NON-NLS-1$
      nameItem = XmlDom4JHelper.getNodeText("data-name", dataNode); //$NON-NLS-1$
      backgroundColor = XmlDom4JHelper.getNodeText("chart/background-color", dataActionDocument, "#ffffff"); //$NON-NLS-1$ //$NON-NLS-2$
      byRow = XmlDom4JHelper.getNodeText("data-orientation", dataNode, "rows").equals("rows"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if (width == -1) {
        width = (int) XmlDom4JHelper.getNodeText("chart/width", dataActionDocument, 400); //$NON-NLS-1$
      }
      if (height == -1) {
        height = (int) XmlDom4JHelper.getNodeText("chart/height", dataActionDocument, 300); //$NON-NLS-1$
      }
    } catch (Exception e) {
      error(Messages.getInstance().getErrorString(
          "CategoryDatasetChartComponent.ERROR_0001_INVALID_CHART_DEFINITION", chartDefinition), e); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  @Override
  public String getContent(final String mimeType) {
    if (chartType.toLowerCase().startsWith("dial")) { //$NON-NLS-1$
      return getDialContent(mimeType);
    } else {
      return getChartContent(mimeType);
    }

  }

  protected Object[] getValue() {
    Object result[] = new Object[2];

    IPentahoResultSet resultSet = null;
    try {
      resultSet = getActionData();
      if (resultSet == null) {
        return null;
      }
      IPentahoMetaData metaData = resultSet.getMetaData();
      // TODO support multiple column headers / row headers
      // TODO support an iteration across columns for a given row

      // find the column that we have been told to you
      Object columnHeaders[][] = metaData.getColumnHeaders();
      int nameColumnNo = -1;
      int valueColumnNo = -1;
      for (int idx = 0; idx < columnHeaders[0].length; idx++) {
        if (columnHeaders[0][idx].toString().equalsIgnoreCase(nameItem)) {
          nameColumnNo = idx;
        }
        if (columnHeaders[0][idx].toString().equalsIgnoreCase(valueItem)) {
          valueColumnNo = idx;
        }
      }
      if (nameColumnNo == -1) {
        // we did not find the specified name column
        error(Messages.getInstance().getErrorString("FlashDial.ERROR_0001_NAME_COLUMN_MISSING", nameItem)); //$NON-NLS-1$
        return null;
      }

      if (valueColumnNo == -1) {
        // we did not find the specified name column
        error(Messages.getInstance().getErrorString("FlashDial.ERROR_0002_VALUE_COLUMN_MISSING", valueItem)); //$NON-NLS-1$
        return null;
      }

      Object row[] = resultSet.next();
      while (row != null) {
        result[0] = row[nameColumnNo].toString();
        try {
          result[1] = new Double(row[valueColumnNo].toString());
        } catch (Exception e) {
        }
        row = resultSet.next();
      }
    } finally {
      if (resultSet != null) {
        resultSet.dispose();
      }
    }
    return result;
  }

  public String getDialContent(final String mimeType) {
    return getDialContent(mimeType, true);
  }

  public String getDialContent(final String mimeType, boolean objectWrapper) {
    Object data[] = null;

    Object tmpVal = getParameter("value", null); //$NON-NLS-1$
    if (tmpVal != null) {
      data = fixupDialData(getParameter("title", title), tmpVal); //$NON-NLS-1$
    } else {
      data = getValue();
      if ((data != null) && (data.length > 0)) {
        if (data.length == 1) {
          tmpVal = data[0];
          data = fixupDialData(getParameter("title", title), data[0]); //$NON-NLS-1$
        } else {
          tmpVal = data[1];
          data = fixupDialData(data[0], data[1]);
        }
      }

      if (data == null) {
        return Messages.getInstance().getErrorString("FlashChartComponent.ERROR_0002_DIAL_DATA_INVALID", String.valueOf(tmpVal)); //$NON-NLS-1$
      }
    }

    title = (String) data[0];
    double value = ((Double) data[1]).doubleValue();
    double span = 0;
    if (chartType.equalsIgnoreCase("DialPct")) { //$NON-NLS-1$
      double fullSpan = XmlDom4JHelper.getNodeText("dial_limits/@full-span", chartNode, 360.0); //$NON-NLS-1$
      double minClamp = XmlDom4JHelper.getNodeText("dial_limits/@min-clamp", chartNode, 0); //$NON-NLS-1$
      double maxClamp = XmlDom4JHelper.getNodeText("dial_limits/@max-clamp", chartNode, 360.0); //$NON-NLS-1$

      span = value / 100.0 * fullSpan;
      span = Math.max(minClamp, Math.min(maxClamp, span));
    } else {
      span = value;
    }

//    String solutionDir = "system/tmp/"; //$NON-NLS-1$
    String fileNamePrefix = "tmp_flash_"; //$NON-NLS-1$
    String extension = ".xml"; //$NON-NLS-1$
    String fileName = null;
    String template = ""; //$NON-NLS-1$
    String gaugeLicense = PentahoSystem.getSystemSetting("FlashChart/GaugeLicense", ""); //$NON-NLS-1$ //$NON-NLS-2$
    if (gaugeLicense.length() > 0) {
      gaugeLicense = "license=" + gaugeLicense + "&"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    if (chartTemplate != null) {
      template = chartTemplate.asXML().replace('"', '\'');
      template = template.replaceFirst("\\{title\\}", title); //$NON-NLS-1$
      template = template.replaceFirst("\\{span\\}", String.valueOf(span).replace('"', '\'')); //$NON-NLS-1$
      template = template.replaceFirst("\\{data\\}", String.valueOf(value).replace('"', '\'')); //$NON-NLS-1$

      if (urlTemplate != null) {
        template = template.replaceFirst("\\{drill-url\\}", urlTemplate); //$NON-NLS-1$
      }

      // Do tick marks
      List nodeList = chartNode.selectNodes("radial_ticks"); //$NON-NLS-1$
      for (int i = 0; i < nodeList.size(); ++i) {
        template = setRadialTicks((Node) nodeList.get(i), template);
      }

      nodeList = chartNode.selectNodes("radial_numbers"); //$NON-NLS-1$
      for (int i = 0; i < nodeList.size(); ++i) {
        template = setRadialNumbers((Node) nodeList.get(i), template);
      }

    } else {
      StringBuffer sb = new StringBuffer();
      sb.append("<gauge>"); //$NON-NLS-1$
      sb
          .append("  <rotate x='").append(width / 2).append("' y='").append(height / 2).append("' span='").append(String.valueOf(span)).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      sb
          .append("    <line x1='").append(width / 2).append("' y1='0' x2='").append(width / 2).append("' y2='").append(height / 2).append("' />\n"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      sb.append("  </rotate>\n"); //$NON-NLS-1$
      sb.append("</gauge>"); //$NON-NLS-1$
      template = sb.toString();
    }

    if (!objectWrapper) {
      return template;
    }

    // create a temporary file
    try {
      File file = PentahoSystem.getApplicationContext().createTempFile(getSession(), fileNamePrefix, extension, true);
      fileName = file.getName();
      if (file.canWrite()) {
        Writer out = new FileWriter(file);
        out.write(template);
        out.close();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

    StringBuffer sb = new StringBuffer();
    sb
        .append("<OBJECT classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\"\n") //$NON-NLS-1$
        .append("codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\" ") //$NON-NLS-1$
        .append("WIDTH=\"").append(width).append("\" HEIGHT=\"").append(height).append("\" id=\"charts\" ALIGN=\"\">") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        .append("<PARAM NAME=\"movie\" VALUE=\"").append(baseUrl).append("chart/gauge.swf?").append(gaugeLicense).append("xml_source=").append(baseUrl).append("getImage?image=").append(fileName).append("\">") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        .append("<PARAM NAME=\"quality\" VALUE=\"high\"> ") //$NON-NLS-1$
        .append("<PARAM NAME=\"bgcolor\" VALUE=\"").append(backgroundColor).append("\"> ") //$NON-NLS-1$ //$NON-NLS-2$

        .append("<EMBED src=\"").append(baseUrl).append("chart/gauge.swf?").append(gaugeLicense).append("xml_source=").append(baseUrl).append("getImage?image=").append(fileName).append("\" ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
        .append("quality=\"high\" bgcolor=\"").append(backgroundColor).append("\" WIDTH=\"").append(width).append("\" HEIGHT=\"").append(height).append("\" NAME=\"charts\" ALIGN=\"\" ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        .append(
            "TYPE=\"application/x-shockwave-flash\" PLUGINSPAGE=\"http://www.macromedia.com/go/getflashplayer\"></EMBED>") //$NON-NLS-1$
        .append(" </OBJECT>"); //$NON-NLS-1$

    return sb.toString();
  }

  private Object[] fixupDialData(Object inTitle, final Object value) {
    Object[] data = null;
    if (value == null) {
      return data;
    }

    if (inTitle == null) {
      inTitle = ""; //$NON-NLS-1$
    }
    inTitle = inTitle.toString();

    try {
      if (value instanceof Double) {
        data = new Object[] { inTitle, value };
      } else {
        data = new Object[] { inTitle, new Double(value.toString()) };
      }
    } catch (Throwable t) {
      error(Messages.getInstance().getErrorString("FlashChartComponent.ERROR_0002_DIAL_DATA_INVALID", value.toString())); //$NON-NLS-1$
    }
    return (data);
  }

  //function that generates the XML code to draw radial ticks
  private String setRadialTicks(final Node node, final String template) {
    try {
      String xml = setRadialTicks(XmlDom4JHelper.getNodeText("@x", node, (width / 2.0)), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@y", node, (height / 2.0)), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@radius", node, (height / 2.0)), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@length", node, 8.0), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@start_angle", node, 0.0), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@end_angle", node, 350.0), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@tick_count", node, 36), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@thickness", node, 2), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@color", node, "000000") //$NON-NLS-1$ //$NON-NLS-2$
      );
      String param_name = XmlDom4JHelper.getNodeText("@param_name", node, null); //$NON-NLS-1$
      return (template.replaceAll("\\{" + param_name + "\\}", xml)); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (Throwable t) {
      error(Messages.getInstance().getString(
          "FlashChartComponent.ERROR_0003_INVALID_XML_ATTRIBUTES_FOR_DIAL", "radial_ticks", node.asXML())); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return (template);
  }

  private String setRadialTicks(final double x_center, final double y_center, final double radius, final double length,
      final double start_angle, final double end_angle, final long ticks_count, final long thickness, final String color) {
    StringBuffer sb = new StringBuffer();
    for (double i = start_angle; i <= end_angle; i += (end_angle - start_angle) / (ticks_count - 1)) {
      sb
          .append("<line x1='").append(x_center + Math.sin(Math.toRadians(i)) * radius).append("' y1='").append(y_center - Math.cos(Math.toRadians(i)) * radius).append("'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      sb
          .append(" x2='").append(x_center + Math.sin(Math.toRadians(i)) * (radius + length)).append("' y2='").append(y_center - Math.cos(Math.toRadians(i)) * (radius + length)).append("'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      sb.append(" thickness='").append(thickness).append("' color='").append(color).append("' />\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return (sb.toString());
  }

  //function that generates the XML code to draw radial ticks
  private String setRadialNumbers(final Node node, final String template) {
    try {
      String xml = setRadialNumbers(XmlDom4JHelper.getNodeText("@x", node, (width / 2.0)), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@y", node, (height / 2.0)), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@radius", node, (height / 2.0)), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@start_angle", node, 0.0), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@end_angle", node, 350.0), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@start_number", node, 0), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@end_number", node, 340), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@tick_count", node, 18), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@font_size", node, 12), //$NON-NLS-1$
          XmlDom4JHelper.getNodeText("@color", node, "000000") //$NON-NLS-1$ //$NON-NLS-2$
      );
      String param_name = XmlDom4JHelper.getNodeText("@param_name", node, null); //$NON-NLS-1$
      return (template.replaceAll("\\{" + param_name + "\\}", xml)); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (Throwable t) {
      error(Messages.getInstance().getString(
          "FlashChartComponent.ERROR_0003_INVALID_XML_ATTRIBUTES_FOR_DIAL", "radial_numbers", node.asXML())); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return (template);
  }

  //function that generates the XML code to draw radial numbers
  private String setRadialNumbers(final double x_center, final double y_center, final double radius,
      final double start_angle, final double end_angle, final long start_number, final long end_number,
      final long ticks_count, final long font_size, final String color) {
    StringBuffer sb = new StringBuffer();
    long number = start_number;

    for (double i = start_angle; i <= end_angle; i += (end_angle - start_angle) / (ticks_count - 1)) {
      sb
          .append("<text x='").append(x_center + Math.sin(Math.toRadians(i)) * radius).append("' y='").append(y_center - Math.cos(Math.toRadians(i)) * radius).append("'"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
      sb
          .append(" width='200' size='").append(font_size).append("' color='").append(color).append("' align='left' rotation='").append(i).append("'>"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
      sb.append(number).append("</text>\n"); //$NON-NLS-1$
      number += (end_number - start_number) / (ticks_count - 1);
    }
    return (sb.toString());
  }

  public String getChartContent(final String mimeType) {
    return getChartContent(mimeType, true);
  }

  public String getChartContent(final String mimeType, boolean objectWrapper) {

    IPentahoResultSet data = getActionData();

    if ((data == null) || (data.getRowCount() == 0)) {
      // there is no data to display, prevent the default chart from showing
      // TODO surface any error to the UI
      return Messages.getInstance().getString("FlashChartComponent.USER_NO_DATA"); //$NON-NLS-1$
    }

    ArrayList nameList = new ArrayList();
    ArrayList series1 = new ArrayList();
    ArrayList series2 = new ArrayList();
    ArrayList series3 = new ArrayList();
    ArrayList series4 = new ArrayList();
    StringBuffer dataXml = new StringBuffer();
    Object row[];
    if (data != null) {
      row = data.next();
      int n = 0;
      while (row != null) {
        if (row[0] == null) {
          nameList.add("null"); //$NON-NLS-1$
        } else {
          nameList.add(row[0].toString());
        }
        if (row.length > 1) {
          if (row[1] == null) {
            series1.add("<null/>"); //$NON-NLS-1$
          } else {
            series1.add(row[1].toString());
          }
        }
        if (row.length > 2) {
          if (row[2] == null) {
            series2.add("<null/>"); //$NON-NLS-1$
          } else {
            series2.add(row[2].toString());
          }
        }
        if (row.length > 3) {
          if (row[3] == null) {
            series3.add("<null/>"); //$NON-NLS-1$
          } else {
            series3.add(row[3].toString());
          }
        }
        if (row.length > 4) {
          if (row[4] == null) {
            series4.add("<null/>"); //$NON-NLS-1$
          } else {
            series4.add(row[4].toString());
          }
        }
        row = data.next();
        n++;
        if (n == topX) {
          row = null;
        }
      }
      dataXml.append("<row>"); //$NON-NLS-1$
      dataXml.append("<null/>"); //$NON-NLS-1$
      int start = 0;
      int end = nameList.size();
      int step = 1;
      if (topX != -1) {
        start = end - 1;
        end = -1;
        step = -1;
      }
      if (data != null) {
        for (int i = start; i != end; i += step) {
          dataXml.append("<string>"); //$NON-NLS-1$
          dataXml.append(XmlHelper.encode((String) nameList.get(i)));
          dataXml.append("</string>"); //$NON-NLS-1$
        }
      }
      dataXml.append("</row>"); //$NON-NLS-1$
      if (series1.size() > 0) {
        dataXml.append("<row>"); //$NON-NLS-1$
        dataXml.append("<string>"); //$NON-NLS-1$
        dataXml.append(XmlHelper.encode((String) data.getMetaData().getColumnHeaders()[0][1]));
        dataXml.append("</string>"); //$NON-NLS-1$
        for (int i = start; i != end; i += step) {
          dataXml.append("<number>"); //$NON-NLS-1$
          dataXml.append((String) series1.get(i));
          dataXml.append("</number>"); //$NON-NLS-1$
        }
        dataXml.append("</row>"); //$NON-NLS-1$
      }
      if (series2.size() > 0) {
        dataXml.append("<row>"); //$NON-NLS-1$
        dataXml.append("<string>"); //$NON-NLS-1$
        dataXml.append(XmlHelper.encode((String) data.getMetaData().getColumnHeaders()[0][2]));
        dataXml.append("</string>"); //$NON-NLS-1$
        for (int i = start; i != end; i += step) {
          dataXml.append("<number>"); //$NON-NLS-1$
          dataXml.append((String) series2.get(i));
          dataXml.append("</number>"); //$NON-NLS-1$
        }
        dataXml.append("</row>"); //$NON-NLS-1$
      }
      if (series3.size() > 0) {
        dataXml.append("<row>"); //$NON-NLS-1$
        dataXml.append("<string>"); //$NON-NLS-1$
        dataXml.append(XmlHelper.encode((String) data.getMetaData().getColumnHeaders()[0][3]));
        dataXml.append("</string>"); //$NON-NLS-1$
        for (int i = start; i != end; i += step) {
          dataXml.append("<number>"); //$NON-NLS-1$
          dataXml.append((String) series3.get(i));
          dataXml.append("</number>"); //$NON-NLS-1$
        }
        dataXml.append("</row>"); //$NON-NLS-1$
      }
      if (series4.size() > 0) {
        dataXml.append("<row>"); //$NON-NLS-1$
        dataXml.append("<string>"); //$NON-NLS-1$
        dataXml.append(XmlHelper.encode((String) data.getMetaData().getColumnHeaders()[0][4]));
        dataXml.append("</string>"); //$NON-NLS-1$
        for (int i = start; i != end; i += step) {
          dataXml.append("<number>"); //$NON-NLS-1$
          dataXml.append((String) series4.get(i));
          dataXml.append("</number>"); //$NON-NLS-1$
        }
        dataXml.append("</row>"); //$NON-NLS-1$
      }
    }
    String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

//    String solutionDir = "system/tmp/"; //$NON-NLS-1$
    String fileNamePrefix = "tmp_flash_"; //$NON-NLS-1$
    String extension = ".xml"; //$NON-NLS-1$
    String fileName = null;
    String template = ""; //$NON-NLS-1$
    String chartLicense = PentahoSystem.getSystemSetting("FlashChart/ChartLicense", ""); //$NON-NLS-1$ //$NON-NLS-2$
    if (chartLicense.length() > 0) {
      chartLicense = "license=" + chartLicense + "&"; //$NON-NLS-1$ //$NON-NLS-2$ 
    }
    if (chartTemplate != null) {
      template = chartTemplate.asXML().replace('"', '\'');
      if (title != null) {
        template = template.replaceFirst("\\{title\\}", title); //$NON-NLS-1$
      } else {
        template = template.replaceFirst("\\{title\\}", ""); //$NON-NLS-1$ //$NON-NLS-2$
      }
      template = template.replaceFirst("\\{data\\}", dataXml.toString().replace('"', '\'')); //$NON-NLS-1$
      if (urlTemplate != null) {
        template = template.replaceFirst("\\{drill-url\\}", urlTemplate); //$NON-NLS-1$
      }
    } else {
      StringBuffer sb = new StringBuffer();
      sb.append("<chart>"); //$NON-NLS-1$
      sb.append("<chart_data>"); //$NON-NLS-1$
      sb.append(dataXml.toString().replace('"', '\''));
      sb.append("</chart_data>"); //$NON-NLS-1$
      sb.append("<chart_grid_h alpha='20' color='000000' thickness='1' type='solid' />"); //$NON-NLS-1$
      sb
          .append("<chart_rect positive_color='ffffff' positive_alpha='20' negative_color='ff0000' negative_alpha='10' />"); //$NON-NLS-1$
      sb.append("<chart_type>"); //$NON-NLS-1$
      sb.append(chartType);
      sb.append("</chart_type>"); //$NON-NLS-1$
      sb
          .append("<chart_value color='ffffff' alpha='90' font='arial' bold='true' size='12' position='inside' prefix='' suffix='' decimals='0' separator='' as_percentage='true' />"); //$NON-NLS-1$

      sb
          .append("<legend_label layout='horizontal' bullet='circle' font='arial' bold='true' size='13' color='ffffff' alpha='85' />"); //$NON-NLS-1$
      sb
          .append("<legend_rect fill_color='aaaaaa' fill_alpha='100' line_color='000000' line_alpha='0' line_thickness='0' />"); //$NON-NLS-1$

      sb.append("</chart>"); //$NON-NLS-1$
      template = sb.toString();
    }

    if (!objectWrapper) {
      return template;
    }

    /*
     <OBJECT classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
     codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" 
     WIDTH="400" 
     HEIGHT="250" 
     id="gauge" 
     ALIGN="">                                                      
     <PARAM NAME=movie VALUE="gauge.swf?xml_source=sample.xml&license=XXXXXXXXXXXXXXXXXX.XXXXXXXXXXX">
     <PARAM NAME=quality VALUE=high>
     <PARAM NAME=bgcolor VALUE=#888888>

     <EMBED src="gauge.swf?xml_source=sample.xml&license=XXXXXXXXXXXXXXXXXX.XXXXXXXXXXX" 
     quality=high 
     bgcolor=#888888  
     WIDTH="400" 
     HEIGHT="250" 
     NAME="gauge" 
     ALIGN="" 
     TYPE="application/x-shockwave-flash" 
     PLUGINSPAGE="http://www.macromedia.com/go/getflashplayer">
     </EMBED>
     </OBJECT>
     */
    if (template.length() < 0) {
      // this is a small chart file, lets assume all the browsers are ok with this size
      // TODO verify the maximum size of parameters for the major browsers
      StringBuffer sb = new StringBuffer();
      sb
          .append("<OBJECT classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\"\n") //$NON-NLS-1$
          .append("codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\" ") //$NON-NLS-1$
          .append("WIDTH=\"").append(width).append("\" HEIGHT=\"").append(height).append("\" id=\"charts\" ALIGN=\"\">") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          .append("<PARAM NAME=\"movie\" VALUE=\"").append(baseUrl).append("chart/charts.swf?").append(chartLicense).append("library_path=").append(baseUrl).append("chart/charts_library\">") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          .append("<PARAM NAME=\"quality\" VALUE=\"high\"> ") //$NON-NLS-1$
          .append("<PARAM NAME=\"bgcolor\" VALUE=\"").append(backgroundColor).append("\"> ") //$NON-NLS-1$ //$NON-NLS-2$
          .append("<PARAM NAME=\"FlashVars\" VALUE=\"source_data=").append(template).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
          .append("<EMBED src=\"").append(baseUrl).append("chart/charts.swf?").append(chartLicense).append("library_path=").append(baseUrl).append("chart/charts_library\" ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          .append("FlashVars=\"source_data=").append(template).append("\" ") //$NON-NLS-1$ //$NON-NLS-2$
          .append("quality=\"high\" bgcolor=\"").append(backgroundColor).append("\" WIDTH=\"").append(width).append("\" HEIGHT=\"").append(height).append("\" NAME=\"charts\" ALIGN=\"\" ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          .append(
              "TYPE=\"application/x-shockwave-flash\" PLUGINSPAGE=\"http://www.macromedia.com/go/getflashplayer\"></EMBED>") //$NON-NLS-1$
          .append("  </OBJECT>"); //$NON-NLS-1$
      return sb.toString();
    } else {
      // create a temporary file
      try {
        File file = PentahoSystem.getApplicationContext().createTempFile(getSession(), fileNamePrefix, extension, true);
        fileName = file.getName();
        if (file.canWrite()) {
          Writer out = new FileWriter(file);
          out.write(template);
          out.close();
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      StringBuffer sb = new StringBuffer();
      sb
          .append("<OBJECT classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\"\n") //$NON-NLS-1$
          .append("codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\" ") //$NON-NLS-1$
          .append("WIDTH=\"").append(width).append("\" HEIGHT=\"").append(height).append("\" id=\"charts\" ALIGN=\"\">") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          .append("<PARAM NAME=\"movie\" VALUE=\"").append(baseUrl).append("chart/charts.swf?").append(chartLicense).append("library_path=").append(baseUrl).append("chart/charts_library&xml_source=").append(baseUrl).append("getImage?image=").append(fileName).append("\">") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
          .append("<PARAM NAME=\"quality\" VALUE=\"high\"> ") //$NON-NLS-1$
          .append("<PARAM NAME=\"bgcolor\" VALUE=\"").append(backgroundColor).append("\"> ") //$NON-NLS-1$ //$NON-NLS-2$
          .append("<EMBED src=\"").append(baseUrl).append("chart/charts.swf?").append(chartLicense).append("library_path=").append(baseUrl).append("chart/charts_library&xml_source=").append(baseUrl).append("getImage?image=").append(fileName).append("\" ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ 
          .append("quality=\"high\" bgcolor=\"").append(backgroundColor).append("\" WIDTH=\"").append(width).append("\" HEIGHT=\"").append(height).append("\" NAME=\"charts\" ALIGN=\"\" ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          .append(
              "TYPE=\"application/x-shockwave-flash\" PLUGINSPAGE=\"http://www.macromedia.com/go/getflashplayer\"></EMBED>") //$NON-NLS-1$
          .append("  </OBJECT>"); //$NON-NLS-1$
      return sb.toString();
    }

  }

  @Override
  public void dispose() {

  }

}
