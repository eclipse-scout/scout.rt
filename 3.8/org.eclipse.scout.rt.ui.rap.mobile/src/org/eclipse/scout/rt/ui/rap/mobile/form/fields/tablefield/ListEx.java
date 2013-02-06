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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tablefield;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class ListEx extends List {

  private static final long serialVersionUID = 1L;

  public ListEx(Composite parent, int style) {
    super(parent, style);
  }

  @SuppressWarnings("null")
  public String getItem(Point point) {
    checkWidget();
    if (point == null) {
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    }
    String result = null;
    Rectangle itemArea = getClientArea();
    if (itemArea.contains(point)) {
      int itemHeight = getItemHeight();
      int index = (point.y / itemHeight) - 1;
      if (point.y % itemHeight != 0) {
        index++;
      }
      index += getTopIndex();
      if (index >= 0 && index < getItemCount()) {
        result = getItem(index);
      }
    }
    return result;
  }

}
