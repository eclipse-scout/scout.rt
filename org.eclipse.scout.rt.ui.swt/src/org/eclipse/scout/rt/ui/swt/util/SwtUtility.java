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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.dnd.FileListTransferObject;
import org.eclipse.scout.commons.dnd.ImageTransferObject;
import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.basic.ISwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.basic.IInputVerifiable;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.keystroke.SwtScoutKeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public final class SwtUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtUtility.class);
  private static final Map<String, Integer> SCOUT_SWT_KEY_MAP;

  private SwtUtility() {
  }

  public static SwtTransferObject[] createSwtTransferables(TransferObject transferObject) {
    List<SwtTransferObject> swtTranferObjects = new ArrayList<SwtTransferObject>();
    if (transferObject instanceof FileListTransferObject) {
      FileListTransferObject scoutTransferObject = ((FileListTransferObject) transferObject);
      swtTranferObjects.add(new SwtTransferObject(FileTransfer.getInstance(), scoutTransferObject.getFilenames()));
    }
    else if (transferObject instanceof TextTransferObject) {
      TextTransferObject scoutTransferObject = ((TextTransferObject) transferObject);
      // text/plain
      swtTranferObjects.add(new SwtTransferObject(TextTransfer.getInstance(), scoutTransferObject.getPlainText()));
      // text/html
      if (StringUtility.hasText(scoutTransferObject.getHtmlText())) {
        swtTranferObjects.add(new SwtTransferObject(HTMLTransfer.getInstance(), scoutTransferObject.getHtmlText()));
      }
    }
    else if (transferObject instanceof JavaTransferObject) {
      JavaTransferObject scoutTransferObject = ((JavaTransferObject) transferObject);
      swtTranferObjects.add(new SwtTransferObject(JVMLocalObjectTransfer.getInstance(), scoutTransferObject.getLocalObject()));
    }
    else if (transferObject instanceof ImageTransferObject) {
      ImageTransferObject scoutTransferObject = ((ImageTransferObject) transferObject);

      Object image = scoutTransferObject.getImage();
      if (image instanceof ImageData) {
        swtTranferObjects.add(new SwtTransferObject(ImageTransfer.getInstance(), (ImageData) image));
      }
      else if (image instanceof byte[]) {
        ByteArrayInputStream imageInput = new ByteArrayInputStream((byte[]) image);
        ImageData imageData = new Image(null, imageInput).getImageData();
        swtTranferObjects.add(new SwtTransferObject(ImageTransfer.getInstance(), imageData));
      }
    }
    else {
      LOG.error("unsupported transfer object type: " + transferObject);
    }
    return swtTranferObjects.toArray(new SwtTransferObject[swtTranferObjects.size()]);
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

  public static MouseButton swtToScoutMouseButton(int swtButton) {
    switch (swtButton) {
      case 1:
        return MouseButton.Left;
      case 3:
        return MouseButton.Right;
      default:
        return MouseButton.Unknown;
    }
  }

  public static ISwtKeyStroke[] getKeyStrokes(IKeyStroke stroke, ISwtEnvironment environment) {
    ArrayList<ISwtKeyStroke> swtKeyStrokes = new ArrayList<ISwtKeyStroke>();
    List<Integer> keyCodes = getSwtKeyCodes(stroke);
    int stateMask = getSwtStateMask(stroke);

    for (Integer keyCode : keyCodes) {
      swtKeyStrokes.add(new SwtScoutKeyStroke(stroke, keyCode, stateMask, environment));
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
    List<Integer> swtKeyCodes = getSwtKeyCodes(keyStroke);
    if (swtKeyCodes.isEmpty()) {
      return SWT.NONE;
    }

    return swtKeyCodes.get(0);
  }

  public static List<Integer> getSwtKeyCodes(IKeyStroke keyStroke) {
    String[] keys = keyStroke.getKeyStroke().split("-");
    List<Integer> swtKeyCodes = new LinkedList<Integer>();

    if (keys.length > 0) {
      swtKeyCodes = scoutToSwtKeys(keys[keys.length - 1]);
    }

    return swtKeyCodes;
  }

  public static int scoutToSwtKey(String scoutKey) {
    List<Integer> swtKeyCodes = scoutToSwtKeys(scoutKey);
    if (swtKeyCodes.isEmpty()) {
      return SWT.NONE;
    }

    return swtKeyCodes.get(0);
  }

  public static List<Integer> scoutToSwtKeys(String scoutKey) {
    List<Integer> swtKeyCodes = new LinkedList<Integer>();

    //If it's a character or a number add its unicode value
    if (scoutKey.length() == 1) {
      swtKeyCodes.add(Integer.valueOf(scoutKey.charAt(0)));
    }

    //Check if there is a mapping defined for this scoutKey.
    //If yes get the mapped swtKey and add it to the list.
    Integer mappedKeyCode = SCOUT_SWT_KEY_MAP.get(scoutKey);
    if (mappedKeyCode == null) {
      if (swtKeyCodes.isEmpty()) {
        LOG.warn("no key mapping for: " + scoutKey);
      }
    }
    else {
      swtKeyCodes.add(mappedKeyCode);

      // in case of enter register keypad enter as well
      if (mappedKeyCode == SWT.CR) {
        swtKeyCodes.add(SWT.KEYPAD_CR);
      }
    }

    return swtKeyCodes;
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> getItemsOfSelection(Class<T> t, StructuredSelection selection) {
    if (selection != null) {
      List<T> result = new ArrayList<T>(selection.size());
      Iterator it = selection.iterator();
      while (it.hasNext()) {
        result.add((T) it.next());
      }
      return result;
    }
    return CollectionUtility.emptyArrayList();
  }

  static {
    SCOUT_SWT_KEY_MAP = new HashMap<String, Integer>();
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
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.ENTER, (int) SWT.CR);
    SCOUT_SWT_KEY_MAP.put("back_space", (int) SWT.BS);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.TAB, (int) SWT.TAB);
    // SCOUT_SWT_KEY_MAP.put("cancel", SWT.CANCEL);
    // SCOUT_SWT_KEY_MAP.put("clear", KeyEvent.VK_CLEAR);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.SHIFT, SWT.SHIFT);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.CONTROL, SWT.CONTROL);
    SCOUT_SWT_KEY_MAP.put("ctrl", SWT.CONTROL);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.ALT, SWT.ALT);
    SCOUT_SWT_KEY_MAP.put("alternate", SWT.ALT);

    SCOUT_SWT_KEY_MAP.put("pause", SWT.PAUSE);
    SCOUT_SWT_KEY_MAP.put("caps_lock", SWT.CAPS_LOCK);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.ESCAPE, (int) SWT.ESC);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.SPACE, (int) ' ');
    SCOUT_SWT_KEY_MAP.put("page_up", SWT.PAGE_UP);
    SCOUT_SWT_KEY_MAP.put("page_down", SWT.PAGE_DOWN);
    SCOUT_SWT_KEY_MAP.put("end", SWT.END);
    SCOUT_SWT_KEY_MAP.put("home", SWT.HOME);

    SCOUT_SWT_KEY_MAP.put(IKeyStroke.LEFT, SWT.ARROW_LEFT);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.UP, SWT.ARROW_UP);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.RIGHT, SWT.ARROW_RIGHT);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.DOWN, SWT.ARROW_DOWN);

    SCOUT_SWT_KEY_MAP.put("multiply", SWT.KEYPAD_MULTIPLY);
    SCOUT_SWT_KEY_MAP.put("add", SWT.KEYPAD_ADD);
    SCOUT_SWT_KEY_MAP.put("separater", SWT.KEYPAD_CR);
    SCOUT_SWT_KEY_MAP.put("subtract", SWT.KEYPAD_SUBTRACT);
    SCOUT_SWT_KEY_MAP.put("decimal", SWT.KEYPAD_DECIMAL);
    SCOUT_SWT_KEY_MAP.put("divide", SWT.KEYPAD_DIVIDE);
    SCOUT_SWT_KEY_MAP.put("delete", (int) SWT.DEL);
    SCOUT_SWT_KEY_MAP.put("num_lock", SWT.NUM_LOCK);
    SCOUT_SWT_KEY_MAP.put("scroll_lock", SWT.SCROLL_LOCK);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F1, SWT.F1);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F2, SWT.F2);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F3, SWT.F3);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F4, SWT.F4);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F5, SWT.F5);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F6, SWT.F6);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F7, SWT.F7);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F8, SWT.F8);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F9, SWT.F9);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F10, SWT.F10);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F11, SWT.F11);
    SCOUT_SWT_KEY_MAP.put(IKeyStroke.F12, SWT.F12);
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
  }

  public static Map<String, Integer> getScoutSwtKeyMap() {
    return SCOUT_SWT_KEY_MAP;
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
   * @param modalities
   *          combination of {@link SWT#SYSTEM_MODAL}, {@link SWT#APPLICATION_MODAL}, {@link SWT#MODELESS}
   * @return best effort to get the "current" parent shell
   *         <p>
   *         Never null
   */
  public static Shell getParentShellIgnoringPopups(Display display, int modalities) {
    Shell shell = display.getActiveShell();
    if (shell == null) {
      if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
        shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      }
    }
    if (shell != null) {
      while (SwtUtility.isPopupShell(shell) && shell.getParent() instanceof Shell) {
        shell = (Shell) shell.getParent();
      }
    }
    // traverse complete tree
    if (shell == null) {
      TreeMap<CompositeLong, Shell> map = new TreeMap<CompositeLong, Shell>();
      for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
        visitShellTreeRec(w.getShell(), modalities, 0, map);
      }
      if (map.size() > 0) {
        shell = map.get(map.firstKey());
      }
    }
    if (shell != null && shell.getData() instanceof ProgressMonitorDialog) {
      // do also ignore the ProgressMonitorDialog, otherwise there will be some strange behaviors
      // when displaying a shell on top of the ProgressMonitorDialog-shell (f.e. when the
      // ProgressMonitorDialog-shell disappears)
      shell = (Shell) shell.getParent();
    }
    return shell;
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
  private static void visitShellTreeRec(Shell shell, int modalities, int level, TreeMap<CompositeLong, Shell> out) {
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
    return SwtUtility.getKeyStrokePrettyPrinted(scoutAction.getKeyStroke());
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
    int stateMask = getSwtStateMask(ks);
    int keyCode = getSwtKeyCode(ks);
    return LegacyActionTools.convertAccelerator(stateMask | keyCode);
  }

  /**
   * Run the inputVerifier on the currently focused control. See {@link #runSwtInputVerifier(Control)} for more details.
   *
   * @since 3.10.0-M5
   */
  public static boolean runSwtInputVerifier() {
    return runSwtInputVerifier(Display.getDefault().getFocusControl());
  }

  /**
   * Force the control's inputVerifier to run
   *
   * @since 3.10.0-M5
   */
  public static boolean runSwtInputVerifier(Control control) {
    if (control == null || control.isDisposed()) {
      return true; //continue, a tray menu can be selected for example
    }

    ISwtScoutComposite compositeOnWidget = SwtScoutComposite.getCompositeOnWidget(control);
    if (compositeOnWidget instanceof IInputVerifiable) {
      return ((IInputVerifiable) compositeOnWidget).runSwtInputVerifier();
    }
    return true; //continue always
  }
}
