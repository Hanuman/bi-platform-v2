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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Nov 17, 2005 
 * @author wseyler
 */

package org.pentaho.platform.uifoundation.chart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.jfree.data.general.Dataset;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

public abstract class AbstractJFreeChartComponent extends AbstractChartComponent {

  private static final long serialVersionUID = 1244685089926020547L;
  private static final int DEFAULT_HEIGHT = 125;
  private static final int DEFAULT_WIDTH = 125;
  
  protected int chartType;

  protected Dataset dataDefinition;

  protected AbstractJFreeChartComponent(final int chartType, final String definitionPath, final int width,
      final int height, final IPentahoUrlFactory urlFactory, final List messages) {
    this(urlFactory, messages);
    this.chartType = chartType;
    this.definitionPath = definitionPath;
    this.width = width;
    this.height = height;
    ActionInfo info = ActionInfo.parseActionString(definitionPath);
    if (info != null) {
      setSourcePath(info.getSolutionName() + File.separator + info.getPath());
    }
  }

  /**
   * @param definitionPath
   * @param urlFactory
   * @param messages
   */
  protected AbstractJFreeChartComponent(final String definitionPath, final IPentahoUrlFactory urlFactory,
      final ArrayList messages) {
    this(urlFactory, messages);
    this.definitionPath = definitionPath;
    ActionInfo info = ActionInfo.parseActionString(definitionPath);
    if (info != null) {
      setSourcePath(info.getSolutionName() + File.separator + info.getPath());
    }
  }

  protected AbstractJFreeChartComponent(final IPentahoUrlFactory urlFactory, final List messages) {
    super(urlFactory, messages);
    AbstractChartComponent.logger = LogFactory.getLog(this.getClass());
  }

  /**
   * Creates a Dataset object (actaully one of it's subclasses from the XML
   * doc
   * 
   * @param doc
   *            XML document that describes the chart
   * @return the Dataset Implementation
   */
  public abstract Dataset createChart(Document doc);

  /**
   * @return Returns the dataSet.
   */
  public Dataset getDataDefinitiont() {
    return dataDefinition;
  }

  /**
   * @param dataSet
   *            The dataSet to set.
   */
  public void setDataDefinition(final Dataset dataSet) {
    this.dataDefinition = dataSet;
  }

  /**
   * @return Returns the chartType.
   */
  public int getChartType() {
    return chartType;
  }

  /**
   * @param chartType
   *            The chartType to set.
   */
  public void setChartType(final int chartType) {
    this.chartType = chartType;
  }

  @Override
  public boolean setDataAction(final String chartDefinition) {
    IActionSequenceResource resource = new ActionSequenceResource(
        "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$ //$NON-NLS-2$
        chartDefinition);
    try {
      Document dataActionDocument = AbstractJFreeChartComponent.getResourceAsDocument(getSession(), resource);
      if (dataActionDocument == null) {
        return false;
      }

      Node dataNode = dataActionDocument.selectSingleNode("chart/data"); //$NON-NLS-1$

      if (dataNode == null) {
        // No data here
        return false;
      }
      chartType = (int) XmlDom4JHelper.getNodeText("chart-type", dataNode, -1); //$NON-NLS-1$
      solution = XmlDom4JHelper.getNodeText("data-solution", dataNode); //$NON-NLS-1$
      actionPath = XmlDom4JHelper.getNodeText("data-path", dataNode); //$NON-NLS-1$
      actionName = XmlDom4JHelper.getNodeText("data-action", dataNode); //$NON-NLS-1$
      actionOutput = XmlDom4JHelper.getNodeText("data-output", dataNode); //$NON-NLS-1$
      byRow = XmlDom4JHelper.getNodeText("data-orientation", dataNode, "rows").equals("rows"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if (width == 0) {
        width = (int) XmlDom4JHelper.getNodeText("chart/width", dataActionDocument, DEFAULT_WIDTH); //$NON-NLS-1$
      }
      if (height == 0) {
        height = (int) XmlDom4JHelper.getNodeText("chart/height", dataActionDocument, DEFAULT_HEIGHT); //$NON-NLS-1$
      }
    } catch (Exception e) {
      error(Messages.getString("CategoryDatasetChartComponent.ERROR_0001_INVALID_CHART_DEFINITION", chartDefinition), e); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  // HACK for BISERVER-171:
  // This is a temporary fix for SolutionRepository.getResourceAsDocument() returning the wrong solution file - 
  // chart definition files should not get a "true" parameter for check for localized file. We need to move 
  // this code into the Solution Repository after 1.6 RC1. 

  public static Document getResourceAsDocument(final IPentahoSession userSession,
      final IActionSequenceResource actionResource) throws IOException  {
    // TODO support locales here
    byte[] xmlBytes = PentahoSystem.get(ISolutionRepository.class, userSession).getResourceAsBytes(actionResource, false, ISolutionRepository.ACTION_EXECUTE);
    if (xmlBytes == null) {
      return null;
    }
    try {
      return XmlDom4JHelper.getDocFromString(new String(xmlBytes), new SolutionURIResolver(userSession));
    } catch(XmlParseException xpe) {
      return null;
    } 
  }
}
