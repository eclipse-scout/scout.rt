package org.eclipse.scout.rt.ui.html.res;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Default implementation of a {@link IWebContentResourceLocator} that searches in the complete jee environment (war
 * file)
 */
public class JEEWebContentResourceLocator implements IWebContentResourceLocator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JEEWebContentResourceLocator.class);

  private ServletContext m_servletContext;

  public JEEWebContentResourceLocator(ServletContext servletContext) {
    m_servletContext = servletContext;
  }

  @Override
  public URL getScriptSource(String path) {
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
      URL url = m_servletContext.getResource(resourcePath);
      if (url != null && LOG.isDebugEnabled()) {
        LOG.debug("locate resource '" + resourcePath + "' -> " + url);
      }
      return url;
    }
    catch (MalformedURLException ex) {
      LOG.warn("resourcePath: " + resourcePath, ex);
      return null;
    }
  }
}
