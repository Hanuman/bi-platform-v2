package org.pentaho.mantle.client.solutionbrowser;

import com.google.gwt.xml.client.Document;

public interface ISolutionDocumentListener {
  public void onFetchSolutionDocument(Document solutionDocument);
  public void beforeFetchSolutionDocument();
}
