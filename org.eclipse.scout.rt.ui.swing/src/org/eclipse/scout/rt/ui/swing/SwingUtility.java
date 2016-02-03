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
package org.eclipse.scout.rt.ui.swing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.BundleContextUtility;
import org.eclipse.scout.commons.CompareUtility;
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
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.BoundsSpec;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;
import org.eclipse.scout.rt.ui.swing.dnd.AwtImageTransferable;
import org.eclipse.scout.rt.ui.swing.dnd.FileListTransferable;
import org.eclipse.scout.rt.ui.swing.dnd.JVMLocalObjectTransferable;
import org.eclipse.scout.rt.ui.swing.dnd.TextTransferable;
import org.eclipse.scout.rt.ui.swing.form.fields.htmlfield.SwingScoutHtmlField;
import org.eclipse.scout.rt.ui.swing.form.fields.labelfield.SwingScoutLabelField;
import org.eclipse.scout.rt.ui.swing.simulator.SimulatorAction;
import org.eclipse.scout.rt.ui.swing.simulator.SwingScoutSimulator;

public final class SwingUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingUtility.class);

  public static final boolean IS_JAVA_7_OR_GREATER = CompareUtility.compareTo(System.getProperty("java.version"), "1.7") >= 0;
  public static final boolean IS_JAVA_7_OR_LESS = CompareUtility.compareTo(System.getProperty("java.version"), "1.7") <= 0;
  public static final boolean DO_RESET_COMPONENT_BOUNDS = BundleContextUtility.parseBooleanProperty("scout.ui.layout.resetBoundsOnInvalidate", true);
  public static final boolean VERIFY_INPUT_ON_WINDOW_CLOSED = BundleContextUtility.parseBooleanProperty("scout.ui.verifyInputOnWindowClosed", false);
  public static final boolean VALIDATE_HTML_CAPABLE = BundleContextUtility.parseBooleanProperty("org.eclipse.scout.rt.ui.swing.validate.htmlCapable", true);

  private static Integer topMarginForField = null;

  private SwingUtility() {
  }

  public static boolean isSynth() {
    return "SynthLookAndFeel".equals(UIManager.getLookAndFeel().getClass().getSimpleName());
  }

  public static boolean isPasteAction() {
    AWTEvent e = EventQueue.getCurrentEvent();
    if (e != null && e.getID() == KeyEvent.KEY_PRESSED) {
      KeyEvent k = (KeyEvent) e;
      if (k.getKeyCode() == KeyEvent.VK_V && k.getModifiers() == KeyEvent.CTRL_MASK) {
        return true;
      }
    }
    return false;
  }

  public static boolean isSunDropAction() {
    AWTEvent e = EventQueue.getCurrentEvent();
    if (e != null && e.getID() == MouseEvent.MOUSE_RELEASED && "sun.awt.dnd.SunDropTargetEvent".equals(e.getClass().getName())) {
      return true;
    }
    return false;
  }

  public static Window getOwnerForChildWindow() {
    Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
    if (w != null) {
      return w;
    }
    w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    if (w != null) {
      return w;
    }
    /*
     * Priority level1
     * modal dialog: +200
     * non-modal dialog: +100
     * frame: +0
     *
     * Priority level2
     * no owned windows: +10
     */
    TreeMap<Integer, Window> prioMap = new TreeMap<Integer, Window>();
    for (Window cand : Window.getWindows()) {
      if (cand == null) {
        continue;
      }
      if (!cand.isVisible()) {
        continue;
      }
      if (!cand.isShowing()) {
        continue;
      }
      int prio = 0;
      Window[] children = cand.getOwnedWindows();
      if (children == null || children.length == 0) {
        prio += 10;
      }
      if (cand instanceof Dialog) {
        Dialog dlg = (Dialog) cand;
        if (dlg.isModal()) {
          prio += 200;
        }
        else {
          prio += 100;
        }
        prioMap.put(prio, cand);
      }
      else if (cand instanceof Frame) {
        if (!prioMap.containsKey(prio)) {
          prioMap.put(prio, cand);
        }
      }
    }
    if (prioMap.size() > 0) {
      return prioMap.get(prioMap.lastKey());
    }
    //last line of defense
    if (prioMap.size() == 0) {
      for (Window cand : Window.getWindows()) {
        if (cand == null) {
          continue;
        }
        if (cand.isVisible()) {
          return cand;
        }
      }
    }
    return null;
  }

  /**
   * JOption panes static methods are not swing-conform and not decoratable.
   * <p>
   * This utility function corrects this by giving the dialog/rootPane the name "Synth.Dialog"
   */
  public static void showMessageDialogSynthCapable(Component parentComponent, Object message, String title, int messageType) {
    JOptionPane pane = new JOptionPane(message, messageType);
    JDialog dlg = pane.createDialog(parentComponent, title);
    dlg.getRootPane().setName("Synth.Dialog");
    dlg.pack();
    dlg.setVisible(true);
  }

  public static MouseButton swingToScoutMouseButton(int swingButton) {
    switch (swingButton) {
      case MouseEvent.BUTTON1:
        return MouseButton.Left;
      case MouseEvent.BUTTON3:
        return MouseButton.Right;
      default:
        return MouseButton.Unknown;
    }
  }

  /**
   * Keystroke to lowercase String Accept valid key strokes such as ctrl-s or
   * shift-ctrl-s, but don't accept alt, alt-strg, ...
   */
  public static String getKeyStrokeText(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_CONTROL:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_ALT_GRAPH: {
        // ignore
        break;
      }
      default: {
        StringBuffer buf = new StringBuffer();
        if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
          buf.append("shift-");
        }
        if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
          buf.append("ctrl-");
        }
        if ((e.getModifiers() & (KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK)) != 0) {
          buf.append("alt-");
        }
        buf.append(getKeyText(e).toLowerCase());
        return buf.toString();
      }
    }
    return null;
  }

  /**
   * Key to lowercase String, extracts the effective key that was pressed
   * (without shift, control, alt)
   */
  public static String getKeyText(KeyEvent e) {
    // special cases
    if (e.getKeyCode() == KeyEvent.VK_DELETE) {
      return "delete";
    }
    // prio 1: get text of unresolved code (shift-1 --> '1')
    String s = "" + e.getKeyChar();
    if (e.getKeyCode() > 0) {
      int flags = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
      for (Field f : KeyEvent.class.getFields()) {
        if ((f.getModifiers() & flags) == flags) {
          try {
            if (f.getName().startsWith("VK_") && ((Integer) f.get(null)) == e.getKeyCode()) {
              s = f.getName().substring(3).toLowerCase();
              break;
            }
          }
          catch (Throwable t) {
            // nop
          }
        }
      }
    }
    if (s.length() != 1) {
      // prio 2: check if the resolved char is valid (shift-1 --> '+')
      if (e.getKeyChar() >= 32 && e.getKeyChar() < 128) {
        s = "" + e.getKeyChar();
      }
    }
    return s.toLowerCase();
  }

  private static ScoutTexts globalTextProvider;

  /**
   * set the text provider for global swing texts
   */
  public static void setNlsTexts(ScoutTexts textProvider) {
    globalTextProvider = textProvider;
  }

  /**
   * @return the session scope specific text (maybe an override of the ScoutTexts text)
   */
  public static String getNlsText(String key, String... messageArguments) {
    if (globalTextProvider != null) {
      return globalTextProvider.getText(key, messageArguments);
    }
    return ScoutTexts.get(key, messageArguments);
  }

  public static String inspectUIResourceType(Object scoutUIResource) {
    if (scoutUIResource == null) {
      return "String";
    }
    else if (scoutUIResource instanceof Boolean) {
      return "Boolean";
    }
    else if (scoutUIResource instanceof Integer) {
      return "Integer";
    }
    else if (scoutUIResource instanceof String) {
      return "String";
    }
    else if (scoutUIResource instanceof Color) {
      return "Color";
    }
    else if (scoutUIResource instanceof Dimension) {
      return "Dimension";
    }
    else if (scoutUIResource instanceof Font) {
      return "Font";
    }
    else if (scoutUIResource instanceof Insets) {
      return "Insets";
    }
    else if (scoutUIResource instanceof Icon) {
      return "Icon";
    }
    else {
      return "String";
    }
  }

  public static Rectangle createRectangle(BoundsSpec r) {
    if (r == null) {
      return null;
    }
    else {
      return new Rectangle(r.x, r.y, r.width, r.height);
    }
  }

  /**
   * @deprecated Use {@link ColorUtility#createColor(String)} instead. Will be removed in the 5.0 Release.
   */
  @Deprecated
  public static Color createColor(String c) {
    return ColorUtility.createColor(c);
  }

  public static Font createFont(FontSpec scoutFont) {
    return createFont(scoutFont, null);
  }

  public static Font createFont(FontSpec scoutFont, Font templateFont) {
    if (scoutFont == null) {
      return null;
    }
    else {
      String name = scoutFont.getName();
      if (name == null) {
        if (templateFont != null) {
          name = templateFont.getName();
        }
        else {
          name = "Dialog";
        }
      }
      int style = Font.PLAIN;
      if (scoutFont.isBold()) {
        style = style | Font.BOLD;
      }
      if (scoutFont.isItalic()) {
        style = style | Font.ITALIC;
      }
      int size = scoutFont.getSize();
      if (size <= 0) {
        if (templateFont != null) {
          size = templateFont.getSize();
        }
        else {
          size = 11;
        }
      }
      return new Font(name, style, size);
    }
  }

  /**
   * Convenience for {@link #createKeystroke(String)} with key.getKeyStroke()
   */
  public static KeyStroke createKeystroke(IKeyStroke key) {
    return createKeystroke(key.getKeyStroke());
  }

  public static KeyStroke createKeystroke(String pattern) {
    String[] a = pattern.split("[- ]");
    String shift = "";
    String ctrl = "";
    String alt = "";
    String kind = "";
    String key = "";
    boolean hasMeta = false;
    for (String s : a) {
      if (IKeyStroke.SHIFT.equalsIgnoreCase(s)) {
        shift = "shift ";
        hasMeta = true;
      }
      else if (IKeyStroke.CONTROL.equalsIgnoreCase(s)) {
        ctrl = "control ";
        hasMeta = true;
      }
      else if ("ctrl".equalsIgnoreCase(s)) {
        ctrl = "control ";
        hasMeta = true;
      }
      else if ("alternate".equalsIgnoreCase(s)) {
        alt = "alt ";
        hasMeta = true;
      }
      else if (IKeyStroke.ALT.equalsIgnoreCase(s)) {
        alt = "alt ";
        hasMeta = true;
      }
      else {
        if (IKeyStroke.TAB.equalsIgnoreCase(s)) {
          kind = "pressed ";
          key = s;
        }
        else if ((!hasMeta) && s.length() == 1) {
          kind = "typed ";
          key = s;
        }
        else {
          //ticket 87370: must be pressed, contextMenu key reacts on pressed too. Otherwise both will fire.
          kind = "pressed ";
          key = s.toUpperCase();
        }
      }
    }
    String javaKey = shift + ctrl + alt + kind + key;
    KeyStroke stroke = KeyStroke.getKeyStroke(javaKey);
    if (stroke == null) {
      LOG.warn("could not create swing keystroke for '" + javaKey + "'. No java key found for scout pattern '" + pattern + "'");
    }
    return stroke;
  }

  public static Transferable createSwingTransferable(TransferObject scoutT) {
    if (scoutT == null) {
      return null;
    }
    if (scoutT instanceof FileListTransferObject) {
      return new FileListTransferable(((FileListTransferObject) scoutT).getFiles());
    }
    else if (scoutT instanceof TextTransferObject) {
      TextTransferObject textTransferObject = (TextTransferObject) scoutT;
      return new TextTransferable(textTransferObject.getPlainText(), textTransferObject.getHtmlText());
    }
    else if (scoutT instanceof ImageTransferObject) {
      ImageTransferObject imgTransferObject = (ImageTransferObject) scoutT;
      Image img = null;
      Object image = imgTransferObject.getImage();
      if (image == null) {
        return null;
      }
      else if (image instanceof Image) {
        img = (Image) image;
      }
      else if (image instanceof byte[]) {
        img = Toolkit.getDefaultToolkit().createImage((byte[]) image);
      }
      if (img != null) {
        return new AwtImageTransferable(img);
      }
    }
    else if (scoutT instanceof JavaTransferObject) {
      return new JVMLocalObjectTransferable(((JavaTransferObject) scoutT).getLocalObject());
    }
    return null;
  }

  /**
   * @param scoutTransferTypes
   *          one of {@link IDNDSupport#TYPE_FILE_TRANSFER}, {@link IDNDSupport#TYPE_IMAGE_TRANSFER},
   *          {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER}, {@link IDNDSupport#TYPE_TEXT_TRANSFER}
   * @param flavors
   * @return
   */
  public static boolean isSupportedTransfer(int scoutTransferTypes, DataFlavor[] flavors) {
    if (scoutTransferTypes == 0 || flavors == null) {
      return false;
    }
    // scout
    int binaryScoutTypes = 0;
    if ((scoutTransferTypes & IDNDSupport.TYPE_FILE_TRANSFER) != 0) {
      binaryScoutTypes |= 1;
    }
    if ((scoutTransferTypes & IDNDSupport.TYPE_IMAGE_TRANSFER) != 0) {
      binaryScoutTypes |= 1 << 1;
    }
    if ((scoutTransferTypes & IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER) != 0) {
      binaryScoutTypes |= 1 << 2;
    }
    if ((scoutTransferTypes & IDNDSupport.TYPE_TEXT_TRANSFER) != 0) {
      binaryScoutTypes |= 1 << 3;
    }
    // swing
    int binarySwingTypes = 0;
    for (DataFlavor flavor : flavors) {
      if (flavor.isFlavorJavaFileListType()) {
        binarySwingTypes |= 1;
      }
      else if (flavor.isFlavorTextType()) {
        binarySwingTypes |= 1 << 3;
      }
      else if (flavor.isMimeTypeEqual(DataFlavor.imageFlavor)) {
        binarySwingTypes |= 1 << 1;
      }
      else if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
        binarySwingTypes |= 1 << 2;
      }
    }
    return ((binaryScoutTypes & binarySwingTypes) != 0);
  }

  @SuppressWarnings("unchecked")
  public static TransferObject createScoutTransferable(Transferable swingT) {
    if (swingT == null) {
      return null;
    }
    DataFlavor[] flavors = swingT.getTransferDataFlavors();
    Exception ex = null;
    FileListTransferObject fileTransferObject = null;
    TextTransferObject textTransferObject = null;
    ImageTransferObject imageTransferObject = null;
    TextTransferObject serializedTransferObject = null;
    JavaTransferObject jvmTransferObject = null;
    for (int i = 0; i < flavors.length; i++) {
      if (flavors[i].isFlavorJavaFileListType()) {
        try {
          ArrayList<File> fileList = new ArrayList<File>();
          fileList.addAll((List) swingT.getTransferData(flavors[i]));
          if (fileTransferObject == null) {
            fileTransferObject = new FileListTransferObject(fileList);
          }
        }
        catch (Exception e) {
          if (ex == null) {
            ex = e;
          }
        }
      }
      else if (flavors[i].isMimeTypeEqual(DataFlavor.javaSerializedObjectMimeType)) {
        try {
          if (serializedTransferObject == null) {
            serializedTransferObject = new TextTransferObject((String) swingT.getTransferData(flavors[i]));
          }
        }
        catch (Exception e) {
          if (ex == null) {
            ex = e;
          }
        }
      }
      else if (flavors[i].isMimeTypeEqual(DataFlavor.imageFlavor)) {
        try {
          if (imageTransferObject == null) {
            imageTransferObject = new ImageTransferObject(swingT.getTransferData(flavors[i]));
          }
        }
        catch (Exception e) {
          if (ex == null) {
            ex = e;
          }
        }
      }
      else if (flavors[i].isFlavorTextType()) {
        try {
          if (textTransferObject == null) {
            textTransferObject = new TextTransferObject((String) swingT.getTransferData(flavors[i]));
          }
        }
        catch (Exception e) {
          if (ex == null) {
            ex = e;
          }
        }
      }
      else if (flavors[i].isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
        try {
          if (jvmTransferObject == null) {
            jvmTransferObject = new JavaTransferObject(swingT.getTransferData(flavors[i]));
          }
        }
        catch (Exception e) {
          if (ex == null) {
            ex = e;
          }
        }
      }
    }
    // this is the priority we need for the transfer-objects:
    if (fileTransferObject != null) {
      return fileTransferObject;
    }
    else if (imageTransferObject != null) {
      return imageTransferObject;
    }
    else if (serializedTransferObject != null) {
      return serializedTransferObject;
    }
    else if (textTransferObject != null) {
      return textTransferObject;
    }
    else if (jvmTransferObject != null) {
      return jvmTransferObject;
    }
    else {
      if (ex != null) {
        LOG.warn("swing transferable=" + swingT, ex);
      }
      return null;
    }
  }

  public static int createHorizontalAlignment(int scoutAlign) {
    switch (scoutAlign) {
      case -1: {
        return SwingConstants.LEFT;
      }
      case 0: {
        return SwingConstants.CENTER;
      }
      case 1: {
        return SwingConstants.RIGHT;
      }
      default: {
        return createDefaultHorizontalAlignment();
      }
    }
  }

  private static int createDefaultHorizontalAlignment() {
    int swingAlign = SwingConstants.RIGHT;
    String defaultAlignment = UIManager.getDefaults().getString("Label.defaultHorizontalAlignment");
    if (defaultAlignment != null) {
      if ("LEFT".equalsIgnoreCase(defaultAlignment)) {
        swingAlign = SwingConstants.LEFT;
      }
      else if ("RIGHT".equalsIgnoreCase(defaultAlignment)) {
        swingAlign = SwingConstants.RIGHT;
      }
      else if ("CENTER".equalsIgnoreCase(defaultAlignment)) {
        swingAlign = SwingConstants.CENTER;
      }
    }
    return swingAlign;
  }

  public static float createAlignmentX(int scoutAlign) {
    switch (scoutAlign) {
      case -1: {
        return 0f;
      }
      case 0: {
        return 0.5f;
      }
      case 1: {
        return 1f;
      }
      default: {
        return 0f;
      }
    }
  }

  public static int createVerticalAlignment(int scoutAlign) {
    switch (scoutAlign) {
      case -1: {
        return SwingConstants.TOP;
      }
      case 0: {
        return SwingConstants.CENTER;
      }
      case 1: {
        return SwingConstants.BOTTOM;
      }
      default: {
        return SwingConstants.TOP;
      }
    }
  }

  public static float createAlignmentY(int scoutAlign) {
    switch (scoutAlign) {
      case -1: {
        return 0F;
      }
      case 0: {
        return 0.5f;
      }
      case 1: {
        return 1f;
      }
      default: {
        return 0f;
      }
    }
  }

  /**
   * @return a component with the specified size and resize constraints
   *         Useful to add fillers to a dynamic layout.
   */
  public static JComponent createGlue(int w, int h, boolean fillHorizontal, boolean fillVertical) {
    JPanel filler = new JPanel(null);
    filler.setOpaque(false);
    filler.setMinimumSize(new Dimension(fillHorizontal ? 0 : w, fillVertical ? 0 : h));
    filler.setPreferredSize(new Dimension(w, h));
    filler.setMaximumSize(new Dimension(fillHorizontal ? 10240 : w, fillVertical ? 10240 : h));
    return filler;
  }

  public static boolean runInputVerifier() {
    Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    return runInputVerifier(comp);
  }

  public static boolean runInputVerifier(Component comp) {
    if (comp == null || !(comp instanceof JComponent)) {
      return true;
    }
    JComponent jFocusOwner = (JComponent) comp;
    InputVerifier iv = jFocusOwner.getInputVerifier();
    if (iv == null) {
      return true;
    }
    else {
      return iv.shouldYieldFocus(jFocusOwner);
    }
  }

  /**
   * install focus forward and backward
   */
  public static void installDefaultFocusHandling(Container c) {
    // focus TAB
    HashSet<KeyStroke> set = new HashSet<KeyStroke>(1);
    set.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
    c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);

    // focus shift-TAB
    set = new HashSet<KeyStroke>(1);
    set.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
    c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, set);

    // make input map WHEN_FOCUSED non-empty for Focus Traversal policy to work
    // correctly
    if (c instanceof JComponent) {
      JComponent jc = (JComponent) c;
      InputMap inputMapWhenFocused = jc.getInputMap(JComponent.WHEN_FOCUSED);
      if (inputMapWhenFocused.size() == 0) {
        inputMapWhenFocused.put(KeyStroke.getKeyStroke(KeyEvent.VK_STOP, KeyEvent.KEY_TYPED), "swingDummyFocusKey");
      }
    }
  }

  /**
   * installs a focus cycle, its keys and the (optional) policy
   */
  public static void installFocusCycleRoot(Container c, FocusTraversalPolicy cyclePolicy) {
    c.setFocusCycleRoot(true);
    c.setFocusTraversalPolicy(cyclePolicy);
    if (c instanceof RootPaneContainer) {
      Container contentPane = ((RootPaneContainer) c).getContentPane();
      if (contentPane != null) {
        contentPane.setFocusTraversalPolicy(null);
        contentPane.setFocusCycleRoot(false);
      }
    }
  }

  public static void installAlternateCopyPaste(JComponent comp) {
    comp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("shift INSERT"), "paste-from-clipboard");
    comp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("shift DELETE"), "cut-to-clipboard");
    comp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ctrl INSERT"), "copy-to-clipboard");
  }

  public static void installDevelopmentShortcuts(JComponent pane) {
    if (Platform.inDevelopmentMode()) {
      SwingScoutSimulator.getInstance().attach();
      //
      pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control R"), "record");
      pane.getActionMap().put("record", new SimulatorAction('R'));
      //
      pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "stop");
      pane.getActionMap().put("stop", new SimulatorAction('S'));
      //
      pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control P"), "play");
      pane.getActionMap().put("play", new SimulatorAction('P'));
      //
      pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control M"), "playMulti");
      pane.getActionMap().put("playMulti", new SimulatorAction('M'));
      //
    }
  }

  /**
   * When using text as tooltip, table cell, tree cell then newline characters are ignored <b>unless</b>
   * the text is encapsulated in &lt;html&gt;...&lt;/html&gt;
   * <p>
   * This helper checks the string for newline characters \n and \r
   */
  public static boolean isMultilineLabelText(String s) {
    if (s != null) {
      if (s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * When using text as tooltip, table cell, tree cell then newline characters are ignored <b>unless</b>
   * the text is encapsulated in &lt;html&gt;...&lt;/html&gt;
   * <p>
   * This helper encapsulates the text with &lt;html&gt;&lt;/html&gt; (only if it is not already encapsulated) and
   * html-encodes the text using {@link StringUtility#htmlEncode(String, boolean)}.
   * <p>
   * See also {@link BasicHTML#isHTMLString(String)}.
   * <p>
   * When null is passed, null is returned.
   */
  public static String createHtmlLabelText(String s, boolean allowTextWrap) {
    if (s != null) {
      if (!BasicHTML.isHTMLString(s)) {
        String body = StringUtility.htmlEncode(s, !allowTextWrap);
        s = "<html>" + body + "</html>";
      }
    }
    return s;
  }

  /**
   * @param r
   *          the original rectangle
   * @param includeReservedInsets
   *          if taskbar and other windowing insets should be included in the
   *          returned area
   * @return the effective view of the monitor that the rectangle mostly covers
   */
  public static Rectangle getFullScreenBoundsFor(Rectangle r, boolean includeReservedInsets) {
    GraphicsDevice screenDevice = getCurrentScreen(r);

    GraphicsConfiguration config = screenDevice.getDefaultConfiguration();
    Rectangle bounds = config.getBounds();
    if (!includeReservedInsets) {
      // getting bounds excluding native windowing system insets (like task bars, ...)
      // therefore the insets has to be calculated...
      Insets screenInsets = getScreenInsets(screenDevice);
      // ... and manually removed from the full screen bounds
      bounds.x += screenInsets.left;
      bounds.y += screenInsets.top;
      bounds.width = bounds.width - screenInsets.left - screenInsets.right;
      bounds.height = bounds.height - screenInsets.top - screenInsets.bottom;
    }

    return bounds;
  }

  /**
   * @param r
   *          the rectangle to be used for the evaluation of current screen device
   * @return the effective screen device that the rectangle mostly covers
   */
  public static GraphicsDevice getCurrentScreen(Rectangle r) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    TreeMap<Integer, GraphicsDevice> prioMap = new TreeMap<Integer, GraphicsDevice>();
    // add default device with lowest prio
    prioMap.put(-1, ge.getDefaultScreenDevice());
    for (GraphicsDevice dev : ge.getScreenDevices()) {
      Rectangle bounds = dev.getDefaultConfiguration().getBounds();

      Rectangle intersection = bounds.intersection(r);
      if (intersection.width < 0 && intersection.height < 0) {
        // (bsh 2010-11-24) make sure that the resulting factor will be negative
        intersection.width *= -1;
      }
      prioMap.put(intersection.width * intersection.height, dev);
    }
    return prioMap.get(prioMap.lastKey());
  }

  /**
   * Gets the insets of the screen.
   * <p>
   * <b>Attention: </b>Due to <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6899304">Java bug 6899304</a>
   * this method only returns correct insets values for the primary screen device. For other screen devices empty insets
   * will be returned. In Windows environments these circumstances (task bar on a none primary screen) will be very rare
   * and therefore ignored until the bug will be fixed in a future Java version.
   * </p>
   *
   * @param screenDevice
   *          a screen thats {@link GraphicsConfiguration} will be used to determine the insets
   * @return the insets of this toolkit's screen, in pixels, if the given screen device is the primary screen, otherwise
   *         empty insets
   * @see Toolkit#getScreenInsets(GraphicsConfiguration)
   */
  public static Insets getScreenInsets(GraphicsDevice screenDevice) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    // <bko 2012-02-29>
    // "Fix" for Sun bug 6899304 ("java.awt.Toolkit.getScreenInsets(GraphicsConfiguration) returns incorrect values")
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6699851
    if (screenDevice == ge.getDefaultScreenDevice()) {
      // only return Toolkit.getScreenInsets for primary screen device
      return Toolkit.getDefaultToolkit().getScreenInsets(screenDevice.getDefaultConfiguration());
    }
    else {
      // return empty insets for other screen devices
      return new Insets(0, 0, 0, 0);
    }
    // </bko>
  }

  /**
   * @param r
   *          the original rectangle
   * @param includeReservedInsets
   *          if taskbar and other windowing insets should be included in the
   *          returned area
   * @return iff there are multiple monitors the other monitor than the
   *         effective view of the monitor that the rectangle mostly coveres, or
   *         null if there is just one screen
   */
  public static Rectangle getOppositeFullScreenBoundsFor(Rectangle r, boolean includeReservedInsets) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    TreeMap<Integer, Rectangle> prioMap = new TreeMap<Integer, Rectangle>();
    for (GraphicsDevice dev : ge.getScreenDevices()) {
      Rectangle bounds;
      if ((!includeReservedInsets) && dev == ge.getDefaultScreenDevice()) {
        bounds = ge.getMaximumWindowBounds();
      }
      else {
        bounds = dev.getDefaultConfiguration().getBounds();
      }
      Rectangle intersection = bounds.intersection(r);
      prioMap.put(intersection.width * intersection.height, bounds);
    }
    if (prioMap.size() <= 1) {
      return null;
    }
    else {
      return prioMap.get(prioMap.firstKey());
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
  public static Rectangle intersectRectangleWithScreen(Rectangle r, boolean includeReservedInsets, boolean singleMonitor) {
    if (singleMonitor) {
      return r.intersection(getFullScreenBoundsFor(r, includeReservedInsets));
    }
    else {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      ArrayList<Rectangle> intersections = new ArrayList<Rectangle>();
      for (GraphicsDevice dev : ge.getScreenDevices()) {
        Rectangle bounds;
        if ((!includeReservedInsets) && dev == ge.getDefaultScreenDevice()) {
          bounds = ge.getMaximumWindowBounds();
        }
        else {
          bounds = dev.getDefaultConfiguration().getBounds();
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

  /**
   * @param r
   *          the original rectangle
   * @param includeReservedInsets
   *          if taskbar and other windowing insets should be included in the
   *          returned area
   * @param singleMonitor
   *          if only one monitor is to be used or all monitors together
   * @return true if r is fully contained in one or all (singleMonitorFlag)
   *         monitors without exceeding one of its edges
   */
  public static boolean isRectangleInsideScreen(Rectangle r, boolean includeReservedInsets, boolean singleMonitor) {
    Rectangle t = intersectRectangleWithScreen(r, includeReservedInsets, singleMonitor);
    return t.equals(r);
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
  public static Rectangle validateRectangleOnScreen(Rectangle r, boolean includeReservedInsets, boolean singleMonitor) {
    return validateRectangleOnScreen(r, new Rectangle(-100000, -100000, 200000, 200000), includeReservedInsets, singleMonitor);
  }

  /**
   * @param r
   *          the original rectangle
   * @param referenceRect
   *          "r" is validated against the screen that this reference rectangle lies upon (or has the
   *          largest intersection with).
   * @param includeReservedInsets
   *          if taskbar and other windowing insets should be included in the
   *          returned area
   * @param singleMonitor
   *          if only one monitor is to be used or all monitors together
   * @return the eventually resized and moved rectangle with regard to one or
   *         all (singleMonitorFlag) monitors
   */
  public static Rectangle validateRectangleOnScreen(Rectangle r, Rectangle referenceRect, boolean includeReservedInsets, boolean singleMonitor) {
    Rectangle t = intersectRectangleWithScreen(r, includeReservedInsets, singleMonitor);
    if (!t.equals(r)) {
      Rectangle a = r.getBounds();
      Rectangle screen = intersectRectangleWithScreen(referenceRect, includeReservedInsets, singleMonitor);
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
   * Adjusts the window such that it fits on the screen, if necessary.
   *
   * @param window
   */
  public static void adjustBoundsToScreen(Window window) {
    Rectangle origBounds = window.getBounds();
    Rectangle newBounds = SwingUtility.validateRectangleOnScreen(origBounds, false, true);
    if (!newBounds.equals(origBounds)) {
      window.setLocation(newBounds.getLocation());
      window.setSize(newBounds.getSize());
    }
  }

  /**
   * @return true if the pixel coordinate is just one pixel left of the
   *         scrollpanes right border. This can be used to avoid double-border
   *         line aliasing effeect where single-lines are needed
   */
  public static boolean isAtScrollPaneRightBorder(Component c, int x) {
    JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, c);
    if (sp != null) {
      int localX = SwingUtilities.convertPoint(c, x, 0, sp).x;
      if (localX + 1 == sp.getWidth()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return true if the pixel coordinate is just one pixel above the
   *         scrollpanes bottom border. This can be used to avoid double-border
   *         line aliasing effeect where single-lines are needed
   */
  public static boolean isAtScrollPaneBottomBorder(Component c, int y) {
    JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, c);
    if (sp != null) {
      int localY = SwingUtilities.convertPoint(c, 0, y, sp).y;
      if (localY + 1 == sp.getHeight()) {
        return true;
      }
    }
    return false;
  }

  public static <T extends Component> T findChildComponent(Component parent, Class<T> type) {
    ArrayList<T> list = new ArrayList<T>(1);
    findChildComponentsRec(parent, type, list, 1);
    if (list.size() > 0) {
      return list.get(0);
    }
    else {
      return null;
    }
  }

  public static <T extends Component> List<T> findChildComponents(Component parent, Class<T> type) {
    ArrayList<T> list = new ArrayList<T>();
    findChildComponentsRec(parent, type, list, Integer.MAX_VALUE);
    return list;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Component> void findChildComponentsRec(Component parent, Class<T> type, List<T> list, int maxCount) {
    if (type.isAssignableFrom(parent.getClass())) {
      list.add((T) parent);
      if (list.size() >= maxCount) {
        return;
      }
    }
    //
    if (parent instanceof Container) {
      for (Component c : ((Container) parent).getComponents()) {
        findChildComponentsRec(c, type, list, maxCount);
        if (list.size() >= maxCount) {
          return;
        }
      }
    }
  }

  /**
   * @return the visible size of a component in its viewport (including
   *         scrollbars)
   */
  public static Dimension getVisibleSizeInViewport(Component c) {
    if (c.getParent() instanceof JViewport) {
      JViewport vp = (JViewport) c.getParent();
      Dimension d = vp.getExtentSize();
      if (vp.getParent() instanceof JScrollPane) {
        JScrollPane sp = (JScrollPane) vp.getParent();
        if (sp.getVerticalScrollBar() != null && sp.getVerticalScrollBar().isVisible()) {
          d.width += sp.getVerticalScrollBar().getWidth();
        }
        if (sp.getHorizontalScrollBar() != null && sp.getHorizontalScrollBar().isVisible()) {
          d.height += sp.getHorizontalScrollBar().getHeight();
        }
      }
      return d;
    }
    else {
      return null;
    }
  }

  /**
   * Returns an appropriate location for a component's tool tip that <i>always</i>
   * lies within the specified frame.
   * <p>
   * Intended be used in custom implementations of {@link JComponent#getToolTipLocation(MouseEvent)}.
   *
   * @param e
   *          the event that caused the display of the tool tip
   * @param c
   *          the parent component of the tool tip
   * @param frame
   *          a component in which the tool tip has to fit (usually the surrounding window of "c")
   * @return
   */
  public static Point getAdjustedToolTipLocation(MouseEvent e, JComponent c, Component frame) {
    JToolTip tip = new JToolTip();
    tip.setTipText(c.getToolTipText(e));
    Dimension tipSize = tip.getPreferredSize();
    // Tool tip will be positioned within the bounds of the specified component (+ 5px inset)
    Rectangle frameR = frame.getBounds();
    if (frame instanceof Container) {
      Container container = (Container) frame;
      Insets insets = container.getInsets();
      frameR.x += insets.left;
      frameR.y += insets.top;
      frameR.width -= (insets.left + insets.right);
      frameR.height -= (insets.top + insets.bottom);
    }
    frameR.x += 5;
    frameR.y += 5;
    frameR.width -= 10;
    frameR.height -= 10;
    // Initial try for the tool tip's position
    Rectangle r = new Rectangle(e.getXOnScreen(), c.getLocationOnScreen().y + c.getSize().height + 1, tipSize.width, tipSize.height);
    // Check if it fits within the frame
    Rectangle intersection = frameR.intersection(r);
    if (r.equals(intersection)) {
      // Tool tip is fully visible within the frame --> use default behaviour
      //
      // Note: The implementation of ToolTipManager.showTipWindow() is not always
      // correct in dual screen mode. The tool tip is _always_ put on that screen,
      // where the most part of the frame lies upon, even if we return coordinates
      // that clearly belong to the other screen. Unfortunately we cannot change
      // that behavior... (bsh 2010-11-24)
      return null;
    }
    // Otherwise, move the tool tip
    int correction = 0;
    if (r.height == intersection.height) {
      // Height is okay, just move left. To make it look better, position the
      // tip 5px below the component.
      r = new Rectangle(r.x, c.getLocationOnScreen().y + c.getSize().height + 5, tipSize.width, tipSize.height);
      correction = -5; // needed to make the ToolTipManager use a lightweight pop-up
    }
    else {
      // The height does not fit. Position the tool tip above the component.
      r = new Rectangle(c.getLocationOnScreen().x + 10, c.getLocationOnScreen().y - tipSize.height - 1, tipSize.width, tipSize.height);
    }
    // Adjust to frame bounds
    intersection = frameR.intersection(r);
    intersection.x -= (r.width - intersection.width);
    intersection.y -= (r.height - intersection.height);
    // Return value is expected to be relative to the component's position
    return new Point(
        (-c.getLocationOnScreen().x) + intersection.x + correction,
        (-c.getLocationOnScreen().y) + intersection.y);
  }

  public static boolean hasScoutLookAndFeelFrameAndDialog() {
    String useScoutLafFrameAndDialog = System.getProperty("scout.laf.useLafFrameAndDialog");
    boolean useLafFrameAndDialog = true;
    if (StringUtility.hasText(useScoutLafFrameAndDialog)) {
      useLafFrameAndDialog = Boolean.parseBoolean(useScoutLafFrameAndDialog);
    }
    return useLafFrameAndDialog;
  }

  /**
   * This method is used to get a top margin for {@link SwingScoutLabelField} and {@link SwingScoutHtmlField} in order
   * to have correct alignment for customized look and feel (e.g. Rayo)
   *
   * @since 3.10.0-M2
   */
  public static int getTopMarginForField() {
    if (topMarginForField == null) {
      String topMarginForFieldProperty = System.getProperty("scout.laf.topMarginForField");
      if (topMarginForFieldProperty != null) {
        topMarginForField = Integer.parseInt(topMarginForFieldProperty);
      }
      else {
        topMarginForField = Integer.valueOf(0);
      }
    }

    return topMarginForField;
  }

  public static void setDefaultImageIcons(Window window) {
    Icon icon = UIManager.getIcon("Window.icon");
    if (icon instanceof ImageIcon) {
      window.setIconImage(((ImageIcon) icon).getImage());
    }
    Object icons = UIManager.getDefaults().get("Window.icons");
    if (icons instanceof List<?>) {
      List<Image> iconList = new ArrayList<Image>();
      for (Object ico : ((List<?>) icons)) {
        if (ico instanceof Image) {
          iconList.add((Image) ico);
        }
      }
      window.setIconImages(iconList);
    }
  }

  /*
   * necessary as workaround for awt bug: when component does not change
   * size, its reported minimumSize, preferredSize and maximumSize are
   * cached instead of beeing calculated using layout manager
   */
  public static void setZeroBounds(Component... components) {
    for (Component c : components) {
      c.setBounds(0, 0, 0, 0);
    }
  }

  /**
   * Replaces 3 digit CSS colors with 6 digit colors: (e.g. #fff with #ffffff)
   *
   * @param rawHtml
   *          may be <code>null</code>
   */
  public static String replace3DigitColors(String rawHtml) {
    if (StringUtility.isNullOrEmpty(rawHtml)) {
      return rawHtml;
    }

    String styleRegex = "style[\\s]*=[\\s]*[\"\'][^\"^\']+?color:[\\s]*?#([a-fA-F0-9])([a-fA-F0-9])([a-fA-F0-9])[\"\']";
    Pattern pattern = Pattern.compile(styleRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher matcher = pattern.matcher(rawHtml);

    StringBuffer sb = new StringBuffer();
    int matchEndIndex = 0;
    while (matcher.find()) {
      //append pre match
      sb.append(rawHtml.substring(matchEndIndex, matcher.start(1)));

      //replace digits
      String c1 = matcher.group(1);
      String c2 = matcher.group(2);
      String c3 = matcher.group(3);
      sb.append(c1 + c1 + c2 + c2 + c3 + c3);

      //mark end of replaced color
      matchEndIndex = matcher.end(3);
    }
    //append post match
    sb.append(rawHtml.substring(matchEndIndex, rawHtml.length()));
    return sb.toString();
  }
}
