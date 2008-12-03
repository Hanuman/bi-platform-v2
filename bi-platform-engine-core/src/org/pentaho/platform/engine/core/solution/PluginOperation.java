package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IPluginOperation;

public class PluginOperation implements IPluginOperation {

  private String name;
  
  private String command;
  
  public PluginOperation( String name, String command ) {
    this.name = name;
    this.command = command;
  }
  
  public String getCommand() {
    return command;
  }

  public String getName() {
    return name;
  }

}
