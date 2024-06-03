/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AbstractLayout, Action, arrays, BenchColumnLayoutData, BookmarkSupport, BusyIndicatorOptions, BusySupport, cookies, DeferredGlassPaneTarget, DesktopBench, DesktopBenchViewActivateEvent, DesktopEventMap, DesktopFormController,
  DesktopHeader, DesktopLayout, DesktopModel, DesktopNavigation, DesktopNotification, Device, DisableBrowserF5ReloadKeyStroke, DisableBrowserTabSwitchingKeyStroke, DisplayParent, DisplayViewId, EnumObject, Event, EventEmitter, EventHandler,
  FileChooser, FileChooserController, Form, GlassPaneTarget, HtmlComponent, HtmlEnvironment, InitModelOf, KeyStrokeContext, Menu, MessageBox, MessageBoxController, NativeNotificationVisibility, ObjectOrChildModel, ObjectOrModel, objects,
  OfflineDesktopNotification, OpenUriHandler, Outline, OutlineContent, OutlineViewButton, Popup, ReloadPageOptions, ResponsiveHandler, scout, SimpleTabArea, SimpleTabBox, Splitter, SplitterMoveEndEvent, SplitterMoveEvent,
  SplitterPositionChangeEvent, strings, styles, Tooltip, Tree, TreeDisplayStyle, UnsavedFormChangesForm, URL, ViewButton, webstorage, Widget, widgets
} from '../index';
import $ from 'jquery';

export class Desktop extends Widget implements DesktopModel, DisplayParent {
  declare model: DesktopModel;
  declare eventMap: DesktopEventMap;
  declare self: Desktop;

  displayStyle: DesktopDisplayStyle;
  title: string;
  selectViewTabsKeyStrokesEnabled: boolean;
  selectViewTabsKeyStrokeModifier: string;
  cacheSplitterPosition: boolean;
  browserHistoryEntry: BrowserHistoryEntry;
  logoId: string;
  logoUrl: string;
  navigationVisible: boolean;
  navigationHandleVisible: boolean;
  logoActionEnabled: boolean;
  benchVisible: boolean;
  headerVisible: boolean;
  geolocationServiceAvailable: boolean;
  benchLayoutData: BenchColumnLayoutData;
  nativeNotificationDefaults: NativeNotificationDefaults;
  menus: Menu[];
  addOns: Widget[];
  dialogs: Form[];
  views: Form[];
  keyStrokes: Action[];
  viewButtons: ViewButton[];
  messageBoxes: MessageBox[];
  fileChoosers: FileChooser[];
  outline: Outline;
  activeForm: Form;
  selectedViewTabs: Map<DisplayViewId, Form>;
  notifications: DesktopNotification[];
  navigation: DesktopNavigation;
  header: DesktopHeader;
  bench: DesktopBench;
  splitter: Splitter;
  splitterVisible: boolean;
  formController: DesktopFormController;
  messageBoxController: MessageBoxController;
  fileChooserController: FileChooserController;
  initialFormRendering: boolean;
  resizing: boolean;
  offline: boolean;
  inBackground: boolean;
  openUriHandler: OpenUriHandler;
  theme: string;
  dense: boolean;
  animateLayoutChange: boolean;
  url: URL;
  responsiveHandler: ResponsiveHandler;
  busySupport: BusySupport;
  bookmarkSupport: BookmarkSupport;

  $notifications: JQuery;
  $overlaySeparator: JQuery;

  /** @internal */
  _resizeHandler: (event: JQuery.ResizeEvent) => void;

  protected _glassPaneTargetFilters: GlassPaneTargetFilter[];
  protected _offlineNotification: OfflineDesktopNotification;
  /** event listeners */
  protected _benchActiveViewChangedHandler: EventHandler<DesktopBenchViewActivateEvent>;
  protected _selectedViewDestroyHandler: EventHandler<Event<Form>>;
  protected _popstateHandler: (event: JQuery.TriggeredEvent) => void;
  protected _repositionTooltipsHandler: () => void;

  constructor() {
    super();

    this.title = null;
    this.selectViewTabsKeyStrokesEnabled = true;
    this.selectViewTabsKeyStrokeModifier = 'control';
    this.cacheSplitterPosition = true;
    this.browserHistoryEntry = null;
    this.logoId = null;
    this.navigationVisible = true;
    this.navigationHandleVisible = true;
    this.logoActionEnabled = false;
    this.benchVisible = true;
    this.headerVisible = true;
    this.geolocationServiceAvailable = Device.get().supportsGeolocation();
    this.benchLayoutData = null;
    this.nativeNotificationDefaults = null;
    this.menus = [];
    this.addOns = [];
    this.dialogs = [];
    this.views = [];
    this.keyStrokes = [];
    this.viewButtons = [];
    this.messageBoxes = [];
    this.fileChoosers = [];
    this.outline = null;
    this.activeForm = null;
    this.selectedViewTabs = new Map();
    this.notifications = [];
    this.navigation = null;
    this.header = null;
    this.bench = null;
    this.splitter = null;
    this.splitterVisible = false;
    this.formController = null;
    this.messageBoxController = null;
    this.fileChooserController = null;
    this.initialFormRendering = false;
    this.offline = false;
    this.inBackground = false;
    this.openUriHandler = null;
    this.theme = null;
    this.dense = false;
    this.url = null;
    this.busySupport = scout.create(BusySupport, {parent: this});
    this.bookmarkSupport = scout.create(BookmarkSupport, {desktop: this});

    this.$notifications = null;
    this.$overlaySeparator = null;

    this._addWidgetProperties(['viewButtons', 'menus', 'views', 'selectedViewTabs', 'dialogs', 'outline', 'messageBoxes', 'notifications', 'fileChoosers', 'addOns', 'keyStrokes', 'activeForm', 'focusedElement']);
    this._addPreserveOnPropertyChangeProperties(['focusedElement', 'selectedViewTabs']);

    this._glassPaneTargetFilters = [];
    this._benchActiveViewChangedHandler = this._onBenchActivateViewChanged.bind(this);
    this._selectedViewDestroyHandler = this._onSelectedViewDestroy.bind(this);
    this._repositionTooltipsHandler = null;
  }

  static DisplayStyle = {
    /**
     * Default style with header, navigation (outline) and bench (forms).
     */
    DEFAULT: 'default',
    /**
     * In this style, only the bench is visible, header and navigation are invisible.
     *
     * Currently, you'll also have to manually set {@link navigationVisible} and {@link headerVisible} to false.
     */
    BENCH: 'bench',
    /**
     * Compact style that can be used for mobile devices where navigation and bench are never visible simultaneously.
     */
    COMPACT: 'compact'
  } as const;

  /**
   * The action that should be performed when handling an "open URI" event.
   */
  static UriAction = {
    /**
     * The object represented by the URI should be downloaded rather than be handled by the browser's rendering engine.
     * It should make the "Save as..." dialog appear which allows the user to store the resource to his local file system.<br>
     * The application's location does not change, and no browser windows or tabs are opened.<br>
     * <br>
     *<b>Important:</b> This action only works if the HTTP header <i>Content-Disposition: attachment</i> is present on the response of the object to be downloaded.<br>
     */
    DOWNLOAD: 'download',
    /**
     * The object represented by the URI should be opened by the browser rather than just be downloaded.
     * This will only work if the browser knows how to handle the given URI.
     * E.g. if it points to a pdf file, most browsers will be able to display it using their pdf viewer. Other files may just be downloaded.
     * If the URI points to a website, it will be opened in a separate window or tab.
     * <br>
     * This is also the preferred action to open URIs with <b>special protocols</b> that are registered in the user's system and delegated to some "protocol
     * handler". This handler may then perform actions in a third party application (e.g. <i>mailto:xyz@example.com</i>
     * would open the system's mail application).<br>
     * Note that this action may open the object in a new window or tab which may be prevented by
     * the browser's popup blocker mechanism.
     */
    OPEN: 'open',
    /**
     * The content represented by the URI should be rendered by the browser and displayed in a new window or tab.<br>
     * The application's location does not change. Note that this action may be prevented by the browser's popup blocker mechanism.
     */
    NEW_WINDOW: 'newWindow',
    /**
     * The content represented by the URI should be rendered by the browser and displayed in a new non-modal popup
     * window.
     * Unlike {@link UriAction.NEW_WINDOW} the newly opened window is limited, i.e. it does not contain the location, the toolbar and the menubar.
     * This may not work on every browser (e.g. on a mobile browser the action {@link UriAction.POPUP_WINDOW} will likely behave the same way as {@link UriAction.NEW_WINDOW}).<br>
     * The application's location does not change. Note that this action may be prevented by the browser's popup blocker
     * mechanism.
     */
    POPUP_WINDOW: 'popupWindow',
    /**
     * The content represented by the URI should be opened in the same window.
     * This will mainly just replace the location of the current window.
     * If the URI points to another website, the current application will be unloaded and replaced.
     * If it points to a file or uses a special protocol, the handling depends on the used browser.
     */
    SAME_WINDOW: 'sameWindow'
  } as const;

