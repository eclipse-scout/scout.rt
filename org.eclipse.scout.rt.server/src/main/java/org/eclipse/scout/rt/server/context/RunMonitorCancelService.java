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
package org.eclipse.scout.rt.server.context;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.services.common.context.IRunMonitorCancelService;

public class RunMonitorCancelService implements IRunMonitorCancelService {

  @Override
  public boolean cancel(long requestSequence) {
    return BEANS.get(RunMonitorCancelRegistry.class).cancel(ServerSessionProvider.currentSession(), requestSequence);
  }
}
