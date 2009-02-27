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
  protected String queryString = null;
  protected String serializedChartModel = null;
  protected ChartModel chartModel = null;
  protected int chartWidth = 400;
  protected int chartHeight = 400;
  protected IPentahoSession userSession = null;
  
  protected String lastError = null;
  
  public ChartBeansGeneratorUtil(){
  }
  
  public InputStream getChart() throws ChartGenerationException{
    if(!validate()){
      if(lastError == null){
        lastError = "An unexpected error has occurred";
      }
      throw new ChartGenerationException(lastError);
    }

    InputStream result = null;
    
    try{
       result = ChartBeansGeneratorUtil.createChart(queryString, chartModel, chartWidth, chartHeight, userSession);
    } catch (Exception e){
      throw new ChartGenerationException(e);
    }
    return(result);
  }
  
  public boolean validate(){
    boolean result = true;

    // Must have a data query
    if((queryString == null) || (queryString.equals(""))){ //$NON-NLS-1$
      lastError = "Query string must not be null";
      // chartGenException = new ChartGenerationException("Query string must not be null");
      return false;
    }
    
    // Must have a chart model
    if((serializedChartModel == null) || (serializedChartModel.equals(""))){ //$NON-NLS-1$
      if(chartModel == null){
        lastError = "No valid chart model found";
        // chartGenException = new ChartGenerationException("No valid chart model found");
        return false;
      } else {
        serializedChartModel = ChartSerializer.serialize(chartModel);
      }
    }
    
    if(userSession == null){
      lastError = "No valid user session found";
      // chartGenException = new ChartGenerationException("No valid user session found");
      return false;
    }
    
    return result;
  }
  
  public static void createChart(String mqlQueryString, ChartModel chartModel, int chartWidth, int chartHeight, IPentahoSession userSession, OutputStream out) throws IOException{
    String serializedChartModel = ChartSerializer.serialize(chartModel);
    
    ChartBeansGeneratorUtil.internalCreateChart(mqlQueryString, serializedChartModel, chartWidth, chartHeight, userSession, out);
  }

  public static void createChart(String mqlQueryString, String serializedChartModel, int chartWidth, int chartHeight, IPentahoSession userSession, OutputStream out) throws IOException{
    ChartBeansGeneratorUtil.internalCreateChart(mqlQueryString, serializedChartModel, chartWidth, chartHeight, userSession, out);
  }
  
  public static InputStream createChart(String mqlQueryString, ChartModel chartModel, int chartWidth, int chartHeight, IPentahoSession userSession) throws IOException{
    String serializedChartModel = ChartSerializer.serialize(chartModel);
    
    return ChartBeansGeneratorUtil.internalCreateChart(mqlQueryString, serializedChartModel, chartWidth, chartHeight, userSession, null);
  }
  
  public static InputStream createChart(String mqlQueryString, String serializedChartModel, int chartWidth, int chartHeight, IPentahoSession userSession) throws IOException{
    return ChartBeansGeneratorUtil.internalCreateChart(mqlQueryString, serializedChartModel, chartWidth, chartHeight, userSession, null);
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
  protected static InputStream internalCreateChart(String mqlQueryString, String serializedChartModel, int chartWidth, int chartHeight, IPentahoSession userSession, OutputStream outputStream) throws IOException{
    InputStream result = null;
    ByteArrayOutputStream resultOutputStream = null;
    OutputStream out = null;
    
    // Make code more readable by defining the output result
    boolean returnInputStream = outputStream == null ? true : false;
    
    // If the caller sends a null OutputStream, then we will return an InputStream
    if(returnInputStream){
      resultOutputStream = new ByteArrayOutputStream();
      out = new BufferedOutputStream(resultOutputStream);
    } else {
      out = outputStream;
    }

    // Setup parameters to be passed to the xaction
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("query", mqlQueryString); //$NON-NLS-1$
    params.put("chart-model", serializedChartModel); //$NON-NLS-1$
    params.put("chart-width", chartWidth); //$NON-NLS-1$
    params.put("chart-height", chartHeight); //$NON-NLS-1$

    SolutionHelper.execute("XAction", userSession, "system/chartbeans/chartbeans_mql.xaction", params, out, true); //$NON-NLS-1$ //$NON-NLS-2$
    
    if(out instanceof BufferedOutputStream){
      out.flush();
    }

    if(returnInputStream){
      result = new ByteArrayInputStream(resultOutputStream.toByteArray());
     
      return(result);
    }
    
    return null;
  }
  
  public String getQueryString() {
    return queryString;
  }

  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  public String getSerializedChartModel() {
    return serializedChartModel;
  }

  public void setSerializedChartModel(String serializedChartModel) {
    this.serializedChartModel = serializedChartModel;
  }

  public ChartModel getChartModel() {
    return chartModel;
  }

  public void setChartModel(ChartModel chartModel) {
    this.chartModel = chartModel;
  }
  
  public int getChartWidth() {
    return chartWidth;
  }

  public void setChartWidth(int chartWidth) {
    this.chartWidth = chartWidth;
  }

  public int getChartHeight() {
    return chartHeight;
  }

  public void setChartHeight(int chartHeight) {
    this.chartHeight = chartHeight;
  }

  public IPentahoSession getUserSession() {
    return userSession;
  }

  public void setUserSession(IPentahoSession userSession) {
    this.userSession = userSession;
  }
  
  public String getLastError() {
    return lastError;
  }
}
