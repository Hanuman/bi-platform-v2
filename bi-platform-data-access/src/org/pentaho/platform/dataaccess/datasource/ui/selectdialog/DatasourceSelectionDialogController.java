package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.util.AbstractXulDialogController;
import org.pentaho.ui.xul.util.DialogController;

import com.google.gwt.user.client.Window;

public class DatasourceSelectionDialogController extends AbstractXulDialogController<IDatasource> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private BindingFactory bf;

  private DatasourceService datasourceService;

  private DatasourceSelectionDialogModel datasourceSelectionDialogModel = new DatasourceSelectionDialogModel();

  private XulDialog datasourceSelectionDialog;

  /**
   * The controller for the datasource dialog, which is shown when the user clicks the Add button in this dialog.
   */
  private DialogController<IDatasource> datasourceDialogController;

  // ~ Constructors ====================================================================================================

  public DatasourceSelectionDialogController() {

  }

  // ~ Methods =========================================================================================================

  /**
   * Sets up bindings.
   */
  public void init() {
    try {
      XulListbox datasourceListbox = (XulListbox) safeGetElementById(document, "datasourceListbox"); //$NON-NLS-1$
      datasourceSelectionDialog = (XulDialog) safeGetElementById(document, "datasourceSelectionDialog"); //$NON-NLS-1$
      XulButton acceptButton = (XulButton) safeGetElementById(document, "datasourceSelectionDialog_accept"); //$NON-NLS-1$

      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(DatasourceSelectionDialogController.this.datasourceSelectionDialogModel,
          "datasources", datasourceListbox, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(datasourceListbox, "selectedIndex", //$NON-NLS-1$
          DatasourceSelectionDialogController.this.datasourceSelectionDialogModel, "selectedIndex"); //$NON-NLS-1$

      // setup binding to disable accept button until user selects a datasource
      bf.setBindingType(Binding.Type.ONE_WAY);
      BindingConvertor<Integer, Boolean> acceptButtonConvertor = new BindingConvertor<Integer, Boolean>() {
        @Override
        public Boolean sourceToTarget(final Integer value) {
          return value > -1;
        }

        @Override
        public Integer targetToSource(final Boolean value) {
          throw new UnsupportedOperationException();
        }
      };
      bf.createBinding(DatasourceSelectionDialogController.this.datasourceSelectionDialogModel, "selectedIndex", //$NON-NLS-1$
          acceptButton, "!disabled", acceptButtonConvertor); //$NON-NLS-1$

      datasourceListbox.setSelectedIndex(-1);
      // workaround for bug in some XulListbox implementations (doesn't fire event on setSelectedIndex call)
      DatasourceSelectionDialogController.this.datasourceSelectionDialogModel.setSelectedIndex(-1);

    } catch (Exception e) {
      e.printStackTrace();
      showMessagebox("Error", e.getLocalizedMessage()); //$NON-NLS-1$
    }
    refreshDatasources();
  }

  /**
   * Shows a informational dialog.
   * 
   * @param title
   *          title of dialog
   * @param message
   *          message within dialog
   */
  private void showMessagebox(final String title, final String message) {
    XulMessageBox messagebox = null;
    try {
      messagebox = (XulMessageBox) document.createElement("messagebox"); //$NON-NLS-1$
    } catch (XulException e) {
      e.printStackTrace();
    }
    messagebox.setTitle(title);
    messagebox.setMessage(message);
    messagebox.open();
  }

  /**
   * A fail-quickly version of <code>getElementById()</code>.
   */
  private XulComponent safeGetElementById(final Document doc, final String id) {
    XulComponent elem = doc.getElementById(id);
    if (elem != null) {
      return elem;
    } else {
      throw new NullPointerException("element with id \"" + id + "\" is null"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  private void refreshDatasources() {
     datasourceService.getDatasources(new XulServiceCallback<List<IDatasource>>() {
    
     public void error(final String message, final Throwable error) {
     System.out.println(message);
     }
    
     public void success(final List<IDatasource> datasourceList) {
     DatasourceSelectionDialogController.this.datasourceSelectionDialogModel.setDatasources(datasourceList);
     }
    
     });
    // test code......
//    List<IDatasource> datasourceList = new ArrayList<IDatasource>();
//    IDatasource ds = new Datasource();
//    ds.setDatasourceName("hello");
//    datasourceList.add(ds);
//    ds = new Datasource();
//    ds.setDatasourceName("goodbye");
//    datasourceList.add(ds);
//    DatasourceSelectionDialogController.this.datasourceSelectionDialogModel.setDatasources(datasourceList);
  }

  /**
   * ID of this controller. This is how event handlers are referenced in <code>.xul</code> files.
   */
  @Override
  public String getName() {
    return "datasourceSelectionDialogController"; //$NON-NLS-1$
  }

  public void setBindingFactory(final BindingFactory bf) {
    this.bf = bf;
  }

  public void setDatasourceService(final DatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  /**
   * @return selected datasource or <code>null</code> if no selected datasource
   */
  @Override
  protected IDatasource getDialogResult() {
    int selectedIndex = datasourceSelectionDialogModel.getSelectedIndex();
    if (selectedIndex > -1) {
      return datasourceSelectionDialogModel.getDatasources().get(selectedIndex);
    } else {
      return null;
    }
  }

  @Override
  protected XulDialog getDialog() {
    return datasourceSelectionDialog;
  }

  public void setDatasourceDialogController(final DialogController<IDatasource> datasourceDialogController) {
    this.datasourceDialogController = datasourceDialogController;
  }

  public void addDatasource() {
    datasourceDialogController.addDialogListener(new DialogListener<IDatasource>() {
      public void onDialogAccept(final IDatasource datasource) {
        Window.alert("user accepted");
        refreshDatasources();
      }

      public void onDialogCancel() {
        Window.alert("user cancelled");
      }
    });
    datasourceDialogController.showDialog();
  }

}
