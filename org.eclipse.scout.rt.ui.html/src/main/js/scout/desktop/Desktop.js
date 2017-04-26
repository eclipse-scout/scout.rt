/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Desktop = function() {
  scout.Desktop.parent.call(this);

  this.desktopStyle = scout.Desktop.DisplayStyle.DEFAULT;
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
  this.navigation;
  this.header;
  this.bench;
  this.splitter;
  this.formController;
  this.messageBoxController;
  this.fileChooserController;
  this.initialFormRendering = false;
  this.offline = false;
  this.notifications = [];
  this.inBackground = false;
  this.geolocationServiceAvailable = scout.device.supportsGeolocation();
  this.openUriHandler;

  this._addAdapterProperties(['activeForm', 'viewButtons', 'menus', 'views', 'selectedViewTabs', 'dialogs', 'outline', 'messageBoxes', 'notifications', 'fileChoosers', 'addOns', 'keyStrokes']);

  // event listeners
  this._benchActiveViewChangedHandler = this._onBenchActivateViewChanged.bind(this);
};
scout.inherits(scout.Desktop, scout.Widget);

scout.Desktop.DisplayStyle = {
  DEFAULT: 'default',
  BENCH: 'bench',
  COMPACT: 'compact'
};

scout.Desktop.UriAction = {
  DOWNLOAD: 'download',
  OPEN: 'open',
  NEW_WINDOW: 'newWindow',
  SAME_WINDOW: 'sameWindow'
};

scout.Desktop.prototype._init = function(model) {
  scout.Desktop.parent.prototype._init.call(this, model);
  this.formController = scout.create('DesktopFormController', {
    displayParent: this,
    session: this.session
  });

  this.messageBoxController = new scout.MessageBoxController(this, this.session);
  this.fileChooserController = new scout.FileChooserController(this, this.session);
  this._resizeHandler = this.onResize.bind(this);
  this._popstateHandler = this.onPopstate.bind(this);
  this.updateSplitterVisibility();
  this.resolveTextKeys(['title']);
  this._setViews(this.views);
  this._setViewButtons(this.viewButtons);
  this._setMenus(this.menus);
  this._setBenchLayoutData(this.benchLayoutData);
  this.openUriHandler = scout.create('OpenUriHandler', {
    session: this.session
  });

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
scout.Desktop.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.Desktop.prototype._initKeyStrokeContext = function() {
  scout.Desktop.parent.prototype._initKeyStrokeContext.call(this);

  // Keystroke on the top-level DOM element which works as a catch-all when the busy indicator is active
  this.keyStrokeContext.registerKeyStroke(new scout.DesktopKeyStroke(this.session));
  this.keyStrokeContext.registerKeyStroke(new scout.DisableBrowserTabSwitchingKeyStroke(this));
};

scout.Desktop.prototype._onBenchActivateViewChanged = function(event) {
  if (this.initialFormRendering) {
    return;
  }
  var view = event.view;
  if (view instanceof scout.Form && this.bench.outlineContent !== view && !view.detailForm) {
    // Notify model that this form is active (only for regular views, not detail forms)
    this._setFormActivated(view);
  }
};

scout.Desktop.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.addClass('desktop');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
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
  this._renderDisplayStyle();
  this._renderNavigationHandleVisible();
  this.addOns.forEach(function(addOn) {
    addOn.render(this.$container);
  }, this);

  this.$container.window()
    .on('resize', this._resizeHandler)
    .on('popstate', this._popstateHandler);

  // prevent general drag and drop, dropping a file anywhere in the application must not open this file in browser
  this._setupDragAndDrop();

  this._disableContextMenu();
};

scout.Desktop.prototype._remove = function() {
  this.formController.remove();
  this.messageBoxController.remove();
  this.fileChooserController.remove();
  this.$container.window()
    .off('resize', this._resizeHandler)
    .off('popstate', this._popstateHandler);
  scout.Desktop.parent.prototype._remove.call(this);
};

