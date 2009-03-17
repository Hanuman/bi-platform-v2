package org.pentaho.platform.webservice.services.metadata.server;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.metadata.MetadataPublisher;
import org.pentaho.platform.plugin.services.webservices.SessionHandler;
import org.pentaho.pms.factory.CwmSchemaFactoryInterface;
import org.pentaho.pms.schema.v3.model.Model;
import org.pentaho.pms.schema.v3.model.ModelEnvelope;
import org.pentaho.pms.service.LocalModelService;

public class ModelService extends LocalModelService {

  public ModelService() {
    IPentahoSession session = SessionHandler.getSession();
    MetadataPublisher.loadAllMetadata(session, false);
    CwmSchemaFactoryInterface cwmSchemaFactory = PentahoSystem.get( CwmSchemaFactoryInterface.class, "ICwmSchemaFactory", session);  //$NON-NLS-1$
    setCwmSchemaFactory(cwmSchemaFactory);
  }
  
  @Override
  public ModelEnvelope[] listModels() throws Exception {
    return super.listModels();
  }
  
  @Override
  public Model getModel( String domain, String id, boolean deep ) throws Exception {
    return super.getModel( domain, id, deep );
  }
  
}
