/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.mom;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.mom.api.AbstractMomTransport;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.healthcheck.AbstractHealthChecker;

/**
 * @since 6.1
 */
public abstract class AbstractMomHealthChecker extends AbstractHealthChecker {

  protected abstract Class<? extends AbstractMomTransport> getConfiguredMomClass();

  /**
   * @param implementor
   *          {@link IMomImplementor} from {@link AbstractMomTransport#getImplementor()}
   */
  protected abstract boolean execCheckHealth(IMomImplementor implementor) throws Exception;

  private final boolean m_isActive;

  public AbstractMomHealthChecker() {
    m_isActive = initializeActive();
  }

  @Override
  protected long getConfiguredTimeoutMillis() {
    return TimeUnit.SECONDS.toMillis(30);
  }

  protected boolean initializeActive() {
    AbstractMomTransport momTransport = BEANS.opt(getConfiguredMomClass());
    return momTransport != null && !momTransport.isNullTransport();
  }

  @Override
  public boolean isActive() {
    return m_isActive;
  }

  @Override
  protected boolean execCheckHealth() throws Exception {
    IMomImplementor implementor = BEANS.get(getConfiguredMomClass()).getImplementor();
    if (implementor == null) {
      return false;
    }
    return execCheckHealth(implementor);
  }

}
