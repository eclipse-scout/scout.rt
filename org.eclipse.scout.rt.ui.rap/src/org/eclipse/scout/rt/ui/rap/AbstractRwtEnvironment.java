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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.service.ISessionStore;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;
import org.eclipse.rwt.widgets.ExternalBrowser;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.HTMLUtility.DefaultFont;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.IBusyManagerService;
import org.eclipse.scout.rt.client.services.common.exceptionhandler.ErrorHandler;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.rap.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.rap.busy.RwtBusyHandler;
import org.eclipse.scout.rt.ui.rap.core.IRwtEnvironmentListener;
import org.eclipse.scout.rt.ui.rap.core.LayoutValidateManager;
import org.eclipse.scout.rt.ui.rap.core.RwtEnvironmentEvent;
import org.eclipse.scout.rt.ui.rap.core.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.core.concurrency.RwtScoutSynchronizer;
import org.eclipse.scout.rt.ui.rap.core.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.core.util.AbstractRwtUtility;
import org.eclipse.scout.rt.ui.rap.core.util.BrowserInfo;
import org.eclipse.scout.rt.ui.rap.core.util.ColorFactory;
import org.eclipse.scout.rt.ui.rap.core.util.FontRegistry;
import org.eclipse.scout.rt.ui.rap.core.util.RwtIconLocator;
import org.eclipse.scout.rt.ui.rap.core.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.core.window.desktop.navigation.RwtScoutNavigationSupport;
import org.eclipse.scout.rt.ui.rap.form.RwtScoutForm;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.KeyStrokeManager;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.util.ScoutFormToolkit;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartEvent;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartListener;
import org.eclipse.scout.rt.ui.rap.window.dialog.RwtScoutDialog;
import org.eclipse.scout.rt.ui.rap.window.filechooser.RwtScoutFileChooser;
import org.eclipse.scout.rt.ui.rap.window.filedownloader.RwtScoutDownloadHandler;
import org.eclipse.scout.rt.ui.rap.window.messagebox.RwtScoutMessageBoxDialog;
import org.eclipse.scout.rt.ui.rap.window.popup.RwtScoutPopup;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * <h3>AbstractRwtEnvironment</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public abstract class AbstractRwtEnvironment implements IRwtEnvironment {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractRwtEnvironment.class);

  private Subject m_subject;

  private int m_status;

  private Bundle m_applicationBundle;
  private RwtScoutSynchronizer m_synchronizer;
  private SessionStoreListener m_sessionStoreListener;

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

  private RwtScoutNavigationSupport m_historySupport;

  private LayoutValidateManager m_layoutValidateManager;

  public AbstractRwtEnvironment(Bundle applicationBundle, Class<? extends IClientSession> clientSessionClazz) {
    m_applicationBundle = applicationBundle;
    m_clientSessionClazz = clientSessionClazz;
    m_sessionStoreListener = new P_SessionStoreListener();
    m_environmentListeners = new EventListenerList();
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

  protected void stopScout() throws CoreException {
    try {
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

  @Override
  public boolean isInitialized() {
    return m_status == RwtEnvironmentEvent.STARTED;
  }

  @Override
  public final void ensureInitialized() {
    if (m_status == RwtEnvironmentEvent.INACTIVE || m_status == RwtEnvironmentEvent.STOPPED) {
      try {
        init();
      }
      catch (Exception e) {
        LOG.error("could not initialize Environment", e);
      }
    }
  }

  protected synchronized void init() throws CoreException {
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
    // workbench must exist
//    if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
//      throw new IllegalStateException("workbench must be active");
//    }
//    // close views that were opened due to workbench caching the latest layout
//    // of views
//    for (IWorkbenchWindow workbenchWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
//      for (IWorkbenchPage workbenchPage : workbenchWindow.getPages()) {
//        for (IViewReference viewReference : workbenchPage.getViewReferences()) {
//          if (m_scoutPartIdToUiPartId.containsValue(viewReference.getId())) {
//            if (workbenchPage.isPartVisible(viewReference.getPart(false))) {
//              workbenchPage.hideView(viewReference);
//            }
//          }
//        }
//      }
//    }
    //
    try {
      m_status = RwtEnvironmentEvent.STARTING;
      fireEnvironmentChanged(new RwtEnvironmentEvent(this, m_status));

      if (getSubject() == null) {
        throw new SecurityException("/rap request is not authenticated with a Subject");
      }
      initLocale();

      final BooleanHolder newSession = new BooleanHolder(true);
      IClientSession tempClientSession = (IClientSession) RWT.getSessionStore().getAttribute(IClientSession.class.getName());
      if (tempClientSession == null || !tempClientSession.isActive()) {
        tempClientSession = SERVICES.getService(IClientSessionRegistryService.class).newClientSession(m_clientSessionClazz, getSubject(), UUID.randomUUID().toString());
        RWT.getSessionStore().setAttribute(IClientSession.class.getName(), tempClientSession);
        RWT.getSessionStore().addSessionStoreListener(m_sessionStoreListener);

        newSession.setValue(true);
      }
      else {
        newSession.setValue(false);
      }
      if (!tempClientSession.isActive()) {
        showClientSessionLoadError(tempClientSession.getLoadError());
        LOG.error("ClientSession is not active, there must be a problem with loading or starting");
        m_status = RwtEnvironmentEvent.INACTIVE;
        return;
      }
      else {
        m_clientSession = tempClientSession;
      }
      if (m_synchronizer == null) {
        m_synchronizer = new RwtScoutSynchronizer(this);
      }
      //put the the display on the session data
      m_clientSession.setData(ENVIRONMENT_KEY, this);

      //
      RwtUtility.setNlsTextsOnDisplay(getDisplay(), m_clientSession.getNlsTexts());
      m_iconLocator = createIconLocator();
      m_colorFactory = new ColorFactory(getDisplay());
      m_keyStrokeManager = new KeyStrokeManager(this);
      m_fontRegistry = new FontRegistry(getDisplay());
      m_historySupport = new RwtScoutNavigationSupport(this);
      m_historySupport.install();
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
      // notify ui available
      // notify desktop that it is loaded
      UICallBack.activate(AbstractRwtEnvironment.class.getName() + AbstractRwtEnvironment.this.hashCode());
      new ClientSyncJob("Desktop opened", getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          if (newSession.getValue()) {
            fireDesktopOpenedFromUIInternal();
            fireGuiAttachedFromUIInternal();
          }
          else {
            fireGuiAttachedFromUIInternal();
            fireDesktopActivatedFromUIInternal();
          }
        }
      }.schedule();

      m_status = RwtEnvironmentEvent.STARTED;
      fireEnvironmentChanged(new RwtEnvironmentEvent(this, m_status));

      attachBusyHandler(m_clientSession);
    }
    finally {
      if (m_status == RwtEnvironmentEvent.STARTING) {
        m_status = RwtEnvironmentEvent.STOPPED;
        fireEnvironmentChanged(new RwtEnvironmentEvent(this, m_status));
      }
    }
  }

  protected RwtBusyHandler attachBusyHandler(IClientSession session) {
    IBusyManagerService service = SERVICES.getService(IBusyManagerService.class);
    if (service == null) {
      return null;
    }
    RwtBusyHandler handler = new RwtBusyHandler(session, this);
    service.register(session, handler);
    return handler;
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

  protected void fireDesktopActivatedFromUIInternal() {
    if (getScoutDesktop() != null) {
      getScoutDesktop().ensureViewStackVisible();
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
    /*
     * HTML: <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
     * XHTML: <!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
     * TODO rwt Issue as long as rwt-index.html uses HTML and not XHTML. tables and other nexted elements ignore style of div parent.
     * Alternative is using color:inherit etc. in inner tables but this does not work in IE8.
     * Therefore we adapt the style tag of <table> and <a> tags.
     */
    int size = 12;
    if (uiComposite.getUiField() != null) {
      FontData[] fa = uiComposite.getUiField().getFont().getFontData();
      if (fa != null && fa.length > 0) {
        if (fa[0].getHeight() > 0) {
          size = fa[0].getHeight();
        }
      }
    }
    String stylePrefix = "color:inherit;background-color:inherit;font-size:" + size + "px;";
    rawHtml = bugfixStyles(rawHtml, stylePrefix);
    return rawHtml;
  }

  private static final Pattern tableTagPattern = Pattern.compile("<table([^>]*)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern styleAttributePattern = Pattern.compile("style\\s*=\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private String bugfixStyles(String html, String stylePrefix) {
    Matcher m = tableTagPattern.matcher(html);
    StringBuilder buf = new StringBuilder();
    int lastPos = 0;
    while (m.find()) {
      buf.append(html.substring(lastPos, m.start()));
      String atts = m.group(1);
      Matcher m2 = styleAttributePattern.matcher(atts);
      if (m2.find()) {
        buf.append(html.substring(m.start(), m.start(1) + m2.start(1)));
        buf.append(stylePrefix);
        buf.append(html.substring(m.start(1) + m2.start(1), m.end()));
      }
      else {
        buf.append(html.substring(m.start(), m.start(1)));
        buf.append(" style=\"");
        buf.append(stylePrefix);
        buf.append("\" ");
        buf.append(html.substring(m.start(1), m.end()));
      }
      lastPos = m.end();
    }
    if (lastPos < html.length()) {
      buf.append(html.substring(lastPos));
    }
    return buf.toString();
  }

  @Override
  public String styleHtmlText(IRwtScoutFormField<?> uiComposite, String rawHtml) {
    if (rawHtml == null) {
      rawHtml = "";
    }
    String cleanHtml = rawHtml;

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
        cleanHtml = HTMLUtility.cleanupHtml(cleanHtml, true, false, createDefaultFontSettings(uiComposite));
      }
    }

    return cleanHtml;
  }

  /**
   * Get SWT specific default font settings
   */
  protected DefaultFont createDefaultFontSettings(IRwtScoutFormField<?> uiComposite) {
    DefaultFont defaultFont = new DefaultFont();
    defaultFont.setSize(12);
    defaultFont.setSizeUnit("px");
    defaultFont.setForegroundColor(0x000000);
    defaultFont.setFamilies(new String[]{"sans-serif"});

    if (uiComposite != null && uiComposite.getUiField() != null) {
      FontData[] fontData = uiComposite.getUiField().getFont().getFontData();
      if (fontData == null || fontData.length <= 0) {
        Label label = new Label(uiComposite.getUiContainer(), SWT.NONE);
        fontData = label.getFont().getFontData();
        label.dispose();
      }
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
      Color color = uiComposite.getUiField().getForeground();
      if (color != null) {
        defaultFont.setForegroundColor(color.getRed() * 0x10000 + color.getGreen() * 0x100 + color.getBlue());
      }
    }
    return defaultFont;
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
  @Override
  public void addGlobalKeyStroke(IRwtKeyStroke stroke, boolean exclusive) {
    boolean internalExclusive = exclusive;
    //If F5 is set we wan't to have this exclusive to the application, else the browser will reload the page
    if (SWT.F5 == stroke.getKeyCode()) {
      internalExclusive = true;
    }
    m_keyStrokeManager.addGlobalKeyStroke(stroke, internalExclusive);
  }

  @Override
  public boolean removeGlobalKeyStroke(IRwtKeyStroke stroke) {
    return m_keyStrokeManager.removeGlobalKeyStroke(stroke);
  }

  @Override
  public void addKeyStroke(Control control, IRwtKeyStroke stoke, boolean exclusive) {
    m_keyStrokeManager.addKeyStroke(control, stoke, exclusive);
  }

  @Override
  public boolean removeKeyStroke(Control control, IRwtKeyStroke stoke) {
    return m_keyStrokeManager.removeKeyStroke(control, stoke);
  }

  @Override
  public boolean removeKeyStrokes(Control control) {
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
        showFormPart(form);
      }
    }
    // dialogs
    IForm[] dialogs = desktop.getDialogStack();
    for (IForm dialog : dialogs) {
      // showDialogFromScout(dialogs[i]);
      showFormPart(dialog);
    }
    IMessageBox[] messageBoxes = desktop.getMessageBoxStack();
    for (IMessageBox messageBoxe : messageBoxes) {
      showMessageBoxFromScout(messageBoxe);
    }
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
    RwtScoutFileChooser sfc = new RwtScoutFileChooser(getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS), fileChooser);
    sfc.showFileChooser();
  }

  protected File validatePath(String path) throws IOException {
    String px = path.replace('\\', File.separatorChar);
    File file = new File(px);
    if (file.exists()) {
      px = file.getCanonicalPath();
      String osName = System.getProperty("os.name");
      if (osName != null && osName.startsWith("Mac OS")) {
        //mac is not able to open files with a space, even when in quotes
        String ext = px.substring(px.lastIndexOf('.'));
        File f = new File(file.getParentFile(), "" + System.nanoTime() + ext);
        file.renameTo(f);
        f.deleteOnExit();
      }
    }
    return file;
  }

  @Override
  public void openBrowserWindowFromScout(String path) {
    String nextId = Long.toString(new Random(this.hashCode()).nextLong());

    if ((StringUtility.find(path, "http://") >= 0)
        || (StringUtility.find(path, "https://") >= 0)) {
      ExternalBrowser.open(nextId, path, ExternalBrowser.STATUS | ExternalBrowser.LOCATION_BAR | ExternalBrowser.NAVIGATION_BAR);
    }
    else {
      try {
        File file = validatePath(path);
        final RwtScoutDownloadHandler handler = new RwtScoutDownloadHandler(nextId, file, "", file.getName());
        Shell parentShell = getParentShellIgnoringPopups(SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.MODELESS);
        parentShell.addDisposeListener(new DisposeListener() {
          private static final long serialVersionUID = 1L;

          @Override
          public void widgetDisposed(DisposeEvent event) {
            handler.dispose();
          }
        });
        handler.startDownload(parentShell);
      }
      catch (IOException e) {
        LOG.error("Unexpected: " + path, e);
      }
    }
  }

  @Override
  public void showMessageBoxFromScout(IMessageBox messageBox) {
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
    if (getScoutDesktop() != null) {
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
              getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                  UICallBack.deactivate(AbstractRwtEnvironment.class.getName() + AbstractRwtEnvironment.this.hashCode());
                }
              });
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
              getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                  UICallBack.deactivate(AbstractRwtEnvironment.class.getName() + AbstractRwtEnvironment.this.hashCode());
                }
              });
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
              try {
                stopScout();
              }
              catch (CoreException ex) {
                LOG.error("desktop closed", ex);
              }
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

  /**
   * {@inheritDoc}
   */
  @Override
  public Shell getParentShellIgnoringPopups(int modalities) {
    return RwtUtility.getParentShellIgnoringPopups(modalities);
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

  protected void initLocale() {
    Locale locale = RwtUtility.getBrowserInfo().getLocale();
    if (locale == null) {
      locale = ClientUIPreferences.getInstance().getLocale();
    }
    if (locale != null) {
      LocaleThreadLocal.set(locale);
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

  private final class P_SessionStoreListener implements SessionStoreListener {
    private static final long serialVersionUID = 1L;

    @Override
    public void beforeDestroy(SessionStoreEvent event) {
      ISessionStore sessionStore = event.getSessionStore();
      String userAgent = "";
      BrowserInfo browserInfo = (BrowserInfo) sessionStore.getAttribute(AbstractRwtUtility.BROWSER_INFO);
      if (browserInfo != null) {
        userAgent = browserInfo.getUserAgent();
      }
      String msg = "Thread: {0} Session goes down...; UserAgent: {2}";
      LOG.warn(msg, new Object[]{Long.valueOf(Thread.currentThread().getId()), userAgent});

      IClientSession clientSession = (IClientSession) sessionStore.getAttribute(IClientSession.class.getName());
      if (clientSession != null) {
        clientSession.stopSession();
      }
    }
  }
}
