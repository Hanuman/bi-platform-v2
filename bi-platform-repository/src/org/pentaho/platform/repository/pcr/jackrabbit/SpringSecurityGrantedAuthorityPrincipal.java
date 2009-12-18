package org.pentaho.platform.repository.pcr.jackrabbit;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Jackrabbit representation of a {@code GrantedAuthoritySid}.
 * 
 * @author mlowery
 */
public class SpringSecurityGrantedAuthorityPrincipal implements Principal {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(SpringSecurityGrantedAuthorityPrincipal.class);

  // ~ Instance fields =================================================================================================

  private String name;

  // ~ Constructors ====================================================================================================

  public SpringSecurityGrantedAuthorityPrincipal(final String name) {
    super();
    this.name = name;
  }

  // ~ Methods =========================================================================================================

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SpringSecurityGrantedAuthorityPrincipal other = (SpringSecurityGrantedAuthorityPrincipal) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "GrantedAuthorityPrincipal[name=" + name + "]";
  }

}
