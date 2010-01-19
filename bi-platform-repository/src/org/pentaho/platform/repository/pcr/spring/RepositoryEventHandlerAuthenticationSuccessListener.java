package org.pentaho.platform.repository.pcr.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IUnifiedRepository;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.util.Assert;

/**
 * {@link ApplicationListener} that invokes {@link IUnifiedRepository.IRepositoryLifecycleManager#newTenant()} and
 * {@link IUnifiedRepository.IRepositoryLifecycleManager#newUser()}.
 * 
 * @author mlowery
 */
public class RepositoryEventHandlerAuthenticationSuccessListener implements ApplicationListener {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(RepositoryEventHandlerAuthenticationSuccessListener.class);

  // ~ Instance fields =================================================================================================

  private IUnifiedRepository repo;

  // ~ Constructors ====================================================================================================

  public RepositoryEventHandlerAuthenticationSuccessListener(final IUnifiedRepository repo) {
    super();
    Assert.notNull(repo);
    this.repo = repo;
  }

  // ~ Methods =========================================================================================================

  public void onApplicationEvent(final ApplicationEvent event) {
    // note that we're looking for InteractiveAuthenticationSuccessEvent which is fired after the SecurityContext
    // is populated; AuthenticationSuccessEvent is fired before SecurityContext is populated
    if (event instanceof InteractiveAuthenticationSuccessEvent) {
      logger.debug("heard interactive authentication success event; creating user home folder (if necessary)");
      try {
        repo.getRepositoryEventHandler().newTenant();
        repo.getRepositoryEventHandler().newUser();
      } catch (Exception e) {
        logger.error("an exception occurred while creating user home folder", e);
      }
    }
  }

}
