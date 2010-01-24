package org.pentaho.platform.repository.pcr.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.repository.pcr.data.node.DataNode;
import org.pentaho.platform.repository.pcr.data.node.DataNodeRef;
import org.pentaho.platform.repository.pcr.data.node.DataProperty;
import org.pentaho.platform.repository.pcr.data.node.NodeRepositoryFileData;

public class NodeRepositoryFileDataAdapter extends XmlAdapter<JaxbSafeNodeRepositoryFileData, NodeRepositoryFileData> {

  @Override
  public JaxbSafeNodeRepositoryFileData marshal(final NodeRepositoryFileData v) throws Exception {
    System.out.println("incoming data: " + v);
    JaxbSafeNodeRepositoryFileData d = new JaxbSafeNodeRepositoryFileData();
    JaxbSafeDataNode node = new JaxbSafeDataNode();
    d.node = node;
    toJaxbSafeDataNode(node, v.getNode());
    System.out.println("outgoing jaxb data: " + d);
    return d;
  }

  protected void toJaxbSafeDataNode(final JaxbSafeDataNode jaxbNode, final DataNode node) {
    jaxbNode.name = node.getName();
    if (node.getId() != null) {
      jaxbNode.id = node.getId().toString();
    }
    List<JaxbSafeDataProperty> jaxbProps = new ArrayList<JaxbSafeDataProperty>();
    for (DataProperty prop : node.getProperties()) {
      JaxbSafeDataProperty jaxbProp = new JaxbSafeDataProperty();
      jaxbProp.name = prop.getName();
      jaxbProp.type = prop.getType();
      switch (prop.getType()) {
        case BOOLEAN: {
          jaxbProp.value = prop.getBoolean();
          break;
        }
        case DATE: {
          jaxbProp.value = prop.getDate();
          break;
        }
        case DOUBLE: {
          jaxbProp.value = prop.getDouble();
          break;
        }
        case LONG: {
          jaxbProp.value = prop.getLong();
          break;
        }
        case STRING: {
          jaxbProp.value = prop.getString();
          break;
        }
        case REF: {
          jaxbProp.value = prop.getString();
          break;
        }
        default: {
          throw new IllegalArgumentException();
        }
      }
      jaxbProps.add(jaxbProp);
    }
    jaxbNode.childProperties = jaxbProps.toArray(new JaxbSafeDataProperty[jaxbProps.size()]);
    List<JaxbSafeDataNode> jaxbNodes = new ArrayList<JaxbSafeDataNode>();
    for (DataNode childNode : node.getNodes()) {
      JaxbSafeDataNode child = new JaxbSafeDataNode();
      jaxbNodes.add(child);
      toJaxbSafeDataNode(child, childNode);
    }
    jaxbNode.childNodes = jaxbNodes.toArray(new JaxbSafeDataNode[jaxbNodes.size()]);
  }

  @Override
  public NodeRepositoryFileData unmarshal(final JaxbSafeNodeRepositoryFileData v) throws Exception {
    System.out.println("incoming jaxb data: " + v);
    DataNode node = toDataNode(v.node);
    NodeRepositoryFileData data = new NodeRepositoryFileData(node);
    System.out.println("outgoing data: " + data);
    return data;
  }

  protected DataNode toDataNode(final JaxbSafeDataNode jaxbNode) {
    DataNode node = new DataNode(jaxbNode.name);
    node.setId(jaxbNode.id);

    for (JaxbSafeDataProperty jaxbProp : jaxbNode.childProperties) {
      switch (jaxbProp.type) {
        case BOOLEAN: {
          node.setProperty(jaxbProp.name, (Boolean) jaxbProp.value);
          break;
        }
        case DATE: {
          node.setProperty(jaxbProp.name, (Date) jaxbProp.value);
          break;
        }
        case DOUBLE: {
          node.setProperty(jaxbProp.name, (Double) jaxbProp.value);
          break;
        }
        case LONG: {
          node.setProperty(jaxbProp.name, (Long) jaxbProp.value);
          break;
        }
        case STRING: {
          node.setProperty(jaxbProp.name, (String) jaxbProp.value);
          break;
        }
        case REF: {
          node.setProperty(jaxbProp.name, new DataNodeRef((String) jaxbProp.value));
          break;
        }
        default: {
          throw new IllegalArgumentException();
        }
      }
    }

    for (JaxbSafeDataNode jaxbChildNode : jaxbNode.childNodes) {
      node.addNode(toDataNode(jaxbChildNode));
    }

    return node;
  }

}
