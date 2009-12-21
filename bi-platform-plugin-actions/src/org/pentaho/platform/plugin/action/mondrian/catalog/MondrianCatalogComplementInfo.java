package org.pentaho.platform.plugin.action.mondrian.catalog;

/**
 *
 * Class to contain extra attributes that we want to connect to the Mondrian Catalog
 * @author Pedro Alves
 *
 */
public class MondrianCatalogComplementInfo {


    private String whereCondition;

    public MondrianCatalogComplementInfo() {
    }


    public String getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(final String whereCondition) {
        this.whereCondition = whereCondition;
    }


}
