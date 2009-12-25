package org.pentaho.platform.repository.pcr.jcr;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.Session;

/**
 * Allows for configurable node id getting, setting, and finding.
 * 
 * @author mlowery
 */
public interface NodeIdStrategy {

  /**
   * Returns the id of the given node.
   * @param node node from which to get id
   * @return id
   */
  Serializable getId(final Node node);

  /**
   * Sets the id of the given node.
   * @param pentahoJcrConstants constants
   * @param node node for which to set id
   * @param id id to set
   */
  void setId(final PentahoJcrConstants pentahoJcrConstants, final Node node, final Serializable id);

  /**
   * Returns the node with the given id.
   * @param session session to use
   * @param id id of node to find
   * @return found node or {@code null} if no such node or access denied
   */
  Node findNodeById(final Session session, final Serializable id);
}