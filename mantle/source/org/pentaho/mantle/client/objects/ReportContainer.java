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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
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
