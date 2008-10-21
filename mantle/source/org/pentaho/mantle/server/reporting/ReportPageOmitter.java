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
