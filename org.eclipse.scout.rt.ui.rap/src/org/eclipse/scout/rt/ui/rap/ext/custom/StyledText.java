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
package org.eclipse.scout.rt.ui.rap.ext.custom;

import org.eclipse.scout.rt.ui.rap.ext.TextEx;
import org.eclipse.swt.widgets.Composite;

//XXX [rap]
public class StyledText extends TextEx {
  private static final long serialVersionUID = 1L;

  public StyledText(Composite parent, int style) {
    super(parent, style);
  }

  public void setCaretOffset(int i) {
    super.setSelection(0, 0);
  }

}
