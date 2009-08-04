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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
*/ 
package org.pentaho.test.platform.plugin;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.openflashchart.factory.PentahoOFC4JChartHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

@SuppressWarnings({"unchecked","nls"})
public class PentahoFlashChartTest {
  
  public static IPentahoResultSet getRelationData() {
    IPentahoResultSet ips = null;

    ArrayList<String> colHeaders = new ArrayList();

    colHeaders.add(0, "DEPARTMENT");
    colHeaders.add(1, "ACTUAL");
    colHeaders.add(2, "BUDGET");

    ArrayList r1 = new ArrayList();
    r1.add("Sales");
    r1.add(11);
    r1.add(12);
    ArrayList r2 = new ArrayList();
    r2.add("Finance");
    r2.add(14);
    r2.add(9);
    ArrayList r3 = new ArrayList();
    r3.add("Human Resource");
    r3.add(7);
    r3.add(100);

    ArrayList data = new ArrayList();
    data.add(r1);
    data.add(r2);
    data.add(r3);

    ips = MemoryResultSet.createFromLists(colHeaders, data);

    System.out.println(ips.getRowCount());

    return ips;
  }
  
  public static IPentahoResultSet getRelationOneColData() {
    IPentahoResultSet ips = null;

    ArrayList<String> colHeaders = new ArrayList();

    colHeaders.add(0, "DEPARTMENT");
    colHeaders.add(1, "ACTUAL");
    colHeaders.add(2, "BUDGET");

    ArrayList r1 = new ArrayList();
    r1.add("Sales");
    r1.add(11);
    ArrayList r2 = new ArrayList();
    r2.add("Finance");
    r2.add(14);
    ArrayList r3 = new ArrayList();
    r3.add("Human Resource");
    r3.add(7);

    ArrayList data = new ArrayList();
    data.add(r1);
    data.add(r2);
    data.add(r3);

    ips = MemoryResultSet.createFromLists(colHeaders, data);

    System.out.println(ips.getRowCount());

    return ips;
  }
  
  public static IPentahoResultSet getRelationBogusData() {
    IPentahoResultSet ips = null;

    ArrayList<String> colHeaders = new ArrayList();

    colHeaders.add(0, "DEPARTMENT");
    colHeaders.add(1, "ACTUAL");
    colHeaders.add(2, "BUDGET");

    ArrayList r1 = new ArrayList();
    r1.add("Sales");
    r1.add("11");
    ArrayList r2 = new ArrayList();
    r2.add("Finance");
    r1.add("14");
    ArrayList r3 = new ArrayList();
    r3.add("Human Resource");
    r1.add("15");

    ArrayList data = new ArrayList();
    data.add(r1);
    data.add(r2);
    data.add(r3);

    ips = MemoryResultSet.createFromLists(colHeaders, data);

    System.out.println(ips.getRowCount());

    return ips;
  }
  
  public static IPentahoResultSet getXYZRelationData() {
    IPentahoResultSet ips = null;

    ArrayList<String> colHeaders = new ArrayList();

    colHeaders.add(0, "DEPARTMENT");
    colHeaders.add(1, "ACTUAL");
    colHeaders.add(2, "BUDGET");
    colHeaders.add(3, "DIFFERENCE");

    ArrayList r1 = new ArrayList();
    r1.add("Sales");
    r1.add(11);
    r1.add(12);
    r1.add(1);    
    ArrayList r2 = new ArrayList();
    r2.add("Finance");
    r2.add(14);
    r2.add(9);
    r2.add(5);
    
    ArrayList r3 = new ArrayList();
    r3.add("Human Resource");
    r3.add(7);
    r3.add(100);
    r3.add(93);

    ArrayList data = new ArrayList();
    data.add(r1);
    data.add(r2);
    data.add(r3);

    ips = MemoryResultSet.createFromLists(colHeaders, data);

    System.out.println(ips.getRowCount());

    return ips;
  }

  
  public static IPentahoResultSet getDimensionalData() {
    IPentahoResultSet ips = null;

    ArrayList<String> colHeaders = new ArrayList();

    // colHeaders.add(0, "DEPARTMENT");
    colHeaders.add(0, "ACTUAL");
    colHeaders.add(1, "BUDGET");
    colHeaders.add(2, "DIFFERENCE");

    ArrayList<String> rowHeaders = new ArrayList();

    rowHeaders.add(0, "Sales");
    rowHeaders.add(1, "Finance");
    rowHeaders.add(2, "Human Resource");

    
    ArrayList r1 = new ArrayList();
    r1.add(11);
    r1.add(12);
    r1.add(1); 
    ArrayList r2 = new ArrayList();
    r2.add(14);
    r2.add(9);
    r2.add(5);
    ArrayList r3 = new ArrayList();
    r3.add(7);
    r3.add(100);
    r3.add(93);
    
    ArrayList data = new ArrayList();
    data.add(r1);
    data.add(r2);
    data.add(r3);

    ips = createFromLists(colHeaders, rowHeaders, data);

    return ips;
  }
  
