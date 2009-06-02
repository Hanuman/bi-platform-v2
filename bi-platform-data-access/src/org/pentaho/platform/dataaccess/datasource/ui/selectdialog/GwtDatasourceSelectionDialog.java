package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.EventHandlerWrapper;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.util.DialogController;

import com.google.gwt.core.client.GWT;

public class GwtDatasourceSelectionDialog implements IXulLoaderCallback, DialogController<IDatasource> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private DatasourceSelectionDialogController datasourceSelectionDialogController;

  private DialogController<IDatasource> datasourceDialogController;

  private DatasourceService datasourceService;

  // ~ Constructors ====================================================================================================

  public GwtDatasourceSelectionDialog(final DatasourceService datasourceService,
      final DialogController<IDatasource> datasourceDialogController) {
    this.datasourceDialogController = datasourceDialogController;
    this.datasourceService = datasourceService;
    try {
      AsyncXulLoader.loadXulFromUrl("datasourceSelectionDialog.xul", "datasourceSelectionDialog", this); //$NON-NLS-1$//$NON-NLS-2$
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // ~ Methods =========================================================================================================

  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayLoaded() {
  }

  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayRemoved() {
  }

  public void xulLoaded(final GwtXulRunner runner) {
    try {
      GwtXulDomContainer container = (GwtXulDomContainer) runner.getXulDomContainers().get(0);

      BindingFactory bf = new GwtBindingFactory(container.getDocumentRoot());

      // begin DatasourceSelectionDialogController setup
      datasourceSelectionDialogController = new DatasourceSelectionDialogController();
      datasourceSelectionDialogController.setBindingFactory(bf);
      datasourceSelectionDialogController.setDatasourceService(datasourceService);
      EventHandlerWrapper editPanelControllerWrapper = GWT.create(DatasourceSelectionDialogController.class);
      editPanelControllerWrapper.setHandler(datasourceSelectionDialogController);
      container.addEventHandler(editPanelControllerWrapper);
      // end DatasourceSelectionDialogController setup

      datasourceSelectionDialogController.setDatasourceDialogController(datasourceDialogController);

      runner.initialize();

      runner.start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<IDatasource> listener) {
    datasourceSelectionDialogController.addDialogListener(listener);
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void hideDialog() {
    datasourceSelectionDialogController.hideDialog();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void removeDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<IDatasource> listener) {
    datasourceSelectionDialogController.removeDialogListener(listener);
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {
    datasourceSelectionDialogController.showDialog();
  }

}
