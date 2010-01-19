package org.pentaho.platform.repository.pcr.data.sample;

import org.pentaho.platform.api.repository.IRepositoryFileData;

/**
 * An {@code IRepositoryFileData} for illustrative purposes.
 * 
 * @author mlowery
 */
public class SampleRepositoryFileData implements IRepositoryFileData {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = 8243282317105073909L;

  // ~ Instance fields =================================================================================================

  private String sampleString;

  private boolean sampleBoolean;

  private int sampleInteger;

  // ~ Constructors ====================================================================================================

  public SampleRepositoryFileData(final String sampleString, final boolean sampleBoolean, final int sampleInteger) {
    super();
    this.sampleString = sampleString;
    this.sampleBoolean = sampleBoolean;
    this.sampleInteger = sampleInteger;
  }

  // ~ Methods =========================================================================================================

  public String getSampleString() {
    return sampleString;
  }

  public boolean getSampleBoolean() {
    return sampleBoolean;
  }

  public int getSampleInteger() {
    return sampleInteger;
  }
}
