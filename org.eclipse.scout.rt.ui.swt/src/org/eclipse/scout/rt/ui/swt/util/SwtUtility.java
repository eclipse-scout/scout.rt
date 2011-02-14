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
package org.eclipse.scout.rt.ui.swt.util;

import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.commons.dnd.FileListTransferObject;
import org.eclipse.scout.commons.dnd.ImageTransferObject;
import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.keystroke.SwtScoutKeyStroke;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

public final class SwtUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtUtility.class);
  private static final HashMap<String, Integer> SCOUT_SWT_KEY_MAP;
  private static final HashMap<Integer, String> SWT_SCOUT_KEY_MAP;

  private SwtUtility() {
  }

  public static Object createSwtTransferable(TransferObject scoutT) {
    if (scoutT == null) {
      return null;
    }
    if (scoutT instanceof FileListTransferObject) {
      return ((FileListTransferObject) scoutT).getFilenames();
    }
    else if (scoutT instanceof TextTransferObject) {
      return ((TextTransferObject) scoutT).getText();
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

  public static TransferObject createScoutTransferable(DropTargetEvent swtT) {
    if (swtT == null || swtT.currentDataType == null) {
      return null;

    }
    Exception ex = null;
    if (FileTransfer.getInstance().isSupportedType(swtT.currentDataType)) {
      String[] fileNames = (String[]) swtT.data;
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
    else if (JVMLocalObjectTransfer.getInstance().isSupportedType(swtT.currentDataType) && !(swtT.data instanceof String)) {
      Object transferData = swtT.data;
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
    else if (TextTransfer.getInstance().isSupportedType(swtT.currentDataType)) {
      String transferData = (String) swtT.data;
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

    // DataFlavor[] flavors=swtT.getTransferDataFlavors();
    // if(swtT.type == FileTransfer)
    // for(int i=0;i<flavors.length;i++){
    // if(flavors[i].isFlavorJavaFileListType()){
    // try{
    // ArrayList<File> fileList=new ArrayList<File>();
    // fileList.addAll((List)swtT.getTransferData(flavors[i]));
    // return new FileListTransferObject(fileList);
    // }
    // catch(Exception e){
    // if(ex==null) ex=e;
    // }
    // }
    // else if(flavors[i].isFlavorTextType()){
    // try{
    // return new TextTransferObject((String)swtT.getTransferData(flavors[i]));
    // }
    // catch(Exception e){
    // if(ex==null) ex=e;
    // }
    // }
    // else if(flavors[i].isMimeTypeEqual(DataFlavor.imageFlavor)){
    // try{
    // return new TextTransferObject((String)swtT.getTransferData(flavors[i]));
    // }
    // catch(Exception e){
    // if(ex==null) ex=e;
    // }
    // }
    // else
    // if(flavors[i].isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)){
    // try{
    // return new JavaTransferObject(swtT.getTransferData(flavors[i]));
    // }
    // catch(Exception e){
    // if(ex==null) ex=e;
    // }
    // }
    // }
    // if(ex!=null){
    // LOG.warn("swt transferable="+swtT,ex);
    // }
    return null;
  }

  /**
   * @param scoutTransferTypes
   * @return all transfer objects or an empty array NOT NULL
   */
  public static Transfer[] convertScoutTransferTypes(int scoutTransferTypes) {
    ArrayList<Transfer> swtTransferList = new ArrayList<Transfer>();
    if ((IDNDSupport.TYPE_FILE_TRANSFER & scoutTransferTypes) != 0) {
      swtTransferList.add(FileTransfer.getInstance());
    }
    if ((IDNDSupport.TYPE_IMAGE_TRANSFER & scoutTransferTypes) != 0) {
      swtTransferList.add(ImageTransfer.getInstance());
    }
    if ((IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER & scoutTransferTypes) != 0) {
      swtTransferList.add(JVMLocalObjectTransfer.getInstance());
    }
    if ((IDNDSupport.TYPE_TEXT_TRANSFER & scoutTransferTypes) != 0) {
      swtTransferList.add(TextTransfer.getInstance());
    }
    return swtTransferList.toArray(new Transfer[swtTransferList.size()]);

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

  public static ISwtKeyStroke[] getKeyStrokes(IKeyStroke stroke, ISwtEnvironment environment) {
    ArrayList<ISwtKeyStroke> swtKeyStrokes = new ArrayList<ISwtKeyStroke>();
    int keycode = getSwtKeyCode(stroke);
    int stateMask = getSwtStateMask(stroke);
    // in case of enter register keypad enter as well
    if (keycode == SWT.CR) {
      swtKeyStrokes.add(new SwtScoutKeyStroke(stroke, SWT.CR, stateMask, environment));
      swtKeyStrokes.add(new SwtScoutKeyStroke(stroke, SWT.KEYPAD_CR, stateMask, environment));
    }
    else {
      swtKeyStrokes.add(new SwtScoutKeyStroke(stroke, keycode, stateMask, environment));
    }
    return swtKeyStrokes.toArray(new ISwtKeyStroke[swtKeyStrokes.size()]);
  }

  public static int getSwtStateMask(IKeyStroke stoke) {
    String[] a = stoke.getKeyStroke().split("-");

    int stateMask = SWT.NONE;
    if (a.length > 1) {
      for (int i = 0; i < a.length - 1; i++) {
        stateMask |= scoutToSwtKey(a[i]);
      }
    }
    return stateMask;
  }

  /**
   * Converts {@link IKeyStroke} to a swt keycode (This is a bitwise OR
   * of zero or more SWT key modifier masks (i.e. SWT.CTRL or SWT.ALT) and a
   * character code).
   * 
   * @param stroke
   * @return
   */
  public static int getSwtKeyCode(IKeyStroke keyStroke) {
    String[] keys = keyStroke.getKeyStroke().split("-");
    int swtKeyCode = SWT.NONE;

    if (keys.length > 0) {
      swtKeyCode = scoutToSwtKey(keys[keys.length - 1]);
    }
    return swtKeyCode;
  }

  public static int scoutToSwtKey(String scoutKey) {
    Integer i = SCOUT_SWT_KEY_MAP.get(scoutKey);
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
      keyText = getKeyTextLower(e.keyCode);
    }

    if (keyText == null) {
      return null;
    }
    if (keyText.equals("shift")) {
      return null;
    }
    if (keyText.equals("ctrl")) {
      return null;
    }
    if (keyText.equals("alt")) {
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

  public static String getKeyTextLower(int keyCode) {
    if (keyCode >= '0' && keyCode <= '9' || keyCode >= 'A' && keyCode <= 'Z') {
      return String.valueOf(Character.toLowerCase((char) keyCode));
    }
    // Check for other ASCII keyCodes.
    int index = ",./;=[\\]".indexOf(keyCode);
    if (index >= 0) {
      return String.valueOf((char) keyCode);
    }
    if (keyCode >= SWT.KEYPAD_0 && keyCode <= SWT.KEYPAD_9) {
      return String.valueOf((char) (keyCode - SWT.KEYPAD_0 + '0'));
    }

    switch (keyCode) {
      case SWT.CR:
        return "enter";
      case SWT.BS:
        return "back_space";
      case SWT.TAB:
        return "tab";
        // case SWT.CANCEL: return "cancel";
        // case KeyEvent.VK_CLEAR: return "clear";
      case SWT.SHIFT:
        return "shift";
      case SWT.CONTROL:
        return "control";
      case SWT.ALT:
        return "alt";
      case SWT.PAUSE:
        return "pause";
      case SWT.CAPS_LOCK:
        return "caps_lock";
      case SWT.ESC:
        return "escape";
      case ' ':
        return "space";
      case SWT.PAGE_UP:
        return "page_up";
      case SWT.PAGE_DOWN:
        return "page_down";
      case SWT.END:
        return "end";
      case SWT.HOME:
        return "home";
      case SWT.ARROW_LEFT:
        return "left";
      case SWT.ARROW_UP:
        return "up";
      case SWT.ARROW_RIGHT:
        return "right";
      case SWT.ARROW_DOWN:
        return "down";
      case SWT.KEYPAD_MULTIPLY:
        return "multiply";
      case SWT.KEYPAD_ADD:
        return "add";
      case SWT.KEYPAD_CR:
        return "separater";
      case SWT.KEYPAD_SUBTRACT:
        return "subtract";
      case SWT.KEYPAD_DECIMAL:
        return "decimal";
      case SWT.KEYPAD_DIVIDE:
        return "divide";
      case SWT.DEL:
        return "delete";
      case SWT.NUM_LOCK:
        return "num_lock";
      case SWT.SCROLL_LOCK:
        return "scroll_lock";
      case SWT.F1:
        return "f1";
      case SWT.F2:
        return "f2";
      case SWT.F3:
        return "f3";
      case SWT.F4:
        return "f4";
      case SWT.F5:
        return "f5";
      case SWT.F6:
        return "f6";
      case SWT.F7:
        return "f7";
      case SWT.F8:
        return "f8";
      case SWT.F9:
        return "f9";
      case SWT.F10:
        return "f10";
      case SWT.F11:
        return "f11";
      case SWT.F12:
        return "f12";
      case SWT.F13:
        return "f13";
      case SWT.F14:
        return "f14";
      case SWT.F15:
        return "f15";
        // case KeyEvent.VK_F16: return "f16";
        // case KeyEvent.VK_F17: return "f17";
        // case KeyEvent.VK_F18: return "f18";
        // case KeyEvent.VK_F19: return "f19";
        // case KeyEvent.VK_F20: return "f20";
        // case KeyEvent.VK_F21: return "f21";
        // case KeyEvent.VK_F22: return "f22";
        // case KeyEvent.VK_F23: return "f23";
        // case KeyEvent.VK_F24: return "f24";
      case SWT.PRINT_SCREEN:
        return "printscreen";
      case SWT.INSERT:
        return "insert";
      case SWT.HELP:
        return "help";
        // case SWT.: return "meta";
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
      case '&':
        return "ampersand";
        // case KeyEvent.VK_ASTERISK: return "asterisk";
        // case KeyEvent.VK_QUOTEDBL: return "quotedbl";
      case '<':
        return "less";
      case '>':
        return "greater";
        // case 161: return "braceleft";
        // case ')': return "braceright";
      case '@':
        return "at";
      case KeyEvent.VK_COLON:
        return "colon";
      case '^':
        return "circumflex";
      case '$':
        return "dollar";
      case 128:
        return "euro_sign";
      case '!':
        return "exclamation_mark";
      case 161:
        return "inverted_exclamation_mark";
      case '(':
        return "left_parenthesis";
      case '#':
        return "number_sign";
      case '-':
        return "minus";
      case '+':
        return "plus";
      case ')':
        return "right_parenthesis";
      case '_':
        return "underscore";
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
      case KeyEvent.VK_UNDO:
        return "undo";
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

  @SuppressWarnings("unchecked")
  public static <T> T[] getItemsOfSelection(Class<T> t, StructuredSelection selection) {
    T[] result = (T[]) Array.newInstance(t, selection.size());
    int i = 0;
    for (Object o : selection.toArray()) {
      result[i++] = (T) o;
    }
    return result;

  }

  static {
    SCOUT_SWT_KEY_MAP = new HashMap<String, Integer>();
    SWT_SCOUT_KEY_MAP = new HashMap<Integer, String>();
    SCOUT_SWT_KEY_MAP.put("0", SWT.KEYPAD_0);
    SCOUT_SWT_KEY_MAP.put("1", SWT.KEYPAD_1);
    SCOUT_SWT_KEY_MAP.put("2", SWT.KEYPAD_2);
    SCOUT_SWT_KEY_MAP.put("3", SWT.KEYPAD_3);
    SCOUT_SWT_KEY_MAP.put("4", SWT.KEYPAD_4);
    SCOUT_SWT_KEY_MAP.put("5", SWT.KEYPAD_5);
    SCOUT_SWT_KEY_MAP.put("6", SWT.KEYPAD_6);
    SCOUT_SWT_KEY_MAP.put("7", SWT.KEYPAD_7);
    SCOUT_SWT_KEY_MAP.put("8", SWT.KEYPAD_8);
    SCOUT_SWT_KEY_MAP.put("9", SWT.KEYPAD_9);
    SCOUT_SWT_KEY_MAP.put("enter", (int) SWT.CR);
    SCOUT_SWT_KEY_MAP.put("back_space", (int) SWT.BS);
    SCOUT_SWT_KEY_MAP.put("tab", (int) SWT.TAB);
    // SCOUT_SWT_KEY_MAP.put("cancel", SWT.CANCEL);
    // SCOUT_SWT_KEY_MAP.put("clear", KeyEvent.VK_CLEAR);
    SCOUT_SWT_KEY_MAP.put("shift", SWT.SHIFT);
    SCOUT_SWT_KEY_MAP.put("control", SWT.CONTROL);
    SCOUT_SWT_KEY_MAP.put("alt", SWT.ALT);
    SCOUT_SWT_KEY_MAP.put("alternate", SWT.ALT);
    SCOUT_SWT_KEY_MAP.put("pause", SWT.PAUSE);
    SCOUT_SWT_KEY_MAP.put("caps_lock", SWT.CAPS_LOCK);
    SCOUT_SWT_KEY_MAP.put("escape", (int) SWT.ESC);
    SCOUT_SWT_KEY_MAP.put("space", (int) ' ');
    SCOUT_SWT_KEY_MAP.put("page_up", SWT.PAGE_UP);
    SCOUT_SWT_KEY_MAP.put("page_down", SWT.PAGE_DOWN);
    SCOUT_SWT_KEY_MAP.put("end", SWT.END);
    SCOUT_SWT_KEY_MAP.put("home", SWT.HOME);
    SCOUT_SWT_KEY_MAP.put("left", SWT.ARROW_LEFT);
    SCOUT_SWT_KEY_MAP.put("up", SWT.ARROW_UP);
    SCOUT_SWT_KEY_MAP.put("right", SWT.ARROW_RIGHT);
    SCOUT_SWT_KEY_MAP.put("down", SWT.ARROW_DOWN);
    SCOUT_SWT_KEY_MAP.put("multiply", SWT.KEYPAD_MULTIPLY);
    SCOUT_SWT_KEY_MAP.put("add", SWT.KEYPAD_ADD);
    SCOUT_SWT_KEY_MAP.put("separater", SWT.KEYPAD_CR);
    SCOUT_SWT_KEY_MAP.put("subtract", SWT.KEYPAD_SUBTRACT);
    SCOUT_SWT_KEY_MAP.put("decimal", SWT.KEYPAD_DECIMAL);
    SCOUT_SWT_KEY_MAP.put("divide", SWT.KEYPAD_DIVIDE);
    SCOUT_SWT_KEY_MAP.put("delete", (int) SWT.DEL);
    SCOUT_SWT_KEY_MAP.put("num_lock", SWT.NUM_LOCK);
    SCOUT_SWT_KEY_MAP.put("scroll_lock", SWT.SCROLL_LOCK);
    SCOUT_SWT_KEY_MAP.put("f1", SWT.F1);
    SCOUT_SWT_KEY_MAP.put("f2", SWT.F2);
    SCOUT_SWT_KEY_MAP.put("f3", SWT.F3);
    SCOUT_SWT_KEY_MAP.put("f4", SWT.F4);
    SCOUT_SWT_KEY_MAP.put("f5", SWT.F5);
    SCOUT_SWT_KEY_MAP.put("f6", SWT.F6);
    SCOUT_SWT_KEY_MAP.put("f7", SWT.F7);
    SCOUT_SWT_KEY_MAP.put("f8", SWT.F8);
    SCOUT_SWT_KEY_MAP.put("f9", SWT.F9);
    SCOUT_SWT_KEY_MAP.put("f10", SWT.F10);
    SCOUT_SWT_KEY_MAP.put("f11", SWT.F11);
    SCOUT_SWT_KEY_MAP.put("f12", SWT.F12);
    SCOUT_SWT_KEY_MAP.put("f13", SWT.F13);
    SCOUT_SWT_KEY_MAP.put("f14", SWT.F14);
    SCOUT_SWT_KEY_MAP.put("f15", SWT.F15);
    // SCOUT_SWT_KEY_MAP.put("f16",(int) KeyEvent.VK_F16);
    // SCOUT_SWT_KEY_MAP.put("f17",(int) KeyEvent.VK_F17);
    // SCOUT_SWT_KEY_MAP.put("f18",(int) KeyEvent.VK_F18);
    // SCOUT_SWT_KEY_MAP.put("f19",(int) KeyEvent.VK_F19);
    // SCOUT_SWT_KEY_MAP.put("f20",(int) KeyEvent.VK_F20);
    // SCOUT_SWT_KEY_MAP.put("f21",(int) KeyEvent.VK_F21);
    // SCOUT_SWT_KEY_MAP.put("f22",(int) KeyEvent.VK_F22);
    // SCOUT_SWT_KEY_MAP.put("f23",(int) KeyEvent.VK_F23);
    // SCOUT_SWT_KEY_MAP.put("f24",(int) KeyEvent.VK_F24);
    SCOUT_SWT_KEY_MAP.put("printscreen", SWT.PRINT_SCREEN);
    SCOUT_SWT_KEY_MAP.put("insert", SWT.INSERT);
    SCOUT_SWT_KEY_MAP.put("help", SWT.HELP);
    // SCOUT_SWT_KEY_MAP.put("meta",(int) SWT.);
    // SCOUT_SWT_KEY_MAP.put("back_quote",(int) KeyEvent.VK_BACK_QUOTE);
    // SCOUT_SWT_KEY_MAP.put("quote",(int) KeyEvent.VK_QUOTE);
    // SCOUT_SWT_KEY_MAP.put("kp_up",(int) KeyEvent.VK_KP_UP);
    // SCOUT_SWT_KEY_MAP.put("kp_down",(int) KeyEvent.VK_KP_DOWN);
    // SCOUT_SWT_KEY_MAP.put("kp_left",(int) KeyEvent.VK_KP_LEFT);
    // SCOUT_SWT_KEY_MAP.put("kp_right",(int) KeyEvent.VK_KP_RIGHT);
    // SCOUT_SWT_KEY_MAP.put("dead_grave",(int) KeyEvent.VK_DEAD_GRAVE);
    // SCOUT_SWT_KEY_MAP.put("dead_acute",(int) KeyEvent.VK_DEAD_ACUTE);
    // SCOUT_SWT_KEY_MAP.put("dead_circumflex",(int)
    // KeyEvent.VK_DEAD_CIRCUMFLEX);
    // SCOUT_SWT_KEY_MAP.put("dead_tilde",(int) KeyEvent.VK_DEAD_TILDE);
    // SCOUT_SWT_KEY_MAP.put("dead_macron",(int) KeyEvent.VK_DEAD_MACRON);
    // SCOUT_SWT_KEY_MAP.put("dead_breve",(int) KeyEvent.VK_DEAD_BREVE);
    // SCOUT_SWT_KEY_MAP.put("dead_abovedot",(int)
    // KeyEvent.VK_DEAD_ABOVEDOT);
    // SCOUT_SWT_KEY_MAP.put("dead_diaeresis",(int)
    // KeyEvent.VK_DEAD_DIAERESIS);
    // SCOUT_SWT_KEY_MAP.put("dead_abovering",(int)
    // KeyEvent.VK_DEAD_ABOVERING);
    // SCOUT_SWT_KEY_MAP.put("dead_doubleacute",(int)
    // KeyEvent.VK_DEAD_DOUBLEACUTE);
    // SCOUT_SWT_KEY_MAP.put("dead_caron",(int) KeyEvent.VK_DEAD_CARON);
    // SCOUT_SWT_KEY_MAP.put("dead_cedilla",(int)
    // KeyEvent.VK_DEAD_CEDILLA);
    // SCOUT_SWT_KEY_MAP.put("dead_ogonek",(int) KeyEvent.VK_DEAD_OGONEK);
    // SCOUT_SWT_KEY_MAP.put("dead_iota",(int) KeyEvent.VK_DEAD_IOTA);
    // SCOUT_SWT_KEY_MAP.put("dead_voiced_sound",(int)
    // KeyEvent.VK_DEAD_VOICED_SOUND);
    // SCOUT_SWT_KEY_MAP.put("dead_semivoiced_sound",(int)
    // KeyEvent.VK_DEAD_SEMIVOICED_SOUND);
    SCOUT_SWT_KEY_MAP.put("ampersand", (int) '&');
    // SCOUT_SWT_KEY_MAP.put("asterisk",(int) KeyEvent.VK_ASTERISK);
    // SCOUT_SWT_KEY_MAP.put("quotedbl",(int) KeyEvent.VK_QUOTEDBL);
    SCOUT_SWT_KEY_MAP.put("less", (int) '<');
    SCOUT_SWT_KEY_MAP.put("greater", (int) '>');
    // SCOUT_SWT_KEY_MAP.put("braceleft",(int) 161);
    // SCOUT_SWT_KEY_MAP.put("braceright",(int) ')');
    SCOUT_SWT_KEY_MAP.put("at", (int) '@');
    SCOUT_SWT_KEY_MAP.put("colon", KeyEvent.VK_COLON);
    SCOUT_SWT_KEY_MAP.put("circumflex", (int) '^');
    SCOUT_SWT_KEY_MAP.put("dollar", (int) '$');
    SCOUT_SWT_KEY_MAP.put("euro_sign", 128);
    SCOUT_SWT_KEY_MAP.put("exclamation_mark", (int) '!');
    SCOUT_SWT_KEY_MAP.put("inverted_exclamation_mark", 161);
    SCOUT_SWT_KEY_MAP.put("left_parenthesis", (int) '(');
    SCOUT_SWT_KEY_MAP.put("number_sign", (int) '#');
    SCOUT_SWT_KEY_MAP.put("minus", (int) '-');
    SCOUT_SWT_KEY_MAP.put("plus", (int) '+');
    SCOUT_SWT_KEY_MAP.put("right_parenthesis", (int) ')');
    SCOUT_SWT_KEY_MAP.put("underscore", (int) '_');
    // SCOUT_SWT_KEY_MAP.put("final",(int) KeyEvent.VK_FINAL);
    // SCOUT_SWT_KEY_MAP.put("convert",(int) KeyEvent.VK_CONVERT);
    // SCOUT_SWT_KEY_MAP.put("nonconvert",(int) KeyEvent.VK_NONCONVERT);
    // SCOUT_SWT_KEY_MAP.put("accept",(int) KeyEvent.VK_ACCEPT);
    // SCOUT_SWT_KEY_MAP.put("modechange",(int) KeyEvent.VK_MODECHANGE);
    // SCOUT_SWT_KEY_MAP.put("kana",(int) KeyEvent.VK_KANA);
    // SCOUT_SWT_KEY_MAP.put("kanji",(int) KeyEvent.VK_KANJI);
    // SCOUT_SWT_KEY_MAP.put("alphanumeric",(int)
    // KeyEvent.VK_ALPHANUMERIC);
    // SCOUT_SWT_KEY_MAP.put("katakana",(int) KeyEvent.VK_KATAKANA);
    // SCOUT_SWT_KEY_MAP.put("hiragana",(int) KeyEvent.VK_HIRAGANA);
    // SCOUT_SWT_KEY_MAP.put("full_width",(int) KeyEvent.VK_FULL_WIDTH);
    // SCOUT_SWT_KEY_MAP.put("half_width",(int) KeyEvent.VK_HALF_WIDTH);
    // SCOUT_SWT_KEY_MAP.put("roman_characters",(int)
    // KeyEvent.VK_ROMAN_CHARACTERS);
    // SCOUT_SWT_KEY_MAP.put("all_candidates",(int)
    // KeyEvent.VK_ALL_CANDIDATES);
    // SCOUT_SWT_KEY_MAP.put("previous_candidate",(int)
    // KeyEvent.VK_PREVIOUS_CANDIDATE);
    // SCOUT_SWT_KEY_MAP.put("code_input",(int) KeyEvent.VK_CODE_INPUT);
    // SCOUT_SWT_KEY_MAP.put("japanese_katakana",(int)
    // KeyEvent.VK_JAPANESE_KATAKANA);
    // SCOUT_SWT_KEY_MAP.put("japanese_hiragana",(int)
    // KeyEvent.VK_JAPANESE_HIRAGANA);
    // SCOUT_SWT_KEY_MAP.put("japanese_roman",(int)
    // KeyEvent.VK_JAPANESE_ROMAN);
    // SCOUT_SWT_KEY_MAP.put("kana_lock",(int) KeyEvent.VK_KANA_LOCK);
    // SCOUT_SWT_KEY_MAP.put("input_method_on_off",(int)
    // KeyEvent.VK_INPUT_METHOD_ON_OFF);
    // SCOUT_SWT_KEY_MAP.put("again",(int) KeyEvent.VK_AGAIN);
    SCOUT_SWT_KEY_MAP.put("undo", KeyEvent.VK_UNDO);
    // SCOUT_SWT_KEY_MAP.put("copy",(int) );
    // SCOUT_SWT_KEY_MAP.put("paste",(int) KeyEvent.VK_PASTE);
    // SCOUT_SWT_KEY_MAP.put("cut",(int) KeyEvent.VK_CUT);
    // SCOUT_SWT_KEY_MAP.put("find",(int) KeyEvent.VK_FIND);
    // SCOUT_SWT_KEY_MAP.put("props",(int) KeyEvent.VK_PROPS);
    // SCOUT_SWT_KEY_MAP.put("stop",(int) KeyEvent.VK_STOP);
    // SCOUT_SWT_KEY_MAP.put("compose",(int) KeyEvent.VK_COMPOSE);
    // SCOUT_SWT_KEY_MAP.put("alt_graph",(int) KeyEvent.VK_ALT_GRAPH);

    // SWT -> Scout
    SWT_SCOUT_KEY_MAP.put((int) SWT.ESC, "");

    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_0, "0");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_1, "1");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_2, "2");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_3, "3");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_4, "4");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_5, "5");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_6, "6");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_7, "7");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_8, "8");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_9, "9");
    SWT_SCOUT_KEY_MAP.put((int) SWT.CR, "enter");
    SWT_SCOUT_KEY_MAP.put((int) SWT.BS, "back_space");
    SWT_SCOUT_KEY_MAP.put((int) SWT.TAB, "tab");
    // SWT_SCOUT_KEY_MAP.put( SWT.CANCEL, "cancel");
    // SWT_SCOUT_KEY_MAP.put( KeyEvent.VK_CLEAR, "clear");
    SWT_SCOUT_KEY_MAP.put(SWT.SHIFT, "shift");
    SWT_SCOUT_KEY_MAP.put(SWT.CONTROL, "control");
    SWT_SCOUT_KEY_MAP.put(SWT.ALT, "alt");
    SWT_SCOUT_KEY_MAP.put(SWT.PAUSE, "pause");
    SWT_SCOUT_KEY_MAP.put(SWT.CAPS_LOCK, "caps_lock");
    SWT_SCOUT_KEY_MAP.put((int) SWT.ESC, "escape");
    SWT_SCOUT_KEY_MAP.put((int) ' ', "space");
    SWT_SCOUT_KEY_MAP.put(SWT.PAGE_UP, "page_up");
    SWT_SCOUT_KEY_MAP.put(SWT.PAGE_DOWN, "page_down");
    SWT_SCOUT_KEY_MAP.put(SWT.END, "end");
    SWT_SCOUT_KEY_MAP.put(SWT.HOME, "home");
    SWT_SCOUT_KEY_MAP.put(SWT.ARROW_LEFT, "left");
    SWT_SCOUT_KEY_MAP.put(SWT.ARROW_UP, "up");
    SWT_SCOUT_KEY_MAP.put(SWT.ARROW_RIGHT, "right");
    SWT_SCOUT_KEY_MAP.put(SWT.ARROW_DOWN, "down");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_MULTIPLY, "multiply");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_ADD, "add");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_CR, "separater");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_SUBTRACT, "subtract");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_DECIMAL, "decimal");
    SWT_SCOUT_KEY_MAP.put(SWT.KEYPAD_DIVIDE, "divide");
    SWT_SCOUT_KEY_MAP.put((int) SWT.DEL, "delete");
    SWT_SCOUT_KEY_MAP.put(SWT.NUM_LOCK, "num_lock");
    SWT_SCOUT_KEY_MAP.put(SWT.SCROLL_LOCK, "scroll_lock");
    SWT_SCOUT_KEY_MAP.put(SWT.F1, "f1");
    SWT_SCOUT_KEY_MAP.put(SWT.F2, "f2");
    SWT_SCOUT_KEY_MAP.put(SWT.F3, "f3");
    SWT_SCOUT_KEY_MAP.put(SWT.F4, "f4");
    SWT_SCOUT_KEY_MAP.put(SWT.F5, "f5");
    SWT_SCOUT_KEY_MAP.put(SWT.F6, "f6");
    SWT_SCOUT_KEY_MAP.put(SWT.F7, "f7");
    SWT_SCOUT_KEY_MAP.put(SWT.F8, "f8");
    SWT_SCOUT_KEY_MAP.put(SWT.F9, "f9");
    SWT_SCOUT_KEY_MAP.put(SWT.F10, "f10");
    SWT_SCOUT_KEY_MAP.put(SWT.F11, "f11");
    SWT_SCOUT_KEY_MAP.put(SWT.F12, "f12");
    SWT_SCOUT_KEY_MAP.put(SWT.F13, "f13");
    SWT_SCOUT_KEY_MAP.put(SWT.F14, "f14");
    SWT_SCOUT_KEY_MAP.put(SWT.F15, "f15");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F16, "f16");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F17, "f17");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F18, "f18");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F19, "f19");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F20, "f20");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F21, "f21");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F22, "f22");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F23, "f23");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_F24, "f24");
    SWT_SCOUT_KEY_MAP.put(SWT.PRINT_SCREEN, "printscreen");
    SWT_SCOUT_KEY_MAP.put(SWT.INSERT, "insert");
    SWT_SCOUT_KEY_MAP.put(SWT.HELP, "help");
    // SWT_SCOUT_KEY_MAP.put((int) SWT., "meta");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_BACK_QUOTE, "back_quote");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_QUOTE, "quote");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KP_UP, "kp_up");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KP_DOWN, "kp_down");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KP_LEFT, "kp_left");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KP_RIGHT, "kp_right");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_GRAVE, "dead_grave");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_ACUTE, "dead_acute");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_CIRCUMFLEX, "dead_circumflex");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_TILDE, "dead_tilde");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_MACRON, "dead_macron");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_BREVE, "dead_breve");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_ABOVEDOT, "dead_abovedot");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_DIAERESIS, "dead_diaeresis");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_ABOVERING, "dead_abovering");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_DOUBLEACUTE, "dead_doubleacute");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_CARON, "dead_caron");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_CEDILLA, "dead_cedilla");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_OGONEK, "dead_ogonek");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_DEAD_IOTA, "dead_iota");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_VOICED_SOUND, "dead_voiced_sound");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_DEAD_SEMIVOICED_SOUND, "dead_semivoiced_sound");
    SWT_SCOUT_KEY_MAP.put((int) '&', "ampersand");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_ASTERISK, "asterisk");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_QUOTEDBL, "quotedbl");
    SWT_SCOUT_KEY_MAP.put((int) '<', "less");
    SWT_SCOUT_KEY_MAP.put((int) '>', "greater");
    // SWT_SCOUT_KEY_MAP.put((int) 161, "braceleft");
    // SCOUT_SWT_KEY_MAP.put("braceright",(int) ')');
    SWT_SCOUT_KEY_MAP.put((int) '@', "at");
    SWT_SCOUT_KEY_MAP.put(KeyEvent.VK_COLON, "colon");
    SWT_SCOUT_KEY_MAP.put((int) '^', "circumflex");
    SWT_SCOUT_KEY_MAP.put((int) '$', "dollar");
    SWT_SCOUT_KEY_MAP.put(128, "euro_sign");
    SWT_SCOUT_KEY_MAP.put((int) '!', "exclamation_mark");
    SWT_SCOUT_KEY_MAP.put(161, "inverted_exclamation_mark");
    SWT_SCOUT_KEY_MAP.put((int) '(', "left_parenthesis");
    SWT_SCOUT_KEY_MAP.put((int) '#', "number_sign");
    SWT_SCOUT_KEY_MAP.put((int) '-', "minus");
    SWT_SCOUT_KEY_MAP.put((int) '+', "plus");
    SCOUT_SWT_KEY_MAP.put("right_parenthesis", (int) ')');
    SWT_SCOUT_KEY_MAP.put((int) '_', "underscore");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_FINAL, "final");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_CONVERT, "convert");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_NONCONVERT, "nonconvert");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_ACCEPT, "accept");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_MODECHANGE, "modechange");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KANA, "kana");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KANJI, "kanji");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_ALPHANUMERIC, "alphanumeric");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KATAKANA, "katakana");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_HIRAGANA, "hiragana");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_FULL_WIDTH, "full_width");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_HALF_WIDTH, "half_width");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_ROMAN_CHARACTERS, "roman_characters");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_ALL_CANDIDATES, "all_candidates");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_PREVIOUS_CANDIDATE, "previous_candidate");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_CODE_INPUT, "code_input");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_JAPANESE_KATAKANA, "japanese_katakana");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_JAPANESE_HIRAGANA, "japanese_hiragana");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_JAPANESE_ROMAN, "japanese_roman");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_KANA_LOCK, "kana_lock");
    // SWT_SCOUT_KEY_MAP.put((int)
    // KeyEvent.VK_INPUT_METHOD_ON_OFF, "input_method_on_off");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_AGAIN, "again");
    SWT_SCOUT_KEY_MAP.put(KeyEvent.VK_UNDO, "undo");
    // SWT_SCOUT_KEY_MAP.put((int) , "copy");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_PASTE, "paste");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_CUT, "cut");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_FIND, "find");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_PROPS, "props");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_STOP, "stop");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_COMPOSE, "compose");
    // SWT_SCOUT_KEY_MAP.put((int) KeyEvent.VK_ALT_GRAPH, "alt_graph");
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
          break;
        case SWT.PAGE_UP:
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

}
