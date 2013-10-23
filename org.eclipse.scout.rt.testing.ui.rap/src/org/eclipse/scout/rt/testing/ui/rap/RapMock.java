/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.testing.ui.rap;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.WaitCondition;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.ext.IDropDownButtonForPatch;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.testing.client.IGuiMock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.seleniumemulation.ElementFinder;
import org.openqa.selenium.internal.seleniumemulation.JavascriptLibrary;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

/**
 *
 */
public class RapMock implements IGuiMock {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RapMock.class);

  static interface MockRunnable<T> extends WaitCondition<T> {
  }

  private static DriverService m_service = null;
  private WebDriver m_driver;
  private RAPSelenium m_bot;
  private IClientSession m_session;
  private int m_sleepDelay = 40;

  private final ElementFinder m_elementFinder = new ElementFinder(new JavascriptLibrary());
  private String m_currentWidgetId = "";
  private WebElement m_currentElement = null;
  private boolean m_modifierPressed = false;
  private Actions m_actionBuilder = null;
  private List<CharSequence> m_keyList = new ArrayList<CharSequence>();

  private static boolean useChrome = false;
  private static boolean useFirefox = !useChrome;
  private static boolean useFirebug = false && useFirefox;

  public void setClientSession(IClientSession session) {
    m_session = session;
  }

  @Override
  public void initializeMock() {
    if (useChrome) {
      m_service = new ChromeDriverService.Builder()
          .usingDriverExecutable(new File("e:/Downloads/java/chromedriver.exe"))
          .usingAnyFreePort()
          .build();
    }
    try {
      if (m_service != null) {
        m_service.start();
      }
    }
    catch (IOException e) {
      throw new IllegalStateException("ChromeDriverService could not be started.", e);
    }
  }

  @Override
  public void shutdownMock() {
    if (m_service != null) {
      m_service.stop();
    }
  }

  @Override
  public void beforeTest() {
    if (m_service != null) {
      if (useChrome) {
        DesiredCapabilities chrome = DesiredCapabilities.chrome();
        m_driver = new RemoteWebDriver(m_service.getUrl(), chrome);
      }
    }
    else {
      if (useFirefox) {
        System.setProperty("webdriver.firefox.bin", "C:/FirefoxPortableTest_11/App/Firefox/firefox.exe");
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        if (useFirebug) {
          try {
            firefoxProfile.addExtension(new File("E:/Downloads/java/firebug-1.9.2-fx.xpi"));
            firefoxProfile.setPreference("extensions.firebug.currentVersion", "1.9.2"); // Avoid startup screen

            firefoxProfile.addExtension(new File("E:/Downloads/java/firepath-0.9.7-fx.xpi"));
          }
          catch (IOException e) {
            throw new IllegalStateException("Could not add/find firefox extensions.", e);
          }
        }

        m_driver = new FirefoxDriver(firefoxProfile);
//        m_driver = new FirefoxDriver(new FirefoxProfile(new File("C:/Temp/webdriver-profile")));
      }
    }
    if (m_driver == null) {
      throw new NullPointerException("no driver instantiated!");
    }
    m_bot = new RAPSelenium(m_driver, "http://localhost:8081");
    m_actionBuilder = new Actions(m_bot.getWrappedDriver());

    m_bot.open("http://localhost:8081/rap");
    m_bot.waitForElementPresent("w2");
  }

  @Override
  public void afterTest() {
    m_driver.quit();
  }

  @Override
  public GuiStrategy getStrategy() {
    return GuiStrategy.Rap;
  }

  public void setCurrentWidgetId(String currentWidgetId) {
    m_currentWidgetId = currentWidgetId;

    m_currentElement = m_elementFinder.findElement(m_bot.getWrappedDriver(), currentWidgetId);
  }

  public String getCurrentWidgetId_() {
    return m_currentWidgetId;
  }

  public WebElement getCurrentElement() {
    return m_currentElement;
  }

  @Override
  public void waitForIdle() {
    if (getDisplay().getThread() == Thread.currentThread()) {
      return;
    }
    //
    for (int pass = 0; pass < 1; pass++) {
      //wait until gui queue is empty
      syncExec(new MockRunnable<Object>() {
        @Override
        public Object run() throws Throwable {
          return null;
        }
      });
      //wait until model queue is empty
      ClientSyncJob idleJob = new ClientSyncJob("Check for idle", m_session) {
        @Override
        protected void runVoid(IProgressMonitor m) throws Throwable {
        }
      };
      idleJob.setSystem(true);

      final CountDownLatch idleJobScheduledSignal = new CountDownLatch(1);
      JobChangeAdapter listener = new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
          idleJobScheduledSignal.countDown();
        }
      };

      try {
        idleJob.addJobChangeListener(listener);
        idleJob.schedule();
        try {
          idleJobScheduledSignal.await();
        }
        catch (InterruptedException e) {
          throw new IllegalStateException("Interrupted");
        }
      }
      finally {
        idleJob.removeJobChangeListener(listener);
      }
    }
  }

  @Override
  public void waitForActiveWindow(final String title) {
    waitUntil(new WaitCondition<Object>() {
      @Override
      public Object run() {
        if (isWindowActive(title)) {
          return true;
        }
        else {
          return null;
        }
      }
    });
    waitForIdle();
  }

  @Override
  public void waitForOpenWindow(final String title) {
    waitUntil(new WaitCondition<Object>() {
      @Override
      public Object run() {
        if (isWindowOpen(title)) {
          return true;
        }
        else {
          return null;
        }
      }
    });
    waitForIdle();
  }

  @Override
  public int getSleepDelay() {
    return m_sleepDelay;
  }

  @Override
  public void setSleepDelay(int sleepDelay) {
    m_sleepDelay = sleepDelay;
  }

  @Override
  public void sleep() {
    sleep(getSleepDelay());
  }

  @Override
  public void sleep(int millis) {
    //only sleep when NOT in gui thread
    if (getDisplay().getThread() == Thread.currentThread()) {
      return;
    }
    //
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      //nop
    }
    waitForIdle();
  }

  @Override
  public boolean isWindowActive(final String title) {
    return syncExec(new MockRunnable<Boolean>() {
      @Override
      public Boolean run() throws Throwable {
        CTabItem view = findWorkbenchView(title);
        if (view != null && view.getParent().getSelection() == view) {
          return true;
        }
        Shell shell = findShell(title);
        if (shell != null && shell == getActiveShell()) {
          return true;
        }
        return false;
      }
    });
  }

  @Override
  public boolean isWindowOpen(final String title) {
    return syncExec(new MockRunnable<Boolean>() {
      @Override
      public Boolean run() throws Throwable {
        CTabItem view = findWorkbenchView(title);
        if (view != null) {
          return true;
        }
        Shell shell = findShell(title);
        if (shell != null) {
          return true;
        }
        return false;
      }
    });
  }

  @Override
  public void activateWindow(final String title) {
    waitForOpenWindow(title);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        CTabItem view = findWorkbenchView(title);
        if (view != null) {
          view.getParent().setSelection(view);
          return null;
        }
        Shell shell = findShell(title);
        if (shell != null) {
          shell.setActive();
          return null;
        }
        throw new IllegalStateException("There is no view with title " + title);
      }
    });
    waitForIdle();
  }

  @Override
  public FieldState getFieldState(FieldType type, int index) {
    final Control c = waitForIndexedField(type, index);
    return syncExec(new MockRunnable<FieldState>() {
      @Override
      public FieldState run() throws Throwable {
        return getFieldStateInternal(c);
      }
    });
  }

  @Override
  public FieldState getScoutFieldState(String name) {
    final Control c = waitForScoutField(name);
    return syncExec(new MockRunnable<FieldState>() {
      @Override
      public FieldState run() throws Throwable {
        return getFieldStateInternal(c);
      }
    });
  }

  @Override
  public FieldState getScoutFieldContainerState(String name) {
    final Control c = waitForScoutField(name);
    return syncExec(new MockRunnable<FieldState>() {
      @Override
      public FieldState run() throws Throwable {
        IRwtScoutComposite swtScoutComposite = RwtScoutComposite.getCompositeOnWidget(c);
        if (swtScoutComposite == null) {
          return null;
        }

        return getFieldStateInternal(swtScoutComposite.getUiContainer());
      }
    });
  }

  @Override
  public List<FieldState> getFieldStates(final FieldType type) {
    return syncExec(new MockRunnable<List<FieldState>>() {
      @Override
      public List<FieldState> run() throws Throwable {
        List<FieldState> list = new ArrayList<FieldState>();
        for (Control c : RwtUtility.findChildComponents(getActiveShell(), Control.class)) {
          if (type == null && getFieldTypeOf(c) != null) {
            list.add(getFieldStateInternal(c));
          }
          else if (type != null && getFieldTypeOf(c) == type) {
            list.add(getFieldStateInternal(c));
          }
        }
        return list;
      }
    });
  }

  @Override
  public FieldState getFocusFieldState() {
    return syncExec(new MockRunnable<FieldState>() {
      @Override
      public FieldState run() throws Throwable {
        Control c = getDisplay().getFocusControl();
        if (c == null) {
          throw new IllegalStateException("There is no focus owner");
        }
        return getFieldStateInternal(c);
      }
    });
  }

  @Override
  public void clickOnPushButton(String text) {
    final Control c = waitForPushButtonWithLabel(text);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Point p = c.toDisplay(5, 5);
        gotoPoint(p.x, p.y);
        clickLeft();
        return null;
      }
    });
    waitForIdle();
  }

  @Override
  public void gotoField(FieldType type, int index) {
    Control c = waitForIndexedField(type, index);
    setCurrentWidgetId(WidgetUtil.getAdapter(c).getId());
    if (FieldType.Text.equals(type)) {
      setCurrentWidgetId(new StringBuffer("//*[@id=\"").append(getCurrentWidgetId_()).append("\"]/input").toString());
    }
  }

  @Override
  public void gotoScoutField(String name) {
    gotoScoutField(name, 0.5, 0.5);
  }

  @Override
  public void gotoScoutField(String name, double x, double y) {
    if (x < 0 || x > 1) {
      throw new IllegalArgumentException("x should be in [0, 1] range.");
    }
    if (y < 0 || y > 1) {
      throw new IllegalArgumentException("y should be in [0, 1] range.");
    }
    final Control c = waitForScoutField(name);
    setCurrentWidgetId(WidgetUtil.getAdapter(c).getId());
    if (c instanceof Text) {
      setCurrentWidgetId(new StringBuffer("//*[@id=\"").append(getCurrentWidgetId_()).append("\"]/input").toString());
    }
  }

  @Override
  public void gotoTable(int tableIndex, final int rowIndex, final int columnIndex) {
    final Table table = (Table) waitForIndexedField(FieldType.Table, tableIndex);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        setCurrentWidgetId(WidgetUtil.getAdapter(table).getId());
        StringBuffer xpathWidgetId = new StringBuffer("//*[@id=\"").append(getCurrentWidgetId_()).append("\"]/div[1]/div[").append(rowIndex + table.getColumnCount()).append("]/div");
        if (columnIndex > 0) {
          xpathWidgetId.append("[").append(columnIndex).append("]");
        }
        setCurrentWidgetId(xpathWidgetId.toString());
        return null;
      }
    });
  }

  @Override
  public void gotoTableHeader(int tableIndex, final int columnIndex) {
    final Table table = (Table) waitForIndexedField(FieldType.Table, tableIndex);
    syncExec(new MockRunnable<Object>() {
      @SuppressWarnings("null")
      @Override
      public Object run() throws Throwable {
        int curIndex = -1;
        int accumulatedWidth = 0;
        Rectangle cellBounds = null;
        for (int i : table.getColumnOrder()) {
          TableColumn col = table.getColumn(i);
          //first column is dummy column
          if (i > 0) {
            curIndex++;
            if (curIndex == columnIndex) {
              cellBounds = new Rectangle(accumulatedWidth, 0, col.getWidth(), table.getHeaderHeight());
              break;
            }
          }
          accumulatedWidth += col.getWidth();
        }
        cellBounds.x -= table.getHorizontalBar().getSelection();
        Point p = table.toDisplay(cellBounds.x + (cellBounds.width / 2), cellBounds.y + (cellBounds.height / 2));
        gotoPoint(p.x, p.y);
        return null;
      }
    });
  }

  @Override
  public void gotoTree(int treeIndex, final String nodeText) {
    final Tree tree = (Tree) waitForIndexedField(FieldType.Tree, treeIndex);
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        setCurrentWidgetId(WidgetUtil.getAdapter(tree).getId());
        TreeItem[] items = tree.getItems();
        for (int i = 0; i < items.length; i++) {
          if (nodeText.equals(items[i].getText())) {
            StringBuffer xpathWidgetId = new StringBuffer("//*[@id=\"").append(getCurrentWidgetId_()).append("\"]/div[1]/div[").append(i + 1).append("]/div[2]");
            setCurrentWidgetId(xpathWidgetId.toString());
            break;
          }
        }
        return null;
      }
    });
  }

  @Override
  public void contextMenu(final String... names) {
    //move to menu
    for (int i = 0; i < names.length; i++) {
      String label = names[i];
      final boolean lastItem = i == names.length - 1;
      final MenuItem m = waitForMenuItem(label);
      syncExec(new MockRunnable<Boolean>() {
        @Override
        public Boolean run() throws Throwable {
          //toggle
          if ((m.getStyle() & (SWT.CHECK | SWT.RADIO)) != 0) {
            m.setSelection(!m.getSelection());
          }
          //fire selection
          Event event = new Event();
          event.display = getDisplay();
          event.time = (int) System.currentTimeMillis();
          event.type = SWT.Selection;
          event.widget = m;
          m.notifyListeners(event.type, event);
          if (lastItem) {
            //nop
          }
          return null;
        }
      });
      waitForIdle();
    }
  }

  @Override
  public List<String> getTableCells(int tableIndex, final int columnIndex) {
    final Table table = (Table) waitForIndexedField(FieldType.Table, tableIndex);
    return syncExec(new MockRunnable<List<String>>() {
      @Override
      public List<String> run() throws Throwable {
        ArrayList<String> list = new ArrayList<String>();
        for (TableItem row : table.getItems()) {
          //first column is dummy column
          list.add(row.getText(columnIndex + 1));
        }
        return list;
      }
    });
  }

  @Override
  public List<String> getTreeNodes(final int treeIndex) {
    final Tree tree = (Tree) waitForIndexedField(FieldType.Tree, treeIndex);
    return syncExec(new MockRunnable<List<String>>() {
      @Override
      public List<String> run() throws Throwable {
        ArrayList<String> list = new ArrayList<String>();
        addTreeItemsRec(tree.getItems(), list);
        return list;
      }
    });
  }

  @Override
  public Set<String> getSelectedTableCells(int tableIndex, final int columnIndex) {
    final Table table = (Table) waitForIndexedField(FieldType.Table, tableIndex);
    return syncExec(new MockRunnable<Set<String>>() {
      @Override
      public Set<String> run() throws Throwable {
        TreeSet<String> set = new TreeSet<String>();
        TableItem[] sel = table.getSelection();
        if (sel != null) {
          for (TableItem row : sel) {
            //first column is dummy column
            set.add(row.getText(columnIndex + 1));
          }
        }
        return set;
      }
    });
  }

  @Override
  public Set<String> getSelectedTreeNodes(int treeIndex) {
    final Tree tree = (Tree) waitForIndexedField(FieldType.Tree, treeIndex);
    return syncExec(new MockRunnable<Set<String>>() {
      @Override
      public Set<String> run() throws Throwable {
        TreeSet<String> set = new TreeSet<String>();
        TreeItem[] sel = tree.getSelection();
        if (sel != null) {
          for (TreeItem row : sel) {
            set.add(row.getText(0));
          }
        }
        return set;
      }
    });
  }

  @Override
  public Set<String> getCheckedTableCells(int tableIndex, final int columnIndex) {
    final Table table = (Table) waitForIndexedField(FieldType.Table, tableIndex);
    return syncExec(new MockRunnable<Set<String>>() {
      @Override
      public Set<String> run() throws Throwable {
        TreeSet<String> check = new TreeSet<String>();
        for (int i = 0; i < table.getItemCount(); i++) {
          TableItem item = table.getItem(i);
          if (item.getData() instanceof ITableRow) {
            ITableRow row = (ITableRow) item.getData();
            if (row.isChecked()) {
              check.add(item.getText(columnIndex + 1));
            }
          }
        }
        return check;
      }
    });
  }

  @Override
  public void gotoPoint(int x, int y) {
    final Control c = waitForLocatedField(x, y);
    setCurrentWidgetId(WidgetUtil.getAdapter(c).getId());
  }

  @Override
  public void move(int deltaX, int deltaY) {
    //XXX RAP
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void clickLeft() {
    m_bot.clickAndWait(getCurrentWidgetId_());
    syncExec(new MockRunnable<Object>() {
      @Override
      public Object run() throws Throwable {
        Control focusControl = Display.getCurrent().getFocusControl();
        setCurrentWidgetId(WidgetUtil.getAdapter(focusControl).getId());
        return null;
      }
    });
  }

  @Override
  public void clickRight() {
    m_actionBuilder.contextClick(getCurrentElement());
    m_actionBuilder.perform();
    waitForIdle();
  }

  @Override
  public void drag(int x1, int y1, int x2, int y2) {
    //XXX RAP
    throw new UnsupportedOperationException("not implemented");
    /*
    gotoPoint(x1, y1);
    m_bot.pressLeft();
    gotoPoint(x2, y2);
    m_bot.releaseLeft();
    waitForIdle();
     */
  }

  @Override
  public void dragWindowRightBorder(WindowState windowState, int pixelToMoveOnX) {
    int borderSize = 4;

    int xPos = windowState.x + windowState.width + borderSize;
    int yPos = windowState.y + windowState.height / 2;
    drag(xPos, yPos, xPos + pixelToMoveOnX, yPos);
  }

  @Override
  public void typeText(final String text) {
    if (m_modifierPressed) {
      m_actionBuilder.sendKeys(text.toUpperCase());
//    m_bot.typeKeys(getCurrentWidgetId(), text);
//      m_keyList.add(text);
    }
    else {
      m_actionBuilder.sendKeys(text).perform();
      waitForIdle();
    }
  }

  @Override
  public void paste(String text) {
    //XXX RAP
    throw new UnsupportedOperationException("not implemented");
    /*
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    //press paste (ctrl-V)
    m_bot.pressKey(Key.Control);
    m_bot.typeText("v");
    m_bot.releaseKey(Key.Control);
     */
  }

  @Override
  public void pressKey(Key key) {
    switch (key) {
      case Shift:
        m_actionBuilder.keyDown(Keys.SHIFT);
        m_modifierPressed = true;
        break;
      case Control:
        m_actionBuilder.keyDown(Keys.CONTROL);
//        m_bot.controlKeyDown();
//        m_bot.keyDownNative("17");
//        m_keyList.add(Keys.CONTROL);
        m_modifierPressed = true;
        break;
      case Alt:
        m_actionBuilder.keyDown(Keys.ALT);
        m_modifierPressed = true;
        break;
      case Windows:
        m_actionBuilder.keyDown(Keys.META);
        m_modifierPressed = true;
        break;
      default:
        m_actionBuilder.sendKeys(toSeleniumKey(key).toString());
        m_actionBuilder.perform();
//        m_bot.keyDown(m_currentWidgetId, toSeleniumKey(key));
        waitForIdle();
        break;
    }
  }

  @Override
  public void releaseKey(Key key) {
    switch (key) {
      case Shift:
        m_actionBuilder.keyUp(Keys.SHIFT);
        m_modifierPressed = false;
        break;
      case Control:
        m_actionBuilder.keyUp(Keys.CONTROL);
//        m_bot.controlKeyUp();
//        m_bot.keyUpNative("17");
//        getCurrentElement().sendKeys(m_keyList.toArray(new CharSequence[m_keyList.size()]));
//        m_keyList.clear();

//        getCurrentElement().sendKeys(Keys.CONTROL, "a");
        m_modifierPressed = false;
        break;
      case Alt:
        m_actionBuilder.keyUp(Keys.ALT);
        m_modifierPressed = false;
        break;
      case Windows:
        m_actionBuilder.keyUp(Keys.META);
        m_modifierPressed = false;
        break;
      default:
        m_actionBuilder.keyUp(toSeleniumKey(key));
//        m_bot.keyUp(m_currentWidgetId, toSeleniumKey(key));
        break;
    }
    m_actionBuilder.perform();
    waitForIdle();
  }

  @Override
  public void typeKey(Key key) {
    if (m_modifierPressed) {
      m_actionBuilder.sendKeys(toSeleniumKey(key)).perform();
    }
    else {
      m_actionBuilder.sendKeys(getCurrentElement(), toSeleniumKey(key)).perform();
//      m_bot.keyPress(toSeleniumKey(key).toString());
    }
    waitForIdle();
  }

  @Override
  public WindowState getWindowState(final String title) {
    return syncExec(new MockRunnable<WindowState>() {
      @Override
      public WindowState run() throws Throwable {
        checkActiveShell();
        CTabItem view = findWorkbenchView(title);
        if (view != null) {
          WindowState state = new WindowState();
          Point p = view.getParent().toDisplay(0, 0);
          Point s = view.getParent().getSize();
          state.x = p.x;
          state.y = p.y;
          state.width = s.x;
          state.height = s.y;
          return state;
        }
        Shell shell = findShell(title);
        if (shell != null) {
          Rectangle r = shell.getBounds();
          WindowState state = new WindowState();
          state.x = r.x;
          state.y = r.y;
          state.width = r.width;
          state.height = r.height;
          return state;
        }
        throw new IllegalStateException("Window " + title + " not found");
      }
    });
  }

  @Override
  public String getClipboardText() {
    waitForIdle();
    return syncExec(new MockRunnable<String>() {
      @Override
      public String run() throws Throwable {
        //XXX RAP
//        Clipboard b = new Clipboard(getDisplay());
//        return (String) b.getContents(TextTransfer.getInstance());
        return "";
      }
    });
  }

  @Override
  public Object internal0(final Object o) {
    return syncExec(new MockRunnable<String>() {
      @Override
      public String run() throws Throwable {
        return null;
      }
    });
  }

  protected void checkActiveShell() {
    if (getActiveShell() == null) {
      throw new IllegalStateException("There is no active shell");
    }
  }

  protected FieldState getFieldStateInternal(Control c) {
    FieldState state = new FieldState();
    //type
    state.type = getFieldTypeOf(c);
    //scout name
    IPropertyObserver scoutObject = RwtScoutComposite.getScoutModelOnWidget(c);
    state.scoutName = (scoutObject != null ? scoutObject.getClass().getName() : null);
    //focus
    state.focus = (c == getDisplay().getFocusControl());
    //bounds
    Point p = c.toDisplay(0, 0);
    state.x = p.x;
    state.y = p.y;
    state.width = c.getBounds().width;
    state.height = c.getBounds().height;
    //text
    if (c instanceof Label) {
      state.text = ((Label) c).getText();
    }
    if (c instanceof Text) {
      state.text = ((Text) c).getText();
    }
    if (c instanceof StyledText) {
      state.text = ((StyledText) c).getText();
    }
    if (c instanceof Button) {
      state.text = ((Button) c).getText();
    }
    return state;
  }

  protected FieldType getFieldTypeOf(Control c) {
    if (c.isDisposed()) {
      return null;
    }
    if (!c.isVisible()) {
      return null;
    }
    //
    if (c instanceof Label) {
      return FieldType.Label;
    }
    if (c instanceof Text) {
      return FieldType.Text;
    }
    if (c instanceof StyledText) {
      return FieldType.Text;
    }
    if (c instanceof Table) {
      return FieldType.Table;
    }
    if (c instanceof Tree) {
      return FieldType.Tree;
    }
    if (c instanceof IDropDownButtonForPatch) {
      return FieldType.DropdownButton;
    }
    if (c instanceof Button) {
      int style = c.getStyle();
      if ((style & SWT.CHECK) != 0) {
        return FieldType.Checkbox;
      }
      else if ((style & SWT.RADIO) != 0) {
        return FieldType.RadioButton;
      }
      else if (c.getParent() instanceof Scrollable) {
        return FieldType.ScrollButton;
      }
      else {
        return FieldType.PushButton;
      }
    }
    return null;
  }

  protected String getScoutNameOf(Control c) {
    IPropertyObserver scoutObject = RwtScoutComposite.getScoutModelOnWidget(c);
    if (scoutObject != null) {
      return scoutObject.getClass().getName();
    }
    return null;
  }

  protected Display getDisplay() {
    IRwtEnvironment env = (IRwtEnvironment) m_session.getData(IRwtEnvironment.ENVIRONMENT_KEY);
    return env.getDisplay();
  }

  protected Shell getActiveShell() {
    return getDisplay().getActiveShell();
  }

  protected String cleanButtonLabel(String s) {
    return StringUtility.removeMnemonic(s);
  }

  protected TreeItem findTreeItemRec(TreeItem[] items, String nodeText) {
    if (items == null) {
      return null;
    }
    //
    for (TreeItem item : items) {
      if (nodeText.equals(item.getText())) {
        return item;
      }
      TreeItem found = findTreeItemRec(item.getItems(), nodeText);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  protected void addTreeItemsRec(TreeItem[] items, List<String> list) {
    if (items == null) {
      return;
    }
    //
    for (TreeItem item : items) {
      list.add(item.getText(0));
      addTreeItemsRec(item.getItems(), list);
    }
  }

  protected List<Composite> enumerateParentContainers() {
    return syncExec(new MockRunnable<ArrayList<Composite>>() {
      @Override
      public ArrayList<Composite> run() throws Throwable {
        ArrayList<Composite> list = new ArrayList<Composite>();
        for (Shell shell : getDisplay().getShells()) {
          if (shell.isVisible()) {
            list.add(shell);
          }
        }
        return list;
      }
    });
  }

  protected Shell findShell(final String title) {
    for (Shell shell : getDisplay().getShells()) {
      if (title.equals(shell.getText())) {
        return shell;
      }
    }
    return null;
  }

  protected CTabItem findWorkbenchView(final String title) {
    //XXX RAP
//    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();XXX RAP
//    if (shell != null) {
//      for (CTabFolder f : SwtUtility.findChildComponents(shell, CTabFolder.class)) {
//        if (f.getItemCount() > 0) {
//          for (CTabItem item : f.getItems()) {
//            if (item.isShowing()) {
//              if (title.equals(cleanButtonLabel(item.getText()))) {
//                return item;
//              }
//            }
//          }
//        }
//      }
//    }
    return null;
  }

  protected Control waitForPushButtonWithLabel(final String label) {
    return waitUntil(new WaitCondition<Control>() {
      @Override
      public Control run() {
        return syncExec(new MockRunnable<Control>() {
          @Override
          public Control run() throws Throwable {
            for (Shell shell : getDisplay().getShells()) {
              Composite parent = shell;
              for (Control o : RwtUtility.findChildComponents(parent, Control.class)) {
                if (o instanceof Button) {
                  if (cleanButtonLabel(label).equals(cleanButtonLabel(((Button) o).getText()))) {
                    return o;
                  }
                }
              }
            }
            return null;
          }
        });
      }
    });
  }

  protected Control waitForScoutField(final String name) {
    return waitUntil(new WaitCondition<Control>() {
      @Override
      public Control run() {
        return syncExec(new MockRunnable<Control>() {
          @Override
          public Control run() throws Throwable {
            Control lastSecondaryCandidate = null;
            for (Composite parent : enumerateParentContainers()) {
              for (Control c : RwtUtility.findChildComponents(parent, Control.class)) {
                String s = getScoutNameOf(c);
                if (s != null && ("." + s).endsWith("." + name)) {
                  lastSecondaryCandidate = c;
                  if (getFieldTypeOf(c) != null) {
                    //primary match
                    return c;
                  }
                }
              }
            }
            return lastSecondaryCandidate;
          }
        });
      }
    });
  }

  protected Control waitForIndexedField(final FieldType type, final int fieldIndex) {
    return waitUntil(new WaitCondition<Control>() {
      @Override
      public Control run() {
        return syncExec(new MockRunnable<Control>() {
          @Override
          public Control run() throws Throwable {
            List<Composite> parents = enumerateParentContainers();
            for (Composite parent : parents) {
              int index = 0;
              for (Control c : RwtUtility.findChildComponents(parent, Control.class)) {
                if (getFieldTypeOf(c) == type) {
                  if (index == fieldIndex) {
                    return c;
                  }
                  index++;
                }
              }
            }
            return null;
          }
        });
      }
    });
  }

  protected Control waitForLocatedField(final int x, final int y) {
    return waitUntil(new WaitCondition<Control>() {
      @Override
      public Control run() {
        return syncExec(new MockRunnable<Control>() {
          @Override
          public Control run() throws Throwable {
            List<Composite> parents = enumerateParentContainers();
            for (Composite parent : parents) {
              for (Control c : RwtUtility.findChildComponents(parent, Control.class, Composite.class)) {
                Rectangle map = getDisplay().map(c, null, c.getBounds());
                if (map.contains(x, y)) {
                  return c;
                }
              }
            }
            return null;
          }
        });
      }
    });
  }

  protected MenuItem waitForMenuItem(final String name) {
    return waitUntil(new WaitCondition<MenuItem>() {
      @Override
      public MenuItem run() {
        return syncExec(new MockRunnable<MenuItem>() {
          @Override
          public MenuItem run() throws Throwable {
            String label = cleanButtonLabel(name);
            //focus control
            Control focusControl = getDisplay().getFocusControl();
            if (focusControl != null) {
              Menu m = focusControl.getMenu();
              if (m != null) {
                for (MenuItem item : m.getItems()) {
                  if (label.equals(cleanButtonLabel(item.getText()))) {
                    return item;
                  }
                }
              }
            }
            //other controls
            for (Composite parent : enumerateParentContainers()) {
              for (Control c : RwtUtility.findChildComponents(parent, Control.class)) {
                Menu m = c.getMenu();
                if (m != null) {
                  for (MenuItem item : m.getItems()) {
                    if (label.equals(cleanButtonLabel(item.getText()))) {
                      return item;
                    }
                  }
                }
              }
            }
            //main menu
            for (Shell shell : getDisplay().getShells()) {
              Menu m = shell.getMenuBar();
              if (m != null) {
                for (MenuItem item : m.getItems()) {
                  if (label.equals(cleanButtonLabel(item.getText()))) {
                    return item;
                  }
                }
              }
            }
            return null;
          }
        });
      }
    });

  }

  protected <T> T syncExec(final MockRunnable<T> r) {
    if (getDisplay().getThread() != Thread.currentThread()) {
      final AtomicReference<T> ret = new AtomicReference<T>();
      final AtomicReference<Throwable> ex = new AtomicReference<Throwable>();
      try {
        getDisplay().syncExec(new Runnable() {
          @Override
          public void run() {
            try {
              ret.set(syncExec(r));
            }
            catch (Throwable t) {
              ex.set(t);
            }
          }
        });
        if (ex.get() != null) {
          throw ex.get();
        }
        return ret.get();
      }
      catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
    //
    try {
      return r.run();
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  protected <T> T waitUntil(final WaitCondition<T> w) {
    try {
      return TestingUtility.waitUntil(WAIT_TIMEOUT, w);
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
    finally {
      waitForIdle();
    }
  }

  @Override
  public void pressLeft() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void releaseLeft() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void gotoTreeExpandIcon(int treeIndex, String nodeText) {
    throw new UnsupportedOperationException("not implemented");
  }

  protected Keys toSeleniumKey(Key key) {
    switch (key) {
      case Shift:
        return Keys.SHIFT;
      case Control:
        return Keys.CONTROL;
      case Alt:
        return Keys.ALT;
      case Delete:
        return Keys.DELETE;
      case Backspace:
        return Keys.BACK_SPACE;
      case Enter:
        return Keys.ENTER;
      case Esc:
        return Keys.ESCAPE;
      case Tab:
        return Keys.TAB;
      case ContextMenu:
        throw new IllegalArgumentException("Unknown keyboard key: " + key);
      case Up:
        return Keys.UP;
      case Down:
        return Keys.DOWN;
      case Left:
        return Keys.LEFT;
      case Right:
        return Keys.RIGHT;
      case Windows:
        return Keys.META;
      case F1:
        return Keys.F1;
      case F2:
        return Keys.F2;
      case F3:
        return Keys.F3;
      case F4:
        return Keys.F4;
      case F5:
        return Keys.F5;
      case F6:
        return Keys.F6;
      case F7:
        return Keys.F7;
      case F8:
        return Keys.F8;
      case F9:
        return Keys.F9;
      case F10:
        return Keys.F10;
      case F11:
        return Keys.F11;
      case F12:
        return Keys.F12;
      case Home:
        return Keys.HOME;
      case End:
        return Keys.END;
      case PageUp:
        return Keys.PAGE_UP;
      case PageDown:
        return Keys.PAGE_DOWN;
      case NumPad0:
        return Keys.NUMPAD0;
      case NumPad1:
        return Keys.NUMPAD1;
      case NumPad2:
        return Keys.NUMPAD2;
      case NumPad3:
        return Keys.NUMPAD3;
      case NumPad4:
        return Keys.NUMPAD4;
      case NumPad5:
        return Keys.NUMPAD5;
      case NumPad6:
        return Keys.NUMPAD6;
      case NumPad7:
        return Keys.NUMPAD7;
      case NumPad8:
        return Keys.NUMPAD8;
      case NumPadMultiply:
        return Keys.MULTIPLY;
      case NumPadDivide:
        return Keys.DIVIDE;
      case NumPadAdd:
        return Keys.ADD;
      case NumPadSubtract:
        return Keys.SUBTRACT;
      case NumPadDecimal:
        return Keys.DECIMAL;
      case NumPadSeparator:
        return Keys.SEPARATOR;
      default:
        throw new IllegalArgumentException("Unknown keyboard key: " + key);
    }
  }

  protected int toKeyCode(IGuiMock.Key key) {
    switch (key) {
      case Shift:
        return KeyEvent.VK_SHIFT;
      case Control:
        return KeyEvent.VK_CONTROL;
      case Alt:
        return KeyEvent.VK_ALT;
      case Delete:
        return KeyEvent.VK_DELETE;
      case Backspace:
        return KeyEvent.VK_BACK_SPACE;
      case Enter:
        return KeyEvent.VK_ENTER;
      case Esc:
        return KeyEvent.VK_ESCAPE;
      case Tab:
        return KeyEvent.VK_TAB;
      case ContextMenu:
        return KeyEvent.VK_CONTEXT_MENU;
      case Up:
        return KeyEvent.VK_UP;
      case Down:
        return KeyEvent.VK_DOWN;
      case Left:
        return KeyEvent.VK_LEFT;
      case Right:
        return KeyEvent.VK_RIGHT;
      case Windows:
        return KeyEvent.VK_WINDOWS;
      case F1:
        return KeyEvent.VK_F1;
      case F2:
        return KeyEvent.VK_F2;
      case F3:
        return KeyEvent.VK_F3;
      case F4:
        return KeyEvent.VK_F4;
      case F5:
        return KeyEvent.VK_F5;
      case F6:
        return KeyEvent.VK_F6;
      case F7:
        return KeyEvent.VK_F7;
      case F8:
        return KeyEvent.VK_F8;
      case F9:
        return KeyEvent.VK_F9;
      case F10:
        return KeyEvent.VK_F10;
      case F11:
        return KeyEvent.VK_F11;
      case F12:
        return KeyEvent.VK_F12;
      case Home:
        return KeyEvent.VK_HOME;
      case End:
        return KeyEvent.VK_END;
      case PageUp:
        return KeyEvent.VK_PAGE_UP;
      case PageDown:
        return KeyEvent.VK_PAGE_DOWN;
      case NumPad0:
        return KeyEvent.VK_NUMPAD0;
      case NumPad1:
        return KeyEvent.VK_NUMPAD1;
      case NumPad2:
        return KeyEvent.VK_NUMPAD2;
      case NumPad3:
        return KeyEvent.VK_NUMPAD3;
      case NumPad4:
        return KeyEvent.VK_NUMPAD4;
      case NumPad5:
        return KeyEvent.VK_NUMPAD5;
      case NumPad6:
        return KeyEvent.VK_NUMPAD6;
      case NumPad7:
        return KeyEvent.VK_NUMPAD7;
      case NumPad8:
        return KeyEvent.VK_NUMPAD8;
      case NumPadMultiply:
        return KeyEvent.VK_MULTIPLY;
      case NumPadDivide:
        return KeyEvent.VK_DIVIDE;
      case NumPadAdd:
        return KeyEvent.VK_ADD;
      case NumPadSubtract:
        return KeyEvent.VK_SUBTRACT;
      case NumPadDecimal:
        return KeyEvent.VK_DECIMAL;
      case NumPadSeparator:
        return KeyEvent.VK_SEPARATOR;
      default:
        throw new IllegalArgumentException("Unknown keyboard key: " + key);
    }
  }

  @Override
  public void clickLeftOnSmartFieldMagnifier(FieldState fieldState) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void clickRightOnSmartFieldMagnifier(FieldState fieldState) {
    throw new UnsupportedOperationException("not implemented yet");
  }
}
