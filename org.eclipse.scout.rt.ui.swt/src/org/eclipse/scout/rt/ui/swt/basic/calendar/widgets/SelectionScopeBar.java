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

import java.util.Calendar;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.basic.calendar.DisplayMode;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Bar to select the calendar type (monthly, weekly, etc.) and condensed mode.
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class SelectionScopeBar extends Composite {
  protected static final IScoutLogger LOG = ScoutLogManager.getLogger(SelectionScopeBar.class);

  /** parent composite */
  private SwtCalendar m_calendar;

  /* contained widgets */
  private Button m_dayWidget;
  private Button m_workWeekWidget;
  private Button m_weekWidget;
  private Button m_monthWidget;
  //private LabelledCombo m_firstDayWidget;
  private Button m_condensedWidget;

  /** display mode of the calendar */
  private int m_scope;
  private int m_firstDay;
  private boolean m_condensed;

  public SelectionScopeBar(SwtCalendar parent, int style) {
    super(parent, style);

    // ref to parent
    m_calendar = parent;

    // first day of week per default initialized to the locale default
    m_firstDay = Calendar.getInstance().getFirstDayOfWeek();

    createControls(parent);
    addListeners();

    // set default scope
    setDisplayMode(DisplayMode.MONTH);

  }

  protected void createControls(Composite parent) {

    GridData gd;

    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalAlignment = GridData.FILL;
    this.setLayoutData(gd);

    // create new grid layout
    GridLayout layout = new GridLayout();
    layout.marginWidth = 8;
    layout.marginHeight = 8;

    layout.horizontalSpacing = 5;
    layout.verticalSpacing = 0;
    layout.numColumns = 6;
    this.setLayout(layout);

    // create button 1
    m_dayWidget = new Button(this, SWT.RADIO);
    m_dayWidget.setText(SwtUtility.getNlsText(Display.getCurrent(), "Day"));
    m_dayWidget.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
    m_dayWidget.setLayoutData(gd);

    // create button 2
    m_workWeekWidget = new Button(this, SWT.RADIO);
    m_workWeekWidget.setText(SwtUtility.getNlsText(Display.getCurrent(), "WorkWeek"));
    m_workWeekWidget.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
    m_workWeekWidget.setLayoutData(gd);

    // create button 3
    m_weekWidget = new Button(this, SWT.RADIO);
    m_weekWidget.setText(SwtUtility.getNlsText(Display.getCurrent(), "Week"));
    m_weekWidget.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
    m_weekWidget.setLayoutData(gd);

    // create button 4
    m_monthWidget = new Button(this, SWT.RADIO);
    m_monthWidget.setText(SwtUtility.getNlsText(Display.getCurrent(), "Month"));
    m_monthWidget.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
    m_monthWidget.setLayoutData(gd);

    // create the label and combo
//    m_firstDayWidget = new LabelledCombo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
//    m_firstDayWidget.setBackground(SWTColors.getInstance().getWhite());
//
//    // get days of the week and add them
//    String[] wdOneBased = new DateFormatSymbols(Locale.getDefault()).getWeekdays();
//    String[] wdZeroBased = new String[7];
//    System.arraycopy(wdOneBased, 1, wdZeroBased, 0, 7);
//    for (String s : wdZeroBased)
//      m_firstDayWidget.comboAdd(s);
//    m_firstDayWidget.comboSetVisibleItemCount(7);
//    m_firstDayWidget.comboSelect(Calendar.MONDAY - 1);
//    m_firstDayWidget.labelSetText(SwtUtility.getNlsText(Display.getCurrent(),"FirstDayOfWeek") + ":");
//    gd = new GridData();
//    gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
//    m_firstDayWidget.setLayoutData(gd);

    // create button 5
    m_condensedWidget = new Button(this, SWT.CHECK);
    m_condensedWidget.setText(SwtUtility.getNlsText(Display.getCurrent(), "Condensed"));
    m_condensedWidget.setBackground(SwtColors.getInstance().getWhite());
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalAlignment = GridData.END;
    m_condensedWidget.setLayoutData(gd);

    setBackground(SwtColors.getInstance().getWhite());
  }

  /** set display mode, e.g. month, week, working week, day */
  public void setDisplayMode(int scope) {
    m_scope = scope;
    switch (scope) {
      case DisplayMode.DAY:
        resetDisplayMode();
        m_dayWidget.setSelection(true);
        break;

      case DisplayMode.WORKWEEK:
        resetDisplayMode();
        m_workWeekWidget.setSelection(true);
        break;

      case DisplayMode.WEEK:
        resetDisplayMode();
        m_weekWidget.setSelection(true);
        break;

      case DisplayMode.MONTH:
        resetDisplayMode();
        m_monthWidget.setSelection(true);
    }
  }

  /** get display mode */
  public int getDisplayMode() {
    return m_scope;
  }

  protected void resetDisplayMode() {
    m_dayWidget.setSelection(false);
    m_workWeekWidget.setSelection(false);
    m_weekWidget.setSelection(false);
    m_monthWidget.setSelection(false);
  }

  /**
   * set id of the set week day, one based, as in the constants defined in the
   * Calendar class
   */
//  public void setFirstDayOfWeek(int day) {
//    m_firstDay = day;
//    m_firstDayWidget.comboSelect(day - 1);
//  }

  /**
   * get id of the set week day, one based, as in the constants defined in the
   * Calendar class
   */
  public int getFirstDayOfWeek() {
    return m_firstDay;
  }

  /** set state of widget representing condensed mode */
  public void setCondensedMode(boolean condensed) {
    m_condensed = condensed;
    m_condensedWidget.setSelection(condensed);
  }

  /** get state of widget representing condensed mode */
  public boolean getCondensedMode() {
    return m_condensed;
  }

  protected void addListeners() {
    m_dayWidget.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_calendar.setDisplayMode(DisplayMode.DAY);
      }
    });
    m_workWeekWidget.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_calendar.setDisplayMode(DisplayMode.WORKWEEK);
      }
    });
    m_weekWidget.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_calendar.setDisplayMode(DisplayMode.WEEK);
      }
    });
    m_monthWidget.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_calendar.setDisplayMode(DisplayMode.MONTH);
      }
    });
//    m_firstDayWidget.addSelectionListener(new SelectionAdapter() {
//      @Override
//      public void widgetSelected(SelectionEvent e) {
//        m_calendar.setFirstDayOfWeek(((Combo) e.getSource()).getSelectionIndex() + 1);
//      }
//    });
    m_condensedWidget.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_calendar.setCondensedMode(((Button) e.getSource()).getSelection());
      }
    });
  }

  @Override
  public void dispose() {
    if (m_dayWidget != null && !m_dayWidget.isDisposed()) {
      m_dayWidget.dispose();
    }

    if (m_workWeekWidget != null && !m_workWeekWidget.isDisposed()) {
      m_workWeekWidget.dispose();
    }

    if (m_weekWidget != null && !m_weekWidget.isDisposed()) {
      m_weekWidget.dispose();
    }

    if (m_monthWidget != null && !m_monthWidget.isDisposed()) {
      m_monthWidget.dispose();
    }

    super.dispose();
  }
}
