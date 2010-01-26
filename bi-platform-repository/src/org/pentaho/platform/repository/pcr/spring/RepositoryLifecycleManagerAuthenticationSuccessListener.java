package org.pentaho.platform.repository.pcr.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.Authentication;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.util.Assert;

/**
 * {@link ApplicationListener} that invokes {@link IUnifiedRepository.IRepositoryLifecycleManager#newTenant()} and
 * {@link IUnifiedRepository.IRepositoryLifecycleManager#newUser()}.
 * 
 * @author mlowery
 */
public class RepositoryLifecycleManagerAuthenticationSuccessListener implements ApplicationListener {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(RepositoryLifecycleManagerAuthenticationSuccessListener.class);

  // ~ Instance fields =================================================================================================

  private IUnifiedRepository repo;

  // ~ Constructors ====================================================================================================

  public RepositoryLifecycleManagerAuthenticationSuccessListener(final IUnifiedRepository repo) {
    super();
    Assert.notNull(repo);
    this.repo = repo;
  }

  // ~ Methods =========================================================================================================

  public void onApplicationEvent(final ApplicationEvent event) {
    // note that we're looking for InteractiveAuthenticationSuccessEvent which is fired after the SecurityContext
    // is populated; AuthenticationSuccessEvent is fired before SecurityContext is populated
    if (event instanceof AuthenticationSuccessEvent) {
      logger.debug("heard authentication success event; creating user home folder (if necessary)");
      try {
        // --- begin SecurityStartupFilter copy
        // SecurityStartupFilter cannot be used here since that filter will only do its work on the next request--not
        // this request; this request is the one processing the login; and the newTenant/newUser code cannot be moved
        // to SecurityStartupFilter as that is a CE class
        Authentication authentication = ((AuthenticationSuccessEvent) event).getAuthentication();
        StandaloneSession pentahoSession = new StandaloneSession(authentication.getName());
        pentahoSession.setAuthenticated(authentication.getName());
        // copy of SecurityHelper.setPrincipal() code
        pentahoSession.setAttribute("SECURITY_PRINCIPAL", authentication); //$NON-NLS-1$
        // --- end SecurityStartupFilter copy
        PentahoSessionHolder.setSession(pentahoSession);
        repo.getRepositoryLifecycleManager().newTenant();
        repo.getRepositoryLifecycleManager().newUser();
      } catch (Exception e) {
        logger.error("an exception occurred while creating user home folder", e);
      }
    }
  }

}
