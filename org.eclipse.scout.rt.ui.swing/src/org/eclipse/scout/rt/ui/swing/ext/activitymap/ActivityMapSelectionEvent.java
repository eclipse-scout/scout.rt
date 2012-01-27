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
package org.eclipse.scout.rt.ui.swing.ext.activitymap;

import java.util.EventObject;

public class ActivityMapSelectionEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private int[] m_rows;
  private double[] m_range;

  public ActivityMapSelectionEvent(JActivityMap source, int[] rows, double[] range) {
    super(source);
    m_rows = rows;
    m_range = range;
  }

  public int[] getRows() {
    return m_rows;
  }

  public double[] getRange() {
    return m_range;
  }

}
