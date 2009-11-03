package org.pentaho.platform.repository.pcr.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Registers the Spring-instantiated {@link IPentahoContentRepository} with {@link PentahoSystem}.
 * 
 * @author mlowery
 */
public class PentahoSystemBeanPostProcessor implements BeanPostProcessor {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(PentahoSystemBeanPostProcessor.class);

  // ~ Methods =========================================================================================================

  public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
    if (bean instanceof IPentahoContentRepository) {
      logger.debug("setting Pentaho content repository on PentahoSystem");
      PentahoSystem.setPentahoContentRepository((IPentahoContentRepository) bean);
    }
    return bean;
  }

  public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
    return bean;
  }

}
