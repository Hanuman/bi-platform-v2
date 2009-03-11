/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.mondrian.catalog;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class MondrianSchema {
    private String name;

    private List<MondrianCube> cubes;

    public MondrianSchema(final String name, final List<MondrianCube> cubes) {
      this.name = name;
      this.cubes = cubes;
    }

    public String getName() {
      return name;
    }

    public List<MondrianCube> getCubes() {
      return cubes;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this).append("name", name).append("cubes", cubes).toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }

  }

