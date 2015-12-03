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
package org.eclipse.scout.rt.platform;

import java.util.EventListener;

/**
 * All instances of {@link IPlatformListener} receive state change notifications from the platform.
 *
 * @since 5.1
 */
@ApplicationScoped
public interface IPlatformListener extends EventListener {

  /**
   * Informs about a state change in the current {@link IPlatform}.
   *
   * @param event
   *          The event describing the state change.
   */
  void stateChanged(PlatformEvent event);

}
