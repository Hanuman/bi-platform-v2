package org.pentaho.mantle.client.solutionbrowser;

import java.util.ArrayList;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

public class SolutionDocumentManager {

  private ArrayList<ISolutionDocumentListener> listeners = new ArrayList<ISolutionDocumentListener>();

  private Document solutionDocument;
  private static SolutionDocumentManager instance;

  private static boolean fetching = false;

  private SolutionDocumentManager() {
  }

  private native void flagSolutionDocumentLoaded()
  /*-{
    $wnd.mantle_repository_loaded = true;
  }-*/;

  public static SolutionDocumentManager getInstance() {
    if (instance == null) {
      instance = new SolutionDocumentManager();
    }
    return instance;
  }

  public void addSolutionDocumentListener(ISolutionDocumentListener listener) {
    listeners.add(listener);
    synchronized (SolutionDocumentManager.class) {
      if (!fetching && solutionDocument == null) {
        fetching = true;
        fetchSolutionDocument(true);
      }
    }
  }

  public void removeSolutionDocumentListener(ISolutionDocumentListener listener) {
    listeners.remove(listener);
  }

  private void fireSolutionDocumentFetched() {
    fetching = false;
    for (ISolutionDocumentListener listener : listeners) {
      listener.onFetchSolutionDocument(solutionDocument);
    }
    // flag that we have the document so that other things might start to use it (PDB-500)
    flagSolutionDocumentLoaded();
  }

  public void beforeFetchSolutionDocument() {
    for (ISolutionDocumentListener listener : listeners) {
      listener.beforeFetchSolutionDocument();
    }
  }
  
  public void fetchSolutionDocument(final boolean forceReload) {
    if (forceReload || solutionDocument == null) {
      fetchSolutionDocument(null);
    }
  }

  public void fetchSolutionDocument(final AsyncCallback<Document> callback, final boolean forceReload) {
    if (forceReload || solutionDocument == null) {
      fetchSolutionDocument(callback);
    } else {
      callback.onSuccess(solutionDocument);
    }
  }

  public void fetchSolutionDocument(final AsyncCallback<Document> callback) {
    // notify listeners that we are about to talk to the server (in case there's anything they want to do
    // such as busy cursor or tree loading indicators)
    beforeFetchSolutionDocument();
    
    RequestBuilder builder = null;
    if (GWT.isScript()) {
      String path = Window.Location.getPath();
      if (!path.endsWith("/")) { //$NON-NLS-1$
        path = path.substring(0, path.lastIndexOf("/") + 1); //$NON-NLS-1$
      }
      builder = new RequestBuilder(RequestBuilder.GET, path + "SolutionRepositoryService?component=getSolutionRepositoryDoc"); //$NON-NLS-1$
    } else {
      builder = new RequestBuilder(RequestBuilder.GET,
          "/MantleService?passthru=SolutionRepositoryService&component=getSolutionRepositoryDoc&userid=joe&password=password"); //$NON-NLS-1$
    }

    RequestCallback internalCallback = new RequestCallback() {

      public void onError(Request request, Throwable exception) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetRepositoryDocument"), false, //$NON-NLS-1$ //$NON-NLS-2$
            false, true);
        dialogBox.center();
      }

      public void onResponseReceived(Request request, Response response) {
        // ok, we have a repository document, we can build the GUI
        // consider caching the document
        solutionDocument = (Document) XMLParser.parse((String) (String) response.getText());
        fireSolutionDocumentFetched();
        callback.onSuccess(solutionDocument);
      }

    };
    try {
      builder.sendRequest(null, internalCallback);
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetRepositoryDocument"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
      dialogBox.center();
    }

  }

}
