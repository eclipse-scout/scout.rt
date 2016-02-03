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
package org.eclipse.scout.rt.ui.rap.util;

import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.scout.commons.BundleContextUtility;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.dnd.FileListTransferObject;
import org.eclipse.scout.commons.dnd.ImageTransferObject;
import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtScoutKeyStroke;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

public final class RwtUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtUtility.class);

  public static final boolean VALIDATE_HTML_CAPABLE = BundleContextUtility.parseBooleanProperty("org.eclipse.scout.rt.ui.rap.validate.htmlCapable", true);

  public static final String BROWSER_INFO = "browser-Info";

  public static final String VARIANT_PROPOSAL_FORM = "proposal-form";
  public static final String VARIANT_LISTBOX = "listbox";
  public static final String VARIANT_LISTBOX_DISABLED = "listboxDisabled";
  public static final String VARIANT_EMPTY = "empty";

  private static final Map<String, Integer> SCOUT_RWT_KEY_MAP;
  private static final Map<Integer, String> RWT_SCOUT_KEY_MAP;

  public static final String EXTENDED_STYLE = "extendedStyle";

  private RwtUtility() {
  }

  /**
   * Convenience method to get the environment from the client session
   *
   * @param session
   *          the clientsession from the current thread
   */
  public static IRwtEnvironment getUiEnvironment(IClientSession session) {
    IRwtEnvironment env = (IRwtEnvironment) session.getData(IRwtEnvironment.ENVIRONMENT_KEY);
    return env;
  }

  /**
   * Convenience method to get the environment from the display
   *
   * @param display
   *          the display from the current thread
   */
  public static IRwtEnvironment getUiEnvironment(Display display) {
    IRwtEnvironment env = (IRwtEnvironment) display.getData(IRwtEnvironment.class.getName());
    return env;
  }

  /**
   * Convenience method to get the locale from the current client session
   *
   * @param display
   *          the display from the current thread
   */
  public static Locale getClientSessionLocale(Display display) {
    return getUiEnvironment(display).getClientSession().getLocale();
  }

  public static BrowserInfo getBrowserInfo() {
    BrowserInfo info = (BrowserInfo) RWT.getUISession().getAttribute(BROWSER_INFO);
    if (info == null) {
      HttpServletRequest request = RWT.getRequest();
      info = createBrowserInfo(request);
      RWT.getUISession().setAttribute(BROWSER_INFO, info);
    }
    return info;
  }

  public static BrowserInfo createBrowserInfo(HttpServletRequest request) {
    return new BrowserInfoBuilder().createBrowserInfo(request);
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

  public static TransferObject createScoutTransferableFromClientFile(DropTargetEvent event, List<File> uploadedFiles) {
    if (event == null || event.currentDataType == null) {
      return null;
    }

    TransferObject scoutTransferObject = null;
    if (ClientFileTransfer.getInstance().isSupportedType(event.currentDataType)) {
      scoutTransferObject = new FileListTransferObject(uploadedFiles);
    }
    return scoutTransferObject;
  }

  /**
   * @param scoutTransferTypes
   * @return all transfer objects or an empty array NOT NULL
   */
  public static Transfer[] convertScoutTransferTypes(int scoutTransferTypes) {
    ArrayList<Transfer> uiTransferList = new ArrayList<Transfer>();
    boolean addClientFileTransfer = false;
    if ((IDNDSupport.TYPE_FILE_TRANSFER & scoutTransferTypes) != 0) {
      uiTransferList.add(FileTransfer.getInstance());
      addClientFileTransfer = true;
    }
    if ((IDNDSupport.TYPE_IMAGE_TRANSFER & scoutTransferTypes) != 0) {
      uiTransferList.add(ImageTransfer.getInstance());
      addClientFileTransfer = true;
    }
    if ((IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER & scoutTransferTypes) != 0) {
      uiTransferList.add(JVMLocalObjectTransfer.getInstance());
    }
    if ((IDNDSupport.TYPE_TEXT_TRANSFER & scoutTransferTypes) != 0) {
      uiTransferList.add(TextTransfer.getInstance());
    }
    if (addClientFileTransfer) {
      uiTransferList.add(ClientFileTransfer.getInstance());
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
    findChildComponentsRec(parent, type, null, list, 1);
    if (list.size() > 0) {
      return list.get(0);
    }
    else {
      return null;
    }
  }

  public static <T extends Widget> T findChildComponent(Widget parent, Class<T> type, Class<? extends Widget> excludedType) {
    ArrayList<T> list = new ArrayList<T>(1);
    findChildComponentsRec(parent, type, excludedType, list, 1);
    if (list.size() > 0) {
      return list.get(0);
    }
    else {
      return null;
    }
  }

  public static <T extends Widget> List<T> findChildComponents(Widget parent, Class<T> type) {
    ArrayList<T> list = new ArrayList<T>();
    findChildComponentsRec(parent, type, null, list, Integer.MAX_VALUE);
    return list;
  }

  public static <T extends Widget> List<T> findChildComponents(Widget parent, Class<T> type, Class<? extends Widget> excludedType) {
    ArrayList<T> list = new ArrayList<T>();
    findChildComponentsRec(parent, type, excludedType, list, Integer.MAX_VALUE);
    return list;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Widget> void findChildComponentsRec(Widget parent, Class<T> type, Class<? extends Widget> excludedType, List<T> list, int maxCount) {
    if (type.isAssignableFrom(parent.getClass())
        && (excludedType == null || !excludedType.isAssignableFrom(parent.getClass()))) {
      list.add((T) parent);
      if (list.size() >= maxCount) {
        return;
      }
    }
    //
    if (parent instanceof Composite) {
      for (Widget c : ((Composite) parent).getChildren()) {
        findChildComponentsRec(c, type, excludedType, list, maxCount);
        if (list.size() >= maxCount) {
          return;
        }
      }
    }
  }

  public static boolean isPopupShell(Shell shell) {
    int style = shell.getStyle();
    Integer extendedStyle = (Integer) shell.getData(EXTENDED_STYLE);
    if (extendedStyle != null) {
      style = style | extendedStyle;
    }
    return (style & SWT.POP_UP) != 0;
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> getItemsOfSelection(Class<T> t, StructuredSelection selection) {
    List<T> result = new ArrayList<T>();
    if (selection != null) {
      Iterator selectionIt = selection.iterator();
      while (selectionIt.hasNext()) {
        result.add((T) selectionIt.next());
      }
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
    // loop over all columns with respect to the current display order
    for (int index : table.getColumnOrder()) {
      TableColumn col = table.getColumn(index);
      if (col != null) {
        if (x >= 0 && x <= col.getWidth()) {
          return col;
        }
        x -= col.getWidth();
      }
    }
    return null;
  }

  public static MouseButton rwtToScoutMouseButton(int rwtButton) {
    switch (rwtButton) {
      case 1:
        return MouseButton.Left;
      case 3:
        return MouseButton.Right;
      default:
        return MouseButton.Unknown;
    }
  }

  public static IRwtKeyStroke[] getKeyStrokes(IKeyStroke stroke, IRwtEnvironment uiEnvironment) {
    ArrayList<IRwtKeyStroke> uiKeyStrokes = new ArrayList<IRwtKeyStroke>();
    int keycode = getRwtKeyCode(stroke);
    int stateMask = getRwtStateMask(stroke);
    // in case of enter register keypad enter as well
    if (keycode == SWT.CR) {
      uiKeyStrokes.add(new RwtScoutKeyStroke(stroke, uiEnvironment, SWT.CR, stateMask));
      uiKeyStrokes.add(new RwtScoutKeyStroke(stroke, uiEnvironment, SWT.KEYPAD_CR, stateMask));
    }
    else {
      uiKeyStrokes.add(new RwtScoutKeyStroke(stroke, uiEnvironment, keycode, stateMask));
    }
    return uiKeyStrokes.toArray(new IRwtKeyStroke[uiKeyStrokes.size()]);
  }

  public static int getRwtStateMask(IKeyStroke stoke) {
    String[] a = stoke.getKeyStroke().split("-");

    int stateMask = SWT.NONE;
    if (a.length > 1) {
      for (int i = 0; i < a.length - 1; i++) {
        stateMask |= scoutToRwtKey(a[i]);
      }
    }
    return stateMask;
  }

  /**
   * Converts {@link IKeyStroke} to a rwt keycode (This is a bitwise OR
   * of zero or more SWT key modifier masks (i.e. SWT.CTRL or SWT.ALT) and a
   * character code).<br>
   * For example if the keyStroke is defined as 'control-alt-f1' the method will return SWT.F1
   *
   * @param stroke
   * @return
   */
  public static int getRwtKeyCode(IKeyStroke keyStroke) {
    String[] keys = keyStroke.getKeyStroke().split("-");
    int rwtKeyCode = SWT.NONE;

    if (keys.length > 0) {
      rwtKeyCode = scoutToRwtKey(keys[keys.length - 1]);
    }
    return rwtKeyCode;
  }

  /**
   * Converts a scoutKey to an Rwt key. For example 'f11' will be converted to SWT.F11
   *
   * @param scoutKey
   *          must be lowercase, e.g. f11 instead of F11
   * @return Rwt key
   */
  public static int scoutToRwtKey(String scoutKey) {
    Integer i = SCOUT_RWT_KEY_MAP.get(scoutKey);
    if (i == null) {
      if (scoutKey.length() != 1) {
        LOG.warn("no key mapping for: " + scoutKey);
        return SWT.NONE;
      }
      else {
        return scoutKey.charAt(0);
      }
    }
    else {
      return i.intValue();
    }
  }

  /**
   * Keystroke to String
   */
  public static String getKeyTextFor(org.eclipse.swt.events.KeyEvent e) {
    String keyText;
    if (e.keyCode == 0) {
      return null;
    }
    else {
      keyText = getKeyTextUpper(e.keyCode);
    }

    if (keyText == null) {
      return null;
    }
    if ("shift".equals(keyText)) {
      return null;
    }
    if ("ctrl".equals(keyText)) {
      return null;
    }
    if ("alt".equals(keyText)) {
      return null;
    }
    StringBuffer buf = new StringBuffer();
    if ((e.stateMask & SWT.SHIFT) != 0) {
      buf.append("shift-");
    }
    if ((e.stateMask & SWT.CONTROL) != 0) {
      buf.append("ctrl-");
    }
    if ((e.stateMask & SWT.ALT) != 0) {
      buf.append("alt-");
    }
    buf.append(keyText);
    return buf.toString();
  }

  public static String getKeyTextUpper(int keyCode) {
    // If keycode is uppercase it is the seccond keycode from the client, first is lower case.
    if (keyCode >= 'A' && keyCode <= 'Z') {
      return "";
    }
    if (keyCode >= '0' && keyCode <= '9' || keyCode >= 'a' && keyCode <= 'z') {
      return String.valueOf(Character.toUpperCase((char) keyCode));
    }
    // Check for other ASCII keyCodes.
    int index = ",./\\[]`'".indexOf(keyCode);
    if (index >= 0) {
      return String.valueOf((char) keyCode);
    }
    if (keyCode >= SWT.KEYPAD_0 && keyCode <= SWT.KEYPAD_9) {
      return String.valueOf((char) (keyCode - SWT.KEYPAD_0 + '0'));
    }

    switch (keyCode) {
      case SWT.CR:
        return "RETURN";
      case SWT.BS:
        return "BACKSPACE";
      case SWT.TAB:
        return "TAB";
        // case SWT.CANCEL: return "cancel";
        // case KeyEvent.VK_CLEAR: return "clear";
//      case SWT.SHIFT:
//        return "SHIFT";
//      case SWT.CONTROL:
//        return "CONTROL";
//      case SWT.COMMAND:
//        return "COMMAND";
      case SWT.ALT:
        return "ALT";
//      case SWT.PAUSE:
//        return "Pause";
//      case SWT.CAPS_LOCK:
//        return "CapsLock";
      case SWT.ESC:
        return "ESCAPE";
      case ' ':
        return "SPACE";
      case SWT.PAGE_UP:
        return "PAGE_UP";
      case SWT.PAGE_DOWN:
        return "PAGE_DOWN";
      case SWT.END:
        return "END";
      case SWT.HOME:
        return "HOME";
      case SWT.ARROW_LEFT:
        return "ARROW_LEFT";
      case SWT.ARROW_UP:
        return "ARROW_UP";
      case SWT.ARROW_RIGHT:
        return "ARROW_RIGHT";
      case SWT.ARROW_DOWN:
        return "ARROW_DOWN";
      case SWT.KEYPAD_MULTIPLY:
        return "NUMPAD_MULTIPLY";
      case SWT.KEYPAD_ADD:
        return "NUMPAD_ADD";
      case SWT.KEYPAD_CR:
        return "RETURN";
      case SWT.KEYPAD_SUBTRACT:
        return "NUMPAD_SUBTRACT";
      case SWT.KEYPAD_DECIMAL:
        return "NUMPAD_DECIMAL";
      case SWT.KEYPAD_DIVIDE:
        return "NUMPAD_DIVIDE";
      case SWT.DEL:
        return "DELETE";
//      case SWT.NUM_LOCK:
//        return "NumLock";
//      case SWT.SCROLL_LOCK:
//        return "Scroll";
      case SWT.F1:
        return "F1";
      case SWT.F2:
        return "F2";
      case SWT.F3:
        return "F3";
      case SWT.F4:
        return "F4";
      case SWT.F5:
        return "F5";
      case SWT.F6:
        return "F6";
      case SWT.F7:
        return "F7";
      case SWT.F8:
        return "F8";
      case SWT.F9:
        return "F9";
      case SWT.F10:
        return "F10";
      case SWT.F11:
        return "F11";
      case SWT.F12:
        return "F12";
        //RAP [sle]: no representation of F13-F14 in qooxdoo
//      case SWT.F13: return "F13";
//      case SWT.F14: return "F14";
//      case SWT.F15: return "F15";
        //RAPEND
        // case KeyEvent.VK_F16: return "F16";
        // case KeyEvent.VK_F17: return "F17";
        // case KeyEvent.VK_F18: return "F18";
        // case KeyEvent.VK_F19: return "F19";
        // case KeyEvent.VK_F20: return "F20";
        // case KeyEvent.VK_F21: return "F21";
        // case KeyEvent.VK_F22: return "F22";
        // case KeyEvent.VK_F23: return "F23";
        // case KeyEvent.VK_F24: return "F24";
//      case SWT.PRINT_SCREEN:
//        return "PrintScreen";
      case SWT.INSERT:
        return "INSERT";
//      case SWT.HELP:
//        return "help";
//         case SWT.: return "Meta";XXX RAP [sle]: no Meta-key in SWT ??
        // case KeyEvent.VK_BACK_QUOTE: return "back_quote";
        // case KeyEvent.VK_QUOTE: return "quote";
        // case KeyEvent.VK_KP_UP: return "kp_up";
        // case KeyEvent.VK_KP_DOWN: return "kp_down";
        // case KeyEvent.VK_KP_LEFT: return "kp_left";
        // case KeyEvent.VK_KP_RIGHT: return "kp_right";
        // case KeyEvent.VK_DEAD_GRAVE: return "dead_grave";
        // case KeyEvent.VK_DEAD_ACUTE: return "dead_acute";
        // case KeyEvent.VK_DEAD_CIRCUMFLEX: return "dead_circumflex";
        // case KeyEvent.VK_DEAD_TILDE: return "dead_tilde";
        // case KeyEvent.VK_DEAD_MACRON: return "dead_macron";
        // case KeyEvent.VK_DEAD_BREVE: return "dead_breve";
        // case KeyEvent.VK_DEAD_ABOVEDOT: return "dead_abovedot";
        // case KeyEvent.VK_DEAD_DIAERESIS: return "dead_diaeresis";
        // case KeyEvent.VK_DEAD_ABOVERING: return "dead_abovering";
        // case KeyEvent.VK_DEAD_DOUBLEACUTE: return "dead_doubleacute";
        // case KeyEvent.VK_DEAD_CARON: return "dead_caron";
        // case KeyEvent.VK_DEAD_CEDILLA: return "dead_cedilla";
        // case KeyEvent.VK_DEAD_OGONEK: return "dead_ogonek";
        // case KeyEvent.VK_DEAD_IOTA: return "dead_iota";
        // case KeyEvent.VK_DEAD_VOICED_SOUND: return "dead_voiced_sound";
        // case KeyEvent.VK_DEAD_SEMIVOICED_SOUND: return
        // "dead_semivoiced_sound";
//      case '&':
//        return "ampersand";
        // case KeyEvent.VK_ASTERISK: return "asterisk";
        // case KeyEvent.VK_QUOTEDBL: return "quotedbl";
//      case '<':
//        return "less";
//      case '>':
//        return "greater";
        // case 161: return "braceleft";
        // case ')': return "braceright";
//      case '@':
//        return "at";
//      case KeyEvent.VK_COLON:
//        return "colon";
//      case '^':
//        return "circumflex";
//      case '$':
//        return "dollar";
//      case 128:
//        return "euro_sign";
//      case '!':
//        return "exclamation_mark";
//      case 161:
//        return "inverted_exclamation_mark";
//      case '(':
//        return "left_parenthesis";
//      case '#':
//        return "number_sign";
//      case '-':
//        return "minus";
//      case '+':
//        return "plus";
//      case ')':
//        return "right_parenthesis";
//      case '_':
//        return "underscore";
        // case KeyEvent.VK_FINAL: return "final";
        // case KeyEvent.VK_CONVERT: return "convert";
        // case KeyEvent.VK_NONCONVERT: return "nonconvert";
        // case KeyEvent.VK_ACCEPT: return "accept";
        // case KeyEvent.VK_MODECHANGE: return "modechange";
        // case KeyEvent.VK_KANA: return "kana";
        // case KeyEvent.VK_KANJI: return "kanji";
        // case KeyEvent.VK_ALPHANUMERIC: return "alphanumeric";
        // case KeyEvent.VK_KATAKANA: return "katakana";
        // case KeyEvent.VK_HIRAGANA: return "hiragana";
        // case KeyEvent.VK_FULL_WIDTH: return "full_width";
        // case KeyEvent.VK_HALF_WIDTH: return "half_width";
        // case KeyEvent.VK_ROMAN_CHARACTERS: return "roman_characters";
        // case KeyEvent.VK_ALL_CANDIDATES: return "all_candidates";
        // case KeyEvent.VK_PREVIOUS_CANDIDATE: return "previous_candidate";
        // case KeyEvent.VK_CODE_INPUT: return "code_input";
        // case KeyEvent.VK_JAPANESE_KATAKANA: return "japanese_katakana";
        // case KeyEvent.VK_JAPANESE_HIRAGANA: return "japanese_hiragana";
        // case KeyEvent.VK_JAPANESE_ROMAN: return "japanese_roman";
        // case KeyEvent.VK_KANA_LOCK: return "kana_lock";
        // case KeyEvent.VK_INPUT_METHOD_ON_OFF: return "input_method_on_off";
        // case KeyEvent.VK_AGAIN: return "again";
//      case KeyEvent.VK_UNDO:
//        return "undo";
        // case : return "copy";
        // case KeyEvent.VK_PASTE: return "paste";
        // case KeyEvent.VK_CUT: return "cut";
        // case KeyEvent.VK_FIND: return "find";
        // case KeyEvent.VK_PROPS: return "props";
        // case KeyEvent.VK_STOP: return "stop";
        // case KeyEvent.VK_COMPOSE: return "compose";
        // case KeyEvent.VK_ALT_GRAPH: return "alt_graph";
    }
    return "" + (char) keyCode;
  }

  static {
    SCOUT_RWT_KEY_MAP = new HashMap<String, Integer>();
    RWT_SCOUT_KEY_MAP = new HashMap<Integer, String>();
//    SCOUT_RWT_KEY_MAP.put("0", SWT.KEYPAD_0);
//    SCOUT_RWT_KEY_MAP.put("1", SWT.KEYPAD_1);
//    SCOUT_RWT_KEY_MAP.put("2", SWT.KEYPAD_2);
//    SCOUT_RWT_KEY_MAP.put("3", SWT.KEYPAD_3);
//    SCOUT_RWT_KEY_MAP.put("4", SWT.KEYPAD_4);
//    SCOUT_RWT_KEY_MAP.put("5", SWT.KEYPAD_5);
//    SCOUT_RWT_KEY_MAP.put("6", SWT.KEYPAD_6);
//    SCOUT_RWT_KEY_MAP.put("7", SWT.KEYPAD_7);
//    SCOUT_RWT_KEY_MAP.put("8", SWT.KEYPAD_8);
//    SCOUT_RWT_KEY_MAP.put("9", SWT.KEYPAD_9);
    SCOUT_RWT_KEY_MAP.put("enter", (int) SWT.CR);
    SCOUT_RWT_KEY_MAP.put("back_space", (int) SWT.BS);
    SCOUT_RWT_KEY_MAP.put("tab", (int) SWT.TAB);
    // SCOUT_RWT_KEY_MAP.put("cancel", SWT.CANCEL);
    // SCOUT_RWT_KEY_MAP.put("clear", KeyEvent.VK_CLEAR);
    SCOUT_RWT_KEY_MAP.put("shift", SWT.SHIFT);
    SCOUT_RWT_KEY_MAP.put("control", SWT.CONTROL);
    SCOUT_RWT_KEY_MAP.put("alt", SWT.ALT);
    SCOUT_RWT_KEY_MAP.put("alternate", SWT.ALT);
    SCOUT_RWT_KEY_MAP.put("pause", SWT.PAUSE);
    SCOUT_RWT_KEY_MAP.put("caps_lock", SWT.CAPS_LOCK);
    SCOUT_RWT_KEY_MAP.put("escape", (int) SWT.ESC);
    SCOUT_RWT_KEY_MAP.put("space", (int) ' ');
    SCOUT_RWT_KEY_MAP.put("page_up", SWT.PAGE_UP);
    SCOUT_RWT_KEY_MAP.put("page_down", SWT.PAGE_DOWN);
    SCOUT_RWT_KEY_MAP.put("end", SWT.END);
    SCOUT_RWT_KEY_MAP.put("home", SWT.HOME);
    SCOUT_RWT_KEY_MAP.put("left", SWT.ARROW_LEFT);
    SCOUT_RWT_KEY_MAP.put("up", SWT.ARROW_UP);
    SCOUT_RWT_KEY_MAP.put("right", SWT.ARROW_RIGHT);
    SCOUT_RWT_KEY_MAP.put("down", SWT.ARROW_DOWN);
    SCOUT_RWT_KEY_MAP.put("multiply", SWT.KEYPAD_MULTIPLY);
    SCOUT_RWT_KEY_MAP.put("add", SWT.KEYPAD_ADD);
    SCOUT_RWT_KEY_MAP.put("separater", SWT.KEYPAD_CR);
    SCOUT_RWT_KEY_MAP.put("subtract", SWT.KEYPAD_SUBTRACT);
    SCOUT_RWT_KEY_MAP.put("decimal", SWT.KEYPAD_DECIMAL);
    SCOUT_RWT_KEY_MAP.put("divide", SWT.KEYPAD_DIVIDE);
    SCOUT_RWT_KEY_MAP.put("delete", (int) SWT.DEL);
    SCOUT_RWT_KEY_MAP.put("num_lock", SWT.NUM_LOCK);
    SCOUT_RWT_KEY_MAP.put("scroll_lock", SWT.SCROLL_LOCK);
    SCOUT_RWT_KEY_MAP.put("f1", SWT.F1);
    SCOUT_RWT_KEY_MAP.put("f2", SWT.F2);
    SCOUT_RWT_KEY_MAP.put("f3", SWT.F3);
    SCOUT_RWT_KEY_MAP.put("f4", SWT.F4);
    SCOUT_RWT_KEY_MAP.put("f5", SWT.F5);
    SCOUT_RWT_KEY_MAP.put("f6", SWT.F6);
    SCOUT_RWT_KEY_MAP.put("f7", SWT.F7);
    SCOUT_RWT_KEY_MAP.put("f8", SWT.F8);
    SCOUT_RWT_KEY_MAP.put("f9", SWT.F9);
    SCOUT_RWT_KEY_MAP.put("f10", SWT.F10);
    SCOUT_RWT_KEY_MAP.put("f11", SWT.F11);
    SCOUT_RWT_KEY_MAP.put("f12", SWT.F12);
    SCOUT_RWT_KEY_MAP.put("f13", SWT.F13);
    SCOUT_RWT_KEY_MAP.put("f14", SWT.F14);
    SCOUT_RWT_KEY_MAP.put("f15", SWT.F15);
    // SCOUT_RWT_KEY_MAP.put("f16",(int) KeyEvent.VK_F16);
    // SCOUT_RWT_KEY_MAP.put("f17",(int) KeyEvent.VK_F17);
    // SCOUT_RWT_KEY_MAP.put("f18",(int) KeyEvent.VK_F18);
    // SCOUT_RWT_KEY_MAP.put("f19",(int) KeyEvent.VK_F19);
    // SCOUT_RWT_KEY_MAP.put("f20",(int) KeyEvent.VK_F20);
    // SCOUT_RWT_KEY_MAP.put("f21",(int) KeyEvent.VK_F21);
    // SCOUT_RWT_KEY_MAP.put("f22",(int) KeyEvent.VK_F22);
    // SCOUT_RWT_KEY_MAP.put("f23",(int) KeyEvent.VK_F23);
    // SCOUT_RWT_KEY_MAP.put("f24",(int) KeyEvent.VK_F24);
    SCOUT_RWT_KEY_MAP.put("printscreen", SWT.PRINT_SCREEN);
    SCOUT_RWT_KEY_MAP.put("insert", SWT.INSERT);
    SCOUT_RWT_KEY_MAP.put("help", SWT.HELP);
    // SCOUT_RWT_KEY_MAP.put("meta",(int) SWT.);
    // SCOUT_RWT_KEY_MAP.put("back_quote",(int) KeyEvent.VK_BACK_QUOTE);
    // SCOUT_RWT_KEY_MAP.put("quote",(int) KeyEvent.VK_QUOTE);
    // SCOUT_RWT_KEY_MAP.put("kp_up",(int) KeyEvent.VK_KP_UP);
    // SCOUT_RWT_KEY_MAP.put("kp_down",(int) KeyEvent.VK_KP_DOWN);
    // SCOUT_RWT_KEY_MAP.put("kp_left",(int) KeyEvent.VK_KP_LEFT);
    // SCOUT_RWT_KEY_MAP.put("kp_right",(int) KeyEvent.VK_KP_RIGHT);
    // SCOUT_RWT_KEY_MAP.put("dead_grave",(int) KeyEvent.VK_DEAD_GRAVE);
    // SCOUT_RWT_KEY_MAP.put("dead_acute",(int) KeyEvent.VK_DEAD_ACUTE);
    // SCOUT_RWT_KEY_MAP.put("dead_circumflex",(int)
    // KeyEvent.VK_DEAD_CIRCUMFLEX);
    // SCOUT_RWT_KEY_MAP.put("dead_tilde",(int) KeyEvent.VK_DEAD_TILDE);
    // SCOUT_RWT_KEY_MAP.put("dead_macron",(int) KeyEvent.VK_DEAD_MACRON);
    // SCOUT_RWT_KEY_MAP.put("dead_breve",(int) KeyEvent.VK_DEAD_BREVE);
    // SCOUT_RWT_KEY_MAP.put("dead_abovedot",(int)
    // KeyEvent.VK_DEAD_ABOVEDOT);
    // SCOUT_RWT_KEY_MAP.put("dead_diaeresis",(int)
    // KeyEvent.VK_DEAD_DIAERESIS);
    // SCOUT_RWT_KEY_MAP.put("dead_abovering",(int)
    // KeyEvent.VK_DEAD_ABOVERING);
    // SCOUT_RWT_KEY_MAP.put("dead_doubleacute",(int)
    // KeyEvent.VK_DEAD_DOUBLEACUTE);
    // SCOUT_RWT_KEY_MAP.put("dead_caron",(int) KeyEvent.VK_DEAD_CARON);
    // SCOUT_RWT_KEY_MAP.put("dead_cedilla",(int)
    // KeyEvent.VK_DEAD_CEDILLA);
    // SCOUT_RWT_KEY_MAP.put("dead_ogonek",(int) KeyEvent.VK_DEAD_OGONEK);
    // SCOUT_RWT_KEY_MAP.put("dead_iota",(int) KeyEvent.VK_DEAD_IOTA);
    // SCOUT_RWT_KEY_MAP.put("dead_voiced_sound",(int)
    // KeyEvent.VK_DEAD_VOICED_SOUND);
    // SCOUT_RWT_KEY_MAP.put("dead_semivoiced_sound",(int)
    // KeyEvent.VK_DEAD_SEMIVOICED_SOUND);
    SCOUT_RWT_KEY_MAP.put("ampersand", (int) '&');
    // SCOUT_RWT_KEY_MAP.put("asterisk",(int) KeyEvent.VK_ASTERISK);
    // SCOUT_RWT_KEY_MAP.put("quotedbl",(int) KeyEvent.VK_QUOTEDBL);
    SCOUT_RWT_KEY_MAP.put("less", (int) '<');
    SCOUT_RWT_KEY_MAP.put("greater", (int) '>');
    // SCOUT_RWT_KEY_MAP.put("braceleft",(int) 161);
    // SCOUT_RWT_KEY_MAP.put("braceright",(int) ')');
    SCOUT_RWT_KEY_MAP.put("at", (int) '@');
    SCOUT_RWT_KEY_MAP.put("colon", KeyEvent.VK_COLON);
    SCOUT_RWT_KEY_MAP.put("circumflex", (int) '^');
    SCOUT_RWT_KEY_MAP.put("dollar", (int) '$');
    SCOUT_RWT_KEY_MAP.put("euro_sign", 128);
    SCOUT_RWT_KEY_MAP.put("exclamation_mark", (int) '!');
    SCOUT_RWT_KEY_MAP.put("inverted_exclamation_mark", 161);
    SCOUT_RWT_KEY_MAP.put("left_parenthesis", (int) '(');
    SCOUT_RWT_KEY_MAP.put("number_sign", (int) '#');
    SCOUT_RWT_KEY_MAP.put("minus", (int) '-');
    SCOUT_RWT_KEY_MAP.put("plus", (int) '+');
    SCOUT_RWT_KEY_MAP.put("right_parenthesis", (int) ')');
    SCOUT_RWT_KEY_MAP.put("underscore", (int) '_');
    // SCOUT_RWT_KEY_MAP.put("final",(int) KeyEvent.VK_FINAL);
    // SCOUT_RWT_KEY_MAP.put("convert",(int) KeyEvent.VK_CONVERT);
    // SCOUT_RWT_KEY_MAP.put("nonconvert",(int) KeyEvent.VK_NONCONVERT);
    // SCOUT_RWT_KEY_MAP.put("accept",(int) KeyEvent.VK_ACCEPT);
    // SCOUT_RWT_KEY_MAP.put("modechange",(int) KeyEvent.VK_MODECHANGE);
    // SCOUT_RWT_KEY_MAP.put("kana",(int) KeyEvent.VK_KANA);
    // SCOUT_RWT_KEY_MAP.put("kanji",(int) KeyEvent.VK_KANJI);
    // SCOUT_RWT_KEY_MAP.put("alphanumeric",(int)
    // KeyEvent.VK_ALPHANUMERIC);
    // SCOUT_RWT_KEY_MAP.put("katakana",(int) KeyEvent.VK_KATAKANA);
    // SCOUT_RWT_KEY_MAP.put("hiragana",(int) KeyEvent.VK_HIRAGANA);
    // SCOUT_RWT_KEY_MAP.put("full_width",(int) KeyEvent.VK_FULL_WIDTH);
    // SCOUT_RWT_KEY_MAP.put("half_width",(int) KeyEvent.VK_HALF_WIDTH);
    // SCOUT_RWT_KEY_MAP.put("roman_characters",(int)
    // KeyEvent.VK_ROMAN_CHARACTERS);
    // SCOUT_RWT_KEY_MAP.put("all_candidates",(int)
    // KeyEvent.VK_ALL_CANDIDATES);
    // SCOUT_RWT_KEY_MAP.put("previous_candidate",(int)
    // KeyEvent.VK_PREVIOUS_CANDIDATE);
    // SCOUT_RWT_KEY_MAP.put("code_input",(int) KeyEvent.VK_CODE_INPUT);
    // SCOUT_RWT_KEY_MAP.put("japanese_katakana",(int)
    // KeyEvent.VK_JAPANESE_KATAKANA);
    // SCOUT_RWT_KEY_MAP.put("japanese_hiragana",(int)
    // KeyEvent.VK_JAPANESE_HIRAGANA);
    // SCOUT_RWT_KEY_MAP.put("japanese_roman",(int)
    // KeyEvent.VK_JAPANESE_ROMAN);
    // SCOUT_RWT_KEY_MAP.put("kana_lock",(int) KeyEvent.VK_KANA_LOCK);
    // SCOUT_RWT_KEY_MAP.put("input_method_on_off",(int)
    // KeyEvent.VK_INPUT_METHOD_ON_OFF);
    // SCOUT_RWT_KEY_MAP.put("again",(int) KeyEvent.VK_AGAIN);
    SCOUT_RWT_KEY_MAP.put("undo", KeyEvent.VK_UNDO);
    // SCOUT_RWT_KEY_MAP.put("copy",(int) );
    // SCOUT_RWT_KEY_MAP.put("paste",(int) KeyEvent.VK_PASTE);
    // SCOUT_RWT_KEY_MAP.put("cut",(int) KeyEvent.VK_CUT);
    // SCOUT_RWT_KEY_MAP.put("find",(int) KeyEvent.VK_FIND);
    // SCOUT_RWT_KEY_MAP.put("props",(int) KeyEvent.VK_PROPS);
    // SCOUT_RWT_KEY_MAP.put("stop",(int) KeyEvent.VK_STOP);
    // SCOUT_RWT_KEY_MAP.put("compose",(int) KeyEvent.VK_COMPOSE);
    // SCOUT_RWT_KEY_MAP.put("alt_graph",(int) KeyEvent.VK_ALT_GRAPH);

    // SWT -> Scout
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_0, "0");
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_1, "1");
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_2, "2");
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_3, "3");
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_4, "4");
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_5, "5");
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_6, "6");
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_7, "7");
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_8, "8");
//    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_9, "9");
    RWT_SCOUT_KEY_MAP.put((int) SWT.CR, "enter");
    RWT_SCOUT_KEY_MAP.put((int) SWT.BS, "back_space");
    RWT_SCOUT_KEY_MAP.put((int) SWT.TAB, "tab");
    // RWT_SCOUT_KEY_MAP.put( SWT.CANCEL, "cancel");
    // RWT_SCOUT_KEY_MAP.put( KeyEvent.VK_CLEAR, "clear");
    RWT_SCOUT_KEY_MAP.put(SWT.SHIFT, "shift");
    RWT_SCOUT_KEY_MAP.put(SWT.CONTROL, "control");
    RWT_SCOUT_KEY_MAP.put(SWT.ALT, "alt");
    RWT_SCOUT_KEY_MAP.put(SWT.PAUSE, "pause");
    RWT_SCOUT_KEY_MAP.put(SWT.CAPS_LOCK, "caps_lock");
    RWT_SCOUT_KEY_MAP.put((int) SWT.ESC, "escape");
    RWT_SCOUT_KEY_MAP.put((int) ' ', "space");
    RWT_SCOUT_KEY_MAP.put(SWT.PAGE_UP, "page_up");
    RWT_SCOUT_KEY_MAP.put(SWT.PAGE_DOWN, "page_down");
    RWT_SCOUT_KEY_MAP.put(SWT.END, "end");
    RWT_SCOUT_KEY_MAP.put(SWT.HOME, "home");
    RWT_SCOUT_KEY_MAP.put(SWT.ARROW_LEFT, "left");
    RWT_SCOUT_KEY_MAP.put(SWT.ARROW_UP, "up");
    RWT_SCOUT_KEY_MAP.put(SWT.ARROW_RIGHT, "right");
    RWT_SCOUT_KEY_MAP.put(SWT.ARROW_DOWN, "down");
    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_MULTIPLY, "multiply");
    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_ADD, "add");
    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_CR, "separater");
    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_SUBTRACT, "subtract");
    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_DECIMAL, "decimal");
    RWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_DIVIDE, "divide");
    RWT_SCOUT_KEY_MAP.put((int) SWT.DEL, "delete");
    RWT_SCOUT_KEY_MAP.put(SWT.NUM_LOCK, "num_lock");
    RWT_SCOUT_KEY_MAP.put(SWT.SCROLL_LOCK, "scroll_lock");
    RWT_SCOUT_KEY_MAP.put(SWT.F1, "f1");
    RWT_SCOUT_KEY_MAP.put(SWT.F2, "f2");
    RWT_SCOUT_KEY_MAP.put(SWT.F3, "f3");
    RWT_SCOUT_KEY_MAP.put(SWT.F4, "f4");
    RWT_SCOUT_KEY_MAP.put(SWT.F5, "f5");
    RWT_SCOUT_KEY_MAP.put(SWT.F6, "f6");
    RWT_SCOUT_KEY_MAP.put(SWT.F7, "f7");
    RWT_SCOUT_KEY_MAP.put(SWT.F8, "f8");
    RWT_SCOUT_KEY_MAP.put(SWT.F9, "f9");
    RWT_SCOUT_KEY_MAP.put(SWT.F10, "f10");
    RWT_SCOUT_KEY_MAP.put(SWT.F11, "f11");
    RWT_SCOUT_KEY_MAP.put(SWT.F12, "f12");
    RWT_SCOUT_KEY_MAP.put(SWT.F13, "f13");
    RWT_SCOUT_KEY_MAP.put(SWT.F14, "f14");
    RWT_SCOUT_KEY_MAP.put(SWT.F15, "f15");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F16, "f16");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F17, "f17");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F18, "f18");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F19, "f19");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F20, "f20");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F21, "f21");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F22, "f22");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F23, "f23");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F24, "f24");
    RWT_SCOUT_KEY_MAP.put(SWT.PRINT_SCREEN, "printscreen");
    RWT_SCOUT_KEY_MAP.put(SWT.INSERT, "insert");
    RWT_SCOUT_KEY_MAP.put(SWT.HELP, "help");
    // RWT_SCOUT_KEY_MAP.put((int) SWT., "meta");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_BACK_QUOTE, "back_quote");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_QUOTE, "quote");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KP_UP, "kp_up");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KP_DOWN, "kp_down");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KP_LEFT, "kp_left");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KP_RIGHT, "kp_right");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_GRAVE, "dead_grave");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_ACUTE, "dead_acute");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_CIRCUMFLEX, "dead_circumflex");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_TILDE, "dead_tilde");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_MACRON, "dead_macron");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_BREVE, "dead_breve");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_ABOVEDOT, "dead_abovedot");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_DIAERESIS, "dead_diaeresis");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_ABOVERING, "dead_abovering");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_DOUBLEACUTE, "dead_doubleacute");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_CARON, "dead_caron");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_CEDILLA, "dead_cedilla");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_OGONEK, "dead_ogonek");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_IOTA, "dead_iota");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_VOICED_SOUND, "dead_voiced_sound");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_SEMIVOICED_SOUND, "dead_semivoiced_sound");
    RWT_SCOUT_KEY_MAP.put((int) '&', "ampersand");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_ASTERISK, "asterisk");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_QUOTEDBL, "quotedbl");
    RWT_SCOUT_KEY_MAP.put((int) '<', "less");
    RWT_SCOUT_KEY_MAP.put((int) '>', "greater");
    // RWT_SCOUT_KEY_MAP.put((int) 161, "braceleft");
    // SCOUT_RWT_KEY_MAP.put("braceright",(int) ')');
    RWT_SCOUT_KEY_MAP.put((int) '@', "at");
    RWT_SCOUT_KEY_MAP.put(KeyEvent.VK_COLON, "colon");
    RWT_SCOUT_KEY_MAP.put((int) '^', "circumflex");
    RWT_SCOUT_KEY_MAP.put((int) '$', "dollar");
    RWT_SCOUT_KEY_MAP.put(128, "euro_sign");
    RWT_SCOUT_KEY_MAP.put((int) '!', "exclamation_mark");
    RWT_SCOUT_KEY_MAP.put(161, "inverted_exclamation_mark");
    RWT_SCOUT_KEY_MAP.put((int) '(', "left_parenthesis");
    RWT_SCOUT_KEY_MAP.put((int) '#', "number_sign");
    RWT_SCOUT_KEY_MAP.put((int) '-', "minus");
    RWT_SCOUT_KEY_MAP.put((int) '+', "plus");
    SCOUT_RWT_KEY_MAP.put("right_parenthesis", (int) ')');
    RWT_SCOUT_KEY_MAP.put((int) '_', "underscore");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_FINAL, "final");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_CONVERT, "convert");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_NONCONVERT, "nonconvert");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_ACCEPT, "accept");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_MODECHANGE, "modechange");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KANA, "kana");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KANJI, "kanji");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_ALPHANUMERIC, "alphanumeric");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KATAKANA, "katakana");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_HIRAGANA, "hiragana");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_FULL_WIDTH, "full_width");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_HALF_WIDTH, "half_width");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_ROMAN_CHARACTERS, "roman_characters");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_ALL_CANDIDATES, "all_candidates");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_PREVIOUS_CANDIDATE, "previous_candidate");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_CODE_INPUT, "code_input");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_JAPANESE_KATAKANA, "japanese_katakana");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_JAPANESE_HIRAGANA, "japanese_hiragana");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_JAPANESE_ROMAN, "japanese_roman");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KANA_LOCK, "kana_lock");
    // RWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_INPUT_METHOD_ON_OFF, "input_method_on_off");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_AGAIN, "again");
    RWT_SCOUT_KEY_MAP.put(KeyEvent.VK_UNDO, "undo");
    // RWT_SCOUT_KEY_MAP.put((int) , "copy");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_PASTE, "paste");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_CUT, "cut");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_FIND, "find");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_PROPS, "props");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_STOP, "stop");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_COMPOSE, "compose");
    // RWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_ALT_GRAPH, "alt_graph");
  }

  public static Map<String, Integer> getScoutRwtKeyMap() {
    return SCOUT_RWT_KEY_MAP;
  }

  /**
   * Since there is no way to easily delegate and simulate up/down pageup/pagedown events on trees, tables and other
   * widgets,
   * this helper offers this convenience.
   */
  public static boolean handleNavigationKey(Widget w, int keyCode) {
    // tree
    if (w instanceof Tree) {
      return handleNavigationKeyOnTree((Tree) w, keyCode);
    }
    else if (w instanceof Table) {
      return handleNavigationKeyOnTable((Table) w, keyCode);
    }
    return false;
  }

  /**
   * Since there is no way to easily delegate and simulate up/down pageup/pagedown events on trees,
   * this helper offers this convenience.
   */
  public static boolean handleNavigationKeyOnTree(Tree tree, int keyCode) {
    if (tree != null && tree.getItemCount() > 0) {
      TreeItem[] a = tree.getSelection();
      TreeItem selItem = a != null && a.length > 0 ? a[0] : null;
      //
      TreeItem next = null;
      switch (keyCode) {
        case SWT.ARROW_DOWN:
          if (selItem == null) {
            next = tree.getItem(0);

          }
          else {
            next = getNextTreeRow(tree, selItem);

          }
          break;
        case SWT.ARROW_UP:
          if (selItem == null) {
            next = getLastTreeRow(tree);
          }
          else {
            next = getPreviousTreeRow(tree, selItem);

          }
          break;
        case SWT.PAGE_DOWN:
          //XXX rap
          /*
          if (selItem == null) {
            next = tree.getItem(0);
            tree.setTopItem(next);
          }
          else {
            Rectangle r = tree.getTopItem().getBounds();
            int y = r.y + tree.getClientArea().height;
            TreeItem t = tree.getTopItem();
            while ((t = getNextTreeRow(tree, t)) != null) {
              r = t.getBounds();
              if (r.y + r.height <= y) {
                next = t;
              }
              else {
                break;
              }
            }
            if (next != null) {
              tree.setTopItem(next);
            }
          }
           */
          break;
        case SWT.PAGE_UP:
          //XXX rap
          /*
          if (selItem == null) {
            next = getLastTreeRow(tree);
            if (next != null) {
              tree.setTopItem(next);
            }
          }
          else if (selItem != tree.getTopItem()) {
            next = tree.getTopItem();
            tree.setTopItem(next);
          }
          else {
            Rectangle r = tree.getTopItem().getBounds();
            int y = r.y + r.height - tree.getClientArea().height + 1;
            TreeItem t = tree.getTopItem();
            while ((t = getPreviousTreeRow(tree, t)) != null) {
              next = t;
              r = t.getBounds();
              if (r.y <= y) {
                break;
              }
            }
            if (next != null) {
              tree.setTopItem(next);
            }
          }
           */
          break;
      }
      if (next != null) {
        tree.setSelection(next);
        // propagate selection
        Event selE = new Event();
        selE.type = SWT.Selection;
        selE.item = next;
        selE.widget = tree;
        for (Listener l : tree.getListeners(SWT.Selection)) {
          l.handleEvent(selE);
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Since there is no way to easily delegate and simulate up/down pageup/pagedown events on tables,
   * this helper offers this convenience.
   */
  public static boolean handleNavigationKeyOnTable(Table table, int keyCode) {
    if (table != null && table.getItemCount() > 0) {
      int count = table.getItemCount();
      TableItem[] a = table.getSelection();
      TableItem selItem = a != null && a.length > 0 ? a[0] : null;
      int selIndex = table.getSelectionIndex();
      //
      TableItem next = null;
      switch (keyCode) {
        case SWT.ARROW_DOWN:
          if (selItem == null) {
            next = table.getItem(0);
          }
          else if (selIndex + 1 < count) {
            next = table.getItem(selIndex + 1);
          }
          break;
        case SWT.ARROW_UP:
          if (selItem == null) {
            next = table.getItem(count - 1);
          }
          else if (selIndex - 1 >= 0) {
            next = table.getItem(selIndex - 1);
          }
          break;
        case SWT.PAGE_DOWN:
          if (selItem == null) {
            next = table.getItem(0);
            table.setTopIndex(table.indexOf(next));
          }
          else {
            Rectangle r = table.getItem(table.getTopIndex()).getBounds();
            next = table.getItem(new Point(r.x, r.y + table.getClientArea().height - 1));
            if (next == null) {
              next = table.getItem(count - 1);
            }
            if (next != null) {
              table.setTopIndex(table.indexOf(next));
            }
          }
          break;
        case SWT.PAGE_UP:
          if (selItem == null) {
            next = table.getItem(count - 1);
            table.setTopIndex(table.indexOf(next));
          }
          else if (selIndex != table.getTopIndex()) {
            next = table.getItem(table.getTopIndex());
          }
          else {
            Rectangle r = table.getItem(table.getTopIndex()).getBounds();
            int y = r.y + r.height - table.getClientArea().height + 1;
            for (int i = selIndex - 1; i >= 0; i--) {
              TableItem test = table.getItem(i);
              if (test.getBounds().y <= y) {
                next = test;
                break;
              }
            }
            if (next == null) {
              next = table.getItem(0);
            }
            if (next != null) {
              table.setTopIndex(table.indexOf(next));
            }
          }
          break;
      }
      if (next != null) {
        table.setSelection(next);
        // propagate selection
        Event selE = new Event();
        selE.type = SWT.Selection;
        selE.item = next;
        selE.widget = table;
        for (Listener l : table.getListeners(SWT.Selection)) {
          l.handleEvent(selE);
        }
        return true;
      }
    }
    return false;
  }

  public static TreeItem getNextTreeRow(Tree tree, TreeItem item) {
    // child
    if (item.getExpanded()) {
      if (item.getItemCount() > 0) {
        return item.getItem(0);
      }
    }
    // sibling
    TreeItem parent;
    while ((parent = item.getParentItem()) != null) {
      int i = parent.indexOf(item);
      if (i >= 0 && i + 1 < parent.getItemCount()) {
        return parent.getItem(i + 1);
      }
      // next
      item = parent;
    }
    // root sibling
    int i = tree.indexOf(item);
    if (i >= 0 && i + 1 < tree.getItemCount()) {
      return tree.getItem(i + 1);
    }
    // default
    return null;
  }

  public static TreeItem getPreviousTreeRow(Tree tree, TreeItem item) {
    // sibling
    TreeItem parent = item.getParentItem();
    if (parent != null) {
      int i = parent.indexOf(item);
      if (i > 0) {
        TreeItem t = parent.getItem(i - 1);
        // deepest child of sibling
        while (t.getExpanded() && t.getItemCount() > 0) {
          t = t.getItem(t.getItemCount() - 1);
        }
        return t;
      }
      else {
        // parent itself
        return parent;
      }
    }
    // root sibling
    int i = tree.indexOf(item);
    if (i > 0) {
      TreeItem t = tree.getItem(i - 1);
      // deepest child
      while (t.getExpanded() && t.getItemCount() > 0) {
        t = t.getItem(t.getItemCount() - 1);
      }
      return t;
    }
    // default
    return null;
  }

  public static TreeItem getLastTreeRow(Tree tree) {
    if (tree.getItemCount() > 0) {
      TreeItem t = tree.getItem(tree.getItemCount() - 1);
      // deepest child
      while (t.getExpanded() && t.getItemCount() > 0) {
        t = t.getItem(t.getItemCount() - 1);
      }
      return t;
    }
    // default
    return null;
  }

  /**
   * Visit the complete workbench shell tree.
   * Ignore popup shells and shells
   * with extendedStyle popup
   * <p>
   * The list is ordered by the following priorities:<br>
   * 1. system modal before application modal before modeless<br>
   * 2. sub shells before parent shells before top level shells
   */
  public static void visitShellTreeRec(Shell shell, int modalities, int level, TreeMap<CompositeLong, Shell> out) {
    if (shell == null) {
      return;
    }
    if (!shell.isVisible()) {
      return;
    }
    if (isPopupShell(shell)) {
      return;
    }
    int style = shell.getStyle();
    if (level == 0) {
      out.put(new CompositeLong(9, -level), shell);
    }
    else if ((style & SWT.SYSTEM_MODAL) != 0) {
      if ((modalities & SWT.SYSTEM_MODAL) != 0) {
        out.put(new CompositeLong(0, -level), shell);
      }
    }
    else if ((style & SWT.APPLICATION_MODAL) != 0) {
      if ((modalities & SWT.APPLICATION_MODAL) != 0) {
        out.put(new CompositeLong(1, -level), shell);
      }
    }
    else {
      if ((modalities & SWT.MODELESS) != 0) {
        out.put(new CompositeLong(2, -level), shell);
      }
    }
    // children
    Shell[] children = shell.getShells();
    if (children != null) {
      for (Shell child : children) {
        visitShellTreeRec(child, modalities, level + 1, out);
      }
    }
  }

  private static final Pattern MNEMONIC_PATTERN = Pattern.compile("(\\&)", Pattern.DOTALL);

  /**
   * Escapes every mnemonic character '&' in the string by simply doubling the character.
   *
   * @param text
   *          the string to be escaped, also <code>null</code> or empty string values are allowed
   * @return the escaped string
   * @see org.eclipse.swt.widgets.Label#setText(String)
   */
  public static String escapeMnemonics(String text) {
    if (StringUtility.isNullOrEmpty(text)) {
      return text;
    }
    return MNEMONIC_PATTERN.matcher(text).replaceAll("\\&$1");
  }

  /**
   * @deprecated Use {@link #runUiInputVerifier(Control)} instead. Will be removed in the 5.0 Release.
   */
  @Deprecated
  public static void verifyUiInput(Control control) {
    runUiInputVerifier(control);
  }

  /**
   * Run the inputVerifier on the currently focused control. See {@link #runUiInputVerifier(Control)} for more details.
   *
   * @since 3.10.0-M5
   */
  public static void runUiInputVerifier() {
    Control focusControl = Display.getDefault().getFocusControl();
    runUiInputVerifier(focusControl);
  }

  /**
   * Force the control's inputVerifier to run
   */
  public static void runUiInputVerifier(Control control) {
    if (control == null || control.isDisposed()) {
      return;
    }

    IRwtScoutComposite compositeOnWidget = RwtScoutComposite.getCompositeOnWidget(control);
    if (compositeOnWidget instanceof RwtScoutComposite) {
      ((RwtScoutComposite) compositeOnWidget).runUiInputVerifier();
    }
  }

  /**
   * Pretty printed version of the key stroke
   * <p>
   * Example:
   * <ul>
   * <li>control-alternate-f1 --> Ctrl+Alt+F1
   * </ul>
   *
   * @since 3.10.0-M4
   */
  public static String getKeyStrokePrettyPrinted(IAction scoutAction) {
    if (scoutAction == null) {
      return "";
    }
    return RwtUtility.getKeyStrokePrettyPrinted(scoutAction.getKeyStroke());
  }

  /**
   * Pretty printed version of the key stroke.
   * See {@link RwtUtility#getKeyStrokePrettyPrinted(IAction)}
   *
   * @since 3.10.0-M4
   */
  public static String getKeyStrokePrettyPrinted(String s) {
    if (!StringUtility.hasText(s)) {
      return "";
    }
    KeyStroke ks = new KeyStroke(s);
    int stateMask = getRwtStateMask(ks);
    int keyCode = getRwtKeyCode(ks);
    return LegacyActionTools.convertAccelerator(stateMask | keyCode);
  }

  /**
   * Checks if the given widget is enabled for markup.
   *
   * @since 4.2
   */
  public static boolean isMarkupEnabled(Widget w) {
    return Boolean.TRUE.equals(w.getData(RWT.MARKUP_ENABLED));
  }

}
