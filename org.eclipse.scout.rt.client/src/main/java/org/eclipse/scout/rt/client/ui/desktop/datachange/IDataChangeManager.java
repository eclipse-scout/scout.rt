/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.datachange;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;

/**
 * Used to handle data change events on entities. The data change manager is used by the {@link IDesktop} and can be
 * accessed by the delegate methods the desktop provides. This is useful to update parts of the UI whenever an insert,
 * update or delete happens on a single entity or on a list of entities.
 *
 * @since 8.0
 */
public interface IDataChangeManager {

  void add(IDataChangeListener listener, boolean weak, Object... dataTypes);

  void addLastCalled(IDataChangeListener listener, boolean weak, Object... dataTypes);

  void addAll(IDataChangeManager dataChangeListeners);

  void remove(IDataChangeListener listener, Object... dataTypes);

  void fireEvent(DataChangeEvent event);

  boolean isBuffering();

  void setBuffering(boolean buffering);
}
