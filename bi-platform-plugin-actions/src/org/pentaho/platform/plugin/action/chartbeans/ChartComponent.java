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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.chart.ChartBoot;
import org.pentaho.chart.ChartFactory;
import org.pentaho.chart.AbstractChartThemeFactory;
import org.pentaho.chart.InvalidChartDefinition;
import org.pentaho.chart.model.ChartModel;
import org.pentaho.chart.model.Theme;
import org.pentaho.chart.model.DialPlot;
import org.pentaho.chart.model.PiePlot;
import org.pentaho.chart.model.util.ChartSerializer;
import org.pentaho.chart.model.util.ChartSerializer.ChartSerializationFormat;
import org.pentaho.chart.plugin.ChartPluginFactory;
import org.pentaho.chart.plugin.ChartProcessingException;
import org.pentaho.chart.plugin.IChartPlugin;
import org.pentaho.chart.plugin.api.PersistenceException;
import org.pentaho.chart.plugin.api.IOutput.OutputTypes;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;

/**
 * This is a bean that permits easy access to the ChartBeans functionality and was
 * specifically designed to be run from within the Pentaho Platform as an Action Sequence 
 * Component.
 * 
 * @author cboyden
 *
 */
public class ChartComponent {
  protected static final String DEFAULT_CHART_PLUGIN = "org.pentaho.chart.plugin.jfreechart.JFreeChartPlugin"; //$NON-NLS-1$
  
  protected static final int DEFAULT_CHART_WIDTH = 400;
  protected static final int DEFAULT_CHART_HEIGHT = 300;
  
  protected String seriesColumnName = null;
  protected int seriesColumn = -1;
  
  protected String categoryColumnName = null;
  protected int categoryColumn = -1;
  
  protected String valueColumnName = null;
  protected int valueColumn = -1;
  
  protected IPentahoResultSet resultSet = null;
  
  protected String chartPlugin = null;
  
  protected Exception bootException = null;
  
  protected String outputType = "text/html"; //$NON-NLS-1$
  
  protected int chartWidth = -1;

  protected int chartHeight = -1;
  
  protected OutputStream outputStream = null;
  
  protected String chartModelJson = null;
  
  protected String chartModelXml = null;
  
  protected ChartModel chartModel = null;

  private static final String DEFAULT_FLASH_LOC = "openflashchart"; //$NON-NLS-1$
  
  private static final String DEFAULT_FLASH_SWF = "open-flash-chart-full-embedded-font.swf"; //$NON-NLS-1$s

  /**
   * Initialize ChartBeans engine
   */
  {
    synchronized (ChartBoot.getInstance()) {
      while (!ChartBoot.getInstance().isBootDone()) {
        if (ChartBoot.getInstance().isBootInProgress()) {
          // Wait 1 second
          try {
            java.lang.Thread.sleep(1000);
          } catch (InterruptedException e) {
            // Do nothing
          }
        } else {
          if (!ChartBoot.getInstance().isBootFailed()) {
            ChartBoot.getInstance().start();
          }
        }
      }// End while: boot is not done

      //Check for an error
      if (ChartBoot.getInstance().isBootFailed()) {
        bootException = ChartBoot.getInstance().getBootFailureReason();
      }
    }// End thread synchronization
  }
  
