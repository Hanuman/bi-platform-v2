package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * The state (a.k.a. model) of this dialog.
 * 
 * @author mlowery
 */
public class DatasourceSelectionDialogModel extends XulEventSourceAdapter {

  /**
   * A cached version of the logicalModelSummaries from the <code>DatasourceService</code>.
   */
  private List<LogicalModelSummary> logicalModelSummaries;

  /**
   * The index of the selected datasource.
   */
  private int selectedIndex;

  public void setLogicalModelSummaries(final List<LogicalModelSummary> logicalModelSummaries) {
    final List<LogicalModelSummary> previousVal = this.logicalModelSummaries;
    this.logicalModelSummaries = logicalModelSummaries == null ? null : new ArrayList<LogicalModelSummary>(logicalModelSummaries);
    this.firePropertyChange("logicalModelSummaries", previousVal, logicalModelSummaries); //$NON-NLS-1$
  }

  public List<LogicalModelSummary> getLogicalModelSummaries() {
    return this.logicalModelSummaries == null ? null : new ArrayList<LogicalModelSummary>(logicalModelSummaries);
  }

  public void setSelectedIndex(final int selectedIndex) {
    final int previousVal = this.selectedIndex;
    this.selectedIndex = selectedIndex;
    this.firePropertyChange("selectedIndex", previousVal, selectedIndex); //$NON-NLS-1$
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

}
