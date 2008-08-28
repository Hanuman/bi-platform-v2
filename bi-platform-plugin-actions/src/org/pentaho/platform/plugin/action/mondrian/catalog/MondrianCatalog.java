package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Simplification of XMLA-specific DataSourcesConfig.Catalog. Should be immutable.
 * 
 * @author mlowery
 */
public class MondrianCatalog {
  private String name;

  private String dataSourceInfo; // optionally overrides this.dataSource.dataSourceInfo

  private String definition;

  private MondrianDataSource dataSource;

  private MondrianSchema schema;

  public MondrianCatalog(final String name, final String dataSourceInfo, final String definition,
      final MondrianDataSource dataSource, final MondrianSchema schema) {
    this.name = name;
    this.dataSourceInfo = dataSourceInfo;
    this.definition = definition;
    this.dataSource = dataSource;
    this.schema = schema;
  }

  public String getName() {
    return name;
  }

  public String getDefinition() {
    return definition;
  }

  public String getDataSourceInfo() {
    return dataSourceInfo;
  }

  public MondrianSchema getSchema() {
    return schema;
  }

  /**
   * Returns dataSource with overridden dataSourceInfo (if any). 
   */
  public MondrianDataSource getEffectiveDataSource() {
    if (null != dataSourceInfo) {
      return new MondrianDataSource(dataSource, dataSourceInfo);
    } else {
      return dataSource;
    }
  }

  public MondrianDataSource getDataSource() {
    return dataSource;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("name", name).append("dataSourceInfo", dataSourceInfo).append( //$NON-NLS-1$//$NON-NLS-2$
        "definition", definition).append("dataSource", dataSource).append("schema", schema).toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
