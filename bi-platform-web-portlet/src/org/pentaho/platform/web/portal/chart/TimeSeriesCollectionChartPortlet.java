/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
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
