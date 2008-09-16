package org.pentaho.mantle.server.reporting;

import org.jfree.report.layout.output.LogicalPageKey;
import org.jfree.report.layout.output.PhysicalPageKey;
import org.jfree.report.modules.output.pageable.base.PageFlowSelector;

public class ReportPageSelector implements PageFlowSelector {

  int page = 0;

  public ReportPageSelector(int page) {
    this.page = page;
  }

  public boolean isPhysicalPageAccepted(PhysicalPageKey key) {
    return false;
  }

  public boolean isLogicalPageAccepted(LogicalPageKey key) {
    if (key.getPosition() == page) {
      return true;
    }
    return false;
  }

}
