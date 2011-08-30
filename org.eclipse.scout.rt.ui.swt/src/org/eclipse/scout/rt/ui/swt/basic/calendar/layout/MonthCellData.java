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
package org.eclipse.scout.rt.ui.swt.basic.calendar.layout;

/**
 * Data object regardint a MonthCellLayout.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public final class MonthCellData {

  private int verticalSpan = 1;

  private int horizontalSpan = 1;

  public int getVerticalSpan() {
    return verticalSpan;
  }

  public void setVerticalSpan(int verticalSpan) {
    this.verticalSpan = verticalSpan;
  }

  public int getHorizontalSpan() {
    return horizontalSpan;
  }

  public void setHorizontalSpan(int horizontalSpan) {
    this.horizontalSpan = horizontalSpan;
  }
}
