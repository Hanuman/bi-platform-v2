package org.pentaho.platform.repository.pcr.data.node;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataNode {

  public enum DataPropertyType {
    BOOLEAN, DATE, DOUBLE, LONG, STRING, REF
  };
  
  private String name;

  private Map<String, DataNode> nodeNameToNodeMap = new HashMap<String, DataNode>();

  private Map<String, DataProperty> propNameToPropMap = new HashMap<String, DataProperty>();

  public DataNode(final String name) {
    super();
    this.name = name;
  }

  public DataNode addNode(final String name) {
    DataNode child = new DataNode(name);
    nodeNameToNodeMap.put(child.getName(), child);
    return child;
  }

  public Iterable<DataNode> getNodes() {
    return nodeNameToNodeMap.values();
  }

  public DataNode getNode(final String name) {
    return nodeNameToNodeMap.get(name);
  }

  public String getName() {
    return name;
  }

  public void setProperty(final String name, String value) {
    propNameToPropMap.put(name, new DataProperty(name, value, DataPropertyType.STRING));
  }

  public void setProperty(final String name, boolean value) {
    propNameToPropMap.put(name, new DataProperty(name, value, DataPropertyType.BOOLEAN));
  }

  public void setProperty(final String name, double value) {
    propNameToPropMap.put(name, new DataProperty(name, value, DataPropertyType.DOUBLE));
  }

  public void setProperty(final String name, long value) {
    propNameToPropMap.put(name, new DataProperty(name, value, DataPropertyType.LONG));
  }

  public void setProperty(final String name, Date value) {
    propNameToPropMap.put(name, new DataProperty(name, value, DataPropertyType.DATE));
  }

  public boolean hasProperty(final String name) {
    return propNameToPropMap.containsKey(name);
  }
  
  public boolean hasNode(final String name) {
    return nodeNameToNodeMap.containsKey(name);
  }
  
  public DataProperty getProperty(final String name) {
    return propNameToPropMap.get(name);
  }
  
  public Iterable<DataProperty> getProperties() {
    return propNameToPropMap.values();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((nodeNameToNodeMap == null) ? 0 : nodeNameToNodeMap.hashCode());
    result = prime * result + ((propNameToPropMap == null) ? 0 : propNameToPropMap.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DataNode other = (DataNode) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (nodeNameToNodeMap == null) {
      if (other.nodeNameToNodeMap != null)
        return false;
    } else if (!nodeNameToNodeMap.equals(other.nodeNameToNodeMap))
      return false;
    if (propNameToPropMap == null) {
      if (other.propNameToPropMap != null)
        return false;
    } else if (!propNameToPropMap.equals(other.propNameToPropMap))
      return false;
    return true;
  }

  
}
