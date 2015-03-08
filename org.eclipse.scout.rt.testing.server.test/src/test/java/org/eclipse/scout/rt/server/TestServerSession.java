package org.eclipse.scout.rt.server;

import org.eclipse.scout.rt.testing.server.ServerBeanContributor;

/**
 * {@link IServerSession} used for JUnit-tests; is registered within {@link ServerBeanContributor}.<br/>
 * Until OSGi has gone, this class must be located in package 'org.eclipse.scout.rt.server' because ServerSession#load
 * resolves the bundle.
 */
public class TestServerSession extends AbstractServerSession {
  private static final long serialVersionUID = 1L;

  public TestServerSession() {
    super(true);
  }

}
