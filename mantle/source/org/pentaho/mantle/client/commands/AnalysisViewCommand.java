package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.mantle.client.dialogs.AnalysisViewDialog;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;

public class AnalysisViewCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;

  public AnalysisViewCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute() {
    final AnalysisViewDialog analysisDialog = new AnalysisViewDialog(navigatorPerspective.getSolutionDocument());
    IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
      }

      public void okPressed() {
        String actionName = System.currentTimeMillis() + ".analysisview.xaction";
        String newAnalysisViewURL = "AnalysisViewService?component=createNewView&name=" + actionName + "&descr=" + actionName + "&actionName="
            + actionName + "&textfield=&schema=" + analysisDialog.getSchema() + "&cube=" + analysisDialog.getCube() + "&solution=system&actionPath=tmp";
        if (!GWT.isScript()) {
          newAnalysisViewURL = "http://localhost:8080" + newAnalysisViewURL + "&userid=joe&password=password";
        }
        
        navigatorPerspective.getPerspectiveCallback().activatePerspective(navigatorPerspective);
        navigatorPerspective.showNewURLTab("New Analysis View", "New Analysis View", newAnalysisViewURL);
        navigatorPerspective.refreshPerspective(false);
      }
    };

    IDialogValidatorCallback validatorCallback = new IDialogValidatorCallback() {
      public boolean validate() {
        return analysisDialog.validate();
      }
    };

    analysisDialog.setValidatorCallback(validatorCallback);
    analysisDialog.setCallback(callback);
    analysisDialog.center();
  }
}
