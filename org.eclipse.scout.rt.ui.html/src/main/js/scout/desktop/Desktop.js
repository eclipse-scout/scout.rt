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
  this._addAdapterProperties(['viewButtons', 'menus', 'views', 'dialogs', 'outline', 'messageBoxes', 'fileChoosers', 'addOns', 'keyStrokes']);

  // event listeners
  this._benchActiveViewChangedHandler = this._onBenchActivateViewChanged.bind(this);
};
scout.inherits(scout.Desktop, scout.ModelAdapter);

scout.Desktop.DisplayStyle = {
  DEFAULT: 'default',
  BENCH: 'bench',
  COMPACT: 'compact'
};

scout.Desktop.prototype._init = function(model) {
  scout.Desktop.parent.prototype._init.call(this, model);
  this.formController = new scout.DesktopFormController(this, this.session);
  this.messageBoxController = new scout.MessageBoxController(this, this.session);
  this.fileChooserController = new scout.FileChooserController(this, this.session);
  this._resizeHandler = this.onResize.bind(this);
  this._popstateHandler = this.onPopstate.bind(this);
  this.updateSplitterVisibility();
  this._syncViewButtons(this.viewButtons);
  this._syncMenus(this.menus);
};

scout.Desktop.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.Desktop.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Keystroke on the top-level DOM element which works as a catch-all when the busy indicator is active
  keyStrokeContext.registerKeyStroke(new scout.DesktopKeyStroke(this.session));
  keyStrokeContext.registerKeyStroke(new scout.DesktopTabSelectKeyStroke(this));
  keyStrokeContext.registerKeyStroke(new scout.DisableBrowserTabSwitchingKeyStroke(this));
};

scout.Desktop.prototype._onChildAdapterCreation = function(propertyName, model) {
  if (propertyName === 'viewButtons') {
    model.desktop = this;
  } else if (propertyName === 'menus') {
    model.desktop = this;
  }
};

scout.Desktop.prototype._onBenchActivateViewChanged = function(event) {
  if (this.initialFormRendering) {
    return;
  }
  var view = event.view;
  if (this.bench.outlineContent !== view && !view.detailForm) {
    // Notify model that this form is active (only for regular views, not detail forms)
    this._setFormActivated(view);
  }
};

scout.Desktop.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.addClass('desktop');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopLayout(this));
  scout.inspector.applyInfo(this, this.$container);

  // Desktop elements are added before this separator, all overlays are opened after (dialogs, popups, tooltips etc.)
  this.$overlaySeparator = this.$container.appendDiv().setVisible(false);

  this._renderNavigationVisible();
  this._renderHeaderVisible();
  this._renderBenchVisible();
  this._renderTitle();
  this._renderLogoUrl();
  this._renderSplitterVisible();
  this._renderInBackground();
  this._renderDisplayStyle();
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
  // Render attached forms, message boxes and file choosers.
  this.initialFormRendering = true;
  this.formController.render();
  this.messageBoxController.render();
  this.fileChooserController.render();
  this._renderDisplayChildsOfOutline();

  this.initialFormRendering = false;
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
  this.bench.on('remove', function() {
    this.bench = null;
    this.invalidateLayoutTree();
  }.bind(this));
  this.bench.remove();
};