  public static MemoryResultSet createFromLists(List colHeaders, List rHeaders, List data) {
    Object columnHeaders[][] = new String[1][colHeaders.size()];
    for (int i = 0; i < colHeaders.size(); i++) {
      columnHeaders[0][i] = colHeaders.get(i);
    }
    Object rowHeaders[][] = new String[rHeaders.size()][1];
    for (int i = 0; i < rHeaders.size(); i++) {
      rowHeaders[i][0] = rHeaders.get(i);
    }

    IPentahoMetaData metaData = new MemoryMetaData(columnHeaders, rowHeaders);
    MemoryResultSet result = new MemoryResultSet(metaData);
    for (int i = 0; i < data.size(); i++) {
      result.addRow(((List) data.get(i)).toArray());
    }
    return result;
  }
  
  public static Node getChartNode(String xml) {
    try {
      Document chartDocument = XmlDom4JHelper.getDocFromString(xml, new PentahoEntityResolver());
      
      Node chartNode = chartDocument.selectSingleNode("chart");
      if (chartNode == null) {
        chartNode = chartDocument.selectSingleNode("chart-attributes");
      }
      return chartNode;

    } catch (XmlParseException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  @Test
  public void testBarChartWithProps() {
    
    String chart = "<chart>" +
                   "<chart-type>BarChart</chart-type>" + 
                   "<is-sketch>true</is-sketch>" +
                   "<fun-factor>10</fun-factor>" +
                   "<plot-background type=\"color\">#FF0000</plot-background>" +
                   "<chart-background type=\"color\">#FF0000</chart-background>" +
                   "<title>TITLE_STR</title>" +
                   "<title-font>" + 
                   "<font-family>Arial</font-family>" + 
                   "<size>11</size>" + 
                   "<is-bold>true</is-bold>" + 
                   "<is-italic>true</is-italic>" + 
                   "</title-font>" +
                   "<range-minimum>1</range-minimum>" +
                   "<range-maximum>1</range-maximum>" +
                   "<range-color>#0F0000</range-color>" +
                   "<range-grid-color>#00F000</range-grid-color>" +
                   "<range-stroke>123</range-stroke>" +
                   "<range-steps>10</range-steps>" +
                   "<domain-minimum>1</domain-minimum>" +
                   "<domain-maximum>1</domain-maximum>" +
                   "<domain-color>#0F0000</domain-color>" +
                   "<domain-grid-color>#00F000</domain-grid-color>" +
                   "<domain-stroke>123</domain-stroke>" +
                   "<domain-steps>10</domain-steps>" +
                   "<color-palette>" +
                   "<color>#FF0000</color>" +
                   "<color>#00FF00</color>" +
                   "</color-palette>" +
                   "</chart>";
    
    IPentahoResultSet rs = getRelationData();
    Node chartNode = getChartNode(chart);
    
    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    Assert.assertEquals(c2, 
      "{\"bg_colour\":\"#FF0000\",\"inner_bg_colour\":\"#FF0000\",\"title\":{\"text\":\"TITLE_STR\",\"style\":\"font-family: Arial; font-size: 11px; font-weight: bold; font-style: italic;\"},\"x_legend\":{\"text\":\"DEPARTMENT\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"},\"y_axis\":{\"min\":1,\"stroke\":123,\"grid-colour\":\"#00F000\",\"colour\":\"#0F0000\",\"max\":1},\"x_axis\":{\"stroke\":123,\"grid-colour\":\"#00F000\",\"colour\":\"#0F0000\",\"labels\":{\"labels\":[\"Sales\",\"Finance\",\"Human Resource\"]},\"offset\":1},\"elements\":[{\"outline-colour\":\"#006666\",\"text\":\"ACTUAL\",\"type\":\"bar_sketch\",\"values\":[{\"top\":11},{\"top\":14},{\"top\":7}],\"colour\":\"#FF0000\",\"offset\":10},{\"outline-colour\":\"#0066CC\",\"text\":\"BUDGET\",\"type\":\"bar_sketch\",\"values\":[{\"top\":12},{\"top\":9},{\"top\":100}],\"colour\":\"#00FF00\",\"offset\":10}]}");



  }
  
  @Test
  public void testBarChart() {
    
    String chart = "<chart>" +
                   "<chart-type>BarChart</chart-type>" + 
                   "</chart>";
    
    IPentahoResultSet rs = getRelationData();
    Node chartNode = getChartNode(chart);
    
    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    Assert.assertEquals(c2, 
    "{\"x_legend\":{\"text\":\"DEPARTMENT\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"},\"y_axis\":{\"min\":0,\"steps\":11,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"max\":121},\"x_axis\":{\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":{\"labels\":[\"Sales\",\"Finance\",\"Human Resource\"]},\"offset\":1},\"elements\":[{\"text\":\"ACTUAL\",\"type\":\"bar\",\"values\":[{\"top\":11},{\"top\":14},{\"top\":7}],\"colour\":\"#006666\"},{\"text\":\"BUDGET\",\"type\":\"bar\",\"values\":[{\"top\":12},{\"top\":9},{\"top\":100}],\"colour\":\"#0066CC\"}]}");


  }
  
  @Test
  public void testHorizontalBarChart() {
    
    String chart = "<chart>" +
                   "<chart-type>BarChart</chart-type>" + 
                   "<orientation>horizontal</orientation>" + 
                   "</chart>";
    
    IPentahoResultSet rs = getRelationData();
    Node chartNode = getChartNode(chart);

    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    
    Assert.assertEquals(c2, 
      "{\"tooltip\":{\"mouse\":2},\"x_legend\":{\"text\":\"DEPARTMENT\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"}," + 
        "\"y_axis\":{\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":[\"Human Resource\",\"Finance\",\"Sales\"],\"offset\":1}," + 
        "\"x_axis\":{\"min\":0,\"steps\":11,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":{},\"max\":121}," + 
        "\"elements\":[{\"text\":\"ACTUAL\",\"type\":\"hbar\",\"values\":[{\"right\":11},{\"right\":14},{\"right\":7}],\"colour\":\"#006666\"},{\"text\":\"BUDGET\",\"type\":\"hbar\",\"values\":[{\"right\":12},{\"right\":9},{\"right\":100}],\"colour\":\"#0066CC\"}]}");


  }
  
  
  @Test
  public void testLineChart() {
    String chart = "<chart>" +
                   "<chart-type>LineChart</chart-type>" + 
                   "</chart>";
    
    IPentahoResultSet rs = getRelationData();
    Node chartNode = getChartNode(chart);
    
    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    Assert.assertEquals(c2, 
      "{\"x_legend\":{\"text\":\"DEPARTMENT\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"},\"y_axis\":{\"min\":0,\"steps\":10,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"max\":110},\"x_axis\":{\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":{\"labels\":[\"Sales\",\"Finance\",\"Human Resource\"]}},\"elements\":[{\"font-size\":10,\"text\":\"ACTUAL\",\"type\":\"line\",\"values\":[{\"value\":11},{\"value\":14},{\"value\":7}],\"colour\":\"#006666\"},{\"font-size\":10,\"text\":\"BUDGET\",\"type\":\"line\",\"values\":[{\"value\":12},{\"value\":9},{\"value\":100}],\"colour\":\"#0066CC\"}]}");



  }
  
  @Test
  public void testAreaChart() {
    String chart = "<chart>" +
                   "<chart-type>AreaChart</chart-type>" + 
                   "</chart>";
    
    IPentahoResultSet rs = getRelationData();
    Node chartNode = getChartNode(chart);

    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    Assert.assertEquals(c2, 
      "{\"x_legend\":{\"text\":\"DEPARTMENT\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"},\"y_axis\":{\"min\":0,\"steps\":10,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"max\":110},\"x_axis\":{\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":{\"labels\":[\"Sales\",\"Finance\",\"Human Resource\"]}},\"elements\":[{\"font-size\":10,\"text\":\"ACTUAL\",\"fillAlpha\":0.35,\"type\":\"area_line\",\"values\":[11,14,7],\"fill\":\"#006666\",\"colour\":\"#006666\"},{\"font-size\":10,\"text\":\"BUDGET\",\"fillAlpha\":0.35,\"type\":\"area_line\",\"values\":[12,9,100],\"fill\":\"#0066CC\",\"colour\":\"#0066CC\"}]}");
  }
  
  @Test
  public void testPieChart() {
    String chart = "<chart>" +
                   "<chart-type>PieChart</chart-type>" + 
                   "</chart>";
    
    IPentahoResultSet rs = getRelationData();
    Node chartNode = getChartNode(chart);

    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    Assert.assertEquals(c2,
      "{\"x_legend\":{\"text\":\"DEPARTMENT\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"},\"y_axis\":{\"min\":0,\"steps\":10,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"max\":110},\"x_axis\":{\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":{\"labels\":[\"Sales\",\"Finance\",\"Human Resource\"]}},\"elements\":[{\"animate\":false,\"colours\":[\"#006666\",\"#0066CC\",\"#009999\",\"#336699\",\"#339966\",\"#3399FF\",\"#663366\",\"#666666\",\"#666699\",\"#669999\",\"#6699CC\",\"#66CCCC\",\"#993300\",\"#999933\",\"#999966\",\"#999999\",\"#9999CC\",\"#9999FF\",\"#99CC33\",\"#99CCCC\",\"#99CCFF\",\"#CC6600\",\"#CC9933\",\"#CCCC33\",\"#CCCC66\",\"#CCCC99\",\"#CCCCCC\",\"#FF9900\",\"#FFCC00\",\"#FFCC66\"],\"type\":\"pie\",\"values\":[{\"value\":11,\"text\":\"Sales\",\"label\":\"Sales\"},{\"value\":14,\"text\":\"Finance\",\"label\":\"Finance\"},{\"value\":7,\"text\":\"Human Resource\",\"label\":\"Human Resource\"}]}]}");
  }
  
  @Test
  public void testDotChart() {
    String chart = "<chart>" +
                   "<chart-type>DotChart</chart-type>" + 
                   "<dataset-type>XYSeriesCollection</dataset-type>" + 
                   "</chart>";
    
    IPentahoResultSet rs = getRelationData();
    Node chartNode = getChartNode(chart);
    
    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    Assert.assertEquals(c2, 
      "{\"x_legend\":{\"text\":\"DEPARTMENT\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"},\"y_axis\":{\"min\":0,\"steps\":10,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"max\":110},\"x_axis\":{\"min\":7,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":{},\"max\":7},\"elements\":[{\"text\":\"Sales\",\"type\":\"scatter\",\"values\":[{\"y\":12,\"x\":11}],\"tip\":\"Sales: 11, 12\",\"colour\":\"#006666\"},{\"text\":\"Finance\",\"type\":\"scatter\",\"values\":[{\"y\":9,\"x\":14}],\"tip\":\"Finance: 14, 9\",\"colour\":\"#0066CC\"},{\"text\":\"Human Resource\",\"type\":\"scatter\",\"values\":[{\"y\":100,\"x\":7}],\"tip\":\"Human Resource: 7, 100\",\"colour\":\"#009999\"}]}");

    

  }
  