  /**
   * Called to process the chart definition and data set to produce
   * a usable chart.
   * 
   * @return state of execution. 'true' if execution was successful, otherwise false.
   * @throws ChartBootException
   * @throws ChartProcessingException
   * @throws ResourceException
   * @throws InvalidChartDefinition
   * @throws IOException
   * @throws PersistenceException
   */
  public boolean execute() throws ChartBootException, ChartProcessingException, ResourceException,
    InvalidChartDefinition, IOException, PersistenceException {
    if (bootException != null) {
      throw new ChartBootException(bootException);
    }
    
    // Transform IPentahoResultSet to an object array
    
    Object[][] data = processChartData(resultSet);
    
    try{
      
      if(chartModel.getTheme() != null){
        AbstractChartThemeFactory chartThemeFactory = new AbstractChartThemeFactory() {
          protected List<File> getThemeFiles() {
            ArrayList<File> themeFiles = new ArrayList<File>();
            themeFiles.add(new File(PentahoSystem.getApplicationContext().getSolutionPath("system/dashboards/resources/gwt/Theme1.xml"))); //$NON-NLS-1$
            themeFiles.add(new File(PentahoSystem.getApplicationContext().getSolutionPath("system/dashboards/resources/gwt/Theme2.xml"))); //$NON-NLS-1$
            themeFiles.add(new File(PentahoSystem.getApplicationContext().getSolutionPath("system/dashboards/resources/gwt/Theme3.xml"))); //$NON-NLS-1$
            themeFiles.add(new File(PentahoSystem.getApplicationContext().getSolutionPath("system/dashboards/resources/gwt/Theme4.xml"))); //$NON-NLS-1$
            themeFiles.add(new File(PentahoSystem.getApplicationContext().getSolutionPath("system/dashboards/resources/gwt/Theme5.xml"))); //$NON-NLS-1$
            themeFiles.add(new File(PentahoSystem.getApplicationContext().getSolutionPath("system/dashboards/resources/gwt/Theme6.xml"))); //$NON-NLS-1$
            return themeFiles;
              }
        };
        
        
        Theme chartTheme = chartThemeFactory.getTheme(chartModel.getTheme());
        if (chartTheme != null) {
          chartTheme.applyTo(chartModel);
            }
      }
      
      InputStream is = ChartFactory.createChart(data, valueColumn, seriesColumn, categoryColumn, chartModel, chartWidth, chartHeight, getOutputType());
      
      // Wrap output as necessary
      if(chartModel.getChartEngine() == ChartModel.CHART_ENGINE_OPENFLASH){
        // Convert stream to string, insert into HTML fragment and re-stream it
        StringBuilder sb = new StringBuilder();
        int c = 0;
        
        // Build string
        while((c = is.read()) >= 0){
          sb.append((char)c);
        }
        
        String flashContent = ChartBeansGeneratorUtil.mergeOpenFlashChartHtmlTemplate(sb.toString().replaceAll("\"", "\\\\\""), DEFAULT_FLASH_LOC + "/" + DEFAULT_FLASH_SWF);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        
        is = new ByteArrayInputStream(flashContent.getBytes("utf-8")); //$NON-NLS-1$
      }

      int val = 0;
      
      //TODO: Buffer for more efficiency
      while((val = is.read()) != -1){
        outputStream.write(val);
      }
    } catch(SQLException e){
      //No SQLException possible from this usage
    }
    
    return true;
  }
  
  /**
   * Transform the IPentahoResultSet into the data format suitable
   * for chart creation.
   * 
   * @return Row / Column data table or null
   */
  protected Object[][] processChartData(IPentahoResultSet resultSet){
    if(resultSet == null){
      return null;
    }
    
    Object[][] result = null;
    
    result = new Object[resultSet.getRowCount()][resultSet.getMetaData().getColumnCount()];
    
    for(int r = 0; r < resultSet.getRowCount(); r++){
      for(int c = 0; c < resultSet.getMetaData().getColumnCount(); c++){
        result[r][c] = resultSet.getValueAt(r, c);
      }
    }
    
    return(result);
  }
  
  /**
   * Define the OutputStream to which the resulting chart shall be written
   * @param outStream Stream receive the chart
   */
  public void setOutputStream(OutputStream outStream) {
    outputStream = outStream;
  }
  
  /**
   * Define the data set that will populate the chart
   * @param chartDataSet data set for charting
   */
  public void setChartData(IPentahoResultSet chartDataSet){
    resultSet = chartDataSet.memoryCopy();
  }
  
  /**
   * Validate the current settings of the ChartComponent. If validate() returns true,
   * then execute may be called. If validate() returns false, a call to execute() is guaranteed
   * to fail.
   * @return state of validation
   * @throws Exception
   */
  public boolean validate() throws Exception{
  //Must have a valid result set
    if(resultSet == null){
      return false;
    }
    
    //Default to the first three columns if no others are explicitly specified
    //Resolve column name to column ordinal if present
    if(seriesColumnName != null){
      //Leave it at -1 if it is specified as blank (The charting engine will handle this properly)
      if(!seriesColumnName.equals("")){ //$NON-NLS-1$
        seriesColumn = resultSet.getMetaData().getColumnIndex(seriesColumnName);
      }
    } else { 
      //Set default ordering as no ordinal has been defined
      if(seriesColumn < 0){
        seriesColumn = 0;
      }
    }
    
    if(categoryColumnName != null){
      //Leave it at -1 if it is specified as blank (The charting engine will handle this properly)
      if(!categoryColumnName.equals("")){ //$NON-NLS-1$
        categoryColumn = resultSet.getMetaData().getColumnIndex(categoryColumnName);
      }
    } else {
    //Set default ordering as no ordinal has been defined
      if(categoryColumn < 0){
        categoryColumn = 1;
      }
    }
    if(valueColumnName != null){
      //Leave it at -1 if it is specified as blank (The charting engine will handle this properly)
      if(!valueColumnName.equals("")){ //$NON-NLS-1$
        valueColumn = resultSet.getMetaData().getColumnIndex(valueColumnName);
      }
    } else {
    //Set default ordering as no ordinal has been defined
      if(valueColumn < 0){
        valueColumn = 2;
      }
    }
    
    loadChartModel();
    
    if(chartModel == null){
      return false;
    }
    
    //Verify that all columns required for a given chart type are present
    if(chartModel.getPlot() instanceof DialPlot){
      if(valueColumn < 0){
        return false;
      }
    } else if(chartModel.getPlot() instanceof PiePlot){
      if((seriesColumn < 0) || (valueColumn < 0)){
        return false;
      }
    } else {
      if((seriesColumn < 0) || (categoryColumn < 0) || (valueColumn < 0)){
        return false;
      }
    }
    
    if(chartWidth <= 0){
      chartWidth = DEFAULT_CHART_WIDTH;
    }
    
    if(chartHeight <= 0){
      chartHeight = DEFAULT_CHART_HEIGHT;
    }

    return true;
  }
  
