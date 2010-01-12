package org.pentaho.platform.repository.pcr.jcr;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository.pcr.RepositoryPaths;

/**
 * Default implementation of {@link ILockTokenHelper}. If user {@code suzy} in tenant {@code acme} locks a file with 
 * UUID {@code abc} then this implementation will store the lock token {@code xyz} as 
 * {@code /pentaho/acme/home/suzy/.lockTokens/abc/xyz}. It is assumed that {@code /pentaho/acme/home/suzy} is never 
 * versioned! Putting lock token storage beneath the user's home folder provides access control.
 * 
 * @author mlowery
 */
public class DefaultLockTokenHelper implements ILockTokenHelper {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultLockTokenHelper.class);

  private final String FOLDER_NAME_LOCK_TOKENS = ".lockTokens"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public DefaultLockTokenHelper() {
    super();
  }

  // ~ Methods =========================================================================================================

  public void addLockToken(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, pentahoJcrConstants);
    Node newLockTokenNode = lockTokensNode.addNode(lock.getNode().getUUID(), pentahoJcrConstants
        .getPHO_NT_LOCKTOKENSTORAGE());
    newLockTokenNode.setProperty(pentahoJcrConstants.getPHO_LOCKEDNODEREF(), lock.getNode());
    newLockTokenNode.setProperty(pentahoJcrConstants.getPHO_LOCKTOKEN(), lock.getLockToken());
  }

  public List<String> getLockTokens(final Session session, final PentahoJcrConstants pentahoJcrConstants)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, pentahoJcrConstants);
    NodeIterator nodes = lockTokensNode.getNodes();
    List<String> lockTokens = new ArrayList<String>();
    while (nodes.hasNext()) {
      lockTokens.add(nodes.nextNode().getProperty(pentahoJcrConstants.getPHO_LOCKTOKEN()).getString());
    }
    return lockTokens;
  }

  public void removeLockToken(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Lock lock)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, pentahoJcrConstants);
    NodeIterator nodes = lockTokensNode.getNodes();
    while (nodes.hasNext()) {
      Node node = nodes.nextNode();
      if (node.getName().equals(lock.getNode().getUUID())) {
        node.remove();
      }
    }
  }

  private Node getOrCreateLockTokensNode(final Session session, final PentahoJcrConstants pentahoJcrConstants)
      throws RepositoryException {
    Node userHomeFolderNode = (Node) session.getItem(RepositoryPaths.getUserHomeFolderPath());

    if (userHomeFolderNode.hasNode(FOLDER_NAME_LOCK_TOKENS)) {
      return userHomeFolderNode.getNode(FOLDER_NAME_LOCK_TOKENS);
    } else {
      Node lockTokensNode = userHomeFolderNode.addNode(FOLDER_NAME_LOCK_TOKENS, pentahoJcrConstants
          .getPHO_NT_INTERNALFOLDER());
      return lockTokensNode;
    }
  }

}
