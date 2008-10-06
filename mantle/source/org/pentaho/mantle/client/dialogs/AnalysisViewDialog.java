package org.pentaho.mantle.client.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.service.Utility;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;

public class AnalysisViewDialog extends PromptDialogBox {

  private ListBox lboxSchema = new ListBox();

  private ListBox lboxCube = new ListBox();

  public static final String FOCUS_ON_TITLE = "title"; //$NON-NLS-1$

  private HashMap<String, List<String>> schemaCubeHashMap;

  public AnalysisViewDialog(Document solutionRepositoryDoc) {
    super(Messages.getInstance().newAnalysisView(), Messages.getInstance().ok(), Messages.getInstance().cancel(), false, true, new VerticalPanel());
    buildAnalysisView(solutionRepositoryDoc);
    Utility.enableMouseSelection();
    lboxSchema.setTabIndex(1);
    lboxCube.setTabIndex(2);
    setFocusWidget(lboxSchema);
  }

  /**
   * Actual construction of the New Analysis View Dialog.
   * 
   * @return The container that contains all the requisite widget.
   */
  public Widget buildAnalysisView(Document solutionRepositoryDoc) {
    VerticalPanel mainPanel = (VerticalPanel) getContent();
    mainPanel.setSpacing(5);
    Label schemaLabel = new Label(Messages.getInstance().schema());
    Label cubeLabel = new Label(Messages.getInstance().cube());

    lboxSchema.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        final String currentSchema = lboxSchema.getItemText(lboxSchema.getSelectedIndex());
        updateCubeListBox(currentSchema);
      }
    });

    // Get the pertinent information for the cube and the schema.
    getSchemaAndCubeInfo();

    lboxSchema.setWidth("15em"); //$NON-NLS-1$
    lboxCube.setWidth("15em"); //$NON-NLS-1$
    mainPanel.add(schemaLabel);
    mainPanel.add(lboxSchema);
    mainPanel.add(cubeLabel);
    mainPanel.add(lboxCube);

    return mainPanel;
  }

  /*
   * We only need get methods because the set is implemented by the widget itself.
   */

  public String getSchema() {
    return lboxSchema.getItemText(lboxSchema.getSelectedIndex());
  }

  public String getCube() {
    return lboxCube.getItemText(lboxCube.getSelectedIndex());
  }

  /**
   * Populates the schema and cube list box based on the information retrieved from the catalogs.
   */
  private void getSchemaAndCubeInfo() {
    AsyncCallback callback = new AsyncCallback() {
      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback() {

          public void onFailure(Throwable caughtLogin) {
            // we are already logged in, or something horrible happened
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().couldNotGetFileProperties(), false, false,
                true);
            dialogBox.center();
          }

          public void onSuccess(Object result) {
            getSchemaAndCubeInfo();
          }
        });
      }

      @SuppressWarnings("unchecked")//$NON-NLS-1$
      public void onSuccess(Object result) {
        if (result != null) {
          schemaCubeHashMap = (HashMap<String, List<String>>) result;

          if (schemaCubeHashMap != null && schemaCubeHashMap.size() >= 1) {
            Iterator iter = schemaCubeHashMap.keySet().iterator();
            while (iter.hasNext()) {
              lboxSchema.addItem(iter.next().toString());
            }
            lboxSchema.setSelectedIndex(0);
            updateCubeListBox(lboxSchema.getItemText(lboxSchema.getSelectedIndex()));
          }
        } else {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().noMondrianSchemas(), false, false, true);
          dialogBox.center();
        }
      }
    };

    MantleServiceCache.getService().getMondrianCatalogs(callback);
  }

  /**
   * This method updates the cube list box based on the selection in the schema list box.
   * 
   * @param currentSchema
   *          The schema currently selected.
   */
  public void updateCubeListBox(String currentSchema) {
    lboxCube.clear();

    List<String> cubeNamesList = (List<String>) schemaCubeHashMap.get(currentSchema);
    int size = cubeNamesList.size();

    for (int i = 0; i < size; i++) {
      lboxCube.addItem(cubeNamesList.get(i));
    }
  }

  /**
   * Checks the input fields for input.
   * 
   * @return Returns false if the expected inputs were not provided.
   */
  public boolean validate() {
    final String schema = getSchema();
    if (schema == null || schema.length() == 0) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().selectSchema(), false, false, true);
      dialogBox.setWidth("15em"); //$NON-NLS-1$
      dialogBox.center();
      return false;
    }

    final String cube = getSchema();
    if (cube == null || cube.length() == 0) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().selectCube(), false, false, true);
      dialogBox.setWidth("15em"); //$NON-NLS-1$
      dialogBox.center();
      return false;
    }

    return true;
  }
} // End of class.
