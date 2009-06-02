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
import org.pentaho.ui.xul.util.DialogController.DialogListener;

/**
 * @author mlowery
 */
public class SwingDatasourceSelectionDialog implements HasDialogController<IDatasource> {

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

  public DialogController<IDatasource> getDialogController() {
    return datasourceSelectionDialogController;
  }

  /**
   * For debug/demo purposes only.
   */
  public static void main(String[] args) throws XulException {
    ConnectionService connectionService = new ConnectionServiceDebugImpl();
    DatasourceService datasourceService = new DatasourceServiceDebugImpl();

    SwingDatasourceEditor editor = new SwingDatasourceEditor(datasourceService, connectionService);
    SwingDatasourceSelectionDialog selectDialog = new SwingDatasourceSelectionDialog(datasourceService, editor
        .getDialogController());
    selectDialog.getDialogController().showDialog();
  }

}
