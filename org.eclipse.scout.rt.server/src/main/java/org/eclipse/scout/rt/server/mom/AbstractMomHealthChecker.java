/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.mom;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.mom.api.IMomTransport;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.healthcheck.AbstractHealthChecker;

/**
 * @since 6.1
 */
public abstract class AbstractMomHealthChecker extends AbstractHealthChecker {

  protected abstract Class<? extends IMomTransport> getConfiguredMomClass();

  private final boolean m_isActive;

  public AbstractMomHealthChecker() {
    m_isActive = initializeActive();
  }

  @Override
  protected long getConfiguredTimeoutMillis() {
    return TimeUnit.SECONDS.toMillis(30);
  }

  protected boolean initializeActive() {
    IMomTransport momTransport = BEANS.opt(getConfiguredMomClass());
    return momTransport != null && !momTransport.isNullTransport();
  }

  @Override
  public boolean isActive() {
    return m_isActive;
  }

  @Override
  protected boolean execCheckHealth() throws Exception {
    IMomTransport momTransport = BEANS.get(getConfiguredMomClass());
    return momTransport.isReady();
  }

}
