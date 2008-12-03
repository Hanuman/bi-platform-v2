package org.pentaho.platform.api.engine;

public interface IPluginOperation {

  /**
   * Gets the id for this operation. There is a set of standard ids, e.g.
   * RUN, EDIT, DELETE etc. The id is not an enum so that the list of
   * operations can be extended by plug-ins
   * @return The operation id
   */
  public String getId();
  
  /**
   * Gets the command for this operation. The commands are typically URLs
   * @return The operation command
   */
  public String getCommand();
  
}
