/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.event;

import java.util.EventObject;

/**
 * Describes an object than can emit events of a specific type. The event listeners are managed by an
 * {@link EventSupport} instance. Event listeners can be added or removed.
 *
 * @param <EVENT>
 *     Event type that is fired.
 * @since 22.0
 */
@FunctionalInterface
public interface IEventEmitter<EVENT extends EventObject> {

  /**
   * @return never {@code null}
   */
  EventSupport<EVENT> getEventSupport();

  default void addEventListener(IEventListener<EVENT> listener) {
    getEventSupport().addListener(listener);
  }

  default void removeEventListener(IEventListener<EVENT> listener) {
    getEventSupport().removeListener(listener);
  }
}
