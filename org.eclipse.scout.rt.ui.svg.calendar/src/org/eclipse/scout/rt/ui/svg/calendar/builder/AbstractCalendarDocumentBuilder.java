package org.eclipse.scout.rt.ui.svg.calendar.builder;

import java.awt.Point;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGTextContentSupport;
import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.DateTimeFormatFactory;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.svg.calendar.Activator;
import org.eclipse.scout.rt.ui.svg.calendar.CalendarSvgUtility;
import org.eclipse.scout.rt.ui.svg.calendar.builder.listener.CalendarDocumentEvent;
import org.eclipse.scout.rt.ui.svg.calendar.builder.listener.ICalendarDocumentListener;
import org.eclipse.scout.rt.ui.svg.calendar.comp.IComponentElementFactory;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGStylable;

public abstract class AbstractCalendarDocumentBuilder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCalendarDocumentBuilder.class);

  private static final Pattern REGEX_URL_GRID_CLICK = Pattern.compile(AbstractCalendarDocumentBuilder.LINK_GRID_PREFIX + "([0-9]{1})([0-9]{1})");
  private static final Pattern REGEX_URL_COMP_CLICK = Pattern.compile(AbstractCalendarDocumentBuilder.LINK_COMPONENT_PREFIX + "([0-9]*)/([0-9]{1})([0-9]{1})");

  public static final float ORIG_CALENDAR_WIDTH = 557.0f; // width of the calendar on 100% scale (as defined in the svg)
  public static final float ORIG_CALENDAR_HEIGHT = 464.667f; // height of the calendar on 100% scale (as defined in the svg)

  private static final float MARGIN = 12.0f; // between the display mode links
  private static final float MIN_FONT_SIZE = 8; // min font size (calendar will scale the text based on the UI size until down to this min value)
  private static final float ORIG_FONT_SIZE = 12; // font size at 100%
  private static final float MAX_FONT_SIZE = 23; // max font size (calendar will scale the text based on the UI size until up to this max value)
  private static final int NUM_TIME_LINE_ROWS = 15; // how many timeline rows exist (7:00-18:00, earlier, later, full day row)

  protected static final int NUM_DAYS_IN_WEEK = 7;
  protected static final int NUM_MILLIS_PER_DAY = 1000 * 60 * 60 * 24;

  private static final String LINK_NEXT_SMALL = "http://local/arrow/nextSmall";
  private static final String LINK_NEXT_BIG = "http://local/arrow/nextBig";
  private static final String LINK_PREV_SMALL = "http://local/arrow/prevSmall";
  private static final String LINK_PREV_BIG = "http://local/arrow/prevBig";
  private static final String LINK_CONTEXT_MENU = "http://local/menu";

  private static final String FONT = "Arial";

  private static final String LINK_COMPONENT_PREFIX = "http://local/comp/";
  private static final String LINK_DISPLAY_MODE_PREFIX = "http://local/displayMode/";
  private static final String LINK_GRID_PREFIX = "http://local/grid/";

  private static final String COLOR_LINKS = CalendarSvgUtility.COLOR_PREFIX + "67a8ce";
  private static final String COLOR_BLACK = CalendarSvgUtility.COLOR_PREFIX + "000000";
  private static final String COLOR_WHITE = CalendarSvgUtility.COLOR_PREFIX + "ffffff";
  private static final String COLOR_FOREIGN_MONTH_BACKGROUND = CalendarSvgUtility.COLOR_PREFIX + "eeeeee";
  private static final String COLOR_TIME_LINE = CalendarSvgUtility.COLOR_PREFIX + "cccccc";
  private static final String COLOR_MONTH_BACKGROUND = COLOR_WHITE;
  private static final String COLOR_SELECTED_DAY_BORDER = COLOR_BLACK;
  private static final String COLOR_NOT_SELECTED_DAY_BORDER = CalendarSvgUtility.COLOR_PREFIX + "c0c0c0";

  private final BridgeContext m_bridgeContext;

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
  private Element[][] m_elTimeLineGrid;
  private final Element[] m_elTimeLineTexts;
  private final Element[] m_elWeekDayHeadings;
  private final Element m_elMenuContainer;

  private Element m_selectedElement;
  private Date m_selectedDate;
  private Date m_shownDate;
  private Date m_startDate;
  private Date m_endDate;
  private CalendarComponent m_selectedComponent;
  private Set<? extends CalendarComponent> m_components;
  private int m_numContextMenus;
  private final Map<Element, Date> m_gridDateMap;

  private int m_linkCounter;
  private final Map<Integer, Object> m_linkIdToItemIdMap;
  private final Map<Object, Integer> m_ItemIdToLinkIdMap;

  private int m_startHour = 6;
  private int m_endHour = 19;
  private boolean m_useOverflowCells = true;
  private boolean m_showDisplayModeSelectionPanel = true;
  private boolean m_markNoonHour = true;
  private boolean m_markOutOfMonthDays = true;
  private DateFormat m_formatHHMM;

  protected AbstractCalendarDocumentBuilder(String svgFile) {
    // read document
    InputStream is = null;
    try {
      is = Activator.getDefault().getBundle().getResource(svgFile).openStream();
      m_bridgeContext = SVGUtility.readSVGDocumentForGraphicalModification(is);
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
    m_formatHHMM = new DateTimeFormatFactory().getHourMinute();

    // initialize locale dependent labels and options
    DateFormatSymbols dateSymbols = new DateFormatSymbols(LocaleThreadLocal.get());
    m_monthsLabels = dateSymbols.getMonths();
    m_weekDayLabels = dateSymbols.getShortWeekdays();
    m_weekDayLongLabels = dateSymbols.getWeekdays();
    m_displayModeLabels = new String[]{ScoutTexts.get("Day"), ScoutTexts.get("WorkWeek"), ScoutTexts.get("Week"), ScoutTexts.get("Month")};
    m_firstDayOfWeek = createCalendar().getFirstDayOfWeek();

    SVGDocument doc = getSVGDocument();
    // get named elements
    m_elComponentsContainer = doc.getElementById("Components");
    m_elTitle = doc.getElementById("Title");
    m_elLinkMenuContainer = doc.getElementById("LinkMenuLayer");
    m_elMoveNextBig = doc.getElementById("nextYear");
    m_elMoveNextSmall = doc.getElementById("nextMonth");
    m_elMovePrevBig = doc.getElementById("prevYear");
    m_elMovePrevSmall = doc.getElementById("prevMonth");
    m_elGridBox = getGridElements("b", getNumWeekdays(), getNumWeeks());
    m_elGridText = getGridElements("t", getNumWeekdays(), getNumWeeks());
    m_elWeekDayHeadings = new Element[]{doc.getElementById("Mo"),
        doc.getElementById("Tu"),
        doc.getElementById("We"),
        doc.getElementById("Th"),
        doc.getElementById("Fr"),
        doc.getElementById("Sa"),
        doc.getElementById("So")};
    m_elDisplayMode = new Element[]{doc.getElementById("displayModeDay"),
        doc.getElementById("displayModeWorkWeek"),
        doc.getElementById("displayModeWeek"),
        doc.getElementById("displayModeMonth")};
    m_elMenuContainer = doc.getElementById("MenuLayer");
    m_elTimeLineGrid = getGridElements("tlg", getNumWeekdays(), NUM_TIME_LINE_ROWS);
    m_elTimeLineTexts = new Element[NUM_TIME_LINE_ROWS];
    for (int i = 0; i < m_elTimeLineTexts.length; i++) {
      m_elTimeLineTexts[i] = doc.getElementById("tlt" + i);
    }
    m_displayModeTextWidth = new float[m_elDisplayMode.length];
    m_gridDateMap = new HashMap<Element, Date>();

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

    // init link Id support
    m_linkCounter = 1;
    m_linkIdToItemIdMap = new HashMap<Integer, Object>();
    m_ItemIdToLinkIdMap = new HashMap<Object, Integer>();
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

  protected abstract void resizeDayBoxes(double gridHeight);

  protected abstract double getGridTop();

  protected void resetTimeLineGrid() {
    m_elTimeLineGrid = getGridElements("tlg", getNumWeekdays(), getEndHour() - getStartHour() + 2);
  }

  protected String formatHour(int h) {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(2000, 01, 01, h, 0, 0);
    String s = m_formatHHMM.format(cal.getTime());
    if (s.charAt(1) == ':') {
      s = "0" + s;
    }
    return s;
  }

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
        int linkId = Integer.parseInt(m.group(1));
        int weekday = Integer.parseInt(m.group(2));
        int week = Integer.parseInt(m.group(3));

        Date clickedDate = getDateAt(weekday, week);
        Object itemId = getItemId(linkId);
        CalendarComponent selected = getComponentWithId(itemId);
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

    float xPos = 4.0f; // start position (aligned with left grid start of svg)
    for (int i = 0; i < m_elDisplayMode.length; i++) {
      boolean isCurrentDisplayMode = linkMenuIds[i] == getDisplayMode();
      Element e = m_elDisplayMode[i];
      SVGUtility.setTextContent(e, m_displayModeLabels[i]);
      CalendarSvgUtility.setFontWeightNormal(e);
      CalendarSvgUtility.setFont(e, FONT);
      CalendarSvgUtility.setFontSize(e, ORIG_FONT_SIZE);
      CalendarSvgUtility.setFontColor(e, COLOR_LINKS, isCurrentDisplayMode);
      CalendarSvgUtility.setCalendarDisplayModeXPos(e, xPos);
      m_displayModeTextWidth[i] = xPos; // remember the original text position to apply scaling later on.

      if (getShowDisplayModeSelectionPanel() == true) {
        SVGUtility.addHyperlink(e, LINK_DISPLAY_MODE_PREFIX + linkMenuIds[i]);
        xPos += SVGTextContentSupport.getComputedTextLength(e) + MARGIN;
      }

      // set the font to bold after the size has been calculated
      if (isCurrentDisplayMode) {
        CalendarSvgUtility.setFontWeightBold(e);
      }
    }
  }

  private void initTimeLineText() {
    Element early = getSVGDocument().getElementById("tlt0");
    if (early != null) {
      SVGUtility.setTextContent(early, ScoutTexts.get("Calendar_earlier"));
    }
    Element late = getSVGDocument().getElementById("tlt13");
    if (late != null) {
      SVGUtility.setTextContent(late, ScoutTexts.get("Calendar_later"));
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
        SVGUtility.setTextContent(e, label);
        CalendarSvgUtility.setFontWeightBold(e);
        CalendarSvgUtility.setTextAlignCenter(e);
        CalendarSvgUtility.setFont(e, FONT);
      }
    }
  }

  public void reconfigureLayout() {
    if (getDisplayMode() == ICalendar.DISPLAY_MODE_MONTH) {
      return; // nothing to reconfigure
    }
    if (getShowDisplayModeSelectionPanel() == false) {
      Element el = getSVGDocument().getElementById("LinkMenuLayer");
      el.setAttribute("display", "none");
    }

    int nrOfHours = (getEndHour() - getStartHour()) + 1;
    double gridTop = getGridTop();
    double gridLeft = 73.998;
    double gridHeight = 441.075 - gridTop;
    if (getShowDisplayModeSelectionPanel() == false) {
      gridHeight = 474.597 - gridTop;
    }
    resizeDayBoxes(gridHeight);

    double gridWidth = 550.16 - gridLeft;
    double cellWidth = gridWidth / getNumWeekdays();
    double cellHeight = gridHeight / (nrOfHours + 1);
    SVGDocument svgDoc = getSVGDocument();

    // remove old timeLinetext elements
    Element el = svgDoc.getElementById("TimeLineText");
    if (el.hasChildNodes())
    {
      while (el.getChildNodes().getLength() >= 1)
      {
        el.removeChild(el.getFirstChild());
      }
    }

    Element newEl;
    double y;

    Element[] timeLineTexts = getElementTimeLineTexts();

    timeLineTexts = new Element[nrOfHours];
    String rowText;
    for (int i = 0; i < nrOfHours; i++) {
      y = gridTop + cellHeight + ((gridHeight / (nrOfHours + 1)) * i);
      newEl = svgDoc.createElementNS(SVGUtility.SVG_NS, "rect");
      newEl.setAttribute("x", "6.377");
      newEl.setAttribute("y", String.valueOf(y));
      newEl.setAttribute("width", "57.371");
      newEl.setAttribute("height", "20.414");
      newEl.setAttribute("fill", "none");
      el.appendChild(newEl);

      y = y + 12.8877;
      newEl = svgDoc.createElementNS(SVGUtility.SVG_NS, "text");
      newEl.setAttribute("transform", "matrix(1 0 0 1 6.3774 " + String.valueOf(y) + ")");
      newEl.setAttribute("style", "fill:#9D9D9C; font-family:Arial; font-size:11");

      rowText = formatHour(getStartHour() + i);
      if (getUseOverflowCells()) {
        if (i == 0) {
          rowText = ScoutTexts.get("Calendar_earlier");
        }
        else if ((getStartHour() + i) == getEndHour()) {
          rowText = ScoutTexts.get("Calendar_later");
        }
      }
      Text textNode = svgDoc.createTextNode(rowText);
      newEl.appendChild(textNode);

      el.appendChild(newEl);
      timeLineTexts[i] = newEl;
    }

    // remove old TimeLineGrid elements
    Element elTimeLineGrid = getSVGDocument().getElementById("TimeLineGrid");
    if (elTimeLineGrid.hasChildNodes())
    {
      while (elTimeLineGrid.getChildNodes().getLength() >= 1)
      {
        elTimeLineGrid.removeChild(elTimeLineGrid.getFirstChild());
      }
    }

    String cellStyle = "fill:none;stroke:#C0C0C0;stroke-width:0.5;stroke-miterlimit:10;";

    for (int d = 0; d < getNumWeekdays(); d++) {
      for (int h = 0; h < nrOfHours + 1; h++) {
        newEl = svgDoc.createElementNS(SVGUtility.SVG_NS, "rect");
        newEl.setAttribute("id", "tlg" + String.valueOf(d) + String.valueOf(h));
        newEl.setAttribute("x", String.valueOf(gridLeft + (cellWidth * d)));
        newEl.setAttribute("y", String.valueOf(gridTop + (cellHeight * h)));
        newEl.setAttribute("width", String.valueOf(cellWidth));
        newEl.setAttribute("height", String.valueOf(cellHeight));
        newEl.setAttribute("style", cellStyle);
        elTimeLineGrid.appendChild(newEl);
      }
    }
    resetTimeLineGrid();
    initGridHyperlink();
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
        ret[week][weekday] = getSVGDocument().getElementById(idPrefix + weekday + "" + week);
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
            SVGUtility.setTextContent(textElement, dayTitle);
          }
          CalendarSvgUtility.setTextAlignCenter(textElement);
        }

        // Background color
        String bgColor = null;
        bgColor = COLOR_MONTH_BACKGROUND;
        if (month != cal.get(Calendar.MONTH) && getMarkOutOfMonthDays()) {
          bgColor = COLOR_FOREIGN_MONTH_BACKGROUND;
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
        m_gridDateMap.put(gridElement, curDate);

        cal.add(Calendar.DAY_OF_MONTH, 1);
      }
    });

    // write month title
    SVGUtility.setTextContent(m_elTitle, getRangeTitle(createCalendar(getShownDate())));

    // only highlight the selected box, if the date is in the currently shown range
    setSelectedDate(getSelectedDate());

    refreshComponents();

    fireVisibleRangeChangedEvent(getStartDate(), getEndDate());
  }

  private Point getPosition(Date d) {
    if (isInRange(d)) {
      int dif = DateUtility.getDaysBetween(m_startDate, d);
      int x = (int) dif % NUM_DAYS_IN_WEEK;
      int y = (int) dif / NUM_DAYS_IN_WEEK;
      return new Point(x, y);
    }
    else {
      return null;
    }
  }

  private Date getDateOfGridElement(Element e) {
    return m_gridDateMap.get(e);
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

  public void setWorkHours(int startHour, int endHour, boolean useOverflowCells) {
    m_startHour = startHour;
    m_endHour = endHour;
    m_useOverflowCells = useOverflowCells;

    initTimeLineText();
  }

  public void setShowDisplayModeSelectionPanel(boolean showDisplayModeSelectionPanel) {
    m_showDisplayModeSelectionPanel = showDisplayModeSelectionPanel;
  }

  public void setMarkNoonHour(boolean markNoonHour) {
    m_markNoonHour = markNoonHour;
  }

  public void setMarkOutOfMonthDays(boolean markOutOfMonthDays) {
    m_markOutOfMonthDays = markOutOfMonthDays;
  }

  public Set<? extends CalendarComponent> getComponents() {
    return m_components;
  }

  public void setComponents(Set<? extends CalendarComponent> components) {
    m_components = components;

    Map<Date, Set<CalendarComponent>> map = new HashMap<Date, Set<CalendarComponent>>();
    if (m_components != null) {
      for (CalendarComponent c : m_components) {
        for (Date d : c.getCoveredDays()) {
          Set<CalendarComponent> l = map.get(d);
          if (l == null) {
            l = new TreeSet<CalendarComponent>();
            map.put(d, l);
          }
          l.add(c);
        }
      }
    }
    refreshComponents(map);
  }

  /**
   * @return Translates the given link ID into the effective
   *         {@link org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem#getItemId()}.
   */
  private Object getItemId(int linkId) {
    return m_linkIdToItemIdMap.get(linkId);
  }

  /**
   * @return Translates the given {@link org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem#getItemId()}
   *         into a numeric link ID.
   */
  private int getOrCreateLinkId(Object itemId) {
    if (itemId == null) {
      throw new IllegalArgumentException("itemId must not be null");
    }
    Integer linkId = m_ItemIdToLinkIdMap.get(itemId);
    if (linkId == null) {
      linkId = m_linkCounter++;
      m_linkIdToItemIdMap.put(linkId, itemId);
      m_ItemIdToLinkIdMap.put(itemId, linkId);
    }
    return linkId.intValue();
  }

  private CalendarComponent getComponentWithId(Object itemId) {
    if (itemId == null) {
      return null;
    }
    for (CalendarComponent c : getComponents()) {
      if (c != null && c.getItem() != null && CompareUtility.equals(c.getItem().getItemId(), itemId)) {
        return c;
      }
    }
    return null;
  }

  private void refreshComponents() {
    setComponents(m_components);
  }

  private void refreshComponents(Map<Date, Set<CalendarComponent>> map) {
    // remove all old components from the components layer
    CalendarSvgUtility.clearChildNodes(m_elComponentsContainer);

    IComponentElementFactory fact = getComponentElementFactory();
    if (fact == null) {
      return;
    }

    for (Entry<Date, Set<CalendarComponent>> e : map.entrySet()) {
      Point p = getPosition(e.getKey());
      if (p != null && e.getValue() != null) {
        Element parent = m_elGridBox[p.y][p.x];
        CalendarComponent[] comps = e.getValue().toArray(new CalendarComponent[e.getValue().size()]);

        fact.setSelectedComponent(getSelectedComponent());
        Map<CalendarComponent, Element> compEls = fact.create(parent, getDateOfGridElement(parent), comps);
        if (compEls != null && compEls.size() > 0) {
          for (Entry<CalendarComponent, Element> el : compEls.entrySet()) {
            m_elComponentsContainer.appendChild(el.getValue());
            int linkId = getOrCreateLinkId(el.getKey().getItem().getItemId());
            SVGUtility.addHyperlink(el.getValue(), LINK_COMPONENT_PREFIX + linkId + "/" + p.x + "" + p.y);
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
    Element e = m_elGridBox[week][weekday];
    return m_gridDateMap.get(e);
  }

  public Date getEndDate() {
    if (m_endDate == null) {
      return null;
    }
    return (Date) m_endDate.clone();
  }

  public Date getStartDate() {
    if (m_startDate == null) {
      return null;
    }
    return (Date) m_startDate.clone();
  }

  public SVGDocument getSVGDocument() {
    return (SVGDocument) m_bridgeContext.getDocument();
  }

  public void dispose() {
    m_bridgeContext.dispose();
  }

  public void setNumContextMenus(int numContextMenus) {
    m_numContextMenus = numContextMenus;
    refreshContextMenu();
  }

  public int getNumContextMenus() {
    return m_numContextMenus;
  }

  protected int getEndHour() {
    return m_endHour;
  }

  protected int getStartHour() {
    return m_startHour;
  }

  protected boolean getUseOverflowCells() {
    return m_useOverflowCells;
  }

  protected boolean getShowDisplayModeSelectionPanel() {
    return m_showDisplayModeSelectionPanel;
  }

  protected boolean getMarkNoonHour() {
    return m_markNoonHour;
  }

  protected boolean getMarkOutOfMonthDays() {
    return m_markOutOfMonthDays;
  }

  protected Element[] getElementTimeLineTexts() {
    return m_elTimeLineTexts;
  }

  protected Element[][] getElementTimeLineGrid() {
    return m_elTimeLineGrid;
  }

  private Date getShownDate() {
    return m_shownDate;
  }

  private void refreshContextMenu() {
    CalendarSvgUtility.clearChildNodes(m_elMenuContainer);
    if (getNumContextMenus() > 0) {
      // rectangle
      final float[] rectDimensions = new float[]{/*x=*/536.088f, /*y=*/447.602f,/*w=*/14.912f,/*h=*/14.914f};// dimensions of the context menu box (as defined in the MonthCalendar.svg file)
      Element rect = getSVGDocument().createElementNS(SVGUtility.SVG_NS, SVGConstants.SVG_RECT_TAG);
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
      Element triangle = getSVGDocument().createElementNS(SVGUtility.SVG_NS, SVGConstants.SVG_POLYGON_TAG);
      triangle.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, trianglePoints);
      m_elMenuContainer.appendChild(triangle);
      SVGUtility.addHyperlink(triangle, LINK_CONTEXT_MENU);
    }
  }

  private interface IGridVisitor {
    void visit(Element element, int x, int y);
  }
}
