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

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.Platform;

public class PlatformHealthChecker extends AbstractHealthChecker {

  @Override
  protected boolean execCheckHealth(HealthCheckCategoryId category) throws Exception {
    return Platform.get().getState() == State.PlatformStarted;
  }
}
