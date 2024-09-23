/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class DesktopListeners extends AbstractGroupedListenerList<DesktopListener, DesktopEvent, Integer> {
  private static final Set<Integer> KNOWN_EVENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
      DesktopEvent.TYPE_DESKTOP_CLOSED,
      DesktopEvent.TYPE_OUTLINE_CHANGED,
      DesktopEvent.TYPE_OUTLINE_CONTENT_ACTIVATE,
      DesktopEvent.TYPE_FORM_SHOW,
      DesktopEvent.TYPE_FORM_HIDE,
      DesktopEvent.TYPE_FORM_ACTIVATE,
      DesktopEvent.TYPE_MESSAGE_BOX_SHOW,
      DesktopEvent.TYPE_MESSAGE_BOX_HIDE,
      DesktopEvent.TYPE_FILE_CHOOSER_SHOW,
      DesktopEvent.TYPE_FILE_CHOOSER_HIDE,
      DesktopEvent.TYPE_OPEN_URI,
      DesktopEvent.TYPE_NOTIFICATION_ADDED,
      DesktopEvent.TYPE_NOTIFICATION_REMOVED,
      DesktopEvent.TYPE_RELOAD_GUI)));

  @Override
  protected Integer eventType(DesktopEvent event) {
    return event.getType();
  }

  @Override
  protected Set<Integer> knownEventTypes() {
    return KNOWN_EVENT_TYPES;
  }

  @Override
  protected Integer otherEventsType() {
    return null;
  }

  @Override
  protected void handleEvent(DesktopListener listener, DesktopEvent event) {
    listener.desktopChanged(event);
  }
}
