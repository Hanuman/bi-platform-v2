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

import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.SolutionDocumentManager;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective.OPEN_METHOD;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;

public class OpenFileCommand extends AbstractCommand {

  private static String lastPath = "/"; //$NON-NLS-1$

  private SolutionBrowserPerspective.OPEN_METHOD openMethod = SolutionBrowserPerspective.OPEN_METHOD.OPEN;

  public OpenFileCommand() {
  }

  public OpenFileCommand(final SolutionBrowserPerspective.OPEN_METHOD openMethod) {
    this.openMethod = openMethod;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    final SolutionBrowserPerspective solutionBrowserPerspective = SolutionBrowserPerspective.getInstance();

    SolutionDocumentManager.getInstance().fetchSolutionDocument(new AsyncCallback<Document>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Document result) {
        final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.OPEN, lastPath, result, false, true);
        if (!MantleApplication.showAdvancedFeatures) {
          dialog.setShowSearch(false);
        }
        dialog.addFileChooserListener(new FileChooserListener() {

          public void fileSelected(String solution, String path, String name, String localizedFileName) {
            dialog.hide();
            lastPath = "/" + solution + path; //$NON-NLS-1$
            if (name.contains("analysis.xaction") && openMethod.equals(OPEN_METHOD.EDIT)) { //$NON-NLS-1$
              solutionBrowserPerspective.openFile("/" + solution + path, name, localizedFileName, SolutionBrowserPerspective.OPEN_METHOD.OPEN); //$NON-NLS-1$
            } else {
              solutionBrowserPerspective.openFile("/" + solution + path, name, localizedFileName, openMethod); //$NON-NLS-1$
            }
          }

          public void fileSelectionChanged(String solution, String path, String name) {
          }
        });
        dialog.center();
      }
    }, false);
  }
}
