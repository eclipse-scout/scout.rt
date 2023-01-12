/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class JsonTableListeners extends AbstractGroupedListenerList<JsonTableListener, JsonTableEvent, Integer> {
  private static final Set<Integer> KNOWN_EVENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(JsonTableEvent.TYPE_ROWS_INSERTED, JsonTableEvent.TYPE_ROWS_DELETED)));

  @Override
  protected Integer eventType(JsonTableEvent event) {
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
  protected void handleEvent(JsonTableListener listener, JsonTableEvent event) {
    listener.tableChanged(event);
  }
}
