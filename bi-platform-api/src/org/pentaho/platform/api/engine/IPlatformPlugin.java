package org.pentaho.platform.api.engine;

import java.util.List;

import org.pentaho.ui.xul.XulOverlay;

public interface IPlatformPlugin {

  /**
   * Returns the name of this plug-in 
   * @return
   */
  public String getName();
  
  /**
   * A short description of where this plugin came from, e.g. "FS: biserver/solutions/pluginA"
   * @return
   */
  public String getSourceDescription();
  
  /**
   * Returns the list of content generators for this plug-in
   * @return
   */
  public List<IContentGeneratorInfo> getContentGenerators();
  
  /**
   * Returns a list of overlays for this plug-in
   * @return
   */
  public List<XulOverlay> getOverlays();
  
  /**
   * Returns a list of content info objects for this plug-in
   * @return
   */
  public List<IContentInfo> getContentInfos();
  
  /**
   * Returns a list of menu overlays for this plug-in
   * @return
   */
  public List getMenuCustomizations();
}
