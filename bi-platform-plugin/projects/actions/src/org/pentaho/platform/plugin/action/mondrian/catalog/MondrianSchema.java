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

