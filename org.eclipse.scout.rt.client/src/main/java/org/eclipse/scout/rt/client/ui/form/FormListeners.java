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
package org.eclipse.scout.rt.client.ui.form;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class FormListeners extends AbstractGroupedListenerList<FormListener, FormEvent, Integer> {
  private static final Set<Integer> KNOWN_EVENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
      FormEvent.TYPE_ACTIVATED,
      FormEvent.TYPE_LOAD_BEFORE,
      FormEvent.TYPE_LOAD_AFTER,
      FormEvent.TYPE_LOAD_COMPLETE,
      FormEvent.TYPE_STORE_BEFORE,
      FormEvent.TYPE_STORE_AFTER,
      FormEvent.TYPE_DISCARDED,
      FormEvent.TYPE_CLOSED,
      FormEvent.TYPE_RESET_COMPLETE,
      FormEvent.TYPE_STRUCTURE_CHANGED,
      FormEvent.TYPE_TO_FRONT,
      FormEvent.TYPE_TO_BACK,
      FormEvent.TYPE_REQUEST_FOCUS,
      FormEvent.TYPE_REQUEST_INPUT)));

  @Override
  protected Integer eventType(FormEvent event) {
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
  protected void handleEvent(FormListener listener, FormEvent event) {
    listener.formChanged(event);
  }
}
