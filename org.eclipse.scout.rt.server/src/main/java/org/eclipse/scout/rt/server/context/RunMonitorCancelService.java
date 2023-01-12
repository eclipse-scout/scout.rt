/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.context;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.services.common.context.IRunMonitorCancelService;

public class RunMonitorCancelService implements IRunMonitorCancelService {

  @Override
  public boolean cancel(long requestSequence) {
    return BEANS.get(RunMonitorCancelRegistry.class).cancelAllBySessionIdAndRequestId(ServerSessionProvider.currentSession().getId(), requestSequence);
  }
}
