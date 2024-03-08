/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.event;

import java.util.List;

/**
 * Thread-safe event listener list of one listener type
 *
 * @since 8.0
 */
public interface IFastListenerList<LISTENER> {

  boolean isEmpty();

  int size();

  void add(LISTENER listener);

  void add(LISTENER listener, boolean weak);

  void remove(LISTENER listener);

  List<LISTENER> list();
}
