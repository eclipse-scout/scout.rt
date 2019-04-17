import * as $ from 'jquery';
import * as scout from '../scout';
import Widget from './../widget/Widget';
import * as arrays from '../util/arrays';
import BenchColumnLayoutData from './BenchColumnLayoutData';
import HtmlComponent from '../layout/HtmlComponent';
import DesktopLayout from './DesktopLayout';
import DesktopNavigation, { BREADCRUMB_STYLE_WIDTH } from './DesktopNavigation';
import DesktopHeader from './DesktopHeader';
import DesktopBench from './DesktopBench';
import Splitter from '../splitter/Splitter';
import DeferredGlassPaneTarget from '../glasspane/DeferredGlassPaneTarget';
import { DisplayHint } from '../form/Form';
import Outline from '../outline/Outline';
import Table from '../table/Table';
import Tree, { DisplayStyle as TreeDisplayStyle } from '../tree/Tree';
import * as cookies from '../util/cookies';

export default class Desktop extends Widget {

  constructor() {
    super();
    this.desktopStyle = DisplayStyle.DEFAULT;
    this.benchVisible = true;
    this.headerVisible = true;
    this.navigationVisible = true;
    this.navigationHandleVisible = true;
    this.menus = [];
    this.addOns = [];
    this.dialogs = [];
    this.views = [];
    this.viewButtons = [];
    this.messageBoxes = [];
    this.fileChoosers = [];
    this.navigation = null;
    this.header = null;
    this.bench = null;
    this.splitter = null;
    this.formController = null;
    this.messageBoxController = null;
    this.fileChooserController = null;
    this.initialFormRendering = false;
    this.offline = false;
    this.notifications = [];
    this.inBackground = false;
    this.geolocationServiceAvailable = false;
    this.openUriHandler = null;
    this.theme = null;

    this._addWidgetProperties(['viewButtons', 'menus', 'views', 'selectedViewTabs', 'dialogs', 'outline', 'messageBoxes', 'notifications', 'fileChoosers', 'addOns', 'keyStrokes', 'activeForm']);

    // event listeners
    this._benchActiveViewChangedHandler = this._onBenchActivateViewChanged.bind(this);
  }

