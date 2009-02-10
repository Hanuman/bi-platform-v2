package org.pentaho.mantle.client.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.FileFilter;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;

import com.google.gwt.user.client.Window;
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
  private String[] fileTypes;
  
  public FileDialog(Document doc, String title, String okText, String[] fileTypes){
    this.doc = doc;
    this.title = title;
    this.okText = okText;
    this.fileTypes = fileTypes;
  }
  
  public void show(){
    final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.OPEN, lastPath, doc, false, true, title, okText){

      @Override
      public void hide() {
        super.hide();
        GlassPane.getInstance().hide();
      }
      
      
    };
    dialog.addFileChooserListener(new FileChooserListener() {

      public void fileSelected(String solution, String path, String name, String localizedFileName) {
        dialog.hide();
        
        FileDialog.lastPath = "/"+solution+ path;
        
        
        for(FileChooserListener listener : listeners){
          listener.fileSelected(solution, path, name, localizedFileName);
        }
      }

      public void fileSelectionChanged(String solution, String path, String name) {
      }
    });
    dialog.setFileFilter(new FileFilter(){

      public boolean accept(String name, boolean isDirectory, boolean isVisible) {
        if(isDirectory && isVisible){
          return true;
        }
        if(name.indexOf(".") == -1){
          return false;
        }
        String extension = name.substring(name.lastIndexOf(".")+1);
        
        for(int i=0; i< fileTypes.length; i++){
          if(fileTypes[i].trim().equalsIgnoreCase(extension) && isVisible){
            return true;
          }
        }
        return false;
      }
      
    });
    
    GlassPane.getInstance().show();
    dialog.center();
  }
  
  public void addFileChooserListener(FileChooserListener listener){
    if(!listeners.contains(listeners)){
      listeners.add(listener);
    }
  }
}
