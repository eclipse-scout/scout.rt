package org.eclipse.scout.rt.ui.svg.calendar.builder;

import java.awt.Point;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.dom.svg.SVGTextContentSupport;
import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.svg.calendar.Activator;
import org.eclipse.scout.rt.ui.svg.calendar.CalendarSvgUtility;
import org.eclipse.scout.rt.ui.svg.calendar.builder.listener.CalendarDocumentEvent;
import org.eclipse.scout.rt.ui.svg.calendar.builder.listener.ICalendarDocumentListener;
import org.eclipse.scout.rt.ui.svg.calendar.comp.IComponentElementFactory;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGStylable;

public abstract class AbstractCalendarDocumentBuilder {
  private final static Pattern REGEX_URL_GRID_CLICK = Pattern.compile(AbstractCalendarDocumentBuilder.LINK_GRID_PREFIX + "([0-9]{1})([0-9]{1})");
  private final static Pattern REGEX_URL_COMP_CLICK = Pattern.compile(AbstractCalendarDocumentBuilder.LINK_COMPONENT_PREFIX + "([0-9]*)/([0-9]{1})([0-9]{1})");

  public final static float ORIG_CALENDAR_WIDTH = 557.0f; // width of the calendar on 100% scale (as defined in the svg)
  public final static float ORIG_CALENDAR_HEIGHT = 464.667f; // height of the calendar on 100% scale (as defined in the svg)

