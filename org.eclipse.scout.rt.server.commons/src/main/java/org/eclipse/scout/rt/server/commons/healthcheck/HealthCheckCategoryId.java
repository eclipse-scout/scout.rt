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

import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.healthcheck.IHealthChecker.IHealthCheckCategory;

/**
 * Identifier for a specific check category.
 *
 * @see IHealthCheckCategory
 */
public final class HealthCheckCategoryId extends AbstractStringId {

  private static final long serialVersionUID = 1L;

  private HealthCheckCategoryId(String id) {
    super(id);
  }

  public static HealthCheckCategoryId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new HealthCheckCategoryId(id);
  }
}
