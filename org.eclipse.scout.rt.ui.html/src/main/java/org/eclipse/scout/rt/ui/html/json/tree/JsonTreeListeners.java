/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class JsonTreeListeners extends AbstractGroupedListenerList<JsonTreeListener, JsonTreeEvent, Integer> {
  private static final Set<Integer> KNOWN_EVENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(JsonTreeEvent.TYPE_NODES_INSERTED, JsonTreeEvent.TYPE_NODES_DELETED)));

  @Override
  protected Integer eventType(JsonTreeEvent event) {
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
  protected void handleEvent(JsonTreeListener listener, JsonTreeEvent event) {
    listener.treeChanged(event);
  }
}
