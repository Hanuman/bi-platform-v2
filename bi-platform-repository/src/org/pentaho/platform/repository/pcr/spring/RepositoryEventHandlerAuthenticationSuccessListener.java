package org.pentaho.platform.repository.pcr.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IRepositoryService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.util.Assert;

/**
 * {@link ApplicationListener} that invokes {@link IRepositoryService.IRepositoryEventHandler#onNewTenant()} and
 * {@link IRepositoryService.IRepositoryEventHandler#onNewUser()}.
 * 
 * @author mlowery
 */
public class RepositoryEventHandlerAuthenticationSuccessListener implements ApplicationListener {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(RepositoryEventHandlerAuthenticationSuccessListener.class);

  // ~ Instance fields =================================================================================================

  private IRepositoryService repositoryService;

  // ~ Constructors ====================================================================================================

  public RepositoryEventHandlerAuthenticationSuccessListener(final IRepositoryService repositoryService) {
    super();
    Assert.notNull(repositoryService);
    this.repositoryService = repositoryService;
  }

  // ~ Methods =========================================================================================================

  public void onApplicationEvent(final ApplicationEvent event) {
    // note that we're looking for InteractiveAuthenticationSuccessEvent which is fired after the SecurityContext
    // is populated; AuthenticationSuccessEvent is fired before SecurityContext is populated
    if (event instanceof InteractiveAuthenticationSuccessEvent) {
      logger.debug("heard interactive authentication success event; creating user home folder (if necessary)");
      try {
        repositoryService.getRepositoryEventHandler().onNewTenant();
        repositoryService.getRepositoryEventHandler().onNewUser();
      } catch (Exception e) {
        logger.error("an exception occurred while creating user home folder", e);
      }
    }
  }

}
