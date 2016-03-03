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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.HTMLUtility.DefaultFont;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.IBusyManagerService;
import org.eclipse.scout.rt.client.services.common.exceptionhandler.ErrorHandler;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.ui.swt.action.menu.ISwtScoutMenuItem;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtScoutMenuItem;
import org.eclipse.scout.rt.ui.swt.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.swt.busy.SwtBusyHandler;
import org.eclipse.scout.rt.ui.swt.concurrency.SwtScoutSynchronizer;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.scout.rt.ui.swt.form.SwtScoutForm;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.internal.debug.layout.spy.LogicalGridLayoutSpy;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStrokeFilter;
import org.eclipse.scout.rt.ui.swt.keystroke.KeyStrokeManager;
import org.eclipse.scout.rt.ui.swt.util.ColorFactory;
import org.eclipse.scout.rt.ui.swt.util.FontRegistry;
import org.eclipse.scout.rt.ui.swt.util.ISwtIconLocator;
import org.eclipse.scout.rt.ui.swt.util.ScoutFormToolkit;
import org.eclipse.scout.rt.ui.swt.util.SwtIconLocator;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
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
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.framework.Bundle;

/**
 * <h3>SwtEnvironment</h3> ...
 *
 * @since 1.0.0 06.03.2008
 */
public abstract class AbstractSwtEnvironment extends AbstractPropertyObserver implements ISwtEnvironment {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSwtEnvironment.class);
  public static final String PROP_WIDGET_IDS_ENABLED = "org.eclipse.scout.rt.widgetIdsEnabled";
  public static final String WIDGET_ID_KEY = "TEST_COMP_NAME";

  private final Bundle m_applicationBundle;

  private final String m_perspectiveId;
  private final Class<? extends IClientSession> m_clientSessionClass;
  private IClientSession m_clientSession;
  private IDesktop m_desktop;

  private int m_status;

  private SwtScoutSynchronizer m_synchronizer;

  private final Object m_immediateSwtJobsLock = new Object();
  private final List<Runnable> m_immediateSwtJobs = new ArrayList<Runnable>();

  private Clipboard m_clipboard;
  private ColorFactory m_colorFactory;
  private FontRegistry m_fontRegistry;
  private ISwtIconLocator m_iconLocator;
  private ISwtScoutTray m_trayComposite;

  private List<ISwtKeyStroke> m_desktopKeyStrokes;
  private KeyStrokeManager m_keyStrokeManager;

  private Control m_popupOwner;
  private Rectangle m_popupOwnerBounds;

  private ScoutFormToolkit m_formToolkit;
  private FormFieldFactory m_formFieldFactory;

  private PropertyChangeSupport m_propertySupport;

  private boolean m_startDesktopCalled;
  private boolean m_activateDesktopCalled;

  private EventListenerList m_environmentListeners;

  private Map<String, String> m_scoutPartIdToUiPartId;
  private Map<IForm, ISwtScoutPart> m_openForms;
  private P_ScoutDesktopListener m_scoutDesktopListener;
  private P_ScoutDesktopPropertyListener m_desktopPropertyListener;

  private P_PerspectiveListener m_perspectiveListener;
  private P_WorkbenchListener m_workbenchListener;

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
  }

  public Bundle getApplicationBundle() {
    return m_applicationBundle;
  }

  private void stopScout() throws CoreException {
    final IClientSession localSession = m_clientSession;
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
      if (m_trayComposite != null) {
        m_trayComposite.disposeTray();
        m_trayComposite = null;
      }
      detachScoutListeners();
      detachSWTListeners();
      if (m_synchronizer != null) {
        m_synchronizer = null;
      }
      m_clientSession = null;

      m_status = SwtEnvironmentEvent.STOPPED;
      // remove workbench listener to avoid recursive calls
      PlatformUI.getWorkbench().removeWorkbenchListener(m_workbenchListener);
      setStartDesktopCalled(false);
    }
    finally {
      if (m_status == SwtEnvironmentEvent.STOPPED) {
        JobEx sendShutdownEventJob = new JobEx("wait for shutdown state lock job.") {
          @Override
          protected IStatus run(IProgressMonitor monitor) {
            // we are stopping. wait until the session has released the state lock
            Object stateLock = localSession.getStateLock();
            while (localSession.isActive()) {
              synchronized (stateLock) {
                try {
                  stateLock.wait();
                }
                catch (InterruptedException e) {
                  LOG.warn("Interrupted while waiting for model thread.");
                }
              }
            }
            getDisplay().asyncExec(new Runnable() {
              @Override
              public void run() {
                fireEnvironmentChanged(new SwtEnvironmentEvent(this, SwtEnvironmentEvent.STOPPED));
              }
            });
            return Status.OK_STATUS;
          }
        };
        sendShutdownEventJob.schedule();
      }
    }
  }

  /**
   * @param scoutPartLocation
   *          the location id defined in {@link IForm} or additional.
   * @param uiPartId
   *          the id of the {@link IViewPart} registered in the plugin.xml as a
   *          view extension.
   */
  @Override
  public void registerPart(String scoutPartLocation, String uiPartId) {
    m_scoutPartIdToUiPartId.put(scoutPartLocation, uiPartId);
  }

  @Override
  public void unregisterPart(String scoutPartLocation) {
    m_scoutPartIdToUiPartId.remove(scoutPartLocation);
  }

  @Override
  public final String[] getAllPartIds() {
    HashSet<String> partIds = new HashSet<String>(m_scoutPartIdToUiPartId.values());
    return partIds.toArray(new String[partIds.size()]);
  }

  @Override
  public final String getSwtPartIdForScoutPartId(String scoutPartLocation) {
    return m_scoutPartIdToUiPartId.get(scoutPartLocation);
  }

  @Override
  public final String getScoutPartIdForSwtPartId(String partId) {
    if (partId == null) {
      return "";
    }
    for (Entry<String, String> entry : m_scoutPartIdToUiPartId.entrySet()) {
      if (entry.getValue().equals(partId)) {
        return entry.getKey();
      }
    }
    return "";
  }

  public IViewPart findViewPart(String viewId) {
    if (viewId != null) {
      IViewDescriptor viewRef = PlatformUI.getWorkbench().getViewRegistry().find(viewId);
      if (viewRef != null && !viewRef.getAllowMultiple()) {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewId);
      }
    }
    return null;
  }

  @Override
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
          view = findViewPart(viewId);

          if (view == null) {
            view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId, null, IWorkbenchPage.VIEW_VISIBLE);
          }
        }
        else {
          view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId, secondaryId, IWorkbenchPage.VIEW_VISIBLE);
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

  @Override
  public AbstractScoutEditorPart getEditorPart(IEditorInput editorInput, String editorId) {
    if (editorInput != null && editorId != null) {
      try {
        IEditorPart editor = null;
        editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(editorInput);

        if (editor == null) {
          editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, editorId);
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

  @Override
  public boolean isInitialized() {
    return m_status == SwtEnvironmentEvent.STARTED;
  }

  @Override
  public final void ensureInitialized() {
    if (m_status == SwtEnvironmentEvent.INACTIVE
        || m_status == SwtEnvironmentEvent.STOPPED) {
      try {
        init();
      }
      catch (CoreException e) {
        LOG.error("could not initialize Environment", e);
      }
    }
  }

  private synchronized void init() throws CoreException {
    if (m_status == SwtEnvironmentEvent.STARTING
        || m_status == SwtEnvironmentEvent.STARTED
        || m_status == SwtEnvironmentEvent.STOPPING) {
      return;
    }
    m_status = SwtEnvironmentEvent.INACTIVE;
    // must be called in display thread
    if (Thread.currentThread() != getDisplay().getThread()) {
      throw new IllegalStateException("must be called in display thread");
    }
    // workbench must exist
    if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
      throw new IllegalStateException("workbench must be active");
    }

    try {
      m_status = SwtEnvironmentEvent.STARTING;
      m_clipboard = new Clipboard(getDisplay());
      fireEnvironmentChanged(new SwtEnvironmentEvent(this, m_status));

      IClientSession tempClientSession = createAndStartClientSession();
      if (!tempClientSession.isActive()) {
        showClientSessionLoadError(tempClientSession.getLoadError());
        LOG.error("ClientSession is not active, there must be a problem with loading or starting");
        m_status = SwtEnvironmentEvent.INACTIVE;
        return;
      }
      else {
        m_clientSession = tempClientSession;
      }
      m_desktop = m_clientSession.getDesktop();
      SwtUtility.setNlsTextsOnDisplay(getDisplay(), m_clientSession.getTexts());
      if (m_synchronizer == null) {
        m_synchronizer = new SwtScoutSynchronizer(this);
      }
      //
      m_iconLocator = createIconLocator();
      m_colorFactory = new ColorFactory(getDisplay());
      m_keyStrokeManager = new KeyStrokeManager(this);
      m_fontRegistry = new FontRegistry(getDisplay());
      attachScoutListeners();
      attachSWTListeners();
      IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      if (activePage != null) {
        IPerspectiveDescriptor activePerspective = activePage.getPerspective();
        if (activePerspective != null) {
          String perspectiveId = activePerspective.getId();
          if (handlePerspectiveOpened(perspectiveId)) {
            handlePerspectiveActivated(perspectiveId);
          }
        }
      }
      // desktop keystokes
      for (IKeyStroke scoutKeyStroke : getClientSession().getDesktop().getKeyStrokes()) {
        ISwtKeyStroke[] swtStrokes = SwtUtility.getKeyStrokes(scoutKeyStroke, this);
        for (ISwtKeyStroke swtStroke : swtStrokes) {
          m_desktopKeyStrokes.add(swtStroke);
          addGlobalKeyStroke(swtStroke);
        }
      }

      // developmentKeyStroke
      initGlobalKeyStrokes();
      // environment shutdownhook
      m_workbenchListener = new P_WorkbenchListener();
      PlatformUI.getWorkbench().addWorkbenchListener(m_workbenchListener);

      // notify ui available
      Runnable job = new Runnable() {
        @Override
        public void run() {
          getScoutDesktop().getUIFacade().fireDesktopOpenedFromUI();
        }
      };
      invokeScoutLater(job, 0);

      // init scout props
      updateWindowTitle();
      updateStatusFromScout();

      m_status = SwtEnvironmentEvent.STARTED;
      fireEnvironmentChanged(new SwtEnvironmentEvent(this, m_status));
      attachBusyHandler();
    }
    finally {
      if (m_status == SwtEnvironmentEvent.STARTING) {
        m_status = SwtEnvironmentEvent.STOPPED;
        fireEnvironmentChanged(new SwtEnvironmentEvent(this, m_status));
      }
    }
  }

  protected void initGlobalKeyStrokes() {
    if (Platform.inDevelopmentMode()) {
      addGlobalKeyStroke(new ISwtKeyStroke() {

        @Override
        public void handleSwtAction(Event e) {
          new LogicalGridLayoutSpy(getDisplay().getActiveShell()).activate();

        }

        @Override
        public int getStateMask() {
          return SWT.ALT | SWT.SHIFT;
        }

        @Override
        public int getKeyCode() {
          return SWT.F2;
        }
      });
    }
  }

  /**
   * Creates and starts a new client session. <br/>
   * This is done in a separate thread to make sure that no client code is
   * running in the ui thread.
   */
  private IClientSession createAndStartClientSession() {
    final IHolder<IClientSession> holder = new Holder<IClientSession>(IClientSession.class);
    JobEx job = new JobEx("Creating and starting client session") {

      @Override
      protected IStatus run(IProgressMonitor monitor) {
        holder.setValue(SERVICES.getService(IClientSessionRegistryService.class).newClientSession(m_clientSessionClass, initUserAgent()));

        return Status.OK_STATUS;
      }

    };
    job.schedule();
    try {
      job.join();
    }
    catch (InterruptedException e) {
      LOG.error("Client session startup interrupted.", e);
    }

    return holder.getValue();
  }

  protected UserAgent initUserAgent() {
    return UserAgent.create(UiLayer.SWT, UiDeviceType.DESKTOP);
  }

  protected SwtBusyHandler attachBusyHandler() {
    IBusyManagerService service = SERVICES.getService(IBusyManagerService.class);
    if (service == null) {
      return null;
    }
    SwtBusyHandler handler = createBusyHandler();
    getDisplay().addListener(SWT.Dispose, new P_BusyHandlerDisposeListener(service));
    service.register(getClientSession(), handler);
    return handler;
  }

  private class P_BusyHandlerDisposeListener implements Listener {
    private IBusyManagerService m_busyManagerService;

    public P_BusyHandlerDisposeListener(IBusyManagerService busyManagerService) {
      m_busyManagerService = busyManagerService;
    }

    @Override
    public void handleEvent(Event event) {
      m_busyManagerService.unregister(getClientSession());
    }
  }

  protected SwtBusyHandler createBusyHandler() {
    return new SwtBusyHandler(getClientSession(), this);
  }

  protected void showClientSessionLoadError(Throwable error) {
    ErrorHandler handler = new ErrorHandler(error);
    MessageBox mbox = new MessageBox(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), SWT.OK);
    mbox.setText("" + handler.getTitle());
    mbox.setMessage(StringUtility.join("\n\n", handler.getText(), handler.getDetail()));
    mbox.open();
  }

  // hide ScoutViews with no Forms
  private class P_HideScoutViews implements Runnable {

    @Override
    public void run() {
      IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      if (activePage != null) {
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
    }
  }

  private void fireGuiAttachedFromUI() {
    if (getScoutDesktop() != null) {
      getScoutDesktop().getUIFacade().fireGuiAttached();
    }
    getDisplay().asyncExec(new P_HideScoutViews());
  }

  private void fireGuiDetachedFromUI() {
    if (getScoutDesktop() != null) {
      getScoutDesktop().getUIFacade().fireGuiDetached();
    }
  }

  private void fireDesktopActivatedFromUI() {
    if (getScoutDesktop() != null) {
      getScoutDesktop().ensureViewStackVisible();
    }
  }

  @Override
  public String styleHtmlText(ISwtScoutFormField<?> uiComposite, String rawHtml) {
    String cleanHtml = rawHtml;
    if (cleanHtml == null) {
      cleanHtml = "";
    }

    if (uiComposite.getScoutObject() instanceof IHtmlField) {
      IHtmlField htmlField = (IHtmlField) uiComposite.getScoutObject();
      if (htmlField.isHtmlEditor()) {
        /*
         * In HTML editor mode, the HTML is not styled except that an empty HTML skeleton is created in case the given HTML is empty.
         * In general no extra styling should be applied because the HTML installed in the editor should be the very same as
         * provided. Otherwise, if the user did some modifications in the HTML source and reloads the HTML in the editor anew,
         * unwanted auto-corrections would be applied.
         */
        if (!StringUtility.hasText(cleanHtml)) {
          cleanHtml = "<html><head></head><body></body></html>";
        }
      }
      else {
        /*
         * Because @{link SwtScoutHtmlField} is file based, it is crucial to set the content-type and charset appropriately.
         * Also, the CSS needs not to be cleaned as the native browser is used.
         */
        cleanHtml = HTMLUtility.cleanupHtml(cleanHtml, true, false, createDefaultFontSettings(uiComposite.getSwtField()));
      }
    }
    return cleanHtml;
  }

  /**
   * Get SWT specific default font settings
   */
  protected DefaultFont createDefaultFontSettings(Control control) {
    DefaultFont defaultFont = new DefaultFont();
    defaultFont.setSize(8);
    defaultFont.setSizeUnit("pt");
    defaultFont.setForegroundColor(0x000000);
    defaultFont.setFamilies(new String[]{"sans-serif"});

    if (control != null) {
      FontData[] fontData = control.getFont().getFontData();
      if (fontData != null && fontData.length > 0) {
        int height = fontData[0].getHeight();
        if (height > 0) {
          defaultFont.setSize(height);
        }
        String fontFamily = fontData[0].getName();
        if (StringUtility.hasText(fontFamily)) {
          defaultFont.setFamilies(new String[]{fontFamily, "sans-serif"});
        }
      }
      Color color = control.getForeground();
      if (color != null) {
        defaultFont.setForegroundColor(color.getRed() * 0x10000 + color.getGreen() * 0x100 + color.getBlue());
      }
    }
    return defaultFont;
  }

  @Override
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

  @Override
  public final void addEnvironmentListener(ISwtEnvironmentListener listener) {
    m_environmentListeners.add(ISwtEnvironmentListener.class, listener);
  }

  @Override
  public final void removeEnvironmentListener(ISwtEnvironmentListener listener) {
    m_environmentListeners.remove(ISwtEnvironmentListener.class, listener);
  }

  private void fireEnvironmentChanged(SwtEnvironmentEvent event) {
    for (ISwtEnvironmentListener l : m_environmentListeners.getListeners(ISwtEnvironmentListener.class)) {
      l.environmentChanged(event);
    }
  }

  // icon handling

  @Override
  public Image getIcon(String name) {
    return getIcon(name, ICON_DECORATION_NONE);
  }

  @Override
  public Image getIcon(String name, int iconDecoration) {
    return m_iconLocator.getIcon(name, iconDecoration);
  }

  @Override
  public ImageDescriptor getImageDescriptor(String iconId) {
    return m_iconLocator.getImageDescriptor(iconId);
  }

  // key stoke handling
  @Override
  public void addGlobalKeyStroke(ISwtKeyStroke stroke) {
    m_keyStrokeManager.addGlobalKeyStroke(stroke);
  }

  @Override
  public boolean removeGlobalKeyStroke(ISwtKeyStroke stroke) {
    return m_keyStrokeManager.removeGlobalKeyStroke(stroke);
  }

  @Override
  public void addKeyStroke(Widget widget, ISwtKeyStroke stoke) {
    m_keyStrokeManager.addKeyStroke(widget, stoke);
  }

  @Override
  public boolean removeKeyStroke(Widget widget, ISwtKeyStroke stoke) {
    return m_keyStrokeManager.removeKeyStroke(widget, stoke);
  }

  @Override
  public void addKeyStrokeFilter(Widget c, ISwtKeyStrokeFilter filter) {
    m_keyStrokeManager.addKeyStrokeFilter(c, filter);
  }

  @Override
  public boolean removeKeyStrokeFilter(Widget c, ISwtKeyStrokeFilter filter) {
    return m_keyStrokeManager.removeKeyStrokeFilter(c, filter);
  }

  // color handling
  @Override
  public Color getColor(String scoutColor) {
    return m_colorFactory.getColor(scoutColor);
  }

  @Override
  public Color getColor(RGB rgb) {
    return m_colorFactory.getColor(rgb);
  }

  // font handling
  @Override
  public Font getFont(FontSpec scoutFont, Font templateFont) {
    return m_fontRegistry.getFont(scoutFont, templateFont);
  }

  @Override
  public Font getFont(Font templateFont, String newName, Integer newStyle, Integer newSize) {
    return m_fontRegistry.getFont(templateFont, newName, newStyle, newSize);
  }

  // form toolkit handling
  @Override
  public ScoutFormToolkit getFormToolkit() {
    if (m_formToolkit == null) {
      m_formToolkit = createScoutFormToolkit(getDisplay());
    }
    return m_formToolkit;
  }

  // desktop handling
  @Override
  public final IDesktop getScoutDesktop() {
    return m_desktop;
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
    if (desktop == null) {
      LOG.warn("Desktop is null, cannot remove listeners.");
      return;
    }

    if (m_scoutDesktopListener != null) {
      desktop.removeDesktopListener(m_scoutDesktopListener);
      m_scoutDesktopListener = null;
    }
    if (m_desktopPropertyListener != null) {
      desktop.removePropertyChangeListener(m_desktopPropertyListener);
      m_desktopPropertyListener = null;
    }
  }

  protected void attachSWTListeners() {
    if (m_perspectiveListener == null) {
      m_perspectiveListener = new P_PerspectiveListener();
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(m_perspectiveListener);
    }
  }

  protected void detachSWTListeners() {
    if (m_perspectiveListener != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().removePerspectiveListener(m_perspectiveListener);
      m_perspectiveListener = null;
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
    for (IForm dialog : desktop.getDialogStack()) {
      showStandaloneForm(dialog);
    }
    for (IMessageBox messageBoxe : desktop.getMessageBoxStack()) {
      showMessageBoxFromScout(messageBoxe);
    }
  }

  public IForm findActiveForm() {
    Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//    Shell activeShell = getDisplay().getActiveShell();
    if (activeShell == null) {
      return null;
    }

    Object data = activeShell.getData();
    if (data instanceof ISwtScoutPart) {
      return ((ISwtScoutPart) data).getForm();
    }
    else if (data instanceof IWorkbenchWindow) {
      IWorkbenchPage activePage = ((IWorkbenchWindow) data).getActivePage();
      if (activePage == null) {
        return null;
      }

      IWorkbenchPart activePart = activePage.getActivePart();
      if (activePart instanceof ISwtScoutPart) {
        return ((ISwtScoutPart) activePart).getForm();
      }
    }
    return null;
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

  @Override
  public void showFileChooserFromScout(IFileChooser fileChooser) {
    SwtScoutFileChooser sfc = new SwtScoutFileChooser(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), fileChooser, this);
    sfc.showFileChooser();
  }

  private final HashMap<IMessageBox, SwtScoutMessageBoxDialog> m_openMessageBoxes = new HashMap<>();

  @Override
  public void showMessageBoxFromScout(IMessageBox messageBox) {
    SwtScoutMessageBoxDialog box = m_openMessageBoxes.get(messageBox);
    if (box == null) {
      box = new SwtScoutMessageBoxDialog(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), messageBox, this);
      box.open();
      m_openMessageBoxes.put(messageBox, box);
    }
  }

  public void removeMessageBoxFromScout(IMessageBox messageBox) {
    m_openMessageBoxes.remove(messageBox);
  }

  @Override
  public void ensureStandaloneFormVisible(IForm form) {
    ISwtScoutPart part = m_openForms.get(form);
    if (part != null) {
      part.activate();
    }
  }

  private Map<String, List<IForm>> openLater = new HashMap<String, List<IForm>>();

  @Override
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
          parentShell = getParentShellIgnoringPopups(SWT.MODELESS);
        }
        SwtScoutDialog dialog = createSwtScoutDialog(parentShell, dialogStyle);
        try {
          m_openForms.put(form, dialog);
          dialog.showForm(form);
          part = dialog;
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
        SwtScoutDialog popupDialog = createSwtScoutPopupDialog(parentShell, dialogStyle);
        if (popupDialog == null) {
          LOG.error("showing popup for " + form + ", but there is neither a focus owner nor the property 'ISwtEnvironment.getPopupOwner()'");
          return;
        }
        try {
          m_openForms.put(form, popupDialog);
          popupDialog.showForm(form);
          part = popupDialog;
        }
        catch (Throwable t) {
          LOG.error(t.getMessage(), t);
        }
        break;
      }
      case IForm.DISPLAY_HINT_VIEW: {
        String scoutViewId = form.getDisplayViewId();
        if (scoutViewId == null) {
          LOG.error("The property displayViewId must not be null if the property displayHint is set to IForm.DISPLAY_HINT_VIEW.");
          return;
        }

        String uiViewId = getSwtPartIdForScoutPartId(scoutViewId);
        if (uiViewId == null) {
          LOG.warn("no view defined for scoutViewId: " + form.getDisplayViewId());
          return;
        }
        IViewPart existingView = findViewPart(uiViewId);

        String formPerspectiveId = form.getPerspectiveId();
        if (formPerspectiveId == null) {
          formPerspectiveId = "";
        }
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        //open form if formPerspectiveId is empty
        //       OR if currentPerspectiveId equals perspecitveId set on form
        if (StringUtility.hasText(formPerspectiveId)
            && existingView == null
            && activePage != null
            && CompareUtility.notEquals(activePage.getPerspective().getId(), formPerspectiveId)) {

          synchronized (openLater) {
            if (!openLater.containsKey(formPerspectiveId) || !openLater.get(formPerspectiveId).contains(form)) {
              if (openLater.get(formPerspectiveId) == null) {
                openLater.put(formPerspectiveId, new ArrayList<IForm>());
              }
              openLater.get(formPerspectiveId).add(form);
            }
          }
          return;
        }

        //Check if an editor or a view should be opened.
        //An editor is opened if the scoutViewId starts with IForm.EDITOR_ID or IWizard.EDITOR_ID.
        //Compared to equals the check with startsWith enables the possibility to link different editors with the forms.
        if (scoutViewId.startsWith(IForm.EDITOR_ID) || scoutViewId.startsWith(IWizard.EDITOR_ID)) {
          if (activePage != null) {
            ScoutFormEditorInput editorInput = new ScoutFormEditorInput(form, this);
            part = getEditorPart(editorInput, uiViewId);
            m_openForms.put(form, part);
          }
        }
        else {
          AbstractScoutView viewPart = getViewPart(uiViewId);
          try {
            viewPart.showForm(form);
            part = viewPart;
            m_openForms.put(form, viewPart);
            // activate first view
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (page != null && page.getActivePart() == null) {
              page.activate(viewPart);
            }
          }
          catch (ProcessingException e) {
            LOG.error(e.getMessage(), e);
          }
        }
        break;
      }
      case IForm.DISPLAY_HINT_POPUP_WINDOW: {
        SwtScoutPopup popupWindow = createSwtScoutPopupWindow();
        if (popupWindow == null) {
          LOG.error("showing popup for " + form + ", but there is neither a focus owner nor the property 'ISwtEnvironment.getPopupOwner()'");
          return;
        }
        try {
          m_openForms.put(form, popupWindow);
          popupWindow.showForm(form);
          part = popupWindow;
        }
        catch (Throwable e1) {
          LOG.error("Failed opening popup for " + form, e1);
          try {
            popupWindow.showForm(form);
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
    final SwtScoutPopup popup = new SwtScoutPopup(this, owner, true, SWT.RESIZE);
    popup.setMaxHeightHint(280);
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

  @Override
  public Control getPopupOwner() {
    return m_popupOwner;
  }

  @Override
  public Rectangle getPopupOwnerBounds() {
    return m_popupOwnerBounds != null ? new Rectangle(m_popupOwnerBounds.x, m_popupOwnerBounds.y, m_popupOwnerBounds.width, m_popupOwnerBounds.height) : null;
  }

  @Override
  public void setPopupOwner(Control owner, Rectangle ownerBounds) {
    m_popupOwner = owner;
    m_popupOwnerBounds = ownerBounds;
  }

  @Override
  public void hideStandaloneForm(IForm form) {
    if (form == null) {
      return;
    }
    final ISwtScoutPart part = m_openForms.remove(form);
    if (part != null && part.getForm().equals(form)) {
      closePart(part);
    }
  }

  private void closePart(ISwtScoutPart part) {
    try {
      part.closePart();
    }
    catch (ProcessingException e) {
      LOG.warn("could not close part.", e);
    }
  }

  protected void handleDesktopPropertyChanged(String propertyName, Object oldVal, Object newValue) {
    if (IDesktop.PROP_STATUS.equals(propertyName)) {
      updateStatusFromScout();
    }
    else if (IDesktop.PROP_TITLE.equals(propertyName)) {
      updateWindowTitle();
    }
  }

  protected void updateStatusFromScout() {
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
              iconId = 1 << 8;//SWT.ICON_CANCEL
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
        String message = null;
        if (newValue != null) {
          message = newValue.getMessage();
        }
        setStatusLineMessage(null, message);
      }
    }
  }

  protected void updateWindowTitle() {
    if (getScoutDesktop() != null) {
      final String title = getScoutDesktop().getTitle();
      // title is null when the getConfiguredTitle on AbstractDesktop returns null.
      // If the title is null the product title will be left in the titlebar.
      if (title == null) {
        for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
          final Shell s = w.getShell();
          if (!s.isDisposed()) {
            s.getDisplay().asyncExec(new Runnable() {
              @Override
              public void run() {
                s.setText(title);
              }
            });
          }
        }
      }
    }
  }

  @Override
  public void setStatusLineMessage(Image image, String message) {
    for (ISwtScoutPart part : m_openForms.values()) {
      part.setStatusLineMessage(image, message);
    }
  }

  @Override
  public Collection<ISwtScoutPart> getOpenFormParts() {
    return new ArrayList<ISwtScoutPart>(m_openForms.values());
  }

  private class P_ScoutDesktopPropertyListener implements PropertyChangeListener {
    @Override
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
    @Override
    public void desktopChanged(final DesktopEvent e) {
      switch (e.getType()) {
        case DesktopEvent.TYPE_FORM_ADDED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              showStandaloneForm(e.getForm());
            }
          };
          invokeSwtLater(t);
          break;
        }
        case DesktopEvent.TYPE_FORM_REMOVED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              hideStandaloneForm(e.getForm());
            }
          };
          invokeSwtLater(t);
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
        case DesktopEvent.TYPE_MESSAGE_BOX_REMOVED: {

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
        case DesktopEvent.TYPE_TRAVERSE_FOCUS_NEXT: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleTraverseFocusFromScout(true);
            }
          };
          invokeSwtLater(t);
          break;
        }
        case DesktopEvent.TYPE_TRAVERSE_FOCUS_PREVIOUS: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleTraverseFocusFromScout(false);
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
                e.setFocusedField(findFocusOwnerField());
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
              lock.wait(TimeUnit.SECONDS.toMillis(2));
            }
            catch (InterruptedException e1) {
              LOG.warn("Interrupted while waiting for the focus owner to be found.", e1);
            }
          }
          break;
        }
        case DesktopEvent.TYPE_FIND_ACTIVE_FORM: {
          final Object lock = new Object();
          Runnable t = new Runnable() {
            @Override
            public void run() {
              try {
                e.setActiveForm(findActiveForm());
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
              lock.wait(TimeUnit.SECONDS.toMillis(2));
            }
            catch (InterruptedException e1) {
              LOG.warn("Interrupted while waiting for the active form to be found.", e1);
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

  @Override
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

  @Override
  public void invokeSwtLater(Runnable job) {
    if (m_synchronizer != null) {
      m_synchronizer.invokeSwtLater(job);
    }
    else {
      LOG.warn("synchronizer is null; clientSession did not start");
    }
  }

  @Override
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
  @Override
  public Shell getParentShellIgnoringPopups(int modalities) {
    return SwtUtility.getParentShellIgnoringPopups(getDisplay(), modalities);
  }

  @Override
  public IClientSession getClientSession() {
    return m_clientSession;
  }

  // GUI FACTORY

  protected ISwtIconLocator createIconLocator() {
    return new SwtIconLocator(getClientSession().getIconLocator());
  }

  protected ScoutFormToolkit createScoutFormToolkit(Display display) {
    return new ScoutFormToolkit(new FormToolkit(display) {
      @Override
      public Form createForm(Composite parent) {
        Form f = super.createForm(parent);
        decorateFormHeading(f);
        return f;
      }
    });
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

  @Override
  public ISwtScoutForm createForm(Composite parent, IForm scoutForm) {
    SwtScoutForm uiForm = new SwtScoutForm();
    uiForm.createField(parent, scoutForm, this);
    assignWidgetId(scoutForm, uiForm.getSwtField(), uiForm.getSwtContainer());
    return uiForm;
  }

  protected void assignWidgetId(ITypeWithClassId model, Widget swtField, Widget swtContainer) {
    if (swtField != null) {
      assignWidgetId(model, swtField);
    }
    else {
      assignWidgetId(model, swtContainer);
    }
  }

  protected void assignWidgetId(ITypeWithClassId model, Widget widget) {
    if (isWidgetIdsEnabled() && widget != null) {
      widget.setData(WIDGET_ID_KEY, model.classId());
    }
  }

  protected boolean isWidgetIdsEnabled() {
    return StringUtility.parseBoolean(System.getProperty(PROP_WIDGET_IDS_ENABLED));
  }

  @Override
  public ISwtScoutFormField createFormField(Composite parent, IFormField model) {
    if (m_formFieldFactory == null) {
      m_formFieldFactory = new FormFieldFactory(m_applicationBundle);
    }
    ISwtScoutFormField<IFormField> uiField = m_formFieldFactory.createFormField(parent, model, this);
    assignWidgetId(model, uiField.getSwtField(), uiField.getSwtContainer());
    return uiField;
  }

  @Override
  public ISwtScoutMenuItem createMenuItem(Menu uiMenu, IMenu scoutMenu, IActionFilter filter) {
    SwtScoutMenuItem swtScoutMenuItem = new SwtScoutMenuItem(scoutMenu, uiMenu, filter, this);
    assignWidgetId(scoutMenu, swtScoutMenuItem.getSwtMenuItem(), swtScoutMenuItem.getParentMenu());
    return swtScoutMenuItem;
  }

  @Override
  public void checkThread() {
    if (!(getDisplay().getThread() == Thread.currentThread())) {
      throw new IllegalStateException("Must be called in swt thread");
    }
  }

  private class P_PerspectiveListener extends PerspectiveAdapter {
    @Override
    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      String perspectiveId = perspective.getId();
      if (handlePerspectiveOpened(perspectiveId)) {
        handlePerspectiveActivated(perspectiveId);
      }
    }

    @Override
    public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      // global keystrokes are bound to a perspective so it is necessary to disable the global keystrokes
      if (m_perspectiveId.equals(perspective.getId())) {
        m_keyStrokeManager.setGlobalKeyStrokesActivated(false);
      }
    }

    @Override
    public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      handlePerspectiveClosed(perspective.getId());
    }

    @Override
    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
      String perspectiveId = perspective.getId();
      //If perspective is resetted make sure that scout views are open
      if (IWorkbenchPage.CHANGE_RESET.equals(changeId)) {
        handlePerspectiveClosed(perspectiveId);
      }
      else if (IWorkbenchPage.CHANGE_RESET_COMPLETE.equals(changeId)) {
        if (handlePerspectiveOpened(perspectiveId)) {
          handlePerspectiveActivated(perspectiveId);
        }
      }
    }
  }

  private synchronized boolean handlePerspectiveOpened(String perspectiveId) {

    if (m_perspectiveId.equals(perspectiveId)) {
      //make sure that the desktop is only started once
      if (!isStartDesktopCalled()) {
        final P_PerspecitveOpenedJob j = new P_PerspecitveOpenedJob(getDesktopOpenedTaskText(), getClientSession());
        j.schedule();
        setStartDesktopCalled(true);
      }

      m_keyStrokeManager.setGlobalKeyStrokesActivated(true);

      return isStartDesktopCalled();
    }

    return false;
  }

  private synchronized boolean handlePerspectiveActivated(String perspectiveId) {
    if (openLater.containsKey(perspectiveId)) {
      List<IForm> list;
      synchronized (openLater) {
        list = openLater.remove(perspectiveId);
      }
      for (IForm form : list) {
        showStandaloneForm(form);
      }
      setActivateDesktopCalled(CompareUtility.notEquals(m_perspectiveId, perspectiveId));
    }

    if (m_perspectiveId.equals(perspectiveId) && isStartDesktopCalled()) {
      //make sure that the desktop is only started once
      if (!isActivateDesktopCalled()) {
        final P_PerspectiveActivatedJob j = new P_PerspectiveActivatedJob(getDesktopOpenedTaskText(), getClientSession());
        j.schedule();
        setActivateDesktopCalled(true);
      }

      m_keyStrokeManager.setGlobalKeyStrokesActivated(true);

      return isActivateDesktopCalled();
    }
    return false;
  }

  private synchronized boolean handlePerspectiveClosed(String perspectiveId) {
    boolean called = false;

    // make sure that the desktop is only started once
    if (m_perspectiveId.equals(perspectiveId)) {
      final P_PerspectiveClosedJob j = new P_PerspectiveClosedJob(getDesktopClosedTaskText(), getClientSession());
      j.schedule();
      called = true;
      setStartDesktopCalled(false);
      setActivateDesktopCalled(false);

      //global keystrokes are bound to a perspective so it is necessary to disable the global keystrokes
      m_keyStrokeManager.setGlobalKeyStrokesActivated(false);
    }

    return called;
  }

  protected void handleTraverseFocusFromScout(boolean forward) {
    Control comp = getDisplay().getFocusControl();
    if (comp != null) {
      if (forward) {
        comp.traverse(SWT.TRAVERSE_TAB_NEXT);
      }
      else {
        comp.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
      }
    }
  }

  protected void handleScoutPrintInSwt(DesktopEvent e) {
    final WidgetPrinter wp = new WidgetPrinter(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());//getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS));
    try {
      wp.print(e.getPrintDevice(), e.getPrintParameters());
    }
    catch (Throwable ex) {
      LOG.error(null, ex);
    }
    finally {
      Runnable r = new Runnable() {
        @Override
        public void run() {
          getScoutDesktop().getUIFacade().fireDesktopPrintedFromUI(wp.getOutputFile());
        }
      };
      invokeScoutLater(r, 0);
    }
  }

  protected String getDesktopOpenedTaskText() {
    return SwtUtility.getNlsText(Display.getCurrent(), "ScoutStarting");
  }

  protected String getDesktopClosedTaskText() {
    return SwtUtility.getNlsText(Display.getCurrent(), "ScoutStoping");
  }

  private final class P_PerspecitveOpenedJob extends ClientAsyncJob {
    public P_PerspecitveOpenedJob(String name, IClientSession session) {
      super(name, session);
    }

    @Override
    protected void runVoid(IProgressMonitor monitor) throws Throwable {
      getDisplay().syncExec(new Runnable() {
        @Override
        public void run() {
          applyScoutState();
        }
      });
      ClientSyncJob clienSyncJob = new ClientSyncJob(getDesktopOpenedTaskText(), super.getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor syncMonitor) throws Throwable {
          fireGuiAttachedFromUI();
        }
      };
      clienSyncJob.schedule();
    }
  }

  private final class P_PerspectiveActivatedJob extends ClientAsyncJob {
    public P_PerspectiveActivatedJob(String name, IClientSession session) {
      super(name, session);
    }

    @Override
    protected void runVoid(IProgressMonitor monitor) throws Throwable {
      ClientSyncJob clienSyncJob = new ClientSyncJob(getDesktopOpenedTaskText(), super.getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor syncMonitor) throws Throwable {
          fireDesktopActivatedFromUI();
        }
      };
      clienSyncJob.schedule();
    }
  }

  private final class P_PerspectiveClosedJob extends ClientAsyncJob {
    public P_PerspectiveClosedJob(String name, IClientSession session) {
      super(name, session);
    }

    @Override
    protected void runVoid(IProgressMonitor monitor) throws Throwable {
      ClientSyncJob clienSyncJob = new ClientSyncJob(getDesktopOpenedTaskText(), super.getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor syncMonitor) throws Throwable {
          fireGuiDetachedFromUI();
        }
      };
      clienSyncJob.schedule();
    }
  }

  private boolean isStartDesktopCalled() {
    return m_startDesktopCalled;
  }

  private void setStartDesktopCalled(boolean startDesktopCalled) {
    m_startDesktopCalled = startDesktopCalled;
  }

  private boolean isActivateDesktopCalled() {
    return m_activateDesktopCalled;
  }

  private void setActivateDesktopCalled(boolean activateDesktopCalled) {
    m_activateDesktopCalled = activateDesktopCalled;
  }

  @Override
  public String getPerspectiveId() {
    return m_perspectiveId;
  }

  /**
   * Delegate the shutdown process to the Scout model. The preShutdown process will always
   * return @code{false}, meaning that there is a veto so that the UI is not allowed to close
   * the workbench.
   * If the desktop gets closed in the Scout model (@link{AbstractSwtEnvironment#stopScout()}, this listener
   * should be removed to avoid recursive calls.
   */
  private class P_WorkbenchListener implements IWorkbenchListener {
    @Override
    public boolean preShutdown(IWorkbench workbench, boolean forced) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutDesktop().getUIFacade().fireDesktopClosingFromUI(false);
        }
      };
      invokeScoutLater(t, 0);
      return false;
    }

    @Override
    public void postShutdown(IWorkbench workbench) {
      // nop
    }
  }
}
