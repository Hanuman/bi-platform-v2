package org.pentaho.platform.repository.solution.dbbased;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IPluginSettings;
import org.pentaho.platform.api.engine.IFileInfoGenerator.ContentType;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.solution.filebased.FileInfo;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.xml.XmlHelper;

public class ExtensionDbBasedSolutionRepository extends DbBasedSolutionRepository {

	private static final long serialVersionUID = -5819989582893333448L;

	@Override
  protected void processDir(final Element parentNode, final RepositoryFile parentDir, final String solutionId,
      final int actionOperation, final int recurseLevels) {
	  
    if (recurseLevels <= 0) {
      return;
    }
    RepositoryFile[] files = parentDir.listRepositoryFiles();
    for (RepositoryFile element : files) {
      if (!element.isDirectory()) {
        String fileName = element.getFileName();
        processFile( fileName, element, parentNode, solutionId, actionOperation );
      }
    }
    for (RepositoryFile element : files) {
      if (element.isDirectory()
          && (!element.getFileName().equalsIgnoreCase("system")) && (!element.getFileName().equalsIgnoreCase("CVS")) && (!element.getFileName().equalsIgnoreCase(".svn"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Element dirNode = parentNode.addElement("file"); //$NON-NLS-1$
        dirNode.addAttribute("type", FileInfo.FILE_TYPE_FOLDER); //$NON-NLS-1$
        contributeAttributes(element, dirNode);
        // TODO read this from the directory index file
        String thisSolution;
        String path = getSolutionPath(element);
        if (solutionId == null) {
          thisSolution = getSolutionId(element);
        } else {
          thisSolution = solutionId;
          dirNode.addElement("path").setText(path); //$NON-NLS-1$
        }
        Document indexDoc = getSolutionDocument(element.getFullPath() + ISolutionRepository.SEPARATOR
            + ISolutionRepository.INDEX_FILENAME, actionOperation);
        if (indexDoc != null) {
          addIndexToRepository(indexDoc, element, dirNode, path, thisSolution);
        } else {
          dirNode.addAttribute("visible", "false"); //$NON-NLS-1$ //$NON-NLS-2$
          String dirName = element.getFileName();
          dirNode.addAttribute("name", XmlHelper.encode(dirName)); //$NON-NLS-1$
          dirNode.addElement("title").setText(dirName); //$NON-NLS-1$
        }
        processDir(dirNode, element, thisSolution, actionOperation, recurseLevels - 1);
      }
    }

		  }

	  protected void processFile( String fileName, RepositoryFile element, final Element parentNode, final String solutionId, 
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

      if (fileName.toLowerCase().endsWith(".url")) { //$NON-NLS-1$
        if (hasAccess(element, actionOperation)) {
          addUrlToRepository(element, parentNode);
          return;
        }
      }
      boolean addFile = "xaction".equals( extension ); //$NON-NLS-1$
      IPluginSettings pluginSettings = (IPluginSettings) PentahoSystem.getObject( getSession(), "IPluginSettings" ); //$NON-NLS-1$
      if( pluginSettings != null ) {
        Set<String> types = pluginSettings.getContentTypes();
        addFile |= types != null && types.contains( extension );
      }
      if( !addFile ) {
        return;
      }
      String path = getSolutionPath(element);
      if (fileName.toLowerCase().endsWith(".xaction")) { //$NON-NLS-1$ 
        // create an action sequence document from this
        info(Messages.getString("SolutionRepository.DEBUG_ADDING_ACTION", fileName)); //$NON-NLS-1$
        IActionSequence actionSequence = getActionSequence(solutionId, path, fileName, loggingLevel, actionOperation);
        if (actionSequence == null) {
          if (((solutionId == null) || (solutionId.length() == 0)) && ((path == null) || (path.length() == 0))) {
            info(Messages.getString("SolutionRepository.INFO_0008_NOT_ADDED", fileName)); //$NON-NLS-1$
          } else {
            error(Messages.getErrorString("SolutionRepository.ERROR_0006_INVALID_SEQUENCE_DOCUMENT", fileName)); //$NON-NLS-1$
          }
        } else {
          addToRepository(actionSequence, parentNode, element);
        }
      }
      else if( pluginSettings != null ) {
        String fullPath = solutionId+ISolutionRepository.SEPARATOR+((StringUtil.isEmpty(path)) ? "" : path+ISolutionRepository.SEPARATOR )+fileName; //$NON-NLS-1$
	      try {
	        IFileInfo fileInfo = getFileInfo( solutionId, path, fileName, extension, pluginSettings, actionOperation );
	        addToRepository( fileInfo, solutionId, path, fileName, parentNode, element);
	      } catch (Exception e) {
	        error( Messages.getErrorString( "SolutionRepository.ERROR_0021_FILE_NOT_ADDED", fullPath ), e ); //$NON-NLS-1$
	      }
	    }

	  }
	  
	  protected IFileInfo getFileInfo( final String solution, final String path, final String fileName, final String extension, IPluginSettings pluginSettings, final int actionOperation ) {
		  IFileInfo fileInfo = null;
		  String fullPath = solution+ISolutionRepository.SEPARATOR+((StringUtil.isEmpty(path)) ? "" : path+ISolutionRepository.SEPARATOR )+fileName; //$NON-NLS-1$
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
	                Document doc = getSolutionDocument( fullPath, actionOperation );
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
      error( Messages.getErrorString( "SolutionRepository.ERROR_0021_FILE_NOT_ADDED", fullPath ), e ); //$NON-NLS-1$
		}
		return fileInfo;
	  }
	  
	  private void addToRepository( final IFileInfo info, final String solution, final String path, final String fileName, final Element parentNode, final RepositoryFile file) {
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
		      if (publishIcon(file.retrieveParent().getFullPath(), iconPath)) {
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