  /**
   * Define the column in the data set that contains the Series/Domain data 
   * @param seriesCol name of column that contains the Series/Domain for the chart
   */
  public void setSeriesColumn(String seriesCol){
    seriesColumnName = seriesCol;
  }

  /**
   * Define the column in the data set that contains the Category data 
   * @param seriesCol name of column that contains the Category for the chart
   */
  public void setCategoryColumn(String categoryCol) {
    categoryColumnName = categoryCol;
  }

  /**
   * Define the column in the data set that contains the Value/Range data 
   * @param seriesCol name of column that contains the Value/Range for the chart
   */
  public void setValueColumn(String valueCol) {
    valueColumnName = valueCol;
  }
  
  /**
   * Fetch an instance of the desired chart plugin
   * @return instance of chart plugin
   * @throws ChartProcessingException
   */
  protected IChartPlugin getChartPlugin() throws ChartProcessingException {
    if (chartPlugin == null) {
      chartPlugin = DEFAULT_CHART_PLUGIN;
    }

    return ChartPluginFactory.getInstance(chartPlugin);
  }
  
  /**
   * Fetch the desired output type
   * @return output type
   */
  protected OutputTypes getOutputType(){
    if(outputType.equals("image/jpg")){ //$NON-NLS-1$
      return OutputTypes.FILE_TYPE_JPEG;
    } else if (outputType.equals("image/png")){ //$NON-NLS-1$
      return OutputTypes.FILE_TYPE_PNG;
    }
    
    return null;    
  }
  
  /**
   * Fetch the desired MimeType
   * @return mime type
   */
  public String getMimeType(){
    loadChartModel();
    
    if(chartModel != null){
      switch(chartModel.getChartEngine()){
        case ChartModel.CHART_ENGINE_JFREE: {
          outputType = "image/png"; //$NON-NLS-1$
        }break;
        case ChartModel.CHART_ENGINE_OPENFLASH: {
          outputType = "text/html"; //$NON-NLS-1$
        }break;
      }
    }

    return outputType;
  }
  
  protected void loadChartModel(){
    if(chartModel == null){
      if(chartModelJson != null){
        chartModel = ChartSerializer.deSerialize(chartModelJson, ChartSerializationFormat.JSON);
      } else {
        if(chartModelXml != null){
          chartModel = ChartSerializer.deSerialize(chartModelXml, ChartSerializationFormat.XML);
        }
      }
    }
  }
  
  /**
   * Set the JSON representation of the ChartModel
   * @param chartModelJson JSON serialized representation of the ChartModel
   */
  public void setChartModelJson(String chartModelJson) {
    this.chartModelJson = chartModelJson;
  }
  
  /**
   * Set the XML representation of the ChartModel
   * @param chartStyleXml XML serialized representation of the ChartModel
   */
  public void setChartModelXml(String chartModelXml){
    this.chartModelXml = chartModelXml;
  }
  
  /**
   * Set the ChartModel
   * @param chartModel model of the chart to be generated
   */
  public void setChartModel(ChartModel chartModel){
    this.chartModel = chartModel;
  }
  
  /**
   * Set the width of the chart in units specific to the ChartPlugin
   * @param chartWidth width of the chart
   */
  public void setChartWidth(int chartWidth){
    this.chartWidth = chartWidth;
  }
  
  /**
   * Set the height of the chart in units specific to the ChartPlugin
   * @param chartHeight height of the chart
   */
  public void setChartHeight(int chartHeight){
    this.chartHeight = chartHeight;
  }
  
  /**
   * Set the width of the chart in units specific to the ChartPlugin
   * @param chartWidth width of the chart
   */
  public void setChartWidth(String chartWidth){
    this.chartWidth = Integer.valueOf(chartWidth);
  }
  
  /**
   * Set the height of the chart in units specific to the ChartPlugin
   * @param chartHeight height of the chart
   */
  public void setChartHeight(String chartHeight){
    this.chartHeight = Integer.valueOf(chartHeight);
  }
}