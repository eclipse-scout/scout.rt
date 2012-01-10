/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.core.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rwt.RWT;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.dnd.FileListTransferObject;
import org.eclipse.scout.commons.dnd.ImageTransferObject;
import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;
import org.osgi.framework.Version;

public abstract class AbstractRwtUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractRwtUtility.class);

  public static final String VARIANT_PROPOSAL_FORM = "proposal-form";
  public static final String VARIANT_LISTBOX = "listbox";
  public static final String VARIANT_LISTBOX_DISABLED = "listboxDisabled";
  public static final String VARIANT_EMPTY = "empty";

  static final String BROWSER_INFO = "browser-Info";

  public static BrowserInfo getBrowserInfo() {
    BrowserInfo info = (BrowserInfo) RWT.getSessionStore().getAttribute(BROWSER_INFO);
    if (info == null) {
      HttpServletRequest request = RWT.getRequest();
      if (LOG.isInfoEnabled()) {
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
          String headerName = (String) headerNames.nextElement();
          String header = request.getHeader(headerName);
          headerName = headerName + (headerName.length() <= 11 ? "\t\t" : "\t");
          LOG.info(headerName + header);
        }
      }
      String userAgent = request.getHeader("User-Agent");

      info = createBrowserInfo(userAgent);
      info.setLocale(request.getLocale());

      if (userAgent.indexOf("Windows") != -1
          || userAgent.indexOf("Win32") != -1
          || userAgent.indexOf("Win64") != -1) {
        info.setSystem(BrowserInfo.System.WINDOWS);
      }
      else if (userAgent.indexOf("Macintosh") != -1
          || userAgent.indexOf("MacPPC") != -1
          || userAgent.indexOf("MacIntel") != -1) {//FIXME
        info.setSystem(BrowserInfo.System.OSX);
      }
      else if (userAgent.indexOf("X11") != -1
          || userAgent.indexOf("Linux") != -1
          || userAgent.indexOf("BSD") != -1) {//FIXME
        if (userAgent.indexOf("Android") != -1) {
          info.setSystem(BrowserInfo.System.ANDROID);
          info.setMobile(true);
        }
        else {
          info.setSystem(BrowserInfo.System.UNIX);
        }
      }
      else if (userAgent.indexOf("iPad") != -1) {
        info.setSystem(BrowserInfo.System.IOS);
        info.setTablet(true);
      }
      else if (userAgent.indexOf("iPhone") != -1
          || userAgent.indexOf("iPod") != -1) {
        info.setSystem(BrowserInfo.System.IOS);
        info.setMobile(true);
      }
      else {
        info.setSystem(BrowserInfo.System.UNKNOWN);
      }
      if (LOG.isInfoEnabled()) {
        LOG.info(info.toString());
      }
      RWT.getSessionStore().setAttribute(BROWSER_INFO, info);
    }
    return info;
  }

  private static BrowserInfo createBrowserInfo(String userAgent) {
    BrowserInfo info = null;
    Version v = null;

    //Opera
    String regex = "Opera[\\s\\/]([0-9\\.]*)";
    boolean isOpera = StringUtility.contains(userAgent, regex);
    if (isOpera) {
      v = getBrowserVersion(userAgent, regex);
      info = new BrowserInfo(BrowserInfo.Type.OPERA, v);
      info.setOpera(isOpera);
      return info;
    }

    //Konqueror
    regex = "KHTML\\/([0-9-\\.]*)";
    boolean isKonqueror = StringUtility.contains(userAgent, regex);
    if (isKonqueror) {
      v = getBrowserVersion(userAgent, regex);
      info = new BrowserInfo(BrowserInfo.Type.KONQUEROR, null);
      info.setWebkit(isKonqueror);
      return info;
    }

    //Webkit Browsers
    regex = "AppleWebKit\\/([^ ]+)";
    boolean isWebkit = userAgent.indexOf("AppleWebKit") != -1
                   && StringUtility.contains(userAgent, regex);
    if (isWebkit) {
      v = getBrowserVersion(userAgent, regex);
      if (userAgent.indexOf("Chrome") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.GOOGLE_CHROME, v);
      }
      else if (userAgent.indexOf("Safari") != -1) {
        if (userAgent.indexOf("Android") != -1) {
          info = new BrowserInfo(BrowserInfo.Type.GOOGLE_CHROME, v);
        }
        else {
          info = new BrowserInfo(BrowserInfo.Type.APPLE_SAFARI, v);
        }
      }
      else if (userAgent.indexOf("OmniWeb") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.OMNI_WEB, v);
      }
      else if (userAgent.indexOf("Shiira") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.SHIRA, v);
      }
      else if (userAgent.indexOf("NetNewsWire") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.BLACKPIXEL_NETNEWSWIRE, v);
      }
      else if (userAgent.indexOf("RealPlayer") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.REALNETWORKS_REALPLAYER, v);
      }
      else if (userAgent.indexOf("Mobile") != -1) {
        // iPad reports this in fullscreen mode
        info = new BrowserInfo(BrowserInfo.Type.APPLE_SAFARI, v);
      }
      else {
        info = new BrowserInfo(BrowserInfo.Type.UNKNOWN, null);
      }
      info.setWebkit(isWebkit);
      return info;
    }

    //Gecko Browsers (Firefox)
    regex = "rv\\:([^\\);]+)(\\)|;)";
    boolean isGecko = userAgent.indexOf("Gecko") != -1
        && StringUtility.contains(userAgent, regex);
    if (isGecko) {
      v = getBrowserVersion(userAgent, regex);
      if (userAgent.indexOf("Firefox") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.MOZILLA_FIREFOX, v);
      }
      else if (userAgent.indexOf("Camino") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.MOZILLA_CAMINO, v);
      }
      else if (userAgent.indexOf("Galeon") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.GNOME_GALOEN, v);
      }
      else {
        info = new BrowserInfo(BrowserInfo.Type.UNKNOWN, null);
      }
      info.setGecko(isGecko);
      return info;
    }

    //Internet Explorer
    regex = "MSIE\\s+([^\\);]+)(\\)|;)";
    boolean isMshtml = StringUtility.contains(userAgent, regex);
    if (isMshtml) {
      v = getBrowserVersion(userAgent, regex);
      if (userAgent.indexOf("MSIE") != -1) {
        info = new BrowserInfo(BrowserInfo.Type.IE, v);
      }
      else {
        info = new BrowserInfo(BrowserInfo.Type.UNKNOWN, null);
      }
      info.setMshtml(isMshtml);
      return info;
    }
    info = new BrowserInfo(BrowserInfo.Type.UNKNOWN, null);
    return info;
  }

  private static Version getBrowserVersion(String userAgent, String regex) {
    Version v = null;
    Matcher matcher = Pattern.compile(".*" + regex + ".*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL).matcher(userAgent);
    if (matcher.matches()) {
      String s = matcher.group(1);
      s = s.replaceAll("^[/\\s]*", "");

      int[] vArr = new int[]{0, 0, 0};
      //Searches for 3 groups containing numbers separated with a dot.
      //Group 3 is optional (MSIE only has a major and a minor version, no micro)
      Matcher m = Pattern.compile("([0-9]+)\\.([0-9]+)[\\.]?([0-9]*)").matcher(s);

//      // Fix Opera version to match wikipedia style
//      version = version.substring( 0, 3 ) + "." + version.substring ( 3);FIXME sle
      if (m.find()) {
        for (int i = 1; i <= 3; i++) {
          String versionPart = m.group(i);
          if (StringUtility.hasText(versionPart)) {
            vArr[i - 1] = Integer.valueOf(versionPart);
          }
        }
      }

      v = new Version(vArr[0], vArr[1], vArr[2]);
    }
    return v;
  }

  public static Object createUiTransferable(TransferObject scoutT) {
    if (scoutT == null) {
      return null;
    }
    if (scoutT instanceof FileListTransferObject) {
      return ((FileListTransferObject) scoutT).getFilenames();
    }
    else if (scoutT instanceof TextTransferObject) {
      return ((TextTransferObject) scoutT).getPlainText();
    }
    else if (scoutT instanceof JavaTransferObject) {
      return ((JavaTransferObject) scoutT).getLocalObject();
    }
    else if (scoutT instanceof ImageTransferObject) {
      Object image = ((ImageTransferObject) scoutT).getImage();
      if (image instanceof byte[]) {
        ByteArrayInputStream imageInput = new ByteArrayInputStream((byte[]) image);
        Image img = new Image(null, imageInput);
        if (img != null) {
          return img.getImageData();
        }
      }
      else if (image instanceof ImageData) {
        return image;
      }
    }
    return null;
  }

  public static TransferObject createScoutTransferable(DropTargetEvent event) {
    if (event == null || event.currentDataType == null) {
      return null;

    }
    Exception ex = null;
    if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
      String[] fileNames = (String[]) event.data;
      ArrayList<File> files = new ArrayList<File>();
      for (String fileName : fileNames) {
        try {
          files.add(new File(fileName));
        }
        catch (Exception e) {
          if (ex == null) {
            ex = e;
          }
        }
      }
      return new FileListTransferObject(files);
    }
    /**
     * workaround for jvm local object, use text transfer types with java object
     */
    else if (JVMLocalObjectTransfer.getInstance().isSupportedType(event.currentDataType) && !(event.data instanceof String)) {
      Object transferData = event.data;
      if (transferData != null) {
        try {
          return new JavaTransferObject(transferData);
        }
        catch (Exception e) {
          if (ex == null) {
            ex = e;
          }
        }
      }
    }
    else if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
      String transferData = (String) event.data;
      if (transferData != null) {
        try {
          return new TextTransferObject(transferData);
        }
        catch (Exception e) {
          if (ex == null) {
            ex = e;
          }
        }
      }
    }
    return null;
  }

  /**
   * @param scoutTransferTypes
   * @return all transfer objects or an empty array NOT NULL
   */
  public static Transfer[] convertScoutTransferTypes(int scoutTransferTypes) {
    ArrayList<Transfer> uiTransferList = new ArrayList<Transfer>();
    if ((IDNDSupport.TYPE_FILE_TRANSFER & scoutTransferTypes) != 0) {
      uiTransferList.add(FileTransfer.getInstance());
    }
    if ((IDNDSupport.TYPE_IMAGE_TRANSFER & scoutTransferTypes) != 0) {
      uiTransferList.add(ImageTransfer.getInstance());
    }
    if ((IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER & scoutTransferTypes) != 0) {
      uiTransferList.add(JVMLocalObjectTransfer.getInstance());
    }
    if ((IDNDSupport.TYPE_TEXT_TRANSFER & scoutTransferTypes) != 0) {
      uiTransferList.add(TextTransfer.getInstance());
    }
    return uiTransferList.toArray(new Transfer[uiTransferList.size()]);

  }

  public static int getHorizontalAlignment(int scoutAlignment) {
    if (scoutAlignment < 0) {
      return SWT.LEFT;
    }
    else if (scoutAlignment == 0) {
      return SWT.CENTER;
    }
    else {
      return SWT.RIGHT;
    }
  }

  public static int getVerticalAlignment(int scoutAlignment) {
    if (scoutAlignment < 0) {
      return SWT.TOP;
    }
    else if (scoutAlignment == 0) {
      return SWT.NONE;
    }
    else {
      return SWT.BOTTOM;
    }
  }

  public static Point getLocationOnScreen(Control c) {
    Point p = c.toDisplay(0, 0);
    return p;
  }

  public static boolean isAncestorOf(Control ancestor, Control child) {
    if (ancestor == null || child == null) {
      return false;
    }
    else if (ancestor == child) {
      return true;
    }
    else {
      return isAncestorOf(ancestor, child.getParent());
    }
  }

  /**
   * @param r
   *          the original rectangle
   * @param includeReservedInsets
   *          if taskbar and other windowing insets should be included in the
   *          returned area
   * @return the effective view of the monitor that the rectangle mostly coveres
   */
  public static Rectangle getFullScreenBoundsFor(Display d, Rectangle r, boolean includeReservedInsets) {
    TreeMap<Integer, Rectangle> prioMap = new TreeMap<Integer, Rectangle>();
    for (Monitor dev : d.getMonitors()) {
      Rectangle bounds;
      if (!includeReservedInsets) {
        bounds = dev.getClientArea();
      }
      else {
        bounds = dev.getBounds();
      }
      Rectangle intersection = bounds.intersection(r);
      prioMap.put(intersection.width * intersection.height, bounds);
      // add default device with lowest prio
      if (dev == d.getPrimaryMonitor()) {
        prioMap.put(-1, bounds);
      }
    }
    return prioMap.get(prioMap.lastKey());
  }

  /**
   * @param r
   *          the original rectangle
   * @param includeReservedInsets
   *          if taskbar and other windowing insets should be included in the
   *          returned area
   * @param singleMonitor
   *          if only one monitor is to be used or all monitors together
   * @return the eventually resized and moved rectangle with regard to one or
   *         all (singleMonitorFlag) monitors
   */
  public static Rectangle validateRectangleOnScreen(Display d, Rectangle r, boolean includeReservedInsets, boolean singleMonitor) {
    Rectangle t = intersectRectangleWithScreen(d, r, includeReservedInsets, singleMonitor);
    if (!t.equals(r)) {
      Rectangle a = r;
      Rectangle screen = intersectRectangleWithScreen(d, new Rectangle(-100000, -100000, 200000, 200000), includeReservedInsets, singleMonitor);
      // first check size
      if (screen.width < a.width) {
        a.width = screen.width;
      }
      if (screen.height < a.height) {
        a.height = screen.height;
      }
      // adjust location
      if (a.x < screen.x) {
        a.x = screen.x;
      }
      if (a.y < screen.y) {
        a.y = screen.y;
      }
      if (a.x + a.width > screen.x + screen.width) {
        a.x = screen.x + screen.width - a.width;
      }
      if (a.y + a.height > screen.y + screen.height) {
        a.y = screen.y + screen.height - a.height;
      }
      return a;
    }
    else {
      return r;
    }
  }

  /**
   * @param r
   *          the original rectangle
   * @param includeReservedInsets
   *          if taskbar and other windowing insets should be included in the
   *          returned area
   * @param singleMonitor
   *          if only one monitor is to be used or all monitors together
   * @return the intersection of r with one or all (singleMonitorFlag) monitors
   */
  public static Rectangle intersectRectangleWithScreen(Display d, Rectangle r, boolean includeReservedInsets, boolean singleMonitor) {
    if (singleMonitor) {
      return r.intersection(getFullScreenBoundsFor(d, r, includeReservedInsets));
    }
    else {
      ArrayList<Rectangle> intersections = new ArrayList<Rectangle>();
      for (Monitor dev : d.getMonitors()) {
        Rectangle bounds;
        if (!includeReservedInsets) {
          bounds = dev.getClientArea();
        }
        else {
          bounds = dev.getBounds();
        }
        Rectangle intersection = bounds.intersection(r);
        if (!intersection.isEmpty()) {
          intersections.add(intersection);
        }
      }
      if (intersections.size() > 0) {
        Rectangle unionAll = null;
        for (Rectangle i : intersections) {
          if (unionAll == null) {
            unionAll = i;
          }
          else {
            unionAll = unionAll.union(i);
          }
        }
        return unionAll;
      }
      else {
        return new Rectangle(0, 0, 0, 0);
      }
    }
  }

  public static <T extends Widget> T findChildComponent(Widget parent, Class<T> type) {
    ArrayList<T> list = new ArrayList<T>(1);
    findChildComponentsRec(parent, type, list, 1);
    if (list.size() > 0) {
      return list.get(0);
    }
    else {
      return null;
    }
  }

  public static <T extends Widget> List<T> findChildComponents(Widget parent, Class<T> type) {
    ArrayList<T> list = new ArrayList<T>();
    findChildComponentsRec(parent, type, list, Integer.MAX_VALUE);
    return list;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Widget> void findChildComponentsRec(Widget parent, Class<T> type, List<T> list, int maxCount) {
    if (type.isAssignableFrom(parent.getClass())) {
      list.add((T) parent);
      if (list.size() >= maxCount) {
        return;
      }
    }
    //
    if (parent instanceof Composite) {
      for (Widget c : ((Composite) parent).getChildren()) {
        findChildComponentsRec(c, type, list, maxCount);
        if (list.size() >= maxCount) {
          return;
        }
      }
    }
  }

  public static boolean isPopupShell(Shell shell) {
    int style = shell.getStyle();
    Integer extendedStyle = (Integer) shell.getData("extendedStyle");
    if (extendedStyle != null) {
      style = style | extendedStyle;
    }
    return (style & SWT.POP_UP) != 0;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] getItemsOfSelection(Class<T> t, StructuredSelection selection) {
    T[] result = (T[]) Array.newInstance(t, selection.size());
    int i = 0;
    for (Object o : selection.toArray()) {
      result[i++] = (T) o;
    }
    return result;

  }

  /**
   * set the text provider for global swt texts on display
   */
  public static void setNlsTextsOnDisplay(Display display, ScoutTexts textProvider) {
    display.setData(ScoutTexts.JOB_PROPERTY_NAME.toString(), textProvider);
  }

  /**
   * @return the session scope specific text (maybe an override of the ScoutTexts text)
   */
  public static String getNlsText(Display display, String key, String... messageArguments) {
    if (display != null) {
      ScoutTexts textProvider = (ScoutTexts) display.getData(ScoutTexts.JOB_PROPERTY_NAME.toString());
      if (textProvider != null) {
        return textProvider.getText(key, messageArguments);
      }
    }
    return ScoutTexts.get(key, messageArguments);
  }

  /**
   * @param p
   *          is the location of the Table control (i.e. not scrollbar adjusted)
   */
  public static TableColumn getRwtColumnAt(Table table, Point p) {
    int x = p.x;
    if (table.getHorizontalBar() != null) {
      x += table.getHorizontalBar().getSelection();
    }
    for (TableColumn c : table.getColumns()) {
      if (x >= 0 && x <= c.getWidth()) {
        return c;
      }
      x = x - c.getWidth();
    }
    return null;
  }

  public static GridLayout createGridLayoutNoSpacing(int columnCount, boolean makeColumnsEqualWidth) {
    GridLayout layout = new GridLayout(columnCount, makeColumnsEqualWidth);
    layout.horizontalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 0;
    return layout;
  }
}
