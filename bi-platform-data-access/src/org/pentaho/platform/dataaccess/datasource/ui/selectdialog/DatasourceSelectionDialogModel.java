package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * The state (a.k.a. model) of this dialog.
 * 
 * @author mlowery
 */
public class DatasourceSelectionDialogModel extends XulEventSourceAdapter {

  /**
   * A cached version of the datasources from the <code>DatasourceService</code>.
   */
  private List<IDatasource> datasources;

  /**
   * The index of the selected datasource.
   */
  private int selectedIndex;

  public void setDatasources(final List<IDatasource> datasources) {
    final List<IDatasource> previousVal = this.datasources;
    this.datasources = datasources == null ? null : new ArrayList<IDatasource>(datasources);
    this.firePropertyChange("datasources", previousVal, datasources); //$NON-NLS-1$
  }

  public List<IDatasource> getDatasources() {
    return this.datasources == null ? null : new ArrayList<IDatasource>(datasources);
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
