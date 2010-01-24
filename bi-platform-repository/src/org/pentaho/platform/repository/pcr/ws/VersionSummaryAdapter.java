package org.pentaho.platform.repository.pcr.ws;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.api.repository.VersionSummary;

import edu.emory.mathcs.backport.java.util.Arrays;

public class VersionSummaryAdapter extends XmlAdapter<JaxbSafeVersionSummary, VersionSummary> {

  @Override
  public JaxbSafeVersionSummary marshal(final VersionSummary v) throws Exception {
    JaxbSafeVersionSummary s = new JaxbSafeVersionSummary();
    s.id = v.getId().toString();
    s.author = v.getAuthor();
    s.date = v.getDate();
    s.message = v.getMessage();
    s.versionedFileId = v.getVersionedFileId().toString();
    s.labels = v.getLabels().toArray(new String[v.getLabels().size()]);
    return s;
  }

  @Override
  public VersionSummary unmarshal(final JaxbSafeVersionSummary v) throws Exception {
    return new VersionSummary(v.id, v.versionedFileId, v.date, v.author, v.message, Arrays.asList(v.labels));
  }

}
