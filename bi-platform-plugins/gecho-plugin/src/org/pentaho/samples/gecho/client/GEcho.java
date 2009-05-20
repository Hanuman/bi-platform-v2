package org.pentaho.samples.gecho.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GEcho implements EntryPoint {

  private VerticalPanel mainPanel = new VerticalPanel();

  private HorizontalPanel greetingPanel = new HorizontalPanel();

  private Button greetServerButton = new Button("Greet Server");

  private Label serverGreetingLabel = new Label();

  public void onModuleLoad() {

    greetingPanel.add(greetServerButton);
    greetingPanel.add(serverGreetingLabel);

    mainPanel.add(greetingPanel);

    RootPanel.get("gechodiv").add(mainPanel);

    greetServerButton.setFocus(true);

    greetServerButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        greetServer();
      }
    });

  }

  private void greetServer() {
    GEchoServiceAsync greetingSvc = GWT.create(GEchoService.class);

    // Set up the callback object.
    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        // TODO: Do something with errors.
      }

      public void onSuccess(String result) {
        serverGreetingLabel.setText(result);
      }
    };

    // Make the call to greet the server
    greetingSvc.echo("GEcho", callback);
  }
}
