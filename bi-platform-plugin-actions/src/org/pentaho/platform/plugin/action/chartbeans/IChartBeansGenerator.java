/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License, version 2.1 as published by the Free Software Foundation. You should have received a copy of the GNU
 * Lesser General Public License along with this program; if not, you can obtain a copy at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html or from the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. Copyright 2008 - 2009 Pentaho Corporation. All rights
 * reserved.
 * 
 * Copyright 2009 - Pentaho Corporation
 * 
 * Written by mbatchelor
 */

package org.pentaho.platform.plugin.action.chartbeans;

import java.io.IOException;
import java.util.Map;

import org.pentaho.platform.api.engine.IPentahoSession;

public interface IChartBeansGenerator {

  /**
   * Convenience method that returns a complete HTML document containing the chart. Resource references point back to
   * the BI Server.
   */
  public String createChartAsHtml(
       IPentahoSession userSession, Map<String, String> parameterMap, 
       String serializedChartDataDefinition, String serializedChartModel, 
       int chartWidth, int chartHeight) throws IOException;

  public String mergeStaticImageHtmlTemplate(String imageUrl);
  
  public String mergeOpenFlashChartHtmlTemplate(String openFlashChartJson, String swfUrl);
  
  public String buildEmptyOpenFlashChartHtmlFragment(String msg);

  public String buildOpenFlashChartHtmlFragment(String openFlashChartJson, String swfUrl, String chartWidth, String chartHeight);
  
  public String getHtmlTemplate();
  
  public String getFlashScriptFragment();
  
  public String getFlashObjectFragment();

  
 }
