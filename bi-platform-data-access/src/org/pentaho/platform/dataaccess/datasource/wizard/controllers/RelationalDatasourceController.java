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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.WaitingDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.models.Aggregation;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelDataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCell;
import org.pentaho.ui.xul.components.XulTreeCol;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeChildren;
import org.pentaho.ui.xul.containers.XulTreeCols;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.TreeCellEditor;
import org.pentaho.ui.xul.util.TreeCellEditorCallback;
import org.pentaho.ui.xul.util.TreeCellRenderer;

public class RelationalDatasourceController extends AbstractXulEventHandler implements IDatasourceTypeController {
  public static final int MAX_SAMPLE_DATA_ROWS = 5;

  public static final int MAX_COL_SIZE = 13;

  public static final String EMPTY_STRING = ""; //$NON-NLS-1$

  public static final String COMMA = ","; //$NON-NLS-1$

  private DatasourceMessages datasourceMessages;

  private XulDialog connectionDialog;

  private WaitingDialog waitingDialogBox;

  private XulDialog waitingDialog = null;

  private XulDialog applyQueryConfirmationDialog = null;

  private XulLabel waitingDialogLabel = null;

  private XulDialog previewResultsDialog = null;

  private IXulAsyncDatasourceService service;

  private DatasourceModel datasourceModel;

  BindingFactory bf;

  XulTree previewResultsTable = null;

  XulTextbox datasourceName = null;

  XulListbox connections = null;

  XulTextbox query = null;

  XulTreeCols previewResultsTreeCols = null;

  XulTextbox previewLimit = null;

  XulButton editConnectionButton = null;

  XulButton removeConnectionButton = null;

  XulButton editQueryButton = null;

  XulButton previewButton = null;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  private XulTree modelDataTable = null;

  private XulButton applyButton = null;

  private XulTreeCol columnNameTreeCol = null;

  private XulTreeCol columnTypeTreeCol = null;

  //private XulTreeCol columnFormatTreeCol = null;\
  XulTree sampleDataTree = null;

  XulDialog aggregationEditorDialog = null;

  XulDialog sampleDataDialog = null;

  CustomAggregateCellEditor aggregationCellEditor = null;

  CustomSampleDataCellEditor sampleDataCellEditor = null;

  CustomSampleDataCellRenderer sampleDataCellRenderer = null;

  //private XulRows rows = null;
  //private XulGrid grid = null;  
  CustomAggregationCellRenderer aggregationCellRenderer = null;

  private XulVbox relationalAggregationEditorVbox = null;

  public RelationalDatasourceController() {

  }

