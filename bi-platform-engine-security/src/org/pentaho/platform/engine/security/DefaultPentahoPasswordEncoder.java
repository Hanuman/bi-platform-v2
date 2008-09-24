package org.pentaho.platform.engine.security;

import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.apache.commons.lang.Validate;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.dao.DataAccessException;

/**
 * Default password encoder for the BI Server.
 * 
 * <p>TODO Stop using <code>IPasswordService</code> since it is inherently reversible which is not a best practice.</p>
 * 
 * <p>Delegates to <code>IPasswordService</code>. Note that <code>IPasswordService</code> and 
 * <code>PasswordEncoder</code> are fundamentally different.</p> <code>IPasswordService</code> implements reversible 
 * encoding; <code>PasswordEncoder</code> is not reversible.
 * 
 * @author mlowery
 */
public class DefaultPentahoPasswordEncoder implements PasswordEncoder {

  public String encodePassword(final String rawPass, final Object salt) throws DataAccessException {
    Validate.notNull(rawPass, "rawPass cannot be null");
    IPasswordService passwordService = null;
    try {
      passwordService = (IPasswordService) PentahoSystem.getObjectFactory().getObject(
          IPasswordService.IPASSWORD_SERVICE, null);
    } catch (ObjectFactoryException e) {
      throw new PasswordEncoderException("password service could not be created", e);
    }

    try {
      return passwordService.encrypt(rawPass);
    } catch (PasswordServiceException e) {
      throw new PasswordEncoderException("password service could not encrypt", e);
    }
  }

  public boolean isPasswordValid(final String encPass, final String rawPass, final Object salt)
      throws DataAccessException {
    Validate.notNull(encPass, "encPass cannot be null");
    Validate.notNull(rawPass, "rawPass cannot be null");
    String encodedRawPass = encodePassword(rawPass, salt);
    return encPass.equals(encodedRawPass);
  }

  private class PasswordEncoderException extends DataAccessException {

    private static final long serialVersionUID = -6706722564780658165L;

    public PasswordEncoderException(final String msg, final Throwable cause) {
      super(msg, cause);
    }

    public PasswordEncoderException(final String msg) {
      super(msg);
    }

  }
}
