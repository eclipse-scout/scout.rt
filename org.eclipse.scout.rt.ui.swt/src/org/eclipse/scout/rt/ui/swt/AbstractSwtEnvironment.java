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
package org.eclipse.scout.rt.ui.swt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.exceptionhandler.ErrorHandler;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.swt.concurrency.SwtScoutSynchronizer;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.scout.rt.ui.swt.form.SwtScoutForm;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStrokeFilter;
import org.eclipse.scout.rt.ui.swt.keystroke.KeyStrokeManager;
import org.eclipse.scout.rt.ui.swt.services.SwtScoutProgressService;
import org.eclipse.scout.rt.ui.swt.util.ColorFactory;
import org.eclipse.scout.rt.ui.swt.util.FontRegistry;
import org.eclipse.scout.rt.ui.swt.util.ScoutFormToolkit;
import org.eclipse.scout.rt.ui.swt.util.SwtIconLocator;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartEvent;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartListener;
import org.eclipse.scout.rt.ui.swt.window.desktop.editor.AbstractScoutEditorPart;
import org.eclipse.scout.rt.ui.swt.window.desktop.editor.ScoutFormEditorInput;
import org.eclipse.scout.rt.ui.swt.window.desktop.tray.ISwtScoutTray;
import org.eclipse.scout.rt.ui.swt.window.desktop.tray.SwtScoutTray;
import org.eclipse.scout.rt.ui.swt.window.desktop.view.AbstractScoutView;
import org.eclipse.scout.rt.ui.swt.window.dialog.SwtScoutDialog;
import org.eclipse.scout.rt.ui.swt.window.filechooser.SwtScoutFileChooser;
import org.eclipse.scout.rt.ui.swt.window.messagebox.SwtScoutMessageBoxDialog;
import org.eclipse.scout.rt.ui.swt.window.popup.SwtScoutPopup;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.framework.Bundle;

/**
 * <h3>SwtEnvironment</h3> ...
 * 
 * @since 1.0.0 06.03.2008
 */
