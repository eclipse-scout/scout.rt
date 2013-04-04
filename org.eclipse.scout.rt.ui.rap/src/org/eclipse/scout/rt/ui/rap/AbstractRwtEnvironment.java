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
package org.eclipse.scout.rt.ui.rap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.lifecycle.PhaseEvent;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.lifecycle.PhaseListener;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.ListUtility;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ILocaleListener;
import org.eclipse.scout.rt.client.LocaleChangeEvent;
import org.eclipse.scout.rt.client.busy.IBusyManagerService;
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
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.rap.busy.RwtBusyHandler;
import org.eclipse.scout.rt.ui.rap.concurrency.RwtScoutSynchronizer;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.form.RwtScoutForm;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.html.HtmlAdapter;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.KeyStrokeManager;
import org.eclipse.scout.rt.ui.rap.servletfilter.LogoutFilter;
import org.eclipse.scout.rt.ui.rap.util.ColorFactory;
import org.eclipse.scout.rt.ui.rap.util.DeviceUtility;
import org.eclipse.scout.rt.ui.rap.util.FontRegistry;
import org.eclipse.scout.rt.ui.rap.util.RwtIconLocator;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.util.ScoutFormToolkit;
import org.eclipse.scout.rt.ui.rap.window.BrowserWindowHandler;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartEvent;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartListener;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormFooter;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormHeader;
import org.eclipse.scout.rt.ui.rap.window.desktop.navigation.RwtScoutNavigationSupport;
import org.eclipse.scout.rt.ui.rap.window.dialog.RwtScoutDialog;
import org.eclipse.scout.rt.ui.rap.window.filechooser.IRwtScoutFileChooser;
import org.eclipse.scout.rt.ui.rap.window.filechooser.IRwtScoutFileChooserService;
import org.eclipse.scout.rt.ui.rap.window.messagebox.RwtScoutMessageBoxDialog;
import org.eclipse.scout.rt.ui.rap.window.popup.RwtScoutPopup;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public abstract class AbstractRwtEnvironment implements IRwtEnvironment {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractRwtEnvironment.class);

  private Subject m_subject;

  private int m_status;

  private Bundle m_applicationBundle;
  private RwtScoutSynchronizer m_synchronizer;
  private ILocaleListener m_localeListener;

  private final Object m_immediateUiJobsLock = new Object();
  private final List<Runnable> m_immediateUiJobs = new ArrayList<Runnable>();

  private ColorFactory m_colorFactory;
  private FontRegistry m_fontRegistry;
  private RwtIconLocator m_iconLocator;

  private List<IRwtKeyStroke> m_desktopKeyStrokes;
  private KeyStrokeManager m_keyStrokeManager;

  private Control m_popupOwner;
  private Rectangle m_popupOwnerBounds;

  private ScoutFormToolkit m_formToolkit;
  private FormFieldFactory m_formFieldFactory;

  private boolean m_startDesktopCalled;
  private boolean m_activateDesktopCalled;

  private EventListenerList m_environmentListeners;

  private HashMap<IForm, IRwtScoutPart> m_openForms;
  private P_ScoutDesktopListener m_scoutDesktopListener;
  private P_ScoutDesktopPropertyListener m_desktopPropertyListener;

  private final Class<? extends IClientSession> m_clientSessionClazz;
  private IClientSession m_clientSession;
  private IDesktop m_desktop;

  private RwtScoutNavigationSupport m_historySupport;
  private LayoutValidateManager m_layoutValidateManager;
  private HtmlAdapter m_htmlAdapter;
  private P_RequestInterceptor m_requestInterceptor;

  public AbstractRwtEnvironment(Bundle applicationBundle, Class<? extends IClientSession> clientSessionClazz) {
    m_applicationBundle = applicationBundle;
    m_clientSessionClazz = clientSessionClazz;
    m_environmentListeners = new EventListenerList();
    m_requestInterceptor = new P_RequestInterceptor();
    m_openForms = new HashMap<IForm, IRwtScoutPart>();
    m_status = RwtEnvironmentEvent.INACTIVE;
    m_desktopKeyStrokes = new ArrayList<IRwtKeyStroke>();
    m_startDesktopCalled = false;
  }

  protected void setSubject(Subject subject) {
    m_subject = subject;
  }

  public Subject getSubject() {
    return m_subject;
  }

  /**
   * @return the applicationBundle
   */
  public Bundle getApplicationBundle() {
    return m_applicationBundle;
  }

  protected IRwtScoutPart putPart(IForm form, IRwtScoutPart part) {
    return m_openForms.put(form, part);
  }

  protected IRwtScoutPart getPart(IForm form) {
    return m_openForms.get(form);
  }

  @Override
  public Collection<IRwtScoutPart> getOpenFormParts() {
    return new ArrayList<IRwtScoutPart>(m_openForms.values());
  }

  protected IRwtScoutPart removePart(IForm form) {
    return m_openForms.remove(form);
  }

  private void closeFormParts() {
    if (m_openForms == null) {
      return;
    }
    List<IForm> openForms = new LinkedList<IForm>(m_openForms.keySet());
    for (IForm form : openForms) {
      //Close the gui part, the form itself may stay open
      hideFormPart(form);
    }
  }

  protected void dispose() {
    try {
      closeFormParts();
      if (m_historySupport != null) {
        m_historySupport.uninstall();
        m_historySupport = null;
      }
      if (m_desktopKeyStrokes != null) {
        for (IRwtKeyStroke uiKeyStroke : m_desktopKeyStrokes) {
          removeGlobalKeyStroke(uiKeyStroke);
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
      if (m_synchronizer != null) {
        m_synchronizer = null;
      }
      detachBusyHandler();
      if (m_requestInterceptor != null) {
        RWT.getLifeCycle().removePhaseListener(m_requestInterceptor);
        m_requestInterceptor = null;
      }

      m_status = RwtEnvironmentEvent.STOPPED;
      fireEnvironmentChanged(new RwtEnvironmentEvent(this, RwtEnvironmentEvent.STOPPED));
    }
    finally {
      if (m_status != RwtEnvironmentEvent.STOPPED) {
        m_status = RwtEnvironmentEvent.STARTED;
        fireEnvironmentChanged(new RwtEnvironmentEvent(this, RwtEnvironmentEvent.STARTED));
      }
    }
  }

  /**
   * @see {@link LogoutFilter}
   */
  protected String getLogoutLocation() {
    String path = RWT.getRequest().getServletPath();

    if (path.length() > 0 && '/' == path.charAt(0)) {
      path = path.substring(1);
    }

    path += "?" + LogoutFilter.LOGOUT_PARAM;

    return path;
  }

  public void logout() {
    HttpServletResponse response = RWT.getResponse();
    String logoutUri = response.encodeRedirectURL(getLogoutLocation());
    String browserText = MessageFormat.format("parent.window.location.href = \"{0}\";", logoutUri);
    JavaScriptExecutor executor = RWT.getClient().getService(JavaScriptExecutor.class);
    if (executor != null) {
      executor.execute(browserText);
    }
  }

  @Override
  public boolean isInitialized() {
    return m_status == RwtEnvironmentEvent.STARTED;
  }

  @Override
  public final void ensureInitialized() {
    ensureInitialized(null);
  }

  protected final void ensureInitialized(Runnable additionalInitCallback) {
    if (m_status == RwtEnvironmentEvent.INACTIVE || m_status == RwtEnvironmentEvent.STOPPED) {
      try {
        init(additionalInitCallback);
      }
      catch (Exception e) {
        LOG.error("could not initialize Environment", e);
      }
    }
  }

  /**
   * @param additionalInitCallback
   *          if not null, the callback is executed at the end of the initialization but before the {@link IDesktop} is
   *          notified.
   */
  protected synchronized void init(Runnable additionalInitCallback) throws CoreException {
    if (m_status == RwtEnvironmentEvent.STARTING
        || m_status == RwtEnvironmentEvent.STARTED
        || m_status == RwtEnvironmentEvent.STOPPING) {
      return;
    }
    m_status = RwtEnvironmentEvent.INACTIVE;
    // must be called in display thread
    if (Thread.currentThread() != getDisplay().getThread()) {
      throw new IllegalStateException("must be called in display thread");
    }
    getDisplay().addListener(SWT.Dispose, new P_DisplayDisposeListener());

    try {
      m_status = RwtEnvironmentEvent.STARTING;

      // enable HTTP request handling
      // the first beforeRequest-event has to be fired manually, because currently no PhaseListener is attached
      beforeHttpRequest();
      RWT.getLifeCycle().addPhaseListener(m_requestInterceptor);

      fireEnvironmentChanged(new RwtEnvironmentEvent(this, m_status));

      if (getSubject() == null) {
        throw new SecurityException("/rap request is not authenticated with a Subject");
      }

      UserAgent userAgent = initUserAgent();
      DeviceUtility.setCurrentDeviceType(userAgent.getUiDeviceType());
      IClientSession clientSession = initClientSession(userAgent);
      if (!clientSession.isActive()) {
        showClientSessionLoadError(clientSession.getLoadError());
        LOG.error("ClientSession is not active, there must be a problem with loading or starting");
        m_status = RwtEnvironmentEvent.INACTIVE;
        return;
      }
      else {
        m_clientSession = clientSession;
      }
      m_desktop = m_clientSession.getDesktop();

      // init RWT locale with the locale of the client session
      if (clientSession.getLocale() != null && !clientSession.getLocale().equals(RWT.getLocale())) {
        RWT.setLocale(clientSession.getLocale());
      }

      if (m_synchronizer == null) {
        m_synchronizer = new RwtScoutSynchronizer(this);
      }

      //put the the display on the session data
      m_clientSession.setData(ENVIRONMENT_KEY, this);

      //
      RwtUtility.setNlsTextsOnDisplay(getDisplay(), m_clientSession.getTexts());
      m_iconLocator = createIconLocator();
      m_colorFactory = new ColorFactory(getDisplay());
      m_keyStrokeManager = new KeyStrokeManager(this);
      m_fontRegistry = new FontRegistry(getDisplay());
      if (UiDecorationExtensionPoint.getLookAndFeel().isBrowserHistoryEnabled()) {
        m_historySupport = new RwtScoutNavigationSupport(this);
        m_historySupport.install();
      }
      m_layoutValidateManager = new LayoutValidateManager();
      attachScoutListeners();

      // desktop keystokes
      for (IKeyStroke scoutKeyStroke : getClientSession().getDesktop().getKeyStrokes()) {
        IRwtKeyStroke[] uiStrokes = RwtUtility.getKeyStrokes(scoutKeyStroke, this);
        for (IRwtKeyStroke uiStroke : uiStrokes) {
          m_desktopKeyStrokes.add(uiStroke);
          addGlobalKeyStroke(uiStroke, false);
        }
      }

      if (additionalInitCallback != null) {
        additionalInitCallback.run();
      }
      applyScoutState();

      // notify ui available
      // notify desktop that it is loaded
      new ClientSyncJob("Desktop opened", getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          if (!getScoutDesktop().isOpened()) {
            fireDesktopOpenedFromUIInternal();
          }
          if (!getScoutDesktop().isGuiAvailable()) {
            fireGuiAttachedFromUIInternal();
          }
        }
      }.schedule();

      m_status = RwtEnvironmentEvent.STARTED;
      fireEnvironmentChanged(new RwtEnvironmentEvent(this, m_status));

      attachBusyHandler();
    }
    finally {
      if (m_status == RwtEnvironmentEvent.STARTING) {
        m_status = RwtEnvironmentEvent.STOPPED;
        fireEnvironmentChanged(new RwtEnvironmentEvent(this, m_status));
      }
    }
  }

  /**
   * As default, the {@link IClientSession} is attached to the http session. This means, the client session lives as
   * long the http session lives. If the http session expires, the client session will be stopped.
   * <p>
   * This method creates and loads a new client session if there is no client session attached to the http session yet.
   * If there already is one attached, the attached session will be returned.
   */
  protected IClientSession initClientSession(UserAgent userAgent) {
    HttpSession httpSession = RWT.getUISession().getHttpSession();
    IClientSession clientSession = (IClientSession) httpSession.getAttribute(IClientSession.class.getName());
    if (clientSession != null) {
      if (!userAgent.equals(clientSession.getUserAgent())) {
        //Force client session shutdown
        httpSession.setAttribute(P_HttpSessionInvalidationListener.class.getName(), null);
        //Make sure a new client session will be initialized
        clientSession = null;
      }
    }
    if (clientSession == null || !clientSession.isActive()) {
      LocaleThreadLocal.set(RwtUtility.getBrowserInfo().getLocale());
      clientSession = SERVICES.getService(IClientSessionRegistryService.class).newClientSession(m_clientSessionClazz, getSubject(), UUID.randomUUID().toString(), userAgent);

      httpSession.setAttribute(IClientSession.class.getName(), clientSession);
      httpSession.setAttribute(P_HttpSessionInvalidationListener.class.getName(), new P_HttpSessionInvalidationListener(clientSession));
    }

    return clientSession;
  }

  protected UserAgent initUserAgent() {
    return UserAgent.create(UiLayer.RAP, UiDeviceType.DESKTOP, RwtUtility.getBrowserInfo().getUserAgent());
  }

  /**
   * Handles event before a HTTP request will be processed.
   */
  protected void beforeHttpRequest() {
  }

  /**
   * Do NOT override this internal method, instead use {@link #beforeHttpRequest()}.
   */
  protected void beforeHttpRequestInternal() {
    if (getClientSession() != null) {
      LocaleThreadLocal.set(getClientSession().getLocale());
    }

    beforeHttpRequest();
  }

  /**
   * Handles event after a HTTP request has been processed.
   */
  protected void afterHttpRequest() {
  }

  /**
   * Do NOT override this internal method, instead use {@link #afterHttpRequest()}.
   */
  protected void afterHttpRequestInternal() {
    afterHttpRequest();
  }

  protected RwtBusyHandler attachBusyHandler() {
    IBusyManagerService service = SERVICES.getService(IBusyManagerService.class);
    if (service == null) {
      return null;
    }
    RwtBusyHandler handler = createBusyHandler();
    service.register(getClientSession(), handler);
    return handler;
  }

  protected RwtBusyHandler createBusyHandler() {
    return new RwtBusyHandler(getClientSession(), this);
  }

  private void detachBusyHandler() {
    IBusyManagerService service = SERVICES.getService(IBusyManagerService.class);
    if (service != null) {
      service.unregister(getClientSession());
    }
  }

  protected void showClientSessionLoadError(Throwable error) {
    ErrorHandler handler = new ErrorHandler(error);
    MessageBox mbox = new MessageBox(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), SWT.OK);
    mbox.setText("" + handler.getTitle());
    mbox.setMessage(StringUtility.join("\n\n", handler.getText(), handler.getDetail()));
    mbox.open();
  }

  protected void fireDesktopOpenedFromUIInternal() {
    if (getScoutDesktop() != null) {
      getScoutDesktop().getUIFacade().fireDesktopOpenedFromUI();
    }
  }

  protected void fireGuiAttachedFromUIInternal() {
    if (getScoutDesktop() != null) {
      getScoutDesktop().getUIFacade().fireGuiAttached();
    }
  }

  protected void fireGuiDetachedFromUIInternal() {
    if (getScoutDesktop() != null) {
      getScoutDesktop().getUIFacade().fireGuiDetached();
    }
  }

  @Override
  public void setClipboardText(String text) {
    //XXX rap     m_clipboard.setContents(new Object[]{text}, new Transfer[]{TextTransfer.getInstance()});
  }

  @Override
  public final void addEnvironmentListener(IRwtEnvironmentListener listener) {
    m_environmentListeners.add(IRwtEnvironmentListener.class, listener);
  }

  @Override
  public final void removeEnvironmentListener(IRwtEnvironmentListener listener) {
    m_environmentListeners.remove(IRwtEnvironmentListener.class, listener);
  }

  private void fireEnvironmentChanged(RwtEnvironmentEvent event) {
    for (IRwtEnvironmentListener l : m_environmentListeners.getListeners(IRwtEnvironmentListener.class)) {
      l.environmentChanged(event);
    }
  }

  @Override
  public String adaptHtmlCell(IRwtScoutComposite<?> uiComposite, String rawHtml) {
    return getHtmlAdapter().adaptHtmlCell(uiComposite, rawHtml);
  }

  @Override
  public String convertLinksWithLocalUrlsInHtmlCell(IRwtScoutComposite<?> uiComposite, String rawHtml) {
    return getHtmlAdapter().convertLinksWithLocalUrlsInHtmlCell(uiComposite, rawHtml);
  }

  @Override
  public String styleHtmlText(IRwtScoutFormField<?> uiComposite, String rawHtml) {
    return getHtmlAdapter().styleHtmlText(uiComposite, rawHtml);
  }

  protected HtmlAdapter createHtmlAdapter() {
    return new HtmlAdapter(this);
  }

  @Override
  public HtmlAdapter getHtmlAdapter() {
    if (m_htmlAdapter == null) {
      m_htmlAdapter = createHtmlAdapter();
    }

    return m_htmlAdapter;
  }

  // icon handling
  @Override
  public Image getIcon(String name) {
    return m_iconLocator.getIcon(name);
  }

  @Override
  public ImageDescriptor getImageDescriptor(String iconId) {
    return m_iconLocator.getImageDescriptor(iconId);
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

  //keyStroke handling
  private static Collection<Integer> fKeyList = Arrays.asList(new Integer[]{SWT.F1, SWT.F2, SWT.F3, SWT.F4, SWT.F5, SWT.F6, SWT.F7, SWT.F8, SWT.F9, SWT.F10, SWT.F11, SWT.F12});

  @Override
  public void addGlobalKeyStroke(IRwtKeyStroke stroke, boolean exclusive) {
    boolean internalExclusive = exclusive;
    //If F1-F12 is set we wan't to have this exclusive to the application, else the browser will reload the page
    if (ListUtility.containsAny(fKeyList, stroke.getKeyCode())) {
      internalExclusive = true;
    }
    m_keyStrokeManager.addGlobalKeyStroke(stroke, internalExclusive);
  }

  @Override
  public boolean removeGlobalKeyStroke(IRwtKeyStroke stroke) {
    if (m_keyStrokeManager == null) {
      return false;
    }
    return m_keyStrokeManager.removeGlobalKeyStroke(stroke);
  }

  @Override
  public void addKeyStroke(Control control, IRwtKeyStroke stoke, boolean exclusive) {
    m_keyStrokeManager.addKeyStroke(control, stoke, exclusive);
  }

  @Override
  public boolean removeKeyStroke(Control control, IRwtKeyStroke stoke) {
    if (m_keyStrokeManager == null) {
      return false;
    }
    return m_keyStrokeManager.removeKeyStroke(control, stoke);
  }

  @Override
  public boolean removeKeyStrokes(Control control) {
    if (m_keyStrokeManager == null) {
      return false;
    }
    return m_keyStrokeManager.removeKeyStrokes(control);
  }

  /**
   * @return the keyStrokeManager
   */
  protected KeyStrokeManager getKeyStrokeManager() {
    return m_keyStrokeManager;
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
    if (m_localeListener == null) {
      m_localeListener = new P_LocaleListener();
      m_clientSession.addLocaleListener(m_localeListener);
    }

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
    if (m_clientSession == null) {
      LOG.warn("ClientSession is null, cannot remove listeners.");
    }
    else {
      if (m_localeListener != null) {
        m_clientSession.removeLocaleListener(m_localeListener);
        m_localeListener = null;
      }
    }

    IDesktop desktop = getScoutDesktop();
    if (desktop == null) {
      LOG.warn("Desktop is null, cannot remove listeners.");
    }
    else {
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
    final IForm[] viewStack = desktop.getViewStack();
    final IForm[] dialogs = desktop.getDialogStack();
    final IMessageBox[] messageBoxes = desktop.getMessageBoxStack();
    if (viewStack.length == 0 && dialogs.length == 0 && messageBoxes.length == 0) {
      return;
    }

    //Schedule the opening because the root shell hasn't probably been layouted yet
    //and therefore the computation for the dialog location might be wrong
    getDisplay().asyncExec(new Runnable() {

      @Override
      public void run() {
        for (IForm form : viewStack) {
          showFormPart(form);
        }
        for (IForm dialog : dialogs) {
          showFormPart(dialog);
        }
        for (IMessageBox messageBoxe : messageBoxes) {
          showMessageBoxFromScout(messageBoxe);
        }
      }

    });
  }

  public IFormField findFocusOwnerField() {
    Control comp = getDisplay().getFocusControl();
    while (comp != null) {
      Object o = comp.getData(IRwtScoutFormField.CLIENT_PROPERTY_SCOUT_OBJECT);
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
    IRwtScoutFileChooserService rwtScoutFileChooserService = SERVICES.getService(IRwtScoutFileChooserService.class);
    if (rwtScoutFileChooserService == null) {
      LOG.warn("Missing bundle: org.eclipse.scout.rt.ui.rap.incubator.filechooser. Please activate it in your Scout perspective under Technologies.");
      return;
    }
    IRwtScoutFileChooser sfc = rwtScoutFileChooserService.createFileChooser(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), fileChooser);
    sfc.showFileChooser();
  }

  @Override
  public void openBrowserWindowFromScout(String path) {
    BrowserWindowHandler browserWindowHandler = createBrowserWindowHandler();
    if (browserWindowHandler == null) {
      return;
    }

    browserWindowHandler.openLink(path);
  }

  protected BrowserWindowHandler createBrowserWindowHandler() {
    return new BrowserWindowHandler();
  }

  @Override
  public void showMessageBoxFromScout(IMessageBox messageBox) {
    //Never show a gui to a already closed messagebox. Otherwise it stays open forever.
    //Because of the auto close mechanism of the messagebox it is possible that it's already closed (on model side).
    if (!messageBox.isOpen()) {
      return;
    }

    RwtScoutMessageBoxDialog box = new RwtScoutMessageBoxDialog(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), messageBox, this);
    box.open();
  }

  @Override
  public void ensureFormPartVisible(IForm form) {
    IRwtScoutPart part = getPart(form);
    if (part != null) {
      part.activate();
    }
    else {
      showFormPart(form);
    }
  }

  protected IRwtScoutPart createUiScoutDialog(IForm form, Shell shell, int dialogStyle) {
    RwtScoutDialog ui = new RwtScoutDialog();
    ui.createPart(form, shell, dialogStyle, this);
    return ui;
  }

  protected IRwtScoutPart createUiScoutPopupDialog(IForm form, Shell shell, int dialogStyle) {
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
    RwtScoutDialog dialog = new RwtScoutDialog();
    dialog.createPart(form, shell, dialogStyle, this);
    dialog.setUiInitialLocation(new Point(ownerBounds.x, ownerBounds.y + ownerBounds.height));
    return dialog;
  }

  protected IRwtScoutPart createUiScoutPopupWindow(IForm f) {
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
    final RwtScoutPopup popup = new RwtScoutPopup();
    popup.setMaxHeightHint(280);
    popup.createPart(f, owner, ownerBounds, SWT.RESIZE, this);
    popup.addRwtScoutPartListener(new RwtScoutPartListener() {
      @Override
      public void partChanged(RwtScoutPartEvent e) {
        switch (e.getType()) {
          case RwtScoutPartEvent.TYPE_CLOSED: {
            popup.closePart();
            break;
          }
          case RwtScoutPartEvent.TYPE_CLOSING: {
            popup.closePart();
            break;
          }
        }
      }
    });
    //close popup when PARENT shell is activated or closed
    owner.getShell().addShellListener(new ShellAdapter() {
      private static final long serialVersionUID = 1L;

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
  public void showFormPart(IForm form) {
    if (form == null) {
      return;
    }
    IRwtScoutPart part = getPart(form);
    if (part != null) {
      return;
    }
    switch (form.getDisplayHint()) {
      case IForm.DISPLAY_HINT_DIALOG: {
        Shell parentShell;
        if (form.isModal()) {
          parentShell = getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS);
        }
        else {
          parentShell = getParentShellIgnoringPopups(0);
        }
        int dialogStyle = SWT.DIALOG_TRIM | SWT.RESIZE | (form.isModal() ? SWT.APPLICATION_MODAL : SWT.MODELESS | SWT.MIN);
        part = createUiScoutDialog(form, parentShell, dialogStyle);
        break;
      }
      case IForm.DISPLAY_HINT_POPUP_DIALOG: {
        Shell parentShell;
        if (form.isModal()) {
          parentShell = getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS);
        }
        else {
          parentShell = getParentShellIgnoringPopups(0);
        }
        int dialogStyle = SWT.DIALOG_TRIM | SWT.RESIZE | (form.isModal() ? SWT.APPLICATION_MODAL : SWT.MODELESS | SWT.MIN);
        part = createUiScoutPopupDialog(form, parentShell, dialogStyle);
        if (part == null) {
          LOG.error("showing popup for " + form + ", but there is neither a focus owner nor the property 'IRwtEnvironment.getPopupOwner()'");
        }
        break;
      }
      case IForm.DISPLAY_HINT_VIEW: {
        //nop
        break;
      }
      case IForm.DISPLAY_HINT_POPUP_WINDOW: {
        part = createUiScoutPopupWindow(form);
        if (part == null) {
          LOG.error("showing popup for " + form + ", but there is neither a focus owner nor the property 'IRwtEnvironment.getPopupOwner()'");
        }
        break;
      }
    }
    if (part != null) {
      try {
        putPart(form, part);
        part.showPart();
      }
      catch (Throwable t) {
        LOG.error(t.getMessage(), t);
      }
    }
  }

  @Override
  public void hideFormPart(IForm form) {
    if (form == null) {
      return;
    }
    IRwtScoutPart part = removePart(form);
    if (part != null) {
      part.closePart();
    }
  }

  protected void handleDesktopPropertyChanged(String propertyName, Object oldVal, Object newValue) {
    if (IDesktop.PROP_STATUS.equals(propertyName)) {
      setStatusFromScout();
    }
  }

  @SuppressWarnings("unused")
  protected void setStatusFromScout() {
    if (getScoutDesktop() == null) {
      return;
    }

    IProcessingStatus newValue = getScoutDesktop().getStatus();
    //when a tray item is available, use it, otherwise set status on views/dialogs
    TrayItem trayItem = null;
//      if (getTrayComposite() != null) {//XXXRAP
//        trayItem = getTrayComposite().getSwtTrayItem();
//      }
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
            //Necessary for backward compatibility to Eclipse 3.4 needed for Lotus Notes 8.5.2
            Version frameworkVersion = new Version(Activator.getDefault().getBundle().getBundleContext().getProperty("osgi.framework.version"));
            if (frameworkVersion.getMajor() == 3
                && frameworkVersion.getMinor() <= 4) {
              iconId = SWT.ICON_INFORMATION;
            }
            else {
              iconId = 1 << 8;//SWT.ICON_CANCEL
            }
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

  public void setStatusLineMessage(Image image, String message) {
    for (IRwtScoutPart part : m_openForms.values()) {
      if (part.setStatusLineMessage(image, message)) {
        return;
      }
    }
  }

  private class P_ScoutDesktopPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      if (!getDisplay().isDisposed()) {
        Runnable job = new Runnable() {
          @Override
          public void run() {
            handleDesktopPropertyChanged(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
          }
        };
        invokeUiLater(job);
      }
    }
  } // end class P_ScoutDesktopPropertyListener

  private class P_ScoutDesktopListener implements DesktopListener {
    @Override
    public void desktopChanged(final DesktopEvent e) {
      if (getDisplay().isDisposed()) {
        return;
      }
      switch (e.getType()) {
        case DesktopEvent.TYPE_FORM_ADDED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              showFormPart(e.getForm());
            }
          };
          invokeUiLater(t);
          break;
        }
        case DesktopEvent.TYPE_FORM_REMOVED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              hideFormPart(e.getForm());
            }
          };
          invokeUiLater(t);
          break;
        }
        case DesktopEvent.TYPE_FORM_ENSURE_VISIBLE: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              ensureFormPartVisible(e.getForm());
            }
          };
          invokeUiLater(t);
          break;
        }
        case DesktopEvent.TYPE_MESSAGE_BOX_ADDED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              showMessageBoxFromScout(e.getMessageBox());
            }
          };
          invokeUiLater(t);
          break;
        }
        case DesktopEvent.TYPE_FILE_CHOOSER_ADDED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              showFileChooserFromScout(e.getFileChooser());
            }
          };
          invokeUiLater(t);
          break;
        }
        case DesktopEvent.TYPE_OPEN_BROWSER_WINDOW: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              openBrowserWindowFromScout(e.getPath());
            }
          };
          invokeUiLater(t);
          break;
        }
        case DesktopEvent.TYPE_DESKTOP_CLOSED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              logout();
            }
          };
          invokeUiLater(t);
          break;
        }
        case DesktopEvent.TYPE_PRINT: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleScoutPrintInRwt(e);
            }
          };
          invokeUiLater(t);
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
            invokeUiLater(t);
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
  public void postImmediateUiJob(Runnable r) {
    synchronized (m_immediateUiJobsLock) {
      m_immediateUiJobs.add(r);
    }
  }

  @Override
  public void dispatchImmediateUiJobs() {
    List<Runnable> list;
    synchronized (m_immediateUiJobsLock) {
      list = new ArrayList<Runnable>(m_immediateUiJobs);
      m_immediateUiJobs.clear();
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
    synchronized (m_immediateUiJobsLock) {
      m_immediateUiJobs.clear();
    }
    if (m_synchronizer != null) {
      return m_synchronizer.invokeScoutLater(job, cancelTimeout);
    }
    else {
      LOG.warn("synchronizer is null; session is closed");
      return null;
    }
  }

  @Override
  public void invokeUiLater(Runnable job) {
    if (m_synchronizer != null) {
      m_synchronizer.invokeUiLater(job);
    }
    else {
      LOG.warn("synchronizer is null; session is closed");
    }
  }

  @Override
  public IClientSession getClientSession() {
    return m_clientSession;
  }

  @Override
  public LayoutValidateManager getLayoutValidateManager() {
    return m_layoutValidateManager;
  }

  // GUI FACTORY
  protected RwtIconLocator createIconLocator() {
    return new RwtIconLocator(getClientSession().getIconLocator());
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
  public IRwtScoutForm createForm(Composite parent, IForm scoutForm) {
    RwtScoutForm uiForm = new RwtScoutForm();
    uiForm.createUiField(parent, scoutForm, this);
    return uiForm;
  }

  /**
   * As default there is no form header created. <br/>
   * Subclasses can override this method to create one.
   */
  @Override
  public IRwtScoutFormHeader createFormHeader(Composite parent, IForm scoutForm) {
    return null;
  }

  /**
   * As default there is no form footer created. <br/>
   * Subclasses can override this method to create one.
   */
  @Override
  public IRwtScoutFormFooter createFormFooter(Composite parent, IForm scoutForm) {
    return null;
  }

  @Override
  public IRwtScoutFormField createFormField(Composite parent, IFormField model) {
    if (m_formFieldFactory == null) {
      m_formFieldFactory = new FormFieldFactory(getApplicationBundle());
    }
    IRwtScoutFormField<IFormField> uiField = m_formFieldFactory.createUiFormField(parent, model, this);
    return uiField;
  }

  @Override
  public void checkThread() {
    if (!(getDisplay().getThread() == Thread.currentThread())) {
      throw new IllegalStateException("Must be called in rwt thread");
    }
  }

  protected void handleScoutPrintInRwt(DesktopEvent e) {
    WidgetPrinter wp = new WidgetPrinter(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS));
    try {
      wp.print(e.getPrintDevice(), e.getPrintParameters());
    }
    catch (Throwable ex) {
      LOG.error(null, ex);
    }
  }

  protected String getDesktopOpenedTaskText() {
    return RwtUtility.getNlsText(Display.getCurrent(), "ScoutStarting");
  }

  protected String getDesktopClosedTaskText() {
    return RwtUtility.getNlsText(Display.getCurrent(), "ScoutStoping");
  }

  protected boolean isStartDesktopCalled() {
    return m_startDesktopCalled;
  }

  protected void setStartDesktopCalled(boolean startDesktopCalled) {
    m_startDesktopCalled = startDesktopCalled;
  }

  protected boolean isActivateDesktopCalled() {
    return m_activateDesktopCalled;
  }

  protected void setActivateDesktopCalled(boolean activateDesktopCalled) {
    m_activateDesktopCalled = activateDesktopCalled;
  }

  private class P_LocaleListener implements ILocaleListener {
    @Override
    public void localeChanged(LocaleChangeEvent event) {
      final Locale locale = event.getLocale();
      invokeUiLater(new Runnable() {
        @Override
        public void run() {
          if (!hasSameLocale(RWT.getLocale(), locale)) {
            setLocale(locale);
          }
        }
      });
    }

    private void setLocale(Locale locale) {
      RWT.setLocale(locale);
    }

    private boolean hasSameLocale(Locale locale1, Locale locale2) {
      boolean result = false;
      if (locale1 != null && locale2 != null) {
        result = locale1.equals(locale2);
      }
      return result;
    }
  }

  private final class P_RequestInterceptor implements PhaseListener {
    private static final long serialVersionUID = 1L;

    @Override
    public PhaseId getPhaseId() {
      return PhaseId.ANY;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
      if (event.getPhaseId() != PhaseId.PREPARE_UI_ROOT) {
        return;
      }

      beforeHttpRequestInternal();
    }

    @Override
    public void afterPhase(PhaseEvent event) {
      if (event.getPhaseId() != PhaseId.RENDER) {
        return;
      }

      afterHttpRequestInternal();
    }
  }

  private class P_DisplayDisposeListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      getDisplay().removeListener(SWT.Dispose, this);

      dispose();
    }

  }

  private static final class P_HttpSessionInvalidationListener implements HttpSessionBindingListener {
    private IClientSession m_clientSession;

    public P_HttpSessionInvalidationListener(IClientSession clientSession) {
      m_clientSession = clientSession;
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
      if (LOG.isInfoEnabled()) {
        UserAgent userAgent = m_clientSession.getUserAgent();
        String msg = "Thread: {0} Session goes down...; UserAgent: {2}";
        LOG.info(msg, new Object[]{Long.valueOf(Thread.currentThread().getId()), userAgent});
      }

      final IDesktop desktop = m_clientSession.getDesktop();
      if (!m_clientSession.isActive() || desktop == null || !desktop.isOpened()) {
        //client session was probably already stopped by the model itself
        return;
      }

      new ClientAsyncJob("HTTP session inactivator", m_clientSession) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          desktop.getUIFacade().fireDesktopClosingFromUI();
        }
      }.runNow(new NullProgressMonitor());
    }
  }

}
