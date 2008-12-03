package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IPluginOperation;

public class PluginOperation implements IPluginOperation {

  private String id;
  
  private String command;
  
  public PluginOperation( String id, String command ) {
    this.id = id;
    this.command = command;
  }

  public String getId() {
    return id;
  }
  
  public String getCommand() {
    return command;
  }

}
