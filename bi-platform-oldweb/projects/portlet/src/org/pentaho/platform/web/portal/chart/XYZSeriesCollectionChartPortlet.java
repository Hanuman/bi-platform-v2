package org.pentaho.platform.web.portal.chart;

import java.util.ArrayList;

import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.uifoundation.chart.AbstractJFreeChartComponent;
import org.pentaho.platform.uifoundation.chart.XYZSeriesCollectionChartComponent;

public class XYZSeriesCollectionChartPortlet extends AbstractDatasetChartPortlet {

  public XYZSeriesCollectionChartPortlet() {
    super();
  }

  @Override
  protected AbstractJFreeChartComponent getNewChartComponent(final String definitionPath, final IPentahoUrlFactory urlFactory,
      final ArrayList messages) {

    return new XYZSeriesCollectionChartComponent(definitionPath, urlFactory, messages);
  }

}
