package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;

public class ContentGeneratorInfo implements IContentGeneratorInfo {

	private String description;
	
	private String id;
	
	private String title;
	
	private String url;
	
	private String type;

	private String fileInfoGeneratorClassname;
	
	private IFileInfoGenerator fileInfoGenerator; 
	
	private String classname;
	
	private String scope;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFileInfoGeneratorClassname() {
    return fileInfoGeneratorClassname;
  }

  public void setFileInfoGeneratorClassname(String fileInfoGeneratorClassname) {
    this.fileInfoGeneratorClassname = fileInfoGeneratorClassname;
  }

  public IFileInfoGenerator getFileInfoGenerator() {
		return fileInfoGenerator;
	}

	public void setFileInfoGenerator(IFileInfoGenerator fileInfoGenerator) {
		this.fileInfoGenerator = fileInfoGenerator;
	}

  public String getClassname() {
    return classname;
  }

  public void setClassname(String classname) {
    this.classname = classname;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }


}
