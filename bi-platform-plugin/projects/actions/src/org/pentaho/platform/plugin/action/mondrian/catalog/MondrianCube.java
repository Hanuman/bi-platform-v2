package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.apache.commons.lang.builder.ToStringBuilder;

public class MondrianCube {
    private String name;

    public MondrianCube(final String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this).append("name", name).toString(); //$NON-NLS-1$
    }

  }

