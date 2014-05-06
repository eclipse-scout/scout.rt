/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.action.menu;

import org.eclipse.swt.events.SelectionListener;

/**
 *
 */
public interface ISwtContextMenuMarker {

  /**
   * @param visible
   */
  void setMarkerVisible(boolean visible);

  /**
   * @return
   */
  boolean isMarkerVisible();

  /**
   * @param listener
   */
  void addSelectionListener(SelectionListener listener);

  /**
   * @param listener
   * @return
   */
  boolean removeSelectionListener(SelectionListener listener);

}
