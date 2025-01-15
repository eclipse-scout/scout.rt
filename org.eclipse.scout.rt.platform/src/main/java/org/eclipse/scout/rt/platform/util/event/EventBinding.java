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
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Represents an event listener that can be attached to or detached from an {@link IEventEmitter}. Useful when the event
 * listener is created before the event emitter is known or if the event emitter changes dynamically.
 *
 * @param <EVENT>
 *     Event type that is fired.
 * @since 22.0
 */
public class EventBinding<EVENT extends EventObject> {

  private IEventEmitter<EVENT> m_target = null;
  private IEventListener<EVENT> m_listener = null;

  /**
   * Attached an event listener to the given event emitter and notifies the given event handler when an event is fired.
   *
   * @param target
   *     event emitter to attach this binding to
   * @param eventHandler
   *     consumer for events that are emitted by the {@code target}
   * @throws AssertionException
   *     if any of the given arguments is {@code null}
   * @throws IllegalStateException
   *     if this binding instance is already attached to an event emitter.
   */
  public void attach(IEventEmitter<EVENT> target, Consumer<EVENT> eventHandler) {
    Assertions.assertNotNull(target, "Missing target");
    Assertions.assertNotNull(eventHandler, "Missing event handler");
    if (isAttached()) {
      throw new IllegalStateException("Already attached");
    }
    m_target = target;
    m_listener = event -> eventHandler.accept(event);
    m_target.getEventSupport().addListener(m_listener);
  }

  /**
   * Detaches the binding from the previously attached event emitter.
   *
   * @throws IllegalStateException
   *     if this binding instance is not attached. The attach status can be checked with {@link #isAttached()}.
   */
  public void detach() {
    if (!isAttached()) {
      throw new IllegalStateException("Already attached");
    }
    m_target.getEventSupport().removeListener(m_listener);
    m_target = null;
    m_listener = null;
  }

  /**
   * @return {@code true} if this binding is currently attached to an event emitter, {@code false} otherwise.
   */
  public boolean isAttached() {
    return m_listener != null;
  }
}