  private final static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCalendarDocumentBuilder.class);

  private final static float MIN_FONT_SIZE = 8; // min font size (calendar will scale the text based on the UI size until down to this min value)
  private final static float ORIG_FONT_SIZE = 12; // font size at 100%
  private final static float MAX_FONT_SIZE = 23; // max font size (calendar will scale the text based on the UI size until up to this max value)
  private final static int NUM_TIME_LINE_ROWS = 15; // how many timeline rows exist (7:00-18:00, earlier, later, full day row)

  protected final static int NUM_DAYS_IN_WEEK = 7;

  private final static String LINK_NEXT_SMALL = "http://local/arrow/nextSmall";
  private final static String LINK_NEXT_BIG = "http://local/arrow/nextBig";
  private final static String LINK_PREV_SMALL = "http://local/arrow/prevSmall";
  private final static String LINK_PREV_BIG = "http://local/arrow/prevBig";
  private final static String LINK_CONTEXT_MENU = "http://local/menu";

  private final static String FONT = "Arial";

  private final static String PROP_GRID_DATE = "Date";

  private final static String LINK_COMPONENT_PREFIX = "http://local/comp/";
  private final static String LINK_DISPLAY_MODE_PREFIX = "http://local/displayMode/";
  private final static String LINK_GRID_PREFIX = "http://local/grid/";

  private final static String COLOR_LINKS = CalendarSvgUtility.COLOR_PREFIX + "67a8ce";
  private final static String COLOR_BLACK = CalendarSvgUtility.COLOR_PREFIX + "000000";
  private final static String COLOR_WHITE = CalendarSvgUtility.COLOR_PREFIX + "ffffff";
  private final static String COLOR_FOREIGN_MONTH_BACKGROUND = CalendarSvgUtility.COLOR_PREFIX + "eeeeee";
  private final static String COLOR_TIME_LINE = CalendarSvgUtility.COLOR_PREFIX + "cccccc";
  private final static String COLOR_MONTH_BACKGROUND = COLOR_WHITE;
  private final static String COLOR_SELECTED_DAY_BORDER = COLOR_BLACK;
  private final static String COLOR_NOT_SELECTED_DAY_BORDER = CalendarSvgUtility.COLOR_PREFIX + "c0c0c0";

  private final SVGDocument m_doc;

  private final EventListenerList m_listenerList;

  private final String[] m_monthsLabels;
  private final String[] m_weekDayLabels;
  private final String[] m_weekDayLongLabels;
  private final String[] m_displayModeLabels;
  private final int m_firstDayOfWeek;

  private final float[] m_displayModeTextWidth;
  private final Element[] m_elDisplayMode;
  private final Element m_elTitle;
  private final Element m_elComponentsContainer;
  private final Element m_elMoveNextBig;
  private final Element m_elMoveNextSmall;
  private final Element m_elMovePrevBig;
  private final Element m_elMovePrevSmall;
  private final Element m_elLinkMenuContainer;
  private final Element[][] m_elGridBox;
  private final Element[][] m_elGridText;
  private final Element[][] m_elTimeLineGrid;
  private final Element[] m_elTimeLineTexts;
  private final Element[] m_elWeekDayHeadings;
  private final Element m_elMenuContainer;

  private Element m_selectedElement;
  private Date m_selectedDate;
  private Date m_shownDate;
  private Date m_startDate;
  private Date m_endDate;
  private CalendarComponent m_selectedComponent;
  private CalendarComponent[] m_components;
  private int m_numContextMenus;

  protected AbstractCalendarDocumentBuilder(String svgFile) {
    // read document
    InputStream is = null;
    try {
      is = Activator.getDefault().getBundle().getResource(svgFile).openStream();
      m_doc = SVGUtility.readSVGDocument(is, true);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Cannot find svg resource '" + svgFile + "'", e);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (Exception e) {
        }
      }
    }

    m_listenerList = new EventListenerList();

    // initialize locale dependent labels and options
    DateFormatSymbols dateSymbols = new DateFormatSymbols(LocaleThreadLocal.get());
    m_monthsLabels = dateSymbols.getMonths();
    m_weekDayLabels = dateSymbols.getShortWeekdays();
    m_weekDayLongLabels = dateSymbols.getWeekdays();
    m_displayModeLabels = new String[]{ScoutTexts.get("Day"), ScoutTexts.get("WorkWeek"), ScoutTexts.get("Week"), ScoutTexts.get("Month")};
    m_firstDayOfWeek = createCalendar().getFirstDayOfWeek();

    // get named elements
    m_elComponentsContainer = m_doc.getElementById("Components");
    m_elTitle = m_doc.getElementById("Title");
    m_elLinkMenuContainer = m_doc.getElementById("LinkMenuLayer");
    m_elMoveNextBig = m_doc.getElementById("nextYear");
    m_elMoveNextSmall = m_doc.getElementById("nextMonth");
    m_elMovePrevBig = m_doc.getElementById("prevYear");
    m_elMovePrevSmall = m_doc.getElementById("prevMonth");
    m_elGridBox = getGridElements("b", getNumWeekdays(), getNumWeeks());
    m_elGridText = getGridElements("t", getNumWeekdays(), getNumWeeks());
    m_elWeekDayHeadings = new Element[]{m_doc.getElementById("Mo"),
        m_doc.getElementById("Tu"),
        m_doc.getElementById("We"),
        m_doc.getElementById("Th"),
        m_doc.getElementById("Fr"),
        m_doc.getElementById("Sa"),
        m_doc.getElementById("So")};
    m_elDisplayMode = new Element[]{m_doc.getElementById("displayModeDay"),
        m_doc.getElementById("displayModeWorkWeek"),
        m_doc.getElementById("displayModeWeek"),
        m_doc.getElementById("displayModeMonth")};
    m_elMenuContainer = m_doc.getElementById("MenuLayer");
    m_elTimeLineGrid = getGridElements("tlg", getNumWeekdays(), NUM_TIME_LINE_ROWS);
    m_elTimeLineTexts = new Element[NUM_TIME_LINE_ROWS];
    for (int i = 0; i < m_elTimeLineTexts.length; i++) {
      m_elTimeLineTexts[i] = m_doc.getElementById("tlt" + i);
    }
    m_displayModeTextWidth = new float[m_elDisplayMode.length];

    // set fonts
    CalendarSvgUtility.setFont(m_elTitle, FONT);
    visitGrid(m_elGridText, new IGridVisitor() {
      @Override
      public void visit(Element element, int weekday, int week) {
        if (element != null) {
          CalendarSvgUtility.setFont(element, FONT);
        }
      }
    });
    if (hasTimeLine()) {
      for (int i = 0; i < m_elTimeLineTexts.length; i++) {
        if (m_elTimeLineTexts[i] != null) {
          CalendarSvgUtility.setFont(m_elTimeLineTexts[i], FONT);
        }
      }
    }
    CalendarSvgUtility.setFontWeightBold(m_elTitle);
    CalendarSvgUtility.setTextAlignCenter(m_elTitle);

    // init elements
    SVGUtility.addHyperlink(m_elMoveNextBig, LINK_NEXT_BIG);
    SVGUtility.addHyperlink(m_elMovePrevSmall, LINK_PREV_SMALL);
    SVGUtility.addHyperlink(m_elMoveNextSmall, LINK_NEXT_SMALL);
    SVGUtility.addHyperlink(m_elMovePrevBig, LINK_PREV_BIG);
    initWeekdayHeadingNames();
    initDisplayModeLinks();
    initTimeLineText();
    initGridHyperlink();
  }

  protected abstract int getNumWeekdays();

  protected abstract int getNumWeeks();

  protected abstract int getSmallNextField();

  protected abstract int getBigNextField();

  protected abstract void truncateToRange(Calendar cal);

  protected abstract String getRangeTitle(Calendar cal);

  protected abstract String getDayTitle(Calendar cal);

  protected abstract boolean hasTimeLine();

  protected abstract int getDisplayMode();

  protected abstract IComponentElementFactory getComponentElementFactory();

  public void hyperlinkActivated(String hyperlinkUrl) {
    Date currentDate = getShownDate();
    if (currentDate == null) {
      return;
    }

    Calendar cal = createCalendar(currentDate);

    if (LINK_NEXT_SMALL.equals(hyperlinkUrl)) {
      cal.add(getSmallNextField(), 1);
      setShownDate(cal.getTime());
    }
    else if (LINK_NEXT_BIG.equals(hyperlinkUrl)) {
      cal.add(getBigNextField(), 1);
      setShownDate(cal.getTime());
    }
    else if (LINK_PREV_SMALL.equals(hyperlinkUrl)) {
      cal.add(getSmallNextField(), -1);
      setShownDate(cal.getTime());
    }
    else if (LINK_PREV_BIG.equals(hyperlinkUrl)) {
      cal.add(getBigNextField(), -1);
      setShownDate(cal.getTime());
    }
    else if (LINK_CONTEXT_MENU.equals(hyperlinkUrl)) {
      firePopupMenuActivatedEvent();
    }
    else if (hyperlinkUrl.startsWith(LINK_GRID_PREFIX)) {
      Matcher m = REGEX_URL_GRID_CLICK.matcher(hyperlinkUrl);
      if (m.matches()) {
        int weekday = Integer.parseInt(m.group(1));
        int week = Integer.parseInt(m.group(2));
        Date clickedDate = getDateAt(weekday, week);
        setSelection(clickedDate);
      }
    }
    else if (hyperlinkUrl.startsWith(LINK_COMPONENT_PREFIX)) {
      Matcher m = REGEX_URL_COMP_CLICK.matcher(hyperlinkUrl);
      if (m.matches()) {
        long id = Long.parseLong(m.group(1));
        int weekday = Integer.parseInt(m.group(2));
        int week = Integer.parseInt(m.group(3));

        Date clickedDate = getDateAt(weekday, week);
        CalendarComponent selected = getComponentWithId(id);
        setSelection(clickedDate, selected);
      }
    }
    else if (hyperlinkUrl.startsWith(LINK_DISPLAY_MODE_PREFIX)) {
      int displayMode = Integer.parseInt(hyperlinkUrl.substring(LINK_DISPLAY_MODE_PREFIX.length()));
      fireDisplayModeLinkActivatedEvent(displayMode);
    }
  }

  public static Calendar createCalendar(Date initDate) {
    Calendar cal = createCalendar();
    cal.setTime(initDate);
    return cal;
  }

  public static Calendar createCalendar() {
    return new GregorianCalendar(LocaleThreadLocal.get());
  }

  public static AbstractCalendarDocumentBuilder createInstance(int displayMode) {
    switch (displayMode) {
      case ICalendar.DISPLAY_MODE_DAY: {
        return new CalendarDayBuilder();
      }
      case ICalendar.DISPLAY_MODE_WORKWEEK: {
        return new CalendarWorkWeekBuilder();
      }
      case ICalendar.DISPLAY_MODE_WEEK: {
        return new CalendarWeekBuilder();
      }
      case ICalendar.DISPLAY_MODE_MONTH: {
        return new CalendarMonthBuilder();
      }
      default: {
        return null;
      }
    }
  }

  private void initDisplayModeLinks() {
    final int[] linkMenuIds = new int[]{ICalendar.DISPLAY_MODE_DAY, ICalendar.DISPLAY_MODE_WORKWEEK, ICalendar.DISPLAY_MODE_WEEK, ICalendar.DISPLAY_MODE_MONTH};
    final float MARGIN = 12.0f; // between the display mode links

    float xPos = 4.0f; // start position (aligned with left grid start of svg)
    for (int i = 0; i < m_elDisplayMode.length; i++) {
      boolean isCurrentDisplayMode = linkMenuIds[i] == getDisplayMode();
      Element e = m_elDisplayMode[i];
      e.setTextContent(m_displayModeLabels[i]);
      CalendarSvgUtility.setFontWeightNormal(e);
      CalendarSvgUtility.setFont(e, FONT);
      CalendarSvgUtility.setFontSize(e, ORIG_FONT_SIZE);
      CalendarSvgUtility.setFontColor(e, COLOR_LINKS, isCurrentDisplayMode);
      CalendarSvgUtility.setCalendarDisplayModeXPos(e, xPos);
      m_displayModeTextWidth[i] = xPos; // remember the original text position to apply scaling later on.

      SVGUtility.addHyperlink(e, LINK_DISPLAY_MODE_PREFIX + linkMenuIds[i]);

      xPos += SVGTextContentSupport.getComputedTextLength(e) + MARGIN;

      // set the font to bold after the size has been calculated
      if (isCurrentDisplayMode) {
        CalendarSvgUtility.setFontWeightBold(e);
      }
    }
  }

  private void initTimeLineText() {
    Element early = m_doc.getElementById("tlt0");
    if (early != null) {
      early.setTextContent(ScoutTexts.get("Calendar_earlier"));
    }
    Element late = m_doc.getElementById("tlt13");
    if (late != null) {
      late.setTextContent(ScoutTexts.get("Calendar_later"));
    }
  }

  private void initGridHyperlink() {
    if (hasTimeLine()) {
      visitGrid(m_elGridBox, new IGridVisitor() {
        @Override
        public void visit(Element element, int x, int y) {
          for (int i = 0; i < m_elTimeLineGrid.length; i++) {
            SVGUtility.addHyperlink(m_elTimeLineGrid[i][x], getGridClickUrl(x, y));
          }
        }
      });
    }
    else {
      visitGrid(m_elGridBox, new IGridVisitor() {
        @Override
        public void visit(Element element, int x, int y) {
          SVGUtility.addHyperlink(element, getGridClickUrl(x, y));
        }
      });
      visitGrid(m_elGridText, new IGridVisitor() {
        @Override
        public void visit(Element element, int x, int y) {
          SVGUtility.addHyperlink(element, getGridClickUrl(x, y));
        }
      });
    }
  }

  private void initWeekdayHeadingNames() {
    int weekstart = m_firstDayOfWeek - 1;
    int numDays = Math.min(m_elWeekDayHeadings.length, getNumWeekdays());
    for (int i = 0; i < numDays; i++) {
      String label = getWeekDayLabel(1 + ((i + weekstart) % (NUM_DAYS_IN_WEEK)));
      Element e = m_elWeekDayHeadings[i];
      if (e != null) {
        e.setTextContent(label);
        CalendarSvgUtility.setFontWeightBold(e);
        CalendarSvgUtility.setTextAlignCenter(e);
        CalendarSvgUtility.setFont(e, FONT);
      }
    }
  }

  public void setSize(int w, int h) {
    final float ratio = Math.max(Math.min(w / ORIG_CALENDAR_WIDTH, ORIG_FONT_SIZE / MIN_FONT_SIZE), ORIG_FONT_SIZE / MAX_FONT_SIZE);
    final float newFontSize = ORIG_FONT_SIZE / ratio;

    // title
    CalendarSvgUtility.setFontSize(m_elTitle, newFontSize);

    // calendar grid
    visitGrid(m_elGridText, new IGridVisitor() {
      @Override
      public void visit(Element element, int weekday, int week) {
        if (element != null) {
          CalendarSvgUtility.setFontSize(element, newFontSize);
        }
      }
    });

    // week day heading
    for (Element e : m_elWeekDayHeadings) {
      if (e != null) {
        CalendarSvgUtility.setFontSize(e, newFontSize);
      }
    }

    // all texts in the components container
    for (Element e : CalendarSvgUtility.getAllChildElements(m_elComponentsContainer, SVGConstants.SVG_TEXT_TAG)) {
      CalendarSvgUtility.setFontSize(e, newFontSize);
      CalendarSvgUtility.setFont(e, FONT);
    }

    // time line texts
    if (hasTimeLine()) {
      for (Element e : m_elTimeLineTexts) {
        if (e != null) {
          CalendarSvgUtility.setFontSize(e, newFontSize);
        }
      }
    }

    // display mode links (font size and position)
    for (int i = 0; i < m_elDisplayMode.length; i++) {
      CalendarSvgUtility.setFontSize(m_elDisplayMode[i], newFontSize);
      CalendarSvgUtility.setCalendarDisplayModeXPos(m_elDisplayMode[i], m_displayModeTextWidth[i] / ratio);
    }
  }

  private Element[][] getGridElements(final String idPrefix, int numX, int numY) {
    final Element[][] ret = new Element[numY][numX];
    visitGrid(ret, new IGridVisitor() {
      @Override
      public void visit(Element element, int weekday, int week) {
        ret[week][weekday] = m_doc.getElementById(idPrefix + weekday + "" + week);
      }
    });
    return ret;
  }

  private void visitGrid(Element[][] grid, IGridVisitor visitor) {
    for (int row = 0; row < grid.length; row++) {
      for (int col = 0; col < grid[row].length; col++) {
        visitor.visit(grid[row][col], col, row);
      }
    }
  }

  public void addCalendarDocumentListener(ICalendarDocumentListener listener) {
    m_listenerList.add(ICalendarDocumentListener.class, listener);
  }

  public void removeCalendarDocumentListener(ICalendarDocumentListener listener) {
    m_listenerList.remove(ICalendarDocumentListener.class, listener);
  }

  private void firePopupMenuActivatedEvent() {
    CalendarDocumentEvent event = new CalendarDocumentEvent(CalendarDocumentEvent.TYPE_POPUP_MENU_ACTIVATED);
    fireCalendarDocumentEvent(event);
  }

  private void fireDisplayModeLinkActivatedEvent(int mode) {
    CalendarDocumentEvent event = new CalendarDocumentEvent(CalendarDocumentEvent.TYPE_DISPLAY_MODE_MENU_ACTIVATED);
    event.displayMode = mode;
    fireCalendarDocumentEvent(event);
  }

  private void fireSelectionChangedEvent(Date selectedDate, CalendarComponent selectedComponent) {
    CalendarDocumentEvent event = new CalendarDocumentEvent(CalendarDocumentEvent.TYPE_SELECTION_CHANGED);
    event.selectedDate = selectedDate;
    event.selectedComponent = selectedComponent;
    fireCalendarDocumentEvent(event);
  }

  private void fireVisibleRangeChangedEvent(Date start, Date end) {
    CalendarDocumentEvent event = new CalendarDocumentEvent(CalendarDocumentEvent.TYPE_VISIBLE_RANGE_CHANGED);
    event.startDate = start;
    event.endDate = end;
    fireCalendarDocumentEvent(event);
  }

  private void fireCalendarDocumentEvent(CalendarDocumentEvent event) {
    for (ICalendarDocumentListener l : m_listenerList.getListeners(ICalendarDocumentListener.class)) {
      try {
        switch (event.type) {
          case CalendarDocumentEvent.TYPE_POPUP_MENU_ACTIVATED: {
            l.popupMenuActivated();
            break;
          }
          case CalendarDocumentEvent.TYPE_SELECTION_CHANGED: {
            l.selectionChanged(event.selectedDate, event.selectedComponent);
            break;
          }
          case CalendarDocumentEvent.TYPE_VISIBLE_RANGE_CHANGED: {
            l.visibleRangeChanged(event.startDate, event.endDate);
            break;
          }
          case CalendarDocumentEvent.TYPE_DISPLAY_MODE_MENU_ACTIVATED: {
            l.displayModeMenuActivated(event.displayMode);
            break;
          }
          default: {
            throw new InvalidParameterException(event.type + " is no valid calendar document event type.");
          }
        }
      }
      catch (Exception ex) {
        LOG.error("Calendar document listener error", ex);
      }
    }
  }

  protected int getNumOfDaysInWeekBefore(Calendar cal) {
    int numDaysBefore = cal.get(Calendar.DAY_OF_WEEK) - m_firstDayOfWeek;
    if (numDaysBefore < 0) {
      numDaysBefore += NUM_DAYS_IN_WEEK;
    }
    return numDaysBefore;
  }

  protected String getMonthLabel(int month) {
    return m_monthsLabels[month];
  }

  protected String getWeekDayLabel(int weekday) {
    return m_weekDayLabels[weekday];
  }

  protected String getWeekDayLabelLong(int weekday) {
    return m_weekDayLongLabels[weekday];
  }

  public void setShownDate(final Date d) {
    m_shownDate = DateUtility.truncDate(d);

    // temporary calendar to do calculations
    final Calendar cal = createCalendar(getShownDate());
    final int month = cal.get(Calendar.MONTH);

    // calculate first date shown in the calendar
    truncateToRange(cal);
    m_startDate = cal.getTime();

    // calculate last date shown in the calendar
    Calendar end = createCalendar();
    end.setTime(m_startDate);
    end.add(Calendar.DAY_OF_MONTH, (m_elGridBox.length * m_elGridBox[0].length));
    m_endDate = new Date(end.getTimeInMillis() - 1);

    // write day of month and shade day cells
    visitGrid(m_elGridBox, new IGridVisitor() {
      @Override
      public void visit(Element gridElement, int wd, int week) {
        Date curDate = cal.getTime();

        // Day text
        Element textElement = m_elGridText[week][wd];
        if (textElement != null) {
          String dayTitle = getDayTitle(cal);
          if (dayTitle != null) {
            textElement.setTextContent(dayTitle);
          }
          CalendarSvgUtility.setTextAlignCenter(textElement);
        }

        // Background color
        String bgColor = null;
        if (month != cal.get(Calendar.MONTH)) {
          bgColor = COLOR_FOREIGN_MONTH_BACKGROUND;
        }
        else {
          bgColor = COLOR_MONTH_BACKGROUND;
        }
        if (hasTimeLine()) {
          for (int i = 0; i < m_elTimeLineGrid.length; i++) {
            CalendarSvgUtility.setBackgroundColor(m_elTimeLineGrid[i][wd], bgColor);
          }
        }
        else {
          CalendarSvgUtility.setBackgroundColor(gridElement, bgColor);
        }

        // tag data
        gridElement.setUserData(PROP_GRID_DATE, curDate, null);

        cal.add(Calendar.DAY_OF_MONTH, 1);
      }
    });

    // write month title
    m_elTitle.setTextContent(getRangeTitle(createCalendar(getShownDate())));

    // only highlight the selected box, if the date is in the currently shown range
    setSelectedDate(getSelectedDate());

    refreshComponents();

    fireVisibleRangeChangedEvent(getStartDate(), getEndDate());
  }

  private Point getPosition(Date d) {
    if (isInRange(d)) {
      long dif = (d.getTime() - m_startDate.getTime()) / (1000 * 60 * 60 * 24);
      int x = (int) dif % NUM_DAYS_IN_WEEK;
      int y = (int) dif / NUM_DAYS_IN_WEEK;
      return new Point(x, y);
    }
    else {
      return null;
    }
  }

  private Point getPosition(Element e) {
    return getPosition(getDateOfGridElement(e));
  }

  private Date getDateOfGridElement(Element e) {
    return (Date) e.getUserData(PROP_GRID_DATE);
  }

  private String getGridClickUrl(int weekday, int week) {
    return LINK_GRID_PREFIX + weekday + "" + week;
  }

  public void setSelection(Date d) {
    setSelection(d, null);
  }

  public void setSelection(Date d, CalendarComponent c) {
    setSelectedDate(d);
    setSelectedComponent(c);
    fireSelectionChangedEvent(d, c);
  }

  private Date getSelectedDate() {
    return m_selectedDate;
  }

  private void setSelectedDate(Date d) {
    m_selectedDate = d;
    Point p = getPosition(d);
    if (p != null) {
      setHighlightedBox(p.x, p.y);
    }
    else {
      clearHighlightedBox();
    }
  }

  private CalendarComponent getSelectedComponent() {
    return m_selectedComponent;
  }

  private void setSelectedComponent(CalendarComponent c) {
    CalendarComponent old = m_selectedComponent;
    m_selectedComponent = c;

    if (old != m_selectedComponent) {
      // trigger refresh of all components (colors of selected might have changed)
      refreshComponents();
    }
  }

  public CalendarComponent[] getComponents() {
    return m_components;
  }

  public void setComponents(CalendarComponent[] components) {
    m_components = components;

    HashMap<Date, HashSet<CalendarComponent>> map = new HashMap<Date, HashSet<CalendarComponent>>();
    if (m_components != null) {
      for (CalendarComponent c : m_components) {
        for (Date d : c.getCoveredDays()) {
          HashSet<CalendarComponent> l = map.get(d);
          if (l == null) {
            l = new HashSet<CalendarComponent>();
            map.put(d, l);
          }
          l.add(c);
        }
      }
    }
    refreshComponents(map);
  }

  private CalendarComponent getComponentWithId(long id) {
    for (CalendarComponent c : getComponents()) {
      if (c != null && c.getItem() != null && c.getItem().getId() == id) {
        return c;
      }
    }
    return null;
  }

  private void refreshComponents() {
    setComponents(m_components);
  }

  private void refreshComponents(HashMap<Date, HashSet<CalendarComponent>> map) {
    // remove all old components from the components layer
    CalendarSvgUtility.clearChildNodes(m_elComponentsContainer);

    for (Entry<Date, HashSet<CalendarComponent>> e : map.entrySet()) {
      Point p = getPosition(e.getKey());
      if (p != null && e.getValue() != null) {
        Element parent = m_elGridBox[p.y][p.x];
        CalendarComponent[] comps = e.getValue().toArray(new CalendarComponent[e.getValue().size()]);

        IComponentElementFactory fact = getComponentElementFactory();
        if (fact != null) {
          fact.setSelectedComponent(getSelectedComponent());
          Map<CalendarComponent, Element> compEls = fact.create(parent, getDateOfGridElement(parent), comps);
          if (compEls != null && compEls.size() > 0) {
            for (Entry<CalendarComponent, Element> el : compEls.entrySet()) {
              m_elComponentsContainer.appendChild(el.getValue());
              SVGUtility.addHyperlink(el.getValue(), LINK_COMPONENT_PREFIX + el.getKey().getItem().getId() + "/" + p.x + "" + p.y);
            }
          }
        }

      }
    }
  }

  private boolean isInRange(final Date d) {
    if (m_startDate == null || m_endDate == null || d == null) {
      return false;
    }
    return (d.after(m_startDate) && d.before(m_endDate)) || d.equals(m_startDate) || d.equals(m_endDate);
  }

  private void clearHighlightedBox() {
    if (m_selectedElement != null) {
      setBorder(m_selectedElement, false);
      m_selectedElement = null;
    }
  }

  private void setBorder(Element element, boolean selected) {
    SVGStylable css = (SVGStylable) element;
    CalendarSvgUtility.setBorderColor(element, selected ? COLOR_SELECTED_DAY_BORDER : COLOR_NOT_SELECTED_DAY_BORDER);
    css.getStyle().setProperty(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2", "");
  }

  private void setHighlightedBox(int weekday, int week) {
    if (m_selectedElement != null) {
      setBorder(m_selectedElement, false);
    }
    m_selectedElement = m_elGridBox[week][weekday];
    setBorder(m_selectedElement, true);
  }

  private Date getDateAt(int weekday, int week) {
    return (Date) m_elGridBox[week][weekday].getUserData(PROP_GRID_DATE);
  }

  public Date getEndDate() {
    return m_endDate;
  }

  public Date getStartDate() {
    return m_startDate;
  }

  public SVGDocument getSVGDocument() {
    return m_doc;
  }

  public void setNumContextMenus(int numContextMenus) {
    m_numContextMenus = numContextMenus;
    refreshContextMenu();
  }

  public int getNumContextMenus() {
    return m_numContextMenus;
  }

  private Date getShownDate() {
    return m_shownDate;
  }

  private void refreshContextMenu() {
    CalendarSvgUtility.clearChildNodes(m_elMenuContainer);
    if (getNumContextMenus() > 0) {
      // rectangle
      final float[] rectDimensions = new float[]{/*x=*/536.088f, /*y=*/447.602f,/*w=*/14.912f,/*h=*/14.914f};// dimensions of the context menu box (as defined in the MonthCalendar.svg file)
      Element rect = m_doc.createElementNS(SVGUtility.SVG_NS, SVGConstants.SVG_RECT_TAG);
      rect.setAttribute(SVGConstants.SVG_X_ATTRIBUTE, "" + rectDimensions[0]);
      rect.setAttribute(SVGConstants.SVG_Y_ATTRIBUTE, "" + rectDimensions[1]);
      rect.setAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE, "" + rectDimensions[2]);
      rect.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, "" + rectDimensions[3]);
      CalendarSvgUtility.setBorderColor(rect, COLOR_NOT_SELECTED_DAY_BORDER);
      CalendarSvgUtility.setBackgroundColor(rect, COLOR_WHITE);
      m_elMenuContainer.appendChild(rect);
      SVGUtility.addHyperlink(rect, LINK_CONTEXT_MENU);

      // triangle
      final String trianglePoints = "549.525,451.794 543.614,458.496 537.703,451.794";// positions of the 3 corners of the triangle (as defined in the MonthCalendar.svg file)
      Element triangle = m_doc.createElementNS(SVGUtility.SVG_NS, SVGConstants.SVG_POLYGON_TAG);
      triangle.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, trianglePoints);
      m_elMenuContainer.appendChild(triangle);
      SVGUtility.addHyperlink(triangle, LINK_CONTEXT_MENU);
    }
  }

  private static interface IGridVisitor {
    void visit(Element element, int x, int y);
  }
}
