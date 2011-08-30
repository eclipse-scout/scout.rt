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

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Bar holding the day names in the weekly or monthly view.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class DayNamesBar extends Composite implements PaintListener {

  /** how many day names to show? */
  private int m_nbDays;

  /** condensed mode */
  private boolean m_condensed;

  /** reference to the calendar widget */
  private SwtCalendar m_calendar;

  /** store the collection of labels */
  private ArrayList<Label> labelList = new ArrayList<Label>();

  public DayNamesBar(Composite parent, int style, SwtCalendar calendar) {
    // per default, 7 days to show
    this(parent, style, calendar, 7, false);
  }

  public DayNamesBar(Composite parent, int style, SwtCalendar calendar, int nbDays, boolean condensed) {
    super(parent, style);

    m_nbDays = nbDays;
    m_condensed = condensed;
    m_calendar = calendar;

    createControls();
  }

  public void createControls() {

    // how many columns? (condensed mode)
    int nbCols;
    if (m_nbDays == 7 && m_condensed) {
      nbCols = 6;
    }
    else {
      nbCols = m_nbDays;
    }

    GridData gd;
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
    this.setLayoutData(gd);

    // create new grid layout
    GridLayout layout = new GridLayout();
    layout.makeColumnsEqualWidth = true;
    layout.marginWidth = 2;
    layout.marginHeight = 2;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.numColumns = nbCols;
    this.setLayout(layout);

    // get the day names (starting sunday, one based)
    String[] wdOneBased = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();
    String[] wdZeroBased = new String[7];
    System.arraycopy(wdOneBased, 1, wdZeroBased, 0, 7);

    // wdZeroBased: begins with Sunday
    int wdOffset = m_calendar.getFirstDayOfWeek() - 1;

    for (int i = 0; i < nbCols; i++) {
      // create label
      Label l = new Label(this, SWT.CENTER);

      // define its text
      if (nbCols == 6 && i == nbCols - 1) {
        // condensed mode + week end day -> two days together
        String day1 = wdZeroBased[(i + wdOffset) % 7];
        String day2 = wdZeroBased[(i + 1 + wdOffset) % 7];
        l.setText(day1 + "/" + day2);
      }
      else {
        l.setText(wdZeroBased[(i + wdOffset) % 7]);
      }

      l.setBackground(SwtColors.getInstance().getWhite());
      gd = new GridData();
      gd.horizontalAlignment = GridData.CENTER;
      gd.grabExcessHorizontalSpace = true;
      l.setLayoutData(gd);

      // add it to list
      labelList.add(l);
    }

    // add paint listener
    addPaintListener(this);
  }

  @Override
  public void paintControl(PaintEvent e) {
    setBackground(SwtColors.getInstance().getWhite());
    setForeground(SwtColors.getInstance().getGray());

    Rectangle bounds = getBounds();
    e.gc.drawRectangle(0, 0, bounds.width - 1, bounds.height - 1);

  }

  @Override
  public void dispose() {
    // dispose all created labels
    for (Label l : labelList) {
      if (l != null && !l.isDisposed()) {
        l.dispose();
      }
    }

    super.dispose();
  }

}
