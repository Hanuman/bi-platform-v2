/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.dialogs.AnalysisViewDialog;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

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
        String actionName = System.currentTimeMillis() + ".analysisview.xaction"; //$NON-NLS-1$
        String newAnalysisViewURL = "AnalysisViewService?component=createNewView&name=" + actionName + "&descr=" + actionName + "&actionName=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + actionName + "&textfield=&schema=" + analysisDialog.getSchema() + "&cube=" + analysisDialog.getCube() + "&solution=system&actionPath=tmp"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (!GWT.isScript()) {
          newAnalysisViewURL = "http://localhost:8080" + newAnalysisViewURL + "&userid=joe&password=password"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        navigatorPerspective.getPerspectiveCallback().activatePerspective(navigatorPerspective);
        navigatorPerspective.showNewURLTab(Messages.getInstance().newAnalysisView(), Messages.getInstance().newAnalysisView(), newAnalysisViewURL);
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

    final Widget openAnalysisView = navigatorPerspective.getOpenAnalysisView();
    if (openAnalysisView != null) {
      String actionName = navigatorPerspective.getTabForWidget(openAnalysisView).getText();
      Widget content = new HTML(Messages.getInstance().analysisViewIsOpen(actionName));
      PromptDialogBox dialog = new PromptDialogBox(Messages.getInstance().open(), Messages.getInstance().ok(), Messages.getInstance().cancel(), false, true, content);
      dialog.setCallback(new IDialogCallback() {

        public void cancelPressed() {
          // TODO Auto-generated method stub
          
        }

        public void okPressed() {
          navigatorPerspective.getContentTabPanel().remove(openAnalysisView);
          analysisDialog.center();
        }
        
      });
      dialog.center();
      dialog.show();

    } else {
      analysisDialog.center();
    }
  }
}
