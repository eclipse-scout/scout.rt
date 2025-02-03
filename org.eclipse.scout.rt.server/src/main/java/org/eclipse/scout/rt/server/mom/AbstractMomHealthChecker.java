/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.mom;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.mom.api.AbstractMomTransport;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.healthcheck.AbstractHealthChecker;
import org.eclipse.scout.rt.server.commons.healthcheck.HealthCheckCategoryId;

/**
 * @since 6.1
 */
public abstract class AbstractMomHealthChecker extends AbstractHealthChecker {

  protected abstract Class<? extends AbstractMomTransport> getConfiguredMomClass();

  /**
   * @param implementor
   *     {@link IMomImplementor} from {@link AbstractMomTransport#getImplementor()}
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
  protected boolean execCheckHealth(HealthCheckCategoryId category) throws Exception {
    IMomImplementor implementor = BEANS.get(getConfiguredMomClass()).getImplementor();
    if (implementor == null) {
      return false;
    }
    return execCheckHealth(implementor);
  }
}
