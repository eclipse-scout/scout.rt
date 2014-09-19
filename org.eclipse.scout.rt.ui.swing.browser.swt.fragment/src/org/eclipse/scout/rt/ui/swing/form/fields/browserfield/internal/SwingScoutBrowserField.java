package org.eclipse.scout.rt.ui.swing.form.fields.browserfield.internal;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
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
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Browser-Field to display the native browser in a {@link Canvas} by using the SWT-AWT-Bridge.
 */
public class SwingScoutBrowserField extends SwingScoutValueFieldComposite<IBrowserField> implements ISwingScoutBrowserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutBrowserField.class);

  private Shell m_swtShell;
  private Browser m_swtBrowser;
  private File m_tempDir;

  private P_CanvasEx m_canvas;
  private P_HierarchyListener m_hierarchyListener;
  private P_MouseEventListener m_mouseEventListener;
  private SwtThread m_swtThread;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);

    //Don't create a label if not explicitly requested by the model. Necessary for backward compatibility, check will be removed in 3.9.0.
    if (ConfigurationUtility.isMethodOverwrite(AbstractFormField.class, "getConfiguredLabelVisible", null, getScoutObject().getClass())) {
      JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
      container.add(label);
      setSwingLabel(label);
    }

    JPanelEx wordComponent = new JPanelEx();
    m_canvas = new P_CanvasEx();
    wordComponent.add(m_canvas);
    container.add(wordComponent);
    setSwingContainer(container);
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

    // Create the SWT-Thread to interact with the browser widget.
    m_swtThread = new SwtThread();
    m_swtThread.start();

    // Defer the creation of the AWT-SWT-bridge until connected to the native screen resource. Otherwise, the OLE component cannot be initialized correctly.
    m_hierarchyListener = new P_HierarchyListener();
    m_canvas.addHierarchyListener(m_hierarchyListener);
  }

  @Override
  protected void detachScout() {
    m_canvas.removeHierarchyListener(m_hierarchyListener);

    if (m_tempDir != null) {
      IOUtility.deleteDirectory(m_tempDir);
      m_tempDir = null;
    }
    super.detachScout();
  }

  /**
   * @return <code>true</code> if the browser widget is created.
   */
  private boolean isSwtAttached() {
    return m_swtBrowser != null;
  }

  /**
   * Creates the SWT-AWT bridge to display the native browser in a {@link Canvas}. This call blocks until the widget is
   * created. This call has no effect if already attached to SWT.
   */
  private void attachSwtSafe() {
    if (isSwtAttached()) {
      return;
    }

    PopupFactoryEx.activate();

    final Display display = m_swtThread.getDisplay();
    display.syncExec(new Runnable() {
      @Override
      public void run() {
        try {
          m_swtShell = SWT_AWT.new_Shell(display, m_canvas);
          m_swtShell.setLayout(new FillLayout());

          m_swtBrowser = new Browser(m_swtShell, SWT.NONE);

          // Install Link and mouse listener.
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

          // Initialize the browser with the Scout model properties.
          IBrowserField scoutField = getScoutObject();

          // Determine the URL to be displayed. If an URL-location is set, the URL wins over the field's value.
          String url = null;
          if (scoutField.getLocation() != null) {
            url = scoutField.getLocation();
          }
          else if (scoutField.getValue() != null) {
            url = toRemoteFileUrl(scoutField.getValue());
          }

          if (url != null && !url.isEmpty()) {
            getSwtBrowser().setUrl(url);
          }
          else {
            getSwtBrowser().setText("");
          }
        }
        catch (RuntimeException e) {
          LOG.error("Failed to connect to SWT. All resources safely disposed.", e);
          detachSwtSafe();
        }
        finally {
          getSwingContainer().revalidate();
        }
      }
    });
  }

  private void detachSwtSafe() {
    if (!isSwtAttached()) {
      return;
    }
    PopupFactoryEx.deactivate();
    try {
      // Terminate the SWT UI-Thread and dispose allocated SWT resources.
      // Do not dispose the Shell and Browser in advance because of IE11 Patch KB2977629. Otherwise, the JVM might crash because of pending browser-events (Bugzilla 444427).
      m_swtThread.dispose();
      m_swtBrowser = null;
      m_swtShell = null;
    }
    catch (RuntimeException e) {
      LOG.error("Failed to disconnect from SWT.", e);
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
    if (m_mouseEventListener != null) {
      Toolkit.getDefaultToolkit().removeAWTEventListener(m_mouseEventListener);
      m_mouseEventListener.setSwtShell(null);
      m_mouseEventListener = null;
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (IBrowserField.PROP_LOCATION.equals(name)) {
      setLocationFromScout();
    }
  }

  protected void setLocationFromScout() {
    setLocationInternal(getScoutObject().getLocation());
  }

  @Override
  protected void setValueFromScout(Object value) {
    setLocationInternal(toRemoteFileUrl(getScoutObject().getValue()));
  }

  /**
   * Provides an URL for the given {@link RemoteFile} to be displayed in the browser. If the file represents an archive,
   * the archive content is looked for an HTML file of the same name (e.g. archive=test.zip, html=test.html).
   * 
   * @param remoteFile
   *          {@link RemoteFile}.
   * @return the URL or <code>null</code> if not applicable.
   */
  protected String toRemoteFileUrl(RemoteFile remoteFile) {
    if (remoteFile == null || !remoteFile.exists()) {
      return null;
    }

    try {
      if (m_tempDir == null) {
        try {
          m_tempDir = IOUtility.createTempDirectory("html");
        }
        catch (ProcessingException e) {
          LOG.error("Failed to create temporary folder for the content to be displayed in the browser.", e);
          return null;
        }
      }

      if (remoteFile.getName().matches(".*\\.(zip|jar)")) {
        remoteFile.writeZipContentToDirectory(m_tempDir);
        String simpleName = remoteFile.getName().replaceAll("\\.(zip|jar)", ".htm");
        for (File f : m_tempDir.listFiles()) {
          if (f.getName().startsWith(simpleName)) {
            return f.toURI().toURL().toExternalForm();
          }
        }
        return null;
      }
      else {
        File f = new File(m_tempDir, remoteFile.getName());
        remoteFile.writeData(f);
        return f.toURI().toURL().toExternalForm();
      }
    }
    catch (Exception e) {
      LOG.error(String.format("Failed to prepare HTML content to be displayed in the browser [remoteFile=%s]", remoteFile), e);
      return null;
    }
  }

  protected void setLocationInternal(final String location) {
    if (!isSwtAttached()) {
      return;
    }

    m_swtThread.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (location != null && !location.isEmpty()) {
          getSwtBrowser().setUrl(location);
        }
        else {
          getSwtBrowser().setText("");
        }
      }
    });
  }

  protected boolean fireBeforeLocationChangedFromSwt(final String location) {
    final AtomicReference<Boolean> accept = new AtomicReference<Boolean>(false);
    ClientJob job = new ClientSyncJob("fireBeforeLocationChangedFromSwt", getSwingEnvironment().getScoutSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) {
        accept.set(getScoutObject().getUIFacade().fireBeforeLocationChangedFromUI(location));
      }
    };
    job.schedule();
    try {
      job.join(TimeUnit.SECONDS.toMillis(10));
      return accept.get().booleanValue();
    }
    catch (InterruptedException e) {
      LOG.warn("Failed to wait for the Scout model to accept a location change.", e);
      return false;
    }
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

  /**
   * @return the SWT browser widget or <code>null</code> if not attached to SWT yet.
   * @see #isSwtAttached()
   */
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
      }
    }
  }

  private class P_CanvasEx extends Canvas {

    private static final long serialVersionUID = 1L;

    @Override
    public void removeNotify() {
      // SWT control must be disposed prior to destroying SWING components.
      // Otherwise, disposal for OLE component does not work properly (e.g. browser process is not terminated)
      detachSwtSafe();
      super.removeNotify();
    }
  }

  /**
   * SWT UI-Thread to interact with the browser widget.
   */
  private static class SwtThread extends Thread {
    private Display m_display;
    private final Object m_displayLock = new Object();

    private static final long DISPLAY_WAIT_TIMEOUT = 15;

    public SwtThread() {
      super("SWT Browser Thread");
    }

    @Override
    public void run() {
      // Create a dedicated Display for this UI-Thread (Bugzilla 389786). That is why Display#getDefault cannot be used.
      synchronized (Device.class) { // synchronization to ensure exclusive Display access (see Display#getDefault).
        m_display = new Display();

        // Prevent this Display from being registered as Default-Display (see Display#create(DeviceData). Otherwise, that Display would be used by other widgets as well.
        if (m_display == Display.getDefault()) {
          try {
            Field f_default = Display.class.getDeclaredField("Default");
            f_default.setAccessible(true);
            f_default.set(null, null);
          }
          catch (Exception e) {
            LOG.error(String.format("Failed to unregister the Display to not be the global Display [display=%s]", m_display), e);
          }
        }
      }

      // Notify waiting callers to access the Display.
      synchronized (m_displayLock) {
        m_displayLock.notifyAll();
      }

      try {
        // The SWT-Thread is running as long the display is not disposed.
        while (!m_display.isDisposed()) {
          if (!m_display.readAndDispatch()) {
            m_display.sleep();
          }
        }
        m_display = null;
      }
      catch (RuntimeException e) {
        LOG.error("Failed to dispatch work to the SWT UI-Thread.", e);
      }
    }

    /**
     * Terminates the SWT UI-Thread and disposes allocated SWT resources.
     */
    public void dispose() {
      m_display.syncExec(new Runnable() {

        @Override
        public void run() {
          m_display.dispose(); // By disposing the Display, the UI-Thread stops the event dispatching and terminates himself.
        }
      });
    }

    /**
     * @return The {@link Display} of the SWT UI-Thread. If not available yet, the caller is blocked for maximal 15
     *         seconds until the SWT-Thread created its {@link Display}; is never <code>null</code>.
     * @throws IllegalStateException
     *           if the {@link Display} could not be acquired within 15 seconds.
     */
    Display getDisplay() {
      final long threshold = System.nanoTime() + TimeUnit.SECONDS.toNanos(DISPLAY_WAIT_TIMEOUT);
      while (m_display == null && System.nanoTime() < threshold) {
        try {
          synchronized (m_displayLock) {
            if (m_display == null) {
              m_displayLock.wait(TimeUnit.SECONDS.toMillis(DISPLAY_WAIT_TIMEOUT));
            }
          }
        }
        catch (InterruptedException e) {
          throw new RuntimeException("Interrupted while waiting for the Display to be created.", e);
        }
      }

      if (m_display == null) {
        throw new IllegalStateException("SWT Display could not be acquired.");
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
          m_swtThread.getDisplay().syncExec(new Runnable() {
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
