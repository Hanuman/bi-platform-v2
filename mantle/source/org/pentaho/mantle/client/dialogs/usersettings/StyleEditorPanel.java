package org.pentaho.mantle.client.dialogs.usersettings;

import org.pentaho.gwt.widgets.client.colorpicker.ColorPickerDialog;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class StyleEditorPanel extends UserPreferencesPanel {

  VerticalPanel content = new VerticalPanel();
  Document styleDocument;

  public StyleEditorPanel() {
    fetchStyleDocument();
    init();
  }

  private void fetchStyleDocument() {
    // go to server and get settings document
    String styleURL = "mantle/MantleStyleManager?method=getAvailableStyles"; //$NON-NLS-1$
    if (!GWT.isScript()) {
      styleURL = "http://localhost:8080/pentaho/mantle/MantleStyleManager?method=getAvailableStyles"; //$NON-NLS-1$
    }
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, styleURL);
    RequestCallback callback = new RequestCallback() {

      public void onError(Request request, Throwable exception) {
        Window.alert(Messages.getInstance().couldNotGetUserSettings());
      }

      public void onResponseReceived(Request request, Response response) {
        try {
          styleDocument = (Document) XMLParser.parse((String) response.getText());
          initStyleUI();
        } catch (Exception e) {
        }
      }

    };
    try {
      builder.sendRequest(null, callback);
    } catch (RequestException e) {
      MessageDialogBox dialog = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().couldNotGetUserSettings(), true, false, true);
      dialog.center();
    }
  }

  private void init() {
    content.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    content.setWidth("100%"); //$NON-NLS-1$
    ScrollPanel scroller = new ScrollPanel();
    scroller.add(content);
    add(scroller);
    scroller.setHeight("400px"); //$NON-NLS-1$
    scroller.setWidth("400px"); //$NON-NLS-1$
  }

  private void initStyleUI() {
    // build ui from style document
    NodeList styles = styleDocument.getDocumentElement().getChildNodes();
    for (int i = 0; i < styles.getLength(); i++) {
      Element childElement = (Element) styles.item(i);
      try {
        final String name = childElement.getAttribute("name"); //$NON-NLS-1$
        final String displayName = childElement.getAttribute("displayName"); //$NON-NLS-1$
        final String effectiveValue = childElement.getAttribute("effectiveValue"); //$NON-NLS-1$
        final String defaultValue = childElement.getAttribute("defaultValue"); //$NON-NLS-1$
        final String globalValue = childElement.getAttribute("globalValue"); //$NON-NLS-1$

        final TextBox effectiveValueTextBox = new TextBox();
        final TextBox globalValueTextBox = new TextBox();
        globalValueTextBox.setText(globalValue);
        globalValueTextBox.setReadOnly(true);

        final HTML stylePreview = new HTML("&nbsp;&nbsp;"); //$NON-NLS-1$
        stylePreview.setWidth("15px"); //$NON-NLS-1$
        stylePreview.setTitle(Messages.getInstance().chooseColor());
        DOM.setStyleAttribute(stylePreview.getElement(), "cursor", "hand"); //$NON-NLS-1$ //$NON-NLS-2$
        DOM.setStyleAttribute(stylePreview.getElement(), "cursor", "pointer"); //$NON-NLS-1$ //$NON-NLS-2$
        DOM.setStyleAttribute(stylePreview.getElement(), "background", effectiveValue); //$NON-NLS-1$
        DOM.setStyleAttribute(stylePreview.getElement(), "border", "1px solid black"); //$NON-NLS-1$ //$NON-NLS-2$
        stylePreview.addClickListener(new ClickListener() {

          public void onClick(Widget sender) {
            final ColorPickerDialog colorPickerDialog = new ColorPickerDialog(effectiveValue);
            colorPickerDialog.setCallback(new IDialogCallback() {

              public void cancelPressed() {
              }

              public void okPressed() {
                effectiveValueTextBox.setText("#" + colorPickerDialog.getHexColor()); //$NON-NLS-1$
                DOM.setStyleAttribute(stylePreview.getElement(), "background", effectiveValueTextBox.getText()); //$NON-NLS-1$
              }

            });
            colorPickerDialog.center();
          }

        });

        final HTML styleDefaultPreview = new HTML("&nbsp;&nbsp;"); //$NON-NLS-1$
        styleDefaultPreview.setWidth("15px"); //$NON-NLS-1$
        DOM.setStyleAttribute(styleDefaultPreview.getElement(), "background", defaultValue); //$NON-NLS-1$
        DOM.setStyleAttribute(styleDefaultPreview.getElement(), "border", "1px solid black"); //$NON-NLS-1$ //$NON-NLS-2$

        final HTML styleGlobalPreview = new HTML("&nbsp;&nbsp;"); //$NON-NLS-1$
        styleGlobalPreview.setWidth("15px"); //$NON-NLS-1$
        DOM.setStyleAttribute(styleGlobalPreview.getElement(), "background", globalValue); //$NON-NLS-1$
        DOM.setStyleAttribute(styleGlobalPreview.getElement(), "border", "1px solid black"); //$NON-NLS-1$ //$NON-NLS-2$

        effectiveValueTextBox.setText(effectiveValue);
        effectiveValueTextBox.addKeyboardListener(new KeyboardListener() {

          public void onKeyDown(Widget sender, char keyCode, int modifiers) {
            DOM.setStyleAttribute(stylePreview.getElement(), "background", effectiveValueTextBox.getText()); //$NON-NLS-1$
          }

          public void onKeyPress(Widget sender, char keyCode, int modifiers) {
            DOM.setStyleAttribute(stylePreview.getElement(), "background", effectiveValueTextBox.getText()); //$NON-NLS-1$
          }

          public void onKeyUp(Widget sender, char keyCode, int modifiers) {
            DOM.setStyleAttribute(stylePreview.getElement(), "background", effectiveValueTextBox.getText()); //$NON-NLS-1$
          }

        });
        effectiveValueTextBox.addChangeListener(new ChangeListener() {
          public void onChange(Widget sender) {
            DOM.setStyleAttribute(stylePreview.getElement(), "background", effectiveValueTextBox.getText()); //$NON-NLS-1$
          }
        });

        TextBox defaultValueTextBox = new TextBox();
        defaultValueTextBox.setText(defaultValue);
        defaultValueTextBox.setReadOnly(true);

        Button applyButton = new Button(Messages.getInstance().apply());
        applyButton.addClickListener(new ClickListener() {
          public void onClick(Widget sender) {
            // go to server, make change
            String setStyleURL = "mantle/MantleStyleManager?method=setStyle&style=" + name + "&value=" //$NON-NLS-1$ //$NON-NLS-2$
                + URL.encodeComponent(effectiveValueTextBox.getText());
            if (!GWT.isScript()) {
              setStyleURL = "http://localhost:8080/pentaho/mantle/MantleStyleManager?method=setStyle&style=" + name + "&value=" //$NON-NLS-1$ //$NON-NLS-2$
                  + effectiveValueTextBox.getText();
            }
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, setStyleURL);
            RequestCallback callback = new RequestCallback() {

              public void onError(Request request, Throwable exception) {
                Window.alert(exception.toString());
              }

              public void onResponseReceived(Request request, Response response) {
                Document myDoc = (Document) XMLParser.parse((String) response.getText());
                Element myStyleElement = (Element) myDoc.getDocumentElement().getChildNodes().item(0);
                String myStyleName = myStyleElement.getAttribute("name"); //$NON-NLS-1$
                String myStyleValue = myStyleElement.getAttribute("effectiveValue"); //$NON-NLS-1$

                if (myStyleValue.equalsIgnoreCase(effectiveValueTextBox.getText())) {
                  MessageDialogBox dialog = new MessageDialogBox(Messages.getInstance().info(), Messages.getInstance().preferencesSetSuccess(), true, true, false);
                  dialog.center();
                } else {
                  MessageDialogBox dialog = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().preferencesSetFailed(), true, true, false);
                  dialog.center();
                }

              }

            };
            try {
              builder.sendRequest(null, callback);
            } catch (RequestException e) {
              MessageDialogBox dialog = new MessageDialogBox(Messages.getInstance().error(), e.getMessage(), true, false, true);
              dialog.center();
            }
          }
        });

        Button revertButton = new Button(Messages.getInstance().revert());
        revertButton.setTitle(Messages.getInstance().revertToDefault());
        revertButton.addClickListener(new ClickListener() {
          public void onClick(Widget sender) {
            // go to server, make change?
            effectiveValueTextBox.setText(defaultValue);
            DOM.setStyleAttribute(stylePreview.getElement(), "background", effectiveValueTextBox.getText()); //$NON-NLS-1$
          }
        });

        Button revertToGlobalButton = new Button(Messages.getInstance().revert());
        revertToGlobalButton.setTitle(Messages.getInstance().revertToGlobal());
        revertToGlobalButton.addClickListener(new ClickListener() {
          public void onClick(Widget sender) {
            // go to server, make change?
            effectiveValueTextBox.setText(globalValue);
            DOM.setStyleAttribute(styleGlobalPreview.getElement(), "background", effectiveValueTextBox.getText()); //$NON-NLS-1$
          }
        });

        FlexTable styleTable = new FlexTable();
        styleTable.setWidget(0, 0, new Label(Messages.getInstance().effectiveValue()));
        styleTable.setWidget(0, 1, effectiveValueTextBox);
        if (name.endsWith("_COLOR")) { //$NON-NLS-1$
          styleTable.setWidget(0, 2, stylePreview);
        } else {
          styleTable.setWidget(0, 2, new Label());
        }
        styleTable.setWidget(0, 3, applyButton);
        // default value
        styleTable.setWidget(1, 0, new Label(Messages.getInstance().defaultValue()));
        styleTable.setWidget(1, 1, defaultValueTextBox);
        if (name.endsWith("_COLOR")) { //$NON-NLS-1$
          styleTable.setWidget(1, 2, styleDefaultPreview);
        } else {
          styleTable.setWidget(1, 2, new Label());
        }
        styleTable.setWidget(1, 3, revertButton);
        // global value
        styleTable.setWidget(2, 0, new Label(Messages.getInstance().globalValue()));
        styleTable.setWidget(2, 1, globalValueTextBox);
        if (name.endsWith("_COLOR")) { //$NON-NLS-1$
          styleTable.setWidget(2, 2, styleGlobalPreview);
        } else {
          styleTable.setWidget(2, 2, new Label());
        }
        styleTable.setWidget(2, 3, revertToGlobalButton);

        CaptionPanel captionPanel = new CaptionPanel(displayName);
        captionPanel.setContentWidget(styleTable);
        content.add(captionPanel);
      } catch (Exception e) {
        // final String effectiveValue = childElement.getAttribute("effectiveValue");
        // Window.alert(effectiveValue);
      }
    }
  }

  public boolean onApply() {
    return true;
  }

  public void onCancel() {
  }

}
