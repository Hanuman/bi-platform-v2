/*
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * Created Sep 15, 2005 
 * @author wseyler
 */

package org.pentaho.platform.plugin.services.connections.xquery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.tinytree.TinyNodeImpl;
import net.sf.saxon.type.Type;

import org.pentaho.commons.connection.AbstractPentahoMetaData;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class XQMetaData extends AbstractPentahoMetaData {
  public static final String DEFAULT_COLUMN_NAME = "Default Column"; //$NON-NLS-1$

  private Object[][] columnHeaders;

  private Object[][] rowHeaders;

  private XQConnection connection;
  
  int rowCount = 0;

  public XQMetaData(final XQConnection xqConnection, final Iterator iter) {
    this.connection = xqConnection;
    List headers = new ArrayList();
    while (iter.hasNext()) {
      rowCount++;
      Object obj = iter.next();
      if (obj instanceof TinyNodeImpl) {
        boolean processedChildren = false;
        AxisIterator aIter = ((TinyNodeImpl) obj).iterateAxis(Axis.DESCENDANT);
        Object descendent = aIter.next();
        while (descendent != null) {
          if ((descendent instanceof TinyNodeImpl) && (((TinyNodeImpl) descendent).getNodeKind() == Type.ELEMENT)) {
            TinyNodeImpl descNode = (TinyNodeImpl) descendent;
            processedChildren = true;
            if (!headers.contains(descNode.getDisplayName())) {
              headers.add(descNode.getDisplayName());
            }
          }
          descendent = aIter.next();
        }
        if (!processedChildren) {
          Object value = ((TinyNodeImpl) obj).getDisplayName();
          if (!headers.contains(value)) {
            headers.add(value);
          }
        }
      }
    }
    if (headers.size() > 0) {
      columnHeaders = new Object[1][headers.size()];
      Iterator headerIter = headers.iterator();
      int i = 0;
      while (headerIter.hasNext()) {
        columnHeaders[0][i] = headerIter.next();
        i++;
      }
    } else {
      columnHeaders = new Object[1][1];
      columnHeaders[0][0] = XQMetaData.DEFAULT_COLUMN_NAME;
    }

  }

  public int getRowCount() {
    return rowCount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getColumnHeaders()
   */
  @Override
  public Object[][] getColumnHeaders() {
    return columnHeaders;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getRowHeaders()
   */
  @Override
  public Object[][] getRowHeaders() {
    return rowHeaders;
  }
}
