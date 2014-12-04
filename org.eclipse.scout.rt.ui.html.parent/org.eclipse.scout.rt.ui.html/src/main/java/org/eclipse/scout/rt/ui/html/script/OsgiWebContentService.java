package org.eclipse.scout.rt.ui.html.script;

import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.ServiceRegistration;

/**
 * Support for contributing a "/WebContent" folder to {@link WebContentRequestInterceptor}
 * <p>
 * TODO imo once osgi support is dropped, then this class is no longer needed, it is replaced by
 * {@link ClassLoader#getResources(String)} inside {@link WebContentRequestInterceptor}
 */
public class OsgiWebContentService extends AbstractService {
  private IWebContentResourceLocator m_locator;

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    m_locator = new OsgiWebContentResourceLocator(registration.getReference().getBundle());
  }

  public IWebContentResourceLocator getLocator() {
    return m_locator;
  }

}
