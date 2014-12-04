package org.eclipse.scout.rt.ui.html.res;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Default implementation of a {@link IWebArchiveResourceLocator} that searches in the complete jee environment (war
 * file)
 */
public class JEEWebArchiveResourceLocator implements IWebArchiveResourceLocator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JEEWebArchiveResourceLocator.class);

  private ServletContext m_servletContext;

  public JEEWebArchiveResourceLocator(ServletContext servletContext) {
    m_servletContext = servletContext;
  }

  @Override
  public URL getScriptResource(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return getResourceImpl("/js/" + path);
  }

  @Override
  public URL getWebContentResource(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return getResourceImpl("/WebContent/" + path);
  }

  protected URL getResourceImpl(String resourcePath) {
    try {
      return m_servletContext.getResource(resourcePath);
    }
    catch (MalformedURLException ex) {
      LOG.warn("resourcePath: " + resourcePath, ex);
      return null;
    }
  }
}
