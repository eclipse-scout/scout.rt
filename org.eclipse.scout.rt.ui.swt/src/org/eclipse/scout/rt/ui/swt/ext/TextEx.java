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
package org.eclipse.scout.rt.ui.swt.ext;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * <h3>TextEx</h3>
 * 
 * @since 1.0.0 07.05.2008
 */
public class TextEx extends Text {

  public TextEx(Composite parent, int style) {
    super(parent, style);
  }

  @Override
  protected void checkSubclass() {
    // allow subclassing
  }

  @Override
  public boolean setFocus() {
    if (getEditable()) {
      return super.setFocus();
    }
    return false;
  }
}
