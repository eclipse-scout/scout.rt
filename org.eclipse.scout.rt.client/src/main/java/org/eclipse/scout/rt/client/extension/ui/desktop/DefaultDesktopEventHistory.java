/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop;

import java.util.UUID;

import org.eclipse.scout.rt.client.ui.AbstractEventHistory;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;

/**
 * The default implementation is created in {@link AbstractDesktop#createEventHistory()} and tracks
 * <ul>
 * <li>{@link DesktopEvent#TYPE_OUTLINE_CONTENT_ACTIVATE}</li>
 * <li>{@link DesktopEvent#TYPE_NOTIFICATION_ADDED}</li>
 * <li>{@link DesktopEvent#TYPE_NOTIFICATION_REMOVED}</li>
 * <li>{@link DesktopEvent#TYPE_OPEN_URI}</li>
 * <li>{@link DesktopEvent#TYPE_FORM_ACTIVATE}</li>
 * </ul>
 * <p>
 * This object is thread safe.
 *
 * @since 5.2
 */
public class DefaultDesktopEventHistory extends AbstractEventHistory<DesktopEvent> {

  public DefaultDesktopEventHistory(long timeToLiveMillis) {
    super(timeToLiveMillis);
  }

  @Override
  public void notifyEvent(DesktopEvent event) {
    switch (event.getType()) {
      case DesktopEvent.TYPE_OUTLINE_CONTENT_ACTIVATE:
      case DesktopEvent.TYPE_FORM_ACTIVATE: {
        addToCache(event.getType(), event);
        break;
      }
      case DesktopEvent.TYPE_NOTIFICATION_ADDED:
      case DesktopEvent.TYPE_NOTIFICATION_REMOVED:
      case DesktopEvent.TYPE_OPEN_URI: {
        addToCache(UUID.randomUUID().toString(), event);
        break;
      }
    }
  }
}
