/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.datachange;

public interface IDataChangeObserver {

  /**
   * Register a {@link IDataChangeListener} on the desktop for these dataTypes
   */
  void registerDataChangeListener(Object... dataTypes);

  /**
   * Unregister the {@link IDataChangeListener} from the desktop for these dataTypes
   */
  void unregisterDataChangeListener(Object... dataTypes);
}
