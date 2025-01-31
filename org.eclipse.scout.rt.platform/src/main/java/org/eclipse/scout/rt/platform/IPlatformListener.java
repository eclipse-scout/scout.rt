/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import java.util.EventListener;

/**
 * All instances of {@link IPlatformListener} receive state change notifications from the platform.
 *
 * @since 5.1
 */
@FunctionalInterface
@ApplicationScoped
public interface IPlatformListener extends EventListener {

  /**
   * Informs about a state change in the current {@link IPlatform}.
   *
   * @param event
   *     The event describing the state change.
   */
  void stateChanged(PlatformEvent event);
}
