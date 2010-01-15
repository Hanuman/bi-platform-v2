package org.pentaho.platform.api.engine;

import java.util.List;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;

public interface IPentahoSystemAdminPlugins {

  public String publish(final IPentahoSession session, final String className);

  public List<IPentahoPublisher> getPublisherList();

  public Document getPublishersDocument();

  /**
   * Registers administrative capabilities that can be invoked later 
   * via {@link PentahoSystem#publish(IPentahoSession, String)}
   * 
   * @param administrationPlugins a list of admin functions to register
   */
  public void setAdministrationPlugins(List<IPentahoPublisher> administrationPlugins);

}
