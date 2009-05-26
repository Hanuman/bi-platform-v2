package org.pentaho.platform.dataaccess.datasource.wizard;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UploadFile implements EntryPoint {

  public void onModuleLoad() {
    // Create a FormPanel and point it at a service.
    final FormPanel uploadForm = new FormPanel();
    uploadForm.setAction(GWT.getModuleBaseURL() + "/UploadService");

    // Because we're going to add a FileUpload widget, we'll need to set the
    // form to use the POST method, and multipart MIME encoding.
    uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
    uploadForm.setMethod(FormPanel.METHOD_POST);

    // Create a panel to hold all of the form widgets.
    VerticalPanel panel = new VerticalPanel();
    uploadForm.setWidget(panel);

    // Create a TextBox, giving it a name so that it will be submitted.
    final TextBox tb = new TextBox();
    tb.setName("textBoxFormElement");
    panel.add(tb);

    // Create a FileUpload widget.
    FileUpload upload = new FileUpload();
    upload.setName("uploadFormElement");
    panel.add(upload);

    // Add a 'Upload' button.
    Button uploadSubmitButton = new Button("Upload");
    panel.add(uploadSubmitButton);

    uploadSubmitButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        uploadForm.submit();
      }
    });

    uploadForm.addFormHandler(new FormHandler() {
      public void onSubmit(FormSubmitEvent event) {
      }

      public void onSubmitComplete(FormSubmitCompleteEvent event) {
        Window.alert(event.getResults());
      }
    });

    RootPanel.get().add(uploadForm);
  }
}