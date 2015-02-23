package org.eclipse.scout.rt.ui.swt.basic.calendar.widgets;

import org.eclipse.scout.rt.ui.swt.basic.calendar.CalendarConstants;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Represents a day timeline on the left part of the calendar pane (only for
 * day, workweek and week mode).
 * 
 * @author Michael Rudolf, Andreas Hoegger
 */
public class TimelineColumn extends Composite implements PaintListener {

  /** should we draw a header line */
  private boolean m_drawHeader;

  /** y offset, depends on m_drawHeader */
  private int m_realOffsetY;

  /** ref. to central calendar panel */
  private CentralPanel m_centralPanel;

  public TimelineColumn(CentralPanel parent, int style) {
    this(parent, style, true);
  }

  public TimelineColumn(CentralPanel parent, int style, boolean drawHeader) {
    super(parent, style);

    /* store ref to central panel */
    m_centralPanel = parent;

    m_drawHeader = drawHeader;

    // real y offset in use
    m_realOffsetY = m_drawHeader ? CalendarConstants.OFFSET_CELL_HEADER_Y : 0;
  }

  /** create controls and add listeners */
  public void init() {
    createControls(m_centralPanel);
    addPaintListener(this);
  }

  protected void createControls(Composite parent) {
    GridData gd;

    gd = new GridData();
    gd.widthHint = CalendarConstants.TIMELINE_WIDTH;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = GridData.BEGINNING;
    gd.verticalAlignment = GridData.FILL;
    this.setLayoutData(gd);

    setBackground(SwtColors.getInstance().getWhite());
  }

  @Override
  public void paintControl(PaintEvent e) {
    // set drawing color
    e.gc.setForeground(SwtColors.getInstance().getGray());

    // draw borders and timeline
    drawBorder(e);
    drawTimeline(e);
  }

  protected void drawBorder(PaintEvent e) {
    Rectangle bounds = getBounds();
    e.gc.drawRectangle(0, 0, bounds.width - 1, bounds.height - 1);
  }

  protected void drawTimeline(PaintEvent e) {

    Rectangle bounds = getBounds();

    // timeless height: 24 pixels x the max nb of timeless items but at most the third of the cell height
    int timelessHeight = Math.min(24 * m_centralPanel.getTimelessMaxCount(), 33 * bounds.height / 100);
    // hTimeless: timelessHeight - 1 but at least 0
    int hTimeless = Math.max(0, timelessHeight - 1);
    int yTimed = m_realOffsetY + hTimeless + 1;

    int slots = (m_centralPanel.getCalendar().getEndHour() - m_centralPanel.getCalendar().getStartHour());
    double deltaY = Double.valueOf((bounds.height - yTimed) / (slots * 1.0));

    // draw noon rect
    int x1 = 1;
    int y1 = (int) Math.round(deltaY * (12 - m_centralPanel.getCalendar().getStartHour())) + yTimed;
    int x2 = bounds.width - 3;
    int y2 = (int) Math.round(deltaY);
    // right background color within noon rectangle
    Rectangle noon = new Rectangle(x1, y1, x2, y2);
    if (m_centralPanel.getCalendar().getMarkNoonHour()) {
      e.gc.setBackground(SwtColors.getInstance().getLightgray());
    }
    else {
      e.gc.setBackground(SwtColors.getInstance().getWhite());
    }
    e.gc.fillRectangle(noon);
    e.gc.setBackground(SwtColors.getInstance().getWhite());

    int time = m_centralPanel.getCalendar().getStartHour();
    for (int i = 0; i < slots; i++) {
      int y = (int) Math.round(deltaY * i) + yTimed;

      e.gc.drawLine(0, y, bounds.width - 1, y);

      if (time <= m_centralPanel.getCalendar().getStartHour() && m_centralPanel.getCalendar().getUseOverflowCells()) {
        e.gc.drawText(SwtUtility.getNlsText(Display.getCurrent(), "Calendar_earlier"), 3, y + 1, true);
      }
      else if ((time >= m_centralPanel.getCalendar().getEndHour() - 1) && m_centralPanel.getCalendar().getUseOverflowCells()) {
        e.gc.drawText(SwtUtility.getNlsText(Display.getCurrent(), "Calendar_later"), 3, y + 1, true);
      }
      else {
        String hours = time < 10 ? "0" + time + ":00" : "" + time + ":00";
        e.gc.drawText(hours, 3, y + 1, true);
      }

      time++;
    }

  }

}