scout.Desktop.prototype._renderBenchVisible = function() {
  this.animateLayoutChange = this.rendered;
  if (this.benchVisible) {
    this._renderBench();
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
  this.navigation.remove();
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

scout.Desktop.prototype._renderNavigationHandleVisible = function() {
  // NOP
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
  this.header.on('remove', function() {
    this.invalidateLayoutTree();
    this.header = null;
  }.bind(this));
  this.header.remove();
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
  this.updateSplitterPosition();
};

scout.Desktop.prototype._removeSplitter = function() {
  if (!this.splitter) {
    return;
  }
  this.splitter.remove();
  this.splitter = null;
};

scout.Desktop.prototype._renderInBackground = function() {
  if (this.navigationVisible && this.benchVisible) {
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
  if (this.splitterVisible === visible) {
    return;
  }
  this._setProperty('splitterVisible', visible);
  if (this.rendered) {
    this._renderSplitterVisible();
  }
};

scout.Desktop.prototype.updateSplitterPosition = function() {
  if (!this.splitter) {
    return;
  }
  // FIXME awe: (user-prefs) Use user-preferences instead of sessionStorage
  var storedSplitterPosition = this.cacheSplitterPosition && sessionStorage.getItem('scout:desktopSplitterPosition');
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
};

scout.Desktop.prototype._syncViewButtons = function(viewButtons, oldViewButtons) {
  this.updateKeyStrokes(viewButtons, oldViewButtons);
  this.viewButtons = viewButtons;
};

scout.Desktop.prototype._syncMenus = function(menus, oldMenus) {
  this.updateKeyStrokes(menus, oldMenus);
  this.menus = menus;
};

scout.Desktop.prototype._syncNavigationVisible = function(visible) {
  this.setNavigationVisible(visible, false);
  return false;
};

scout.Desktop.prototype.setNavigationVisible = function(visible, notifyServer) {
  if (this.navigationVisible === visible) {
    return;
  }
  this._setProperty('navigationVisible', visible);
  notifyServer = scout.nvl(notifyServer, true);
  if (notifyServer) {
    this._sendProperty('navigationVisible');
  }
  if (this.rendered) {
    this._renderNavigationVisible();
  }
  this.updateSplitterVisibility();
};

scout.Desktop.prototype._syncBenchVisible = function(visible) {
  this.setBenchVisible(visible, false);
  return false;
};

scout.Desktop.prototype.setBenchVisible = function(visible, notifyServer) {
  if (this.benchVisible === visible) {
    return;
  }
  this._setProperty('benchVisible', visible);
  notifyServer = scout.nvl(notifyServer, true);
  if (notifyServer) {
    this._sendProperty('benchVisible');
  }
  if (this.rendered) {
    this._renderBenchVisible();
  }
  this.updateSplitterVisibility();
};

scout.Desktop.prototype.setHeaderVisible = function(visible, notifyServer) {
  if (this.headerVisible === visible) {
    return;
  }
  this._setProperty('headerVisible', visible);
  notifyServer = scout.nvl(notifyServer, true);
  if (notifyServer) {
    this._sendProperty('headerVisible');
  }
  if (this.rendered) {
    this._renderHeaderVisible();
  }
};

scout.Desktop.prototype.outlineDisplayStyle = function() {
  if (this.outline) {
    return this.outline.displayStyle;
  }
};

scout.Desktop.prototype.shrinkNavigation = function() {
  if (this.navigationVisible && this.outlineDisplayStyle() === scout.Tree.DisplayStyle.DEFAULT) {
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

scout.Desktop.prototype._goOffline = function() {
  if (this.offline) {
    return;
  }
  this.offline = true;
  this._offlineNotification = scout.create('DesktopNotification.Offline', {
    parent: this,
    closable: false,
    duration: scout.DesktopNotification.INFINITE,
    status: {
      message: this.session.text('ui.ConnectionInterrupted'),
      severity: scout.Status.Severity.ERROR
    }
  });
  this._offlineNotification.show();
};

scout.Desktop.prototype._goOnline = function() {
  if (!this._hideOfflineMessagePending) {
    this.hideOfflineMessage();
  }
};

scout.Desktop.prototype.hideOfflineMessage = function() {
  this._hideOfflineMessagePending = false;
  this.removeNotification(this._offlineNotification);
  this._offlineNotification = null;
};

scout.Desktop.prototype.addNotification = function(notification) {
  if (!notification) {
    return;
  }
  this.notifications.push(notification);
  if (this.$notifications) {
    // Bring to front
    this.$notifications.appendTo(this.$container);
  } else {
    this.$notifications = this.$container.appendDiv('notifications');
  }
  notification.fadeIn(this.$notifications);
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
  if (this.$notifications) {
    notification.fadeOut(this._onNotificationRemoved.bind(this, notification));
  } else {
    scout.arrays.remove(this.notifications, notification);
  }
};

/**
 * Removes every popup which is a descendant of the given widget.
 */
scout.Desktop.prototype.removePopupsFor = function(widget) {
  this.$container.children('.popup').each(function(i, elem) {
    var $popup = $(elem),
      popup = scout.Widget.getWidgetFor($popup);

    if (widget.has(popup)) {
      popup.remove();
    }
  });
};

scout.Desktop.prototype._openUriInIFrame = function(uri) {
  // Create a hidden iframe and set the URI as src attribute value
  var $iframe = this.session.$entryPoint.appendElement('<iframe>', 'download-frame')
    .attr('tabindex', -1)
    .attr('src', uri);

  // Remove the iframe again after 10s (should be enough to get the download started)
  setTimeout(function() {
    $iframe.remove();
  }, 10 * 1000);
};

scout.Desktop.prototype._openUriAsNewWindow = function(uri) {
  var popupBlockerHandler = new scout.PopupBlockerHandler(this.session),
    popup = popupBlockerHandler.openWindow(uri);

  if (!popup) {
    popupBlockerHandler.showNotification(uri);
  }
};

scout.Desktop.prototype.bringOutlineToFront = function() {
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
    .not('.notifications')); // exclude notification box like 'connection interrupted' to be locked

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
  popupWindow.one('initialized', function() {
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

scout.Desktop.prototype._showForm = function(form, displayParent, position, notifyServer) {
  this._setFormActivated(form, notifyServer);
  // register listener to recover active form when child dialog is removed
  displayParent.formController.registerAndRender(form, position, true);
};

scout.Desktop.prototype._hideForm = function(form) {
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

scout.Desktop.prototype._activateForm = function(form, notifyServer) {
  form.displayParent.formController.activateForm(form);
  this._setFormActivated(form, notifyServer);
};

scout.Desktop.prototype._setOutlineActivated = function() {
  this._setFormActivated();
};

scout.Desktop.prototype._setFormActivated = function(form, notifyServer) {
  // If desktop is in rendering process the can not set a new active for. instead the active form from the model is set selected.
  if (!this.rendered || this.initialFormRendering) {
    return;
  }

  if ((form && this.activeForm !== form.id) || (!form && this.activeForm)) {
    this.activeForm = form ? form.id : null;
    notifyServer = scout.nvl(notifyServer, true);
    if (notifyServer) {
      this._sendFormActivated(form);
    }
  }
};

scout.Desktop.prototype._sendFormActivated = function(form) {
  var eventData = {
    formId: form ? form.id : null
  };

  this._send('formActivated', eventData, 0, function(previous) {
    return this.type === previous.type;
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
    this._send('historyEntryActivated', historyState);
  }
};

scout.Desktop.prototype._onSplitterMove = function(event) {
  // disallow wider than 50%
  this.resizing = true;
  var max = Math.floor(this.$container.outerWidth(true) / 2);
  if (event.position > max) {
    event.position = max;
  }
  this.revalidateLayout();
};

scout.Desktop.prototype._onSplitterMoveEnd = function(event) {
  var splitterPosition = event.position;

  // Store size
  if (this.cacheSplitterPosition) {
    sessionStorage.setItem('scout:desktopSplitterPosition', splitterPosition);
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
        sessionStorage.setItem('scout:desktopSplitterPosition', this.splitter.position);
        this.revalidateLayout();
        this.resizing = false;
      }.bind(this)
    });
  } else {
    this.resizing = false;
  }
};

scout.Desktop.prototype._onFormShow = function(event) {
  var form,
    displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    form = this.session.getOrCreateModelAdapter(event.form, displayParent);
    this._showForm(form, displayParent, event.position, false);
  }
};

scout.Desktop.prototype._onFormHide = function(event) {
  var form,
    displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    form = this.session.getModelAdapter(event.form);
    this._hideForm(form);
  }
};

scout.Desktop.prototype._onFormActivate = function(event) {
  var form,
    displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    form = this.session.getOrCreateModelAdapter(event.form, displayParent);
    this._activateForm(form, false);
  }
};

scout.Desktop.prototype._onMessageBoxShow = function(event) {
  var messageBox,
    displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    messageBox = this.session.getOrCreateModelAdapter(event.messageBox, displayParent);
    displayParent.messageBoxController.registerAndRender(messageBox);
  }
};

scout.Desktop.prototype._onMessageBoxHide = function(event) {
  var messageBox,
    displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    messageBox = this.session.getModelAdapter(event.messageBox);
    displayParent.messageBoxController.unregisterAndRemove(messageBox);
  }
};

scout.Desktop.prototype._onFileChooserShow = function(event) {
  var fileChooser,
    displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    fileChooser = this.session.getOrCreateModelAdapter(event.fileChooser, displayParent);
    displayParent.fileChooserController.registerAndRender(fileChooser);
  }
};

