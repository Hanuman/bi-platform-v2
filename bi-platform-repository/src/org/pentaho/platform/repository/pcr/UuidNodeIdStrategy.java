package org.pentaho.platform.repository.pcr;

import java.io.Serializable;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.springframework.extensions.jcr.JcrTemplate;

/**
 * {@link NodeIdStrategy} that uses node UUIDs.
 * 
 * @author mlowery
 */
public class UuidNodeIdStrategy implements NodeIdStrategy {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private final JcrTemplate jcrTemplate;
  
  // ~ Constructors ====================================================================================================

  public UuidNodeIdStrategy(JcrTemplate jcrTemplate) {
    this.jcrTemplate = jcrTemplate;
  }

  // ~ Methods =========================================================================================================

  /**
   * Returns UUID of node. Obviously won't work on nodes that are not mix:referenceable.
   */
  public Serializable getId(final Node node) {
    try {
      return node.getUUID();
    } catch (RepositoryException e) {
      throw jcrTemplate.convertJcrAccessException(e);
    }
  }

  /**
   * Adds mix:referenceable to node. Node will get an actual id on save.
   */
  public void setId(final Node node, final Serializable ignored) {
    try {
      node.addMixin(PentahoJcrConstants.MIX_REFERENCEABLE);
    } catch (RepositoryException e) {
      throw jcrTemplate.convertJcrAccessException(e);
    }
  }

  /**
   * Uses {@code session.getNodeByUUID}.
   */
  public Node findNodeById(final Session session, final Serializable id) {
    try {
      return session.getNodeByUUID(id.toString());
    } catch (ItemNotFoundException e) {
      return null;
    } catch (RepositoryException e) {
      throw jcrTemplate.convertJcrAccessException(e);
    }
  }
}