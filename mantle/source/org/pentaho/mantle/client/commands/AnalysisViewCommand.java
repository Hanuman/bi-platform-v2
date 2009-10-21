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
import org.pentaho.mantle.client.dialogs.AnalysisViewDialog;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

public class AnalysisViewCommand extends AbstractCommand {

  public AnalysisViewCommand() {
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    final SolutionBrowserPerspective navigatorPerspective = SolutionBrowserPerspective.getInstance();
    final AnalysisViewDialog analysisDialog = new AnalysisViewDialog(navigatorPerspective.getSolutionDocument());
    IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
      }

      public void okPressed() {
        // without the timer, this code would cause a crash in IE6 and IE7
        Timer timer = new Timer() {
          @Override
          public void run() {
            String actionName = System.currentTimeMillis() + ".analysisview.xaction"; //$NON-NLS-1$
            String newAnalysisViewURL = "AnalysisViewService?component=createNewView&name=" + actionName + "&descr=" + actionName + "&actionName=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + actionName + "&textfield=&schema=" + analysisDialog.getSchema() + "&cube=" + analysisDialog.getCube() + "&solution=system&actionPath=tmp"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (!GWT.isScript()) {
              newAnalysisViewURL = "http://localhost:8080/pentaho/" + newAnalysisViewURL + "&userid=joe&password=password"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            navigatorPerspective.showNewURLTab(Messages.getString("newAnalysisView"), Messages.getString("newAnalysisView"), newAnalysisViewURL); //$NON-NLS-1$ //$NON-NLS-2$
            
            //Set it to save-enabled and fire event
            navigatorPerspective.getCurrentFrame().setSaveEnabled(true);
            navigatorPerspective.fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.OPEN, SolutionBrowserPerspective.CURRENT_SELECTED_TAB);
            
            //navigatorPerspective.refreshPerspective(false);
          }
        };
        timer.schedule(1);
      }
    };

    IDialogValidatorCallback validatorCallback = new IDialogValidatorCallback() {
      public boolean validate() {
        return analysisDialog.validate();
      }
    };

    analysisDialog.setValidatorCallback(validatorCallback);
    analysisDialog.setCallback(callback);
    //
    // Commented out analysis view check, JPivot now supports multiple views
    // in a single session.  Leaving the code here during testing phase.
    //
    
//    final Widget openAnalysisView = navigatorPerspective.getOpenAnalysisView();
//    if (openAnalysisView != null) {
//      String actionName = navigatorPerspective.getTabForWidget(openAnalysisView).getText();
//      Widget content = new HTML(Messages.getString("analysisViewIsOpen", actionName)); //$NON-NLS-1$
//      PromptDialogBox dialog = new PromptDialogBox(Messages.getString("open"), Messages.getString("ok"), Messages.getString("cancel"), false, true, content); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//      dialog.setCallback(new IDialogCallback() {
//
//        public void cancelPressed() {
//          // TODO Auto-generated method stub
//          
//        }
//
//        public void okPressed() {
//          navigatorPerspective.getContentTabPanel().remove(openAnalysisView);
//          analysisDialog.center();
//        }
//        
//      });
//      dialog.center();
//      dialog.show();
//
//    } else {
    analysisDialog.center();
//    }
  }
}
