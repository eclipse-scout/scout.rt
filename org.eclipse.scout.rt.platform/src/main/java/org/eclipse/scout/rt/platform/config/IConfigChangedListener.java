/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.config;

import java.util.EventListener;

/**
 * Interface for listeners that want to be notified if the state or value of a {@link IConfigProperty} changes.
 */
@FunctionalInterface
public interface IConfigChangedListener extends EventListener {
  /**
   * Callback indicating that the given event occurred.
   *
   * @param event
   *     The event describing the change.
   */
  void configPropertyChanged(ConfigPropertyChangeEvent event);
}
