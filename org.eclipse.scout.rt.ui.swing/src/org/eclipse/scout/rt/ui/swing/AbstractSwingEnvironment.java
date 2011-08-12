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

import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.scout.commons.CSSPatch;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.concurrency.SwingScoutSynchronizer;
import org.eclipse.scout.rt.ui.swing.ext.IEmbeddedFrameProviderService;
import org.eclipse.scout.rt.ui.swing.ext.JFrameEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.focus.SwingScoutFocusTraversalPolicy;
import org.eclipse.scout.rt.ui.swing.form.ISwingScoutForm;
import org.eclipse.scout.rt.ui.swing.form.SwingScoutForm;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.OnFieldLabelDecorator;
import org.eclipse.scout.rt.ui.swing.form.fields.tabbox.ISwingScoutTabItem;
import org.eclipse.scout.rt.ui.swing.form.fields.tabbox.SwingScoutTabItem;
import org.eclipse.scout.rt.ui.swing.inject.AppendActionsInjector;
import org.eclipse.scout.rt.ui.swing.inject.CreateActionInjector;
import org.eclipse.scout.rt.ui.swing.inject.InitLookAndFeelInjector;
import org.eclipse.scout.rt.ui.swing.inject.UIDefaultsInjector;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutBoundsProvider;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutView;
import org.eclipse.scout.rt.ui.swing.window.SwingWindowManager;
import org.eclipse.scout.rt.ui.swing.window.desktop.ISwingScoutDesktop;
import org.eclipse.scout.rt.ui.swing.window.desktop.ISwingScoutRootFrame;
import org.eclipse.scout.rt.ui.swing.window.desktop.SwingScoutDesktop;
import org.eclipse.scout.rt.ui.swing.window.desktop.SwingScoutRootFrame;
import org.eclipse.scout.rt.ui.swing.window.desktop.layout.MultiSplitLayoutConstraints;
import org.eclipse.scout.rt.ui.swing.window.desktop.tray.ISwingScoutTray;
import org.eclipse.scout.rt.ui.swing.window.desktop.tray.SwingScoutTray;
import org.eclipse.scout.rt.ui.swing.window.dialog.SwingScoutDialog;
import org.eclipse.scout.rt.ui.swing.window.filechooser.ISwingScoutFileChooser;
import org.eclipse.scout.rt.ui.swing.window.filechooser.SwingScoutFileChooser;
import org.eclipse.scout.rt.ui.swing.window.frame.SwingScoutFrame;
import org.eclipse.scout.rt.ui.swing.window.internalframe.SwingScoutInternalFrame;
import org.eclipse.scout.rt.ui.swing.window.messagebox.ISwingScoutMessageBox;
import org.eclipse.scout.rt.ui.swing.window.messagebox.SwingScoutMessageBox;
import org.eclipse.scout.rt.ui.swing.window.popup.SwingScoutPopup;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractSwingEnvironment implements ISwingEnvironment {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSwingEnvironment.class);

  private static enum BusyStatus {
    IDLE, DETECTING, BUSY
  }

  private boolean m_initialized;
  private IClientSession m_scoutSession;
  private SwingScoutSynchronizer m_synchronizer;
  private SwingIconLocator m_iconLocator;
  private ISwingScoutTray m_trayComposite;
  private BusyStatus m_busyStatus;
  private Frame m_rootFrame;
  private FormFieldFactory m_formFieldFactory;
  private ISwingScoutRootFrame m_rootComposite;
  private PropertyChangeSupport m_propertySupport = new PropertyChangeSupport(this);
  private WeakHashMap<IForm, ISwingScoutForm> m_standaloneFormComposites;
  private Component m_popupOwner;
  private Rectangle m_popupOwnerBounds;
  private final Object m_immediateSwingJobsLock = new Object();
  private final List<Runnable> m_immediateSwingJobs = new ArrayList<Runnable>();

  public AbstractSwingEnvironment() {
    checkThread();
    m_standaloneFormComposites = new WeakHashMap<IForm, ISwingScoutForm>();
    init();
  }

  public void init() {
    checkThread();
    if (!m_initialized) {
      m_initialized = true;
      // disable direct draw (see
      // http://java.sun.com/j2se/1.5.0/docs/guide/2d/flags.html#noddraw)
      System.setProperty("sun.java2d.noddraw", "true");
      System.getProperty("sun.java2d.noddraw", "true");// read to make it
      // only system properties are visible inside javax.swing
      initLookAndFeel(System.getProperties());
      interceptUIDefaults(UIManager.getDefaults());
      CSSPatch.apply();
      m_rootFrame = createRootFrame();
      if (m_rootFrame != null) {
        setWindowIcon(m_rootFrame);
      }
      m_synchronizer = new SwingScoutSynchronizer(this);
      // add job manager listener for busy handling
      Job.getJobManager().addJobChangeListener(new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent e) {
          // ignore double-check jobs
          if (e.getJob().getName().equals("JobChangeAdapter, double-check")) {
            return;
          }
          if (!Job.getJobManager().isIdle()) {
            for (Job j : Job.getJobManager().find(ClientJob.class)) {
              if (j instanceof ClientJob) {
                ClientJob c = (ClientJob) j;
                if (c.isSync() && !c.isWaitFor()) {
                  // there is a running job, still busy
                  return;
                }
              }
            }
          }
          // idle
          if (m_busyStatus == BusyStatus.BUSY || m_busyStatus == BusyStatus.DETECTING) {
            // check whether the job queue is still idle in 100ms
            Job j = new Job("JobChangeAdapter, double-check") {
              @Override
              protected IStatus run(IProgressMonitor m) {
                if (!Job.getJobManager().isIdle()) {
                  for (Job runningJob : Job.getJobManager().find(ClientJob.class)) {
                    if (runningJob instanceof ClientJob) {
                      ClientJob c = (ClientJob) runningJob;
                      if (c.isSync() && !c.isWaitFor()) {
                        //there is a running job, still busy
                        return Status.OK_STATUS;
                      }
                    }
                  }
                }
                if (m_busyStatus == BusyStatus.BUSY || m_busyStatus == BusyStatus.DETECTING) {
                  SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                      setBusyInternal(BusyStatus.IDLE);
                    }
                  });
                }
                return Status.OK_STATUS;
              }
            };
            j.setSystem(true);
            j.schedule(100);
          }
        }
      });
    }
  }

  /**
   * Initialize the Swing look and feel.
   * Three styles are supported: Scout, Synth (Swing), and Default (Swing)
   * <p>
   * Properties: scout.laf, javax.swing.plaf.synth.style, swing.defaultlaf
   */
  protected void initLookAndFeel(Properties initProperties) {
    new InitLookAndFeelInjector().inject(initProperties);
  }

  public Icon getIcon(String name) {
    return m_iconLocator.getIcon(name);
  }

  public Image getImage(String name) {
    return m_iconLocator.getImage(name);
  }

  public List<Image> getImages(String... names) {
    List<Image> result = new ArrayList<Image>();
    if (names != null) {
      for (String name : names) {
        if (name != null) {
          Image img = getImage(name);
          if (img != null) {
            result.add(img);
          }
        }
      }
    }
    return result;
  }

  /**
   * Sets the icon of the specified window (e.g. used in the Task bar, Title bar, etc.).
   * If 'legacyIcon' is not null, the specified image is used. If (and only if) 'legacyIcon'
   * is null, the 'icons' list is used. The operating system can then choose the best
   * matching image out of this list (usually, you put different sizes of icons in this list).
   */
  protected void setWindowIcon(Window window) {
    // legacy
    Image legacyIcon = Activator.getImage("window");
    if (legacyIcon != null) {
      window.setIconImage(legacyIcon);
    }
    else {
      ArrayList<Image> icons = new ArrayList<Image>();
      for (String name : new String[]{"window16", "window32", "window48", "window256"}) {
        if (name != null) {
          Image img = Activator.getImage(name);
          if (img != null) {
            icons.add(img);
          }
        }
      }
      window.setIconImages(icons);
    }
  }

  protected ISwingScoutTray createTray(IDesktop desktop) {
    SwingScoutTray ui = new SwingScoutTray();
    ui.createField(desktop, this);
    decorate(desktop, ui);
    return ui;
  }

  public ISwingScoutTray getTrayComposite() {
    return m_trayComposite;
  }

  public int getFormColumnWidth() {
    return 360;
  }

  @Override
  public int getFormColumnGap() {
    return 12;
  }

  public int getFormRowHeight() {
    return 23;
  }

  @Override
  public int getFormRowGap() {
    return 6;
  }

  public int getFieldLabelWidth() {
    return 130;
  }

  @Override
  public int getProcessButtonHeight() {
    return 28;
  }

  public int getIconButtonSize() {
    return 23;
  }

  public int getDropDownButtonWidth() {
    return 35;
  }

  public void interceptUIDefaults(UIDefaults defaults) {
    new UIDefaultsInjector().inject(defaults);
  }

  public Frame getRootFrame() {
    checkThread();
    return m_rootFrame;
  }

  public ISwingScoutRootFrame getRootComposite() {
    return m_rootComposite;
  }

  /**
   * Is called before desktop is displayed
   * 
   * @param clientSession
   * @return true to start desktop or false to exit application
   * @throws Exception
   */
  protected boolean execBeforeDesktop(IClientSession clientSession) throws Exception {
    return true;
  }

  public void showGUI(IClientSession session) {
    checkThread();
    m_scoutSession = session;
    //set global text provider
    SwingUtility.setNlsTexts(m_scoutSession.getNlsTexts());
    if (m_rootFrame == null) {
      m_scoutSession.stopSession();
      return;
    }
    m_iconLocator = createIconLocator();
    try {
      if (!execBeforeDesktop(session)) {
        System.exit(0);
      }
    }
    catch (Exception e) {
      LOG.error("GUI initialization failed", e);
      System.exit(0);
    }
    final IDesktop desktop = m_scoutSession.getDesktop();
    if (desktop != null) {
      m_scoutSession.getDesktop().addDesktopListener(new DesktopListener() {
        public void desktopChanged(final DesktopEvent e) {
          switch (e.getType()) {
            case DesktopEvent.TYPE_FORM_ADDED: {
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  showStandaloneForm(getRootFrame(), e.getForm());
                }
              };
              invokeSwingLater(t);
              break;
            }
            case DesktopEvent.TYPE_FORM_REMOVED: {
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  hideStandaloneForm(e.getForm());
                }
              };
              invokeSwingLater(t);
              break;
            }
            case DesktopEvent.TYPE_FORM_ENSURE_VISIBLE: {
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  activateStandaloneForm(e.getForm());
                }
              };
              invokeSwingLater(t);
              break;
            }
            case DesktopEvent.TYPE_MESSAGE_BOX_ADDED: {
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  showMessageBox(getRootFrame(), e.getMessageBox());
                }
              };
              invokeSwingLater(t);
              break;
            }
            case DesktopEvent.TYPE_FILE_CHOOSER_ADDED: {
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  showFileChooser(getRootFrame(), e.getFileChooser());
                }
              };
              invokeSwingLater(t);
              break;
            }
            case DesktopEvent.TYPE_FIND_FOCUS_OWNER: {
              final Object lock = new Object();
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  try {
                    IFormField f = findFocusOwnerField();
                    if (f != null) {
                      e.setFocusedField(f);
                    }
                  }
                  finally {
                    synchronized (lock) {
                      lock.notifyAll();
                    }
                  }
                }
              };
              synchronized (lock) {
                invokeSwingLater(t);
                try {
                  lock.wait(2000L);
                }
                catch (InterruptedException e1) {
                  //nop
                }
              }
              break;
            }
          }
        }
      });

      m_rootComposite = createRootComposite(getRootFrame(), m_scoutSession.getDesktop());
      m_rootComposite.showSwingFrame();
      // load state of views
      for (IForm f : desktop.getViewStack()) {
        if (f.isAutoAddRemoveOnDesktop()) {
          showStandaloneForm(getRootFrame(), f);
        }
      }
      //tray icon
      if (desktop.isTrayVisible()) {
        m_trayComposite = createTray(desktop);
      }
      // dialogs
      for (IForm f : desktop.getDialogStack()) {
        if (f.isAutoAddRemoveOnDesktop()) {
          showStandaloneForm(getRootFrame(), f);
        }
      }
      // messageboxes
      for (IMessageBox mb : desktop.getMessageBoxStack()) {
        showMessageBox(getRootFrame(), mb);
      }
      // notify desktop that it is loaded
      new ClientSyncJob("Desktop opened", session) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          desktop.getUIFacade().fireGuiAttached();
          desktop.getUIFacade().fireDesktopOpenedFromUI();
        }
      }.schedule();
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  public IClientSession getScoutSession() {
    return m_scoutSession;
  }

  public boolean isBusy() {
    return m_busyStatus == BusyStatus.BUSY;
  }

  public void setBusyFromSwing(boolean b) {
    checkThread();
    if (b) {
      setBusyInternal(BusyStatus.DETECTING);
      Job j = new Job("Busy in 200ms") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              if (m_busyStatus == BusyStatus.DETECTING) {
                setBusyInternal(BusyStatus.BUSY);
              }
            }
          });
          return Status.OK_STATUS;
        }
      };
      j.setSystem(true);
      j.schedule(300);
    }
  }

  /*
   * must be called in swing thread
   */
  private void setBusyInternal(BusyStatus status) {
    checkThread();
    if (m_busyStatus != status) {
      boolean oldValue = m_busyStatus == BusyStatus.BUSY;
      m_busyStatus = status;
      boolean newValue = m_busyStatus == BusyStatus.BUSY;
      m_propertySupport.firePropertyChange(PROP_BUSY, oldValue, newValue);
    }
  }

  @Override
  public void postImmediateSwingJob(Runnable r) {
    synchronized (m_immediateSwingJobsLock) {
      m_immediateSwingJobs.add(r);
    }
  }

  @Override
  public void dispatchImmediateSwingJobs() {
    List<Runnable> list;
    synchronized (m_immediateSwingJobsLock) {
      list = new ArrayList<Runnable>(m_immediateSwingJobs);
      m_immediateSwingJobs.clear();
    }
    for (Runnable r : list) {
      try {
        r.run();
      }
      catch (Throwable t) {
        LOG.warn("running " + r, t);
      }
    }
  }

  public JobEx invokeScoutLater(Runnable j, long cancelTimeout) {
    synchronized (m_immediateSwingJobsLock) {
      m_immediateSwingJobs.clear();
    }
    return m_synchronizer.invokeScoutLater(j, cancelTimeout);
  }

  public void invokeSwingLater(Runnable j) {
    m_synchronizer.invokeSwingLater(j);
  }

  /*
   * Factory
   */
  protected SwingIconLocator createIconLocator() {
    return new SwingIconLocator(getScoutSession().getIconLocator());
  }

  @Override
  public Frame createRootFrame() {
    Frame rootFrame = null;
    /*
     * support for (browser) embedded applications using a hWnd
     */
    long hWnd = 0;
    Pattern pat = Pattern.compile("hWnd=([-]?[0-9]+)");
    for (String s : Platform.getApplicationArgs()) {
      Matcher m = pat.matcher(s.trim());
      if (m.matches()) {
        hWnd = Long.parseLong(m.group(1));
        break;
      }
    }
    if (hWnd != 0) {
      IEmbeddedFrameProviderService es = SERVICES.getService(IEmbeddedFrameProviderService.class);
      if (es != null) {
        try {
          rootFrame = es.createEmbeddedFrame(hWnd);
          rootFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
              m_rootFrame = null;
              if (m_scoutSession != null) {
                m_scoutSession.stopSession();
              }
            }
          });
        }
        catch (Throwable t) {
          LOG.error("create embedded frame using " + es.getClass(), t);
        }
      }
    }
    if (rootFrame == null) {
      rootFrame = new JFrameEx();
      ((JFrameEx) rootFrame).getRootPane().setName("Synth.Frame");
      ((JFrameEx) rootFrame).setAutoCorrectSize(false);
    }
    return rootFrame;
  }

  public ISwingScoutRootFrame createRootComposite(Frame rootFrame, IDesktop desktop) {
    ISwingScoutRootFrame ui = new SwingScoutRootFrame(rootFrame);
    ui.createField(desktop, this);
    decorate(desktop, ui);
    return ui;
  }

  protected void decorate(Object scoutObject, Object swingScoutComposite) {

  }

  public ISwingScoutDesktop createDesktop(Window owner, IDesktop desktop) {
    ISwingScoutDesktop ui = new SwingScoutDesktop();
    ui.createField(desktop, this);
    decorate(desktop, ui);
    return ui;
  }

  public void showStandaloneForm(Component parent, IForm f) {
    if (f == null) {
      return;
    }
    ISwingScoutView view = null;
    ISwingScoutForm formComposite = m_standaloneFormComposites.get(f);
    if (formComposite != null) {
      view = formComposite.getView();
    }
    if (view != null) {
      return;
    }
    //
    Window w = null;
    if (w == null) {
      w = SwingWindowManager.getInstance().getActiveModalDialog();
    }
    if (w == null) {
      w = SwingWindowManager.getInstance().getActiveWindow();
    }
    if (w == null) {
      if (parent instanceof Window) {
        w = (Window) parent;
      }
      else {
        w = SwingUtilities.getWindowAncestor(parent);
      }
    }
    //
    switch (f.getDisplayHint()) {
      case IForm.DISPLAY_HINT_DIALOG: {
        if (f.isModal()) {
          view = createDialog(w, f);
        }
        else {
          view = createFrame(w, f);
        }
        break;
      }
      case IForm.DISPLAY_HINT_VIEW: {
        Object constraints = getViewLayoutConstraintsFor(f);
        view = createView(constraints, f);
        break;
      }
      case IForm.DISPLAY_HINT_POPUP_WINDOW: {
        view = createPopupWindow(w, f);
        if (view == null) {
          LOG.error("showing popup for " + f + ", but there is neither a focus owner nor the property 'ISwingEnvironment.getPopupOwner()'");
          return;
        }
        break;
      }
      case IForm.DISPLAY_HINT_POPUP_DIALOG: {
        view = createPopupDialog(w, f);
        if (view == null) {
          LOG.error("showing popup for " + f + ", but there is neither a focus owner nor the property 'ISwingEnvironment.getPopupOwner()'");
          return;
        }
        break;
      }
    }
    if (view != null) {
      formComposite = createForm(view, f);
      m_standaloneFormComposites.put(f, formComposite);
      view.openView();
    }
  }

  public void hideStandaloneForm(IForm f) {
    if (f != null) {
      ISwingScoutForm formComposite = m_standaloneFormComposites.remove(f);
      if (formComposite != null) {
        ISwingScoutView viewComposite = formComposite.getView();
        formComposite.detachSwingView();
        if (viewComposite != null) {
          viewComposite.closeView();
        }
      }
    }
  }

  public ISwingScoutForm[] getStandaloneFormComposites() {
    ArrayList<ISwingScoutForm> list = new ArrayList<ISwingScoutForm>();
    for (ISwingScoutForm f : m_standaloneFormComposites.values()) {
      if (f != null) {
        list.add(f);
      }
    }
    return list.toArray(new ISwingScoutForm[list.size()]);
  }

  public ISwingScoutForm getStandaloneFormComposite(IForm f) {
    if (f != null) {
      ISwingScoutForm formComposite = m_standaloneFormComposites.get(f);
      return formComposite;
    }
    return null;
  }

  public void activateStandaloneForm(IForm f) {
    if (f != null) {
      ISwingScoutForm formComposite = m_standaloneFormComposites.get(f);
      if (formComposite != null) {
        RootPaneContainer root = (RootPaneContainer) SwingUtilities.getAncestorOfClass(RootPaneContainer.class, formComposite.getSwingFormPane());
        if (root instanceof JInternalFrame) {
          try {
            ((JInternalFrame) root).setSelected(true);
          }
          catch (PropertyVetoException e) {
          }
        }
        else if (root instanceof Window) {
          ((Window) root).requestFocusInWindow();
        }
      }
    }
  }

  public Object getViewLayoutConstraintsFor(IForm f) {
    String viewId = f.getDisplayViewId();
    return getViewLayoutConstraintsFor(viewId);
  }

  public Object getViewLayoutConstraintsFor(String viewId) {
    // begin mapping
    if (IForm.VIEW_ID_OUTLINE_SELECTOR.equalsIgnoreCase(viewId)) {
      viewId = IForm.VIEW_ID_SW;
    }
    else if (IForm.VIEW_ID_OUTLINE.equalsIgnoreCase(viewId)) {
      viewId = IForm.VIEW_ID_NW;
    }
    else if (IForm.VIEW_ID_PAGE_DETAIL.equalsIgnoreCase(viewId)) {
      viewId = IForm.VIEW_ID_N;
    }
    else if (IForm.VIEW_ID_PAGE_TABLE.equalsIgnoreCase(viewId)) {
      viewId = IForm.VIEW_ID_CENTER;
    }
    else if (IForm.VIEW_ID_PAGE_SEARCH.equalsIgnoreCase(viewId)) {
      viewId = IForm.VIEW_ID_S;
    }
    // end mapping
    if (viewId == null) {
      return new MultiSplitLayoutConstraints(1, 1, 0, new float[]{10, 10, 10, 10, 10, 10, 10, 10, 10});
    }
    else if (viewId.equalsIgnoreCase(IForm.VIEW_ID_N)) {
      return new MultiSplitLayoutConstraints(0, 1, 40, new float[]{7, 9, 7, 6, 6, 5, 4, 3, 4});
    }
    else if (viewId.equalsIgnoreCase(IForm.VIEW_ID_NE)) {
      return new MultiSplitLayoutConstraints(0, 2, 70, new float[]{0, 0, 9, 0, 0, 8, 0, 0, 5});
    }
    else if (viewId.equalsIgnoreCase(IForm.VIEW_ID_E)) {
      return new MultiSplitLayoutConstraints(1, 2, 80, new float[]{0, 0, 8, 0, 0, 9, 0, 0, 8});
    }
    else if (viewId.equalsIgnoreCase(IForm.VIEW_ID_SE)) {
      return new MultiSplitLayoutConstraints(2, 2, 90, new float[]{0, 0, 5, 0, 0, 7, 0, 0, 9});
    }
    else if (viewId.equalsIgnoreCase(IForm.VIEW_ID_S)) {
      return new MultiSplitLayoutConstraints(2, 1, 60, new float[]{5, 3, 4, 5, 5, 4, 6, 9, 7});
    }
    else if (viewId.equalsIgnoreCase(IForm.VIEW_ID_SW)) {
      return new MultiSplitLayoutConstraints(2, 0, 10, new float[]{0, 0, 0, 0, 0, 0, 9, 0, 0});
    }
    else if (viewId.equalsIgnoreCase(IForm.VIEW_ID_W)) {
      return new MultiSplitLayoutConstraints(1, 0, 20, new float[]{8, 0, 0, 9, 0, 0, 8, 0, 0});
    }
    else if (viewId.equalsIgnoreCase(IForm.VIEW_ID_NW)) {
      return new MultiSplitLayoutConstraints(0, 0, 30, new float[]{9, 0, 0, 8, 0, 0, 7, 0, 0});
    }
    else if (viewId.equalsIgnoreCase(IForm.VIEW_ID_CENTER)) {
      return new MultiSplitLayoutConstraints(1, 1, 50, new float[]{6, 6, 6, 7, 9, 6, 5, 6, 6});
    }
    else {
      LOG.warn("unexpected viewId \"" + viewId + "\"");
      return new MultiSplitLayoutConstraints(1, 1, 50, new float[]{6, 6, 6, 7, 9, 6, 6, 6, 6});
    }
  }

  public IFormField findFocusOwnerField() {
    Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    while (comp instanceof JComponent) {
      IPropertyObserver o = SwingScoutComposite.getScoutModelOnWidget(comp);
      if (o instanceof IFormField) {
        return (IFormField) o;
      }
      // next
      comp = comp.getParent();
    }
    return null;
  }

  public void showMessageBox(Component parent, IMessageBox mb) {
    Window w = null;
    if (w == null) {
      w = SwingWindowManager.getInstance().getActiveModalDialog();
    }
    if (w == null) {
      w = SwingWindowManager.getInstance().getActiveWindow();
    }
    if (w == null) {
      if (parent instanceof Window) {
        w = (Window) parent;
      }
      else {
        w = SwingUtilities.getWindowAncestor(parent);
      }
    }
    ISwingScoutMessageBox mbox = createMessageBox(w, mb);
    mbox.showSwingMessageBox();
  }

  public ISwingScoutMessageBox createMessageBox(Window w, IMessageBox mb) {
    ISwingScoutMessageBox ui = new SwingScoutMessageBox(w);
    ui.createField(mb, this);
    ui.setName("Synth.Dialog");
    decorate(mb, ui);
    return ui;
  }

  public void showFileChooser(Component parent, IFileChooser fc) {
    Window w = null;
    if (w == null) {
      w = SwingWindowManager.getInstance().getActiveModalDialog();
    }
    if (w == null) {
      w = SwingWindowManager.getInstance().getActiveWindow();
    }
    if (w == null) {
      if (parent instanceof Window) {
        w = (Window) parent;
      }
      else {
        w = SwingUtilities.getWindowAncestor(parent);
      }
    }
    ISwingScoutFileChooser sfc = createFileChooser(w, fc);
    sfc.showFileChooser();
  }

  public ISwingScoutFileChooser createFileChooser(Window w, IFileChooser fc) {
    ISwingScoutFileChooser ui = new SwingScoutFileChooser(this, fc, w, true);
    decorate(fc, ui);
    return ui;
  }

  public ISwingScoutFormField createFormField(JComponent parent, IFormField field) {
    if (m_formFieldFactory == null) {
      m_formFieldFactory = new FormFieldFactory();
    }
    ISwingScoutFormField ui = m_formFieldFactory.createFormField(parent, field, this);
    decorate(field, ui);
    return ui;
  }

  public ISwingScoutView createDialog(Window owner, IForm form) {
    ISwingScoutBoundsProvider boundsProvider = null;
    if (form.isCacheBounds()) {
      boundsProvider = new P_FormBoundsProvider(form);
    }
    ISwingScoutView ui = new SwingScoutDialog(this, owner, boundsProvider);
    ui.setName("Synth.Dialog");
    Window w = SwingUtilities.getWindowAncestor(ui.getSwingContentPane());
    if (w != null) {
      setWindowIcon(w);
    }
    decorate(form, ui);
    return ui;
  }

  public ISwingScoutView createFrame(Window owner, IForm form) {
    ISwingScoutBoundsProvider boundsProvider = null;
    if (form.isCacheBounds()) {
      boundsProvider = new P_FormBoundsProvider(form);
    }
    ISwingScoutView ui = new SwingScoutFrame(this, boundsProvider);
    ui.setName("Synth.Frame");
    Window w = SwingUtilities.getWindowAncestor(ui.getSwingContentPane());
    if (w != null) {
      setWindowIcon(w);
    }
    decorate(form, ui);
    return ui;
  }

  public ISwingScoutView createPopupDialog(Window parentWindow, IForm form) {
    Component owner = getPopupOwner();
    if (owner == null) {
      owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    }
    if (owner == null) {
      return null;
    }
    Rectangle ownerBounds = getPopupOwnerBounds();
    if (ownerBounds == null) {
      ownerBounds = owner.getBounds();
    }
    final Rectangle ownerBoundsFinal = ownerBounds;
    ISwingScoutBoundsProvider boundsProvider = new ISwingScoutBoundsProvider() {
      @Override
      public Rectangle getBounds() {
        return ownerBoundsFinal;
      }

      @Override
      public void storeBounds(Rectangle bounds) {
      }
    };
    ISwingScoutView ui = new SwingScoutDialog(this, parentWindow, boundsProvider);
    ui.setName("Synth.Dialog");
    Window w = SwingUtilities.getWindowAncestor(ui.getSwingContentPane());
    if (w != null) {
      setWindowIcon(w);
    }
    decorate(form, ui);
    return ui;
  }

  public ISwingScoutView createPopupWindow(Window parentWindow, IForm form) {
    Component owner = getPopupOwner();
    if (owner == null) {
      owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    }
    if (owner == null) {
      return null;
    }
    Rectangle ownerBounds = getPopupOwnerBounds();
    if (ownerBounds == null) {
      ownerBounds = owner.getBounds();
    }
    final SwingScoutPopup ui = new SwingScoutPopup(this, owner, ownerBounds);
    //close popup when PARENT shell is activated, closed or focused
    SwingUtilities.getWindowAncestor(owner).addWindowListener(new WindowAdapter() {
      @Override
      public void windowActivated(WindowEvent e) {
        //auto-detach
        e.getWindow().removeWindowListener(this);
        ui.closeView();
      }

      @Override
      public void windowClosed(WindowEvent e) {
        //auto-detach
        e.getWindow().removeWindowListener(this);
        ui.closeView();
      }
    });
    SwingUtilities.getWindowAncestor(owner).addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowGainedFocus(WindowEvent e) {
        //auto-detach
        e.getWindow().removeWindowFocusListener(this);
        ui.closeView();
      }
    });
    ui.setName("Synth.Popup");
    decorate(form, ui);
    return ui;
  }

  public ISwingScoutView createView(Object viewConstraints, IForm form) {
    ISwingScoutBoundsProvider boundsProvider = null;
    if (form.isCacheBounds()) {
      boundsProvider = new P_FormBoundsProvider(form);
    }
    ISwingScoutView ui = new SwingScoutInternalFrame(this, viewConstraints, boundsProvider);
    ui.setName("Synth.View");
    decorate(form, ui);
    return ui;
  }

  public ISwingScoutForm createForm(JComponent parent, IForm model) {
    ISwingScoutForm ui = new SwingScoutForm(this, model);
    ui.createField(model, this);
    decorate(model, ui);
    return ui;
  }

  public ISwingScoutForm createForm(ISwingScoutView targetViewComposite, IForm model) {
    ISwingScoutForm ui = new SwingScoutForm(this, targetViewComposite, model);
    ui.createField(model, this);
    decorate(model, ui);
    return ui;
  }

  public ISwingScoutTabItem createTabItem(JComponent parent, IGroupBox field) {
    SwingScoutTabItem ui = new SwingScoutTabItem();
    ui.createField(field, this);
    decorate(field, ui);
    return ui;
  }

  public void appendActions(JComponent parent, List<? extends IAction> actions) {
    new AppendActionsInjector().inject(this, parent, actions);
  }

  public ISwingScoutAction createAction(JComponent parent, IAction action) {
    ISwingScoutAction ui = new CreateActionInjector().inject(this, parent, action);
    decorate(action, ui);
    return ui;
  }

  /**
   * @return true if component can realistically gain focus
   */
  @SuppressWarnings("deprecation")
  public boolean acceptAsFocusTarget(Component comp) {
    checkThread();
    return new SwingScoutFocusTraversalPolicy().accept(comp);
  }

  @Override
  public JStatusLabelEx createStatusLabel() {
    JStatusLabelEx ui = new JStatusLabelEx();
    ui.setName("Synth.StatusLabel");
    return ui;
  }

  @Override
  public OnFieldLabelDecorator createOnFieldLabelDecorator(JComponent c, boolean mandatory) {
    return new OnFieldLabelDecorator(c, mandatory);
  }

  @Override
  public JComponent createLogo() {
    JLabel logo = new JLabel();
    logo.setIcon(getIcon("logo"));
    return logo;
  }

  @Override
  public Component getPopupOwner() {
    return m_popupOwner;
  }

  @Override
  public Rectangle getPopupOwnerBounds() {
    return m_popupOwnerBounds;
  }

  @Override
  public void setPopupOwner(Component owner, Rectangle ownerBounds) {
    m_popupOwner = owner;
    m_popupOwnerBounds = ownerBounds;
  }

  private static void checkThread() {
    if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Must be called in swing thread");
  }

  /**
   * To provide cached bounds
   */
  private class P_FormBoundsProvider implements ISwingScoutBoundsProvider {
    private final IForm m_form;

    public P_FormBoundsProvider(IForm form) {
      m_form = form;
    }

    @Override
    public Rectangle getBounds() {
      Rectangle r = ClientUIPreferences.getInstance().getFormBounds(m_form);
      // ticket 78127: validate on screen to avoid out-of-screen placement
      if (r != null) {
        r = SwingUtility.validateRectangleOnScreen(r, false, false);
      }
      return r;
    }

    @Override
    public void storeBounds(Rectangle bounds) {
      ClientUIPreferences.getInstance().setFormBounds(m_form, bounds);
    }
  }
}