  static DEFAULT_THEME = 'default';

  protected override _init(model: InitModelOf<this>) {
    // Note: session and desktop are tightly coupled. Because a lot of widgets want to register
    // a listener on the desktop in their init phase, they access the desktop by calling 'this.session.desktop'
    // that's why we need this instance as early as possible. When that happens they access a desktop which is
    // not yet fully initialized. But anyway, it's already possible to attach a listener, for instance.
    // Because of this line of code here, we don't have to set the variable in App.ts, after the desktop has been
    // created. Also note that Scout Java uses a different pattern to solve the same problem, there a VirtualDesktop
    // is used during initialization. When initialization is done, all registered listeners on the virtual desktop
    // are copied to the real desktop instance.
    let session = model.session || model.parent.session;
    session.desktop = this;

    // Needs to be initialized before notifications are created because notifications read this value during init
    model.nativeNotificationDefaults = this._createNativeNotificationDefaults(model);

    super._init(model);
    this.url = new URL();
    this._initTheme();
    this.formController = scout.create(DesktopFormController, {
      displayParent: this,
      session: this.session
    });
    this.messageBoxController = new MessageBoxController(this, this.session);
    this.fileChooserController = new FileChooserController(this, this.session);
    this._resizeHandler = this.onResize.bind(this);
    this._popstateHandler = this.onPopstate.bind(this);
    this.updateSplitterVisibility();
    this.resolveTextKeys(['title']);
    this._setViews(this.views);
    this._setViewButtons(this.viewButtons);
    this._setSelectedViewTabs(this.selectedViewTabs);
    this._setMenus(this.menus);
    this._setKeyStrokes(this.keyStrokes);
    this._setBenchLayoutData(this.benchLayoutData);
    this._setDisplayStyle(this.displayStyle);
    this._setDense(this.dense);
    this.openUriHandler = scout.create(OpenUriHandler, {
      session: this.session
    });
    this._glassPaneTargetFilters.push((targetElem, element) => {
      // Exclude all child elements of the given widget
      // Use case: element is a popup and has tooltip open. The tooltip is displayed in the desktop and considered as glass pane target by the selector above
      let target = scout.widget(targetElem);
      return !element.has(target);
    });
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.keyStrokeContext.registerKeyStrokes([
      new DisableBrowserF5ReloadKeyStroke(this),
      new DisableBrowserTabSwitchingKeyStroke(this)
    ]);
  }

  protected _createNativeNotificationDefaults(model: DesktopModel): NativeNotificationDefaults {
    return $.extend({
      title: model.title,
      iconId: model.logoId
    }, model.nativeNotificationDefaults);
  }

  /** @see DesktopModel.nativeNotificationDefaults */
  setNativeNotificationDefaults(defaults: NativeNotificationDefaults) {
    this.setProperty('nativeNotificationDefaults', defaults);
  }

  protected _onBenchActivateViewChanged(event: DesktopBenchViewActivateEvent) {
    if (this.initialFormRendering) {
      return;
    }
    let view = event.view;
    if (view instanceof Form && this.bench.outlineContent !== view && !view.detailForm) {
      // Notify model that this form is active (only for regular views, not detail forms)
      this._setFormActivated(view);
    }
  }

  protected override _render() {
    this.$container = this.$parent;
    this.$container.addClass('desktop');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());

    // Attach resize listener before other elements can add their own resize listener (e.g. an addon) to make sure it is executed first
    this.$container.window()
      .on('resize', this._resizeHandler)
      .on('popstate', this._popstateHandler);

    // Desktop elements are added before this separator, all overlays are opened after (dialogs, popups, tooltips etc.)
    this.$overlaySeparator = this.$container.appendDiv('overlay-separator').setVisible(false);

    this._renderDense();
    this._renderNavigationVisible();
    this._renderHeaderVisible();
    this._renderBenchVisible();
    this._renderTitle();
    this._renderLogoUrl();
    this._renderSplitterVisible();
    this._renderInBackground();
    this._renderNavigationHandleVisible();
    this._renderNotifications();
    this._renderBrowserHistoryEntry();
    this.addOns.forEach(addOn => addOn.render());

    // prevent general drag and drop, dropping a file anywhere in the application must not open this file in browser
    this._setupDragAndDrop();

