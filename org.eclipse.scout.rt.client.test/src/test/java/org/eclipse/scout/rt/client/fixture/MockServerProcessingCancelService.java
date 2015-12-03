/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.fixture;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.context.IRunMonitorCancelService;

public class MockServerProcessingCancelService implements IRunMonitorCancelService {

  public MockServerProcessingCancelService() {
  }

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
