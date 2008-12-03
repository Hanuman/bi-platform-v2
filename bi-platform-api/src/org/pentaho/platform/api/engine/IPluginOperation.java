package org.pentaho.platform.api.engine;

public interface IPluginOperation {

  /**
   * Gets the name for this operation. There is a set of standard names, e.g.
   * RUN, EDIT, DELETE etc. The name is not an enum so that the list of
   * operations can be extended by plug-ins
   * @return The operation name
   */
  public String getName();
  
  /**
   * Gets the command for this operation. The commands are typically URLs
   * @return The operation command
   */
  public String getCommand();
  
}
