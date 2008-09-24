package org.pentaho.mantle.login.client;

import java.util.List;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.login.client.messages.MantleLoginMessages;
import org.pentaho.mantle.login.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MantleLoginDialog {

  private AsyncCallback<Boolean> outterCallback; // from outside context
  private final TextBox userTextBox = new TextBox();
  private final ListBox usersListBox = new ListBox();
  private final PasswordTextBox passwordTextBox = new PasswordTextBox();
  private PromptDialogBox dialog;
  private CheckBox newWindowChk;
  private boolean serviceReturned;
  private boolean subscription;
  private Timer timer;

  private static MantleLoginServiceAsync SERVICE;
  private static MantleLoginMessages MSGS = Messages.getInstance();

  private boolean showNewWindowOption;

  static {
    SERVICE = (MantleLoginServiceAsync) GWT.create(MantleLoginService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    String moduleRelativeURL = GWT.getModuleBaseURL() + "MantleLoginService";
    endpoint.setServiceEntryPoint(moduleRelativeURL);
  }

  private final IDialogCallback myCallback = new IDialogCallback() {

    public void cancelPressed() {
    }

    public void okPressed() {
      // http://localhost:8080/pentaho/j_acegi_security_check?j_username=joe&j_password=password
      RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "/pentaho/j_acegi_security_check");
      builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
      RequestCallback callback = new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          outterCallback.onFailure(exception);
        }

        public void onResponseReceived(Request request, Response response) {

          final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onSuccess(Boolean result) {

              if (result) {
                outterCallback.onSuccess(newWindowChk != null && newWindowChk.isChecked());
              } else {
                outterCallback.onFailure(new Throwable(MSGS.authFailed()));
              }
            }

            public void onFailure(final Throwable caught) {
              MessageDialogBox errBox = new MessageDialogBox(MSGS.loginError(), MSGS.authFailed(), false, false, true);
              errBox.setCallback(new IDialogCallback() {
                public void cancelPressed() {
                }

                public void okPressed() {
                  outterCallback.onFailure(caught);
                }
              });
              errBox.show();
            }
          };
          SERVICE.isAuthenticated(callback);
        }
      };
      try {
        int selectedIdx = usersListBox.getSelectedIndex();
        String username = "";
        if(selectedIdx > 0){
          username = usersListBox.getItemText(selectedIdx);
        } else {
          username = userTextBox.getText();
        }
        
        builder.sendRequest("j_username=" + username + "&j_password=" + passwordTextBox.getText(), callback);
      } catch (RequestException e) {
        e.printStackTrace();
      }
    }

  };

  public MantleLoginDialog() {
    dialog = new PromptDialogBox(MSGS.login(), MSGS.login(), MSGS.cancel(), false, true);
    dialog.setCallback(myCallback);
    getSubscriptionLevel();
    fetchAllUsers();
  }

  public MantleLoginDialog(AsyncCallback callback, boolean showNewWindowOption) {
    this();
    setCallback(callback);
    this.setShowNewWindowOption(showNewWindowOption);
  }

  public void setShowNewWindowOption(boolean show) {
    showNewWindowOption = show;
  }

  public static void performLogin(AsyncCallback callback) {
    MantleLoginDialog dialog = new MantleLoginDialog(callback, false);
    dialog.show();
  }

  private Widget buildLoginPanel() {
    userTextBox.setWidth("100%");
    passwordTextBox.setWidth("100%");
    usersListBox.setWidth("100%");
    
    VerticalPanel loginPanel = new VerticalPanel();

    loginPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    loginPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    SimplePanel spacer;
    if(!this.subscription){
      loginPanel.add(new Label(MSGS.sampleUser() + ":"));
      loginPanel.add(usersListBox);
    
    
      spacer = new SimplePanel();
      spacer.setHeight("8px");
      loginPanel.add(spacer);
    }
    loginPanel.add(new Label(MSGS.username() + ":"));
    loginPanel.add(userTextBox);
    
    spacer = new SimplePanel();
    spacer.setHeight("8px");
    loginPanel.add(spacer);
    
    loginPanel.setCellHeight(spacer, "8px");
    loginPanel.add(new Label(MSGS.password() + ":"));
    loginPanel.add(passwordTextBox);

    // New Window checkbox
    if (this.showNewWindowOption) {
      spacer = new SimplePanel();
      spacer.setHeight("8px");
      loginPanel.add(spacer);
      loginPanel.setCellHeight(spacer, "8px");

      newWindowChk = new CheckBox();
      newWindowChk.setText(MSGS.launchInNewWindow());
      newWindowChk.setChecked(true);

      loginPanel.add(newWindowChk);
    }

    return loginPanel;
  }

  public void setCallback(AsyncCallback<Boolean> callback) {
    outterCallback = callback;
  }

  private String getUser() {
    return userTextBox.getText();
  }

  private String getPassword() {
    return passwordTextBox.getText();
  }

  public void center() {
    // called to 'reshow' the dialog after failure
    dialog.center();
  }
  
  private void setServiceReturned(){
    serviceReturned = true;
  }
  
  public void show() {
    if(!serviceReturned){
      timer = new Timer() {
        public void run() {
        }
      };
      timer.scheduleRepeating(200);
      return;
    }
    dialog.setContent(buildLoginPanel());
    userTextBox.setTabIndex(1);
    passwordTextBox.setTabIndex(2);
    newWindowChk.setTabIndex(3);
    passwordTextBox.setText("");
    dialog.setFocusWidget(userTextBox);

    dialog.center();
  }

  public void hide() {
    dialog.hide();
  }
  
  
  public void fetchAllUsers() {
    final AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(MSGS.error(), caught.toString(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Object result) {
        // filter out existing
        usersListBox.clear();
        usersListBox.addItem("Select a User");
        List<String> users = (List<String>) result;
        for (String user : users) {
          usersListBox.addItem(user);
        }
      }
    };
    
    SERVICE.getAllUsers(callback);
  }
  
  private void getSubscriptionLevel() {
    final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(MSGS.error(), caught.toString(), false, false, true);
        dialogBox.center();
      }

      public void onSuccess(Boolean result) {
        subscription = result;
        setServiceReturned();
      }
    };
    
    SERVICE.isSubscription(callback);
  }

}
