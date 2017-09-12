/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui;

import java.util.EventListener;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;

/**
 * Listener to observe arbitrary changes on any abstract data model or enumerations see
 * {@link IDesktop#addDataChangeListener(DataChangeListener, Object...)} and
 * {@link IDesktop#removeDataChangeListener(DataChangeListener, Object...)}
 */
@FunctionalInterface
public interface DataChangeListener extends EventListener {

  void dataChanged(Object... dataTypes);

}
