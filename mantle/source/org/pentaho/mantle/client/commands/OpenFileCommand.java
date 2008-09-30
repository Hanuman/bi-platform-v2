package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;

public class OpenFileCommand implements Command {

  private static String lastPath = "/"; //$NON-NLS-1$
  
  SolutionBrowserPerspective navigatorPerspective;

  public OpenFileCommand(SolutionBrowserPerspective navigatorPerspective) {
    this.navigatorPerspective = navigatorPerspective;
  }

  public void execute() {
    final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.OPEN, lastPath, navigatorPerspective.getSolutionDocument(), false, true);
    if (!MantleApplication.showAdvancedFeatures) {
      dialog.setShowSearch(false);
    }
    dialog.addFileChooserListener(new FileChooserListener() {

      public void fileSelected(String solution, String path, String name, String localizedFileName) {
        dialog.hide();
        lastPath = "/" + solution + path; //$NON-NLS-1$
        navigatorPerspective.openFile("/" + solution + path, name, localizedFileName, SolutionBrowserPerspective.OPEN_METHOD.OPEN); //$NON-NLS-1$
      }

      public void fileSelectionChanged(String solution, String path, String name) {
      }
    });
    dialog.center();
  }
}
