/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
