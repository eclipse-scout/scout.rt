/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, BenchColumnLayoutData, cookies, DeferredGlassPaneTarget, DesktopLayout, DesktopNavigation, Device, DisableBrowserF5ReloadKeyStroke, DisableBrowserTabSwitchingKeyStroke, Event, FileChooserController, Form, HtmlComponent, HtmlEnvironment, KeyStrokeContext, MessageBoxController, objects, Outline, Popup, scout, SimpleTabArea, strings, styles, Tree, URL, webstorage, Widget, widgets} from '../index';
import $ from 'jquery';

export default class Desktop extends Widget {

  constructor() {
    super();

    this.desktopStyle = Desktop.DisplayStyle.DEFAULT;

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
    /** @type NativeNotificationDefaults|null */
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
    this.selectedViewTabs = [];
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
    this._glassPaneTargetFilters = [];
    this.url = null;

    this._addWidgetProperties(['viewButtons', 'menus', 'views', 'selectedViewTabs', 'dialogs', 'outline', 'messageBoxes', 'notifications', 'fileChoosers', 'addOns', 'keyStrokes', 'activeForm', 'focusedElement']);
    this._addPreserveOnPropertyChangeProperties(['focusedElement']);

    // event listeners
    this._benchActiveViewChangedHandler = this._onBenchActivateViewChanged.bind(this);
  }

  static DisplayStyle = {
    DEFAULT: 'default',
    BENCH: 'bench',
    COMPACT: 'compact'
  };

