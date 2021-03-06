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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Jun 27, 2006
 * @author Ron Troyer
 */

package org.pentaho.platform.web.portal.chart;

import java.util.ArrayList;

import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.uifoundation.chart.AbstractJFreeChartComponent;
import org.pentaho.platform.uifoundation.chart.TimeSeriesCollectionChartComponent;

public class TimeSeriesCollectionChartPortlet extends AbstractDatasetChartPortlet {

  public TimeSeriesCollectionChartPortlet() {
    super();
  }

  @Override
  protected AbstractJFreeChartComponent getNewChartComponent(final String definitionPath, final IPentahoUrlFactory urlFactory,
      final ArrayList messages) {

    return new TimeSeriesCollectionChartComponent(definitionPath, urlFactory, messages);

  }
}
