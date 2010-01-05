package org.pentaho.platform.repository.pcr.jcr;

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

/**
 * Helper class that stores, retrieves, and removes lock tokens. In section 8.4.7 of the JSR-170 specification, it 
 * states, "the user must additionally ensure that a reference to the lock token is preserved separately so that it 
 * can later be attached to another session." This manual step is necessary when using open-scoped locks and this 
 * implementation uses open-scoped locks exclusively.
 * 
 * @author mlowery
 */
public interface ILockTokenHelper {
  /**
   * Stores a lock token associated with the session's user.
   * 
   * @param session session whose userID will be used
   * @param pentahoJcrConstants constants
   * @param lock recently created lock; can get the locked node and lock token from this object
   */
  void addLockToken(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock)
      throws RepositoryException;

  /**
   * Returns all lock tokens belonging to the session's user. Lock tokens can then be added to the session by calling
   * {@code Session.addLockToken(token)}.
   * 
   * @param session session whose userID will be used
   * @param pentahoJcrConstants constants
   * @return list of tokens
   */
  List<String> getLockTokens(final Session session, final PentahoJcrConstants pentahoJcrConstants)
      throws RepositoryException;

  /**
   * Removes a lock token
   * 
   * @param session session whose userID will be used
   * @param pentahoJcrConstants constants
   * @param lock lock whose token is to be removed; can get the locked node and lock token from this object
   */
  void removeLockToken(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock)
      throws RepositoryException;
}