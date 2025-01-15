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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * The <code>IHealthChecker</code> represents a single check to determine the application health status (OK / FAILED).
 *
 * @since 6.1
 */
@ApplicationScoped
public interface IHealthChecker {

  String getName();

  /**
   * @return <code>true</code> if this <code>IHealthChecker</code> has to be checked.
   */
  boolean isActive();

  /**
   * @param category
   *     the {@link IHealthCheckCategory} if provided, <code>null</code> if none is provided
   * @param context
   *     {@link RunContext} used to run the check in.
   * @return <code>true</code> if check was successful, <code>false</code> otherwise.
   */
  boolean checkHealth(RunContext context, HealthCheckCategoryId category);

  /**
   * @param category
   *     non-null category; if no category is available all checks are considered which are marked as
   *     {@link #isActive()}
   * @return <code>true</code> if this <code>IHealthChecker</code> accepts the provided category; <code>false</code> to
   * exclude this check for this category
   */
  default boolean acceptCategory(HealthCheckCategoryId category) {
    return true;
  }

  /**
   * Interface defining the possible health check categories
   */
  @ApplicationScoped
  public static interface IHealthCheckCategory {
    HealthCheckCategoryId getId();
  }

  // --------------------------------------
  // default categories

  public static final class Startup implements IHealthCheckCategory {
    public static final HealthCheckCategoryId ID = HealthCheckCategoryId.of("startup");

    @Override
    public HealthCheckCategoryId getId() {
      return ID;
    }
  }

  public static final class Readiness implements IHealthCheckCategory {
    public static final HealthCheckCategoryId ID = HealthCheckCategoryId.of("readiness");

    @Override
    public HealthCheckCategoryId getId() {
      return ID;
    }
  }

  public static final class Liveness implements IHealthCheckCategory {
    public static final HealthCheckCategoryId ID = HealthCheckCategoryId.of("liveness");

    @Override
    public HealthCheckCategoryId getId() {
      return ID;
    }
  }
}
