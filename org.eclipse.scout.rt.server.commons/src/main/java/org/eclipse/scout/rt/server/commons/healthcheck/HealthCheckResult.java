/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.healthcheck;

import java.util.List;

/**
 * Result object for {@link HealthCheckService}.
 */
public final class HealthCheckResult {

  private List<IHealthChecker> m_checks;
  private List<IHealthChecker> m_failed;

  public HealthCheckResult(List<IHealthChecker> checks, List<IHealthChecker> failed) {
    m_checks = checks;
    m_failed = failed;
  }

  /**
   * @return list of all executed {@link IHealthChecker} checks.
   */
  public List<IHealthChecker> getAllChecks() {
    return m_checks;
  }

  /**
   * @return list of failed {@link IHealthChecker} checks.
   */
  public List<IHealthChecker> getFailedChecks() {
    return m_failed;
  }

  /**
   * @return {@code true} if all health checks where executed successful, otherwise {@code false} if at least one check
   * failed.
   */
  public boolean isSuccess() {
    return m_failed.isEmpty();
  }
}
