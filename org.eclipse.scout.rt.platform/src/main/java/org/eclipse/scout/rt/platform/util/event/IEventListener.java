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
 * Represents an action that is triggered by an {@link IEventEmitter} when an event is fired.
 *
 * @param <EVENT>
 *     Event type that is fired.
 * @since 22.0
 */
@FunctionalInterface
public interface IEventListener<EVENT extends EventObject> {

  void fireEvent(EVENT event);
}
