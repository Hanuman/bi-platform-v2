package org.pentaho.mantle.client.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportContainer implements Serializable {

  private int numPages = -1;
  private boolean promptNeeded = false;
  private HashMap<Integer, String> reportPages = new HashMap<Integer, String>();
  private List<ReportParameter> reportParameters = new ArrayList<ReportParameter>();
  private String reportPath = null;
  
  public int getNumPages() {
    return numPages;
  }

  public void setNumPages(int numPages) {
    this.numPages = numPages;
  }

  public HashMap<Integer, String> getReportPages() {
    return reportPages;
  }

  public void setReportPages(HashMap<Integer, String> reportPages) {
    this.reportPages = reportPages;
  }

  public boolean isPromptNeeded() {
    return promptNeeded;
  }

  public void setPromptNeeded(boolean promptNeeded) {
    this.promptNeeded = promptNeeded;
  }

  public List<ReportParameter> getReportParameters() {
    return reportParameters;
  }

  public void setReportParameters(List<ReportParameter> reportParameters) {
    this.reportParameters = reportParameters;
  }

  public String getReportPath() {
    return reportPath;
  }

  public void setReportPath(String reportPath) {
    this.reportPath = reportPath;
  }

}
