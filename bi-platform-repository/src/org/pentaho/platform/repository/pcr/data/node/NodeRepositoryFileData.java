package org.pentaho.platform.repository.pcr.data.node;

import org.pentaho.platform.api.repository.IRepositoryFileData;

public class NodeRepositoryFileData implements IRepositoryFileData {

  private static final long serialVersionUID = 3986247263739435232L;

  private DataNode node;

  public NodeRepositoryFileData(DataNode node) {
    super();
    this.node = node;
  }
  
  public DataNode getNode() {
    return node;
  }
  
}
