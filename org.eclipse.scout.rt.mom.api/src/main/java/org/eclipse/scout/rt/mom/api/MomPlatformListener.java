/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
