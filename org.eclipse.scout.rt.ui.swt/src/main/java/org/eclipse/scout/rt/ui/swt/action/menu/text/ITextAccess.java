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
package org.eclipse.scout.rt.ui.swt.action.menu.text;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
public interface ITextAccess {

  boolean isEnabled();

  boolean isEditable();

  /**
   * @return
   */
  Point getSelection();

  /**
   * @return
   */
  String getText();

  /**
   * @return
   */
  String getSelectedText();

  /**
   * @return
   */
  Control getTextControl();

  /**
   * @return
   */
  boolean hasSelection();

  boolean isMasked();

  void copy();

  void paste();

  /**
   * @return
   */
  boolean hasTextOnClipboard();

  /**
   *
   */
  void cut();
}
