package org.pentaho.platform.repository.pcr.ws;

import java.util.EnumSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.api.repository.RepositoryFileAce;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;

import edu.emory.mathcs.backport.java.util.Arrays;

public class RepositoryFileAclAceAdapter extends XmlAdapter<JaxbSafeRepositoryFileAclAce, RepositoryFileAce> {

  @Override
  public JaxbSafeRepositoryFileAclAce marshal(RepositoryFileAce v) throws Exception {
    JaxbSafeRepositoryFileAclAce jaxbAce = new JaxbSafeRepositoryFileAclAce();
    jaxbAce.recipient = v.getSid().getName();
    if (v.getSid().getType() == RepositoryFileSid.Type.USER) {
      jaxbAce.user = true;
    } else {
      jaxbAce.user = false;
    }
    jaxbAce.permissions = v.getPermissions().toArray(new RepositoryFilePermission[v.getPermissions().size()]);
    return jaxbAce;
  }

  @Override
  public RepositoryFileAce unmarshal(JaxbSafeRepositoryFileAclAce v) throws Exception {
    if (v.user) {
      return new RepositoryFileAce(new RepositoryFileSid(v.recipient, RepositoryFileSid.Type.USER), EnumSet
          .copyOf(Arrays.asList(v.permissions)));
    } else {
      return new RepositoryFileAce(new RepositoryFileSid(v.recipient, RepositoryFileSid.Type.ROLE), EnumSet
          .copyOf(Arrays.asList(v.permissions)));
    }
  }

}
