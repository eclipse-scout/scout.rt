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
package org.eclipse.scout.rt.ui.swing.ext;

/**
 *
 */
public interface IDropDownButtonListener{

  /**
   * Fired when icon has been left clicked.
   */
  void iconClicked(Object source);

  /**
   * Fired when arrow has been left clicked, or icon has been clicked with right mouse button.
   */
  void menuClicked(Object source);

}
