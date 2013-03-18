package org.eclipse.scout.rt.ui.swing.form.fields.browserfield.internal;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.RunnableWithException;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.PopupFactoryEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.browserfield.ISwingScoutBrowserField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SwingScoutBrowserField extends SwingScoutValueFieldComposite<IBrowserField> implements ISwingScoutBrowserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutBrowserField.class);

  private static Display swtDisplay;

  static {
    StaticDisplayDispatcher disp = new StaticDisplayDispatcher();
    disp.start();
    swtDisplay = disp.getDisplay();
  }

  private Shell m_swtShell;
  private Browser m_swtBrowser;
  private File m_tempDir;
  private String m_currentLocation;
  //
  private final List<RunnableWithException<?>> m_swtCommandQueue;
  private final Object m_swtCommandQueueLock;
  private P_CanvasEx m_canvas;
  private P_HierarchyListener m_hierarchyListener;
  private P_MouseEventListener m_mouseEventListener = null;

  public SwingScoutBrowserField() {
    m_swtCommandQueueLock = new Object();
    m_swtCommandQueue = new LinkedList<RunnableWithException<?>>();
  }

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    JPanelEx wordComponent = new JPanelEx();
    m_canvas = new P_CanvasEx();
    wordComponent.add(m_canvas);
    container.add(wordComponent);
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(wordComponent);
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  /**
   * The mouse listener is a workaround for Bugzilla 389786 and needs only to be installed when Java 7
   * is used.
   */
  private void installMouseListener() {
    if (SwingUtility.IS_JAVA_7_OR_GREATER) {
      // attach an event listener to fix focus problems with embedded word
      m_mouseEventListener = new P_MouseEventListener();
      m_mouseEventListener.setSwtShell(m_swtShell);
      Toolkit.getDefaultToolkit().addAWTEventListener(m_mouseEventListener, AWTEvent.MOUSE_EVENT_MASK);
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_hierarchyListener == null) {
      m_hierarchyListener = new P_HierarchyListener();
      m_canvas.addHierarchyListener(m_hierarchyListener);
    }
  }

  @Override
  protected void detachScout() {
    if (m_hierarchyListener != null) {
      m_canvas.removeHierarchyListener(m_hierarchyListener);
      m_hierarchyListener = null;
    }
    if (m_tempDir != null) {
      IOUtility.deleteDirectory(m_tempDir);
      m_tempDir = null;
    }
    super.detachScout();
  }

  private boolean isSwtAttached() {
    return m_swtBrowser != null;
  }

  private void attachSwtSafe() {
    if (isSwtAttached()) {
      return;
    }
    PopupFactoryEx.activate();
    try {
      // must be executed synchronously
      swtDisplay.syncExec(new Runnable() {
        @Override
        public void run() {
          try {
            m_swtShell = SWT_AWT.new_Shell(Display.getDefault(), m_canvas);
            m_swtBrowser = new Browser(m_swtShell, SWT.NONE);
            m_swtShell.setLayout(new FillLayout());
            runSwtCommandsInsideSwtThread();
            //add link listener
            m_swtBrowser.addLocationListener(new LocationAdapter() {
              @Override
              public void changing(LocationEvent event) {
                event.doit = fireBeforeLocationChangedFromSwt(event.location);
              }

              @Override
              public void changed(LocationEvent event) {
                fireAfterLocationChangedFromSwt(event.location);
              }
            });

            installMouseListener();
          }
          catch (Exception e) {
            LOG.error("Unexpected error occured while attaching Microsoft Word. All resources safely disposed.", e);
            detachSwtSafe();
          }
          finally {
            getSwingContainer().revalidate();
          }
        }
      });
    }
    catch (Exception e) {
      LOG.error("Error occured while attaching SWT.", e);
    }
  }

  private void detachSwtSafe() {
    if (!isSwtAttached()) {
      return;
    }
    PopupFactoryEx.deactivate();
    try {
      // must be executed synchronously
      swtDisplay.syncExec(new Runnable() {
        @Override
        public void run() {
          runSwtCommandsInsideSwtThread();
          synchronized (m_swtCommandQueueLock) {
            m_swtCommandQueue.clear();
          }
          DisposeUtil.disposeSafe(m_swtBrowser);
          m_swtBrowser = null;
          DisposeUtil.closeAndDisposeSafe(m_swtShell);
          m_swtShell = null;
        }
      });
    }
    catch (Throwable t) {
      LOG.error("Error occured while detaching SWT.", t);
    }
    finally {
      removeMouseListener();
    }
  }

  /**
   * The mouse listener is a workaround for Bugzilla 389786 and is only installed when Java 7 is used.
   * The listener is only removed if it was installed before.
   */
  private void removeMouseListener() {
    // now remove the event listener
    if (m_mouseEventListener != null) {
      Toolkit.getDefaultToolkit().removeAWTEventListener(m_mouseEventListener);
      m_mouseEventListener.setSwtShell(null);
      m_mouseEventListener = null;
    }
  }

  private void swtAsyncExec(final RunnableWithException<?> command) {
    synchronized (m_swtCommandQueueLock) {
      m_swtCommandQueue.add(command);
    }
    swtDisplay.asyncExec(new Runnable() {
      @Override
      public void run() {
        runSwtCommandsInsideSwtThread();
      }
    });
  }

  /**
   * commands are only be executed if COM and scout are attached
   */
  private void runSwtCommandsInsideSwtThread() {
    if (isSwtAttached() && getScoutObject() != null) {
      while (true) {
        synchronized (m_swtCommandQueueLock) {
          if (m_swtCommandQueue.isEmpty()) {
            break;
          }
          RunnableWithException r = m_swtCommandQueue.remove(0);
          try {
            r.run();
          }
          catch (Throwable e) {
            LOG.error("running command in COM", e);
          }
        }
      }
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (IBrowserField.PROP_LOCATION.equals(name)) {
      setLocationFromScout();
    }
  }

  @Override
  protected void setValueFromScout(Object o) {
    setLocationFromScout();
  }

  protected void setLocationFromScout() {
    String location = getScoutObject().getLocation();
    RemoteFile r = getScoutObject().getValue();
    if (location == null && r != null && r.exists()) {
      try {
        if (m_tempDir == null) {
          try {
            m_tempDir = IOUtility.createTempDirectory("html");
          }
          catch (ProcessingException e) {
            LOG.error("create temporary folder", e);
          }
        }
        if (r.getName().matches(".*\\.(zip|jar)")) {
          r.writeZipContentToDirectory(m_tempDir);
          String simpleName = r.getName().replaceAll("\\.(zip|jar)", ".htm");
          for (File f : m_tempDir.listFiles()) {
            if (f.getName().startsWith(simpleName)) {
              location = f.toURI().toURL().toExternalForm();
              break;
            }
          }
        }
        else {
          File f = new File(m_tempDir, r.getName());
          r.writeData(f);
          location = f.toURI().toURL().toExternalForm();
        }
      }
      catch (Throwable t) {
        LOG.error("preparing html content for " + r, t);
      }
    }
    m_currentLocation = location;
    //post the document to swt
    swtAsyncExec(new RunnableWithException<Object>() {
      @Override
      public Object run() throws Throwable {
        if (m_currentLocation != null) {
          getSwtBrowser().setUrl(m_currentLocation);
        }
        else {
          getSwtBrowser().setText("");
        }
        return null;
      }
    });
  }

  protected boolean fireBeforeLocationChangedFromSwt(final String location) {
    final AtomicReference<Boolean> accept = new AtomicReference<Boolean>();
    ClientSyncJob job = new ClientSyncJob("fireBeforeLocationChangedFromSwt", getSwingEnvironment().getScoutSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) {
        accept.set(getScoutObject().getUIFacade().fireBeforeLocationChangedFromUI(location));
      }
    };
    job.schedule();
    try {
      job.join(10000L);
    }
    catch (InterruptedException e) {
      // nop
    }
    return accept.get() != null ? accept.get().booleanValue() : false;
  }

  protected void fireAfterLocationChangedFromSwt(final String location) {
    ClientSyncJob job = new ClientSyncJob("fireAfterLocationChangedFromSwt", getSwingEnvironment().getScoutSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        getScoutObject().getUIFacade().fireAfterLocationChangedFromUI(location);
      }
    };
    job.schedule();
  }

  protected Browser getSwtBrowser() {
    return m_swtBrowser;
  }

  private class P_HierarchyListener implements HierarchyListener {

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
      if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED && (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && e.getChanged() == m_canvas) {
        if (e.getChanged().isDisplayable()) {
          attachSwtSafe();
        }
        else {
          detachSwtSafe();
        }
      }
    }
  }

  private class P_CanvasEx extends Canvas {

    private static final long serialVersionUID = 1L;

    @Override
    public void removeNotify() {
      // SWT control must be disposed prior to destroying SWING components.
      // Otherwise, disposal for OLE component does not work properly (e.g. WINWORD process is not terminated)
      detachSwtSafe();
      super.removeNotify();
    }
  }

  private static class StaticDisplayDispatcher extends Thread {
    private Display m_display;

    public StaticDisplayDispatcher() {
      super("SWT HTML Display Dispatcher");
      setDaemon(true);
    }

    @Override
    public void run() {
      m_display = Display.getDefault();
      if (m_display.getThread() == Thread.currentThread()) {
        while (!m_display.isDisposed()) {
          if (!m_display.readAndDispatch()) {
            m_display.sleep();
          }
        }
      }
    }

    Display getDisplay() {
      while (m_display == null) {
        try {
          Thread.sleep(100L);
        }
        catch (InterruptedException e) {
          //nop
        }
      }
      return m_display;
    }
  }

  /**
   * This listener is only added as a workaround since there is a focus problem
   * in the SWT_AWT bridge when using Java 7 which was reported in Bugzilla 389786
   * (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=389786).
   * This workaround should be removed if the ticket gets fixed. The workaround installs
   * an MouseEvent listener. Upon clicking on a Swing field, the SWT shell gets disabled
   * and re-enabled again which forces the focus to be released on the SWT field so that
   * the Swing field will regain the focus.
   */
  private class P_MouseEventListener implements AWTEventListener {

    private Shell m_swtShell;

    public void setSwtShell(Shell swtShell) {
      m_swtShell = swtShell;
    }

    @Override
    public void eventDispatched(AWTEvent e) {
      if (e.getID() == MouseEvent.MOUSE_CLICKED) {
        if (m_swtShell != null) {
          swtDisplay.syncExec(new Runnable() {
            @Override
            public void run() {
              if (m_swtShell.getEnabled()) {
                m_swtShell.setEnabled(false);
                m_swtShell.setEnabled(true);
              }
            }
          });
        }
      }
    }
  }

}
