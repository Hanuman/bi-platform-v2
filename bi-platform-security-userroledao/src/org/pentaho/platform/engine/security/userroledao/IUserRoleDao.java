package org.pentaho.platform.engine.security.userroledao;

import java.util.List;

/**
 * Contract for data access objects that read and write users and roles.
 * 
 * @author mlowery
 */
public interface IUserRoleDao {

  void createUser(IPentahoUser newUser) throws AlreadyExistsException, UncategorizedUserRoleDaoException;

  void deleteUser(IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException;

  IPentahoUser getUser(String name) throws UncategorizedUserRoleDaoException;

  List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException;

  void updateUser(IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException;

  void createRole(IPentahoRole newRole) throws AlreadyExistsException, UncategorizedUserRoleDaoException;

  void deleteRole(IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException;

  IPentahoRole getRole(String name) throws UncategorizedUserRoleDaoException;

  List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException;

  void updateRole(IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException;

}