  @Test
  public void testBubbleChart() {
    String chart = "<chart>" +
                   "<chart-type>BubbleChart</chart-type>" + 
                   "<dataset-type>XYZSeriesCollection</dataset-type>" + 
                   "</chart>";
    
    IPentahoResultSet rs = getXYZRelationData();
    Node chartNode = getChartNode(chart);
 
    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    Assert.assertEquals(c2, 
      "{\"x_legend\":{\"text\":\"DEPARTMENT\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"},\"y_axis\":{\"min\":0,\"steps\":10,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"max\":110},\"x_axis\":{\"min\":7,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":{},\"max\":7},\"elements\":[{\"text\":\"Sales\",\"type\":\"scatter\",\"values\":[{\"y\":12,\"x\":11}],\"dot-size\":1,\"colour\":\"#006666\"},{\"text\":\"Finance\",\"type\":\"scatter\",\"values\":[{\"y\":9,\"x\":14}],\"dot-size\":5,\"colour\":\"#0066CC\"},{\"text\":\"Human Resource\",\"type\":\"scatter\",\"values\":[{\"y\":100,\"x\":7}],\"dot-size\":100,\"colour\":\"#009999\"}]}");

  

  }
  
  @Test
  public void testBubbleChartDimensional() {
    String chart = "<chart>" +
                   "<chart-type>BubbleChart</chart-type>" + 
                   "<dataset-type>XYZSeriesCollection</dataset-type>" + 
                   "</chart>";
    
    IPentahoResultSet rs = getDimensionalData();
    Node chartNode = getChartNode(chart);

    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    Assert.assertEquals(c2, 
      "{\"x_legend\":{\"text\":\"ACTUAL\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"},\"y_axis\":{\"min\":0,\"steps\":10,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"max\":110},\"x_axis\":{\"min\":7,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":{},\"max\":7},\"elements\":[{\"text\":\"Sales\",\"type\":\"scatter\",\"values\":[{\"y\":12,\"x\":11}],\"dot-size\":1,\"colour\":\"#006666\"},{\"text\":\"Finance\",\"type\":\"scatter\",\"values\":[{\"y\":9,\"x\":14}],\"dot-size\":5,\"colour\":\"#0066CC\"},{\"text\":\"Human Resource\",\"type\":\"scatter\",\"values\":[{\"y\":100,\"x\":7}],\"dot-size\":100,\"colour\":\"#009999\"}]}");
  }

  
  @Test
  public void testBarLineChart() {
    
    String chart = "<chart>" +
                   "<chart-type>BarLineChart</chart-type>" +
                   "<bar-series>" + 
                     "<series>ACTUAL</series>" + 
                     "<series>DIFFERENCE</series>" +
                   "</bar-series>" +
                   "<line-series><series>BUDGET</series></line-series>" + 
                   "</chart>";
    
    IPentahoResultSet rs = getXYZRelationData();
    Node chartNode = getChartNode(chart);
    
    
    String c2 = PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
    // verify there is a right axis, and that there is a y_axis_right
    Assert.assertTrue(c2.indexOf("\"axis\":\"right\"") >= 0);
    Assert.assertTrue(c2.indexOf("\"y_axis_right\":{") >= 0);
    
    Assert.assertEquals(c2, 
        "{\"y_axis_right\":{\"min\":0,\"steps\":10,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"max\":110},\"x_legend\":{\"text\":\"DEPARTMENT\",\"style\":\"font-family: Arial; font-size: 14px; font-weight: normal; font-style: normal;\"},\"y_axis\":{\"min\":0,\"steps\":10,\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"max\":110},\"x_axis\":{\"stroke\":1,\"grid-colour\":\"#aaaaaa\",\"colour\":\"#000000\",\"labels\":{\"labels\":[\"Sales\",\"Finance\",\"Human Resource\"]},\"offset\":1},\"elements\":[{\"text\":\"ACTUAL\",\"type\":\"bar\",\"values\":[{\"top\":11},{\"top\":14},{\"top\":7}],\"colour\":\"#006666\"},{\"font-size\":10,\"text\":\"BUDGET\",\"axis\":\"right\",\"type\":\"line\",\"values\":[{\"value\":12},{\"value\":9},{\"value\":100}],\"colour\":\"#0066CC\"},{\"text\":\"DIFFERENCE\",\"type\":\"bar\",\"values\":[{\"top\":1},{\"top\":5},{\"top\":93}],\"colour\":\"#009999\"}]}");
    
  }
  
  
  @Test
  public void testBogusData() {
    String chart = "<chart>" +
    "<chart-type>BarChart</chart-type>" + 
    "</chart>";

    IPentahoResultSet rs = getRelationBogusData();
    Node chartNode = getChartNode(chart);

    try {
      PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
      Assert.fail();
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getMessage().toLowerCase().indexOf("result set") >= 0);
    }
  }
  
  
  @Test
  public void testSmallDataFail() {
    String chart = "<chart>" +
    "<chart-type>BubbleChart</chart-type>" + 
    "<dataset-type>XYZSeriesCollection</dataset-type>" + 
    "</chart>";

    IPentahoResultSet rs = getRelationData();
    Node chartNode = getChartNode(chart);

    try {
      PentahoOFC4JChartHelper.generateChartJson(chartNode, rs, false, null);
      Assert.fail();
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getMessage().toLowerCase().indexOf("xyz") >= 0);
    }
  }
  
}
