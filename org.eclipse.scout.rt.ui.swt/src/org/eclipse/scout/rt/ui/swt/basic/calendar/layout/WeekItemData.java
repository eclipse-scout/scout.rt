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
  public int timelessMaxCount;

  /** timeless item count in the parent cell */
  public int timelessCount;

  /** timeless index of that item */
  public int timelessIndex;

  /** corresponding CalendarItemContainer of that item */
  public CalendarItemContainer m_item;

  /** height of the offset caused by the presence of a header */
  public int offsetCellHeader = 0;

}
