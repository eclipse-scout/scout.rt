/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.rt.client.ui.AbstractEventHistory;

/**
 * The default implementation is created in {@link AbstractForm#createEventHistory()} and tracks
 * <ul>
 * <li>{@link FormEvent#TYPE_REQUEST_FOCUS}</li>
 * <li>{@link FormEvent#TYPE_TO_BACK}</li>
 * <li>{@link FormEvent#TYPE_TO_FRONT}</li>
 * </ul>
 * <p>
 * This object is thread safe.
 *
 * @since 3.8
 */
public class DefaultFormEventHistory extends AbstractEventHistory<FormEvent> {

  /**
   * @param timeToLiveMillis
   */
  public DefaultFormEventHistory(long timeToLiveMillis) {
    super(timeToLiveMillis);
  }

  @Override
  public void notifyEvent(FormEvent event) {
    switch (event.getType()) {
      case FormEvent.TYPE_REQUEST_FOCUS:
      case FormEvent.TYPE_REQUEST_INPUT:
      case FormEvent.TYPE_TO_BACK:
      case FormEvent.TYPE_TO_FRONT: {
        addToCache(event.getType(), event);
        break;
      }
    }
  }
}