scout.Desktop.prototype._postRender = function() {
  scout.Desktop.parent.prototype._postRender.call(this);

  // Render attached forms, message boxes and file choosers.
  this.initialFormRendering = true;
  this._renderDisplayChildsOfOutline();
  this.formController.render();
  this.messageBoxController.render();
  this.fileChooserController.render();
  this._renderNotifications();

  this.initialFormRendering = false;
};

scout.Desktop.prototype._renderNotifications = function() {
  this.notifications.forEach(function(notification) {
    this._renderNotification(notification);
  }.bind(this));
};

scout.Desktop.prototype._renderDisplayStyle = function() {
  var DisplayStyle = scout.Desktop.DisplayStyle,
    isCompact = this.displayStyle === DisplayStyle.COMPACT;

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

  this.invalidateLayoutTree();
};

scout.Desktop.prototype._createLayout = function() {
  return new scout.DesktopLayout(this);
};

/**
 * Displays attached forms, message boxes and file choosers.
 * Outline does not need to be rendered to show the child elements, it needs to be active (necessary if navigation is invisible)
 */
scout.Desktop.prototype._renderDisplayChildsOfOutline = function() {
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

scout.Desktop.prototype._removeDisplayChildsOfOutline = function() {
  if (!this.outline) {
    return;
  }
  this.outline.formController.remove();
  this.outline.messageBoxController.remove();
  this.outline.fileChooserController.remove();
};

scout.Desktop.prototype._renderTitle = function() {
  var title = this.title;
  if (title === undefined || title === null) {
    return;
  }
  var $scoutDivs = $('div.scout');
  if ($scoutDivs.length <= 1) { // only set document title in non-portlet case
    $scoutDivs.document(true).title = title;
  }
};

scout.Desktop.prototype._renderActiveForm = function() {
  // NOP -> is handled in _setFormActivated when ui changes active form or if model changes form in _onFormShow/_onFormActivate
};

scout.Desktop.prototype._renderBench = function() {
  if (this.bench) {
    return;
  }
  this.bench = scout.create('DesktopBench', {
    parent: this,
    animateRemoval: true,
    headerTabArea: this.header ? this.header.tabArea : undefined,
    outlineContentVisible: this.displayStyle !== scout.Desktop.DisplayStyle.COMPACT
  });
  this.bench.on('viewActivated', this._benchActiveViewChangedHandler);
  this.bench.render(this.$container);
  this.bench.$container.insertBefore(this.$overlaySeparator);
  this.invalidateLayoutTree();
};

scout.Desktop.prototype._removeBench = function() {
  if (!this.bench) {
    return;
  }
  this.bench.off('viewActivated', this._benchActiveViewChangedHandler);
  this.bench.on('destroy', function() {
    this.bench = null;
    this.invalidateLayoutTree();
  }.bind(this));
  this.bench.destroy();
};

scout.Desktop.prototype._renderBenchVisible = function() {
  this.animateLayoutChange = this.rendered;
  if (this.benchVisible) {
    this._renderBench();
    this._renderInBackground();
  } else {
    this._removeBench();
  }
};

scout.Desktop.prototype._renderNavigation = function() {
  if (this.navigation) {
    return;
  }
  this.navigation = scout.create('DesktopNavigation', {
    parent: this,
    outline: this.outline,
    toolBoxVisible: this.displayStyle === scout.Desktop.DisplayStyle.COMPACT,
    layoutData: {
      fullWidth: this.displayStyle === scout.Desktop.DisplayStyle.COMPACT
    }
  });
  this.navigation.render(this.$container);
  this.navigation.$container.prependTo(this.$container);
  this.invalidateLayoutTree();
};

scout.Desktop.prototype._removeNavigation = function() {
  if (!this.navigation) {
    return;
  }
  this.navigation.destroy();
  this.navigation = null;
  this.invalidateLayoutTree();
};

scout.Desktop.prototype._renderNavigationVisible = function() {
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

scout.Desktop.prototype._renderHeader = function() {
  if (this.header) {
    return;
  }
  this.header = scout.create('DesktopHeader', {
    parent: this,
    animateRemoval: this.displayStyle === scout.Desktop.DisplayStyle.COMPACT,
    toolBoxVisible: this.displayStyle !== scout.Desktop.DisplayStyle.COMPACT
  });
  this.header.render(this.$container);
  this.header.$container.insertBefore(this.$overlaySeparator);
  this.invalidateLayoutTree();
};

scout.Desktop.prototype._removeHeader = function() {
  if (!this.header) {
    return;
  }
  this.header.on('destroy', function() {
    this.invalidateLayoutTree();
    this.header = null;
  }.bind(this));
  this.header.destroy();
};

scout.Desktop.prototype._renderHeaderVisible = function() {
  if (this.headerVisible) {
    this._renderHeader();
  } else {
    this._removeHeader();
  }
};

scout.Desktop.prototype._renderLogoUrl = function() {
  if (this.header) {
    this.header.setLogoUrl(this.logoUrl);
  }
};

scout.Desktop.prototype._renderSplitterVisible = function() {
  if (this.splitterVisible) {
    this._renderSplitter();
  } else {
    this._removeSplitter();
  }
};

scout.Desktop.prototype._renderSplitter = function() {
  if (this.splitter || !this.navigation) {
    return;
  }
  this.splitter = scout.create('Splitter', {
    parent: this,
    $anchor: this.navigation.$container,
    $root: this.$container
  });
  this.splitter.render(this.$container);
  this.splitter.$container.insertBefore(this.$overlaySeparator);
  this.splitter.on('move', this._onSplitterMove.bind(this));
  this.splitter.on('moveEnd', this._onSplitterMoveEnd.bind(this));
  this.splitter.on('positionChanged', this._onSplitterPositionChanged.bind(this));
  this.updateSplitterPosition();
};

scout.Desktop.prototype._removeSplitter = function() {
  if (!this.splitter) {
    return;
  }
  this.splitter.destroy();
  this.splitter = null;
};

scout.Desktop.prototype._renderInBackground = function() {
  if (this.bench) {
    this.bench.$container.toggleClass('drop-shadow', this.inBackground);
  }
};

scout.Desktop.prototype._renderBrowserHistoryEntry = function() {
  if (!scout.device.supportsHistoryApi()) {
    return;
  }
  var myWindow = this.$container.window(true),
    history = this.browserHistoryEntry;
  myWindow.history.pushState({
    deepLinkPath: history.deepLinkPath
  }, history.title, history.path);
};

scout.Desktop.prototype._setupDragAndDrop = function() {
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

scout.Desktop.prototype.updateSplitterVisibility = function() {
  // Splitter should only be visible if navigation and bench are visible, but never in compact mode (to prevent unnecessary splitter rendering)
  this.setSplitterVisible(this.navigationVisible && this.benchVisible && this.displayStyle !== scout.Desktop.DisplayStyle.COMPACT);
};

scout.Desktop.prototype.setSplitterVisible = function(visible) {
  this.setProperty('splitterVisible', visible);
};

scout.Desktop.prototype.updateSplitterPosition = function() {
  if (!this.splitter) {
    return;
  }
  // TODO [7.0] awe: (user-prefs) Use user-preferences instead of sessionStorage
  var storedSplitterPosition = this.cacheSplitterPosition && scout.webstorage.getItem(sessionStorage, 'scout:desktopSplitterPosition');
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

scout.Desktop.prototype._disableContextMenu = function() {
  // Switch off browser's default context menu for the entire scout desktop (except input fields)
  this.$container.on('contextmenu', function(event) {
    if (event.target.nodeName !== 'INPUT' && event.target.nodeName !== 'TEXTAREA' && !event.target.isContentEditable) {
      event.preventDefault();
    }
  });
};

scout.Desktop.prototype.setOutline = function(outline) {
  if (this.outline === outline) {
    return;
  }
  try {
    if (this.bench) {
      this.bench.setChanging(true);
    }
    if (this.rendered) {
      this._removeDisplayChildsOfOutline();
    }

    this.outline = outline;
    this._setOutlineActivated();
    if (this.navigation) {
      this.navigation.setOutline(this.outline);
    }
    // call render after triggering event so glasspane rendering taking place can refer to the current outline content
    this.trigger('outlineChanged');

    if (this.rendered) {
      this._renderDisplayChildsOfOutline();
      this._renderDisplayStyle();
    }
  } finally {
    if (this.bench) {
      this.bench.setChanging(false);
    }
  }
};

scout.Desktop.prototype._setViews = function(views) {
  if (views) {
    views.forEach(function(view) {
      view.setDisplayParent(this);
    }.bind(this));
  }
  this._setProperty('views', views);
};

scout.Desktop.prototype._setViewButtons = function(viewButtons) {
  this.updateKeyStrokes(viewButtons, this.viewButtons);
  this._setProperty('viewButtons', viewButtons);
};

scout.Desktop.prototype._setMenus = function(menus) {
  this.updateKeyStrokes(menus, this.menus);
  this._setProperty('menus', menus);
};

scout.Desktop.prototype.setMenus = function(menus) {
  if (this.header) {
    this.header.setMenus(menus);
  }
};

scout.Desktop.prototype.setNavigationHandleVisible = function(visible) {
  this.setProperty('navigationHandleVisible', visible);
};

scout.Desktop.prototype._renderNavigationHandleVisible = function() {
  this.$container.toggleClass('has-navigation-handle', this.navigationHandleVisible);
};

scout.Desktop.prototype.setNavigationVisible = function(visible) {
  this.setProperty('navigationVisible', visible);
  this.updateSplitterVisibility();
};

scout.Desktop.prototype.setBenchVisible = function(visible) {
  this.setProperty('benchVisible', visible);
  this.updateSplitterVisibility();
};

scout.Desktop.prototype.setHeaderVisible = function(visible) {
  this.setProperty('headerVisible', visible);
};

scout.Desktop.prototype._setBenchLayoutData = function(layoutData) {
  layoutData = scout.BenchColumnLayoutData.ensure(layoutData);
  this._setProperty('benchLayoutData', layoutData);
};

scout.Desktop.prototype.outlineDisplayStyle = function() {
  if (this.outline) {
    return this.outline.displayStyle;
  }
};

scout.Desktop.prototype.shrinkNavigation = function() {
  if (this.outline.toggleBreadcrumbStyleEnabled && this.navigationVisible &&
      this.outlineDisplayStyle() === scout.Tree.DisplayStyle.DEFAULT) {
    this.outline.setDisplayStyle(scout.Tree.DisplayStyle.BREADCRUMB);
  } else {
    this.setNavigationVisible(false);
  }
};

scout.Desktop.prototype.enlargeNavigation = function() {
  if (this.navigationVisible && this.outlineDisplayStyle() === scout.Tree.DisplayStyle.BREADCRUMB) {
    this.outline.setDisplayStyle(scout.Tree.DisplayStyle.DEFAULT);
  } else {
    this.setNavigationVisible(true);
    // Layout immediately to have view tabs positioned correctly before animation starts
    this.validateLayoutTree();
  }
};

scout.Desktop.prototype.switchToBench = function() {
  this.setHeaderVisible(true);
  this.setBenchVisible(true);
  this.setNavigationVisible(false);
};

scout.Desktop.prototype.switchToNavigation = function() {
  this.setNavigationVisible(true);
  this.setHeaderVisible(false);
  this.setBenchVisible(false);
};

scout.Desktop.prototype.revalidateHeaderLayout = function() {
  if (this.header) {
    this.header.revalidateLayout();
  }
};

scout.Desktop.prototype.goOffline = function() {
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

scout.Desktop.prototype.goOnline = function() {
  this._removeOfflineNotification();
};

scout.Desktop.prototype._removeOfflineNotification = function() {
  if (this._offlineNotification) {
    setTimeout(this.removeNotification.bind(this, this._offlineNotification), 3000);
    this._offlineNotification = null;
  }
};

scout.Desktop.prototype.addNotification = function(notification) {
  if (!notification) {
    return;
  }
  this.notifications.push(notification);
  this._renderNotification(notification);
};

scout.Desktop.prototype._renderNotification = function(notification) {
  if (!this.rendered) {
    return;
  }

  if (this.$notifications) {
    // Bring to front
    this.$notifications.appendTo(this.$container);
  } else {
    this.$notifications = this.$container.appendDiv('desktop-notifications');
  }
  notification.fadeIn(this.$notifications);
  if (notification.duration > 0) {
    notification.removeTimeout = setTimeout(notification.hide.bind(notification), notification.duration);
  }
};

/**
 * Removes the given notification.
 * @param notification Either an instance of scout.DesktopNavigation or a String containing an ID of a notification instance.
 */
scout.Desktop.prototype.removeNotification = function(notification) {
  if (typeof notification === 'string') {
    var notificationId = notification;
    notification = scout.arrays.find(this.notifications, function(n) {
      return notificationId === n.id;
    });
  }
  if (!notification) {
    return;
  }
  if (notification.removeTimeout) {
    clearTimeout(notification.removeTimeout);
  }
  scout.arrays.remove(this.notifications, notification);
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
scout.Desktop.prototype.destroyPopupsFor = function(widget) {
  this.$container.children('.popup').each(function(i, elem) {
    var $popup = $(elem),
      popup = scout.Widget.getWidgetFor($popup);

    if (widget.has(popup)) {
      popup.destroy();
    }
  });
};

scout.Desktop.prototype.openUri = function(uri, action) {
  if (!this.rendered) {
    this._postRenderActions.push(this.openUri.bind(this, uri, action));
    return;
  }
  this.openUriHandler.openUri(uri, action);
};

scout.Desktop.prototype.bringOutlineToFront = function() {
  if (!this.rendered) {
    this._postRenderActions.push(this.bringOutlineToFront.bind(this));
    return;
  }

  if (!this.inBackground || this.displayStyle === scout.Desktop.DisplayStyle.BENCH) {
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

scout.Desktop.prototype.sendOutlineToBack = function() {
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
scout.Desktop.prototype.inFront = function() {
  return true; // Desktop is always available to the user.
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box, file-chooser or wait-dialog is showed with the Desktop as its 'displayParent'.
 */
scout.Desktop.prototype.glassPaneTargets = function() {
  // Do not return $container, because this is the parent of all forms and message boxes. Otherwise, no form could gain focus, even the form requested desktop modality.
  var glassPaneTargets = $.makeArray(this.$container
    .children()
    .not('.splitter') // exclude splitter to be locked
    .not('.desktop-notifications') // exclude notification box like 'connection interrupted' to be locked
    .not('.overlay-separator') // exclude overlay separator (marker element)
  );

  // When a popup-window is opened its container must also be added to the result
  this._pushPopupWindowGlassPaneTargets(glassPaneTargets);

  return glassPaneTargets;
};

/**
 * This 'deferred' object is used because popup windows are not immediately usable when they're opened.
 * That's why we must render the glass-pane of a popup window later. Which means, at the point in time
 * when its $container is created and ready for usage. To avoid race conditions we must also wait until
 * the glass pane renderer is ready. Only when both conditions are fullfilled, we can render the glass
 * pane.
 */
scout.Desktop.prototype._deferredGlassPaneTarget = function(popupWindow) {
  var deferred = new scout.DeferredGlassPaneTarget();
  popupWindow.one('init', function() {
    deferred.ready([popupWindow.$container]);
  });
  return deferred;
};

scout.Desktop.prototype._pushPopupWindowGlassPaneTargets = function(glassPaneTargets) {
  this.formController._popupWindows.forEach(function(popupWindow) {
    glassPaneTargets.push(popupWindow.initialized ?
      popupWindow.$container[0] : this._deferredGlassPaneTarget(popupWindow));
  }, this);
};

scout.Desktop.prototype.showForm = function(form, displayParent, position) {
  displayParent = displayParent || this;

  this._setFormActivated(form);
  // register listener to recover active form when child dialog is removed
  displayParent.formController.registerAndRender(form, position, true);
};

scout.Desktop.prototype.hideForm = function(form) {
  if (this.displayStyle === scout.Desktop.DisplayStyle.COMPACT && form.isView() && this.benchVisible) {
    var openViews = this.bench.getViews().slice();
    scout.arrays.remove(openViews, form);
    if (openViews.length === 0) {
      // Hide bench and show navigation if this is the last view to be hidden
      this.switchToNavigation();
    }
  }
  form.displayParent.formController.unregisterAndRemove(form);
};

scout.Desktop.prototype.activateForm = function(form) {
  form.displayParent.formController.activateForm(form);
  this._setFormActivated(form);
};

scout.Desktop.prototype._setOutlineActivated = function() {
  this._setFormActivated();
  if (this.outline) {
    this.outline.activateCurrentPage();
  }
};

scout.Desktop.prototype._setFormActivated = function(form) {
  // If desktop is in rendering process the can not set a new active form. instead the active form from the model is set selected.
  if (!this.rendered || this.initialFormRendering) {
    return;
  }
  if (this.activeForm === form) {
    return;
  }

  this.activeForm = form;

  this.triggerFormActivated(form);
};

scout.Desktop.prototype.triggerFormActivated = function(form) {
  this.trigger('formActivated', {
    form: form
  });
};

/**
 * Called when the animation triggered by animationLayoutChange is complete (e.g. navigation or bench got visible/invisible)
 */
scout.Desktop.prototype.onLayoutAnimationComplete = function() {
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

scout.Desktop.prototype.onResize = function(event) {
  this.revalidateLayout();
};

scout.Desktop.prototype.onPopstate = function(event) {
  var historyState = event.originalEvent.state;
  if (historyState && historyState.deepLinkPath) {
    this.trigger('historyEntryActivated', historyState);
  }
};

scout.Desktop.prototype._onSplitterMove = function(event) {
  // disallow wider than 50%
  this.resizing = true;
  var max = Math.floor(this.$container.outerWidth(true) / 2);
  if (event.position > max) {
    event.setPosition(max);
  }
};

scout.Desktop.prototype._onSplitterPositionChanged = function(event) {
  this.revalidateLayout();
};

scout.Desktop.prototype._onSplitterMoveEnd = function(event) {
  var splitterPosition = event.position;

  // Store size
  if (this.cacheSplitterPosition) {
    storeSplitterPosition(this.splitter.position);
  }

  // Check if splitter is smaller than min size
  if (splitterPosition < scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH) {
    // Set width of navigation to BREADCRUMB_STYLE_WIDTH, using an animation.
    // While animating, update the desktop layout.
    // At the end of the animation, update the desktop layout, and store the splitter position.
    this.navigation.$container.animate({
      width: scout.DesktopNavigation.BREADCRUMB_STYLE_WIDTH
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
        storeSplitterPosition(this.splitter.position);
        this.revalidateLayout();
        this.resizing = false;
      }.bind(this)
    });
  } else {
    this.resizing = false;
  }

  // ----- Helper functions -----

  function storeSplitterPosition(splitterPosition) {
    scout.webstorage.setItem(sessionStorage, 'scout:desktopSplitterPosition', splitterPosition);
  }
};

scout.Desktop.prototype._onNotificationRemove = function(notification) {
  if (this.notifications.length === 0 && this.$notifications) {
    this.$notifications.remove();
    this.$notifications = null;
  }
};

scout.Desktop.prototype.onReconnecting = function() {
  if (!this.offline) {
    return;
  }
  this._offlineNotification.reconnect();
};

scout.Desktop.prototype.onReconnectingSucceeded = function() {
  if (!this.offline) {
    return;
  }
  this.offline = false;
  this._offlineNotification.reconnectSucceeded();
  this._removeOfflineNotification();
};

scout.Desktop.prototype.onReconnectingFailed = function() {
  if (!this.offline) {
    return;
  }
  this._offlineNotification.reconnectFailed();
};

scout.Desktop.prototype.dataChange = function(dataType) {
  this.events.trigger('dataChange', dataType);
};
