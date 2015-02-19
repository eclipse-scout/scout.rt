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
package org.eclipse.scout.rt.ui.swing.form.fields.plannerfield.layout;

import java.awt.Dimension;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;

public class SwingScoutGridData {
  private Dimension[] m_sizes;

  public SwingScoutGridData(ISwingEnvironment env, GridData gd) {
    m_sizes = new Dimension[3];
    //
    int gridW = gd.w;
    int gridH = gd.h;
    boolean verticallyResizable = (gd.weightY > 0 || (gd.weightY < 0 && gd.h >= 2));
    boolean horizontallyResizable = true;
    // min
    Dimension d = new Dimension();
    if (horizontallyResizable) {
      d.width = 32;
    }
    else {
      d.width = env.getFormColumnWidth() * gridW + env.getFormColumnGap() * Math.max(0, gridW - 1);
    }
    if (verticallyResizable) {
      d.height = env.getFormRowHeight() * 2 + env.getFormRowGap() * 1;
    }
    else {
      d.height = env.getFormRowHeight() * gridH + env.getFormRowGap() * Math.max(0, gridH - 1);
    }
    m_sizes[SwingLayoutUtility.MIN] = d;
    // pref
    d = new Dimension();
    d.width = env.getFormColumnWidth() * gridW + env.getFormColumnGap() * Math.max(0, gridW - 1);
    d.height = env.getFormRowHeight() * gridH + env.getFormRowGap() * Math.max(0, gridH - 1);
    m_sizes[SwingLayoutUtility.PREF] = d;
    // max
    d = new Dimension();
    if (horizontallyResizable) {
      d.width = 10240;
    }
    else {
      d.width = env.getFormColumnWidth() * gridW + env.getFormColumnGap() * Math.max(0, gridW - 1);
    }
    if (verticallyResizable) {
      d.height = 10240;
    }
    else {
      d.height = env.getFormRowHeight() * gridH + env.getFormRowGap() * Math.max(0, gridH - 1);
    }
    m_sizes[SwingLayoutUtility.MAX] = d;
  }

  public Dimension getLayoutSize(int sizeflag) {
    return new Dimension(m_sizes[sizeflag]);
  }

}
