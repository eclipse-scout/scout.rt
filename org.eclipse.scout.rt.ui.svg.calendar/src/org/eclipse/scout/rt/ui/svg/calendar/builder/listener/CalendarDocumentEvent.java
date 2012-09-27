package org.eclipse.scout.rt.ui.svg.calendar.builder.listener;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;

public class CalendarDocumentEvent {
  public static final int TYPE_POPUP_MENU_ACTIVATED = 1;
  public static final int TYPE_SELECTION_CHANGED = 2;
  public static final int TYPE_VISIBLE_RANGE_CHANGED = 4;
  public static final int TYPE_DISPLAY_MODE_MENU_ACTIVATED = 8;

  public final int type;
  public Date selectedDate;
  public CalendarComponent selectedComponent;
  public Date startDate, endDate;
  public int displayMode;

  public CalendarDocumentEvent(int type) {
    this.type = type;
  }
}
