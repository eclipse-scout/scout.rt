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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>HealthCheckService</code> uses all available active {@link IHealthChecker} classes to determine the
 * application status. Use {@link #check()} or {@link #check(HealthCheckCategoryId)} to execute all corresponding checks
 * and gather the check status result.
 * <p>
 */
@ApplicationScoped
public class HealthCheckService {

  private static final Logger LOG = LoggerFactory.getLogger(HealthCheckService.class);

  /**
   * Executes all active {@link IHealthChecker} checks.
   */
  public HealthCheckResult check() {
    return check(null);
  }

  /**
   * Executes all active {@link IHealthChecker} checks.
   *
   * @param category
   *     An optional parameter category may be specified, to run only some {@link IHealthChecker} classes. See
   *     {@link IHealthChecker#acceptCategory(HealthCheckCategoryId)} for further explanation of filtering. If no
   *     category is specified (e.g. value {@code null}) all active checks are executed.
   */
  public HealthCheckResult check(HealthCheckCategoryId category) {
    List<IHealthChecker> checks = getActiveHealthCheckers(category);
    List<IHealthChecker> failed = new ArrayList<>();

    RunContext context = execCreateRunContext();
    for (IHealthChecker check : checks) {
      try {
        if (!check.checkHealth(context, category)) {
          failed.add(check);
        }
      }
      catch (Throwable t) { //NOSONAR
        LOG.error("HealthChecker[{}] failed", check.getName(), t);
        failed.add(check);
      }
    }
    return new HealthCheckResult(checks, failed);
  }

  protected RunContext execCreateRunContext() {
    return RunContexts.empty();
  }

  protected List<IHealthChecker> getActiveHealthCheckers(HealthCheckCategoryId category) {
    List<IHealthChecker> all = BEANS.all(IHealthChecker.class);
    List<IHealthChecker> actives = new ArrayList<>(all.size());

    for (IHealthChecker check : all) {
      try {
        if (execAcceptCheck(check, category)) {
          actives.add(check);
        }
        else {
          LOG.debug("HealthChecker[{}] was ignored (called with category {})", check.getName(), category);
        }
      }
      catch (Throwable t) { //NOSONAR
        LOG.error("Active-check crashed with HealthChecker[{}]", check.getName(), t);
      }
    }
    return Collections.unmodifiableList(actives);
  }

  /**
   * @param category
   *     nullable category to be checked; if category is null this parameter is ignored
   * @return <code>false</code> to ignore given <code>IHealthChecker</code>
   */
  protected boolean execAcceptCheck(IHealthChecker check, HealthCheckCategoryId category) {
    return (category == null || check.acceptCategory(category)) && check.isActive();
  }
}
