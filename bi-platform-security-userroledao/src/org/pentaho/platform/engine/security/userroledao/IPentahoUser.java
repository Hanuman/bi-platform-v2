package org.pentaho.platform.engine.security.userroledao;

import java.io.Serializable;
import java.util.Set;

/**
 * A user of the Pentaho platform. Contains a set of roles for which this user is a member.
 * 
 * @author mlowery
 */
public interface IPentahoUser extends Serializable {

  String getUsername();

  Set<IPentahoRole> getRoles();

  void setRoles(Set<IPentahoRole> roles);

  /**
   * @return Same meaning as Set.add return value.
   */
  boolean addRole(IPentahoRole role);

  /**
   * @return Same meaning as Set.remove return value.
   */
  boolean removeRole(IPentahoRole role);

  void clearRoles();

  String getPassword();

  void setPassword(String password);

  boolean isEnabled();

  void setEnabled(boolean enabled);

  String getDescription();

  void setDescription(String description);
}
