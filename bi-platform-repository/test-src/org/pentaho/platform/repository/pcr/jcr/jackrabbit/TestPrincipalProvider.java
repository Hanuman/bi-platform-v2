package org.pentaho.platform.repository.pcr.jcr.jackrabbit;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.Session;

import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.security.AnonymousPrincipal;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.apache.jackrabbit.core.security.principal.PrincipalIteratorAdapter;
import org.apache.jackrabbit.core.security.principal.PrincipalProvider;
import org.pentaho.platform.repository.pcr.jcr.jackrabbit.SpringSecurityGrantedAuthorityPrincipal;

/**
 * PrincipalProvider for unit test purposes. Has joe and the other Pentaho users. In addition, it has "everyone", 
 * "admin", and "anonymous".
 * 
 * <p>
 * Some parts copied from SimplePrincipalProvider.
 * </p>
 * 
 * @author mlowery
 */
public class TestPrincipalProvider implements PrincipalProvider {

  private final Map<String, Principal> principals = new HashMap<String, Principal>();

  public TestPrincipalProvider() {
    principals.put("admin", new AdminPrincipal("admin"));
    principals.put("anonymous", new AnonymousPrincipal());

    EveryonePrincipal everyone = EveryonePrincipal.getInstance();
    principals.put(everyone.getName(), everyone);

    principals.put("joe", new UserPrincipal("joe"));
    principals.put("suzy", new UserPrincipal("suzy"));
    principals.put("tiffany", new UserPrincipal("tiffany"));
    principals.put("pat", new UserPrincipal("pat"));
    principals.put("Authenticated", new SpringSecurityGrantedAuthorityPrincipal("Authenticated"));
    principals.put("acme_Authenticated", new SpringSecurityGrantedAuthorityPrincipal("acme_Authenticated"));
    principals.put("acme_Admin", new SpringSecurityGrantedAuthorityPrincipal("acme_Admin"));
    principals.put("duff_Authenticated", new SpringSecurityGrantedAuthorityPrincipal("duff_Authenticated"));
    principals.put("duff_Admin", new SpringSecurityGrantedAuthorityPrincipal("duff_Admin"));
  }

  public Principal getPrincipal(String principalName) {
    if (principals.containsKey(principalName)) {
      return (Principal) principals.get(principalName);
    } else {
      return null;
    }
  }

  public PrincipalIterator findPrincipals(String simpleFilter) {
    return findPrincipals(simpleFilter, PrincipalManager.SEARCH_TYPE_ALL);
  }

  public PrincipalIterator findPrincipals(String simpleFilter, int searchType) {
    Principal p = getPrincipal(simpleFilter);
    if (p == null) {
      return PrincipalIteratorAdapter.EMPTY;
    } else if (p instanceof Group && searchType == PrincipalManager.SEARCH_TYPE_NOT_GROUP || !(p instanceof Group)
        && searchType == PrincipalManager.SEARCH_TYPE_GROUP) {
      return PrincipalIteratorAdapter.EMPTY;
    } else {
      return new PrincipalIteratorAdapter(Collections.singletonList(p));
    }
  }

  public PrincipalIterator getPrincipals(int searchType) {
    PrincipalIterator it;
    switch (searchType) {
      case PrincipalManager.SEARCH_TYPE_GROUP:
        it = new PrincipalIteratorAdapter(Collections.singletonList(EveryonePrincipal.getInstance()));
        break;
      case PrincipalManager.SEARCH_TYPE_NOT_GROUP:
        Set<Principal> set = new HashSet<Principal>(principals.values());
        set.remove(EveryonePrincipal.getInstance());
        it = new PrincipalIteratorAdapter(set);
        break;
      case PrincipalManager.SEARCH_TYPE_ALL:
        it = new PrincipalIteratorAdapter(principals.values());
        break;
      // no default
      default:
        throw new IllegalArgumentException("Unknown search type " + searchType);
    }
    return it;
  }

  public PrincipalIterator getGroupMembership(Principal principal) {
    if (principal instanceof EveryonePrincipal) {
      return PrincipalIteratorAdapter.EMPTY;
    }

    Set<Principal> principals = new HashSet<Principal>();
    if (principal.getName().equals("joe") || principal.getName().equals("suzy")
        || principal.getName().equals("tiffany")) {
      principals.add(new SpringSecurityGrantedAuthorityPrincipal("Authenticated"));
      principals.add(new SpringSecurityGrantedAuthorityPrincipal("acme_Authenticated"));
    } else if (principal.getName().equals("pat")) {
      principals.add(new SpringSecurityGrantedAuthorityPrincipal("Authenticated"));
      principals.add(new SpringSecurityGrantedAuthorityPrincipal("duff_Authenticated"));
    }
    return new PrincipalIteratorAdapter(principals);
  }

  public void init(Properties options) {
    // nothing to do
  }

  public void close() {
    // nothing to do
  }

  public boolean canReadPrincipal(Session session, Principal principal) {
    return true;
  }

}