scout.Desktop.prototype._onFileChooserHide = function(event) {
  var fileChooser,
    displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    fileChooser = this.session.getModelAdapter(event.fileChooser);
    displayParent.fileChooserController.unregisterAndRemove(fileChooser);
  }
};

scout.Desktop.prototype._onOpenUri = function(event) {
  $.log.debug('(Desktop#_onOpenUri) uri=' + event.uri + ' action=' + event.action);
  if (!event.uri) {
    return;
  }

  if (event.action === 'download') {
    if (scout.device.isIos()) {
      // The iframe trick does not work for ios
      // Since the file cannot be stored on the file system it will be shown in the browser if possible
      // -> create a new window to not replace the existing content.
      // Drawback: Popup-Blocker will show up
      this._openUriAsNewWindow(event.uri);
    } else {
      this._openUriInIFrame(event.uri);
    }
  } else if (event.action === 'open') {
    // Open in same window.
    // Don't call _openUriInIFrame here, if action is set to open, an url is expected to be opened in the same window
    // Additionally, some url types require to be opened in the same window like tel or mailto, at least on mobile devices
    window.location.href = event.uri;
  } else if (event.action === 'newWindow') {
    this._openUriAsNewWindow(event.uri);
  }
};

scout.Desktop.prototype._onOutlineChanged = function(event) {
  this.setOutline(this.session.getOrCreateModelAdapter(event.outline, this));
};