  _init(model) {
    super._init(model);

    this._initTheme();
    /*this.formController = scout.create('DesktopFormController', {
        displayParent: this,
        session: this.session
    });*/
    //this.messageBoxController = new scout.MessageBoxController(this, this.session);
    //this.fileChooserController = new scout.FileChooserController(this, this.session);
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
    /*this.openUriHandler = scout.create('OpenUriHandler', {
        session: this.session
    });*/

    // Note: session and desktop are tightly coupled. Because a lot of widgets want to register
    // a listener on the desktop in their init phase, they access the desktop by calling 'this.session.desktop'
    // that's why we need this instance as early as possible. When that happens they access a desktop which is
    // not yet fully initialized. But anyway, it's already possible to attach a listener, for instance.
    // Because of this line of code here, we don't have to set the variable in App.js, after the desktop has been
    // created. Also note that Scout Java uses a different pattern to solve the same problem, there a VirtualDesktop
    // is used during initialization. When initialization is done, all registered listeners on the virtual desktop
    // are copied to the real desktop instance.
    this.session.desktop = this;
  };

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return null; //new scout.KeyStrokeContext();
  };

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    /*this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    // Keystroke on the top-level DOM element which works as a catch-all when the busy indicator is active
    this.keyStrokeContext.registerKeyStroke(new scout.DesktopKeyStroke(this.session));
    this.keyStrokeContext.registerKeyStroke(new scout.DisableBrowserTabSwitchingKeyStroke(this));*/
  };

  _onBenchActivateViewChanged(event) {
    if (this.initialFormRendering) {
      return;
    }
    var view = event.view;
    /*if (view instanceof scout.Form && this.bench.outlineContent !== view && !view.detailForm) {
        // Notify model that this form is active (only for regular views, not detail forms)
        this._setFormActivated(view);
    }*/
  };

  _render() {
    this.$container = this.$parent;
    this.$container.addClass('desktop');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());

    // Desktop elements are added before this separator, all overlays are opened after (dialogs, popups, tooltips etc.)
    this.$overlaySeparator = this.$container.appendDiv('overlay-separator').setVisible(false);

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
    this.addOns.forEach(function(addOn) {
      addOn.render();
    }, this);

    this.$container.window()
      .on('resize', this._resizeHandler)
      .on('popstate', this._popstateHandler);

    // prevent general drag and drop, dropping a file anywhere in the application must not open this file in browser
    this._setupDragAndDrop();

    this._disableContextMenu();
  };

  _remove() {
    //this.formController.remove();
    //this.messageBoxController.remove();
    //this.fileChooserController.remove();
    this.$container.window()
      .off('resize', this._resizeHandler)
      .off('popstate', this._popstateHandler);
    super.remove();
  };

  _postRender() {
    super._postRender();

    // Render attached forms, message boxes and file choosers.
    this.initialFormRendering = true;
    this._renderDisplayChildrenOfOutline();
    //this.formController.render();
    //this.messageBoxController.render();
    //this.fileChooserController.render();
    this.initialFormRendering = false;
  };

  _setDisplayStyle() {
    var isCompact = this.displayStyle === DisplayStyle.COMPACT;

    if (this.header) {
      this.header.setToolBoxVisible(!isCompact);
      this.header.animateRemoval = isCompact;
    }
    if (this.navigation) {
      this.navigation.setToolBoxVisible(isCompact);
      this.navigation.htmlComp.layoutData.fullWidth = isCompact;
    }
    if (this.bench) {
      this.bench.setOutlineContentVisible(!isCompact);
    }
    if (this.outline) {
      this.outline.setCompact(isCompact);
      this.outline.setEmbedDetailContent(isCompact);
    }
  };

  _createLayout() {
    return new DesktopLayout(this);
  };

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

    if (this.outline.selectedViewTabs) {
      this.outline.selectedViewTabs.forEach(function(selectedView) {
        this.formController._activateView(selectedView);
      }.bind(this));
    }
  };

  _removeDisplayChildrenOfOutline() {
    if (!this.outline) {
      return;
    }
    this.outline.formController.remove();
    this.outline.messageBoxController.remove();
    this.outline.fileChooserController.remove();
  };

  computeParentForDisplayParent(displayParent) {
    // Outline must not be used as parent, otherwise the children (form, messageboxes etc.) would be removed if navigation is made invisible
    // The functions _render/removeDisplayChildrenOfOutline take care that the elements are correctly rendered/removed on an outline switch
    var parent = displayParent;
    if (displayParent instanceof Outline) {
      parent = this;
    }
    return parent;
  };

  _renderTitle() {
    var title = this.title;
    if (title === undefined || title === null) {
      return;
    }
    var $scoutDivs = $('div.scout');
    if ($scoutDivs.length <= 1) { // only set document title in non-portlet case
      $scoutDivs.document(true).title = title;
    }
  };

  _renderActiveForm() {
    // NOP -> is handled in _setFormActivated when ui changes active form or if model changes form in _onFormShow/_onFormActivate
  };

  _renderBench() {
    if (this.bench) {
      return;
    }
    this.bench = scout.create(DesktopBench, {
      parent: this,
      animateRemoval: true,
      headerTabArea: this.header ? this.header.tabArea : undefined,
      outlineContentVisible: this.displayStyle !== DisplayStyle.COMPACT
    });
    this.bench.on('viewActivate', this._benchActiveViewChangedHandler);
    this.bench.render();
    this.bench.$container.insertBefore(this.$overlaySeparator);
    this.invalidateLayoutTree();
  };

  _removeBench() {
    if (!this.bench) {
      return;
    }
    this.bench.off('viewActivate', this._benchActiveViewChangedHandler);
    this.bench.on('destroy', function() {
      this.bench = null;
      this.invalidateLayoutTree();
    }.bind(this));
    this.bench.destroy();
  };

  _renderBenchVisible() {
    this.animateLayoutChange = this.rendered;
    if (this.benchVisible) {
      this._renderBench();
      this._renderInBackground();
    } else {
      this._removeBench();
    }
  };

  _renderNavigation() {
    if (this.navigation) {
      return;
    }
    this.navigation = scout.create(DesktopNavigation, {
      parent: this,
      outline: this.outline,
      toolBoxVisible: this.displayStyle === DisplayStyle.COMPACT,
      layoutData: {
        fullWidth: this.displayStyle === DisplayStyle.COMPACT
      }
    });
    this.navigation.render();
    this.navigation.$container.prependTo(this.$container);
    this.invalidateLayoutTree();
  };

  _removeNavigation() {
    if (!this.navigation) {
      return;
    }
    this.navigation.destroy();
    this.navigation = null;
    this.invalidateLayoutTree();
  };

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
  };

  _renderHeader() {
    if (this.header) {
      return;
    }
    this.header = scout.create(DesktopHeader, {
      parent: this,
      animateRemoval: this.displayStyle === DisplayStyle.COMPACT,
      toolBoxVisible: this.displayStyle !== DisplayStyle.COMPACT
    });
    this.header.render();
    this.header.$container.insertBefore(this.$overlaySeparator);
    this.invalidateLayoutTree();
  };

  _removeHeader() {
    if (!this.header) {
      return;
    }
    this.header.on('destroy', function() {
      this.invalidateLayoutTree();
      this.header = null;
    }.bind(this));
    this.header.destroy();
  };

  _renderHeaderVisible() {
    if (this.headerVisible) {
      this._renderHeader();
    } else {
      this._removeHeader();
    }
  };

  _renderLogoUrl() {
    if (this.header) {
      //this.header.setLogoUrl(this.logoUrl);
    }
  };

  _renderSplitterVisible() {
    if (this.splitterVisible) {
      this._renderSplitter();
    } else {
      this._removeSplitter();
    }
  };

  _renderSplitter() {
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
  };

  _removeSplitter() {
    if (!this.splitter) {
      return;
    }
    this.splitter.destroy();
    this.splitter = null;
  };

  _renderInBackground() {
    if (this.bench) {
      this.bench.$container.toggleClass('drop-shadow', this.inBackground);
    }
  };

  _renderBrowserHistoryEntry() {
    /*if (!scout.device.supportsHistoryApi()) {
        return;
    }
    var myWindow = this.$container.window(true),
        history = this.browserHistoryEntry;
    if (history) {
        var setStateFunc = (this.rendered ? myWindow.history.pushState : myWindow.history.replaceState).bind(myWindow.history);
        setStateFunc({
            deepLinkPath: history.deepLinkPath
        }, history.title, history.path);
    }*/
  };

  _setupDragAndDrop() {
    var dragEnterOrOver = function(event) {
      event.stopPropagation();
      event.preventDefault();
      // change cursor to forbidden (no dropping allowed)
      event.originalEvent.dataTransfer.dropEffect = 'none';
    };

    this.$container.on('dragenter', dragEnterOrOver);
    this.$container.on('dragover', dragEnterOrOver);
    this.$container.on('drop', function(event) {
      event.stopPropagation();
      event.preventDefault();
    });
  };

  updateSplitterVisibility() {
    // Splitter should only be visible if navigation and bench are visible, but never in compact mode (to prevent unnecessary splitter rendering)
    this.setSplitterVisible(this.navigationVisible && this.benchVisible && this.displayStyle !== DisplayStyle.COMPACT);
  };

  setSplitterVisible(visible) {
    this.setProperty('splitterVisible', visible);
  };

  updateSplitterPosition() {
    if (!this.splitter) {
      return;
    }
    var storedSplitterPosition = this.cacheSplitterPosition && this._loadCachedSplitterPosition();
    if (storedSplitterPosition) {
      // Restore splitter position
      var splitterPosition = parseInt(storedSplitterPosition, 10);
      this.splitter.setPosition(splitterPosition);
      this.invalidateLayoutTree();
    } else {
      // Set initial splitter position (default defined by css)
      this.splitter.setPosition();
      this.invalidateLayoutTree();
    }
  };

  _disableContextMenu() {
    // Switch off browser's default context menu for the entire scout desktop (except input fields)
    this.$container.on('contextmenu', function(event) {
      if (event.target.nodeName !== 'INPUT' && event.target.nodeName !== 'TEXTAREA' && !event.target.isContentEditable) {
        event.preventDefault();
      }
    });
  };

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
  };

  _setViews(views) {
    if (views) {
      views.forEach(function(view) {
        view.setDisplayParent(this);
      }.bind(this));
    }
    this._setProperty('views', views);
  };

  _setViewButtons(viewButtons) {
    this.updateKeyStrokes(viewButtons, this.viewButtons);
    this._setProperty('viewButtons', viewButtons);
  };

  setMenus(menus) {
    if (this.header) {
      this.header.setMenus(menus);
    }
  };

  _setMenus(menus) {
    this.updateKeyStrokes(menus, this.menus);
    this._setProperty('menus', menus);
  };

  _setKeyStrokes(keyStrokes) {
    this.updateKeyStrokes(keyStrokes, this.keyStrokes);
    this._setProperty('keyStrokes', keyStrokes);
  };

  setNavigationHandleVisible(visible) {
    this.setProperty('navigationHandleVisible', visible);
  };

  _renderNavigationHandleVisible() {
    this.$container.toggleClass('has-navigation-handle', this.navigationHandleVisible);
  };

  setNavigationVisible(visible) {
    this.setProperty('navigationVisible', visible);
    this.updateSplitterVisibility();
  };

  setBenchVisible(visible) {
    this.setProperty('benchVisible', visible);
    this.updateSplitterVisibility();
  };

  setHeaderVisible(visible) {
    this.setProperty('headerVisible', visible);
  };

  _setBenchLayoutData(layoutData) {
    layoutData = BenchColumnLayoutData.ensure(layoutData);
    this._setProperty('benchLayoutData', layoutData);
  };

  outlineDisplayStyle() {
    if (this.outline) {
      return this.outline.displayStyle;
    }
  };

  shrinkNavigation() {
    if (this.outline.toggleBreadcrumbStyleEnabled && this.navigationVisible &&
      this.outlineDisplayStyle() === TreeDisplayStyle.DEFAULT) {
      this.outline.setDisplayStyle(TreeDisplayStyle.BREADCRUMB);
    } else {
      this.setNavigationVisible(false);
    }
  };

  enlargeNavigation() {
    if (this.navigationVisible && this.outlineDisplayStyle() === TreeDisplayStyle.BREADCRUMB) {
      this.outline.setDisplayStyle(TreeDisplayStyle.DEFAULT);
    } else {
      this.setNavigationVisible(true);
      // Layout immediately to have view tabs positioned correctly before animation starts
      this.validateLayoutTree();
    }
  };

  switchToBench() {
    this.setHeaderVisible(true);
    this.setBenchVisible(true);
    this.setNavigationVisible(false);
  };

  switchToNavigation() {
    this.setNavigationVisible(true);
    this.setHeaderVisible(false);
    this.setBenchVisible(false);
  };

  revalidateHeaderLayout() {
    if (this.header) {
      this.header.revalidateLayout();
    }
  };

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
  };

  goOnline() {
    this._removeOfflineNotification();
  };

  _removeOfflineNotification() {
    if (this._offlineNotification) {
      setTimeout(this.removeNotification.bind(this, this._offlineNotification), 3000);
      this._offlineNotification = null;
    }
  };

  addNotification(notification) {
    if (!notification) {
      return;
    }
    this.notifications.push(notification);
    if (this.rendered) {
      this._renderNotification(notification);
    }
  };

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
      notification.one('remove', function() {
        this.removeNotification(notification);
      }.bind(this));
    }
  };

  _renderNotifications() {
    this.notifications.forEach(function(notification) {
      this._renderNotification(notification);
    }.bind(this));
  };

  /**
   * Removes the given notification.
   * @param notification Either an instance of scout.DesktopNavigation or a String containing an ID of a notification instance.
   */
  removeNotification(notification) {
    if (typeof notification === 'string') {
      var notificationId = notification;
      notification = arrays.find(this.notifications, function(n) {
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
    if (this.$notifications) {
      notification.fadeOut();
      notification.one('remove', this._onNotificationRemove.bind(this, notification));
    }
  };

  /**
   * Destroys every popup which is a descendant of the given widget.
   */
  destroyPopupsFor(widget) {
    this.$container.children('.popup').each(function(i, elem) {
      var $popup = $(elem),
        popup = scout.widget($popup);

      if (widget.has(popup)) {
        popup.destroy();
      }
    });
  };

  openUri(uri, action) {
    if (!this.rendered) {
      this._postRenderActions.push(this.openUri.bind(this, uri, action));
      return;
    }
    this.openUriHandler.openUri(uri, action);
  };

  bringOutlineToFront() {
    if (!this.rendered) {
      this._postRenderActions.push(this.bringOutlineToFront.bind(this));
      return;
    }

    if (!this.inBackground || this.displayStyle === DisplayStyle.BENCH) {
      return;
    }

    this.inBackground = false;
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
  };

  sendOutlineToBack() {
    if (this.inBackground) {
      return;
    }
    this.inBackground = true;
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
  };

  /**
   * === Method required for objects that act as 'displayParent' ===
   *
   * Returns 'true' if the Desktop is currently accessible to the user.
   */
  inFront() {
    return true; // Desktop is always available to the user.
  };

  /**
   * === Method required for objects that act as 'displayParent' ===
   *
   * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box, file-chooser or wait-dialog is showed with the Desktop as its 'displayParent'.
   */
  _glassPaneTargets(element) {
    // Do not return $container, because this is the parent of all forms and message boxes. Otherwise, no form could gain focus, even the form requested desktop modality.
    var $glassPaneTargets = this.$container
      .children()
      .not('.splitter') // exclude splitter to be locked
      .not('.desktop-notifications') // exclude notification box like 'connection interrupted' to be locked
      .not('.overlay-separator'); // exclude overlay separator (marker element)

    if (element && element.$container) {
      $glassPaneTargets = $glassPaneTargets.not(element.$container);
    }

    // Exclude all child elements of the given widget
    // Use case: element is a popup and has tooltip open. The tooltip is displayed in the desktop and considered as glass pane target by the selector above
    $glassPaneTargets = $glassPaneTargets.filter(function(i, targetElem) {
      var target = scout.widget(targetElem);
      return !element.has(target);
    });

    var glassPaneTargets;
    if (element instanceof Form && element.displayHint === DisplayHint.VIEW) {
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
  };

  /**
   * This 'deferred' object is used because popup windows are not immediately usable when they're opened.
   * That's why we must render the glass-pane of a popup window later. Which means, at the point in time
   * when its $container is created and ready for usage. To avoid race conditions we must also wait until
   * the glass pane renderer is ready. Only when both conditions are fullfilled, we can render the glass
   * pane.
   */
  _deferredGlassPaneTarget(popupWindow) {
    var deferred = new DeferredGlassPaneTarget();
    popupWindow.one('init', function() {
      deferred.ready([popupWindow.$container]);
    });
    return deferred;
  };

  _getBenchGlassPaneTargetsForView(view) {
    var $glassPanes = [];

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
  };

  _getTabGlassPaneTargetsForView(view, tabBox) {
    var $glassPanes = [];
    if (tabBox && tabBox.tabArea) {
      tabBox.tabArea.tabs.forEach(function(tab) {
        if (tab.view !== view) {
          $glassPanes.push(tab.$container);
          // Workaround for javascript not being able to prevent hover event propagation:
          // In case of tabs, the hover selector is defined on the element that is the direct parent
          // of the glass pane. Under these circumstances, the hover style isn't be prevented by the glass pane.
          tab.$container.addClass('no-hover');
        }
      });
    }
    return $glassPanes;
  };

  _pushPopupWindowGlassPaneTargets(glassPaneTargets, element) {
    this.formController._popupWindows.forEach(function(popupWindow) {
      if (element === popupWindow.form) {
        // Don't block form itself
        return;
      }
      glassPaneTargets.push(popupWindow.initialized ?
        popupWindow.$container[0] : this._deferredGlassPaneTarget(popupWindow));
    }, this);
  };

  showForm(form, position) {
    var displayParent = form.displayParent || this;
    form.setDisplayParent(displayParent);

    this._setFormActivated(form);
    // register listener to recover active form when child dialog is removed
    displayParent.formController.registerAndRender(form, position, true);
  };

  hideForm(form) {
    if (!form.displayParent) {
      // showForm has probably never been called -> nothing to do here
      // May happen if form.close() is called immediately after form.open() without waiting for the open promise to resolve
      // Hint: it is not possible to check whether the form is rendered and then return (which would be the obvious thing to do).
      // Reason: Forms in popup windows are removed before getting closed, see DesktopFormController._onPopupWindowUnload
      return;
    }

    if (this.displayStyle === DisplayStyle.COMPACT && form.isView() && this.benchVisible) {
      var openViews = this.bench.getViews().slice();
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
  };

  activateForm(form) {
    var displayParent = form.displayParent || this;
    displayParent.formController.activateForm(form);
    this._setFormActivated(form);

    // If the form has a modal child dialog, this dialog needs to be activated as well.
    form.dialogs.forEach(function(dialog) {
      if (dialog.modal) {
        this.activateForm(dialog);
      }
    }, this);
  };

  _setOutlineActivated() {
    this._setFormActivated();
    if (this.outline) {
      this.outline.activateCurrentPage();
    }
  };

  _setFormActivated(form) {
    // If desktop is in rendering process the can not set a new active form. instead the active form from the model is set selected.
    if (!this.rendered || this.initialFormRendering) {
      return;
    }
    if (this.activeForm === form) {
      return;
    }

    this.activeForm = form;

    this.triggerFormActivate(form);
  };

  triggerFormActivate(form) {
    this.trigger('formActivate', {
      form: form
    });
  };

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
  };

  onResize(event) {
    this.revalidateLayoutTree();
  };

  onPopstate(event) {
    var historyState = event.originalEvent.state;
    if (historyState && historyState.deepLinkPath) {
      this.trigger('historyEntryActivate', historyState);
    }
  };

  _onSplitterMove(event) {
    // disallow a position greater than 50%
    this.resizing = true;
    var max = Math.floor(this.$container.outerWidth(true) / 2);
    if (event.position > max) {
      event.setPosition(max);
    }
  };

  _onSplitterPositionChange(event) {
    // No need to revalidate while layouting (desktop layout sets the splitter position and would trigger a relayout)
    if (!this.htmlComp.layouting) {
      this.revalidateLayout();
    }
  };

  _onSplitterMoveEnd(event) {
    var splitterPosition = event.position;

    // Store size
    if (this.cacheSplitterPosition) {
      this._storeCachedSplitterPosition(this.splitter.position);
    }

    // Check if splitter is smaller than min size
    if (splitterPosition < BREADCRUMB_STYLE_WIDTH) {
      // Set width of navigation to BREADCRUMB_STYLE_WIDTH, using an animation.
      // While animating, update the desktop layout.
      // At the end of the animation, update the desktop layout, and store the splitter position.
      this.navigation.$container.animate({
        width: BREADCRUMB_STYLE_WIDTH
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
          this._storeCachedSplitterPosition(this.splitter.position);
          this.revalidateLayout();
          this.resizing = false;
        }.bind(this)
      });
    } else {
      this.resizing = false;
    }
  };

  _loadCachedSplitterPosition() {
    return null;
    /*scout.webstorage.getItem(sessionStorage, 'scout:desktopSplitterPosition') ||
                scout.webstorage.getItem(localStorage, 'scout:desktopSplitterPosition:' + window.location.pathname);*/
  };

  _storeCachedSplitterPosition(splitterPosition) {
    /*
            scout.webstorage.setItem(sessionStorage, 'scout:desktopSplitterPosition', splitterPosition);
            scout.webstorage.setItem(localStorage, 'scout:desktopSplitterPosition:' + window.location.pathname, splitterPosition);*/
  };

  _onNotificationRemove(notification) {
    if (this.notifications.length === 0 && this.$notifications) {
      this.$notifications.remove();
      this.$notifications = null;
    }
  };

  onReconnecting() {
    if (!this.offline) {
      return;
    }
    this._offlineNotification.reconnect();
  };

  onReconnectingSucceeded() {
    if (!this.offline) {
      return;
    }
    this.offline = false;
    this._offlineNotification.reconnectSucceeded();
    this._removeOfflineNotification();
  };

  onReconnectingFailed() {
    if (!this.offline) {
      return;
    }
    this._offlineNotification.reconnectFailed();
  };

  dataChange(dataType) {
    this.events.trigger('dataChange', dataType);
  };

  _activeTheme() {
    return /*scout.cookies.get('scout.ui.theme') || */ 'default';
  };

  _initTheme() {
    var theme = this.theme;
    if (theme === null) {
      theme = this._activeTheme();
    }
    this.setTheme(theme);
  };

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
  };

  _switchTheme(theme) {
    // Add a persistent cookie which expires in 30 days
    cookies.set('scout.ui.theme', theme, 30 * 24 * 3600);

    // Reload page in order to download the CSS files for the new theme
    // Don't remove body but make it invisible, otherwise JS exceptions might be thrown if body is removed while an action executed
    $('body').setVisible(false);
    scout.reloadPage({
      clearBody: false
    });
  };

}

export const DisplayStyle = Object.freeze({
  DEFAULT: 'default',
  BENCH: 'bench',
  COMPACT: 'compact'
});

export const UriAction = Object.freeze({
  DOWNLOAD: 'download',
  OPEN: 'open',
  NEW_WINDOW: 'newWindow',
  POPUP_WINDOW: 'popupWindow',
  SAME_WINDOW: 'sameWindow'
});
