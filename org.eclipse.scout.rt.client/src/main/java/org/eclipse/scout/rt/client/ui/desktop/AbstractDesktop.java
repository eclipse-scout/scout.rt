/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.deeplink.DeepLinkException;
import org.eclipse.scout.rt.client.deeplink.IDeepLinks;
import org.eclipse.scout.rt.client.deeplink.OutlineDeepLinkHandler;
import org.eclipse.scout.rt.client.extension.ui.desktop.DefaultDesktopEventHistory;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopBeforeClosingChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopClosingChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopDefaultViewChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopFormAboutToShowChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopGuiAttachedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopGuiDetachedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopInitChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopOpenedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopOutlineChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageDetailFormChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageDetailTableChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageSearchFormChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopTablePageLoadedChain;
import org.eclipse.scout.rt.client.services.common.bookmark.internal.BookmarkUtility;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.transformation.IDeviceTransformationService;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.IFormMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.ExtensionUtility;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The desktop model (may) consist of
 * <ul>
 * <li>set of available outlines
 * <li>active outline
 * <li>active table view
 * <li>active detail form
 * <li>active search form
 * <li>form stack (swing: dialogs on desktop as {@code JInternalFrame}s)
 * <li>dialog stack of modal and non-modal dialogs (swing: dialogs as {@code JDialog}, {@code JFrame})
 * <li>active message box stack
 * <li>menubar menus
 * <li>toolbar and viewbar actions
 * </ul>
 * The Eclipse Scout SDK creates a subclass of this class that can be used as initial desktop.
 */
