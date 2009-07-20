package org.pentaho.platform.api.engine;

public class PluginBeanDefinition {
    private String beanId, classname;
    
    public PluginBeanDefinition(String beanId, String classname) { 
      this.beanId = beanId;
      this.classname = classname;
    }

    public String getBeanId() {
      return beanId;
    }

    public void setBeanId(String beanId) {
      this.beanId = beanId;
    }

    public String getClassname() {
      return classname;
    }

    public void setClassname(String classname) {
      this.classname = classname;
    }
}