    this._disableContextMenu();
  }

  protected override _remove() {
    this.formController.remove();
    this.messageBoxController.remove();
    this.fileChooserController.remove();
    this.$container.window()
      .off('resize', this._resizeHandler)
      .off('popstate', this._popstateHandler);
    super._remove();
  }

  protected override _postRender() {
    super._postRender();

    // Render attached forms, message boxes and file choosers.
    this.initialFormRendering = true;
    this._renderDisplayChildrenOfOutline();
    this.formController.render();
    this.messageBoxController.render();
    this.fileChooserController.render();
    this.initialFormRendering = false;
  }

  protected _setDisplayStyle(displayStyle: DesktopDisplayStyle) {
    this._setProperty('displayStyle', displayStyle);

    let compact = this.displayStyle === Desktop.DisplayStyle.COMPACT;
    if (this.header) {
      if (this.header.tabArea) {
        const DisplayStyle = SimpleTabArea.DisplayStyle;
        this.header.tabArea.setDisplayStyle(compact ? DisplayStyle.SPREAD_EVEN : DisplayStyle.DEFAULT);
      }
      this.header.setToolBoxVisible(!compact);
      this.header.animateRemoval = compact;
    }
    if (this.navigation) {
      this.navigation.setToolBoxVisible(compact);
      this.navigation.htmlComp.layoutData.fullWidth = compact;
    }
    if (this.bench) {
      this.bench.setOutlineContentVisible(!compact);
    }
    if (this.outline) {
      this.outline.setCompact(compact);
      this.outline.setEmbedDetailContent(compact);
    }
  }

  /** @see BusySupport.setBusy */
  setBusy(busy: boolean | BusyIndicatorOptions) {
    this.busySupport.setBusy(busy);
  }

  /**
   * @returns true if the desktop has a busy indicator active.
   */
  get busy(): boolean {
    return this.busySupport.isBusy();
  }

  /** @see DesktopModel.dense */
  setDense(dense: boolean) {
    this.setProperty('dense', dense);
  }

  protected _setDense(dense: boolean) {
    this._setProperty('dense', dense);

    styles.clearCache();
    HtmlEnvironment.get().init(this.dense ? 'dense' : null);
  }

  protected _renderDense() {
    this.$container.toggleClass('dense', this.dense);
  }

  protected _createLayout(): AbstractLayout {
    return new DesktopLayout(this);
  }

  /**
   * Displays attached forms, message boxes and file choosers.
   * Outline does not need to be rendered to show the child elements, it needs to be active (necessary if navigation is invisible)
   */
  protected _renderDisplayChildrenOfOutline() {
    if (!this.outline) {
      return;
    }
    this.outline.formController.render();
    this.outline.messageBoxController.render();
    this.outline.fileChooserController.render();

    // Restore the selected views after an outline change or select them initially based on the model state
    if (this.outline.selectedViewTabs.size > 0) {
      this.outline.selectedViewTabs.forEach(selectedView => {
        this.formController._activateView(selectedView);
      });
    } else {
      // views on the outline are not activated by default. Check for modal views on this outline
      let modalViews = this.outline.views.filter(view => view.modal);
      // activate each modal view in the order it was originally activated
      modalViews.forEach(this.formController._activateView.bind(this.formController));
    }
  }

  protected _removeDisplayChildrenOfOutline() {
    if (!this.outline) {
      return;
    }
    this.outline.formController.remove();
    this.outline.messageBoxController.remove();
    this.outline.fileChooserController.remove();
  }

  computeParentForDisplayParent(displayParent: DisplayParent): Widget {
    // Outline must not be used as parent, otherwise the children (form, messageboxes etc.) would be removed if navigation is made invisible
    // The functions _render/removeDisplayChildrenOfOutline take care that the elements are correctly rendered/removed on an outline switch
    let parent = displayParent;
    if (displayParent instanceof Outline) {
      parent = this;
    }
    return parent;
  }

  protected _renderTitle() {
    let title = this.title;
    if (title === undefined || title === null) {
      return;
    }
    let $scoutDivs = $('div.scout');
    if ($scoutDivs.length <= 1) { // only set document title in non-portlet case
      $scoutDivs.document(true).title = title;
    }
  }

  protected _renderActiveForm() {
    // NOP -> is handled in _setFormActivated when ui changes active form or if model changes form in _onFormShow/_onFormActivate
  }

  protected _renderBench() {
    if (this.bench) {
      return;
    }
    this.bench = this._createBench();
    this.bench.on('viewActivate', this._benchActiveViewChangedHandler);
    this.bench.render();
    this.bench.$container.insertBefore(this.$overlaySeparator);
    this.invalidateLayoutTree();
  }

  protected _createBench(): DesktopBench {
    return scout.create(DesktopBench, {
      parent: this,
      animateRemoval: true,
      headerTabArea: this.header ? this.header.tabArea : undefined,
      outlineContentVisible: this.displayStyle !== Desktop.DisplayStyle.COMPACT
    });
  }

  protected _removeBench() {
    if (!this.bench) {
      return;
    }
    this.bench.off('viewActivate', this._benchActiveViewChangedHandler);
    this.bench.on('destroy', () => {
      this.bench = null;
      this.invalidateLayoutTree();
    });
    this.bench.destroy();
  }

  protected _renderBenchVisible() {
    this.animateLayoutChange = this.rendered;
    if (this.benchVisible) {
      this._renderBench();
      this._renderInBackground();
    } else {
      this._removeBench();
    }
  }

  protected _renderNavigation() {
    if (this.navigation) {
      return;
    }
    this.navigation = this._createNavigation();
    this.navigation.render();
    this.navigation.$container.prependTo(this.$container);
    this.$container.removeClass('navigation-invisible');
    this.invalidateLayoutTree();
  }

  protected _createNavigation(): DesktopNavigation {
    return scout.create(DesktopNavigation, {
      parent: this,
      outline: this.outline,
      toolBoxVisible: this.displayStyle === Desktop.DisplayStyle.COMPACT,
      layoutData: {
        fullWidth: this.displayStyle === Desktop.DisplayStyle.COMPACT
      }
    });
  }

  protected _removeNavigation() {
    this.$container.addClass('navigation-invisible');
    if (!this.navigation) {
      return;
    }
    this.navigation.destroy();
    this.navigation = null;
    this.invalidateLayoutTree();
  }

  protected _renderNavigationVisible() {
    this.animateLayoutChange = this.rendered;
    if (this.navigationVisible) {
      this._renderNavigation();
    } else {
      if (!this.animateLayoutChange) {
        this._removeNavigation();
      } else {
        // re layout to trigger animation
        this.invalidateLayoutTree();
      }
    }
  }

  protected _renderHeader() {
    if (this.header) {
      return;
    }
    this.header = this._createHeader();
    this.header.render();
    if (this.navigation && this.navigation.rendered) {
      this.header.$container.insertAfter(this.navigation.$container);
    } else {
      this.header.$container.prependTo(this.$container);
    }
    // register header tab area
    if (this.bench) {
      this.bench._setTabArea(this.header.tabArea);
    }
    this.invalidateLayoutTree();
  }

  protected _createHeader(): DesktopHeader {
    let compact = this.displayStyle === Desktop.DisplayStyle.COMPACT;
    return scout.create(DesktopHeader, {
      parent: this,
      logoUrl: this.logoUrl,
      animateRemoval: compact,
      toolBoxVisible: !compact,
      tabArea: {
        displayStyle: compact ? SimpleTabArea.DisplayStyle.SPREAD_EVEN : SimpleTabArea.DisplayStyle.DEFAULT
      }
    });
  }

  protected _removeHeader() {
    if (!this.header) {
      return;
    }
    this.header.on('destroy', () => {
      this.invalidateLayoutTree();
      this.header = null;
    });
    this.header.destroy();
  }

  protected _renderHeaderVisible() {
    if (this.headerVisible) {
      this._renderHeader();
    } else {
      this._removeHeader();
    }
  }

  protected _renderLogoUrl() {
    if (this.header) {
      this.header.setLogoUrl(this.logoUrl);
    }
  }

  protected _renderSplitterVisible() {
    if (this.splitterVisible) {
      this._renderSplitter();
    } else {
      this._removeSplitter();
    }
  }

  protected _renderSplitter() {
    if (this.splitter || !this.navigation) {
      return;
    }
    this.splitter = scout.create(Splitter, {
      parent: this,
      $anchor: this.navigation.$container,
      $root: this.$container
    });
    this.splitter.render();
    this.splitter.$container.insertBefore(this.$overlaySeparator);
    this.splitter.on('move', this._onSplitterMove.bind(this));
    this.splitter.on('moveEnd', this._onSplitterMoveEnd.bind(this));
    this.splitter.on('positionChange', this._onSplitterPositionChange.bind(this));
    this.updateSplitterPosition();
  }

  protected _removeSplitter() {
    if (!this.splitter) {
      return;
    }
    this.splitter.destroy();
    this.splitter = null;
  }

  protected _renderInBackground() {
    this.$container.toggleClass('in-background', this.inBackground && this.displayStyle !== Desktop.DisplayStyle.COMPACT);
    if (this.bench) {
      this.bench.$container.toggleClass('drop-shadow', this.inBackground);
    }
  }

  protected _renderBrowserHistoryEntry() {
    if (!Device.get().supportsHistoryApi()) {
      return;
    }
    let myWindow = this.$container.window(true),
      history = this.browserHistoryEntry;
    if (history) {
      let historyPath = this._createHistoryPath(history);
      let setStateFunc = (this.rendered ? myWindow.history.pushState : myWindow.history.replaceState).bind(myWindow.history);
      let historyState: DesktopHistoryState = {deepLinkPath: history.deepLinkPath};
      setStateFunc(historyState, history.title, historyPath);
    }
  }

  /**
   * Takes the {@link BrowserHistoryEntry.path} and appends additional URL parameters.
   */
  protected _createHistoryPath(history: BrowserHistoryEntry): string {
    if (!history.pathVisible) {
      return '';
    }
    let historyPath = history.path;
    let cloneUrl = this.url.clone();
    cloneUrl.removeParameter('dl');
    cloneUrl.removeParameter('i');
    if (objects.countOwnProperties(cloneUrl.parameterMap) > 0) {
      let pathUrl = new URL(historyPath);
      for (let paramName in cloneUrl.parameterMap) {
        pathUrl.addParameter(paramName, cloneUrl.getParameter(paramName) as string);
      }
      historyPath = pathUrl.toString({alwaysFirst: ['dl', 'i']});
    }
    return historyPath;
  }

  protected _setupDragAndDrop() {
    let dragEnterOrOver = (event: JQuery.DragEnterEvent | JQuery.DragOverEvent) => {
      event.stopPropagation();
      event.preventDefault();
      // change cursor to forbidden (no dropping allowed)
      event.originalEvent.dataTransfer.dropEffect = 'none';
    };

    this.$container.on('dragenter', dragEnterOrOver);
    this.$container.on('dragover', dragEnterOrOver);
    this.$container.on('drop', (event: JQuery.DropEvent) => {
      event.stopPropagation();
      event.preventDefault();
    });
  }

  updateSplitterVisibility() {
    // Splitter should only be visible if navigation and bench are visible, but never in compact mode (to prevent unnecessary splitter rendering)
    this.setSplitterVisible(this.navigationVisible && this.benchVisible && this.displayStyle !== Desktop.DisplayStyle.COMPACT);
  }

  setSplitterVisible(visible: boolean) {
    this.setProperty('splitterVisible', visible);
  }

  updateSplitterPosition() {
    if (!this.splitter) {
      return;
    }
    let storedSplitterPosition = this.cacheSplitterPosition && this._loadCachedSplitterPosition();
    if (storedSplitterPosition) {
      // Restore splitter position
      let splitterPosition = parseInt(storedSplitterPosition, 10);
      this.splitter.setPosition(splitterPosition);
      this.invalidateLayoutTree();
    } else {
      // Set initial splitter position (default defined by css)
      this.splitter.setPosition();
      this.invalidateLayoutTree();
    }
  }

  protected _disableContextMenu() {
    // Switch off browser's default context menu for the entire scout desktop (except input fields)
    this.$container.on('contextmenu', event => {
      if (event.target.nodeName !== 'INPUT' && event.target.nodeName !== 'TEXTAREA' && !event.target.isContentEditable) {
        event.preventDefault();
      }
    });
  }

  /** @see DesktopModel.outline */
  setOutline(outline: Outline) {
    if (this.outline === outline) {
      return;
    }
    try {
      if (this.bench) {
        this.bench.setChanging(true);
      }
      if (this.rendered) {
        this._removeDisplayChildrenOfOutline();
      }

      this.outline = outline;
      this._setDisplayStyle(this.displayStyle);
      this._setOutlineActivated();
      if (this.navigation) {
        this.navigation.setOutline(this.outline);
      }
      // call render after triggering event so glasspane rendering taking place can refer to the current outline content
      this.trigger('outlineChange');

      if (this.rendered) {
        this._renderDisplayChildrenOfOutline();
      }
    } finally {
      if (this.bench) {
        this.bench.setChanging(false);
      }
    }
  }

  protected _setViews(views: Form[]) {
    if (views) {
      views.forEach(view => view.setDisplayParent(this));
    }
    this._setProperty('views', views);
  }

  protected _setViewButtons(viewButtons: ViewButton[]) {
    this.updateKeyStrokes(viewButtons, this.viewButtons);
    this._setProperty('viewButtons', viewButtons);
  }

  /** @see DesktopModel.menus */
  setMenus(menus: ObjectOrChildModel<Menu>[]) {
    if (this.header) {
      this.header.setMenus(menus);
    }
  }

  protected _setMenus(menus: Menu[]) {
    this.updateKeyStrokes(menus, this.menus);
    this._setProperty('menus', menus);
  }

  protected _setKeyStrokes(keyStrokes: Action[]) {
    this.updateKeyStrokes(keyStrokes, this.keyStrokes);
    this._setProperty('keyStrokes', keyStrokes);
  }

  /** @see DesktopModel.navigationHandleVisible */
  setNavigationHandleVisible(visible: boolean) {
    this.setProperty('navigationHandleVisible', visible);
  }

  protected _renderNavigationHandleVisible() {
    this.$container.toggleClass('has-navigation-handle', this.navigationHandleVisible);
  }

  /** @see DesktopModel.navigationVisible */
  setNavigationVisible(visible: boolean) {
    this.setProperty('navigationVisible', visible);
    this.updateSplitterVisibility();
  }

  /** @see DesktopModel.benchVisible */
  setBenchVisible(visible: boolean) {
    this.setProperty('benchVisible', visible);
    this.updateSplitterVisibility();
  }

  /** @see DesktopModel.headerVisible */
  setHeaderVisible(visible: boolean) {
    this.setProperty('headerVisible', visible);
  }

  protected _setBenchLayoutData(layoutData: ObjectOrModel<BenchColumnLayoutData>) {
    layoutData = BenchColumnLayoutData.ensure(layoutData);
    this._setProperty('benchLayoutData', layoutData);
  }

  protected _setInBackground(inBackground: boolean) {
    this._setProperty('inBackground', inBackground);
  }

  outlineDisplayStyle(): TreeDisplayStyle {
    if (this.outline) {
      return this.outline.displayStyle;
    }
  }

  shrinkNavigation() {
    if (this.outline && this.outline.toggleBreadcrumbStyleEnabled && this.navigationVisible && this.outlineDisplayStyle() === Tree.DisplayStyle.DEFAULT) {
      this.outline.setDisplayStyle(Tree.DisplayStyle.BREADCRUMB);
    } else {
      this.setNavigationVisible(false);
    }
  }

  enlargeNavigation() {
    if (this.outline && this.navigationVisible && this.outlineDisplayStyle() === Tree.DisplayStyle.BREADCRUMB) {
      this.outline.setDisplayStyle(Tree.DisplayStyle.DEFAULT);
      if (this.cacheSplitterPosition && this.splitter) {
        this.validateLayoutTree();
        this._storeCachedSplitterPosition(this.splitter.position);
      }
    } else {
      this.setNavigationVisible(true);
    }
  }

  /**
   * @param headerVisible whether the desktop header should be visible. Default is true.
   */
  switchToBench(headerVisible?: boolean) {
    this.setHeaderVisible(scout.nvl(headerVisible, true));
    this.setBenchVisible(true);
    this.setNavigationVisible(false);
  }

  switchToNavigation() {
    this.setNavigationVisible(true);
    this.setHeaderVisible(false);
    this.setBenchVisible(false);
  }

  revalidateHeaderLayout() {
    if (this.header) {
      this.header.revalidateLayout();
    }
  }

  goOffline() {
    if (this.offline) {
      return;
    }
    this.offline = true;
    this._removeOfflineNotification();
    this._offlineNotification = scout.create(OfflineDesktopNotification, {
      parent: this
    });
    this._offlineNotification.show();
  }

  goOnline() {
    this._removeOfflineNotification();
  }

  protected _removeOfflineNotification() {
    if (this._offlineNotification) {
      setTimeout(this.removeNotification.bind(this, this._offlineNotification), 3000);
      this._offlineNotification = null;
    }
  }

  addNotification(notification: DesktopNotification) {
    if (!notification) {
      return;
    }
    this.notifications.push(notification);
    if (this.rendered) {
      this._renderNotification(notification);
    }
  }

  protected _renderNotification(notification: DesktopNotification) {
    if (this.$notifications) {
      // Bring to front
      this.$notifications.appendTo(this.$container);
    } else {
      this.$notifications = this.$container.appendDiv('desktop-notifications');
    }
    notification.fadeIn(this.$notifications);
    if (notification.duration > 0) {
      notification.removeTimeout = setTimeout(notification.hide.bind(notification), notification.duration);
      notification.one('remove', () => this.removeNotification(notification));
    }
  }

  protected _renderNotifications() {
    this.notifications.forEach(notification => this._renderNotification(notification));
  }

  /**
   * Removes the given notification.
   * @param notification Either an instance of DesktopNavigation or a String containing an ID of a notification instance.
   */
  removeNotification(desktopNotification: DesktopNotification | string) {
    let notification: DesktopNotification;
    if (typeof desktopNotification === 'string') {
      notification = arrays.find(this.notifications, n => desktopNotification === n.id);
    } else {
      notification = desktopNotification;
    }
    if (!notification) {
      return;
    }
    if (notification.removeTimeout) {
      clearTimeout(notification.removeTimeout);
    }
    arrays.remove(this.notifications, notification);
    if (!this.rendered) {
      return;
    }
    if (notification.rendered) {
      notification.one('remove', this._onNotificationRemove.bind(this));
    }
    notification.fadeOut();
  }

  getPopups(): Popup[] {
    if (!this.$container) {
      return [];
    }
    let popups: Popup[] = [];
    this.$container.children('.popup').each((i, elem) => {
      let $popup = $(elem);
      let popup = widgets.get($popup);
      if (popup instanceof Popup) {
        popups.push(popup);
      }
    });
    return popups;
  }

  getPopupsFor(widget: Widget): Popup[] {
    return this.getPopups().filter(popup => widget.has(popup));
  }

  /**
   * Removes every popup which is a descendant of the given widget.
   */
  removePopupsFor(widget: Widget) {
    this.getPopupsFor(widget).forEach(popup => popup.remove());
  }

  /**
   * Opens the uri using {@link OpenUriHandler}
   * @param uri the uri to open
   * @param action the action to be performed on the given uri. Default is Desktop.UriAction.OPEN.
   */
  openUri(uri: string, action?: DesktopUriAction) {
    if (!this.rendered) {
      this._postRenderActions.push(this.openUri.bind(this, uri, action));
      return;
    }
    this.openUriHandler.openUri(uri, action);
  }

  bringOutlineToFront() {
    if (!this.rendered) {
      this._postRenderActions.push(this.bringOutlineToFront.bind(this));
      return;
    }

    if (!this.inBackground || this.displayStyle === Desktop.DisplayStyle.BENCH) {
      return;
    }

    this._setInBackground(false);
    this._setOutlineActivated();

    if (this.navigationVisible) {
      this.navigation.bringToFront();
    }
    if (this.benchVisible) {
      this.bench.bringToFront();
    }
    if (this.headerVisible) {
      this.header.bringToFront();
    }

    this._renderInBackground();
  }

  sendOutlineToBack() {
    if (this.inBackground) {
      return;
    }
    this._setInBackground(true);
    if (this.navigationVisible) {
      this.navigation.sendToBack();
    }
    if (this.benchVisible) {
      this.bench.sendToBack();
    }
    if (this.headerVisible) {
      this.header.sendToBack();
    }
    this._renderInBackground();
  }

  /**
   * === Method required for objects that act as 'displayParent' ===
   *
   * Returns 'true' if the Desktop is currently accessible to the user.
   */
  inFront(): boolean {
    return true; // Desktop is always available to the user.
  }

  /**
   * === Method required for objects that act as 'displayParent' ===
   *
   * @returns the DOM elements to paint a glassPanes over, once a modal Form, message-box, file-chooser or wait-dialog is showed with the Desktop as its 'displayParent'.
   */
  protected override _glassPaneTargets(element: Widget): GlassPaneTarget[] {
    // Do not return $container, because this is the parent of all forms and message boxes. Otherwise, no form could gain focus, even the form requested desktop modality.
    let $glassPaneTargets = this.$container
      .children()
      .not('.splitter') // exclude splitter to be locked
      .not('.desktop-notifications') // exclude notification box like 'connection interrupted' to be locked
      .not('.overlay-separator'); // exclude overlay separator (marker element)

    if (element) {
      if (element.$container) {
        $glassPaneTargets = $glassPaneTargets.not(element.$container);
      }
      let overlays = this.$overlaySeparator.nextAll().toArray();
      let nextSiblings = [];
      // If the element is an overlay, get all next siblings and exclude them because they must not be covered
      if (element.$container && overlays.indexOf(element.$container[0]) > -1) {
        nextSiblings = element.$container.nextAll().toArray();
      }

      // The top-most element should not have a glass-pane (#274353)
      let topMostElement = null;
      if (overlays.length) {
        for (let i = overlays.length - 1; i >= 0; i--) {
          // Don't consider filtered glass-pane targets like the HelpPopup.js
          // These targets stand outside the regular modality hierarchy.
          let overlay = overlays[i];
          if (!this._isGlassPaneTargetFiltered(overlay, element)) {
            continue;
          }
          topMostElement = overlay;
          break; // stop looking further
        }
      }

      $glassPaneTargets = $glassPaneTargets.filter((i, targetElem) => {
        if (nextSiblings.indexOf(targetElem) > -1) {
          return false;
        }
        if (targetElem === topMostElement) {
          return false;
        }
        return this._isGlassPaneTargetFiltered(targetElem, element);
      });
    }

    let glassPaneTargets: GlassPaneTarget[];
    if (element instanceof Form && element.displayHint === Form.DisplayHint.VIEW) {
      $glassPaneTargets = $glassPaneTargets
        .not('.desktop-bench')
        .not('.desktop-header');

      if (this.header && this.header.toolBox && this.header.toolBox.$container) {
        $glassPaneTargets.push(this.header.toolBox.$container);
      }

      glassPaneTargets = $.makeArray($glassPaneTargets);
      arrays.pushAll(glassPaneTargets, this._getBenchGlassPaneTargetsForView(element));
    } else {
      glassPaneTargets = $.makeArray($glassPaneTargets);
    }

    // When a popup-window is opened its container must also be added to the result
    this._pushPopupWindowGlassPaneTargets(glassPaneTargets, element);

    return glassPaneTargets;
  }

  protected _isGlassPaneTargetFiltered(targetElem: HTMLElement, element: Widget): boolean {
    return this._glassPaneTargetFilters.every(filter => filter(targetElem, element));
  }

  /**
   * Adds a filter which is applied when the glass pane targets are collected.
   * If the filter returns <code>false</code>, the target won't be accepted and not covered by a glass pane.
   * This filter should be used primarily for elements like the help-popup which stand outside the regular modality hierarchy.
   *
   * @param filter a function with the parameter target and element. Target is the element which
   *     would be covered by a glass pane, element is the element the user interacts with (e.g. the modal dialog).
   * @see _glassPaneTargets
   */
  addGlassPaneTargetFilter(filter: GlassPaneTargetFilter) {
    this._glassPaneTargetFilters.push(filter);
  }

  removeGlassPaneTargetFilter(filter: GlassPaneTargetFilter) {
    arrays.remove(this._glassPaneTargetFilters, filter);
  }

  /**
   * This 'deferred' object is used because popup windows are not immediately usable when they're opened.
   * That's why we must render the glass-pane of a popup window later. Which means, at the point in time
   * when its $container is created and ready for usage. To avoid race conditions we must also wait until
   * the glass pane renderer is ready. Only when both conditions are fulfilled, we can render the glass
   * pane.
   */
  protected _deferredGlassPaneTarget(popupWindow: EventEmitter & { $container: JQuery }): DeferredGlassPaneTarget {
    let deferred = new DeferredGlassPaneTarget();
    popupWindow.one('init', () => deferred.ready([popupWindow.$container]));
    return deferred;
  }

  protected _getBenchGlassPaneTargetsForView(view: Form): JQuery[] {
    let $glassPanes: JQuery[] = [];

    $glassPanes = $glassPanes.concat(this._getTabGlassPaneTargetsForView(view, this.header));

    if (this.bench) {
      this.bench.visibleTabBoxes().forEach(tabBox => {
        if (!tabBox.rendered) {
          return;
        }
        if (tabBox.hasView(view)) {
          $glassPanes = $glassPanes.concat(this._getTabGlassPaneTargetsForView(view, tabBox));
        } else {
          $glassPanes.push(tabBox.$container);
        }
      });
    }
    return $glassPanes;
  }

  protected _getTabGlassPaneTargetsForView(view: Form, tabBox: SimpleTabBox<OutlineContent> | DesktopHeader): JQuery[] {
    let $glassPanes: JQuery[] = [];
    if (tabBox && tabBox.tabArea) {
      tabBox.tabArea.tabs.forEach(tab => {
        if (tab.view !== view) {
          $glassPanes.push(tab.$container);
          // Workaround for javascript not being able to prevent hover event propagation:
          // In case of tabs, the hover selector is defined on the element that is the direct parent
          // of the glass pane. Under these circumstances, the hover style isn't be prevented by the glass pane.
          tab.$container.addClass('glasspane-parent');
        }
      });
    }
    return $glassPanes;
  }

  protected _pushPopupWindowGlassPaneTargets(glassPaneTargets: GlassPaneTarget[], element: Widget) {
    this.formController.popupWindows.forEach(popupWindow => {
      if (element === popupWindow.form) {
        // Don't block form itself
        return;
      }
      glassPaneTargets.push(popupWindow.initialized ? popupWindow.$container[0] : this._deferredGlassPaneTarget(popupWindow));
    });
  }

  showForm(form: Form, position?: number) {
    let displayParent: DisplayParent = form.displayParent || this;
    form.setDisplayParent(displayParent);

    this._setFormActivated(form);
    // register listener to recover active form when child dialog is removed
    displayParent.formController.registerAndRender(form, position, true);
  }

  hideForm(form: Form) {
    if (!form.displayParent) {
      // showForm has probably never been called -> nothing to do here
      // May happen if form.close() is called immediately after form.open() without waiting for the open promise to resolve
      // Hint: it is not possible to check whether the form is rendered and then return (which would be the obvious thing to do).
      // Reason: Forms in popup windows are removed before getting closed, see DesktopFormController._onPopupWindowUnload
      return;
    }

    if (this.displayStyle === Desktop.DisplayStyle.COMPACT && form.isView() && this.benchVisible) {
      let openViews = this.bench.getViews().slice();
      arrays.remove(openViews, form);
      if (openViews.length === 0) {
        // Hide bench and show navigation if this is the last view to be hidden
        this.switchToNavigation();
      }
    }
    form.displayParent.formController.unregisterAndRemove(form);
    if (!this.benchVisible || this.bench.getViews().length === 0) {
      // Bring outline to front if last view has been closed,
      // even if bench is invisible (compact case) to update state correctly and reshow elements (dialog etc.) linked to the outline
      this.bringOutlineToFront();
    }
  }

  /**
   * @see Form.isShown
   */
  isFormShown(form: Form): boolean {
    let displayParent = form.displayParent || this;
    return displayParent.formController.isFormShown(form);
  }

  /**
   * Collects all forms that are currently shown, independent of the {@link Form.displayParent}.
   * This means, forms that have an {@link Outline} or another {@link Form} as display parent, are returned as well.
   *
   * *Note*: `shown` does not necessarily mean, the user can see the content of the form for sure, see {@link Form.isShown}.
   */
  getShownForms(): Form[] {
    let forms = [];
    let displayParents = [this, ...this.getOutlines()];

    for (let displayParent of displayParents) {
      forms = forms.concat(displayParent.views).concat(displayParent.dialogs);
    }

    // Form can also be a display parent, collect child forms as well
    for (let form of forms) {
      forms = forms.concat(form.views).concat(form.dialogs);
    }
    return forms;
  }

  findFormWithExclusiveKey(exclusiveKey: any, formClass?: new() => Form): Form {
    let key = exclusiveKey;
    if (typeof exclusiveKey === 'function') {
      key = exclusiveKey();
    }
    scout.assertValue(key, 'ExclusiveKey expected');
    for (let form of this.getShownForms()) {
      if (formClass && !(form instanceof formClass)) {
        continue;
      }
      if (objects.equals(form.exclusiveKey(), key)) {
        return form;
      }
    }
    return null;
  }

  /**
   * Creates a new form using the given `formClass` and `formModel` unless there is already a form shown that is an instance of the `formClass` and has the same `exclusiveKey`.
   * @param exclusiveKey can be anything, a primitive, an object or a function returning the key.
   */
  createFormExclusive<TForm extends Form>(formClass: new() => TForm, formModel: InitModelOf<TForm>, exclusiveKey: any | (() => any)): TForm;
  /**
   * Creates a new form using the given `formCreator` unless there is already a form shown with the same `exclusiveKey`.
   * @param exclusiveKey can be anything, a primitive, an object or a function returning the key.
   */
  createFormExclusive<TForm extends Form>(formCreator: () => TForm, exclusiveKey: any | (() => any)): TForm;

  createFormExclusive<TForm extends Form>(formClassOrCreator: (new() => TForm) | (() => TForm), formModelOrExclusiveKey: any, exclusiveKey?: any): TForm {
    let formClass;
    let formCreator;
    let formModel;
    if (objects.isSameOrExtendsClass(formClassOrCreator, Form)) {
      formClass = formClassOrCreator;
      formModel = formModelOrExclusiveKey;
    } else {
      formCreator = formClassOrCreator;
      exclusiveKey = formModelOrExclusiveKey;
    }

    // If there is a form with the same exclusive key and form class, return it
    let form = !objects.isNullOrUndefined(exclusiveKey) && this.findFormWithExclusiveKey(exclusiveKey, formClass) as TForm;
    if (form) {
      return form;
    }

    // Otherwise, create a new form
    if (formClass) {
      form = scout.create(formClass, $.extend({}, formModel, {exclusiveKey}));
    } else {
      form = formCreator();
      form.setExclusiveKey(exclusiveKey);
    }
    return form;
  }

  /**
   * Brings the form into foreground so the user can see and work with it.
   *
   * In case of a {@link Form.DisplayHint.VIEW}, the tab will be selected.
   * In case of a {@link Form.DisplayHint.DIALOG}, the form will be moved to the front of all dialogs.
   *
   * If the form belongs to an outline (has {@link Form.displayParent} set to that outline) that is currently not active, the outline will be activated first.
   *
   * Only one form can be active at once. The currently active form is reflected by {@link activeForm}.
   */
  activateForm(form: Form) {
    if (!form) {
      this._setFormActivated(null);
      return;
    }
    let displayParent = form.displayParent || this;
    displayParent.formController.activateForm(form);
    this._setFormActivated(form);

    // If the form has a modal child dialog, this dialog needs to be activated as well.
    form.dialogs.forEach(dialog => {
      if (dialog.modal) {
        this.activateForm(dialog);
      }
    });
  }

  /** @internal */
  _setOutlineActivated() {
    this._setFormActivated(null);
    if (this.outline) {
      this.outline.activateCurrentPage();
    }
  }

  /** @internal */
  _setFormActivated(form: Form) {
    // If desktop is in rendering process it can not set a new active form. Instead, the active form from the model is selected.
    if (!this.rendered || this.initialFormRendering) {
      return;
    }
    if (this.activeForm === form) {
      return;
    }

    this.activeForm = form;

    if (!form) {
      // no form is activated -> show outline
      this.bringOutlineToFront();
    } else if (form.isView() && !form.detailForm && this.bench && this.bench.hasView(form)) {
      // view form was activated. send the outline to back to ensure the form is attached
      // exclude detail forms even though detail forms usually are not activated
      // Also only consider "real" views used in the bench and ignore other views (e.g. used in a form menu)
      this.sendOutlineToBack();

      this._updateSelectedViewTabs(form);
    }

    this.triggerFormActivate(form);
  }

  protected _setSelectedViewTabs(views: Map<DisplayViewId, Form> | Form[]) {
    this.selectedViewTabs = this.prepareSelectedViewTabs(views);
  }

  prepareSelectedViewTabs(views: Map<DisplayViewId, Form> | Form[]): Map<DisplayViewId, Form> {
    let map = new Map();
    views.forEach(view => {
      map.set(DesktopBench.normalizeDisplayViewId(view.displayViewId), view);
      view.one('destroy', this._selectedViewDestroyHandler);
    });
    return map;
  }

  protected _updateSelectedViewTabs(form: Form) {
    let displayViewId = DesktopBench.normalizeDisplayViewId(form.displayViewId);
    let selectedViewTabs = this.selectedViewTabs;
    if (form.displayParent instanceof Outline) {
      selectedViewTabs = form.displayParent.selectedViewTabs;
    } else if (form.displayParent instanceof Desktop) {
      // As soon as a desktop tab was selected, don't remember outline tabs in that section anymore
      // because it should not automatically unselect a desktop tab when switching outlines
      this.getOutlines().forEach(outline => outline.selectedViewTabs.delete(displayViewId));
    } else {
      return;
    }

    let previousForm = selectedViewTabs.get(displayViewId);
    if (previousForm === form) {
      return;
    }
    if (previousForm) {
      previousForm.off('destroy', this._selectedViewDestroyHandler);
    }
    selectedViewTabs.set(displayViewId, form);
    form.one('destroy', this._selectedViewDestroyHandler);
  }

  protected _onSelectedViewDestroy(event: Event<Form>) {
    let form = event.source;
    let displayViewId = DesktopBench.normalizeDisplayViewId(form.displayViewId);
    this.getOutlines().forEach(outline => outline.selectedViewTabs.delete(displayViewId));
    this.selectedViewTabs.delete(displayViewId);
  }

  getOutlines(): Outline[] {
    let outlines = new Set<Outline>();
    for (let child of this.children) {
      if (child instanceof Outline) {
        outlines.add(child);
      }
      if (child instanceof OutlineViewButton && child.outline) {
        outlines.add(child.outline);
      }
    }
    return Array.from(outlines);
  }

  triggerFormActivate(form: Form) {
    this.trigger('formActivate', {
      form: form
    });
  }

  cancelViews(forms: Form[]): JQuery.Promise<void> {
    let event = this.trigger('cancelForms', {
      forms: forms
    });
    if (!event.defaultPrevented) {
      return this._cancelViews(forms);
    }
    return $.resolvedPromise();
  }

  protected _cancelViews(forms: Form[]): JQuery.Promise<void> {
    // do not cancel forms when the form child hierarchy does not get canceled.
    forms = forms.filter((form: Form) => !arrays.find(form.views, view => view.modal));

    // if there's only one form simply cancel it directly
    if (forms.length === 1) {
      const form = forms[0];
      form.activate();
      return form.cancel();
    }

    // collect all forms in the display child hierarchy with unsaved changes.
    let unsavedForms = forms.filter(form => {
      let saveNeededChildDialogs = false;
      form.visitDisplayChildren((dialog: Form) => {
        if (dialog.saveNeeded) {
          saveNeededChildDialogs = true;
        }
      }, displayChild => displayChild instanceof Form);
      return form.saveNeeded || saveNeededChildDialogs;
    });

    // initialize with a resolved promise in case there are no unsaved forms.
    let waitFor: JQuery.Promise<Form[]> = $.resolvedPromise();
    if (unsavedForms.length > 0) {
      let unsavedFormChangesForm = scout.create(UnsavedFormChangesForm, {
        parent: this,
        session: this.session,
        displayParent: this,
        unsavedForms: unsavedForms
      });
      unsavedFormChangesForm.open();
      // promise that is resolved when the UnsavedFormChangesForm is stored and rejected if it is cancelled
      const deferred = $.Deferred();
      waitFor = deferred.promise();

      unsavedFormChangesForm.whenSave().then(() => {
        let formsToSave = unsavedFormChangesForm.openFormsField.value;
        formsToSave.forEach(form => {
          form.visitDisplayChildren(dialog => {
            // forms should be stored with ok(). Other display children can simply be closed.
            if (dialog instanceof Form) {
              dialog.ok();
            } else {
              dialog.close();
            }
          });
          form.ok();
        });
        deferred.resolve(formsToSave);
      });
      // reject promise if form is aborted or cancelled
      // will also be executed when the form is saved but has no effect as the promise is already resolved in this case
      unsavedFormChangesForm.whenClose().then(() => deferred.reject());
    }
    // only close the remaining forms if the UnsavedFormChangesForm is not aborted or cancelled (i.e. the promise is not rejected)
    return waitFor.then(formsToSave => {
      if (formsToSave) {
        // already saved & closed forms (handled by the UnsavedFormChangesForm)
        arrays.removeAll(forms, formsToSave);
      }
      // close the remaining forms that don't require saving.
      forms.forEach(form => {
        form.visitDisplayChildren(dialog => dialog.close());
        form.close();
      });
    });
  }

  /**
   * Called when the animation triggered by animationLayoutChange is complete (e.g. navigation or bench got visible/invisible)
   */
  onLayoutAnimationComplete() {
    if (!this.headerVisible) {
      this._removeHeader();
    }
    if (!this.navigationVisible) {
      this._removeNavigation();
    }
    if (!this.benchVisible) {
      this._removeBench();
    }
    this.trigger('animationEnd');
    this.animateLayoutChange = false;
  }

  onLayoutAnimationStep() {
    this.repositionTooltips();
  }

  onResize(event: JQuery.ResizeEvent) {
    this.revalidateLayoutTree();
  }

  resetPopstateHandler() {
    this.setPopstateHandler(this.onPopstate.bind(this));
  }

  setPopstateHandler(handler: (event: JQuery.TriggeredEvent) => void) {
    if (this.rendered || this.rendering) {
      let window = this.$container.window();
      if (this._popstateHandler) {
        window.off('popstate', this._popstateHandler);
      }
      if (handler) {
        window.on('popstate', handler);
      }
    }
    this._popstateHandler = handler;
  }

  onPopstate(event: JQuery.TriggeredEvent) {
    let originalEvent = event.originalEvent as PopStateEvent;
    let historyState = originalEvent.state as DesktopHistoryState;
    if (historyState && historyState.deepLinkPath) {
      this.trigger('historyEntryActivate', historyState);
    }
  }

  protected _onSplitterMove(event: SplitterMoveEvent) {
    // disallow a position greater than 50%
    this.resizing = true;
    let max = Math.floor(this.$container.outerWidth(true) / 2);
    if (event.position > max) {
      event.source.setPosition(max);
      event.preventDefault();
    }
  }

  protected _onSplitterPositionChange(event: SplitterPositionChangeEvent) {
    // No need to revalidate while layouting (desktop layout sets the splitter position and would trigger a re-layout)
    if (!this.htmlComp.layouting) {
      this.revalidateLayout();
    }
  }

  protected _onSplitterMoveEnd(event: SplitterMoveEndEvent) {
    let splitterPosition = event.position;

    // Store size
    if (this.cacheSplitterPosition) {
      this._storeCachedSplitterPosition(this.splitter.position);
    }

    // Check if splitter is smaller than min size
    if (splitterPosition < DesktopNavigation.BREADCRUMB_STYLE_WIDTH) {
      // Set width of navigation to BREADCRUMB_STYLE_WIDTH, using an animation.
      // While animating, update the desktop layout.
      // At the end of the animation, update the desktop layout, and store the splitter position.
      this.navigation.$container.animate({
        width: DesktopNavigation.BREADCRUMB_STYLE_WIDTH
      }, {
        progress: function() {
          this.resizing = true;
          this.splitter.setPosition();
          this.revalidateLayout();
          this.resizing = false; // progress seems to be called after complete again -> layout requires flag to be properly set
        }.bind(this),
        complete: function() {
          this.resizing = true;
          this.splitter.setPosition();
          // Store size
          if (this.cacheSplitterPosition) {
            this._storeCachedSplitterPosition(this.splitter.position);
          }
          this.revalidateLayout();
          this.resizing = false;
        }.bind(this)
      });
    } else {
      this.resizing = false;
    }
  }

  protected _loadCachedSplitterPosition(): string {
    return webstorage.getItemFromSessionStorage('scout:desktopSplitterPosition') ||
      webstorage.getItemFromLocalStorage('scout:desktopSplitterPosition:' + window.location.pathname);
  }

  protected _storeCachedSplitterPosition(splitterPosition: number) {
    webstorage.setItemToSessionStorage('scout:desktopSplitterPosition', splitterPosition + '');
    webstorage.setItemToLocalStorage('scout:desktopSplitterPosition:' + window.location.pathname, splitterPosition + '');
  }

  /** @internal */
  _onNotificationRemove(event: Event<DesktopNotification>) {
    if (this.notifications.length === 0 && this.$notifications) {
      this.$notifications.remove();
      this.$notifications = null;
    }
  }

  onReconnecting() {
    if (!this.offline) {
      return;
    }
    this._offlineNotification.reconnect();
  }

  onReconnectingSucceeded() {
    if (!this.offline) {
      return;
    }
    this.offline = false;
    this._offlineNotification.reconnectSucceeded();
    this._removeOfflineNotification();
  }

  onReconnectingFailed() {
    if (!this.offline) {
      return;
    }
    this._offlineNotification.reconnectFailed();
  }

  dataChange(dataType: object) {
    this.trigger('dataChange', dataType);
  }

  protected _activeTheme(): string {
    return cookies.get('scout.ui.theme') || Desktop.DEFAULT_THEME;
  }

  logoAction() {
    if (this.logoActionEnabled) {
      this.trigger('logoAction');
    }
  }

  protected _initTheme() {
    let theme = this.theme;
    if (this.url.hasParameter('theme')) {
      theme = strings.nullIfEmpty(this.url.getParameter('theme') as string) || Desktop.DEFAULT_THEME;
    } else if (!theme) {
      theme = this._activeTheme();
    }
    this.setTheme(theme);
  }

  /**
   * Changes the current theme.
   *
   * The theme name is stored in a persistent cookie called scout.ui.theme.
   * In order to activate it, the browser is reloaded so that the CSS files for the new theme can be downloaded.
   *
   * Since it is a persistent cookie, the theme will be activated again the next time the app is started, unless the cookie is deleted.
   * @see DesktopModel.theme
   */
  setTheme(theme: string) {
    this.setProperty('theme', theme);
    if (this.theme !== this._activeTheme()) {
      this._switchTheme(theme);
    }
  }

  protected _switchTheme(theme: string) {
    // Add a persistent cookie which expires in 30 days
    cookies.set('scout.ui.theme', theme, 30 * 24 * 3600);

    // Reload page in order to download the CSS files for the new theme
    // Don't remove body but make it invisible, otherwise JS exceptions might be thrown if body is removed while an action executed
    $('body').setVisible(false);
    let reloadOptions: ReloadPageOptions = {
      clearBody: false
    };
    // If parameter 'theme' exists in the URL, remove it now - otherwise the parameter would overrule the cookie settings
    if (this.url.hasParameter('theme')) {
      this.url.removeParameter('theme');
      reloadOptions.redirectUrl = this.url.toString();
    }
    scout.reloadPage(reloadOptions);
  }

  /**
   * Moves all the given overlays (popups, dialogs, message boxes etc.) before the target overlay and activates the focus context of the target overlay.
   *
   * @param overlaysToMove the overlays which should be moved before the target overlay
   * @param $targetOverlay the overlay which should eventually be on top of the movable overlays
   */
  moveOverlaysBehindAndFocus(overlaysToMove: HTMLElement[], $targetOverlay: JQuery | HTMLElement) {
    $targetOverlay = $.ensure($targetOverlay);
    $targetOverlay.nextAll().toArray().forEach(overlay => {
      if (arrays.containsAll(overlaysToMove, [overlay])) {
        $(overlay).insertBefore($targetOverlay);
      }
    });

    // Activate the focus context of the form (will restore the previously focused field)
    // This must not be done when the currently focused element is part of this dialog's DOM
    // subtree, even if it has a separate focus context. Otherwise, the dialog would be
    // (unnecessarily) activated, causing the current focus context to lose the focus.
    // Example: editable table with a cell editor popup --> editor should keep the focus
    // when the user clicks the clear icon ("x") inside the editor field.
    if (!$targetOverlay.isOrHas($targetOverlay.activeElement())) {
      this.session.focusManager.activateFocusContext($targetOverlay);
    }
  }

  /**
   * If the given widget is an overlay (i.e. its $container is one of the elements after $overlaySeparator),
   * the DOM order is adjusted such that the overlay is not displayed in front of other overlays that
   * belong to a later "context".
   *
   * For example, when two dialogs are open, both are rendered after the desktop's $overlaySeparator.
   *
   * - Context 0: Any overlay opened by a view is rendered before dialog1.$container.
   * - Context 1: Any overlay opened by dialog1 is rendered between dialog1.$container and before dialog2.$container.
   * - Context 2: Any overlay opened by dialog2 is rendered after dialog2.$container.
   *
   * Within each context, tooltips are always displayed first, followed by all other overlays (e.g. popups),
   * each in their opening order.
   */
  adjustOverlayOrder(overlay: Widget) {
    if (!this.rendered) {
      return;
    }

    let $allOverlays = this.$overlaySeparator.nextAll();
    if (!$allOverlays.is(overlay.$container)) {
      return; // not an overlay
    }
    const $overlays = $allOverlays.not(overlay.$container);

    // A version of scout.widget() that also works when the DOM is not yet linked with the
    // model (e.g. when an error tooltip is initially shown when opening a dialog).
    const findWidget = elem => {
      let widget = overlay.findParent(p => p.$container && p.$container.is(elem));
      return widget || scout.widget(elem);
    };

    // Returns the DOM element of the closest parent widget that is an overlay. To find it, we traverse the
    // list of overlay in reverse order and check if it contains the given widget. The order is important,
    // because dialogs linked via 'displayParent' form a single hierarchy.
    const findParentOverlay = widget => {
      let parentOverlay = null;
      $overlays.toArray().reverse().some(elem => {
        let overlayWidget = findWidget(elem);
        if (overlayWidget && !(overlayWidget instanceof Desktop) && overlayWidget.has(widget)) {
          parentOverlay = overlayWidget;
          return true; // found
        }
        return false;
      });
      return parentOverlay;
    };

    // ------

    // Find the parent overlay of the given overlay widget.
    // The result can also be null, e.g. when opening overlays from a view.
    let parentOverlay = findParentOverlay(overlay);

    // Find all tooltips that belong to the same parent overlay
    let siblingTooltipElements = $overlays.toArray().filter(elem => {
      let widget = findWidget(elem);
      if (widget instanceof Tooltip) {
        let parent = findParentOverlay(widget);
        if (parent === parentOverlay) {
          return true;
        }
      }
      return false;
    });

    // If the current overlay context contains tooltips, insert the given overlay after them.
    // Otherwise, insert it at the beginning of the current overlay context.
    if (arrays.hasElements(siblingTooltipElements)) {
      overlay.$container.insertAfter(arrays.last(siblingTooltipElements));
    } else {
      overlay.$container.insertAfter(parentOverlay ? parentOverlay.$container : this.$overlaySeparator);
    }
  }

  tooltipRendered(tooltip: Tooltip) {
    this.adjustOverlayOrder(tooltip);
    if (this._repositionTooltipsHandler) {
      return;
    }
    if (!this.$container.children('.tooltip').length) {
      return;
    }
    this._repositionTooltipsHandler = () => {
      this.repositionTooltips();
      this.session.layoutValidator.schedulePostValidateFunction(this._repositionTooltipsHandler);
    };
    this.session.layoutValidator.schedulePostValidateFunction(this._repositionTooltipsHandler);
  }

  tooltipRemoved(tooltip: Tooltip) {
    if (!this._repositionTooltipsHandler) {
      return;
    }
    if (this.$container.children('.tooltip').length) {
      return;
    }
    this.session.layoutValidator.removePostValidateFunction(this._repositionTooltipsHandler);
    this._repositionTooltipsHandler = null;
  }

  repositionTooltips() {
    this.$container.children('.tooltip').each(function() {
      let widget = scout.widget($(this)) as Tooltip;
      widget.position();
    });
  }

  protected override _renderTrackFocus() {
    if (this.trackFocus) {
      // Use capture phase because FocusContext stops propagation
      this.$container[0].addEventListener('focusin', this._focusInListener, true);
    } else {
      this.$container[0].removeEventListener('focusin', this._focusInListener, true);
    }
  }

  protected override _onFocusIn(event: JQuery.FocusInEvent) {
    super._onFocusIn(event);
    let $target = $(event.target);
    let focusedElement = scout.widget($target);
    this.setProperty('focusedElement', focusedElement);
  }
}

export type DesktopDisplayStyle = EnumObject<typeof Desktop.DisplayStyle>;
export type DesktopUriAction = EnumObject<typeof Desktop.UriAction>;
export type NativeNotificationDefaults = {
  title?: string;
  iconId?: string;
  visibility?: NativeNotificationVisibility;
};
export type BrowserHistoryEntry = {
  path: string;
  title: string;
  deepLinkPath: string;
  pathVisible: boolean;
};
export type DesktopHistoryState = { deepLinkPath: string };
export type GlassPaneTargetFilter = (target: HTMLElement, element: Widget) => boolean;
