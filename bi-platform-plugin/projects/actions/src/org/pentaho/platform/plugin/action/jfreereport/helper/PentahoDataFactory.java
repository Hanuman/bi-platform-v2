/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.plugin.action.jfreereport.helper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableModel;

import org.jfree.report.DataRow;
import org.jfree.report.ReportDataFactoryException;
import org.jfree.report.modules.misc.datafactory.StaticDataFactory;

/**
 * This needs the latest CVS version of JFreeReport (0.8.7-5-cvs)...
 *
 * @author Thomas Morgner
 */
public class PentahoDataFactory extends StaticDataFactory {
  private ClassLoader classLoader;

  public PentahoDataFactory(final ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public TableModel queryData(final String string, final DataRow dataRow) throws ReportDataFactoryException {
    final TableModel tableModel = super.queryData(string, dataRow);

    try {
      final Class cls = tableModel.getClass();
      final Map map = new HashMap();
      for (int i = 0; i < dataRow.getColumnCount(); i++) {
        map.put(dataRow.getColumnName(i), dataRow.get(i));
      }
      final Object[] args = { map };
      final Class[] argt = { Map.class };
      final Method theMethod = cls.getMethod("setParameters", argt); //$NON-NLS-1$
      if (theMethod != null) {
        theMethod.invoke(tableModel, args);
      }
    } catch (Exception ignored) {
      // Method does not exist... ok, ignore it.
    }

    return tableModel;
  }
}
