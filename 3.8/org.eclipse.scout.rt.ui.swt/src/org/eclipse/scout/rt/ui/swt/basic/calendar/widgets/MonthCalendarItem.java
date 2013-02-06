/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic.calendar.widgets;

import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarItemContainer;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;

/**
 * Item within a monthly calendar.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class MonthCalendarItem extends AbstractCalendarItem {

  public MonthCalendarItem(AbstractCell parent, int style, CalendarItemContainer item) {
    super(parent, style, item);
  }

  @Override
  protected void createControls() {
  }

  @Override
  protected void setLayout() {

    // set grid data for this composite within parent one
    GridData gd2 = new GridData();
    gd2.horizontalAlignment = GridData.FILL;
    gd2.grabExcessHorizontalSpace = true;
    gd2.horizontalSpan = 2;
    gd2.heightHint = 15; // TODO: check this value
    this.setLayoutData(gd2);

  }

  @Override
  protected void hookListeners() {
    super.hookListeners();
  }

  @Override
  public void paintControl(PaintEvent e) {
    // background color
    Color color = new Color(SwtColors.getStandardDisplay(), getItem().getColor().getRed(), getItem().getColor().getGreen(), getItem().getColor().getBlue());
    if (getItem().getItem().equals(getCell().getCalendar().getSelectedItem())) {
      color = SwtColors.getInstance().getDarker(color);
    }
    setBackground(color);

    // label
    Rectangle r = getBounds();
    //
    // relative coordinate system, origine (0,0)
    r.x = 0;
    r.y = 0;
    getItem().setLabeled(true);/*r.height>=fm.getAscent() && r.width>=SWITCH_ITEM_WIDTH*/
    FontMetrics fm = e.gc.getFontMetrics();
    if (getItem().isLabeled()) {
      String s = getCell().getCalendar().getModel().getLabel(getItem().getItem(), getCell().getDate().getTime());
      int centery = Math.max(0, (r.height - fm.getAscent()) / 2 - 3);
      e.gc.drawString(s, r.x + 3, r.y + centery);
    }

  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