  @Bindable
  public void init() {
    //rows = (XulRows) document.getElementById("relationalSampleDataRows");//$NON-NLS-1$
    //grid = (XulGrid) document.getElementById("relationalSampleDataGrid");//$NON-NLS-1$
    relationalAggregationEditorVbox = (XulVbox) document.getElementById("relationalAggregationEditorVbox"); //$NON-NLS-1$
    applyButton = (XulButton) document.getElementById("apply"); //$NON-NLS-1$
    modelDataTable = (XulTree) document.getElementById("modelDataTable");
    sampleDataTree = (XulTree) document.getElementById("relationalSampleDataTable");
    aggregationEditorDialog = (XulDialog) document.getElementById("relationalAggregationEditorDialog");
    aggregationCellEditor = new CustomAggregateCellEditor(aggregationEditorDialog, datasourceMessages, document, bf);
    modelDataTable.registerCellEditor("aggregation-cell-editor", aggregationCellEditor);
    aggregationCellRenderer = new CustomAggregationCellRenderer();
    modelDataTable.registerCellRenderer("aggregation-cell-editor", aggregationCellRenderer);
    sampleDataDialog = (XulDialog) document.getElementById("relationalSampleDataDialog");
    sampleDataCellEditor = new CustomSampleDataCellEditor(sampleDataDialog);
    modelDataTable.registerCellEditor("sample-data-cell-editor", sampleDataCellEditor);
    sampleDataCellRenderer = new CustomSampleDataCellRenderer();
    modelDataTable.registerCellRenderer("sample-data-cell-editor", sampleDataCellRenderer);
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$    
    applyQueryConfirmationDialog = (XulDialog) document.getElementById("applyQueryConfirmationDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$    
    waitingDialog = (XulDialog) document.getElementById("waitingDialog"); //$NON-NLS-1$
    waitingDialogLabel = (XulLabel) document.getElementById("waitingDialogLabel");//$NON-NLS-1$    
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$
    datasourceName = (XulTextbox) document.getElementById("relationalDatasourceName"); //$NON-NLS-1$
    connections = (XulListbox) document.getElementById("connectionList"); //$NON-NLS-1$
    query = (XulTextbox) document.getElementById("query"); //$NON-NLS-1$
    connectionDialog = (XulDialog) document.getElementById("connectionDialog");//$NON-NLS-1$
    previewResultsDialog = (XulDialog) document.getElementById("previewResultsDialog");//$NON-NLS-1$
    previewResultsTable = (XulTree) document.getElementById("previewResultsTable"); //$NON-NLS-1$
    previewResultsTreeCols = (XulTreeCols) document.getElementById("previewResultsTreeCols"); //$NON-NLS-1$
    previewLimit = (XulTextbox) document.getElementById("previewLimit"); //$NON-NLS-1$
    editConnectionButton = (XulButton) document.getElementById("editConnection"); //$NON-NLS-1$
    removeConnectionButton = (XulButton) document.getElementById("removeConnection"); //$NON-NLS-1$
    editQueryButton = (XulButton) document.getElementById("editQuery"); //$NON-NLS-1$
    previewButton = (XulButton) document.getElementById("preview"); //$NON-NLS-1$
    columnNameTreeCol = (XulTreeCol) document.getElementById("relationalColumnNameTreeCol"); //$NON-NLS-1$
    columnTypeTreeCol = (XulTreeCol) document.getElementById("relationalColumnTypeTreeCol"); //$NON-NLS-1$
    //columnFormatTreeCol = (XulTreeCol) document.getElementById("relationalColumnFormatTreeCol"); //$NON-NLS-1$

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(datasourceModel.getRelationalModel(), "previewValidated", previewButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel.getRelationalModel(), "applyValidated", applyButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$

    BindingConvertor<String, Boolean> widgetBindingConvertor = new BindingConvertor<String, Boolean>() {

      @Override
      public Boolean sourceToTarget(String value) {
        return !((value == null) || value.length() <= 0);
      }

      @Override
      public String targetToSource(Boolean value) {
        return null;
      }

    };

    List<Binding> bindingsThatNeedInitialized = new ArrayList<Binding>();
    
    BindingConvertor<IConnection, Boolean> buttonConvertor = new BindingConvertor<IConnection, Boolean>() {

      @Override
      public Boolean sourceToTarget(IConnection value) {
        return !(value == null);
      }

      @Override
      public IConnection targetToSource(Boolean value) {
        return null;
      }

    };

    bf.setBindingType(Binding.Type.ONE_WAY);
    final Binding domainBinding = bf.createBinding(datasourceModel.getRelationalModel(),
        "connections", connections, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel.getRelationalModel(),
        "selectedConnection", editConnectionButton, "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding(datasourceModel.getRelationalModel(),
        "selectedConnection", removeConnectionButton, "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel.getRelationalModel(),
        "selectedConnection", connections, "selectedIndex", new BindingConvertor<IConnection, Integer>() { //$NON-NLS-1$ //$NON-NLS-2$

          @Override
          public Integer sourceToTarget(IConnection connection) {
            if (connection != null) {
              return datasourceModel.getRelationalModel().getConnectionIndex(connection);
            } else {
              return -1;
            }

          }

          @Override
          public IConnection targetToSource(Integer value) {
            if (value >= 0) {
              return datasourceModel.getRelationalModel().getConnections().get(value);
            } else {
              return null;
            }

          }

        });
    bf.createBinding(datasourceModel.getRelationalModel(), "dataRows", modelDataTable, "elements");
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel.getRelationalModel(), "previewLimit", previewLimit, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    // Not sure if editQuery button is doing much
    //bf.createBinding(editQueryButton, "!disabled", "removeConnectionButton", "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel.getRelationalModel(), "query", query, "value"); //$NON-NLS-1$ //$NON-NLS-2$

    try {
      // Fires the population of the model listbox. This cascades down to the categories and columns. In essence, this
      // call initializes the entire UI.
      domainBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
    

    for(Binding b : bindingsThatNeedInitialized){
      try {
        b.fireSourceChanged();

      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }

  @Bindable
  public void setDatasourceModel(DatasourceModel model) {
    this.datasourceModel = model;
  }

  @Bindable
  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  public String getName() {
    return "relationalDatasourceController";
  }

  @Bindable
  public void applyQuery() {
    if (datasourceModel.getRelationalModel().getBusinessData() != null) {
      applyQueryConfirmationDialog.show();
    } else {
      generateModel();
    }
  }

  @Bindable
  public void generateModel() {
      query.setDisabled(true);
      if (applyQueryConfirmationDialog.isVisible()) {
        applyQueryConfirmationDialog.hide();
      }
      showWaitingDialog(datasourceMessages.getString("DatasourceController.GENERATE_MODEL"), datasourceMessages
          .getString("DatasourceController.WAIT"));
      service.generateLogicalModel(datasourceModel.getRelationalModel().getDatasourceName(), datasourceModel.getRelationalModel()
          .getSelectedConnection().getName(), datasourceModel.getRelationalModel().getQuery(), datasourceModel
          .getRelationalModel().getPreviewLimit(), new XulServiceCallback<BusinessData>() {

        public void error(String message, Throwable error) {
          hideWaitingDialog();
          query.setDisabled(false);
          displayErrorMessage(error);
        }

        public void success(BusinessData businessData) {
          try {
            hideWaitingDialog();

            // merge any potential changes from earlier models
            if (datasourceModel.getRelationalModel().getBusinessData() != null) {
              Domain oldDomain = datasourceModel.getRelationalModel().getBusinessData().getDomain();
              Domain newDomain = businessData.getDomain();
              datasourceModel.copyOverMetadata(oldDomain, newDomain);
            }

            datasourceModel.getRelationalModel().setBusinessData(null);
            query.setDisabled(false);
            // Setting the editable property to true so that the table can be populated with correct cell types                    
            columnNameTreeCol.setEditable(true);
            columnTypeTreeCol.setEditable(true);
            //columnFormatTreeCol.setEditable(true);
            datasourceModel.getRelationalModel().setBusinessData(businessData);
          } catch (Exception xe) {
            xe.printStackTrace();
          }
        }
      });
  }

  public void editQuery() {

  }

  @Bindable
  public void closeConnectionDialog() {
    connectionDialog.hide();
  }

  @Bindable
  public void closeApplyQueryConfirmationDialog() {
    applyQueryConfirmationDialog.hide();
  }

  @Bindable
  public void displayPreview() {

      showWaitingDialog(datasourceMessages.getString("DatasourceController.GENERATE_PREVIEW_DATA"), datasourceMessages
          .getString("DatasourceController.WAIT"));
      service.doPreview(datasourceModel.getRelationalModel().getSelectedConnection().getName(), datasourceModel
          .getRelationalModel().getQuery(), datasourceModel.getRelationalModel().getPreviewLimit(),
          new XulServiceCallback<SerializedResultSet>() {

            public void error(String message, Throwable error) {
              hideWaitingDialog();
              displayErrorMessage(error);
            }

            public void success(SerializedResultSet rs) {
              try {
                List<List<String>> data = rs.getData();
                String[] columns = rs.getColumns();
                int columnCount = columns.length;
                // Remove any existing children
                List<XulComponent> previewResultsList = previewResultsTable.getChildNodes();

                for (int i = 0; i < previewResultsList.size(); i++) {
                  previewResultsTable.removeChild(previewResultsList.get(i));
                }
                XulTreeChildren treeChildren = previewResultsTable.getRootChildren();
                if (treeChildren != null) {
                  treeChildren.removeAll();
                }
                // Remove all the existing columns
                int curTreeColCount = previewResultsTable.getColumns().getColumnCount();
                List<XulComponent> cols = previewResultsTable.getColumns().getChildNodes();
                for (int i = 0; i < curTreeColCount; i++) {
                  previewResultsTable.getColumns().removeChild(cols.get(i));
                }
                previewResultsTable.update();
                // Recreate the colums
                XulTreeCols treeCols = previewResultsTable.getColumns();
                if (treeCols == null) {
                  try {
                    treeCols = (XulTreeCols) document.createElement("treecols");
                  } catch (XulException e) {

                  }
                }
                // Setting column data
                for (int i = 0; i < columnCount; i++) {
                  try {
                    XulTreeCol treeCol = (XulTreeCol) document.createElement("treecol");
                    treeCol.setLabel(columns[i]);
                    treeCol.setWidth(columns[i].length() + 120);
                    treeCols.addColumn(treeCol);
                  } catch (XulException e) {

                  }
                }

                XulTreeCols treeCols1 = previewResultsTable.getColumns();
                int count = previewResultsTable.getColumns().getColumnCount();
                // Create the tree children and setting the data
                try {
                  for (int i = 0; i < data.size(); i++) {
                    XulTreeRow row = (XulTreeRow) document.createElement("treerow");
                    for (int j = 0; j < columnCount; j++) {
                      XulTreeCell cell = (XulTreeCell) document.createElement("treecell");
                      cell.setLabel(getCellData(data, i, j));
                      row.addCell(cell);
                    }

                    previewResultsTable.addTreeRow(row);
                  }
                  previewResultsTable.update();
                  hideWaitingDialog();
                  previewResultsDialog.show();
                } catch (XulException e) {
                  // TODO: add logging
                  hideWaitingDialog();
                  System.out.println(e.getMessage());
                  e.printStackTrace();
                }
              } catch (Exception e) {
                hideWaitingDialog();
                displayErrorMessage(e);
              }
            }
          });
  }

  @Bindable
  public void closePreviewResultsDialog() {
    previewResultsDialog.hide();
  }

  public IXulAsyncDatasourceService getService() {
    return service;
  }

  public void setService(IXulAsyncDatasourceService service) {
    this.service = service;
  }

  @Bindable
  public void openErrorDialog(String title, String message) {
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
    errorDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if (!errorDialog.isHidden()) {
      errorDialog.hide();
    }
  }

  @Bindable
  public void openSuccesDialog(String title, String message) {
    successDialog.setTitle(title);
    successLabel.setValue(message);
    successDialog.show();
  }

  @Bindable
  public void closeSuccessDialog() {
    if (!successDialog.isHidden()) {
      successDialog.hide();
    }
  }

  /*  public void showWaitingDialog(String title, String message) {
      getWaitingDialog().setTitle(title);
      getWaitingDialog().setMessage(message);
      getWaitingDialog().show();
    }

    public void hideWaitingDialog() {
      getWaitingDialog().hide();
    }
  */

  @Bindable
  public void showWaitingDialog(String title, String message) {
    waitingDialog.setTitle(title);
    waitingDialogLabel.setValue(message);
    waitingDialog.show();

  }

  @Bindable
  public void hideWaitingDialog() {
    waitingDialog.hide();
  }

  public void displayErrorMessage(Throwable th) {
    errorDialog.setTitle(ExceptionParser.getErrorHeader(th, getDatasourceMessages().getString("DatasourceEditor.USER_ERROR_TITLE")));
    errorLabel.setValue(ExceptionParser.getErrorMessage(th, getDatasourceMessages().getString("DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED")));
    errorDialog.show();
  }

  /**
   * @param datasourceMessages the datasourceMessages to set
   */
  public void setDatasourceMessages(DatasourceMessages datasourceMessages) {
    this.datasourceMessages = datasourceMessages;
  }

  /**
   * @return the datasourceMessages
   */
  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }

  /**
   * @return the waitingDialog
   */
  public WaitingDialog getWaitingDialog() {
    return this.waitingDialogBox;
  }

  /**
   * @param waitingDialog the waitingDialog to set
   */
  public void setWaitingDialog(WaitingDialog waitingDialog) {
    this.waitingDialogBox = waitingDialog;
  }

  @Bindable
  public void closeAggregationEditorDialog() {
    aggregationCellEditor.hide();
  }

  @Bindable
  public void saveAggregationValues() {
    aggregationCellEditor.notifyListeners();
  }

  @Bindable
  public void closeSampleDataDialog() {
    sampleDataCellEditor.hide();
  }

  private class CustomSampleDataCellEditor implements TreeCellEditor {
    XulDialog dialog = null;

    TreeCellEditorCallback callback = null;

    public CustomSampleDataCellEditor(XulDialog dialog) {
      super();
      this.dialog = dialog;
    }

    public Object getValue() {
      // TODO Auto-generated method stub
      return null;
    }

    public void hide() {
      dialog.hide();
    }

    public void setValue(Object val) {

    }

    public void show(int row, int col, Object boundObj, String columnBinding, TreeCellEditorCallback callback) {
      this.callback = callback;
      ModelDataRow modelDataRow = (ModelDataRow) boundObj;
      XulTreeCol column = sampleDataTree.getColumns().getColumn(0);
      column.setLabel(modelDataRow.getColumnName());
      List<String> values = modelDataRow.getSampleDataList();
      List<String> sampleDataList = new ArrayList<String>();
      for (int i = 0; i < MAX_SAMPLE_DATA_ROWS && i < modelDataRow.getSampleDataList().size(); i++) {
        sampleDataList.add(values.get(i));
      }
      sampleDataTree.setElements(sampleDataList);
      sampleDataTree.update();
      dialog.setTitle(datasourceMessages.getString("DatasourceController.SAMPLE_DATA"));
      dialog.show();
    }
  }

  private class CustomAggregationCellRenderer implements TreeCellRenderer {

    public Object getNativeComponent() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getText(Object value) {
      StringBuffer buffer = new StringBuffer();
      if (value instanceof Aggregation) {
        Aggregation aggregation = (Aggregation) value;
        List<AggregationType> aggregationList = aggregation.getAggregationList();
        for (int i = 0; i < aggregationList.size(); i++) {
          if (buffer.length() + datasourceMessages.getString(aggregationList.get(i).getDescription()).length() < MAX_COL_SIZE) {
            buffer.append(datasourceMessages.getString(aggregationList.get(i).getDescription()));
            if ((i < aggregationList.size() - 1 && (buffer.length()
                + datasourceMessages.getString(aggregationList.get(i + 1).getDescription()).length() + COMMA.length() < MAX_COL_SIZE))) {
              buffer.append(COMMA);
            }
          } else {
            break;
          }
        }
      }
      return buffer.toString();
    }

    public boolean supportsNativeComponent() {
      // TODO Auto-generated method stub
      return false;
    }

  }

  private class CustomSampleDataCellRenderer implements TreeCellRenderer {

    public Object getNativeComponent() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getText(Object value) {
      if (value instanceof String) {
        return getSampleData((String) value);
      } else if (value instanceof Vector) {
        Vector<String> vectorValue = (Vector<String>) value;
        StringBuffer sampleDataBuffer = new StringBuffer();
        for (int i = 0; i < vectorValue.size(); i++) {
          sampleDataBuffer.append(vectorValue.get(i));
        }
        return getSampleData(sampleDataBuffer.toString());
      }
      return EMPTY_STRING;
    }

    public boolean supportsNativeComponent() {
      // TODO Auto-generated method stub
      return false;
    }

    private String getSampleData(String sampleData) {
      if (sampleData != null && sampleData.length() > 0) {
        if (sampleData.length() <= MAX_COL_SIZE) {
          return sampleData;
        } else {
          return sampleData.substring(0, MAX_COL_SIZE);
        }
      }
      return EMPTY_STRING;
    }
  }

  public void initializeBusinessData(BusinessData businessData) {
    modelDataTable.update();
    datasourceModel.setDatasourceType(DatasourceType.SQL);

    SqlPhysicalModel model = (SqlPhysicalModel) businessData.getDomain().getPhysicalModels().get(0);
    String queryStr = model.getPhysicalTables().get(0).getTargetTable();
    //    datasourceModel.setDatasourceType(DatasourceType.SQL);
    datasourceModel.getRelationalModel().setDatasourceName(businessData.getDomain().getId());
    datasourceModel.getRelationalModel().setQuery(queryStr);
    for (IConnection conn : datasourceModel.getRelationalModel().getConnections()) {
      if (model.getDatasource().getDatabaseName().equals(conn.getName())) {
        datasourceModel.getRelationalModel().setSelectedConnection(conn);
        break;
      }
    }
    datasourceModel.getRelationalModel().setBusinessData(null);
    query.setDisabled(false);
    // Setting the editable property to true so that the table can be populated with correct cell types                    
    columnNameTreeCol.setEditable(true);
    columnTypeTreeCol.setEditable(true);
    //columnFormatTreeCol.setEditable(true);
    datasourceModel.getRelationalModel().setBusinessData(businessData);

  }

  public boolean supportsBusinessData(BusinessData businessData) {
    return (businessData.getDomain().getPhysicalModels().get(0) instanceof SqlPhysicalModel);
  }
  
  private String getCellData(List<List<String>> data, int rowNumber,  int columnNumber) {
    String returnValue = null;
    int rowCount = 0;
    for (List<String> row : data) {
      if(rowCount == rowNumber) {
        returnValue = row.get(columnNumber);
      }
      rowCount++;
    }
    return returnValue;
  }
}
