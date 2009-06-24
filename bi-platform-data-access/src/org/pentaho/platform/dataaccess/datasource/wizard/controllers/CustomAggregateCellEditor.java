package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.Aggregation;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulListitem;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulGroupbox;
import org.pentaho.ui.xul.containers.XulHbox;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.gwt.AbstractGwtXulContainer;
import org.pentaho.ui.xul.util.TreeCellEditor;
import org.pentaho.ui.xul.util.TreeCellEditorCallback;

public class CustomAggregateCellEditor extends XulEventSourceAdapter implements TreeCellEditor {
  DatasourceMessages datasourceMessages = null;

  Document document = null;

  XulDialog dialog = null;

  BindingFactory bf = null;

  Aggregation aggregation = null;

  List<XulCheckbox> aggregationCheckboxList = new ArrayList<XulCheckbox>();
  XulMenuList listbox = null;
  XulVbox rightAggregateBox = null;
  XulVbox leftAggregateBox = null;     
  TreeCellEditorCallback callback = null;

  public CustomAggregateCellEditor(XulDialog dialog, DatasourceMessages datasourceMessages, Document document,
      BindingFactory bf) {
    super();
    this.dialog = dialog;
    this.datasourceMessages = datasourceMessages;
    this.document = document;
    this.bf = bf;
    dialog.setBgcolor("#FFFFFF");
  }

  public Object getValue() {
    // TODO Auto-generated method stub
    return null;
  }

  public void hide() {
    dialog.hide();
  }

  public void setCheckboxChanged() {
    List<AggregationType> aggTypeList = new ArrayList<AggregationType>();
    for (XulCheckbox checkbox : aggregationCheckboxList) {
      if (checkbox.isChecked()) {
        aggTypeList.add(AggregationType.valueOf(checkbox.getID()));
      }
    }
    aggregation.setAggregationList(aggTypeList);
  }

  public void setValue(Object val) {
    // Clear the dialog box with all the existing checkboxes if any
    for (XulComponent component : dialog.getChildNodes()) {
      if (!(component instanceof XulButton)) {
        dialog.removeChild(component);
      }
    }
    // Create the list of check box in XulDialog
    aggregation = (Aggregation) val;
    aggregationCheckboxList.clear();
    List<AggregationType> aggregationList = aggregation.getAggregationList();
    AggregationType[] aggregationTypeArray = AggregationType.values();
    try {
      XulGroupbox groupBox = (XulGroupbox) document.createElement("groupbox");
      XulHbox mainAggregateBox = (XulHbox) document.createElement("hbox");
      leftAggregateBox = (XulVbox) document.createElement("vbox");
      rightAggregateBox = (XulVbox) document.createElement("vbox");
      for (int i = 0; i < aggregationTypeArray.length/2; i++) {
        XulCheckbox aggregationCheckBox;
        aggregationCheckBox = (XulCheckbox) document.createElement("checkbox");
        aggregationCheckBox.setLabel(datasourceMessages.getString(aggregationTypeArray[i].getDescription()));
        aggregationCheckBox.setID(aggregationTypeArray[i].name());
        aggregationCheckBox.setCommand(null);
        if (aggregationList.contains(aggregationTypeArray[i])) {
          aggregationCheckBox.setChecked(true);
        } else {
          aggregationCheckBox.setChecked(false);
        }
        aggregationCheckboxList.add(aggregationCheckBox);
        leftAggregateBox.addComponent(aggregationCheckBox);
        bf.createBinding(aggregationCheckBox, "checked", this, "checkboxChanged");
      }
      for (int j = aggregationTypeArray.length/2; j < aggregationTypeArray.length; j++) {
        XulCheckbox aggregationCheckBox;
        aggregationCheckBox = (XulCheckbox) document.createElement("checkbox");
        aggregationCheckBox.setLabel(datasourceMessages.getString(aggregationTypeArray[j].getDescription()));
        aggregationCheckBox.setID(aggregationTypeArray[j].name());
        aggregationCheckBox.setCommand(null);
        if (aggregationList.contains(aggregationTypeArray[j])) {
          aggregationCheckBox.setChecked(true);
        } else {
          aggregationCheckBox.setChecked(false);
        }
        aggregationCheckboxList.add(aggregationCheckBox);
        rightAggregateBox.addComponent(aggregationCheckBox);
        bf.createBinding(aggregationCheckBox, "checked", this, "checkboxChanged");
      }
      mainAggregateBox.addComponent(leftAggregateBox);
      mainAggregateBox.addComponent(rightAggregateBox);
      groupBox.setCaption(datasourceMessages.getString("aggregationEditorDialog.available"));
      groupBox.addChild(mainAggregateBox);
      groupBox.setHeight(120);
      groupBox.setWidth(200);
      ((AbstractGwtXulContainer) groupBox).layout();
      dialog.addChild(groupBox);
      XulLabel label = (XulLabel) document.createElement("label");
      label.setValue(datasourceMessages.getString("aggregationEditorDialog.default"));
      listbox = (XulMenuList) document.createElement("menulist");
      XulMenupopup menuPopup = (XulMenupopup) document.createElement("menupopup");
      listbox.addChild(menuPopup);
      listbox.setID("DefaultAggregationType");
      listbox.setBinding("name");
      dialog.addChild(label);
      dialog.addChild(listbox);
      bf.setBindingType(Binding.Type.ONE_WAY);
      Binding binding = bf.createBinding(aggregation, "aggregationList", listbox, "elements");
      bf.createBinding(aggregation, "defaultAggregationType", listbox, "selectedIndex");
      try {
        binding.fireSourceChanged();
      } catch (IllegalArgumentException e) {
        throw new XulException(e);
      } catch (InvocationTargetException e) {
        throw new XulException(e);
      }

    } catch (XulException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void show(int row, int col, Object boundObj, String columnBinding, TreeCellEditorCallback callback) {
    this.callback = callback;
    dialog.show();
  }

  public void notifyListeners() {
    hide();
    // Construct a new array list of aggregation based on what user selected
    // pass it to listener
    ArrayList<AggregationType> aggregationTypeList = new ArrayList<AggregationType>();
    for (XulComponent component : leftAggregateBox.getChildNodes()) {
      if (component instanceof XulCheckbox) {
        XulCheckbox checkbox = (XulCheckbox) component;
        if (checkbox.isChecked()) {
          aggregationTypeList.add(AggregationType.valueOf(checkbox.getID()));
        }
      }
    }
    for (XulComponent component : rightAggregateBox.getChildNodes()) {
      if (component instanceof XulCheckbox) {
        XulCheckbox checkbox = (XulCheckbox) component;
        if (checkbox.isChecked()) {
          aggregationTypeList.add(AggregationType.valueOf(checkbox.getID()));
        }
      }
    }    
    Object item = listbox.getSelectedItem();
    Aggregation aggregation = null; 
    if(item != null) {
      aggregation = new Aggregation(aggregationTypeList, AggregationType.valueOf(item.toString()));
    }
    this.callback.onCellEditorClosed(aggregation);
  }
}