scout.Desktop.prototype._onOutlineContentActivate = function(event) {
  this.bringOutlineToFront();
};

scout.Desktop.prototype._onAddNotification = function(event) {
  scout.create('DesktopNotification', {
    parent: this,
    id: event.id,
    duration: event.duration,
    status: event.status,
    closable: event.closable
  }).show();
};

scout.Desktop.prototype._onRemoveNotification = function(event) {
  this.removeNotification(event.id);
};

scout.Desktop.prototype._onNotificationRemoved = function(notification) {
  scout.arrays.remove(this.notifications, notification);
  if (this.notifications.length === 0) {
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
  this._hideOfflineMessagePending = true;
  setTimeout(this.hideOfflineMessage.bind(this), 3000);
};

scout.Desktop.prototype.onReconnectingFailed = function() {
  if (!this.offline) {
    return;
  }
  this._offlineNotification.reconnectFailed();
};

scout.Desktop.prototype.onModelAction = function(event) {
  if (event.type === 'formShow') {
    this._onFormShow(event);
  } else if (event.type === 'formHide') {
    this._onFormHide(event);
  } else if (event.type === 'formActivate') {
    this._onFormActivate(event);
  } else if (event.type === 'messageBoxShow') {
    this._onMessageBoxShow(event);
  } else if (event.type === 'messageBoxHide') {
    this._onMessageBoxHide(event);
  } else if (event.type === 'fileChooserShow') {
    this._onFileChooserShow(event);
  } else if (event.type === 'fileChooserHide') {
    this._onFileChooserHide(event);
  } else if (event.type === 'openUri') {
    this._onOpenUri(event);
  } else if (event.type === 'outlineChanged') {
    this._onOutlineChanged(event);
  } else if (event.type === 'outlineContentActivate') {
    this._onOutlineContentActivate(event);
  } else if (event.type === 'addNotification') {
    this._onAddNotification(event);
  } else if (event.type === 'removeNotification') {
    this._onRemoveNotification(event);
  } else {
    scout.Desktop.parent.prototype.onModelAction.call(this, event);
  }
};
