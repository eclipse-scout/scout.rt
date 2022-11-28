/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
