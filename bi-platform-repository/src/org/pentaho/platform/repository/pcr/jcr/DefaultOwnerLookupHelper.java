package org.pentaho.platform.repository.pcr.jcr;

import java.security.Principal;
import java.security.acl.Group;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.core.SessionImpl;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.springframework.util.Assert;

/**
 * Default {@code IOwnerLookupHelper} implementation. Uses Jackrabbit-specific node types. Uses low-level node 
 * operations to keep the fetching of the owner fast. (Otherwise, we could have used the {@code AccessControlManager} 
 * API but that would entail fetching the entire ACL along with its ACEs.)
 * 
 * <p>
 * This implementation fails silently (but returns {@code null}) when there is no ACL yet applied to the node.
 * </p>
 * 
 * @author mlowery
 */
public class DefaultOwnerLookupHelper implements IOwnerLookupHelper {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public DefaultOwnerLookupHelper() {
    super();
  }

  // ~ Methods =========================================================================================================

  public RepositoryFileSid getOwner(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node node) throws RepositoryException {
    RepositoryFileSid owner = null;
    // TODO mlowery use proper namespaces
    if (node.hasNode("rep:policy")) {
      Node aclNode = node.getNode("rep:policy");
      String aclOwnerName = aclNode.getProperty(pentahoJcrConstants.getPHO_ACLOWNERNAME()).getString();

      Assert.isTrue(session instanceof SessionImpl);
      SessionImpl jrSession = (SessionImpl) session;

      Principal ownerPrincipal = jrSession.getPrincipalManager().getPrincipal(aclOwnerName);

      if (ownerPrincipal instanceof Group) {
        owner = new RepositoryFileSid(ownerPrincipal.getName(), RepositoryFileSid.Type.ROLE);
      } else {
        owner = new RepositoryFileSid(ownerPrincipal.getName(), RepositoryFileSid.Type.USER);
      }
    }
    return owner;
  }

}
