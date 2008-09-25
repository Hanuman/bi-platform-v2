package org.pentaho.platform.engine.security;

import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.apache.commons.lang.Validate;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.messages.Messages;
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
    Validate.notNull(rawPass, Messages.getString("DefaultPentahoPasswordEncoder.ERROR_0001_RAWPASS_CANNOT_BE_NULL")); //$NON-NLS-1$
    IPasswordService passwordService = null;
    try {
      passwordService = (IPasswordService) PentahoSystem.getObjectFactory().getObject(
          IPasswordService.IPASSWORD_SERVICE, null);
    } catch (ObjectFactoryException e) {
      throw new PasswordEncoderException(Messages
          .getString("DefaultPentahoPasswordEncoder.ERROR_0003_PASSWORD_SERVICE_CANNOT_BE_CREATED"), e); //$NON-NLS-1$
    }

    try {
      return passwordService.encrypt(rawPass);
    } catch (PasswordServiceException e) {
      throw new PasswordEncoderException(Messages
          .getString("DefaultPentahoPasswordEncoder.ERROR_0004_PASSWORD_SERVICE_COULD_NOT_ENCRYPT"), e); //$NON-NLS-1$
    }
  }

  public boolean isPasswordValid(final String encPass, final String rawPass, final Object salt)
      throws DataAccessException {
    Validate.notNull(encPass, Messages.getString("DefaultPentahoPasswordEncoder.ERROR_0002_ENCPASS_CANNOT_BE_NULL")); //$NON-NLS-1$
    Validate.notNull(rawPass, Messages.getString("DefaultPentahoPasswordEncoder.ERROR_0001_RAWPASS_CANNOT_BE_NULL")); //$NON-NLS-1$
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
