package org.pentaho.platform.plugin.action.chartbeans;

public class ChartGenerationException extends Exception {
  
  private static final long serialVersionUID = 4840561957831529L;

  public ChartGenerationException(String s){
    super(s);
  }

  public ChartGenerationException(Throwable t){
    super(t);
  }
}