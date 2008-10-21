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
package org.pentaho.mantle.client.perspective.solutionbrowser.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pentaho.mantle.client.objects.ReportContainer;

public class ReportCache {

  private static final int MAX_REPORT_CACHE_SIZE = 20;
  private static final int CACHE_FLUSH_CHUNK_SIZE = 10;

  private static HashMap<String, HashMap<Integer, String>> reportMap = new HashMap<String, HashMap<Integer, String>>();
  private static HashMap<String, Integer> reportPageNumberMap = new HashMap<String, Integer>();
  private static HashMap<String, List<Integer>> cachedAgeMap = new HashMap<String, List<Integer>>();

  private static void removeExcessCacheItems(String reportKey) {
    List<Integer> pageNumbers = cachedAgeMap.get(reportKey);
    if (pageNumbers == null) {
      return;
    }
    List<Integer> pagesToRemove = new ArrayList<Integer>();
    if (pageNumbers.size() > MAX_REPORT_CACHE_SIZE) {
      for (int i = 0; i < CACHE_FLUSH_CHUNK_SIZE && i < pageNumbers.size(); i++) {
        Integer pageNumber = pageNumbers.get(i);
        pagesToRemove.add(pageNumber);
      }
    }
    for (Integer pageNumber : pagesToRemove) {
      reportMap.get(reportKey).remove(pageNumber);
      pageNumbers.remove(pageNumber);
    }
  }

  public static void setNumberOfReportPages(String reportKey, int numberOfPages) {
    reportPageNumberMap.put(reportKey, numberOfPages);
  }

  public static int getNumberOfReportPages(String reportKey) {
    Integer numberOfReportPages = reportPageNumberMap.get(reportKey);
    if (numberOfReportPages == null) {
      return 0;
    }
    return numberOfReportPages;
  }

  public static void addReportPages(String reportKey, ReportContainer reportContainer) {
    for (Integer pageNumber : reportContainer.getReportPages().keySet()) {
      addReportPage(reportKey, pageNumber, reportContainer.getReportPages().get(pageNumber));
    }
  }

  public static void removeReportPage(String reportKey, Integer pageNumber) {
    HashMap<Integer, String> reportPageMap = (HashMap<Integer, String>) reportMap.get(reportKey);
    if (reportPageMap != null) {
      reportPageMap.remove(pageNumber);
    }
  }

  public static void addReportPage(String reportKey, Integer pageNumber, String reportHTML) {
    // make room in cache if needed
    removeExcessCacheItems(reportKey);
    HashMap<Integer, String> reportPageMap = (HashMap<Integer, String>) reportMap.get(reportKey);
    if (reportPageMap == null) {
      reportPageMap = new HashMap<Integer, String>();
      reportMap.put(reportKey, reportPageMap);
    }
    reportPageMap.put(pageNumber, reportHTML);

    List<Integer> pageNumberList = cachedAgeMap.get(reportKey);
    if (pageNumberList == null) {
      pageNumberList = new ArrayList<Integer>();
      pageNumberList.add(pageNumber);
      cachedAgeMap.put(reportKey, pageNumberList);
    }
    if (!pageNumberList.contains(pageNumber)) {
      pageNumberList.add(pageNumber);
    }
  }

  public static String getReportPage(String reportKey, Integer pageNumber) {
    HashMap<Integer, String> reportPageMap = (HashMap<Integer, String>) reportMap.get(reportKey);
    if (reportPageMap != null) {
      return reportPageMap.get(pageNumber);
    }
    return null;
  }

  public static void removeReport(String reportKey) {
    reportMap.remove(reportKey);
  }

  public static void resetCache() {
    reportMap.clear();
  }

}
