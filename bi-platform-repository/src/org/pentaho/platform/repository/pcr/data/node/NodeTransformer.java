package org.pentaho.platform.repository.pcr.data.node;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository.IRepositoryFileData;
import org.pentaho.platform.repository.pcr.jcr.ITransformer;
import org.pentaho.platform.repository.pcr.jcr.PentahoJcrConstants;
import org.springframework.util.Assert;

public class NodeTransformer implements ITransformer<NodeRepositoryFileData> {

  protected void createOrUpdateContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeRepositoryFileData data, final Node fileNode) throws RepositoryException {
    Node unstructuredNode = null;
    if (fileNode.hasNode(pentahoJcrConstants.getJCR_CONTENT())) {
      unstructuredNode = fileNode.getNode(pentahoJcrConstants.getJCR_CONTENT());
    } else {
      unstructuredNode = fileNode.addNode(pentahoJcrConstants.getJCR_CONTENT(), pentahoJcrConstants
          .getPHO_NT_INTERNALFOLDER());
    }

    // clear out all nodes since it's the quickest way to guarantee that existing nodes that should be deleted are 
    // removed
    final String pattern = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":" + "*"; //$NON-NLS-1$ //$NON-NLS-2$
    NodeIterator nodes = unstructuredNode.getNodes(pattern);
    while (nodes.hasNext()) {
      nodes.nextNode().remove();
    }

    internalCreateOrUpdate(session, pentahoJcrConstants, unstructuredNode, data.getNode());
  }

  public void createContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final NodeRepositoryFileData data, final Node fileNode) throws RepositoryException {
    createOrUpdateContentNode(session, pentahoJcrConstants, data, fileNode);
  }

  protected void internalCreateOrUpdate(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node jcrParentNode, final DataNode dataNode) throws RepositoryException {
    final String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
    // get or create the node represented by dataNode
    Node jcrNode = null;
    if (jcrParentNode.hasNode(prefix + dataNode.getName())) {
      jcrNode = jcrParentNode.getNode(prefix + dataNode.getName());
    } else {
      jcrNode = jcrParentNode.addNode(prefix + dataNode.getName(), pentahoJcrConstants.getPHO_NT_INTERNALFOLDER());
    }
    // set any properties represented by dataNode
    for (DataProperty dataProp : dataNode.getProperties()) {
      switch (dataProp.getType()) {
        case STRING: {
          jcrNode.setProperty(prefix + dataProp.getName(), dataProp.getString());
          break;
        }
        case BOOLEAN: {
          jcrNode.setProperty(prefix + dataProp.getName(), dataProp.getBoolean());
          break;
        }
        case DOUBLE: {
          jcrNode.setProperty(prefix + dataProp.getName(), dataProp.getDouble());
          break;
        }
        case LONG: {
          jcrNode.setProperty(prefix + dataProp.getName(), dataProp.getLong());
          break;
        }
        case DATE: {
          Calendar cal = Calendar.getInstance();
          cal.setTime(dataProp.getDate());
          jcrNode.setProperty(prefix + dataProp.getName(), cal);
          break;
        }
        case REF: {
          jcrNode.setProperty(prefix + dataProp.getName(), session.getNodeByUUID(dataProp.getRef().getId().toString()));
          break;
        }
        default: {
          throw new IllegalArgumentException();
        }
      }
    }
    // now process any child nodes of dataNode
    for (DataNode child : dataNode.getNodes()) {
      internalCreateOrUpdate(session, pentahoJcrConstants, jcrNode, child);
    }
  }

  public NodeRepositoryFileData fromContentNode(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node fileNode) throws RepositoryException {
    Node unstructuredNode = fileNode.getNode(pentahoJcrConstants.getJCR_CONTENT());
    final String pattern = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":" + "*"; //$NON-NLS-1$ //$NON-NLS-2$
    Assert.isTrue(unstructuredNode.getNodes(pattern).getSize() == 1);
    Node jcrNode = unstructuredNode.getNodes(pattern).nextNode();
    return new NodeRepositoryFileData(internalRead(session, pentahoJcrConstants, jcrNode, null));
  }

  protected DataNode internalRead(final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Node jcrNode, final DataNode parentDataNode) throws RepositoryException {
    final String prefix = session.getNamespacePrefix(PentahoJcrConstants.PHO_NS) + ":"; //$NON-NLS-1$
    final String pattern = prefix + "*"; //$NON-NLS-1$

    DataNode dataNode = parentDataNode != null ? parentDataNode.addNode(jcrNode.getName().substring(prefix.length()))
        : new DataNode(jcrNode.getName().substring(prefix.length()));
    dataNode.setId(jcrNode.getUUID());

    PropertyIterator props = jcrNode.getProperties(pattern);
    while (props.hasNext()) {
      Property prop = props.nextProperty();
      switch (prop.getType()) {
        case PropertyType.STRING: {
          dataNode.setProperty(prop.getName().substring(prefix.length()), prop.getString());
          break;
        }
        case PropertyType.BOOLEAN: {
          dataNode.setProperty(prop.getName().substring(prefix.length()), prop.getBoolean());
          break;
        }
        case PropertyType.DOUBLE: {
          dataNode.setProperty(prop.getName().substring(prefix.length()), prop.getDouble());
          break;
        }
        case PropertyType.LONG: {
          dataNode.setProperty(prop.getName().substring(prefix.length()), prop.getLong());
          break;
        }
        case PropertyType.DATE: {
          dataNode.setProperty(prop.getName().substring(prefix.length()), prop.getDate().getTime());
          break;
        }
        case PropertyType.REFERENCE: {
          dataNode.setProperty(prop.getName().substring(prefix.length()), new DataNodeRef(prop.getNode().getUUID()));
          break;
        }
        default: {
          throw new IllegalArgumentException();
        }
      }
    }

    // iterate over children
    NodeIterator nodes = jcrNode.getNodes(pattern);
    while (nodes.hasNext()) {
      Node child = nodes.nextNode();
      internalRead(session, pentahoJcrConstants, child, dataNode);
    }

    return dataNode;
  }

  public boolean supports(String extension, Class<? extends IRepositoryFileData> clazz) {
    return clazz.isAssignableFrom(NodeRepositoryFileData.class);
  }

  public void updateContentNode(Session session, PentahoJcrConstants pentahoJcrConstants, NodeRepositoryFileData data,
      Node fileNode) throws RepositoryException {
    createOrUpdateContentNode(session, pentahoJcrConstants, data, fileNode);
  }

}
