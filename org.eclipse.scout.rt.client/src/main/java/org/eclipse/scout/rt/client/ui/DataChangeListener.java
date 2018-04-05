/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeListener;

/**
 * Listener to observe arbitrary changes on any abstract data model or enumerations see
 * {@link IDesktop#addDataChangeListener(IDataChangeListener, Object...)} and
 * {@link IDesktop#removeDataChangeListener(IDataChangeListener, Object...)}
 * <p>
 *
 * @deprecated use {@link IDataChangeListener} instead
 */
@Deprecated
@FunctionalInterface
public interface DataChangeListener extends IDataChangeListener {

  void dataChanged(Object... eventTypes);

  @Override
  default void dataChanged(DataChangeEvent event) {
    dataChanged(event.getEventType());
  }
}
