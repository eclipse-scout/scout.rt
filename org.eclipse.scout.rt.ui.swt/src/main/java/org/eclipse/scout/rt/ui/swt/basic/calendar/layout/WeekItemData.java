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

import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarItemContainer;

/**
 * Contains info regarding a calendar item within a week or day cell,
 * which is necessary for layouting.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public final class WeekItemData {

  /** max timeless item count in any cell of the week */
  private int timelessMaxCount;

  /** timeless item count in the parent cell */
  private int timelessCount;

  /** timeless index of that item */
  private int timelessIndex;

  /** corresponding CalendarItemContainer of that item */
  private CalendarItemContainer m_item;

  /** height of the offset caused by the presence of a header */
  private int offsetCellHeader = 0;

  public int getTimelessMaxCount() {
    return timelessMaxCount;
  }

  public void setTimelessMaxCount(int timelessMaxCount) {
    this.timelessMaxCount = timelessMaxCount;
  }

  public int getTimelessCount() {
    return timelessCount;
  }

  public void setTimelessCount(int timelessCount) {
    this.timelessCount = timelessCount;
  }

  public int getTimelessIndex() {
    return timelessIndex;
  }

  public void setTimelessIndex(int timelessIndex) {
    this.timelessIndex = timelessIndex;
  }

  public CalendarItemContainer getItem() {
    return m_item;
  }

  public void setItem(CalendarItemContainer item) {
    m_item = item;
  }

  public int getOffsetCellHeader() {
    return offsetCellHeader;
  }

  public void setOffsetCellHeader(int offsetCellHeader) {
    this.offsetCellHeader = offsetCellHeader;
  }
}