public abstract class AbstractSwtEnvironment extends AbstractPropertyObserver implements ISwtEnvironment {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSwtEnvironment.class);

  private static enum BusyStatus {
    IDLE, DETECTING, BUSY
  }

  private SwtScoutSynchronizer m_synchronizer;
  private BusyStatus m_busyStatus;
  private SwtIconLocator m_iconLocator;
  private ColorFactory m_colorFactory;
  private KeyStrokeManager m_keyStrokeManager;
  private ScoutFormToolkit m_formToolkit;
  private FormFieldFactory m_formFieldFactory;
  private EventListenerList m_environmentListeners;
  private final String m_perspectiveId;
  private final Class<? extends IClientSession> m_clientSessionClass;
  private IClientSession m_clientSession;
  private HashMap<String, String> m_scoutPartIdToUiPartId;
  private P_ScoutDesktopListener m_scoutDesktopListener;
  private IJobChangeListener m_jobChangeListener;
  private HashMap<IForm, ISwtScoutPart> m_openForms;
  private FontRegistry m_fontRegistry;
  private PropertyChangeSupport m_propertySupport;
  private int m_status;
  private List<ISwtKeyStroke> m_desktopKeyStrokes;
  private Clipboard m_clipboard;
  private ISwtScoutTray m_trayComposite;
  private P_ScoutDesktopPropertyListener m_desktopPropertyListener;
  private boolean m_startDesktopCalled;
  private OptimisticLock m_activateViewLock;
  private final Bundle m_applicationBundle;
  private final Object m_immediateSwtJobsLock = new Object();
  private final List<Runnable> m_immediateSwtJobs = new ArrayList<Runnable>();
  private Control m_popupOwner;
  private Rectangle m_popupOwnerBounds;

  public AbstractSwtEnvironment(Bundle applicationBundle, String perspectiveId, Class<? extends IClientSession> clientSessionClass) {
    m_applicationBundle = applicationBundle;
    m_perspectiveId = perspectiveId;
    m_clientSessionClass = clientSessionClass;
    //
    m_environmentListeners = new EventListenerList();
    m_scoutPartIdToUiPartId = new HashMap<String, String>();
    m_openForms = new HashMap<IForm, ISwtScoutPart>();
    m_propertySupport = new PropertyChangeSupport(this);
    m_status = SwtEnvironmentEvent.INACTIVE;
    m_desktopKeyStrokes = new ArrayList<ISwtKeyStroke>();
    m_startDesktopCalled = false;
    m_activateViewLock = new OptimisticLock();
  }

  public Bundle getApplicationBundle() {
    return m_applicationBundle;
  }

  protected void execScoutStarted() {
  }

  private void stopScout() throws CoreException {
    try {
      if (m_desktopKeyStrokes != null) {
        for (ISwtKeyStroke swtKeyStroke : m_desktopKeyStrokes) {
          removeGlobalKeyStroke(swtKeyStroke);
        }
        m_desktopKeyStrokes.clear();
      }
      if (m_iconLocator != null) {
        m_iconLocator.dispose();
        m_iconLocator = null;
      }
      if (m_colorFactory != null) {
        m_colorFactory.dispose();
        m_colorFactory = null;
      }
      m_keyStrokeManager = null;
      if (m_fontRegistry != null) {
        m_fontRegistry.dispose();
        m_fontRegistry = null;
      }
      if (m_formToolkit != null) {
        m_formToolkit.dispose();
        m_formToolkit = null;
      }
      detachScoutListeners();
      if (m_jobChangeListener != null) {
        Job.getJobManager().removeJobChangeListener(m_jobChangeListener);
        m_jobChangeListener = null;
      }
      if (m_synchronizer != null) {
        m_synchronizer = null;
      }
      m_clientSession = null;

      m_status = SwtEnvironmentEvent.STOPPED;
      fireEnvironmentChanged(new SwtEnvironmentEvent(this, SwtEnvironmentEvent.STOPPED));
    }
    finally {
      if (m_status != SwtEnvironmentEvent.STOPPED) {
        m_status = SwtEnvironmentEvent.STARTED;
        fireEnvironmentChanged(new SwtEnvironmentEvent(this, SwtEnvironmentEvent.STARTED));
      }
    }
  }

  protected void execScoutStopped() {

  }

  /**
   * @param scoutPartLocation
   *          the location id defined in {@link IForm} or additional.
   * @param uiPartId
   *          the id of the {@link IViewPart} registered in the plugin.xml as a
   *          view extension.
   */
  public void registerPart(String scoutPartLocation, String uiPartId) {
    m_scoutPartIdToUiPartId.put(scoutPartLocation, uiPartId);
  }

  public void unregisterPart(String scoutPartLocation) {
    m_scoutPartIdToUiPartId.remove(scoutPartLocation);
  }

  public final String[] getAllPartIds() {
    HashSet<String> partIds = new HashSet<String>(m_scoutPartIdToUiPartId.values());
    return partIds.toArray(new String[partIds.size()]);
  }

  public final String getSwtPartIdForScoutPartId(String scoutPartLocation) {
    return m_scoutPartIdToUiPartId.get(scoutPartLocation);
  }

  public final String getScoutPartIdForSwtPartId(String partId) {
    if (partId == null) {
      return "";
    }
    Set<Entry<String, String>> entrySet = m_scoutPartIdToUiPartId.entrySet();
    for (Entry<String, String> entry : entrySet) {
      if (entry.getValue().equals(partId)) {
        return entry.getKey();
      }
    }
    return "";
  }

  public boolean acquireActivateViewLock() {
    return m_activateViewLock.acquire();
  }

  public boolean isActivateViewLockAcquired() {
    return m_activateViewLock.isAcquired();
  }

  public void releaseActivateViewLock() {
    m_activateViewLock.release();
  }

  public AbstractScoutView getViewPart(String viewId) {
    if (viewId != null) {
      String secondaryId = null;
      IViewDescriptor viewRef = PlatformUI.getWorkbench().getViewRegistry().find(viewId);
      if (viewRef.getAllowMultiple()) {
        secondaryId = "" + System.currentTimeMillis();
      }
      try {
        IViewPart view = null;
        if (secondaryId == null) {
          view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewId);
          if (view == null) {
            view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId);
          }
          else {
            LOG.warn("DUPLICATED VIEW OPEND; viewID:" + viewId);
          }
        }
        else {
          view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
        }
        if (!(view instanceof AbstractScoutView)) {
          LOG.warn("views used in scout's enviromnent must be extensions of AbstractScoutView");
        }
        else {
          return (AbstractScoutView) view;
        }
      }
      catch (PartInitException e) {
        LOG.error("could not inizialize view", e);
      }
    }
    return null;
  }

  protected ScoutFormEditorInput getEditorInput(IForm form) {
    ScoutFormEditorInput editorInput = new ScoutFormEditorInput(form, this);
    return editorInput;
  }

  private AbstractScoutEditorPart getEditorPart(IEditorInput editorInput, String editorId) {
    if (editorInput != null && editorId != null) {
      try {
        IEditorPart editor = null;
        editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(editorInput);

        if (editor == null) {
          editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editorId);
        }
        else {
          LOG.warn("DUPLICATED EDITOR OPEND; viewID:" + editorId);
        }
        if (!(editor instanceof AbstractScoutEditorPart)) {
          LOG.warn("editors used in scout's enviromnent must be extensions of AbstractScoutEditorPart");
        }
        else {
          return (AbstractScoutEditorPart) editor;
        }
      }
      catch (PartInitException e) {
        LOG.error("could not inizialize editor", e);
      }
    }
    return null;
  }

  public boolean isInitialized() {
    return m_status == SwtEnvironmentEvent.STARTED;
  }

  public final void ensureInitialized() {
    if (m_status == SwtEnvironmentEvent.INACTIVE) {
      m_clipboard = new Clipboard(getDisplay());
      try {
        init();
      }
      catch (CoreException e) {
        LOG.error("could not initialize Environment", e);
      }
    }
  }

  private synchronized void init() throws CoreException {
    m_status = SwtEnvironmentEvent.INACTIVE;
    // must be called in display thread
    if (Thread.currentThread() != getDisplay().getThread()) {
      throw new IllegalStateException("must be called in display thread");
    }
    // workbench must exist
    if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
      throw new IllegalStateException("workbench must be active");
    }
    // close views that were opened due to workbench caching the latest layout
    // of views
    for (IWorkbenchWindow workbenchWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
      for (IWorkbenchPage workbenchPage : workbenchWindow.getPages()) {
        for (IViewReference viewReference : workbenchPage.getViewReferences()) {
          if (m_scoutPartIdToUiPartId.containsValue(viewReference.getId())) {
            workbenchPage.hideView(viewReference);
          }
        }
      }
    }
    //
    try {
      m_status = SwtEnvironmentEvent.STARTING;
      fireEnvironmentChanged(new SwtEnvironmentEvent(this, m_status));
      IClientSession tempClientSession = SERVICES.getService(IClientSessionRegistryService.class).getClientSession(m_clientSessionClass);
      if (!tempClientSession.isActive()) {
        showClientSessionLoadError(tempClientSession.getLoadError());
        LOG.error("ClientSession is not active, there must be a problem with loading or starting");
      }
      else {
        m_clientSession = tempClientSession;
      }
      if (m_clientSession != null && m_synchronizer == null) {
        m_synchronizer = new SwtScoutSynchronizer(this);
      }
      if (m_jobChangeListener == null) {
        // add job manager listener for busy handling
        m_jobChangeListener = new JobChangeAdapter() {
          @Override
          public void done(IJobChangeEvent e) {
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
                    getDisplay().asyncExec(new Runnable() {
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
        };
        Job.getJobManager().addJobChangeListener(m_jobChangeListener);
      }
      //
      m_iconLocator = createIconLocator();
      m_colorFactory = new ColorFactory(getDisplay());
      m_keyStrokeManager = new KeyStrokeManager(this);
      m_fontRegistry = new FontRegistry(getDisplay());
      m_formToolkit = createScoutFormToolkit(getDisplay());
      if (m_clientSession != null) {
        attachScoutListeners();
        setStatusFromScout();
        getDisplay().asyncExec(new Runnable() {
          public void run() {
            applyScoutState();
          }
        });
      }
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new P_PerspectiveListener());
      IPerspectiveDescriptor activePerspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
      if (activePerspective != null) {
        handlePerspectiveOpened(activePerspective.getId());
      }
      if (m_clientSession != null) {
        // desktop keystokes
        for (IKeyStroke scoutKeyStroke : getClientSession().getDesktop().getKeyStrokes()) {
          ISwtKeyStroke[] swtStrokes = SwtUtility.getKeyStrokes(scoutKeyStroke, this);
          for (ISwtKeyStroke swtStroke : swtStrokes) {
            m_desktopKeyStrokes.add(swtStroke);
            addGlobalKeyStroke(swtStroke);
          }
        }
        // environment shutdownhook
        PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
          public boolean preShutdown(IWorkbench workbench, boolean forced) {
            return true;
          }

          public void postShutdown(IWorkbench workbench) {
            Runnable t = new Runnable() {
              @Override
              public void run() {
                getScoutDesktop().getUIFacade().fireGuiDetached();
                getScoutDesktop().getUIFacade().fireDesktopClosingFromUI();
              }
            };
            JobEx job = invokeScoutLater(t, 0);
            try {
              job.join(600000);
            }
            catch (InterruptedException e) {
              //nop
            }
          }
        });
      }
      // notify ui available
      Runnable job = new Runnable() {
        @Override
        public void run() {
          getScoutDesktop().getUIFacade().fireGuiAttached();
        }
      };
      invokeScoutLater(job, 0);

      m_status = SwtEnvironmentEvent.STARTED;
      fireEnvironmentChanged(new SwtEnvironmentEvent(this, m_status));
    }
    finally {
      if (m_status == SwtEnvironmentEvent.STARTING) {
        m_status = SwtEnvironmentEvent.STOPPED;
        fireEnvironmentChanged(new SwtEnvironmentEvent(this, m_status));
      }
    }
  }

  protected void showClientSessionLoadError(Throwable error) {
    ErrorHandler handler = new ErrorHandler(error);
    MessageBox mbox = new MessageBox(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), SWT.OK);
    mbox.setText("" + handler.getTitle());
    mbox.setMessage(StringUtility.join("\n\n", handler.getText(), handler.getDetail()));
    mbox.open();
  }

  private void fireDesktopOpenedFromUI() {
    getScoutDesktop().getUIFacade().fireDesktopOpenedFromUI();
    // hide ScoutViews with no Forms
    invokeSwtLater(new Runnable() {
      @Override
      public void run() {
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (IViewReference viewReference : activePage.getViewReferences()) {
          IViewPart view = viewReference.getView(false);
          if (view != null && view instanceof AbstractScoutView) {
            if (((AbstractScoutView) view).getForm() == null) {
              activePage.hideView(viewReference);
            }
          }
          else if (m_scoutPartIdToUiPartId.containsValue(viewReference.getId())) {
            activePage.hideView(viewReference);
          }
        }
      }
    });
  }

  private void fireDesktopResetFromUI() {
    getScoutDesktop().getUIFacade().fireDesktopResetFromUI();
  }

  public boolean isBusy() {
    return m_busyStatus == BusyStatus.BUSY;
  }

  public void setBusyFromSwt(boolean b) {
    checkThread();
    if (b) {
      if (m_busyStatus == BusyStatus.DETECTING || m_busyStatus == BusyStatus.BUSY) {
        // status is already set to busy or detecting
        return;
      }
      // Do not directly set status to busy in order to prevent mouse cursor flickering (default <-> waiting cursor).
      // A job is scheduled that checks busy conditions after sleeping for 300 ms.
      setBusyInternal(BusyStatus.DETECTING);
      Job j = new Job("Busy in 300ms") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          getDisplay().asyncExec(new Runnable() {
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

  protected void setBusyInternal(BusyStatus status) {
    checkThread();
    if (m_busyStatus != status) {
      m_busyStatus = status;
      SwtScoutProgressService service = SERVICES.getService(SwtScoutProgressService.class);
      service.setWaitingCursor(m_busyStatus == BusyStatus.BUSY, this);
    }
  }

  public void setClipboardText(String text) {
    m_clipboard.setContents(new Object[]{text}, new Transfer[]{TextTransfer.getInstance()});
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  public final void addEnvironmentListener(ISwtEnvironmentListener listener) {
    m_environmentListeners.add(ISwtEnvironmentListener.class, listener);
  }

  public final void removeEnvironmentListener(ISwtEnvironmentListener listener) {
    m_environmentListeners.remove(ISwtEnvironmentListener.class, listener);
  }

  private void fireEnvironmentChanged(SwtEnvironmentEvent event) {
    for (ISwtEnvironmentListener l : m_environmentListeners.getListeners(ISwtEnvironmentListener.class)) {
      l.environmentChanged(event);
    }
  }

  // icon handling
  public Image getIcon(String name) {
    return m_iconLocator.getIcon(name);
  }

  public ImageDescriptor getImageDescriptor(String iconId) {

    return m_iconLocator.getImageDescriptor(iconId);
  }

  // key stoke handling
  public void addGlobalKeyStroke(ISwtKeyStroke stroke) {
    m_keyStrokeManager.addGlobalKeyStroke(stroke);
  }

  public boolean removeGlobalKeyStroke(ISwtKeyStroke stroke) {
    return m_keyStrokeManager.removeGlobalKeyStroke(stroke);
  }

  public void addKeyStroke(Widget widget, ISwtKeyStroke stoke) {
    m_keyStrokeManager.addKeyStroke(widget, stoke);
  }

  public boolean removeKeyStroke(Widget widget, ISwtKeyStroke stoke) {
    return m_keyStrokeManager.removeKeyStroke(widget, stoke);
  }

  public void addKeyStrokeFilter(Widget c, ISwtKeyStrokeFilter filter) {
    m_keyStrokeManager.addKeyStrokeFilter(c, filter);
  }

  public boolean removeKeyStrokeFilter(Widget c, ISwtKeyStrokeFilter filter) {
    return m_keyStrokeManager.removeKeyStrokeFilter(c, filter);
  }

  // color handling
  public Color getColor(String scoutColor) {
    return m_colorFactory.getColor(scoutColor);
  }

  public Color getColor(RGB rgb) {
    return m_colorFactory.getColor(rgb);
  }

  // font handling
  public Font getFont(FontSpec scoutFont, Font templateFont) {

    return m_fontRegistry.getFont(scoutFont, templateFont);
  }

  // form toolkit handling
  public ScoutFormToolkit getFormToolkit() {
    return m_formToolkit;
  }

  // desktop handling
  public final IDesktop getScoutDesktop() {
    if (m_clientSession != null) {
      return m_clientSession.getDesktop();
    }
    else {
      return null;
    }
  }

  protected void attachScoutListeners() {
    if (m_scoutDesktopListener == null) {
      m_scoutDesktopListener = new P_ScoutDesktopListener();
      getScoutDesktop().addDesktopListener(m_scoutDesktopListener);
    }
    if (m_desktopPropertyListener == null) {
      m_desktopPropertyListener = new P_ScoutDesktopPropertyListener();
      getScoutDesktop().addPropertyChangeListener(m_desktopPropertyListener);
    }
  }

  protected void detachScoutListeners() {
    IDesktop desktop = getScoutDesktop();
    if (desktop != null) {
      if (m_scoutDesktopListener != null) {
        desktop.removeDesktopListener(m_scoutDesktopListener);
        m_scoutDesktopListener = null;
      }
      if (m_desktopPropertyListener != null) {
        desktop.removePropertyChangeListener(m_desktopPropertyListener);
        m_desktopPropertyListener = null;
      }
    }
  }

  protected void applyScoutState() {
    IDesktop desktop = getScoutDesktop();
    // load state of internal frames and dialogs
    for (IForm form : desktop.getViewStack()) {
      if (form.isAutoAddRemoveOnDesktop()) {
        showStandaloneForm(form);
      }
    }
    //tray icon
    if (desktop.isTrayVisible()) {
      m_trayComposite = createTray(desktop);
    }
    // dialogs
    IForm[] dialogs = desktop.getDialogStack();
    for (IForm dialog : dialogs) {
      // showDialogFromScout(dialogs[i]);
      showStandaloneForm(dialog);
    }
    IMessageBox[] messageBoxes = desktop.getMessageBoxStack();
    for (IMessageBox messageBoxe : messageBoxes) {
      showMessageBoxFromScout(messageBoxe);
    }
  }

  public IFormField findFocusOwnerField() {
    Control comp = getDisplay().getFocusControl();
    while (comp != null) {
      Object o = comp.getData(ISwtScoutFormField.CLIENT_PROPERTY_SCOUT_OBJECT);
      if (o instanceof IFormField) {
        return (IFormField) o;
      }
      // next
      comp = comp.getParent();
    }
    return null;
  }

  public void showFileChooserFromScout(IFileChooser fileChooser) {
    SwtScoutFileChooser sfc = new SwtScoutFileChooser(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), fileChooser, this);
    sfc.showFileChooser();
  }

  public void showMessageBoxFromScout(IMessageBox messageBox) {
    SwtScoutMessageBoxDialog box = new SwtScoutMessageBoxDialog(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), messageBox, this);
    box.open();

  }

  public void ensureStandaloneFormVisible(IForm form) {
    ISwtScoutPart part = m_openForms.get(form);
    if (part != null) {
      part.activate();
    }

  }

  public void showStandaloneForm(final IForm form) {
    if (form == null) {
      return;
    }
    ISwtScoutPart part = m_openForms.get(form);
    if (part != null) {
      return;
    }
    switch (form.getDisplayHint()) {
      case IForm.DISPLAY_HINT_DIALOG: {
        int dialogStyle = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | (form.isModal() ? SWT.APPLICATION_MODAL : SWT.MODELESS | SWT.MIN);
        Shell parentShell;
        if (form.isModal()) {
          parentShell = getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS);
        }
        else {
          parentShell = getParentShellIgnoringPopups(0);
        }
        SwtScoutDialog dialog = createSwtScoutDialog(parentShell, dialogStyle);
        try {
          m_openForms.put(form, dialog);
          dialog.showForm(form);
        }
        catch (ProcessingException e) {
          LOG.error(e.getMessage(), e);
        }
        break;
      }
      case IForm.DISPLAY_HINT_POPUP_DIALOG: {
        int dialogStyle = SWT.RESIZE | (form.isModal() ? SWT.APPLICATION_MODAL : SWT.MODELESS);
        Shell parentShell;
        if (form.isModal()) {
          parentShell = getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS);
        }
        else {
          parentShell = getParentShellIgnoringPopups(0);
        }
        SwtScoutDialog dialog = createSwtScoutPopupDialog(parentShell, dialogStyle);
        if (dialog == null) {
          LOG.error("showing popup for " + form + ", but there is neither a focus owner nor the property 'ISwtEnvironment.getPopupOwner()'");
          return;
        }
        try {
          m_openForms.put(form, dialog);
          dialog.showForm(form);
        }
        catch (Throwable t) {
          LOG.error(t.getMessage(), t);
        }
        break;
      }
      case IForm.DISPLAY_HINT_VIEW: {
        String scoutViewId = form.getDisplayViewId();
        String uiViewId = m_scoutPartIdToUiPartId.get(scoutViewId);
        if (uiViewId == null) {
          LOG.warn("no view defined for scoutViewId: " + form.getDisplayViewId());
          return;
        }
        if (IForm.EDITOR_ID.equals(form.getDisplayViewId()) || IWizard.EDITOR_ID.equals(form.getDisplayViewId())) {
          ScoutFormEditorInput editorInput = getEditorInput(form);
          AbstractScoutEditorPart editor = getEditorPart(editorInput, uiViewId);
          m_openForms.put(form, editor);
        }
        else {
          AbstractScoutView view = getViewPart(uiViewId);
          try {
            view.showForm(form);
            m_openForms.put(form, view);
          }
          catch (ProcessingException e) {
            LOG.error(e.getMessage(), e);
          }
        }
        break;
      }
      case IForm.DISPLAY_HINT_POPUP_WINDOW: {
        SwtScoutPopup popup = createSwtScoutPopupWindow();
        if (popup == null) {
          LOG.error("showing popup for " + form + ", but there is neither a focus owner nor the property 'ISwtEnvironment.getPopupOwner()'");
          return;
        }
        try {
          m_openForms.put(form, popup);
          popup.showForm(form);
        }
        catch (Throwable e1) {
          LOG.error("Failed opening popup for " + form, e1);
          try {
            popup.showForm(form);
          }
          catch (Throwable t) {
            LOG.error(t.getMessage(), t);
          }
        }
        break;
      }
    }
  }

  protected SwtScoutDialog createSwtScoutDialog(Shell shell, int dialogStyle) {
    return new SwtScoutDialog(shell, this, dialogStyle);
  }

  protected SwtScoutDialog createSwtScoutPopupDialog(Shell shell, int dialogStyle) {
    Control owner = getPopupOwner();
    if (owner == null) {
      owner = getDisplay().getFocusControl();
    }
    if (owner == null) {
      return null;
    }
    Rectangle ownerBounds = getPopupOwnerBounds();
    if (ownerBounds == null) {
      ownerBounds = owner.getBounds();
      Point pDisp = owner.toDisplay(0, 0);
      ownerBounds.x = pDisp.x;
      ownerBounds.y = pDisp.y;
    }
    SwtScoutDialog dialog = new SwtScoutDialog(shell, this, dialogStyle);
    dialog.setInitialLocation(new Point(ownerBounds.x, ownerBounds.y + ownerBounds.height));
    return dialog;
  }

  protected SwtScoutPopup createSwtScoutPopupWindow() {
    Control owner = getPopupOwner();
    if (owner == null) {
      owner = getDisplay().getFocusControl();
    }
    if (owner == null) {
      return null;
    }
    Rectangle ownerBounds = getPopupOwnerBounds();
    if (ownerBounds == null) {
      ownerBounds = owner.getBounds();
      Point pDisp = owner.toDisplay(0, 0);
      ownerBounds.x = pDisp.x;
      ownerBounds.y = pDisp.y;
    }
    final SwtScoutPopup popup = new SwtScoutPopup(this, owner, ownerBounds);
    popup.addSwtScoutPartListener(new SwtScoutPartListener() {
      public void partChanged(SwtScoutPartEvent e) {
        switch (e.getType()) {
          case SwtScoutPartEvent.TYPE_CLOSED: {
            popup.closePart();
            break;
          }
          case SwtScoutPartEvent.TYPE_CLOSING: {
            popup.closePart();
            break;
          }
        }
      }
    });
    //close popup when PARENT shell is activated or closed
    owner.getShell().addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        //auto-detach
        ((Shell) e.getSource()).removeShellListener(this);
        popup.closePart();
      }

      @Override
      public void shellActivated(ShellEvent e) {
        //auto-detach
        ((Shell) e.getSource()).removeShellListener(this);
        popup.closePart();
      }
    });
    return popup;
  }

  public Control getPopupOwner() {
    return m_popupOwner;
  }

  public Rectangle getPopupOwnerBounds() {
    return m_popupOwnerBounds != null ? new Rectangle(m_popupOwnerBounds.x, m_popupOwnerBounds.y, m_popupOwnerBounds.width, m_popupOwnerBounds.height) : null;
  }

  public void setPopupOwner(Control owner, Rectangle ownerBounds) {
    m_popupOwner = owner;
    m_popupOwnerBounds = ownerBounds;
  }

  public void hideStandaloneForm(IForm form) {
    if (form == null) {
      return;
    }
    ISwtScoutPart part = m_openForms.remove(form);
    if (part != null) {
      try {
        part.closePart();
      }
      catch (ProcessingException e) {
        LOG.warn("could not close part.", e);
      }
    }
  }

  protected void handleDesktopPropertyChanged(String propertyName, Object oldVal, Object newValue) {
    if (IDesktop.PROP_STATUS.equals(propertyName)) {
      setStatusFromScout();
    }
  }

  protected void setStatusFromScout() {
    if (getScoutDesktop() != null) {
      IProcessingStatus newValue = getScoutDesktop().getStatus();
      //when a tray item is available, use it, otherwise set status on views/dialogs
      TrayItem trayItem = null;
      if (getTrayComposite() != null) {
        trayItem = getTrayComposite().getSwtTrayItem();
      }
      if (trayItem != null) {
        String s = newValue != null ? newValue.getMessage() : null;
        if (newValue != null && s != null) {
          int iconId;
          switch (newValue.getSeverity()) {
            case IProcessingStatus.WARNING: {
              iconId = SWT.ICON_WARNING;
              break;
            }
            case IProcessingStatus.FATAL:
            case IProcessingStatus.ERROR: {
              iconId = SWT.ICON_ERROR;
              break;
            }
            case IProcessingStatus.CANCEL: {
              iconId = SWT.ICON_CANCEL;
              break;
            }
            default: {
              iconId = SWT.ICON_INFORMATION;
              break;
            }
          }
          ToolTip tip = new ToolTip(getParentShellIgnoringPopups(SWT.MODELESS), SWT.BALLOON | iconId);
          tip.setMessage(s);
          trayItem.setToolTip(tip);
          tip.setVisible(true);
        }
        else {
          ToolTip tip = new ToolTip(getParentShellIgnoringPopups(SWT.MODELESS), SWT.NONE);
          trayItem.setToolTip(tip);
          tip.setVisible(true);
        }
      }
      else {
        for (ISwtScoutPart part : m_openForms.values()) {
          part.setStatus(newValue);
        }
      }
    }
  }

  private class P_ScoutDesktopPropertyListener implements PropertyChangeListener {
    public void propertyChange(final PropertyChangeEvent evt) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          handleDesktopPropertyChanged(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
      };
      invokeSwtLater(job);
    }
  } // end class P_ScoutDesktopPropertyListener

  private class P_ScoutDesktopListener implements DesktopListener {
    public void desktopChanged(final DesktopEvent e) {
      switch (e.getType()) {
        case DesktopEvent.TYPE_FORM_ADDED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              showStandaloneForm(e.getForm());
            }
          };
          if (isActivateViewLockAcquired()) {
            getDisplay().asyncExec(t);
          }
          else {
            invokeSwtLater(t);
          }
          break;
        }
        case DesktopEvent.TYPE_FORM_REMOVED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              hideStandaloneForm(e.getForm());
            }
          };
          if (isActivateViewLockAcquired()) {
            getDisplay().asyncExec(t);
          }
          else {
            invokeSwtLater(t);
          }
          break;
        }
        case DesktopEvent.TYPE_FORM_ENSURE_VISIBLE: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              ensureStandaloneFormVisible(e.getForm());
            }
          };
          invokeSwtLater(t);
          break;
        }
        case DesktopEvent.TYPE_MESSAGE_BOX_ADDED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              showMessageBoxFromScout(e.getMessageBox());
            }
          };
          invokeSwtLater(t);
          break;
        }
        case DesktopEvent.TYPE_FILE_CHOOSER_ADDED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              showFileChooserFromScout(e.getFileChooser());
            }
          };
          invokeSwtLater(t);
          break;
        }
        case DesktopEvent.TYPE_DESKTOP_CLOSED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              try {
                stopScout();
              }
              catch (CoreException ex) {
                LOG.error("desktop closed", ex);
              }
            }
          };
          invokeSwtLater(t);
          break;
        }
        case DesktopEvent.TYPE_PRINT: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleScoutPrintInSwt(e);
            }
          };
          invokeSwtLater(t);
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
            invokeSwtLater(t);
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
  }

  @Override
  public void postImmediateSwtJob(Runnable r) {
    synchronized (m_immediateSwtJobsLock) {
      m_immediateSwtJobs.add(r);
    }
  }

  @Override
  public void dispatchImmediateSwtJobs() {
    List<Runnable> list;
    synchronized (m_immediateSwtJobsLock) {
      list = new ArrayList<Runnable>(m_immediateSwtJobs);
      m_immediateSwtJobs.clear();
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

  public JobEx invokeScoutLater(Runnable job, long cancelTimeout) {
    synchronized (m_immediateSwtJobsLock) {
      m_immediateSwtJobs.clear();
    }
    if (m_synchronizer != null) {
      return m_synchronizer.invokeScoutLater(job, cancelTimeout);
    }
    else {
      LOG.warn("synchronizer is null; clientSession did not start");
      return null;
    }
  }

  public void invokeSwtLater(Runnable job) {
    if (m_synchronizer != null) {
      m_synchronizer.invokeSwtLater(job);
    }
    else {
      LOG.warn("synchronizer is null; clientSession did not start");
    }
  }

  public Display getDisplay() {
    if (PlatformUI.isWorkbenchRunning()) {
      return PlatformUI.getWorkbench().getDisplay();
    }
    else {
      LOG.warn("Workbench is not yet started, accessing the display is unusual: " + new Exception().getStackTrace()[1]);
      Display display = Display.getCurrent();
      if (display == null) {
        display = Display.getDefault();
      }
      return display;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Shell getParentShellIgnoringPopups(int modalities) {
    Shell shell = getDisplay().getActiveShell();
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
    return shell;
  }

  /**
   * Visit the complete workbench shell tree. Ignore popup shells and shells
   * with extendedStyle popup The list is ordered by the following priorities:
   * 1. system modal before application modal before modeless 2. sub shells
   * before parent shells before top level shells
   */
  private void visitShellTreeRec(Shell shell, int modalities, int level, TreeMap<CompositeLong, Shell> out) {
    if (shell != null) {
      if (!SwtUtility.isPopupShell(shell)) {
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
    }
  }

  public IClientSession getClientSession() {
    return m_clientSession;
  }

  // GUI FACTORY

  protected SwtIconLocator createIconLocator() {
    return new SwtIconLocator(getClientSession().getIconLocator());
  }

  protected ScoutFormToolkit createScoutFormToolkit(Display display) {
    return new ScoutFormToolkit(new FormToolkit(display));
  }

  @Override
  public ISwtScoutTray getTrayComposite() {
    return m_trayComposite;
  }

  protected ISwtScoutTray createTray(IDesktop desktop) {
    SwtScoutTray ui = new SwtScoutTray();
    ui.createField(null, desktop, this);
    return ui;
  }

  public ISwtScoutForm createForm(Composite parent, IForm scoutForm) {
    SwtScoutForm uiForm = new SwtScoutForm();
    uiForm.createField(parent, scoutForm, this);
    return uiForm;
  }

  public ISwtScoutFormField createFormField(Composite parent, IFormField model) {
    if (m_formFieldFactory == null) {
      m_formFieldFactory = new FormFieldFactory(m_applicationBundle);
    }
    ISwtScoutFormField<IFormField> uiField = m_formFieldFactory.createFormField(parent, model, this);
    return uiField;
  }

  public void checkThread() {
    if (!(getDisplay().getThread() == Thread.currentThread())) {
      throw new IllegalStateException("Must be called in swt thread");
    }
  }

  private class P_JobEnsureViewStackVisible extends ClientSyncJob {
    public P_JobEnsureViewStackVisible(IClientSession session) {
      super("Ensure view stack visible", session);
    }

    @Override
    protected void runVoid(IProgressMonitor monitor) throws Throwable {
      if (m_clientSession != null) {
        m_clientSession.getDesktop().ensureViewStackVisible();
      }
    }
  }

  private class P_PerspectiveListener extends PerspectiveAdapter {
    @Override
    public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      handlePerspectiveOpened(perspective.getId());
      if (m_perspectiveId.equals(perspective.getId())) {
        new P_JobEnsureViewStackVisible(m_clientSession).schedule();
      }
    }

    @Override
    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      handlePerspectiveOpened(perspective.getId());
      if (m_perspectiveId.equals(perspective.getId())) {
        m_keyStrokeManager.setGlobalKeyStrokesActivated(true);
      }
    }

    @Override
    public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      // global keystrokes are bound to a perspective so it is necessary to
      // disable the global keystrokes
      if (m_perspectiveId.equals(perspective.getId())) {
        m_keyStrokeManager.setGlobalKeyStrokesActivated(false);
      }
    }

    @Override
    public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      // global keystrokes are bound to a perspective so it is necessary to
      // disable the global keystrokes
      if (m_perspectiveId.equals(perspective.getId())) {
        if (m_keyStrokeManager != null) {
          m_keyStrokeManager.setGlobalKeyStrokesActivated(false);
        }
      }
    }

    @Override
    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
      // If perspective is resetted make sure that scout views are open
      if (IWorkbenchPage.CHANGE_RESET.equals(changeId) && m_perspectiveId.equals(perspective.getId())) {
      }
      else if (IWorkbenchPage.CHANGE_RESET_COMPLETE.equals(changeId) && m_perspectiveId.equals(perspective.getId())) {
        handlePerspectiveReset(perspective.getId());
        new P_JobEnsureViewStackVisible(m_clientSession).schedule();
      }
    }
  }

  private synchronized boolean handlePerspectiveOpened(String perspectiveId) {
    boolean called = false;

    // make sure that the desktop is only started once
    if (m_perspectiveId.equals(perspectiveId) && !isStartDesktopCalled()) {
      final P_DesktopOpenedJob j = new P_DesktopOpenedJob(getDesktopOpenedTaskText());
      j.schedule(10);
      setStartDesktopCalled(true);
      called = true;
    }

    if (m_perspectiveId.equals(perspectiveId)) {
      m_keyStrokeManager.setGlobalKeyStrokesActivated(true);
    }

    return called;
  }

  private synchronized boolean handlePerspectiveReset(String perspectiveId) {
    // make sure that the desktop is only started once
    if (m_perspectiveId.equals(perspectiveId)) {
      final P_DesktopResetJob j = new P_DesktopResetJob(getDesktopResetTaskText());
      j.schedule(100);
    }
    return true;
  }

  protected void handleScoutPrintInSwt(DesktopEvent e) {
    WidgetPrinter wp = new WidgetPrinter(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS));
    try {
      wp.print(e.getPrintDevice(), e.getPrintParameters());
    }
    catch (Throwable ex) {
      LOG.error(null, ex);
    }
  }

  protected String getDesktopOpenedTaskText() {
    return ScoutTexts.get("ScoutStarting");
  }

  protected String getDesktopResetTaskText() {
    return ScoutTexts.get("ScoutStarting");
  }

  private final class P_DesktopOpenedJob extends Job {
    public P_DesktopOpenedJob(String name) {
      super(name);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      final CyclicBarrier barrier = new CyclicBarrier(2);
      monitor.beginTask(getDesktopOpenedTaskText(), IProgressMonitor.UNKNOWN);
      getDisplay().asyncExec(new Runnable() {
        public void run() {
          BusyIndicator.showWhile(getDisplay(), new Runnable() {
            public void run() {
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  fireDesktopOpenedFromUI();
                  try {
                    barrier.await();
                  }
                  catch (InterruptedException e) {
                    LOG.warn("CyclicBarrier interrupted", e);
                  }
                  catch (BrokenBarrierException e) {
                    LOG.warn("CyclicBarrier broken", e);
                  }
                }
              };
              JobEx job = invokeScoutLater(t, 0);
              try {
                job.join(120000);
              }
              catch (InterruptedException e) {
                //nop
              }
            }
          });
        }
      });
      try {
        barrier.await(60, TimeUnit.SECONDS);
      }
      catch (TimeoutException e) {
        cancel();
      }
      catch (InterruptedException e) {
        LOG.warn("CyclicBarrier interrupted", e);
      }
      catch (BrokenBarrierException e) {
        LOG.warn("CyclicBarrier broken", e);
      }
      monitor.done();
      return Status.OK_STATUS;
    }
  }

  private final class P_DesktopResetJob extends Job {
    public P_DesktopResetJob(String name) {
      super(name);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      final CyclicBarrier barrier = new CyclicBarrier(2);
      monitor.beginTask(getDesktopResetTaskText(), IProgressMonitor.UNKNOWN);
      getDisplay().asyncExec(new Runnable() {
        public void run() {
          BusyIndicator.showWhile(getDisplay(), new Runnable() {
            public void run() {
              Runnable t = new Runnable() {
                @Override
                public void run() {
                  fireDesktopResetFromUI();
                  try {
                    barrier.await();
                  }
                  catch (InterruptedException e) {
                    LOG.warn("CyclicBarrier interrupted", e);
                  }
                  catch (BrokenBarrierException e) {
                    LOG.warn("CyclicBarrier broken", e);
                  }
                }
              };
              JobEx job = invokeScoutLater(t, 0);
              try {
                job.join(120000);
              }
              catch (InterruptedException e) {
                //nop
              }
            }
          });
        }
      });
      try {
        barrier.await(60, TimeUnit.SECONDS);
      }
      catch (TimeoutException e) {
        cancel();
      }
      catch (InterruptedException e) {
        LOG.warn("CyclicBarrier interrupted", e);
      }
      catch (BrokenBarrierException e) {
        LOG.warn("CyclicBarrier broken", e);
      }
      monitor.done();
      return Status.OK_STATUS;
    }
  }

  private boolean isStartDesktopCalled() {
    return m_startDesktopCalled;
  }

  private void setStartDesktopCalled(boolean startDesktopCalled) {
    m_startDesktopCalled = startDesktopCalled;
  }

  public PerspectiveAdapter getPerspectiveListener() {
    return new P_PerspectiveListener();
  }

  public String getPerspectiveId() {
    return m_perspectiveId;
  }
}
