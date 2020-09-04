/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class WidgetListeners extends AbstractGroupedListenerList<WidgetListener, WidgetEvent, Integer> {
  private static final Set<Integer> KNOWN_EVENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
      WidgetEvent.TYPE_SCROLL_TO_TOP,
      WidgetEvent.TYPE_FOCUS_IN,
      WidgetEvent.TYPE_FOCUS_OUT)));

  @Override
  protected Integer eventType(WidgetEvent event) {
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
  protected void handleEvent(WidgetListener listener, WidgetEvent event) {
    listener.widgetChanged(event);
  }
}
