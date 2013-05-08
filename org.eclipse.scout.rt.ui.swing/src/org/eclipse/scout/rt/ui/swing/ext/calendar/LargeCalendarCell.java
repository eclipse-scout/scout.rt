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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.calendar.DateTimeFormatFactory;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

public class LargeCalendarCell extends AbstractCalendarCell {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LargeCalendarCell.class);
  private static final int SWITCH_ITEM_WIDTH = 60;
  private static final int SWITCH_ITEM_HEIGHT = 40;
  private static final int SWITCH_COMPRESSED_HEIGHT = 4;
  private static final long HOUR_MILLIS = 3600 * 1000;

  private static final Border EMPTY_BORDER = new EmptyBorder(1, 1, 1, 1);
  private static final Dimension MIN_SIZE = new Dimension(10, 10);
  private static final Dimension PREF_SIZE = new Dimension(80, 60);
  private static final Dimension MAX_SIZE = new Dimension(1000, 1000);

  private DateChooser m_dateChooser;
  protected Date m_repDate;
  protected Color m_notMajorColor;
  protected Color m_yesMajorColor;
  protected boolean m_selected;
  // gui
  private boolean m_isMajor;// in current month resp. in current week
  private int m_displayType = DateChooser.DISPLAY_MODE_MONTH;
  private int m_startHour = 6;// 6-7 is for all items before 7 //MIN=0
  private int m_endHour = 19;// 18-19 is for all items after 18 //MAX=23
  private boolean m_useOverflowCells = false;
  private String m_weekLabel;
  private String m_dateLabel;
  private String m_dayLabel;
  private TreeSet<ItemWrapper> m_itemsCached;
  private int m_countTimeless = 0;
  private boolean m_firstColumn = false;// first column in calendar
  private DateFormat m_formatHHMM;

  public LargeCalendarCell(DateChooser dateChooser) {
    m_dateChooser = dateChooser;
    m_formatHHMM = new DateTimeFormatFactory().getHourMinute();
    setLayout(null);
    setOpaque(true);
    setBackground(Color.white);
    m_notMajorColor = new Color(0xeeeeee);
    m_yesMajorColor = getBackground();
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        m_dateChooser.setSelectedItem(getItemAt(e.getPoint()));
      }
    });

    ItemDragGestureListener idgl = new ItemDragGestureListener();
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, idgl);

    CellDropTargetAdapter dta = new CellDropTargetAdapter();
    new DropTarget(this, dta);

    ToolTipManager.sharedInstance().registerComponent(this);
  }

  @Override
  protected void onSpacePressed() {
    m_dateChooser.setSelectedDate(getRepresentedDate());
  }

  @Override
  protected void onFocusGained() {
    m_dateChooser.setSelectedDate(getRepresentedDate());
  }

  @Override
  public void resetItemCache() {
    m_itemsCached = null;
  }

  @Override
  public void setWorkingHours(int startHour, int endHour, boolean useOverflowCells) {
    m_startHour = startHour;
    m_endHour = endHour;
    m_useOverflowCells = useOverflowCells;
  }

  @Override
  public int getTimelessItemCount() {
    getItems();// enforce cache filling
    return m_countTimeless;
  }

  @Override
  public int getTimedItemCount() {
    return getItems().size() - m_countTimeless;
  }

  public Collection/* of sorted ItemWrapper */getItems() {
    if (m_itemsCached == null) {
      // fetch data from model
      m_itemsCached = new TreeSet<ItemWrapper>();
      m_countTimeless = 0;
      Collection items = m_dateChooser.getModel().getItemsAt(getRepresentedDate());
      if (items != null) {
        try {
          for (Iterator it = items.iterator(); it.hasNext();) {
            Object item = it.next();
            m_itemsCached.add(new ItemWrapper(item));
          }
        }
        catch (ConcurrentModificationException cme) {
          LOG.warn("ConcurrentModificationException on getItems");
          return m_itemsCached;
        }
      }
      if (m_itemsCached.size() > 0) {
        // calculate distribution of time-dependent items
        ArrayList<ItemBoundsOrganizer> orgs = new ArrayList<ItemBoundsOrganizer>();
        ArrayList<ItemBoundsOrganizer> conflictSet = new ArrayList<ItemBoundsOrganizer>();
        // build structure
        int horizontalSlotCount = 0;
        for (Iterator<ItemWrapper> it = m_itemsCached.iterator(); it.hasNext();) {
          ItemWrapper newItem = it.next();
          if (newItem.isTimed()) {
            ItemBoundsOrganizer newOrg = new ItemBoundsOrganizer(newItem);
            conflictSet.clear();
            int minLevel = 0;
            for (int i = 0, n = orgs.size(); i < n; i++) {
              ItemBoundsOrganizer existingOrg = orgs.get(i);
              if (existingOrg.getCC().intersects(newOrg.getCC())) {
                conflictSet.add(existingOrg);
                minLevel = Math.max(minLevel, existingOrg.getMinLevel() + 1);
              }
            }
            newOrg.setMinLevel(minLevel);
            horizontalSlotCount = Math.max(horizontalSlotCount, minLevel + 1);
            orgs.add(newOrg);
            for (Iterator cit = conflictSet.iterator(); cit.hasNext();) {
              ItemBoundsOrganizer org = (ItemBoundsOrganizer) cit.next();
              /**
               * @rn imo, 06.07.2007, Ticket 57375
               */
              org.limitMaxLevel(minLevel - 1);
            }
          }
        }
        // save (horizontal) bounds
        for (Iterator it = orgs.iterator(); it.hasNext();) {
          ItemBoundsOrganizer org = (ItemBoundsOrganizer) it.next();
          org.saveHorizontalExtents(horizontalSlotCount);
        }
        orgs = null;
        // calculate distribution of time-independent items
        for (Iterator<ItemWrapper> it = m_itemsCached.iterator(); it.hasNext();) {
          ItemWrapper cc = it.next();
          if (!cc.isTimed()) {
            cc.setHorizontalExtents(0f, 1f);
            m_countTimeless++;
          }
        }
      }// end if size>0
    }// end if cached
    return m_itemsCached;
  }

  @Override
  public void refresh() {
    repaint();
  }

  @Override
  public Dimension getMinimumSize() {
    return MIN_SIZE;
  }

  @Override
  public Dimension getPreferredSize() {
    return PREF_SIZE;
  }

  @Override
  public Dimension getMaximumSize() {
    return MAX_SIZE;
  }

  @Override
  public boolean isSelected() {
    return m_selected;
  }

  @Override
  public void setSelected(boolean b) {
    m_selected = b;
  }

  @Override
  public Date getRepresentedDate() {
    return m_repDate;
  }

  public boolean isFirstColumn() {
    return m_firstColumn;
  }

  @Override
  public void setRepresentedState(Calendar c, boolean isMajor, boolean firstColumn, int displayType) {
    Date oldRepDate = m_repDate;
    m_repDate = new Date(c.getTime().getTime());
    m_isMajor = isMajor;
    m_displayType = displayType;
    m_firstColumn = firstColumn;
    // clear list only if date changed
    if (oldRepDate == null || (!oldRepDate.equals(m_repDate))) {
      resetItemCache();
    }
    // labels for large size
    if (isFirstColumn() && (m_displayType != DateChooser.DISPLAY_MODE_DAY)) {
      m_weekLabel = SwingUtility.getNlsText("WeekShort") + " " + c.get(Calendar.WEEK_OF_YEAR);
    }
    else {
      m_weekLabel = "";
    }
    DateFormat fmt = DateFormat.getDateInstance(DateFormat.LONG);
    if (fmt instanceof SimpleDateFormat) {
      String pattern = ((SimpleDateFormat) fmt).toPattern();
      //remove year
      pattern = pattern.replaceAll("[/\\-,. ]*[y]+[/\\-,.]*", "").trim();
      //MMM instead of MMMMM
      pattern = pattern.replaceAll("[M]+", "MMM").trim();
      ((SimpleDateFormat) fmt).applyPattern(pattern);
    }
    m_dateLabel = fmt.format(m_repDate);
    m_dayLabel = "" + c.get(Calendar.DATE);
    // gui
    repaint();
  }

  @Override
  public Object getItemAt(Point p) {
    // return last item, as this one is the top item displayed
    Object foundItem = null;
    for (Iterator it = getItems().iterator(); it.hasNext();) {
      ItemWrapper cc = (ItemWrapper) it.next();
      if (cc.contains(p)) {
        foundItem = cc.getItem();
      }
    }
    return foundItem;
  }

  /**
   * @return the absolut time of day in millis regardless of daylight savings
   *         date.getTime() is not correct (+-1 hour) on days where daylight
   *         savings is applied
   */
  public static long getTimeOfDayMillis(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    long timeRel = ((c.get(Calendar.HOUR_OF_DAY) * 60L + c.get(Calendar.MINUTE)) * 60L + c.get(Calendar.SECOND)) * 1000L + c.get(Calendar.MILLISECOND);
    c.set(Calendar.HOUR, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    long dayAbs = c.getTimeInMillis();
    return dayAbs + timeRel;
  }

  @Override
  public String getToolTipText(MouseEvent e) {
    Object item = getItemAt(e.getPoint());
    if (item != null) {
      String text = m_dateChooser.getModel().getTooltip(item, getRepresentedDate());
      return SwingUtility.createHtmlLabelText(text, true);
    }
    else {
      return null;
    }
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    setBackground(m_yesMajorColor);
    if (!m_isMajor && m_dateChooser.getMarkOutOfMonthDays()) {
      setBackground(m_notMajorColor);
    }
    Insets insets = getInsets();
    Rectangle view;
    if (insets != null) {
      view = new Rectangle(insets.left, insets.top, getWidth() - insets.left - insets.right, getHeight() - insets.top - insets.bottom);
    }
    else {
      view = new Rectangle(0, 0, getWidth(), getHeight());
    }
    Graphics gSub = g.create(view.x, view.y, view.width, view.height);
    gSub.setClip(0, 0, view.width, view.height);
    ArrayList<Rectangle> reservedAreas = new ArrayList<Rectangle>();
    Rectangle subView = paintBefore(gSub, reservedAreas);
    Graphics gSubSub = gSub.create(subView.x, subView.y, subView.width, subView.height);
    // translate reserved areas
    ArrayList<Rectangle> subReservedAreas = new ArrayList<Rectangle>(reservedAreas.size());
    for (Rectangle r : reservedAreas) {
      r = new Rectangle(r);
      r.translate(-subView.x, -subView.y);
      subReservedAreas.add(r);
    }
    gSubSub.setClip(0, 0, subView.width, subView.height);
    int subSubX = view.x + subView.x;
    int subSubY = view.y + subView.y;
    if (m_displayType == DateChooser.DISPLAY_MODE_MONTH) {
      paintMediumList(gSubSub, subSubX, subSubY, subReservedAreas);
    }
    else if (m_displayType == DateChooser.DISPLAY_MODE_WEEK || m_displayType == DateChooser.DISPLAY_MODE_WORKWEEK) {
      paintLargeList(gSubSub, subSubX, subSubY, subReservedAreas);
    }
    else /* day */{
      paintLargeList(gSubSub, subSubX, subSubY, subReservedAreas);
    }
    gSub.setClip(0, 0, view.width, view.height);
    paintAfter(gSub);
  }

  protected Rectangle/* sub view */paintBefore(Graphics g, Collection<Rectangle> reservedAreas) {
    Rectangle subView = g.getClipBounds();
    if (getWidth() >= SWITCH_ITEM_WIDTH && getHeight() >= SWITCH_ITEM_HEIGHT) {
      int w = subView.width;
      FontMetrics fm = getFontMetrics(getFont());
      int ascent = fm.getAscent();
      //
      if (m_displayType != DateChooser.DISPLAY_MODE_DAY) {
        g.setColor(getForeground());
        // date label
        String s = m_dateLabel;
        int wStr = fm.stringWidth(s);
        int splitx = Math.max(1, w - wStr - 1);
        g.setClip(splitx, 0, w - splitx, fm.getHeight());
        reservedAreas.add(g.getClipBounds());
        g.drawString(s, splitx, ascent);
        if (isFirstColumn() && (m_displayType != DateChooser.DISPLAY_MODE_DAY)) {
          // week label
          s = m_weekLabel;
          if (s != null && s.length() > 0) {
            wStr = fm.stringWidth(s);
            if (wStr <= splitx - 5) {
              g.setClip(1, 0, wStr, fm.getHeight());
              reservedAreas.add(g.getClipBounds());
              g.drawString(s, 1, ascent);
            }
          }
        }
        int dy = fm.getHeight() + 1;
        subView.y += dy;
        subView.height -= dy;
      }// end if not day-mode
      if (m_displayType == DateChooser.DISPLAY_MODE_DAY || m_displayType == DateChooser.DISPLAY_MODE_WEEK || m_displayType == DateChooser.DISPLAY_MODE_WORKWEEK) {
        Rectangle timedView = new Rectangle(subView.x, subView.y + m_dateChooser.getTimelessSectionHeightHint(), subView.width, subView.height - m_dateChooser.getTimelessSectionHeightHint());
        // draw time raster
        g.setClip(subView);
        boolean drawLabel = isFirstColumn();
        // time interval
        Color lineCol = new Color(0xdddddd);
        Color darkLineCol = new Color(0xaaaaaa);
        Color textCol = new Color(0xaaaaaa);
        g.setColor(lineCol);
        int maxLabelWidth = 0;
        int step = Math.max(1, ((m_endHour - m_startHour) * fm.getHeight() + timedView.height - 1) / timedView.height);
        for (int hour = m_startHour; hour <= m_endHour; hour = hour + step) {
          int y = timedView.y + (hour - m_startHour) * (timedView.height) / (m_endHour + 1 - m_startHour);
          boolean majorHour = (hour == 12 || hour == 13);
          if (majorHour) {
            g.setColor(darkLineCol);
          }
          else {
            g.setColor(lineCol);
          }
          g.drawLine(0, y, w, y);
          if (drawLabel) {
            String s;
            if (hour == m_startHour && hour != 0) {
              s = SwingUtility.getNlsText("Calendar_earlier");
            }
            else if (hour == m_endHour && hour != 23) {
              s = SwingUtility.getNlsText("Calendar_later");
            }
            else if (hour == 0) {
              s = formatHour(0);
            }
            else if (hour < 10) {
              s = formatHour(hour);
            }
            else {
              s = formatHour(hour);
            }
            //
            g.setColor(textCol);
            g.drawString(s, 1, y + ascent);
            //
            maxLabelWidth = Math.max(maxLabelWidth, fm.stringWidth(s) + 4);
          }
        }
        if (drawLabel) {
          int dx = maxLabelWidth;
          // update used view
          subView.x += dx;
          subView.width -= dx;
        }
      }
    }
    else {
      //see in paintAfter
    }
    return subView;
  }

  protected void paintAfter(Graphics g) {
    if (getWidth() >= SWITCH_ITEM_WIDTH && getHeight() >= SWITCH_ITEM_HEIGHT) {
      //see in paintBefore
    }
    else {
      if (m_displayType != DateChooser.DISPLAY_MODE_DAY) {
        int w = g.getClipBounds().width;
        g.setColor(getForeground());
        FontMetrics fm = getFontMetrics(getFont());
        String s = m_dayLabel;
        int wStr = fm.stringWidth(s);
        g.setClip((w - wStr) / 2 - 1, 0, wStr, fm.getHeight());
        g.drawString(s, (w - wStr) / 2 - 1, fm.getAscent());
      }
    }
  }

  private String formatHour(int h) {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2000, 01, 01, h, 0, 0);
    String s = m_formatHHMM.format(cal.getTime());
    if (s.charAt(1) == ':') {
      s = "0" + s;
    }
    return s;
  }

  // one line per item or in compressed (minimalized) mode an array of colors
  protected void paintMediumList(Graphics g, int absX, int absY, Collection<Rectangle> reservedAreas) {
    int w = g.getClipBounds().width;
    int h = g.getClipBounds().height;
    int n = getItems().size();
    if (n <= 0) {
      return;
    }
    FontMetrics fm = getFontMetrics(getFont());
    int heightByFont = n * fm.getHeight();
    boolean showLabel = (heightByFont <= h && getWidth() >= SWITCH_ITEM_WIDTH);
    boolean showCompressed = (h / n < SWITCH_COMPRESSED_HEIGHT);
    boolean showGaps = (n > 1);
    int index = 0;
    Rectangle r = new Rectangle();
    int compressedCols = 1;
    int compressedRows = n;
    if (showCompressed) {
      compressedCols = Math.max(1, (int) Math.sqrt(n));
      compressedRows = Math.max(1, (n + compressedCols - 1) / compressedCols);
    }
    for (Iterator it = getItems().iterator(); it.hasNext();) {
      ItemWrapper cc = (ItemWrapper) it.next();
      if (showCompressed) {
        int col = index % compressedCols;
        int row = index / compressedCols;
        r.setBounds(col * w / compressedCols, row * h / compressedRows, (col + 1) * w / compressedCols - col * w / compressedCols, (row + 1) * h / compressedRows - row * h / compressedRows);
      }
      else if (showLabel) {
        r.setBounds(0, index * heightByFont / n, w, (index + 1) * heightByFont / n - index * heightByFont / n);
      }
      else {
        r.setBounds(0, index * h / n, w, (index + 1) * h / n - index * h / n);
      }
      // add top/bottom gap
      if (showGaps && r.height >= 4) {
        r.y += 1;
        r.height -= 2;
      }
      // clear reserved areas
      if (showLabel) {
        r = removeReservedAreas(r, reservedAreas);
      }
      // store dimensions in absolut coordinates
      cc.setBounds(absX + r.x, absY + r.y, r.width, r.height);
      // bg
      Color color = cc.getColor();
      if (m_dateChooser.getSelectedItem() == cc.getItem()) {
        color = color.darker();
      }
      g.setColor(color);
      g.setClip(r);
      g.fillRect(r.x, r.y, r.width, r.height);
      // label
      if (showLabel) {
        g.setColor(getForeground());
        String s = m_dateChooser.getModel().getLabel(cc.getItem(), getRepresentedDate());
        if (s != null) {
          g.drawString(s, r.x + 1, r.y + r.height - fm.getDescent());
        }
      }
      // next
      index++;
    }
  }

  private Rectangle removeReservedAreas(Rectangle r, Collection<Rectangle> reservedAreas) {
    for (Rectangle res : reservedAreas) {
      Rectangle i;
      i = r.intersection(res);
      if (!i.isEmpty()) {
        if (i.x <= r.x) {
          int dx = i.x + i.width - r.x;
          r.x = dx;
          r.width -= dx;
        }
        else {
          r.width = i.x - r.x;
        }
      }
      i = r.intersection(res);
      if (!i.isEmpty()) {
        if (i.y <= r.y) {
          int dy = i.y + i.height - r.y;
          r.y = dy;
          r.height -= dy;
        }
        else {
          r.height = i.y - r.y;
        }
      }
    }
    return r;
  }

  // items at time positions
  // between m_startHour and m_endHour
  protected void paintLargeList(Graphics g, int absX, int absY, Collection<Rectangle> reservedAreas) {
    Rectangle clipRect = g.getClipBounds();
    int w = clipRect.width;
    int hTimeless = Math.max(0, m_dateChooser.getTimelessSectionHeightHint() - 1);
    int yTimed = hTimeless + 1;
    int hTimed = clipRect.height - hTimeless;
    if (getItems().size() <= 0) {
      return;
    }
    int timelessIndex = 0;
    int timelessItemHeight = 0;
    if (m_countTimeless > 0) {
      timelessItemHeight = Math.max(5, hTimeless / m_countTimeless);
    }
    // paint
    FontMetrics fm = getFontMetrics(getFont());
    Rectangle r = new Rectangle();
    long intervalMillis = (m_endHour + 1 - m_startHour) * HOUR_MILLIS;
    for (Iterator it = getItems().iterator(); it.hasNext();) {
      ItemWrapper cc = (ItemWrapper) it.next();
      if (cc.isTimed()) {
        r.x = (int) (cc.getX0() * w);
        r.width = (int) (cc.getX1() * w) - r.x;
        r.y = yTimed + (int) (cc.getFromRelative() * hTimed / intervalMillis);
        r.height = yTimed + (int) (cc.getToRelative() * hTimed / intervalMillis) - r.y;
        if (r.height <= 4) {
          r.height = 4;// minimum height
        }
      }
      else {// timeless
        r.x = 0;
        r.width = w;
        r.y = 0 + timelessIndex * timelessItemHeight;
        r.height = r.y < hTimeless ? timelessItemHeight : 0;
        timelessIndex++;
      }
      // end if timed
      // insets and top/bottom gap
      r.x = r.x + 1;
      r.width = Math.max(1, r.width - 2);
      r.y += 1;
      r.height = Math.max(1, r.height - 2);
      // store dimensions in ABSOLUT (+x0,+y0) coordinates
      cc.setBounds(r.x + absX, r.y + absY, r.width, r.height);
      // bg
      Color color = cc.getColor();
      if (m_dateChooser.getSelectedItem() == cc.getItem()) {
        color = color.darker();
      }
      g.setColor(color);
      g.setClip(r);
      g.fillRect(r.x, r.y, r.width, r.height);
      // label
      cc.setLabeled(true);/*
                           * r.height>=fm.getAscent() &&
                           * r.width>=SWITCH_ITEM_WIDTH
                           */
      if (cc.isLabeled()) {
        g.setColor(getForeground());
        String s = m_dateChooser.getModel().getLabel(cc.getItem(), getRepresentedDate());
        int centery = Math.max(0, (r.height - fm.getAscent()) / 2);
        g.setClip(r.x, r.y + centery, r.width, fm.getHeight());
        if (s != null) {
          g.drawString(s, r.x + 3, r.y + centery + fm.getAscent());
        }
      }
    }// end for items
  }

  private class ItemWrapper implements Comparable<ItemWrapper> {
    private Object m_item;
    private boolean m_labeled;
    private long m_fromRelative, m_toRelative;// truncated to actual DAY
    private Rectangle m_bounds;
    private boolean m_fullDay;
    private Date m_dragSourceDate;
    private float m_x0, m_x1;

    public ItemWrapper(Object item) {
      m_item = item;
      m_bounds = new Rectangle(1, 2, 3, 4);
      long repTimeOfDayStart = getTimeOfDayMillis(getRepresentedDate());
      long displayInterval = HOUR_MILLIS * (m_endHour + 1 - m_startHour);
      CalendarModel model = m_dateChooser.getModel();
      //
      m_fullDay = model.isFullDay(item);
      //
      m_fromRelative = getTimeOfDayMillis(model.getFromDate(item)) - repTimeOfDayStart - HOUR_MILLIS * m_startHour;
      if (m_fromRelative < 0) {
        m_fromRelative = 0;
      }
      if (m_fromRelative > displayInterval) {
        m_fromRelative = displayInterval;
      }
      //
      Date d2 = model.getToDate(item);
      if (d2 == null) {
        m_toRelative = m_fromRelative;
      }
      else {
        m_toRelative = getTimeOfDayMillis(d2) - repTimeOfDayStart - HOUR_MILLIS * m_startHour;
      }
      if (m_toRelative < 0) {
        m_toRelative = 0;
      }
      if (m_toRelative > displayInterval) {
        m_toRelative = displayInterval;
      }
      // check end of day set
      if (m_fromRelative >= displayInterval - HOUR_MILLIS && m_toRelative >= displayInterval - m_endHour * HOUR_MILLIS) {
        m_fromRelative = displayInterval - HOUR_MILLIS;
        m_toRelative = displayInterval;
      }
      // check emty set
      if (m_toRelative == m_fromRelative) {
        m_toRelative = m_fromRelative + HOUR_MILLIS;
      }
    }

    public void setHorizontalExtents(float x0, float x1) {
      m_x0 = x0;
      m_x1 = x1;
    }

    public float getX0() {
      return m_x0;
    }

    public float getX1() {
      return m_x1;
    }

    public Object getItem() {
      return m_item;
    }

    public void setBounds(int x, int y, int w, int h) {
      m_bounds.setBounds(x, y, w, h);
    }

    public boolean contains(Point p) {
      return m_bounds.contains(p);
    }

    public void setLabeled(boolean on) {
      m_labeled = on;
    }

    public void setDragSourceDate(Date date) {
      m_dragSourceDate = date;
    }

    public boolean isLabeled() {
      return m_labeled;
    }

    public boolean isTimed() {
      return !m_fullDay;
    }

    @Override
    public String toString() {
      return "Wrapper[" + m_item + (m_item != null ? "@" + Integer.toHexString(m_item.hashCode()) : "") + "]";
    }

    public boolean intersects(ItemWrapper other) {
      if (this.m_fromRelative <= other.m_fromRelative && other.m_fromRelative < this.m_toRelative) {
        return true;
      }
      else if (this.m_fromRelative < other.m_toRelative && other.m_toRelative <= this.m_toRelative) {
        return true;
      }
      return false;
    }

    public long getFromRelative() {
      return m_fromRelative;
    }

    public long getToRelative() {
      return m_toRelative;
    }

    public Date getDragSourceDate() {
      return m_dragSourceDate;
    }

    // wrappers
    public Color getColor() {
      Color c = m_dateChooser.getModel().getColor(m_item);
      if (c == null) {
        c = getBackground();
      }
      return c;
    }

    private Date getCompareDate() {
      return m_dateChooser.getModel().getFromDate(m_item);
    }

    private Integer getCompareId() {
      return new Integer(m_item.hashCode());
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(ItemWrapper o) {
      if (m_item instanceof Comparable) {
        return ((Comparable) m_item).compareTo(o.getItem());
      }
      else {
        int i = this.getCompareDate().compareTo(o.getCompareDate());
        if (i == 0) {
          i = this.getCompareId().compareTo(o.getCompareId());
        }
        return i;
      }
    }

    @Override
    public boolean equals(Object o) {
      return m_item == o;
    }

    @Override
    public int hashCode() {
      return m_item.hashCode();
    }

  }// end class

  private class ItemBoundsOrganizer {
    private ItemWrapper m_cc;
    private int m_minLevel;
    private int m_maxLevel = 100000;

    public ItemBoundsOrganizer(ItemWrapper cc) {
      m_cc = cc;
    }

    public ItemWrapper getCC() {
      return m_cc;
    }

    public int getMinLevel() {
      return m_minLevel;
    }

    public void setMinLevel(int i) {
      m_minLevel = i;
    }

    public void limitMaxLevel(int maximumValue) {
      m_maxLevel = Math.min(m_maxLevel, maximumValue);
    }

    private void saveHorizontalExtents(int horizontalSlotCount) {
      float x0, x1;// 0..1
      x0 = 1f * m_minLevel / horizontalSlotCount;
      x1 = 1f * Math.min(m_maxLevel + 1, horizontalSlotCount) / horizontalSlotCount;
      m_cc.setHorizontalExtents(x0, x1);
    }

    @Override
    public String toString() {
      return "BoundsOrganizer[" + m_cc + "]";
    }
  }// end class

  private class CellDropTargetAdapter extends DropTargetAdapter {

    @Override
    public void drop(DropTargetDropEvent e) {
      ItemWrapper itemWrapper;
      Transferable tr = e.getTransferable();
      for (DataFlavor flavor : tr.getTransferDataFlavors()) {
        try {
          if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType) && tr.getTransferData(flavor) instanceof ItemWrapper) {
            itemWrapper = (ItemWrapper) tr.getTransferData(flavor);
            m_dateChooser.getModel().moveItem(itemWrapper.getItem(), getRepresentedDate());
            break;
          }
        }
        catch (Exception x) {
          x.printStackTrace();
        }
      }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
      m_dateChooser.setSelectedDate(getRepresentedDate());
    }
  }

  private class ItemDragGestureListener implements DragGestureListener {

    @Override
    public void dragGestureRecognized(DragGestureEvent e) {
      Object item = getItemAt(e.getDragOrigin());
      if (item == null) {
        return;
      }
      if (m_dateChooser.getModel().isDraggable(item)) {
        ItemWrapper cic = new ItemWrapper(item);
        cic.setDragSourceDate(getRepresentedDate());
        CalendarItemTransferable cit = new CalendarItemTransferable(cic);
        e.startDrag(null, cit);
      }
    }
  }
}
