/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;

/**
 * {@link IPlatformListener} to shutdown all MOM transports upon platform shutdown.
 */
@Order(IMom.DESTROY_ORDER)
public class MomPlatformListener implements IPlatformListener {

  @Override
  public void stateChanged(final PlatformEvent event) {
    if (event.getState() == State.PlatformStopping) {
      for (final IMomTransport transport : BEANS.all(IMomTransport.class)) {
        transport.destroy();
      }
    }
  }
}
