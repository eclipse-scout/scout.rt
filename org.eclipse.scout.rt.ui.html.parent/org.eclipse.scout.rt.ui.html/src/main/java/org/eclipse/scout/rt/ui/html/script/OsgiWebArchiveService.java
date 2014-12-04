package org.eclipse.scout.rt.ui.html.script;

import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Support for contributing a "/WebContent" folder to {@link WebArchiveResourceRequestInterceptor}
 * <p>
 * TODO imo once osgi support is dropped, then this class is no longer needed, it is replaced by
 * {@link ClassLoader#getResources(String)} inside {@link WebArchiveResourceRequestInterceptor}
 */
public class OsgiWebArchiveService extends AbstractService {
  private Bundle m_bundle;

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    m_bundle = registration.getReference().getBundle();
  }

  public Bundle getBundle() {
    return m_bundle;
  }

}
