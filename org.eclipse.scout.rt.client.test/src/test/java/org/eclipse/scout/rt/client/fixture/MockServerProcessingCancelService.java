/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.fixture;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.context.IRunMonitorCancelService;

public class MockServerProcessingCancelService implements IRunMonitorCancelService {

  @Override
  public boolean cancel(long requestSequence) {
    Thread t = BEANS.get(MockServiceTunnel.class).getThreadByRequestSequence(requestSequence);
    if (t != null) {
      t.interrupt();
      return true;
    }
    return false;
  }
}
