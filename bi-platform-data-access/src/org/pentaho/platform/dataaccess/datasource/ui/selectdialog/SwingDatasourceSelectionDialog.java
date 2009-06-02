package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.SwingDatasourceEditor;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceDebugImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceDebugImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.swing.SwingXulLoader;
import org.pentaho.ui.xul.swing.SwingXulRunner;
import org.pentaho.ui.xul.util.DialogController;

/**
 * @author mlowery
 */
public class SwingDatasourceSelectionDialog implements DialogController<IDatasource> {

  private XulRunner runner;

  private DatasourceSelectionDialogController datasourceSelectionDialogController;

  public SwingDatasourceSelectionDialog(final DatasourceService datasourceService,
      final DialogController<IDatasource> datasourceDialogController) throws XulException {
    XulDomContainer container = new SwingXulLoader()
        .loadXul("org/pentaho/platform/dataaccess/datasource/wizard/public/datasourceSelectionDialog.xul"); //$NON-NLS-1$

    runner = new SwingXulRunner();
    runner.addContainer(container);

    BindingFactory bf = new DefaultBindingFactory();
    bf.setDocument(container.getDocumentRoot());

    datasourceSelectionDialogController = new DatasourceSelectionDialogController();
    datasourceSelectionDialogController.setBindingFactory(bf);

    container.addEventHandler(datasourceSelectionDialogController);
    datasourceSelectionDialogController.setDatasourceService(datasourceService);

    datasourceSelectionDialogController.setDatasourceDialogController(datasourceDialogController);

    datasourceSelectionDialogController.addDialogListener(new DialogListener<IDatasource>() {
      public void onDialogAccept(IDatasource datasource) {
        System.out.printf("OK (returned %s)\n", datasource);
      }

      public void onDialogCancel() {
        System.out.println("Cancel");
      }
    });
    runner.initialize();
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

  public void showDialog() {
    datasourceSelectionDialogController.showDialog();
  }

  /**
   * For debug/demo purposes only.
   */
  public static void main(String[] args) throws XulException {
    ConnectionService connectionService = new ConnectionServiceDebugImpl();
    DatasourceService datasourceService = new DatasourceServiceDebugImpl();

    SwingDatasourceEditor editor = new SwingDatasourceEditor(datasourceService, connectionService);
    SwingDatasourceSelectionDialog selectDialog = new SwingDatasourceSelectionDialog(datasourceService, editor);
    selectDialog.showDialog();
  }

}