  /**
   * The action that should be performed when handling an "open URI" event.
   */
  static UriAction = {
    /**
     * The object represented by the URI should be downloaded rather then be handled by the browser's rendering engine.
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
     * Note that this action may open the object in a new window or tab tab which may be prevented by
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
  };

  static DEFAULT_THEME = 'default';

  _init(model) {
    // Note: session and desktop are tightly coupled. Because a lot of widgets want to register
    // a listener on the desktop in their init phase, they access the desktop by calling 'this.session.desktop'
    // that's why we need this instance as early as possible. When that happens they access a desktop which is
    // not yet fully initialized. But anyway, it's already possible to attach a listener, for instance.
    // Because of this line of code here, we don't have to set the variable in App.js, after the desktop has been
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
    this.formController = scout.create('DesktopFormController', {
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
    this._setMenus(this.menus);
    this._setKeyStrokes(this.keyStrokes);
    this._setBenchLayoutData(this.benchLayoutData);
    this._setDisplayStyle(this.displayStyle);
    this._setDense(this.dense);
    this.openUriHandler = scout.create('OpenUriHandler', {
      session: this.session
    });
    this._glassPaneTargetFilters.push((targetElem, element) => {
      // Exclude all child elements of the given widget
      // Use case: element is a popup and has tooltip open. The tooltip is displayed in the desktop and considered as glass pane target by the selector above
      let target = scout.widget(targetElem);
      return !element.has(target);
    });
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.keyStrokeContext.registerKeyStroke([
      new DisableBrowserF5ReloadKeyStroke(this),
      new DisableBrowserTabSwitchingKeyStroke(this)
    ]);
  }

  /**
   * @param {object} model desktop model
   * @return {NativeNotificationDefaults}
   */
  _createNativeNotificationDefaults(model) {
    return $.extend({
      title: model.title,
      iconId: model.logoId
    }, model.nativeNotificationDefaults);
  }

  /**
   * @param {NativeNotificationDefaults} defaults
   */
  setNativeNotificationDefaults(defaults) {
    this.setProperty('nativeNotificationDefaults', defaults);
  }

  _onBenchActivateViewChanged(event) {
    if (this.initialFormRendering) {
      return;
    }
    let view = event.view;
    if (view instanceof Form && this.bench.outlineContent !== view && !view.detailForm) {
      // Notify model that this form is active (only for regular views, not detail forms)
      this._setFormActivated(view);
    }
  }

  _render() {
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
    this.addOns.forEach(addOn => {
      addOn.render();
    }, this);

    // prevent general drag and drop, dropping a file anywhere in the application must not open this file in browser
    this._setupDragAndDrop();

    this._disableContextMenu();
  }

  _remove() {
    this.formController.remove();
    this.messageBoxController.remove();
    this.fileChooserController.remove();
    this.$container.window()
      .off('resize', this._resizeHandler)
      .off('popstate', this._popstateHandler);
    super._remove();
  }

  _postRender() {
    super._postRender();

    // Render attached forms, message boxes and file choosers.
    this.initialFormRendering = true;
    this._renderDisplayChildrenOfOutline();
    this.formController.render();
    this.messageBoxController.render();
    this.fileChooserController.render();
    this.initialFormRendering = false;
  }

  _setDisplayStyle(displayStyle) {
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

  setDense(dense) {
    this.setProperty('dense', dense);
  }

  _setDense(dense) {
    this._setProperty('dense', dense);

    styles.clearCache();
    HtmlEnvironment.get().init(this.dense ? 'dense' : null);
  }

  _renderDense() {
    this.$container.toggleClass('dense', this.dense);
  }

  _createLayout() {
    return new DesktopLayout(this);
  }

  /**
   * Displays attached forms, message boxes and file choosers.
   * Outline does not need to be rendered to show the child elements, it needs to be active (necessary if navigation is invisible)
   */
  _renderDisplayChildrenOfOutline() {
    if (!this.outline) {
      return;
    }
    this.outline.formController.render();
    this.outline.messageBoxController.render();
    this.outline.fileChooserController.render();

    // this restores the selected view after a page refresh. selectedViewTabs is only set by the model.
    if (this.outline.selectedViewTabs) {
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

  _removeDisplayChildrenOfOutline() {
    if (!this.outline) {
      return;
    }
    this.outline.formController.remove();
    this.outline.messageBoxController.remove();
    this.outline.fileChooserController.remove();
  }

  computeParentForDisplayParent(displayParent) {
    // Outline must not be used as parent, otherwise the children (form, messageboxes etc.) would be removed if navigation is made invisible
    // The functions _render/removeDisplayChildrenOfOutline take care that the elements are correctly rendered/removed on an outline switch
    let parent = displayParent;
    if (displayParent instanceof Outline) {
      parent = this;
    }
    return parent;
  }

  _renderTitle() {
    let title = this.title;
    if (title === undefined || title === null) {
      return;
    }
    let $scoutDivs = $('div.scout');
    if ($scoutDivs.length <= 1) { // only set document title in non-portlet case
      $scoutDivs.document(true).title = title;
    }
  }

  _renderActiveForm() {
    // NOP -> is handled in _setFormActivated when ui changes active form or if model changes form in _onFormShow/_onFormActivate
  }

  _renderBench() {
    if (this.bench) {
      return;
    }
    this.bench = this._createBench();
    this.bench.on('viewActivate', this._benchActiveViewChangedHandler);
    this.bench.render();
    this.bench.$container.insertBefore(this.$overlaySeparator);
    this.invalidateLayoutTree();
  }

  _createBench() {
    return scout.create('DesktopBench', {
      parent: this,
      animateRemoval: true,
      headerTabArea: this.header ? this.header.tabArea : undefined,
      outlineContentVisible: this.displayStyle !== Desktop.DisplayStyle.COMPACT
    });
  }

  _removeBench() {
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

  _renderBenchVisible() {
    this.animateLayoutChange = this.rendered;
    if (this.benchVisible) {
      this._renderBench();
      this._renderInBackground();
    } else {
      this._removeBench();
    }
  }

  _renderNavigation() {
    if (this.navigation) {
      return;
    }
    this.navigation = this._createNavigation();
    this.navigation.render();
    this.navigation.$container.prependTo(this.$container);
    this.$container.removeClass('navigation-invisible');
    this.invalidateLayoutTree();
  }

  _createNavigation() {
    return scout.create('DesktopNavigation', {
      parent: this,
      outline: this.outline,
      toolBoxVisible: this.displayStyle === Desktop.DisplayStyle.COMPACT,
      layoutData: {
        fullWidth: this.displayStyle === Desktop.DisplayStyle.COMPACT
      }
    });
  }

  _removeNavigation() {
    this.$container.addClass('navigation-invisible');
    if (!this.navigation) {
      return;
    }
    this.navigation.destroy();
    this.navigation = null;
    this.invalidateLayoutTree();
  }

  _renderNavigationVisible() {
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

  _renderHeader() {
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

  _createHeader() {
    let compact = this.displayStyle === Desktop.DisplayStyle.COMPACT;
    return scout.create('DesktopHeader', {
      parent: this,
      logoUrl: this.logoUrl,
      animateRemoval: compact,
      toolBoxVisible: !compact,
      tabArea: {
        displayStyle: compact ? SimpleTabArea.DisplayStyle.SPREAD_EVEN : SimpleTabArea.DisplayStyle.DEFAULT
      }
    });
  }

  _removeHeader() {
    if (!this.header) {
      return;
    }
    this.header.on('destroy', () => {
      this.invalidateLayoutTree();
      this.header = null;
    });
    this.header.destroy();
  }

  _renderHeaderVisible() {
    if (this.headerVisible) {
      this._renderHeader();
    } else {
      this._removeHeader();
    }
  }

  _renderLogoUrl() {
    if (this.header) {
      this.header.setLogoUrl(this.logoUrl);
    }
  }

  _renderSplitterVisible() {
    if (this.splitterVisible) {
      this._renderSplitter();
    } else {
      this._removeSplitter();
    }
  }

  _renderSplitter() {
    if (this.splitter || !this.navigation) {
      return;
    }
    this.splitter = scout.create('Splitter', {
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

  _removeSplitter() {
    if (!this.splitter) {
      return;
    }
    this.splitter.destroy();
    this.splitter = null;
  }

  _renderInBackground() {
    this.$container.toggleClass('in-background', this.inBackground && this.displayStyle !== Desktop.DisplayStyle.COMPACT);
    if (this.bench) {
      this.bench.$container.toggleClass('drop-shadow', this.inBackground);
    }
  }

  _renderBrowserHistoryEntry() {
    if (!Device.get().supportsHistoryApi()) {
      return;
    }
    let myWindow = this.$container.window(true),
      history = this.browserHistoryEntry;
    if (history) {
      let historyPath = this._createHistoryPath(history);
      let setStateFunc = (this.rendered ? myWindow.history.pushState : myWindow.history.replaceState).bind(myWindow.history);
      setStateFunc({
        deepLinkPath: history.deepLinkPath
      }, history.title, historyPath);
    }
  }

  /**
   * Takes the history.path provided by the browserHistoryEvent and appends additional URL parameters.
   */
  _createHistoryPath(history) {
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
        pathUrl.addParameter(paramName, cloneUrl.getParameter(paramName));
      }
      historyPath = pathUrl.toString({alwaysFirst: ['dl', 'i']});
    }
    return historyPath;
  }

  _setupDragAndDrop() {
    let dragEnterOrOver = event => {
      event.stopPropagation();
      event.preventDefault();
      // change cursor to forbidden (no dropping allowed)
      event.originalEvent.dataTransfer.dropEffect = 'none';
    };

    this.$container.on('dragenter', dragEnterOrOver);
    this.$container.on('dragover', dragEnterOrOver);
    this.$container.on('drop', event => {
      event.stopPropagation();
      event.preventDefault();
    });
  }

  updateSplitterVisibility() {
    // Splitter should only be visible if navigation and bench are visible, but never in compact mode (to prevent unnecessary splitter rendering)
    this.setSplitterVisible(this.navigationVisible && this.benchVisible && this.displayStyle !== Desktop.DisplayStyle.COMPACT);
  }

  setSplitterVisible(visible) {
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

  _disableContextMenu() {
    // Switch off browser's default context menu for the entire scout desktop (except input fields)
    this.$container.on('contextmenu', event => {
      if (event.target.nodeName !== 'INPUT' && event.target.nodeName !== 'TEXTAREA' && !event.target.isContentEditable) {
        event.preventDefault();
      }
    });
  }

  setOutline(outline) {
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

  _setViews(views) {
    if (views) {
      views.forEach(view => {
        view.setDisplayParent(this);
      });
    }
    this._setProperty('views', views);
  }

  _setViewButtons(viewButtons) {
    this.updateKeyStrokes(viewButtons, this.viewButtons);
    this._setProperty('viewButtons', viewButtons);
  }

  setMenus(menus) {
    if (this.header) {
      this.header.setMenus(menus);
    }
  }

  _setMenus(menus) {
    this.updateKeyStrokes(menus, this.menus);
    this._setProperty('menus', menus);
  }

  _setKeyStrokes(keyStrokes) {
    this.updateKeyStrokes(keyStrokes, this.keyStrokes);
    this._setProperty('keyStrokes', keyStrokes);
  }

  setNavigationHandleVisible(visible) {
    this.setProperty('navigationHandleVisible', visible);
  }

  _renderNavigationHandleVisible() {
    this.$container.toggleClass('has-navigation-handle', this.navigationHandleVisible);
  }

  setNavigationVisible(visible) {
    this.setProperty('navigationVisible', visible);
    this.updateSplitterVisibility();
  }

  setBenchVisible(visible) {
    this.setProperty('benchVisible', visible);
    this.updateSplitterVisibility();
  }

  setHeaderVisible(visible) {
    this.setProperty('headerVisible', visible);
  }

  _setBenchLayoutData(layoutData) {
    layoutData = BenchColumnLayoutData.ensure(layoutData);
    this._setProperty('benchLayoutData', layoutData);
  }

  _setInBackground(inBackground) {
    this._setProperty('inBackground', inBackground);
  }

  outlineDisplayStyle() {
    if (this.outline) {
      return this.outline.displayStyle;
    }
  }

  shrinkNavigation() {
    if (this.outline && this.outline.toggleBreadcrumbStyleEnabled && this.navigationVisible &&
      this.outlineDisplayStyle() === Tree.DisplayStyle.DEFAULT) {
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
   * @param {boolean} headerVisible whether the desktop header should be made visible
   */
  switchToBench(headerVisible) {
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
    this._offlineNotification = scout.create('DesktopNotification:Offline', {
      parent: this
    });
    this._offlineNotification.show();
  }

  goOnline() {
    this._removeOfflineNotification();
  }

  _removeOfflineNotification() {
    if (this._offlineNotification) {
      setTimeout(this.removeNotification.bind(this, this._offlineNotification), 3000);
      this._offlineNotification = null;
    }
  }

  addNotification(notification) {
    if (!notification) {
      return;
    }
    this.notifications.push(notification);
    if (this.rendered) {
      this._renderNotification(notification);
    }
  }

  _renderNotification(notification) {
    if (this.$notifications) {
      // Bring to front
      this.$notifications.appendTo(this.$container);
    } else {
      this.$notifications = this.$container.appendDiv('desktop-notifications');
    }
    notification.fadeIn(this.$notifications);
    if (notification.duration > 0) {
      notification.removeTimeout = setTimeout(notification.hide.bind(notification), notification.duration);
      notification.one('remove', () => {
        this.removeNotification(notification);
      });
    }
  }

  _renderNotifications() {
    this.notifications.forEach(notification => {
      this._renderNotification(notification);
    });
  }

  /**
   * Removes the given notification.
   * @param {DesktopNotification|string} notification Either an instance of DesktopNavigation or a String containing an ID of a notification instance.
   */
  removeNotification(notification) {
    if (typeof notification === 'string') {
      let notificationId = notification;
      notification = arrays.find(this.notifications, n => {
        return notificationId === n.id;
      });
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
      notification.one('remove', this._onNotificationRemove.bind(this, notification));
    }
    notification.fadeOut();
  }

  getPopups() {
    if (!this.$container) {
      return [];
    }
    let popups = [];
    this.$container.children('.popup').each((i, elem) => {
      let $popup = $(elem);
      let popup = widgets.get($popup);
      if (popup instanceof Popup) {
        popups.push(popup);
      }
    });
    return popups;
  }

  getPopupsFor(widget) {
    return this.getPopups().filter(popup => widget.has(popup));
  }

  /**
   * Removes every popup which is a descendant of the given widget.
   */
  removePopupsFor(widget) {
    this.getPopupsFor(widget).forEach(popup => {
      popup.remove();
    });
  }

  /**
   * Opens the uri using {@link OpenUriHandler}
   * @param {string} uri the uri to open
   * @param {Desktop.UriAction} [action] the action to be performed on the given uri. Default is Desktop.UriAction.OPEN.
   */
  openUri(uri, action) {
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
  inFront() {
    return true; // Desktop is always available to the user.
  }

  /**
   * === Method required for objects that act as 'displayParent' ===
   *
   * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box, file-chooser or wait-dialog is showed with the Desktop as its 'displayParent'.
   */
  _glassPaneTargets(element) {
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

    let glassPaneTargets;
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

  _isGlassPaneTargetFiltered(targetElem, element) {
    return this._glassPaneTargetFilters.every(filter => {
      return filter(targetElem, element);
    }, this);
  }

  /**
   * Adds a filter which is applied when the glass pane targets are collected.
   * If the filter returns <code>false</code>, the target won't be accepted and not covered by a glass pane.
   * This filter should be used primarily for elements like the help-popup which stand outside
   * of the regular modality hierarchy.
   *
   * @param {function} filter a function with the parameter target and element. Target is the element which
   *     would be covered by a glass pane, element is the element the user interacts with (e.g. the modal dialog).
   * @see _glassPaneTargets
   */
  addGlassPaneTargetFilter(filter) {
    this._glassPaneTargetFilters.push(filter);
  }

  removeGlassPaneTargetFilter(filter) {
    arrays.remove(this._glassPaneTargetFilters, filter);
  }

  /**
   * This 'deferred' object is used because popup windows are not immediately usable when they're opened.
   * That's why we must render the glass-pane of a popup window later. Which means, at the point in time
   * when its $container is created and ready for usage. To avoid race conditions we must also wait until
   * the glass pane renderer is ready. Only when both conditions are fullfilled, we can render the glass
   * pane.
   */
  _deferredGlassPaneTarget(popupWindow) {
    let deferred = new DeferredGlassPaneTarget();
    popupWindow.one('init', () => {
      deferred.ready([popupWindow.$container]);
    });
    return deferred;
  }

  _getBenchGlassPaneTargetsForView(view) {
    let $glassPanes = [];

    $glassPanes = $glassPanes.concat(this._getTabGlassPaneTargetsForView(view, this.header));

    if (this.bench) {
      this.bench.visibleTabBoxes().forEach(function(tabBox) {
        if (!tabBox.rendered) {
          return;
        }
        if (tabBox.hasView(view)) {
          $glassPanes = $glassPanes.concat(this._getTabGlassPaneTargetsForView(view, tabBox));
        } else {
          $glassPanes.push(tabBox.$container);
        }
      }, this);
    }
    return $glassPanes;
  }

  _getTabGlassPaneTargetsForView(view, tabBox) {
    let $glassPanes = [];
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

  _pushPopupWindowGlassPaneTargets(glassPaneTargets, element) {
    this.formController._popupWindows.forEach(function(popupWindow) {
      if (element === popupWindow.form) {
        // Don't block form itself
        return;
      }
      glassPaneTargets.push(popupWindow.initialized ?
        popupWindow.$container[0] : this._deferredGlassPaneTarget(popupWindow));
    }, this);
  }

  showForm(form, position) {
    let displayParent = form.displayParent || this;
    form.setDisplayParent(displayParent);

    this._setFormActivated(form);
    // register listener to recover active form when child dialog is removed
    displayParent.formController.registerAndRender(form, position, true);
  }

  hideForm(form) {
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
   * @see {@link Form.isShown}
   * @param {Form} form
   */
  isFormShown(form) {
    let displayParent = form.displayParent || this;
    return displayParent.formController.isFormShown(form);
  }

  activateForm(form) {
    if (!form) {
      this._setFormActivated(null);
      return;
    }
    let displayParent = form.displayParent || this;
    displayParent.formController.activateForm(form);
    this._setFormActivated(form);

    // If the form has a modal child dialog, this dialog needs to be activated as well.
    form.dialogs.forEach(function(dialog) {
      if (dialog.modal) {
        this.activateForm(dialog);
      }
    }, this);
  }

  _setOutlineActivated() {
    this._setFormActivated(null);
    if (this.outline) {
      this.outline.activateCurrentPage();
    }
  }

  _setFormActivated(form) {
    // If desktop is in rendering process the can not set a new active form. instead the active form from the model is set selected.
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
    } else if (form.displayHint === Form.DisplayHint.VIEW && !form.detailForm && this.bench && this.bench.hasView(form)) {
      // view form was activated. send the outline to back to ensure the form is attached
      // exclude detail forms even though detail forms usually are not activated
      // Also only consider "real" views used in the bench and ignore other views (e.g. used in a form menu)
      this.sendOutlineToBack();
    }

    this.triggerFormActivate(form);
  }

  triggerFormActivate(form) {
    this.trigger('formActivate', {
      form: form
    });
  }

  cancelViews(forms) {
    let event = new Event();
    event.forms = forms;
    this.trigger('cancelForms', event);
    if (!event.defaultPrevented) {
      this._cancelViews(forms);
    }
  }

  _cancelViews(forms) {
    // do not cancel forms when the form child hierarchy does not get canceled.
    forms = forms.filter(form => {
      return !arrays.find(form.views, view => {
        return view.modal;
      });
    });

    // if there's only one form simply cancel it directly
    if (forms.length === 1) {
      forms[0].cancel();
      return;
    }

    // collect all forms in the display child hierarchy with unsaved changes.
    let unsavedForms = forms.filter(form => {
      let requiresSaveChildDialogs = false;
      form.visitDisplayChildren(dialog => {
        if (dialog.lifecycle.requiresSave()) {
          requiresSaveChildDialogs = true;
        }
      }, displayChild => {
        return displayChild instanceof Form;
      });
      return form.lifecycle.requiresSave() || requiresSaveChildDialogs;
    });

    // initialize with a resolved promise in case there are no unsaved forms.
    let waitFor = $.resolvedPromise();
    if (unsavedForms.length > 0) {
      let unsavedFormChangesForm = scout.create('scout.UnsavedFormChangesForm', {
        parent: this,
        session: this.session,
        displayParent: this,
        unsavedForms: unsavedForms
      });
      unsavedFormChangesForm.open();
      // promise that is resolved when the UnsavedFormChangesForm is stored
      waitFor = unsavedFormChangesForm.whenSave().then(() => {
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
        return formsToSave;
      });
    }
    waitFor.then(formsToSave => {
      if (formsToSave) {
        // already saved & closed forms (handled by the UnsavedFormChangesForm)
        arrays.removeAll(forms, formsToSave);
      }
      // close the remaining forms that don't require saving.
      forms.forEach(form => {
        form.visitDisplayChildren(dialog => {
          dialog.close();
        });
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

  onResize(event) {
    this.revalidateLayoutTree();
  }

  resetPopstateHandler() {
    this.setPopstateHandler(this.onPopstate.bind(this));
  }

  setPopstateHandler(handler) {
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

  onPopstate(event) {
    let historyState = event.originalEvent.state;
    if (historyState && historyState.deepLinkPath) {
      this.trigger('historyEntryActivate', historyState);
    }
  }

  _onSplitterMove(event) {
    // disallow a position greater than 50%
    this.resizing = true;
    let max = Math.floor(this.$container.outerWidth(true) / 2);
    if (event.position > max) {
      event.setPosition(max);
    }
  }

  _onSplitterPositionChange(event) {
    // No need to revalidate while layouting (desktop layout sets the splitter position and would trigger a relayout)
    if (!this.htmlComp.layouting) {
      this.revalidateLayout();
    }
  }

  _onSplitterMoveEnd(event) {
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

  _loadCachedSplitterPosition() {
    return webstorage.getItemFromSessionStorage('scout:desktopSplitterPosition') ||
      webstorage.getItemFromLocalStorage('scout:desktopSplitterPosition:' + window.location.pathname);
  }

  _storeCachedSplitterPosition(splitterPosition) {
    webstorage.setItemToSessionStorage('scout:desktopSplitterPosition', splitterPosition);
    webstorage.setItemToLocalStorage('scout:desktopSplitterPosition:' + window.location.pathname, splitterPosition);
  }

  _onNotificationRemove(notification) {
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

  dataChange(dataType) {
    this.events.trigger('dataChange', dataType);
  }

  _activeTheme() {
    return cookies.get('scout.ui.theme') || Desktop.DEFAULT_THEME;
  }

  logoAction() {
    if (this.logoActionEnabled) {
      this.trigger('logoAction');
    }
  }

  _initTheme() {
    let theme = this.theme;
    if (this.url.hasParameter('theme')) {
      theme = strings.nullIfEmpty(this.url.getParameter('theme')) || Desktop.DEFAULT_THEME;
    } else if (theme === null) {
      theme = this._activeTheme();
    }
    this.setTheme(theme);
  }

  /**
   * Changes the current theme.
   * <p>
   * The theme name is stored in a persistent cookie called scout.ui.theme.
   * In order to activate it, the browser is reloaded so that the CSS files for the new theme can be downloaded.
   * <p>
   * Since it is a persistent cookie, the theme will be activated again the next time the app is started, unless the cookie is deleted.
   */
  setTheme(theme) {
    this.setProperty('theme', theme);
    if (this.theme !== this._activeTheme()) {
      this._switchTheme(theme);
    }
  }

  _switchTheme(theme) {
    // Add a persistent cookie which expires in 30 days
    cookies.set('scout.ui.theme', theme, 30 * 24 * 3600);

    // Reload page in order to download the CSS files for the new theme
    // Don't remove body but make it invisible, otherwise JS exceptions might be thrown if body is removed while an action executed
    $('body').setVisible(false);
    let reloadOptions = {
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
   * @param overlaysToMove {HTMLElement[]} the overlays which should be moved before the target overlay
   * @param $targetOverlay {$|HTMLElement} the overlay which should eventually be on top of the movable overlays
   */
  moveOverlaysBehindAndFocus(overlaysToMove, $targetOverlay) {
    $targetOverlay = $.ensure($targetOverlay);
    $targetOverlay.nextAll().toArray()
      .forEach(overlay => {
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

  repositionTooltips() {
    this.$container.children('.tooltip').each(function() {
      scout.widget($(this)).position();
    });
  }

  _renderTrackFocus() {
    if (this.trackFocus) {
      // Use capture phase because FocusContext stops propagation
      this.$container[0].addEventListener('focusin', this._focusInListener, true);
    } else {
      this.$container[0].removeEventListener('focusin', this._focusInListener, true);
    }
  }

  _onFocusIn(event) {
    super._onFocusIn(event);
    let $target = $(event.target);
    let focusedElement = scout.widget($target);
    this.setProperty('focusedElement', focusedElement);
  }
}
