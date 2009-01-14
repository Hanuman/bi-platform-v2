package org.pentaho.mantle.client.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;

import com.google.gwt.xml.client.Document;

/*
 * Convenience class for showing FileChooserDialog while maintaining a last browsed location.
 * 
 */
public class FileDialog{

  private static String lastPath = "";
  private List<FileChooserListener> listeners = new ArrayList<FileChooserListener>();
  private Document doc;
  private String title, okText;
  
  public FileDialog(Document doc, String title, String okText){
    this.doc = doc;
    this.title = title;
    this.okText = okText;
  }
  
  public void show(){
    final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.OPEN, lastPath, doc, false, true, title, okText);
    dialog.addFileChooserListener(new FileChooserListener() {

      public void fileSelected(String solution, String path, String name, String localizedFileName) {
        dialog.hide();
        FileDialog.lastPath = path;
        for(FileChooserListener listener : listeners){
          listener.fileSelected(solution, path, name, localizedFileName);
        }
      }

      public void fileSelectionChanged(String solution, String path, String name) {
      }
    });
    dialog.center();
  }
  
  public void addFileChooserListener(FileChooserListener listener){
    if(!listeners.contains(listeners)){
      listeners.add(listener);
    }
  }
}
