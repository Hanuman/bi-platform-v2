package org.pentaho.platform.repository.solution.filebased;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IFileInfoGenerator.ContentType;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.PluginSettings;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.xml.XmlHelper;

public class FileBasedSolutionRepositoryExperimental extends FileBasedSolutionRepository {

	private static final long serialVersionUID = -5819989582893333448L;

	@Override
	  protected void processDir(final Element parentNode, final File parentDir, final String solutionId, int pathIdx,
		      final int actionOperation) {
		    File files[] = parentDir.listFiles();
		    for (File element : files) {
		      if (!element.isDirectory()) {
		        String fileName = element.getName();
		        processFile( fileName, element, parentNode, solutionId, pathIdx, actionOperation );
		      }
		    }
		    for (File element : files) {
		      if (element.isDirectory()
		          && (!element.getName().equalsIgnoreCase("system")) && (!element.getName().equalsIgnoreCase("CVS")) && (!element.getName().equalsIgnoreCase(".svn"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		        Element dirNode = parentNode.addElement("file"); //$NON-NLS-1$
		        dirNode.addAttribute("type", FileInfo.FILE_TYPE_FOLDER); //$NON-NLS-1$
		        // TODO read this from the directory index file
		        String thisSolution;
		        String path = ""; //$NON-NLS-1$
		        if (solutionId == null) {
		          thisSolution = element.getName();
		          pathIdx = rootPath.length() + File.separator.length() + thisSolution.length();
		        } else {
		          thisSolution = solutionId;
		          path = element.getAbsolutePath().substring(pathIdx);
		          // windows \ characters in the path gets messy in urls, so
		          // switch them to /
		          path = path.replace('\\', '/');
		          dirNode.addElement("path").setText(path); //$NON-NLS-1$
		        }
		        File indexFile = new File(element, ISolutionRepository.INDEX_FILENAME);
		        Document indexDoc = null;
		        if (indexFile.exists()) {
		          indexDoc = getSolutionDocument(thisSolution, path, ISolutionRepository.INDEX_FILENAME);
		        }
		        if (indexDoc != null) {
		          addIndexToRepository(indexDoc, element, dirNode, path, thisSolution);
		        } else {
		          dirNode.addAttribute("visible", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		          String dirName = element.getName();
		          dirNode.addAttribute("name", XmlHelper.encode(dirName)); //$NON-NLS-1$
		          dirNode.addElement("title").setText(dirName); //$NON-NLS-1$
		        }
		        processDir(dirNode, element, thisSolution, pathIdx, actionOperation);
		      } else if ((solutionId == null) && element.getName().equalsIgnoreCase(ISolutionRepository.INDEX_FILENAME)) {
		        Document indexDoc = null;
		        indexDoc = getSolutionDocument("", "", ISolutionRepository.INDEX_FILENAME); //$NON-NLS-1$ //$NON-NLS-2$
		        if (indexDoc != null) {
		          addIndexToRepository(indexDoc, parentDir, parentNode, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
		        }
		      }
		    }
		  }

	  protected void processFile( String fileName, File element, final Element parentNode, final String solutionId, int pathIdx,
		      final int actionOperation ) {
	      if (fileName.equals("Entries") || fileName.equals("Repository") || fileName.equals("Root")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	          // ignore any CVS files
	          return;
	        }
	      int lastPoint = fileName.lastIndexOf('.');
	      if( lastPoint == -1 ) {
	      	// ignore anything with no extension
	    	  return;
	      }
	      String extension = fileName.substring( lastPoint+1 ).toLowerCase();
	        String solutionPath = element.getAbsolutePath().substring(rootPath.length());
	        if ("url".equals( extension )) { //$NON-NLS-1$
	          addUrlToRepository(element, parentNode, solutionPath);
	        }
	      boolean addFile = "xaction".equals( extension );
	  	PluginSettings pluginSettings = (PluginSettings) PentahoSystem.getObject( getSession(), "IPluginSettings" );
		if( pluginSettings != null ) {
	    	Set<String> types = pluginSettings.getContentTypes();
	    	addFile |= types != null && types.contains( extension );
		}
		if( !addFile ) {
			return;
		}
	        String path = element.getAbsolutePath().substring(pathIdx);
	        if (!path.equals(fileName)) {
	          path = path.substring(0, path.length() - fileName.length() - 1);
	          // windows \ characters in the path gets messy in urls, so
	          // switch them to /
	          path = path.replace('\\', '/');
	        } else {
	          path = ""; //$NON-NLS-1$
	        }
	        if (fileName.toLowerCase().endsWith(".xaction")) { //$NON-NLS-1$ 
	          // create an action sequence document from this
	          info(Messages.getString("SolutionRepository.DEBUG_ADDING_ACTION", fileName)); //$NON-NLS-1$
	          IActionSequence actionSequence = getActionSequence(solutionId, path, fileName, loggingLevel, actionOperation);
	          if (actionSequence == null) {
	            error(Messages.getErrorString("SolutionRepository.ERROR_0006_INVALID_SEQUENCE_DOCUMENT", fileName)); //$NON-NLS-1$
	          } else {
	            addToRepository(actionSequence, parentNode, element);
	          }
	        }
	        else if( pluginSettings != null ) {
	        	String fullPath = solutionId+ISolutionRepository.SEPARATOR+((StringUtil.isEmpty(path)) ? "" : path+ISolutionRepository.SEPARATOR )+fileName;
	        	try {
	            	IFileInfo fileInfo = getFileInfo( solutionId, path, fileName, extension, pluginSettings );
	                addToRepository( fileInfo, solutionId, path, fileName, parentNode, element);
	        	} catch (Exception e) {
	        		error( "Could not add file to repository index: "+fullPath, e );
	        	}
	        }

	  }
	  
	  protected IFileInfo getFileInfo( final String solution, final String path, final String fileName, final String extension, PluginSettings pluginSettings ) {
		  IFileInfo fileInfo = null;
		  String fullPath = solution+ISolutionRepository.SEPARATOR+((StringUtil.isEmpty(path)) ? "" : path+ISolutionRepository.SEPARATOR )+fileName;
		try {
			
	    	IContentGeneratorInfo info = pluginSettings.getDefaultContentGeneratorInfoForType( extension, getSession());
	        IFileInfoGenerator fig = info.getFileInfoGenerator();
	        if( fig != null ) {
	        	fig.setLogger( this );
	        	ContentType contentType = fig.getContentType();
	        	if( contentType == ContentType.INPUTSTREAM ) {
	                InputStream in = getResourceInputStream( fullPath, true);
	                fileInfo = fig.getFileInfo(solution, path, fileName, in);
	        	}
	        	else if( contentType == ContentType.DOM4JDOC ) {
	                Document doc = getSolutionDocument( fullPath );
	                fileInfo = fig.getFileInfo(solution, path, fileName, doc );
	        	}
	        	else if( contentType == ContentType.BYTES ) {
	                byte bytes[] = getResourceAsBytes( fullPath, true );
	                fileInfo = fig.getFileInfo(solution, path, fileName, bytes );
	        	}
	        	else if( contentType == ContentType.STRING ) {
	                String str = getResourceAsString( fullPath );
	                fileInfo = fig.getFileInfo(solution, path, fileName, str );
	        	}
	        }
		} catch (Exception e) {
			error( "Could not add file to repository index: "+fullPath, e );
		}
		return fileInfo;
	  }
	  
	  private void addToRepository( final IFileInfo info, final String solution, final String path, final String fileName, final Element parentNode, final File file) {
		    Element dirNode = parentNode.addElement("file"); //$NON-NLS-1$
		    dirNode.addAttribute("type", FileInfo.FILE_TYPE_ACTIVITY); //$NON-NLS-1$
		    dirNode.addElement("filename").setText(fileName); //$NON-NLS-1$
		    dirNode.addElement("path").setText(path); //$NON-NLS-1$
		    dirNode.addElement("solution").setText(solution); //$NON-NLS-1$
		    dirNode.addElement("title").setText( info.getTitle() ); //$NON-NLS-1$
		    String description = info.getDescription();
		    if (description == null) {
		      dirNode.addElement("description"); //$NON-NLS-1$
		    } else {
		      dirNode.addElement("description").setText(description); //$NON-NLS-1$
		    }
		    String author = info.getAuthor();
		    if (author == null) {
		      dirNode.addElement("author"); //$NON-NLS-1$
		    } else {
		      dirNode.addElement("author").setText(author); //$NON-NLS-1$
		    }
		    String iconPath = info.getIcon();
		    if ((iconPath != null) && !iconPath.equals("")) { //$NON-NLS-1$
		      String rolloverIconPath = null;
		      int rolloverIndex = iconPath.indexOf("|"); //$NON-NLS-1$
		      if (rolloverIndex > -1) {
		        rolloverIconPath = iconPath.substring(rolloverIndex + 1);
		        iconPath = iconPath.substring(0, rolloverIndex);
		      }
		      if (publishIcon(file.getParentFile().getAbsolutePath(), iconPath)) {
		        dirNode.addElement("icon").setText("getImage?image=icons/" + iconPath); //$NON-NLS-1$ //$NON-NLS-2$
		      } else {
		        dirNode.addElement("icon").setText(info.getIcon()); //$NON-NLS-1$
		      }
		      if (rolloverIconPath != null) {
		        if (publishIcon(PentahoSystem.getApplicationContext().getSolutionPath(
		            solution + File.separator + path), rolloverIconPath)) {
		          dirNode.addElement("rollovericon").setText("getImage?image=icons/" + rolloverIconPath); //$NON-NLS-1$ //$NON-NLS-2$
		        } else {
		          dirNode.addElement("rollovericon").setText(rolloverIconPath); //$NON-NLS-1$
		        }
		      }
		    }
		    String displayType = info.getDisplayType();
		    if ((displayType == null) || ("none".equalsIgnoreCase(displayType))) { //$NON-NLS-1$
		      // this should be hidden from users
		      dirNode.addAttribute("visible", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		    } else {
		      dirNode.addAttribute("visible", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		      dirNode.addAttribute("displaytype", displayType); //$NON-NLS-1$
		    }

		    ISubscriptionRepository subscriptionRepository = PentahoSystem.getSubscriptionRepository(getSession());
		    boolean subscribable = false;
		    if (subscriptionRepository != null) {
		      subscribable = subscriptionRepository.getContentByActionReference(solution + ISolutionRepository.SEPARATOR
		          + path + ISolutionRepository.SEPARATOR + fileName ) != null;
		    }
		    dirNode.addElement("properties").setText("subscribable=" + Boolean.toString(subscribable)); //$NON-NLS-1$ //$NON-NLS-2$

		  }

	
}
