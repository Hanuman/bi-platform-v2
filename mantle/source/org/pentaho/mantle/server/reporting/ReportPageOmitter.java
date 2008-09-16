package org.pentaho.mantle.server.reporting;

import org.jfree.report.layout.output.LogicalPageKey;
import org.jfree.report.layout.output.PhysicalPageKey;
import org.jfree.report.modules.output.pageable.base.PageFlowSelector;

public class ReportPageOmitter implements PageFlowSelector {

  int pageToOmit = 0;

  public ReportPageOmitter(int pageToOmit) {
    this.pageToOmit = pageToOmit;
  }

  public boolean isPhysicalPageAccepted(PhysicalPageKey key) {
    return false;
  }

  public boolean isLogicalPageAccepted(LogicalPageKey key) {
    if (key.getPosition() == pageToOmit) {
      return false;
    }
    return true;
  }

}
