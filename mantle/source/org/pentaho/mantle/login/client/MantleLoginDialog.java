package org.pentaho.mantle.login.client;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

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
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MantleLoginDialog extends PromptDialogBox {

  private AsyncCallback<Boolean> outerCallback; // from outside context
  private final TextBox userTextBox = new TextBox();
  private final ListBox usersListBox = new ListBox();
  private final PasswordTextBox passwordTextBox = new PasswordTextBox();
  private CheckBox newWindowChk = new CheckBox();

  private static boolean showUsersList = false;
  private static boolean showNewWindowOption = true;
  private static MantleLoginServiceAsync SERVICE;
  private static MantleLoginMessages MSGS = Messages.getInstance();

  private static LinkedHashMap<String, String[]> defaultUsers = new LinkedHashMap<String, String[]>();

  static {
    defaultUsers.put(MSGS.selectUser(), new String[] { "", "" }); //$NON-NLS-1$ //$NON-NLS-2$
    defaultUsers.put("Joe (admin)", new String[] { "joe", "password" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    defaultUsers.put("Suzy", new String[] { "suzy", "password" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    defaultUsers.put("Pat", new String[] { "pat", "password" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    defaultUsers.put("Tiffany", new String[] { "tiffany", "password" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    SERVICE = (MantleLoginServiceAsync) GWT.create(MantleLoginService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    String moduleRelativeURL = GWT.getModuleBaseURL() + "MantleLoginService"; //$NON-NLS-1$
    endpoint.setServiceEntryPoint(moduleRelativeURL);
  }

  private final IDialogCallback myCallback = new IDialogCallback() {

    public void cancelPressed() {
    }

    public void okPressed() {
      String path = Window.Location.getPath();
      if (!path.endsWith("/")) { //$NON-NLS-1$
        path = path.substring(0, path.lastIndexOf("/") + 1); //$NON-NLS-1$
      }
      RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, path + "j_acegi_security_check"); //$NON-NLS-1$
      builder.setHeader("Content-Type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
      RequestCallback callback = new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          outerCallback.onFailure(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onSuccess(Boolean result) {

              if (result) {
                long year = 1000 * 60 * 60 * 24 * 365;
                // one year into the future
                Date expirationDate = new Date(System.currentTimeMillis() + year);
                Cookies.setCookie("loginNewWindowChecked", "" + newWindowChk.isChecked(), expirationDate); //$NON-NLS-1$ //$NON-NLS-2$
                outerCallback.onSuccess(newWindowChk != null && newWindowChk.isChecked());
              } else {
                outerCallback.onFailure(new Throwable(MSGS.authFailed()));
              }
            }

            public void onFailure(final Throwable caught) {
              MessageDialogBox errBox = new MessageDialogBox(MSGS.loginError(), MSGS.authFailed(), false, false, true);
              errBox.setCallback(new IDialogCallback() {
                public void cancelPressed() {
                }

                public void okPressed() {
                  outerCallback.onFailure(caught);
                }
              });
              errBox.show();
            }
          };
          SERVICE.isAuthenticated(callback);
        }
      };
      try {
        String username = userTextBox.getText();
        builder.sendRequest("j_username=" + username + "&j_password=" + passwordTextBox.getText(), callback); //$NON-NLS-1$ //$NON-NLS-2$
      } catch (RequestException e) {
        e.printStackTrace();
      }
    }

  };

  public MantleLoginDialog() {
    super(MSGS.login(), MSGS.login(), MSGS.cancel(), false, true);
    setCallback(myCallback);
    userTextBox.setTabIndex(1);
    passwordTextBox.setTabIndex(2);
    if (showNewWindowOption) {
      newWindowChk.setTabIndex(3);
    }
    passwordTextBox.setText(""); //$NON-NLS-1$
    setFocusWidget(userTextBox);
    addDefaultUsers();
    SERVICE.isShowUsersList(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        setContent(buildLoginPanel());
        if (isAttached() && isVisible()) {
          center();
        }
      }

      public void onSuccess(Boolean result) {
        showUsersList = result;
        setContent(buildLoginPanel());
        if (isAttached() && isVisible()) {
          center();
        }
      }
    });
  }

  public MantleLoginDialog(AsyncCallback callback, boolean showNewWindowOption) {
    this();
    setCallback(callback);
    setShowNewWindowOption(showNewWindowOption);
  }

  public void setShowNewWindowOption(boolean show) {
    showNewWindowOption = show;
  }

  public static void performLogin(final AsyncCallback callback) {
    // let's only login if we are not actually logged in
    SERVICE.isAuthenticated(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        MantleLoginDialog dialog = new MantleLoginDialog(callback, false);
        dialog.show();
      }

      public void onSuccess(Boolean result) {
        if (!result) {
          MantleLoginDialog dialog = new MantleLoginDialog(callback, false);
          dialog.show();
        }
      }
    });
  }

  private Widget buildLoginPanel() {
    userTextBox.setWidth("100%"); //$NON-NLS-1$
    passwordTextBox.setWidth("100%"); //$NON-NLS-1$
    usersListBox.setWidth("100%"); //$NON-NLS-1$

    VerticalPanel loginPanel = new VerticalPanel();

    loginPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    loginPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    SimplePanel spacer;
    if (showUsersList) {
      loginPanel.add(new Label(MSGS.sampleUser() + ":")); //$NON-NLS-1$
      loginPanel.add(usersListBox);
      spacer = new SimplePanel();
      spacer.setHeight("8px"); //$NON-NLS-1$
      loginPanel.add(spacer);
    }
    loginPanel.add(new Label(MSGS.username() + ":")); //$NON-NLS-1$
    loginPanel.add(userTextBox);

    spacer = new SimplePanel();
    spacer.setHeight("8px"); //$NON-NLS-1$
    loginPanel.add(spacer);

    loginPanel.setCellHeight(spacer, "8px"); //$NON-NLS-1$
    loginPanel.add(new Label(MSGS.password() + ":")); //$NON-NLS-1$
    loginPanel.add(passwordTextBox);

    // New Window checkbox
    if (showNewWindowOption) {
      spacer = new SimplePanel();
      spacer.setHeight("8px"); //$NON-NLS-1$
      loginPanel.add(spacer);
      loginPanel.setCellHeight(spacer, "8px"); //$NON-NLS-1$

      newWindowChk.setText(MSGS.launchInNewWindow());

      String cookieCheckedVal = Cookies.getCookie("loginNewWindowChecked"); //$NON-NLS-1$
      if (cookieCheckedVal != null) {
        newWindowChk.setChecked(Boolean.parseBoolean(cookieCheckedVal));
      } else {
        // default is false, per BISERVER-2384
        newWindowChk.setChecked(false);
      }

      loginPanel.add(newWindowChk);
    }

    return loginPanel;
  }

  public void setCallback(AsyncCallback<Boolean> callback) {
    outerCallback = callback;
  }

  public void addDefaultUsers() {
    for (Map.Entry<String, String[]> entry : defaultUsers.entrySet()) {
      usersListBox.addItem(entry.getKey());
    }
    usersListBox.addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        String key = usersListBox.getValue(usersListBox.getSelectedIndex());
        userTextBox.setText(defaultUsers.get(key)[0]);
        passwordTextBox.setText(defaultUsers.get(key)[1]);
      }
    });
  }

}
