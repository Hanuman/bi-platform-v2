package org.pentaho.platform.repository.pcr.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.util.Assert;

public class CreateUserHomeFolderAuthenticationSuccessListener implements ApplicationListener {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(CreateUserHomeFolderAuthenticationSuccessListener.class);

  // ~ Instance fields =================================================================================================

  private IPentahoContentRepository pentahoContentRepository;

  // ~ Constructors ====================================================================================================

  public CreateUserHomeFolderAuthenticationSuccessListener(final IPentahoContentRepository pentahoContentRepository) {
    super();
    Assert.notNull(pentahoContentRepository);
    this.pentahoContentRepository = pentahoContentRepository;
  }

  // ~ Methods =========================================================================================================

  public void onApplicationEvent(final ApplicationEvent event) {
    // note that we're looking for InteractiveAuthenticationSuccessEvent which is fired after the SecurityContext
    // is populated; AuthenticationSuccessEvent is fired before SecurityContext is populated
    if (event instanceof InteractiveAuthenticationSuccessEvent) {
      logger.debug("heard interactive authentication success event; creating user home folder (if necessary)");
      try {
        pentahoContentRepository.createUserHomeFolderIfNecessary();
      } catch (Exception e) {
        logger.error("an exception occurred while creating user home folder", e);
      }
    }
  }

}
