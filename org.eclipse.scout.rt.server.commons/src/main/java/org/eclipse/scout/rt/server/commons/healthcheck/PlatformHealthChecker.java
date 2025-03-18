/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.healthcheck;

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

public class PlatformHealthChecker extends AbstractHealthChecker {

  @Override
  protected boolean execCheckHealth(HealthCheckCategoryId category) throws Exception {
    return Platform.get().getState() == State.PlatformStarted;
  }

  @Override
  public boolean acceptCategory(HealthCheckCategoryId category) {
    return ObjectUtility.isOneOf(category, Startup.ID, Liveness.ID);
  }
}
