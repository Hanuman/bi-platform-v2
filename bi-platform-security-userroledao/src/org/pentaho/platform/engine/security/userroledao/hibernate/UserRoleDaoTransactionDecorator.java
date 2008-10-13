package org.pentaho.platform.engine.security.userroledao.hibernate;

import java.util.List;

import org.pentaho.platform.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Wraps a {@link IUserRoleDao}, beginning, committing, and rolling back transactions before and after each operation.
 * 
 * <p>Why not just do the transactions in the DAO implementation?  Because transactions are a 
 * <a href="http://en.wikipedia.org/wiki/Cross-cutting_concern">cross-cutting concern</a>, an aspect that is often 
 * scattered throughout the code but is best separated from other code.</p>
 * 
 * @author mlowery
 */
public class UserRoleDaoTransactionDecorator implements IUserRoleDao {

  /**
   * Spring's transaction template that begins and commits a transaction, and automatically rolls back on a runtime
   * exception. Recommended configuration for this bean: <code>propagationBehavior</code> set to 
   * <code>TransactionDefinition.PROPAGATION_REQUIRES_NEW)</code>. 
   */
  private TransactionTemplate transactionTemplate;

  /**
   * The wrapped DAO to which to delegate.
   */
  private IUserRoleDao dao;

  public void createRole(final IPentahoRole roleToCreate) throws AlreadyExistsException,
      UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        dao.createRole(roleToCreate);
      }
    });
  }

  public void createUser(final IPentahoUser userToCreate) throws AlreadyExistsException,
      UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        dao.createUser(userToCreate);
      }
    });
  }

  public void deleteRole(final IPentahoRole roleToDelete) throws NotFoundException, UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        dao.deleteRole(roleToDelete);
      }
    });
  }

  public void deleteUser(final IPentahoUser userToDelete) throws NotFoundException, UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        dao.deleteUser(userToDelete);
      }
    });
  }

  public IPentahoRole getRole(final String name) throws UncategorizedUserRoleDaoException {
    return (IPentahoRole) transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return dao.getRole(name);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException {
    return (List<IPentahoRole>) transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return dao.getRoles();
      }
    });
  }

  public IPentahoUser getUser(final String username) throws UncategorizedUserRoleDaoException {
    return (IPentahoUser) transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return dao.getUser(username);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException {
    return (List<IPentahoUser>) transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return dao.getUsers();
      }
    });
  }

  public void updateRole(final IPentahoRole roleToUpdate) throws NotFoundException, UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        dao.updateRole(roleToUpdate);
      }
    });
  }

  public void updateUser(final IPentahoUser userToUpdate) throws NotFoundException, UncategorizedUserRoleDaoException {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        dao.updateUser(userToUpdate);
      }
    });
  }

  public void setTransactionTemplate(final TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  public void setDao(final IUserRoleDao dao) {
    this.dao = dao;
  }

}
