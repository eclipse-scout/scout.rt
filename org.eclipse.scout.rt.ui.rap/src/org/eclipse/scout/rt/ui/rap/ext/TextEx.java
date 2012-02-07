/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext;

import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * <h3>TextEx</h3> Added disabled copy menu
 * 
 * @since 3.7.0 June 2011
 */
public class TextEx extends Text {
  private static final long serialVersionUID = 1L;

  public TextEx(Composite parent, int style) {
    super(parent, style);
  }

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) getDisplay().getData(IRwtEnvironment.class.getName());
  }

  @Override
  protected void checkSubclass() {
    // allow subclassing
  }

  @Override
  public boolean setFocus() {
    boolean editable = getEditable();
    if (editable) {
      super.setFocus();
    }
    return editable;
  }

  public void setOnFieldLabel(String text) {
    checkWidget();
    if (text == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    if (text != null) {
      setText(text);
    }
  }
}
