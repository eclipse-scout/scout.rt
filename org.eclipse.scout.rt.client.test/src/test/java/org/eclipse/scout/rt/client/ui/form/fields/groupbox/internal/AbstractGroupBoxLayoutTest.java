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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import static org.junit.Assert.assertEquals;

import java.io.PrintStream;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
public class AbstractGroupBoxLayoutTest {
  public static final double EPS = 1E-6f;

  protected void assertGridData(int x, int y, int w, int h, GridData gd) {
    assertEquals("GridData[x]:", x, gd.x);
    assertEquals("GridData[y]:", y, gd.y);
    assertEquals("GridData[w]:", w, gd.w);
    assertEquals("GridData[h]:", h, gd.h);

  }

  protected void assertGridData(int x, int y, int w, int h, double weightX, double weightY, GridData gd) {
    assertGridData(x, y, w, h, gd);
    assertEquals("GridData[weightX]:", weightX, gd.weightX, EPS);
    assertEquals("GridData[weightY]:", weightY, gd.weightY, EPS);
  }

  protected void printLayout(IFormField field, PrintStream out, int level) {
    for (int i = 0; i < level; i++) {
      out.print("  ");
    }
    out.println("field [" + field + "] GridData [" + field.getGridData() + "]");
    if (field instanceof ICompositeField) {
      for (IFormField f : ((ICompositeField) field).getFields()) {
        printLayout(f, out, level + 1);
      }
    }
  }
}
