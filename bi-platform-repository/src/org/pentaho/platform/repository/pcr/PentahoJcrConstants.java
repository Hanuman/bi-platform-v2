package org.pentaho.platform.repository.pcr;

/**
 * Pentaho JCR constants for node and property names.
 * 
 * <p>
 * The naming convention for the constants is {@code PENTAHO_<NAME>} where {@code <NAME>} is the node or property name 
 * as all capital letters with no underscores. This matches Jackrabbit's convention.
 * </p>
 * 
 * @author mlowery
 */
public interface PentahoJcrConstants {

  String PENTAHO_URI = "http://www.pentaho.org/jcr/1.0";

  String JCR_ENCODING = "jcr:encoding";

  String JCR_DATA = "jcr:data";

  String MIX_REFERENCEABLE = "mix:referenceable";

  String JCR_CREATED = "jcr:created";

  //String JCR_LASTMODIFIED = "jcr:lastModified";

  //String JCR_MIMETYPE = "jcr:mimeType";

  String NT_FOLDER = "nt:folder";

  String NT_FILE = "nt:file";

  String NT_LINKEDFILE = "nt:linkedFile";

  String NT_UNSTRUCTURED = "nt:unstructured";

  String JCR_PRIMARYTYPE = "jcr:primaryType";
  
  String JCR_MIXINTYPES = "jcr:mixinTypes";

  String NT_RESOURCE = "nt:resource";

  String JCR_CONTENT = "jcr:content";

  String PENTAHO_RUNARGUMENTS = "runArguments";
  
  String PENTAHO_AUX = "aux";
  
  String PENTAHO_PENTAHOFILE = "pentahoFile";
  
  String PENTAHO_PENTAHORESOURCE = "pentahoResource";
  
  String PENTAHO_MIMETYPE = "mimeType";
  
  String PENTAHO_LASTMODIFIED = "lastModified";
  
  String JCR_MIMETYPE = "jcr:mimeType";
  
  String JCR_LASTMODIFIED = "jcr:lastModified";

  String MIX_VERSIONABLE = "mix:versionable";

  String PENTAHO_VERSIONED = "versioned";
  

}
