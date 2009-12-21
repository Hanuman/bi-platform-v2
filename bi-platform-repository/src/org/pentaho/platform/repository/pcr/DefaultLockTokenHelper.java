package org.pentaho.platform.repository.pcr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository.pcr.JcrPentahoContentDao.ILockTokenHelper;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

public class DefaultLockTokenHelper implements ILockTokenHelper {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultLockTokenHelper.class);

  private final String PATTERN_USER_HOME_FOLDER_PATH = "/pentaho/{0}/home/{1}";

  private final String FOLDER_NAME_LOCK_TOKENS = ".lockTokens";

  private final String PATTERN_QUERY_LOCK_TOKEN = "/jcr:root" + PATTERN_USER_HOME_FOLDER_PATH + "/"
      + FOLDER_NAME_LOCK_TOKENS + "/{2}";

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public DefaultLockTokenHelper() {
    super();
  }

  // ~ Methods =========================================================================================================

  private String internalGetTenantId() {
    return "acme";
  }

  private String internalGetUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Assert.state(auth != null);

    if (auth.getPrincipal() instanceof UserDetails) {
      return ((UserDetails) auth.getPrincipal()).getUsername();
    } else {
      return auth.getPrincipal().toString();
    }
  }

  private Node getOrCreateLockTokensNode(final Session session, final NodeIdStrategy nodeIdStrategy)
      throws RepositoryException {
    String tenantId = internalGetTenantId();
    String username = internalGetUsername();
    Item item = session.getItem(MessageFormat.format(PATTERN_USER_HOME_FOLDER_PATH, tenantId, username));
    Assert.isTrue(item.isNode());
    Node userHomeFolderNode = (Node) item;
    if (userHomeFolderNode.hasNode(FOLDER_NAME_LOCK_TOKENS)) {
      return userHomeFolderNode.getNode(FOLDER_NAME_LOCK_TOKENS);
    } else {
      JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, nodeIdStrategy, userHomeFolderNode);
      Node lockTokensNode = userHomeFolderNode.addNode(FOLDER_NAME_LOCK_TOKENS, JcrRepositoryFileUtils
          .addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_INTERNALFOLDER));
      session.save();
      JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, nodeIdStrategy, userHomeFolderNode);
      return lockTokensNode;
    }
  }

  public void addLockToken(final Session session, final NodeIdStrategy nodeIdStrategy, final Lock lock)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, nodeIdStrategy);
    JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, nodeIdStrategy, lockTokensNode);
    Node newLockTokenNode = lockTokensNode.addNode(lock.getNode().getUUID(), JcrRepositoryFileUtils.addPentahoPrefix(
        session, PentahoJcrConstants.PENTAHO_LOCKTOKENSTORAGE));
    newLockTokenNode.setProperty(JcrRepositoryFileUtils.addPentahoPrefix(session,
        PentahoJcrConstants.PENTAHO_LOCKEDNODEREF), lock.getNode());
    newLockTokenNode.setProperty(JcrRepositoryFileUtils
        .addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_LOCKTOKEN), lock.getLockToken());
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, nodeIdStrategy, lockTokensNode);
  }

  public List<String> getLockTokens(final Session session, final NodeIdStrategy nodeIdStrategy)
      throws RepositoryException {
    Node lockTokensNode = getOrCreateLockTokensNode(session, nodeIdStrategy);
    NodeIterator nodes = lockTokensNode.getNodes();
    List<String> lockTokens = new ArrayList<String>();
    while (nodes.hasNext()) {
      lockTokens.add(nodes.nextNode().getProperty(
          JcrRepositoryFileUtils.addPentahoPrefix(session, PentahoJcrConstants.PENTAHO_LOCKTOKEN)).getString());
    }
    return lockTokens;
  }

  public void removeLockToken(final Session session, final NodeIdStrategy nodeIdStrategy, final Lock lock)
      throws RepositoryException {
    String tenantId = internalGetTenantId();
    String username = internalGetUsername();
    Node lockTokensNode = getOrCreateLockTokensNode(session, nodeIdStrategy);
    Query query = session.getWorkspace().getQueryManager().createQuery(
        MessageFormat.format(PATTERN_QUERY_LOCK_TOKEN, tenantId, username, lock.getNode().getUUID()), Query.XPATH);
    QueryResult queryResult = query.execute();
    NodeIterator nodes = queryResult.getNodes();
    JcrRepositoryFileUtils.checkoutNearestVersionableNodeIfNecessary(session, nodeIdStrategy, lockTokensNode);
    while (nodes.hasNext()) {
      nodes.nextNode().remove();
    }
    session.save();
    JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, nodeIdStrategy, lockTokensNode);
  }
}
