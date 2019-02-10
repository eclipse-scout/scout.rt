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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * This column may be used if the value is relevant for the gui, and not just the display text.
 */
@ClassId("2804d433-bf07-4b47-aeae-c631fe2e8434")
public abstract class AbstractBeanColumn<VALUE> extends AbstractColumn<VALUE> implements IBeanColumn<VALUE> {

  public AbstractBeanColumn() {
    this(true);
  }

  public AbstractBeanColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredUiSortPossible() {
    return true;
  }

  protected String getPlainText(ITableRow row) {
    return null;
  }

  @Override
  protected void execDecorateCell(Cell cell, ITableRow row) {
    cell.setText(getPlainText(row)); // used for excel export
  }

  /**
   * sorting of rows based on this column<br>
   * in order to get a reasonable sorting the bean needs to implement {@link Comparable} or the subclass of
   * {@link #AbstractBeanColumn} needs to implement {@link #getPlainText(ITableRow)}
   */
  @Override
  @SuppressWarnings("unchecked")
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    VALUE o1 = getValue(r1);
    VALUE o2 = getValue(r2);
    if (o1 == null && o2 == null) {
      return 0;
    }
    if (o1 == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }
    if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
      return ((Comparable) o1).compareTo(o2);
    }

    String plainText1 = getPlainText(r1);
    String plainText2 = getPlainText(r2);
    int c = StringUtility.compareIgnoreCase(plainText1, plainText2);
    if (c != 0) {
      return c;
    }
    return super.compareTableRows(r1, r2);
  }
}
