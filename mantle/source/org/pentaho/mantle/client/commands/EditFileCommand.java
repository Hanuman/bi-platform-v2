package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;

public class EditFileCommand implements Command {

  private static String lastPath = "/"; //$NON-NLS-1$

  SolutionBrowserPerspective solutionBrowserPerspective;

  public EditFileCommand(SolutionBrowserPerspective solutionBrowserPerspective) {
    this.solutionBrowserPerspective = solutionBrowserPerspective;
  }

  public void execute() {
    final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.OPEN, lastPath, solutionBrowserPerspective.getSolutionDocument(), false, true);
    if (!MantleApplication.showAdvancedFeatures) {
      dialog.setShowSearch(false);
    }
    dialog.addFileChooserListener(new FileChooserListener() {

      public void fileSelected(String solution, String path, String name, String localizedFileName) {
        dialog.hide();
        lastPath = "/" + solution + path; //$NON-NLS-1$
        if (name.contains("analysisview.xaction")) { //$NON-NLS-1$
          solutionBrowserPerspective.openFile("/" + solution + path, name, localizedFileName, SolutionBrowserPerspective.OPEN_METHOD.OPEN); //$NON-NLS-1$
        } else {
          solutionBrowserPerspective.openFile("/" + solution + path, name, localizedFileName, SolutionBrowserPerspective.OPEN_METHOD.EDIT); //$NON-NLS-1$
        }
      }

      public void fileSelectionChanged(String solution, String path, String name) {
      }
    });
    dialog.center();
  }
}
