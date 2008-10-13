package org.pentaho.platform.engine.security.userroledao;

import java.io.Serializable;
import java.util.Set;

/**
 * A role in the Pentaho platform. Contains a set of users to which the role is assigned. A role is also known as an 
 * authority.
 * 
 * @author mlowery
 */
public interface IPentahoRole extends Serializable {

  String getName();

  String getDescription();

  void setDescription(String description);

  Set<IPentahoUser> getUsers();

  void setUsers(Set<IPentahoUser> users);

  /**
   * @return Same meaning as Set.add return value.
   */
  boolean addUser(IPentahoUser user);

  /**
   * @return Same meaning as Set.remove return value.
   */
  boolean removeUser(IPentahoUser user);

  void clearUsers();
}