@SuppressWarnings("deprecation")
public abstract class AbstractDesktop extends AbstractPropertyObserver implements IDesktop, IContributionOwner, IExtensibleObject {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractDesktop.class);

  private IEventHistory<DesktopEvent> m_eventHistory;
  private final IDesktopExtension m_localDesktopExtension;
  private List<IDesktopExtension> m_desktopExtensions;
  private final EventListenerList m_listenerList;
  private int m_dataChanging;
  private final List<Object[]> m_dataChangeEventBuffer;
  private final Map<Object, EventListenerList> m_dataChangeListenerList;
  private final IDesktopUIFacade m_uiFacade;
  private List<IOutline> m_availableOutlines;
  private IOutline m_outline;
  private boolean m_outlineChanging = false;
  private P_ActiveOutlineListener m_activeOutlineListener;
  private ITable m_pageDetailTable;
  private IForm m_pageDetailForm;
  private IForm m_pageSearchForm;
  private final FormStore m_formStore;
  private final MessageBoxStore m_messageBoxStore;
  private final FileChooserStore m_fileChooserStore;
  private List<IMenu> m_menus;
  private List<IViewButton> m_viewButtons;
  private boolean m_autoPrefixWildcardForTextSearch;
  private boolean m_desktopInitialized;
  private boolean m_isForcedClosing = false;
  private final List<Object> m_addOns;
  private IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractDesktop, org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> m_objectExtensions;

  /**
   * do not instantiate a new desktop<br>
   * get it via {@code ClientScoutSession.getSession().getModelManager()}
   */
  public AbstractDesktop() {
    this(true);
  }

  public AbstractDesktop(boolean callInitializer) {
    m_localDesktopExtension = new P_LocalDesktopExtension();
    m_listenerList = new EventListenerList();
    m_dataChangeListenerList = new HashMap<>();
    m_dataChangeEventBuffer = new ArrayList<>();
    m_formStore = BEANS.get(FormStore.class);
    m_messageBoxStore = BEANS.get(MessageBoxStore.class);
    m_fileChooserStore = BEANS.get(FileChooserStore.class);
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent().withDesktop(this));
    m_addOns = new ArrayList<>();
    m_objectExtensions = new ObjectExtensions<>(this, true);
    if (callInitializer) {
      callInitializer();
    }
  }

  protected final void callInitializer() {
    // Run the initialization on behalf of this Desktop.
    ClientRunContexts.copyCurrent().withDesktop(this).run(new IRunnable() {
      @Override
      public void run() throws Exception {
        interceptInitConfig();
      }
    });
  }

  protected IEventHistory<DesktopEvent> createEventHistory() {
    return new DefaultDesktopEventHistory(5000L);
  }

  @Override
  public IEventHistory<DesktopEvent> getEventHistory() {
    return m_eventHistory;
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  /*
   * Configuration
   */
  /**
   * Configures the title of this desktop. The title is typically used as title for the main application window.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the title of this desktop
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredTitle() {
    return null;
  }

  /**
   * Configures the css class(es) of this tree.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(15)
  protected String getConfiguredCssClass() {
    return null;
  }

  /**
   * Configures the outlines associated with this desktop. If multiple outlines are configured, there is typically a
   * need to provide some means of switching between different outlines, such as a {@link AbstractOutlineViewButton}.
   * <p>
   * Note that {@linkplain IDesktopExtension desktop extensions} might contribute additional outlines to this desktop.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return an array of outline type tokens
   * @see IOutline
   */
  @ConfigProperty(ConfigProperty.OUTLINES)
  @Order(20)
  protected List<Class<? extends IOutline>> getConfiguredOutlines() {
    return null;
  }

  /**
   * @return <code>true</code> if UI key strokes to select view tabs are enabled, <code>false</code> otherwise. Default
   *         value is <code>true</code>.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredSelectViewTabsKeyStrokesEnabled() {
    return true;
  }

  /**
   * @return optional modifier to use for UI key strokes to select view tabs (only relevant when
   *         {@link #isSelectViewTabsKeyStrokesEnabled()} is <code>true</code>). Default value is
   *         {@link IKeyStroke#CONTROL}.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(40)
  protected String getConfiguredSelectViewTabsKeyStrokeModifier() {
    return IKeyStroke.CONTROL;
  }

  /**
   * Configures the desktop style which defines the basic layout of the application in the UI.
   * <p>
   * Subclasses can override this method. Default is {@link IDesktop#DISPLAY_STYLE_DEFAULT}
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(50)
  protected String getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  /**
   * Configures the logo id.
   * <p>
   * If specified, the logo will be displayed on the top right corner of the desktop.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @since 6.0
   * @see IIconProviderService
   */
  protected String getConfiguredLogoId() {
    return null;
  }

  /**
   * Configures whether the position of the splitter between the navigation and bench should be stored in the session
   * storage, so that the position may be restored after a page reload. If set to false, the default position is used.
   *
   * @return {@code true} if the splitter position should be cached, false if not
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredCacheSplitterPosition() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredNavigationVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(75)
  protected boolean getConfiguredNavigationHandleVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredHeaderVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredBenchVisible() {
    return true;
  }

  private List<Class<? extends IAction>> getConfiguredActions() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IAction>> fca = ConfigurationUtility.filterClasses(dca, IAction.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  /**
   * Called while this desktop is initialized.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(10)
  protected void execInit() {
  }

  /**
   * Called after this desktop was opened and displayed on the GUI.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(20)
  protected void execOpened() {
  }

  /**
   * Called just after the core desktop receives the request to close the desktop.
   * <p>
   * Subclasses can override this method to execute some custom code before the desktop gets into its closing state. The
   * default behavior is to do nothing. By throwing an explicit {@link VetoException} the closing process will be
   * stopped.
   */
  @ConfigOperation
  @Order(30)
  protected void execBeforeClosing() {
  }

  /**
   * Called before this desktop is being closed.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(40)
  protected void execClosing() {
  }

  /**
   * Called after a UI has been attached to this desktop. The desktop is opened at this point. This method may be called
   * more than once. It is called immediately after the desktop has been opened and also in case of a reload, when a new
   * UiSession has been created.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(50)
  protected void execGuiAttached() {
  }

  /**
   * Called after a UI has been detached from this desktop. This desktop must not necessarily be open.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(60)
  protected void execGuiDetached() {
  }

  /**
   * Called whenever a new outline has been activated on this desktop.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param oldOutline
   *          old outline that was active before
   * @param newOutline
   *          new outline that is active after the change
   */
  @ConfigOperation
  @Order(70)
  protected void execOutlineChanged(IOutline oldOutline, IOutline newOutline) {
  }

  /**
   * Method invoked right before the given form is shown and therefore added to the desktop. That is before any UI is
   * informed about the new form.<br/>
   * Overwrite this method to modify the given form, or to replace it with another form instance. The default
   * implementation simply returns the given form.
   *
   * @param form
   *          the form which is about to show.
   * @return the form to show, or <code>null</code> to not show the form.
   */
  @ConfigOperation
  @Order(80)
  protected IForm execFormAboutToShow(IForm form) {
    return form;
  }

  /**
   * Called whenever a new page has been activated (selected) on this desktop.
   * <p>
   * Subclasses can override this method.<br/>
   * This default implementation does nothing.
   *
   * @param oldForm
   *          is the search form of the old (not selected anymore) page or {@code null}
   * @param newForm
   *          is the search form of the new (selected) page or {@code null}
   */
  @Order(90)
  @ConfigOperation
  protected void execPageSearchFormChanged(IForm oldForm, IForm newForm) {
  }

  /**
   * Called whenever a new page has been activated (selected) on this desktop.
   * <p>
   * Subclasses can override this method.<br/>
   * This default implementation does nothing.
   *
   * @param oldForm
   *          is the detail form of the old (not selected anymore) page or {@code null}
   * @param newForm
   *          is the detail form of the new (selected) page or {@code null}
   */
  @Order(100)
  @ConfigOperation
  protected void execPageDetailFormChanged(IForm oldForm, IForm newForm) {
  }

  /**
   * Called whenever a new page has been activated (selected) on this desktop.
   * <p>
   * Subclasses can override this method.<br/>
   * This default implementation keeps track of the current outline table form and updates it accordingly (including
   * visibility). See also {@link #getOutlineTableForm()}.
   *
   * @param oldTable
   *          is the table of the old (not selected anymore) table page or {@code null}
   * @param newTable
   *          is the table of the new (selected) table page or {@code null}
   */
  @Order(110)
  @ConfigOperation
  protected void execPageDetailTableChanged(ITable oldTable, ITable newTable) {
  }

  /**
   * Called after a table page was loaded or reloaded.
   * <p>
   * Subclasses can override this method.<br/>
   * This default implementation minimizes the page search form when data has been found.
   *
   * @param tablePage
   *          the table page that has been (re)loaded
   */
  @Order(120)
  @ConfigOperation
  protected void execTablePageLoaded(IPageWithTable<?> tablePage) {
    ISearchForm searchForm = tablePage.getSearchFormInternal();
    if (searchForm != null) {
      searchForm.setMinimized(tablePage.getTable().getRowCount() > 0);
    }
  }

  /**
   * Called by activateDefaultView when Scout application was not started with a deep-link or when deep-link could not
   * be executed. The default implementation does nothing.
   */
  @Order(130)
  @ConfigOperation
  protected void execDefaultView() {
  }

  public List<IDesktopExtension> getDesktopExtensions() {
    return CollectionUtility.arrayList(m_desktopExtensions);
  }

  /**
   * @return the special extension that contributes the contents of this desktop itself
   */
  protected IDesktopExtension getLocalDesktopExtension() {
    return m_localDesktopExtension;
  }

  protected org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop> createLocalExtension() {
    return new LocalDesktopExtension<AbstractDesktop>(this);
  }

  @Override
  public final List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    m_eventHistory = createEventHistory();
    // add convenience observer for event history
    addDesktopListener(new DesktopListener() {
      @Override
      public void desktopChanged(DesktopEvent e) {
        IEventHistory<DesktopEvent> h = getEventHistory();
        if (h != null) {
          h.notifyEvent(e);
        }
      }
    });

    BEANS.get(IDeviceTransformationService.class).install(this);
    initDesktopExtensions();
    setTitle(getConfiguredTitle());
    setCssClass(getConfiguredCssClass());
    setSelectViewTabsKeyStrokesEnabled(getConfiguredSelectViewTabsKeyStrokesEnabled());
    setSelectViewTabsKeyStrokeModifier(getConfiguredSelectViewTabsKeyStrokeModifier());
    setLogoId(getConfiguredLogoId());
    setNavigationVisible(getConfiguredNavigationVisible());
    setNavigationHandleVisible(getConfiguredNavigationHandleVisible());
    setBenchVisible(getConfiguredBenchVisible());
    setHeaderVisible(getConfiguredHeaderVisible());
    setDisplayStyle(getConfiguredDisplayStyle());
    initDisplayStyle(getDisplayStyle());
    setCacheSplitterPosition(getConfiguredCacheSplitterPosition());
    List<IDesktopExtension> extensions = getDesktopExtensions();
    m_contributionHolder = new ContributionComposite(this);

    // outlines
    OrderedCollection<IOutline> outlines = new OrderedCollection<IOutline>();
    for (IDesktopExtension ext : extensions) {
      try {
        ext.contributeOutlines(outlines);
      }
      catch (Exception t) {
        LOG.error("contributing outlines by {}", ext, t);
      }
    }
    List<IOutline> contributedOutlines = m_contributionHolder.getContributionsByClass(IOutline.class);
    outlines.addAllOrdered(contributedOutlines);

    // move outlines
    ExtensionUtility.moveModelObjects(outlines);
    m_availableOutlines = outlines.getOrderedList();

    // actions (keyStroke, menu, viewButton, toolButton)
    List<IAction> actionList = new ArrayList<IAction>();
    for (IDesktopExtension ext : extensions) {
      try {
        ext.contributeActions(actionList);
      }
      catch (Exception t) {
        LOG.error("contributing actions by {}", ext, t);
      }
    }
    List<IAction> contributedActions = m_contributionHolder.getContributionsByClass(IAction.class);
    actionList.addAll(contributedActions);

    // build complete menu and viewButton lists
    // only top level menus
    OrderedComparator orderedComparator = new OrderedComparator();
    List<IMenu> menuList = new ActionFinder().findActions(actionList, IMenu.class, false);
    ExtensionUtility.moveModelObjects(menuList);
    Collections.sort(menuList, orderedComparator);
    m_menus = menuList;

    List<IViewButton> viewButtonList = new ActionFinder().findActions(actionList, IViewButton.class, false);
    ExtensionUtility.moveModelObjects(viewButtonList);
    Collections.sort(viewButtonList, orderedComparator);
    m_viewButtons = viewButtonList;

    // add dynamic keyStrokes
    List<IKeyStroke> ksList = new ActionFinder().findActions(actionList, IKeyStroke.class, true);
    for (IKeyStroke ks : ksList) {
      try {
        ks.initAction();
      }
      catch (RuntimeException e) {
        LOG.error("could not initialize key stroke '{}'.", ks, e);
      }
    }
    addKeyStrokes(ksList.toArray(new IKeyStroke[ksList.size()]));

    // init outlines
    for (IOutline o : m_availableOutlines) {
      try {
        o.initTree();
      }
      catch (Exception e) {
        LOG.error("Could not init outline {}", o, e);
      }
    }
    addPropertyChangeListener(new P_LocalPropertyChangeListener());
  }

  protected final void interceptInit() {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop>> extensions = getAllExtensions();
    DesktopInitChain chain = new DesktopInitChain(extensions);
    chain.execInit();
  }

  private void initDesktopExtensions() {
    m_desktopExtensions = new LinkedList<IDesktopExtension>();
    m_desktopExtensions.add(getLocalDesktopExtension());
    injectDesktopExtensions(m_desktopExtensions);
  }

  /**
   * Override to provide a set of extensions (modules) that contribute their content to this desktop.
   * <p>
   * The default list contains only the {@link #getLocalDesktopExtension()}
   * </p>
   * <p>
   * The extension that are held by this desktop must call {@link IDesktopExtension#setCoreDesktop(this)} before using
   * the extension. That way the extension can use and access this desktop's methods.
   * </p>
   *
   * @param desktopExtensions
   *          a live list can be modified.
   */
  protected void injectDesktopExtensions(List<IDesktopExtension> desktopExtensions) {
    List<IDesktopExtension> extensions = BEANS.all(IDesktopExtension.class);
    for (IDesktopExtension e : extensions) {
      e.setCoreDesktop(this);
    }
    desktopExtensions.addAll(extensions);
  }

  @Override
  public void initDesktop() {
    if (m_desktopInitialized) {
      return;
    }
    // extensions
    for (IDesktopExtension ext : getDesktopExtensions()) {
      try {
        ContributionCommand cc = ext.initDelegate();
        if (cc == ContributionCommand.Stop) {
          break;
        }
      }
      catch (Exception t) {
        LOG.error("extension {} failed", ext, t);
      }
    }
    ActionUtility.initActions(getActions());
    m_desktopInitialized = true;
  }

  protected void initDisplayStyle(String style) {
    if (DISPLAY_STYLE_BENCH.equals(style)) {
      setNavigationVisible(false);
      setNavigationHandleVisible(false);
      setHeaderVisible(false);
      setBenchVisible(true);
    }
    else if (DISPLAY_STYLE_COMPACT.equals(style)) {
      setNavigationVisible(true);
      setNavigationHandleVisible(false);
      setHeaderVisible(false);
      setBenchVisible(false);
    }
    else {
      setNavigationVisible(true);
      setNavigationHandleVisible(true);
      setHeaderVisible(true);
      setBenchVisible(true);
    }
  }

  @Override
  public boolean isShowing(IForm form) {
    if (form == null) {
      return false;
    }

    // wrapped form
    if (form.getOuterForm() != null) {
      return form.getOuterForm().isShowing();
    }

    // dialog or view
    if (m_formStore.contains(form)) {
      return true;
    }

    // active detail or search Form
    if (form == m_pageDetailForm || form == m_pageSearchForm) {
      return true;
    }

    // active menu Form
    for (IMenu menu : getMenus()) {
      if (menu instanceof IFormMenu && menu.isSelected() && ((IFormMenu) menu).getForm() == form) {
        return true;
      }
    }

    return false;
  }

  @Override
  public <FORM extends IForm> FORM findForm(Class<FORM> formType) {
    if (formType == null) {
      return null;
    }

    for (final IForm candidate : m_formStore.values()) {
      if (formType.isAssignableFrom(candidate.getClass())) {
        @SuppressWarnings("unchecked")
        FORM form = (FORM) candidate;
        return form;
      }
    }
    return null;
  }

  @Override
  public <FORM extends IForm> List<FORM> findForms(final Class<FORM> formType) {
    if (formType == null) {
      return CollectionUtility.emptyArrayList();
    }

    final List<FORM> forms = new ArrayList<FORM>();
    for (final IForm candidate : m_formStore.values()) {
      if (formType.isAssignableFrom(candidate.getClass())) {
        @SuppressWarnings("unchecked")
        FORM form = (FORM) candidate;
        forms.add(form);
      }
    }
    return forms;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IOutline> T findOutline(Class<T> outlineType) {
    for (IOutline o : getAvailableOutlines()) {
      if (outlineType.isAssignableFrom(o.getClass())) {
        return (T) o;
      }
    }
    return null;
  }

  @Override
  public <T extends IAction> T findAction(Class<T> actionType) {
    return new ActionFinder().findAction(getActions(), actionType);
  }

  /**
   * @deprecated use {@link #findAction(Class)}, will be removed in Scout 6.1
   */
  @Override
  @Deprecated
  public <T extends IToolButton> T findToolButton(Class<T> toolButtonType) {
    return findAction(toolButtonType);
  }

  @Override
  public <T extends IMenu> T findMenu(Class<T> menuType) {
    return findAction(menuType);
  }

  @Override
  public <T extends IViewButton> T findViewButton(Class<T> viewButtonType) {
    return findAction(viewButtonType);
  }

  @Override
  public String getDisplayStyle() {
    return propertySupport.getPropertyString(PROP_DISPLAY_STYLE);
  }

  @Override
  public void setDisplayStyle(String displayStyle) {
    propertySupport.setProperty(PROP_DISPLAY_STYLE, displayStyle);
  }

  @Override
  public String getLogoId() {
    return propertySupport.getPropertyString(PROP_LOGO_ID);
  }

  @Override
  public void setLogoId(String id) {
    propertySupport.setPropertyString(PROP_LOGO_ID, id);
  }

  /**
   * this feature isn't supported in html ui
   *
   * @deprecated will be removed in o-release
   */
  @Override
  @Deprecated
  public IFormField getFocusOwner() {
    return fireFindFocusOwner();
  }

  @Override
  public IForm getActiveForm() {
    return (IForm) propertySupport.getProperty(PROP_ACTIVE_FORM);
  }

  protected void setActiveForm(IForm form) {
    propertySupport.setProperty(PROP_ACTIVE_FORM, form);
  }

  @Override
  public List<IForm> getForms(IDisplayParent displayParent) {
    return m_formStore.getByDisplayParent(displayParent);
  }

  @Override
  public List<IForm> getViewStack() {
    return getViews();
  }

  @Override
  public List<IForm> getViews() {
    return m_formStore.getViews();
  }

  @Override
  public List<IForm> getViews(IDisplayParent displayParent) {
    return m_formStore.getViewsByDisplayParent(displayParent);
  }

  @Override
  public <F extends IForm, H extends IFormHandler> List<F> findAllOpenViews(Class<? extends F> formClass, Class<? extends H> handlerClass, Object exclusiveKey) {
    List<F> forms = new ArrayList<>();
    if (exclusiveKey != null) {
      for (IForm view : getViews()) {
        if (getPageDetailForm() == view || getPageSearchForm() == view) {
          continue;
        }
        Object candidateKey = view.computeExclusiveKey();
        if (candidateKey == null) {
          continue;
        }
        else {
          LOG.debug("form: {} vs {}", candidateKey, exclusiveKey);
          if (exclusiveKey.equals(candidateKey)
              && view.getClass() == formClass
              && view.getHandler() != null
              && view.getHandler().getClass() == handlerClass
              && view.getHandler().isOpenExclusive()) {
            forms.add(formClass.cast(view));
          }
        }
      }
    }
    return forms;
  }

  @Override
  public <F extends IForm, H extends IFormHandler> F findOpenView(Class<? extends F> formClass, Class<? extends H> handlerClass, Object exclusiveKey) {
    return CollectionUtility.firstElement(findAllOpenViews(formClass, handlerClass, exclusiveKey));
  }

  @Override
  public List<IForm> getDialogStack() {
    return getDialogs();
  }

  @Override
  public List<IForm> getDialogs() {
    return m_formStore.getDialogs();
  }

  @Override
  public List<IForm> getDialogs(final IDisplayParent displayParent, final boolean includeChildDialogs) {
    final List<IForm> dialogs = new ArrayList<>();

    for (final IForm dialog : m_formStore.getDialogsByDisplayParent(displayParent)) {
      // Add the dialog's child dialogs first.
      if (includeChildDialogs) {
        dialogs.addAll(getDialogs(dialog, true));
      }

      // Add the dialog.
      dialogs.add(dialog);
    }

    return dialogs;
  }

  @Override
  public List<IMessageBox> getMessageBoxStack() {
    return getMessageBoxes();
  }

  @Override
  public List<IMessageBox> getMessageBoxes() {
    return m_messageBoxStore.values();
  }

  @Override
  public List<IMessageBox> getMessageBoxes(IDisplayParent displayParent) {
    return m_messageBoxStore.getByDisplayParent(displayParent);
  }

  @Override
  // TODO [5.2] dwi: Clarify whether this method is still used.
  public List<IForm> getSimilarViewForms(final IForm form) {
    if (form == null) {
      return CollectionUtility.emptyArrayList();
    }

    final Object formKey;
    try {
      formKey = form.computeExclusiveKey();
    }
    catch (final RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
      return CollectionUtility.emptyArrayList();
    }

    if (formKey == null) {
      return CollectionUtility.emptyArrayList();
    }

    final IForm currentDetailForm = getPageDetailForm();
    final IForm currentSearchForm = getPageSearchForm();

    final List<IForm> similarForms = new ArrayList<>();
    for (final IForm candidateView : m_formStore.getViewsByKey(formKey)) {
      if (candidateView == currentDetailForm) {
        continue;
      }
      if (candidateView == currentSearchForm) {
        continue;
      }

      if (candidateView.getClass().equals(form.getClass())) {
        similarForms.add(candidateView);
      }
    }

    return similarForms;
  }

  @Override
  public void ensureViewStackVisible() { // TODO [5.2] dwi: Clarify whether this method is still used.
    for (IForm view : m_formStore.getViews()) {
      activateForm(view);
    }
  }

  @Override

  public void ensureVisible(IForm form) {
    activateForm(form);
  }

  @Override
  public void activateForm(IForm form) {
    if (form == null) {
      setActiveForm(form);
      return;
    }

    if (!m_formStore.contains(form)) {
      return; // only dialogs or views can be activated.
    }

    IDisplayParent displayParent = form.getDisplayParent();
    if (displayParent instanceof IForm) {
      activateForm((IForm) displayParent);
    }
    else if (displayParent instanceof IOutline && !displayParent.equals(getOutline())) {
      activateOutline(((IOutline) displayParent));
    }
    setActiveForm(form);
    fireFormActivate(form);
  }

  @Override
  public void activateOutline(IOutline outline) {
    activateOutlineInternal(outline);
    fireOutlineContentActivate();
  }

  protected void activateOutlineInternal(IOutline outline) {
    final IOutline newOutline = resolveOutline(outline);
    if (m_outline == newOutline || m_outlineChanging) {
      return;
    }

    ClientRunContexts.copyCurrent()
        .withOutline(newOutline, true)
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              m_outlineChanging = true;
              setOutlineInternal(newOutline);
            }
            finally {
              m_outlineChanging = false;
            }
          }
        });
  }

  protected void setOutlineInternal(IOutline newOutline) {
    if (m_outline == newOutline) {
      return;
    }
    //
    IOutline oldOutline = m_outline;
    if (m_activeOutlineListener != null && oldOutline != null) {
      oldOutline.removeTreeListener(m_activeOutlineListener);
      oldOutline.removePropertyChangeListener(m_activeOutlineListener);
      m_activeOutlineListener = null;
    }
    // set new outline to set facts
    m_outline = newOutline;
    // deactivate old page
    if (oldOutline != null) {
      oldOutline.clearContextPage();
    }
    //
    if (m_outline != null) {
      m_activeOutlineListener = new P_ActiveOutlineListener();
      m_outline.addTreeListener(m_activeOutlineListener);
      m_outline.addPropertyChangeListener(m_activeOutlineListener);
      setBrowserHistoryEntry(BEANS.get(OutlineDeepLinkHandler.class).createBrowserHistoryEntry(m_outline));
    }
    // <bsh 2010-10-15>
    // Those three "setXyz(null)" statements used to be called unconditionally. Now, they
    // are only called when the new outline is null. When the new outline is _not_ null, we
    // will override the "null" anyway (see below).
    // This change is needed for the "on/off semantics" of the tool tab buttons to work correctly.
    if (m_outline == null) {
      setPageDetailForm(null);
      setPageDetailTable(null);
      setPageSearchForm(null, true);
    }
    // </bsh>
    updateActiveFormOnOutlineChanged();
    fireOutlineChanged(oldOutline, m_outline);
    onOutlineChangedInternal();
  }

  // FIXME awe: review with C.GU - is it sufficient to set the active form to null?
  // or do we have to find another suitable active form? I guess the UI will send
  // the new active form later...
  protected void updateActiveFormOnOutlineChanged() {
    // If active form is not set or parent of active form is desktop
    // we must do nothing
    IForm activeForm = getActiveForm();
    if (activeForm == null || activeForm.getDisplayParent() == this) {
      return;
    }

    // Does the active form belong to the current outline?
    // if not, we must set the active form to null otherwise we do nothing
    List<IForm> formsByOutline = m_formStore.getByDisplayParent(m_outline);
    if (!formsByOutline.contains(activeForm)) {
      setActiveForm(null);
    }
  }

  /**
   * Called after the outline has been changed
   */
  protected void onOutlineChangedInternal() {
    if (m_outline == null) {
      return;
    }
    // reload selected page in case it is marked dirty
    if (m_outline.getActivePage() != null) {
      try {
        m_outline.getActivePage().ensureChildrenLoaded();
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
    m_outline.setNodeExpanded(m_outline.getRootNode(), true);
    setPageDetailForm(m_outline.getDetailForm());
    setPageDetailTable(m_outline.getDetailTable());
    setPageSearchForm(m_outline.getSearchForm(), true);
    m_outline.makeActivePageToContextPage();
  }

  @Override
  public void addForm(IForm form) {
    showForm(form);
  }

  @Override
  public void showForm(IForm form) {
    // Let the desktop extensions to intercept the given form.
    final IHolder<IForm> formHolder = new Holder<>(form);
    for (IDesktopExtension extension : getDesktopExtensions()) {
      try {
        if (extension.formAboutToShowDelegate(formHolder) == ContributionCommand.Stop) {
          break;
        }
      }
      catch (Exception e) {
        formHolder.setValue(form);
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
    form = formHolder.getValue();

    // Only show the form if not null nor already showing.
    if (form == null || m_formStore.contains(form)) {
      return;
    }

    // Validate the Form's modality configuration.
    Assertions.assertNotNull(form.getDisplayParent(), "Property 'displayParent' must not be null");
    boolean applicationModal = (form.isModal() && form.getDisplayParent() == this);
    boolean view = (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW);

    // Ensure not to show a 'application-modal' view. Otherwise, no user interaction would be possible.
    Assertions.assertFalse(view && applicationModal, "'desktop-modality' not supported for views");

    // Ensure not to show a modal Form if the application is in 'application-modal' state. Otherwise, no user interaction would be possible.
    if (view && form.isModal()) {
      boolean applicationLocked = m_formStore.containsApplicationModalDialogs() || m_messageBoxStore.containsApplicationModalMessageBoxes() || m_fileChooserStore.containsApplicationModalFileChoosers();
      Assertions.assertFalse(applicationLocked, "Modal view cannot be showed because application is in 'desktop-modal' state; otherwise, no user interaction would be possible.");
    }

    m_formStore.add(form);
    activateForm(form);
    fireFormShow(form);
  }

  @Override
  public void removeForm(IForm form) {
    hideForm(form);
  }

  @Override
  public void hideForm(IForm form) {
    if (form == null || !m_formStore.contains(form)) {
      return;
    }

    // Unset the currently active Form if being the given Form.
    // The new active Form will be set by the UI or manually by IDesktop.activateForm(IForm).
    if (getActiveForm() == form) {
      setActiveForm(null);
    }
    m_formStore.remove(form);
    fireFormHide(form);
  }

  @Override
  public void addMessageBox(final IMessageBox messageBox) {
    showMessageBox(messageBox);
  }

  @Override
  public void showMessageBox(IMessageBox messageBox) {
    if (messageBox == null || m_messageBoxStore.contains(messageBox)) {
      return;
    }

    // Ensure 'displayParent' to be set.
    Assertions.assertNotNull(messageBox.getDisplayParent(), "Property 'displayParent' must not be null");

    m_messageBoxStore.add(messageBox);
    fireMessageBoxShow(messageBox);
  }

  @Override
  public void removeMessageBox(IMessageBox messageBox) {
    hideMessageBox(messageBox);
  }

  @Override
  public void hideMessageBox(IMessageBox messageBox) {
    if (messageBox == null || !m_messageBoxStore.contains(messageBox)) {
      return;
    }

    m_messageBoxStore.remove(messageBox);
    fireMessageBoxHide(messageBox);
  }

  @Override
  public boolean isShowing(IMessageBox messageBox) {
    return m_messageBoxStore.contains(messageBox);
  }

  @Override
  public List<IOutline> getAvailableOutlines() {
    return CollectionUtility.arrayList(m_availableOutlines);
  }

  @Override
  public void setAvailableOutlines(List<? extends IOutline> availableOutlines) {
    setOutline((IOutline) null);
    m_availableOutlines = CollectionUtility.arrayList(availableOutlines);
  }

  @Override
  public IOutline getOutline() {
    return m_outline;
  }

  @Override
  public boolean isOutlineChanging() {
    return m_outlineChanging;
  }

  @Override
  public void setOutline(IOutline outline) {
    activateOutline(outline);
  }

  @Override
  public void activateFirstPage() {
    if (m_outline.isRootNodeVisible()) {
      m_outline.selectNode(m_outline.getRootNode(), false);
    }
    else {
      List<ITreeNode> children = m_outline.getRootNode().getChildNodes();
      if (CollectionUtility.hasElements(children)) {
        for (ITreeNode node : children) {
          if (node.isVisible() && node.isEnabled()) {
            m_outline.selectNode(node, false);
            break;
          }
        }
      }
    }
  }

  private IOutline resolveOutline(IOutline outline) {
    for (IOutline o : getAvailableOutlines()) {
      if (o == outline) {
        return o;
      }
    }
    return null;
  }

  @Override
  public void setOutline(Class<? extends IOutline> outlineType) {
    if (outlineType == null) {
      return;
    }
    for (IOutline o : getAvailableOutlines()) {
      if (outlineType.isInstance(o)) {
        setOutline(o);
        return;
      }
    }
  }

  @Override
  public Set<IKeyStroke> getKeyStrokes() {
    return CollectionUtility.hashSet(propertySupport.<IKeyStroke> getPropertySet(PROP_KEY_STROKES));
  }

  @Override
  public void setKeyStrokes(Collection<? extends IKeyStroke> ks) {
    propertySupport.setPropertySet(PROP_KEY_STROKES, CollectionUtility.<IKeyStroke> hashSetWithoutNullElements(ks));
  }

  @Override
  public void addKeyStrokes(IKeyStroke... keyStrokes) {
    if (keyStrokes != null && keyStrokes.length > 0) {
      Map<String, IKeyStroke> map = new HashMap<String, IKeyStroke>();
      for (IKeyStroke ks : getKeyStrokes()) {
        map.put(ks.getKeyStroke(), ks);
      }
      for (IKeyStroke ks : keyStrokes) {
        map.put(ks.getKeyStroke(), ks);
      }
      setKeyStrokes(map.values());
    }
  }

  @Override
  public void removeKeyStrokes(IKeyStroke... keyStrokes) {
    if (keyStrokes != null && keyStrokes.length > 0) {
      Map<String, IKeyStroke> map = new HashMap<String, IKeyStroke>();
      for (IKeyStroke ks : getKeyStrokes()) {
        map.put(ks.getKeyStroke(), ks);
      }
      for (IKeyStroke ks : keyStrokes) {
        map.remove(ks.getKeyStroke());
      }
      setKeyStrokes(map.values());
    }
  }

  @Override
  public List<IMenu> getMenus() {
    return CollectionUtility.arrayList(m_menus);
  }

  @Override
  public List<IAction> getActions() {
    List<IAction> result = new ArrayList<IAction>();
    result.addAll(getKeyStrokes());
    result.addAll(getMenus());
    result.addAll(getViewButtons());
    return result;
  }

  @Override
  public <T extends IMenu> T getMenu(Class<? extends T> searchType) {
    // ActionFinder performs instance-of checks. Hence the menu replacement mapping is not required
    return new ActionFinder().findAction(getMenus(), searchType);
  }

  @Deprecated
  @Override
  public <T extends IToolButton> T getToolButton(Class<? extends T> searchType) {
    // ActionFinder performs instance-of checks. Hence the toolbutton replacement mapping is not required
    return new ActionFinder().findAction(getMenus(), searchType);
  }

  @Override
  public List<IMenu> getToolButtons() {
    return getMenus();
  }

  @Override
  public <T extends IViewButton> T getViewButton(Class<? extends T> searchType) {
    // ActionFinder performs instance-of checks. Hence the viewbutton replacement mapping is not required
    return new ActionFinder().findAction(getViewButtons(), searchType);
  }

  @Override
  public List<IViewButton> getViewButtons() {
    return CollectionUtility.arrayList(m_viewButtons);
  }

  @Override
  public IForm getPageDetailForm() {
    return m_pageDetailForm;
  }

  @Override
  public void setPageDetailForm(IForm f) {
    if (m_pageDetailForm != f) {
      IForm oldForm = m_pageDetailForm;
      m_pageDetailForm = f;
      //extensions
      for (IDesktopExtension ext : getDesktopExtensions()) {
        try {
          ContributionCommand cc = ext.pageDetailFormChangedDelegate(oldForm, m_pageDetailForm);
          if (cc == ContributionCommand.Stop) {
            break;
          }
        }
        catch (Exception e) {
          LOG.error("extension {}", ext, e);
        }
      }
    }
  }

  @Override
  public IForm getPageSearchForm() {
    return m_pageSearchForm;
  }

  @Override
  public void setPageSearchForm(IForm f) {
    setPageSearchForm(f, false);
  }

  public void setPageSearchForm(IForm f, boolean force) {
    if (force || m_pageSearchForm != f) {
      IForm oldForm = m_pageSearchForm;
      m_pageSearchForm = f;
      //extensions
      for (IDesktopExtension ext : getDesktopExtensions()) {
        try {
          ContributionCommand cc = ext.pageSearchFormChangedDelegate(oldForm, m_pageSearchForm);
          if (cc == ContributionCommand.Stop) {
            break;
          }
        }
        catch (Exception t) {
          LOG.error("extension {}", ext, t);
        }
      }
    }
  }

  @Override
  public ITable getPageDetailTable() {
    return m_pageDetailTable;
  }

  @Override
  public void setPageDetailTable(ITable t) {
    if (m_pageDetailTable != t) {
      ITable oldTable = m_pageDetailTable;
      m_pageDetailTable = t;
      //extensions
      for (IDesktopExtension ext : getDesktopExtensions()) {
        try {
          ContributionCommand cc = ext.pageDetailTableChangedDelegate(oldTable, m_pageDetailTable);
          if (cc == ContributionCommand.Stop) {
            break;
          }
        }
        catch (Exception x) {
          LOG.error("extension {}", ext, x);
        }
      }
    }
  }

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  @Override
  public String getCssClass() {
    return propertySupport.getPropertyString(PROP_CSS_CLASS);
  }

  @Override
  public void setCssClass(String cssClass) {
    propertySupport.setPropertyString(PROP_CSS_CLASS, cssClass);
  }

  @Override
  public boolean isSelectViewTabsKeyStrokesEnabled() {
    return propertySupport.getPropertyBool(PROP_SELECT_VIEW_TABS_KEY_STROKES_ENABLED);
  }

  @Override
  public void setSelectViewTabsKeyStrokesEnabled(boolean selectViewTabsKeyStrokesEnabled) {
    propertySupport.setPropertyBool(PROP_SELECT_VIEW_TABS_KEY_STROKES_ENABLED, selectViewTabsKeyStrokesEnabled);
  }

  @Override
  public String getSelectViewTabsKeyStrokeModifier() {
    return propertySupport.getPropertyString(PROP_SELECT_VIEW_TABS_KEY_STROKE_MODIFIER);
  }

  @Override
  public void setSelectViewTabsKeyStrokeModifier(String selectViewTabsKeyStrokeModifier) {
    propertySupport.setPropertyString(PROP_SELECT_VIEW_TABS_KEY_STROKE_MODIFIER, selectViewTabsKeyStrokeModifier);
  }

  @Override
  public void addNotification(IDesktopNotification notification) {
    fireNotification(DesktopEvent.TYPE_NOTIFICATION_ADDED, notification);
  }

  @Override
  public void removeNotification(IDesktopNotification notification) {
    fireNotification(DesktopEvent.TYPE_NOTIFICATION_REMOVED, notification);
  }

  @Override
  public BrowserHistoryEntry getBrowserHistoryEntry() {
    return (BrowserHistoryEntry) propertySupport.getProperty(PROP_BROWSER_HISTORY_ENTRY);
  }

  @Override
  public void setBrowserHistoryEntry(BrowserHistoryEntry browserHistory) {
    propertySupport.setProperty(PROP_BROWSER_HISTORY_ENTRY, browserHistory);
  }

  @Override
  public boolean isCacheSplitterPosition() {
    return propertySupport.getPropertyBool(PROP_CACHE_SPLITTER_POSITION);
  }

  @Override
  public void setCacheSplitterPosition(boolean b) {
    propertySupport.setPropertyBool(PROP_CACHE_SPLITTER_POSITION, b);
  }

  @Override
  public List<IFileChooser> getFileChooserStack() {
    return getFileChoosers();
  }

  @Override
  public List<IFileChooser> getFileChoosers() {
    return m_fileChooserStore.values();
  }

  @Override
  public List<IFileChooser> getFileChoosers(IDisplayParent displayParent) {
    return m_fileChooserStore.getByDisplayParent(displayParent);
  }

  @Override
  public void addFileChooser(final IFileChooser fileChooser) {
    showFileChooser(fileChooser);
  }

  @Override
  public void showFileChooser(IFileChooser fileChooser) {
    if (fileChooser == null || m_fileChooserStore.contains(fileChooser)) {
      return;
    }

    // Ensure 'displayParent' to be set.
    Assertions.assertNotNull(fileChooser.getDisplayParent(), "Property 'displayParent' must not be null");

    m_fileChooserStore.add(fileChooser);
    fireFileChooserShow(fileChooser);
  }

  @Override
  public void hideFileChooser(IFileChooser fileChooser) {
    if (fileChooser == null || !m_fileChooserStore.contains(fileChooser)) {
      return;
    }

    m_fileChooserStore.remove(fileChooser);
    fireFileChooserHide(fileChooser);
  }

  @Override
  public boolean isShowing(IFileChooser fileChooser) {
    return m_fileChooserStore.contains(fileChooser);
  }

  @Override
  public void openUri(String url, IOpenUriAction openUriAction) {
    Assertions.assertNotNull(url);
    Assertions.assertNotNull(openUriAction);
    fireOpenUri(url, openUriAction);
  }

  @Override
  public void openUri(BinaryResource res, IOpenUriAction openUriAction) {
    Assertions.assertNotNull(res);
    Assertions.assertNotNull(openUriAction);
    fireOpenUri(res, openUriAction);
  }

  @Override
  public boolean isAutoPrefixWildcardForTextSearch() {
    return m_autoPrefixWildcardForTextSearch;
  }

  @Override
  public void setAutoPrefixWildcardForTextSearch(boolean b) {
    m_autoPrefixWildcardForTextSearch = b;
  }

  @Override
  public boolean isOpened() {
    return propertySupport.getPropertyBool(PROP_OPENED);
  }

  private void setOpenedInternal(boolean b) {
    propertySupport.setPropertyBool(PROP_OPENED, b);
  }

  private void setGuiAvailableInternal(boolean guiAvailable) {
    propertySupport.setPropertyBool(PROP_GUI_AVAILABLE, guiAvailable);
  }

  @Override
  public boolean isGuiAvailable() {
    return propertySupport.getPropertyBool(PROP_GUI_AVAILABLE);
  }

  @Override
  public void addDesktopListener(DesktopListener l) {
    m_listenerList.add(DesktopListener.class, l);
  }

  @Override
  public void addDesktopListenerAtExecutionEnd(DesktopListener l) {
    m_listenerList.insertAtFront(DesktopListener.class, l);
  }

  @Override
  public void removeDesktopListener(DesktopListener l) {
    m_listenerList.remove(DesktopListener.class, l);
  }

  @Override
  public void addDataChangeListener(DataChangeListener listener, Object... dataTypes) {
    if (dataTypes == null || dataTypes.length == 0) {
      EventListenerList list = m_dataChangeListenerList.get(null);
      if (list == null) {
        list = new EventListenerList();
        m_dataChangeListenerList.put(null, list);
      }
      list.add(DataChangeListener.class, listener);
    }
    else {
      for (Object dataType : dataTypes) {
        if (dataType != null) {
          EventListenerList list = m_dataChangeListenerList.get(dataType);
          if (list == null) {
            list = new EventListenerList();
            m_dataChangeListenerList.put(dataType, list);
          }
          list.add(DataChangeListener.class, listener);
        }
      }
    }
  }

  @Override
  public void removeDataChangeListener(DataChangeListener listener, Object... dataTypes) {
    if (dataTypes == null || dataTypes.length == 0) {
      for (Iterator<EventListenerList> it = m_dataChangeListenerList.values().iterator(); it.hasNext();) {
        EventListenerList list = it.next();
        list.removeAll(DataChangeListener.class, listener);
        if (list.getListenerCount(DataChangeListener.class) == 0) {
          it.remove();
        }
      }
    }
    else {
      for (Object dataType : dataTypes) {
        if (dataType != null) {
          EventListenerList list = m_dataChangeListenerList.get(dataType);
          if (list != null) {
            list.remove(DataChangeListener.class, listener);
            if (list.getListenerCount(DataChangeListener.class) == 0) {
              m_dataChangeListenerList.remove(dataType);
            }
          }
        }
      }
    }
  }

  @Override
  public boolean isDataChanging() {
    return m_dataChanging > 0;
  }

  @Override
  public void setDataChanging(boolean b) {
    if (b) {
      m_dataChanging++;
    }
    else {
      if (m_dataChanging > 0) {
        m_dataChanging--;
        if (m_dataChanging == 0) {
          processDataChangeBuffer();
        }
      }
    }
  }

  @Override
  public void dataChanged(Object... dataTypes) {
    if (isDataChanging()) {
      if (dataTypes != null && dataTypes.length > 0) {
        m_dataChangeEventBuffer.add(dataTypes);
      }
    }
    else {
      fireDataChangedImpl(dataTypes);
    }
  }

  private void processDataChangeBuffer() {
    Set<Object> knownEvents = new HashSet<Object>();
    for (Object[] dataTypes : m_dataChangeEventBuffer) {
      for (Object dataType : dataTypes) {
        knownEvents.add(dataType);
      }
    }
    m_dataChangeEventBuffer.clear();
    fireDataChangedImpl(knownEvents.toArray(new Object[knownEvents.size()]));
  }

  private void fireDataChangedImpl(Object... dataTypes) {
    if (dataTypes != null && dataTypes.length > 0) {
      // Important: Use LinkedHashMaps to make event firing deterministic!
      // (If listeners would be called in random order, bugs may not be reproduced very well.)
      HashMap<DataChangeListener, Set<Object>> map = new LinkedHashMap<>();
      for (Object dataType : dataTypes) {
        if (dataType != null) {
          EventListenerList list = m_dataChangeListenerList.get(dataType);
          if (list != null) {
            for (DataChangeListener listener : list.getListeners(DataChangeListener.class)) {
              Set<Object> typeSet = map.get(listener);
              if (typeSet == null) {
                typeSet = new LinkedHashSet<Object>();
                map.put(listener, typeSet);
              }
              typeSet.add(dataType);
            }
          }
        }
      }
      for (Map.Entry<DataChangeListener, Set<Object>> e : map.entrySet()) {
        DataChangeListener listener = e.getKey();
        Set<Object> typeSet = e.getValue();
        listener.dataChanged(typeSet.toArray());
      }
    }
  }

  private void fireDesktopClosed() {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_DESKTOP_CLOSED);
    fireDesktopEvent(e);
  }

  private void fireOutlineChanged(IOutline oldOutline, IOutline newOutline) {
    if (oldOutline != newOutline) {
      //extensions
      for (IDesktopExtension ext : getDesktopExtensions()) {
        try {
          ContributionCommand cc = ext.outlineChangedDelegate(oldOutline, newOutline);
          if (cc == ContributionCommand.Stop) {
            break;
          }
        }
        catch (RuntimeException e) {
          BEANS.get(ExceptionHandler.class).handle(e);
        }
      }
    }
    // fire
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_OUTLINE_CHANGED, newOutline);
    fireDesktopEvent(e);
  }

  private void fireOutlineContentActivate() {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_OUTLINE_CONTENT_ACTIVATE);
    fireDesktopEvent(e);
  }

  private void fireFormShow(IForm form) {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_FORM_SHOW, form);
    fireDesktopEvent(e);
  }

  private void fireFormActivate(IForm form) {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_FORM_ACTIVATE, form);
    fireDesktopEvent(e);
  }

  private void fireFormHide(IForm form) {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_FORM_HIDE, form);
    fireDesktopEvent(e);
  }

  private void fireMessageBoxShow(IMessageBox messageBox) {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_MESSAGE_BOX_SHOW, messageBox);
    fireDesktopEvent(e);
  }

  private void fireMessageBoxHide(IMessageBox messageBox) {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_MESSAGE_BOX_HIDE, messageBox);
    fireDesktopEvent(e);
  }

  private void fireFileChooserShow(IFileChooser fileChooser) {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_FILE_CHOOSER_SHOW, fileChooser);
    fireDesktopEvent(e);
  }

  private void fireFileChooserHide(IFileChooser fileChooser) {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_FILE_CHOOSER_HIDE, fileChooser);
    fireDesktopEvent(e);
  }

  private void fireOpenUri(String uri, IOpenUriAction openUriAction) {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_OPEN_URI, uri, openUriAction);
    fireDesktopEvent(e);
  }

  private void fireOpenUri(BinaryResource res, IOpenUriAction openUriAction) {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_OPEN_URI, res, openUriAction);
    fireDesktopEvent(e);
  }

  private IFormField fireFindFocusOwner() {
    DesktopEvent e = new DesktopEvent(this, DesktopEvent.TYPE_FIND_FOCUS_OWNER);
    fireDesktopEvent(e);
    return e.getFocusedField();
  }

  private void fireNotification(int eventType, IDesktopNotification notification) {
    DesktopEvent e = new DesktopEvent(this, eventType, notification);
    fireDesktopEvent(e);
  }

  // main handler
  private void fireDesktopEvent(DesktopEvent event) {
    DesktopListener[] listeners = m_listenerList.getListeners(DesktopListener.class);
    if (listeners != null && listeners.length > 0) {
      for (DesktopListener listener : listeners) {
        listener.desktopChanged(event);
      }
    }
  }

  @Override
  public void activateBookmark(Bookmark bm) {
    activateBookmark(bm, true);
  }

  @Override
  public void activateBookmark(Bookmark bm, boolean activateOutline) {
    BookmarkUtility.activateBookmark(this, bm, activateOutline);
  }

  @Override
  public Bookmark createBookmark() {
    return BookmarkUtility.createBookmark(this);
  }

  @Override
  public Bookmark createBookmark(IPage<?> page) {
    return BookmarkUtility.createBookmark(page);
  }

  @Override
  public void refreshPages(Class<?>... pageTypes) {
    for (IOutline outline : getAvailableOutlines()) {
      outline.refreshPages(pageTypes);
    }
  }

  @Override
  public void refreshPages(List<Class<? extends IPage>> pages) {
    for (IOutline outline : getAvailableOutlines()) {
      outline.refreshPages(pages);
    }
  }

  @Override
  public void releaseUnusedPages() {
    for (IOutline outline : getAvailableOutlines()) {
      outline.releaseUnusedPages();
    }
  }

  @Override
  public void afterTablePageLoaded(IPageWithTable<?> tablePage) {
    //extensions
    for (IDesktopExtension ext : getDesktopExtensions()) {
      try {
        ContributionCommand cc = ext.tablePageLoadedDelegate(tablePage);
        if (cc == ContributionCommand.Stop) {
          break;
        }
      }
      catch (Exception t) {
        LOG.error("extension {}", ext, t);
      }
    }
  }

  @Override
  public void closeInternal() {
    setOpenedInternal(false);
    detachGui();

    List<IForm> showedForms = new ArrayList<IForm>();
    // Remove showed forms
    for (IForm form : m_formStore.values()) {
      hideForm(form);
      showedForms.add(form);
    }
    //extensions
    for (IDesktopExtension ext : getDesktopExtensions()) {
      try {
        ContributionCommand cc = ext.desktopClosingDelegate();
        if (cc == ContributionCommand.Stop) {
          break;
        }
      }
      catch (RuntimeException t) {
        LOG.error("extension {}", ext, t);
      }
    }

    // close open forms
    for (IForm form : showedForms) {
      if (form != null) {
        try {
          form.doClose();
        }
        catch (RuntimeException e) {
          LOG.error("Exception while closing form", e);
        }
      }
    }

    // outlines
    for (IOutline outline : getAvailableOutlines()) {
      try {
        outline.disposeTree();
      }
      catch (RuntimeException e) {
        LOG.warn("Exception while disposing outline.", e);
      }
    }

    ActionUtility.disposeActions(getActions());

    fireDesktopClosed();
  }

  private void attachGui(String deepLinkPath) {
    if (isGuiAvailable()) {
      return;
    }
    setGuiAvailableInternal(true);

    // extensions
    for (IDesktopExtension ext : getDesktopExtensions()) {
      try {
        ContributionCommand cc = ext.guiAttachedDelegate();
        if (cc == ContributionCommand.Stop) {
          break;
        }
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }

    activateDefaultView(deepLinkPath);
  }

  /**
   * Activates the initial, default view of the Desktop. This method checks whether the Scout application has been
   * started with a deep-link. If this applies, the deep-link is executed. Otherwise or if the deep-link has caused an
   * error execDefaultView is called.
   *
   * @param deepLinkPath
   *          The deep-link path which was passed as URL parameter to the Scout application (if available). This
   *          parameter may be null. See: {@link IDeepLinks}.
   */
  protected void activateDefaultView(String deepLinkPath) {
    try {
      if (!handleDeepLink(deepLinkPath)) {
        interceptDefaultView();
      }
    }
    catch (DeepLinkException e) {
      LOG.warn("Failed to execute deep-link '{}', reason: {}", deepLinkPath, e.getMessage());
      interceptDefaultView();
      showDeepLinkError(e);
    }
  }

  /**
   * Checks whether the given path is a valid deep-link. If that is the case the deep-link is handled. When an error
   * occurs while handling the deep-link a {@link DeepLinkException} is thrown, usually this exception is thrown when
   * the business-logic failed to find the resource addressed by the deep-link (similar to the 404 HTTP error status).
   *
   * @param deepLinkPath
   * @return
   * @throws DeepLinkException
   *           when deep-link could not be executed
   */
  protected boolean handleDeepLink(String deepLinkPath) throws DeepLinkException {
    if (hasApplicationModalElement()) {
      LOG.debug("Could not handle deep-link because modal element prevents changes on UI state. deepLinkPath={}", deepLinkPath);
      return false;
    }

    if (StringUtility.isNullOrEmpty(deepLinkPath)) {
      return false;
    }

    boolean handled = false;
    IDeepLinks deepLinks = BEANS.get(IDeepLinks.class);
    if (deepLinks.canHandleDeepLink(deepLinkPath)) {
      handled = deepLinks.handleDeepLink(deepLinkPath);
    }
    return handled;
  }

  protected boolean hasApplicationModalElement() {
    if (m_messageBoxStore.containsApplicationModalMessageBoxes()) {
      return true;
    }
    if (m_formStore.containsApplicationModalDialogs()) {
      return true;
    }
    return m_fileChooserStore.containsApplicationModalFileChoosers();
  }

  /**
   * Displays a message box showing a generic message that the deep-link could not be executed.
   */
  protected void showDeepLinkError(DeepLinkException e) {
    if (e.getCause() instanceof VetoException) {
      // permission problems
      BEANS.get(ExceptionHandler.class).handle(e.getCause());
    }
    else {
      // other problems
      String errorMessage = TEXTS.get("DeepLinkError");
      MessageBoxes.createOk().withBody(errorMessage).show();
    }
  }

  private void detachGui() {
    if (!isGuiAvailable()) {
      return;
    }
    setGuiAvailableInternal(false);

    // extensions
    for (IDesktopExtension ext : getDesktopExtensions()) {
      try {
        ContributionCommand cc = ext.guiDetachedDelegate();
        if (cc == ContributionCommand.Stop) {
          break;
        }
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  public boolean runMenu(Class<? extends IMenu> menuType) {
    for (IMenu m : getMenus()) {
      if (runMenuRec(m, menuType)) {
        return true;
      }
    }
    return false;
  }

  private boolean runMenuRec(IMenu m, Class<? extends IMenu> menuType) {
    if (m.getClass() == menuType) {
      if (m.isVisible() && m.isEnabled()) {
        m.doAction();
        return true;
      }
      else {
        return false;
      }
    }
    // children
    for (IMenu c : m.getChildActions()) {
      if (runMenuRec(c, menuType)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Collection<Object> getAddOns() {
    return Collections.unmodifiableCollection(m_addOns);
  }

  @Override
  public void addAddOn(Object addOn) {
    m_addOns.add(addOn);
  }

  @Override
  public IDesktopUIFacade getUIFacade() {
    return m_uiFacade;
  }

  private boolean isForcedClosing() {
    return m_isForcedClosing;
  }

  private void setForcedClosing(boolean forcedClosing) {
    m_isForcedClosing = forcedClosing;
  }

  @Override
  public void setNavigationVisible(boolean visible) {
    propertySupport.setProperty(PROP_NAVIGATION_VISIBLE, visible);
  }

  @Override
  public boolean isNavigationVisible() {
    return (boolean) propertySupport.getProperty(PROP_NAVIGATION_VISIBLE);
  }

  @Override
  public void setNavigationHandleVisible(boolean visible) {
    propertySupport.setProperty(PROP_NAVIGATION_HANDLE_VISIBLE, visible);
  }

  @Override
  public boolean isNavigationHandleVisible() {
    return (boolean) propertySupport.getProperty(PROP_NAVIGATION_HANDLE_VISIBLE);
  }

  @Override
  public void setBenchVisible(boolean visible) {
    propertySupport.setProperty(PROP_BENCH_VISIBLE, visible);
  }

  @Override
  public boolean isBenchVisible() {
    return (boolean) propertySupport.getProperty(PROP_BENCH_VISIBLE);
  }

  @Override
  public void setHeaderVisible(boolean visible) {
    propertySupport.setProperty(PROP_HEADER_VISIBLE, visible);
  }

  @Override
  public boolean isHeaderVisible() {
    return (boolean) propertySupport.getProperty(PROP_HEADER_VISIBLE);
  }

  /**
   * local desktop extension that calls local exec methods and returns local contributions in this class itself
   */
  private class P_LocalDesktopExtension implements IDesktopExtension {
    @Override
    public IDesktop getCoreDesktop() {
      return AbstractDesktop.this;
    }

    @Override
    public void setCoreDesktop(IDesktop desktop) {
      // NOP
    }

    @Override
    public void contributeOutlines(OrderedCollection<IOutline> outlines) {
      List<Class<? extends IOutline>> configuredOutlines = getConfiguredOutlines();
      if (configuredOutlines != null) {
        for (Class<?> element : configuredOutlines) {
          try {
            IOutline o = (IOutline) element.newInstance();
            outlines.addOrdered(o);
          }
          catch (Exception e) {
            BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + element.getName() + "'.", e));
          }
        }
      }
    }

    @Override
    public void contributeActions(Collection<IAction> actions) {
      for (Class<? extends IAction> actionClazz : getConfiguredActions()) {
        actions.add(ConfigurationUtility.newInnerInstance(AbstractDesktop.this, actionClazz));
      }
    }

    @Override
    public ContributionCommand initDelegate() {
      interceptInit();
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand desktopOpenedDelegate() {
      interceptOpened();
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand desktopBeforeClosingDelegate() {
      interceptBeforeClosing();
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand desktopClosingDelegate() {
      interceptClosing();
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand guiAttachedDelegate() {
      interceptGuiAttached();
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand guiDetachedDelegate() {
      interceptGuiDetached();
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand outlineChangedDelegate(IOutline oldOutline, IOutline newOutline) {
      interceptOutlineChanged(oldOutline, newOutline);
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand formAboutToShowDelegate(IHolder<IForm> formHolder) {
      formHolder.setValue(interceptFormAboutToShow(formHolder.getValue()));
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand pageSearchFormChangedDelegate(IForm oldForm, IForm newForm) {
      interceptPageSearchFormChanged(oldForm, newForm);
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand pageDetailFormChangedDelegate(IForm oldForm, IForm newForm) {
      interceptPageDetailFormChanged(oldForm, newForm);
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand pageDetailTableChangedDelegate(ITable oldTable, ITable newTable) {
      interceptPageDetailTableChanged(oldTable, newTable);
      return ContributionCommand.Continue;
    }

    @Override
    public ContributionCommand tablePageLoadedDelegate(IPageWithTable<?> tablePage) {
      interceptTablePageLoaded(tablePage);
      return ContributionCommand.Continue;
    }
  }

  protected class P_UIFacade implements IDesktopUIFacade {

    @Override
    public void historyEntryActivatedFromUI(String deepLinkPath) {
      try {
        handleDeepLink(deepLinkPath);
      }
      catch (DeepLinkException e) {
        showDeepLinkError(e);
      }
    }

    @Override
    public void fireGuiAttached(String deepLinkPath) {
      attachGui(deepLinkPath);
    }

    @Override
    public void fireGuiDetached() {
      detachGui();
    }

    @Override
    public void openFromUI() {
      setOpenedInternal(true);
      //extensions
      for (IDesktopExtension ext : getDesktopExtensions()) {
        try {
          ContributionCommand cc = ext.desktopOpenedDelegate();
          if (cc == ContributionCommand.Stop) {
            break;
          }
        }
        catch (Exception e) {
          BEANS.get(ExceptionHandler.class).handle(e);
        }
      }
    }

    @Override
    public void closeFromUI(boolean forcedClosing) {
      setForcedClosing(forcedClosing);
      // necessary so that no forms can be opened anymore.
      if (forcedClosing) {
        setOpenedInternal(false);
      }
      ClientSessionProvider.currentSession().stop();
    }

    @Override
    public void setNavigationVisibleFromUI(boolean visible) {
      setNavigationVisible(visible);
    }

    @Override
    public void setHeaderVisibleFromUI(boolean visible) {
      setHeaderVisible(visible);
    }

    @Override
    public void setBenchVisibleFromUI(boolean visible) {
      setBenchVisible(visible);
    }
  }

  private class P_ActiveOutlineListener extends TreeAdapter implements PropertyChangeListener {
    @Override
    public void treeChanged(TreeEvent e) {
      switch (e.getType()) {
        case TreeEvent.TYPE_NODES_SELECTED: {
          try {
            ClientSessionProvider.currentSession().getMemoryPolicy().afterOutlineSelectionChanged(AbstractDesktop.this);
          }
          catch (Exception t) {
            LOG.warn("MemoryPolicy.afterOutlineSelectionChanged", t);
          }
          break;
        }
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(IOutline.PROP_DETAIL_FORM)) {
        setPageDetailForm(((IOutline) e.getSource()).getDetailForm());
      }
      else if (e.getPropertyName().equals(IOutline.PROP_DETAIL_TABLE)) {
        setPageDetailTable(((IOutline) e.getSource()).getDetailTable());
      }
      else if (e.getPropertyName().equals(IOutline.PROP_SEARCH_FORM)) {
        setPageSearchForm(((IOutline) e.getSource()).getSearchForm());
      }
    }
  }

  /**
   * <p>
   * Default behavior of the pre-hook is to delegate the before closing call to the each desktop extension. The method
   * {@link IDesktopExtension#desktopBeforeClosingDelegate()} of each desktop extension will be called. If at least one
   * of the desktop extension's delegate throws a {@link VetoException}, the closing process will be stopped.
   * </p>
   * <p>
   * Additionally, before closing, a dialog is shown to allow the user to save unsaved forms. At this point it is also
   * possible to cancel closing.
   * </p>
   * If the flag {@link AbstractDesktop#m_isForcedClosing} is set to <code>true</code>, closing is always continued,
   * without considering desktopExtensions or unsaved forms.
   */
  @Override
  public boolean doBeforeClosingInternal() {
    return isForcedClosing() || (continueClosingInDesktopExtensions() && continueClosingConsideringUnsavedForms());
  }

  protected boolean continueClosingConsideringUnsavedForms() {
    List<IForm> forms = getUnsavedForms();
    if (forms.size() > 0) {
      try {
        UnsavedFormChangesForm f = new UnsavedFormChangesForm(forms);
        f.startNew();
        f.waitFor();
        if (f.getCloseSystemType() == IButton.SYSTEM_TYPE_CANCEL) {
          return false;
        }
      }
      catch (RuntimeException e) {
        LOG.error("Error closing forms", e);
      }
    }
    return true;
  }

  private boolean continueClosingInDesktopExtensions() {
    boolean continueClosing = true;
    List<IDesktopExtension> extensions = getDesktopExtensions();
    if (extensions != null) {
      for (IDesktopExtension ext : extensions) {
        try {
          ContributionCommand cc = ext.desktopBeforeClosingDelegate();
          if (cc == ContributionCommand.Stop) {
            break;
          }
        }
        catch (VetoException e) {
          continueClosing = false;
        }
        catch (RuntimeException e) {
          BEANS.get(ExceptionHandler.class).handle(e);
        }
      }
    }
    return continueClosing;
  }

  @Override
  public List<IForm> getUnsavedForms() {
    List<IForm> saveNeededForms = new ArrayList<IForm>();
    List<IForm> showedForms = m_formStore.values();
    // last element on the stack is the first that needs to be saved: iterate from end to start
    for (int i = showedForms.size() - 1; i >= 0; i--) {
      IForm f = showedForms.get(i);
      if (f.isAskIfNeedSave() && f.isSaveNeeded()) {
        saveNeededForms.add(f);
      }
    }
    return saveNeededForms;
  }

  @Override
  public String getTheme() {
    return propertySupport.getPropertyString(PROP_THEME);
  }

  @Override
  public void setTheme(String theme) {
    propertySupport.setProperty(PROP_THEME, theme);
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalDesktopExtension<DESKTOP extends AbstractDesktop> extends AbstractExtension<DESKTOP> implements org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<DESKTOP> {

    public LocalDesktopExtension(DESKTOP desktop) {
      super(desktop);
    }

    @Override
    public void execInit(DesktopInitChain chain) {
      getOwner().execInit();
    }

    @Override
    public void execOpened(DesktopOpenedChain chain) {
      getOwner().execOpened();
    }

    @Override
    public void execBeforeClosing(DesktopBeforeClosingChain chain) {
      getOwner().execBeforeClosing();
    }

    @Override
    public void execPageDetailFormChanged(DesktopPageDetailFormChangedChain chain, IForm oldForm, IForm newForm) {
      getOwner().execPageDetailFormChanged(oldForm, newForm);
    }

    @Override
    public void execTablePageLoaded(DesktopTablePageLoadedChain chain, IPageWithTable<?> tablePage) {
      getOwner().execTablePageLoaded(tablePage);
    }

    @Override
    public void execOutlineChanged(DesktopOutlineChangedChain chain, IOutline oldOutline, IOutline newOutline) {
      getOwner().execOutlineChanged(oldOutline, newOutline);
    }

    @Override
    public IForm execFormAboutToShow(DesktopFormAboutToShowChain chain, IForm form) {
      return getOwner().execFormAboutToShow(form);
    }

    @Override
    public void execClosing(DesktopClosingChain chain) {
      getOwner().execClosing();
    }

    @Override
    public void execPageSearchFormChanged(DesktopPageSearchFormChangedChain chain, IForm oldForm, IForm newForm) {
      getOwner().execPageSearchFormChanged(oldForm, newForm);
    }

    @Override
    public void execPageDetailTableChanged(DesktopPageDetailTableChangedChain chain, ITable oldTable, ITable newTable) {
      getOwner().execPageDetailTableChanged(oldTable, newTable);
    }

    @Override
    public void execGuiAttached(DesktopGuiAttachedChain chain) {
      getOwner().execGuiAttached();
    }

    @Override
    public void execGuiDetached(DesktopGuiDetachedChain chain) {
      getOwner().execGuiDetached();
    }

    @Override
    public void execDefaultView(DesktopDefaultViewChain chain) {
      getOwner().execDefaultView();
    }

  }

  protected final void interceptOpened() {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopOpenedChain chain = new DesktopOpenedChain(extensions);
    chain.execOpened();
  }

  protected final void interceptBeforeClosing() {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopBeforeClosingChain chain = new DesktopBeforeClosingChain(extensions);
    chain.execBeforeClosing();
  }

  protected final void interceptPageDetailFormChanged(IForm oldForm, IForm newForm) {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopPageDetailFormChangedChain chain = new DesktopPageDetailFormChangedChain(extensions);
    chain.execPageDetailFormChanged(oldForm, newForm);
  }

  protected final void interceptTablePageLoaded(IPageWithTable<?> tablePage) {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopTablePageLoadedChain chain = new DesktopTablePageLoadedChain(extensions);
    chain.execTablePageLoaded(tablePage);
  }

  protected final void interceptOutlineChanged(IOutline oldOutline, IOutline newOutline) {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopOutlineChangedChain chain = new DesktopOutlineChangedChain(extensions);
    chain.execOutlineChanged(oldOutline, newOutline);
  }

  protected final IForm interceptFormAboutToShow(IForm form) {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopFormAboutToShowChain chain = new DesktopFormAboutToShowChain(extensions);
    return chain.execFormAboutToShow(form);
  }

  protected final void interceptClosing() {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopClosingChain chain = new DesktopClosingChain(extensions);
    chain.execClosing();
  }

  protected final void interceptPageSearchFormChanged(IForm oldForm, IForm newForm) {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopPageSearchFormChangedChain chain = new DesktopPageSearchFormChangedChain(extensions);
    chain.execPageSearchFormChanged(oldForm, newForm);
  }

  protected final void interceptPageDetailTableChanged(ITable oldTable, ITable newTable) {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopPageDetailTableChangedChain chain = new DesktopPageDetailTableChangedChain(extensions);
    chain.execPageDetailTableChanged(oldTable, newTable);
  }

  protected final void interceptGuiAttached() {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopGuiAttachedChain chain = new DesktopGuiAttachedChain(extensions);
    chain.execGuiAttached();
  }

  protected final void interceptGuiDetached() {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopGuiDetachedChain chain = new DesktopGuiDetachedChain(extensions);
    chain.execGuiDetached();
  }

  protected final void interceptDefaultView() {
    List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions = getAllExtensions();
    DesktopDefaultViewChain chain = new DesktopDefaultViewChain(extensions);
    chain.execDefaultView();
  }

  protected class P_LocalPropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName() == PROP_DISPLAY_STYLE) {
        initDisplayStyle((String) evt.getNewValue());
      }
    }

  }
}
