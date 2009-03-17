package org.pentaho.platform.webservice.services.metadata.server;

import java.util.ArrayList;

import org.pentaho.platform.plugin.services.webservices.BaseWebServiceWrapper;
import org.pentaho.platform.webservice.plugin.messages.Messages;
import org.pentaho.pms.schema.v3.model.Attribute;
import org.pentaho.pms.schema.v3.model.Category;
import org.pentaho.pms.schema.v3.model.Column;
import org.pentaho.pms.schema.v3.model.Model;
import org.pentaho.pms.schema.v3.model.ModelEnvelope;

public class ModelServiceWrapper extends BaseWebServiceWrapper {

  /**
   * Returns the metadata model service class
   */
  public Class getServiceClass() {
    return ModelService.class;
  }

  /**
   * Add extra classes needed by the metadata model service
   */
  protected ArrayList<Class> getExtraClasses() {
    ArrayList<Class> extraClasses = new ArrayList<Class>();
    extraClasses.add( ModelEnvelope.class );
    extraClasses.add( Model.class );
    extraClasses.add( Column.class );
    extraClasses.add( Category.class );
    extraClasses.add( Attribute.class );
    
    return extraClasses;
  }
    
  public String getTitle() {
    return Messages.getString( "ModelServiceWrapper.USER_MODEL_SERVICE_TITLE" ); //$NON-NLS-1$
  }
    
  public String getDescription() {
    return Messages.getString( "ModelServiceWrapper.USER_MODEL_SERVICE_DESC" ); //$NON-NLS-1$
  }

}
