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
 */
package org.pentaho.platform.plugin.action.openflashchart.factory;

import jofc2.model.elements.AreaHollowChart;
import jofc2.model.elements.AreaLineChart;
import jofc2.model.elements.LineChart;
import jofc2.model.elements.LineChart.Style.Type;

public class AreaChartFactory extends LineChartFactory {

  @Override
  public LineChart getLineChartFromColumn(int col) {
    LineChart ac = null;
    if(linechartstyle.getType() != Type.HALLOW_DOT.getType()) {
      AreaLineChart ahc = new AreaLineChart();
      ahc.setFillColor(getColor(col));
      ac = ahc;
    } else {
      AreaHollowChart ahc = new AreaHollowChart();
      ahc.setFillColor(getColor(col));
      ac = ahc;
    }

    Number[] numbers = new Number[getRowCount()];
    for (int row = 0; row < getRowCount(); row++) {
      numbers[row] = ((Number) getValueAt(row, col)).doubleValue();
    }

    ac.addValues(numbers);
    ac.setColour(getColor(col));
    
    if (linechartwidth != null) {
      ac.setWidth(linechartwidth);
    }
    if (tooltipText != null) {
      ac.setTooltip(tooltipText);
    }
    
    // set the title for this series
    ac.setText(getColumnHeader(col));

    // set the onclick event to the base url template
    if (null != baseURLTemplate) {
      ac.setOnClick(baseURLTemplate);
    }
    if (alpha != null) {
      ac.setAlpha(alpha);
    }
    
    return ac;
  }
}
