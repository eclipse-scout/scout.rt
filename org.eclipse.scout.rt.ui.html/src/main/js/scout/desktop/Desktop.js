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

  /**
   * FIXME dwi: (activeForm): selected tool form action wird nun auch als 'activeForm' verwendet (siehe TableKeystrokeContext.js)
   * Wahrscheinlich müssen wir das refactoren und eine activeForm property verwenden.  Diese Property muss
   * mit dem Server synchronisiert werden, damit auch das server-seitige desktop.getActiveForm() stimmt.
   * Auch im zusammenhang mit focus-handling nochmals überdenken.
   */
  this._addAdapterProperties(['viewButtons', 'actions', 'views', 'dialogs', 'outline', 'messageBoxes', 'fileChoosers', 'addOns', 'keyStrokes']);

  this.viewTabsController;
  this.formController;
  this.messageBoxController;
  this.fileChooserController;
  this.initialFormRendering = false;
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

scout.DesktopStyle = {
  DEFAULT: 'DEFAULT',
  BENCH: 'BENCH'
};

scout.Desktop.prototype._init = function(model) {
  scout.Desktop.parent.prototype._init.call(this, model);
  this.viewTabsController = new scout.ViewTabsController(this);
  this.formController = new scout.DesktopFormController(this, this.session);
  this.messageBoxController = new scout.MessageBoxController(this, this.session);
  this.fileChooserController = new scout.FileChooserController(this, this.session);
  this._addNullOutline(model.outline);
  this._resizeHandler = this.onResize.bind(this);
  this.navigationVisible = scout.nvl(model.navigationVisible, this.desktopStyle === scout.DesktopStyle.DEFAULT);
  this.headerVisible = scout.nvl(model.headerVisible, this.desktopStyle === scout.DesktopStyle.DEFAULT);
  this.benchVisible = scout.nvl(model.benchVisible, true);
};

scout.Desktop.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.Desktop.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Keystroke on the top-level DOM element which works as a catch-all when the busy indicator is active
  keyStrokeContext.registerKeyStroke(new scout.DesktopKeyStroke(this.session));
};

scout.Desktop.prototype._onChildAdapterCreation = function(propertyName, model) {
  if (propertyName === 'viewButtons') {
    model.desktop = this;
  } else if (propertyName === 'actions') {
    model.desktop = this;
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
  this._renderLogoUrl();
  this._renderSplitter();
  this._setSplitterPosition();
  this.addOns.forEach(function(addOn) {
    addOn.render(this.$container);
  });
  this.$container.window().on('resize', this._resizeHandler);

  // prevent general drag and drop, dropping a file anywhere in the application must not open this file in browser
  this._setupDragAndDrop();

  this._disableContextMenu();
};

scout.Desktop.prototype._remove = function() {
  this.$container.window().off('resize', this._resizeHandler);
  scout.Desktop.parent.prototype._remove.call(this);
};

scout.Desktop.prototype._disableContextMenu = function() {
  // Switch off browser's default context menu for the entire scout desktop (except input fields)
  this.$container.on('contextmenu', function(event) {
    if (event.target.nodeName !== 'INPUT' && event.target.nodeName !== 'TEXTAREA' && !event.target.isContentEditable) {
      event.preventDefault();
    }
  });
};

/**
 * Goes up in display hierarchy to find the form to select on desktop. null if outline is selected.
 */
scout.Desktop.prototype._findActiveSelectablePart = function(form) {
  if (form.parent.isView && form.parent.isDialog) {
    if (form.parent.isView()) {
      return form.parent;
    } else if (form.parent.isDialog()) {
      return this._findActiveSelectablePart(form.parent);
    }
  }
  return null;
};

scout.Desktop.prototype._postRender = function() {
  // Render attached forms, message boxes and file choosers.
  this.initialFormRendering = true;
  this.formController.render();
  this.messageBoxController.render();
  this.fileChooserController.render();

  // find active form and set selected.
  var selectable;
  if (this.activeForm) {
    var form = this.session.getModelAdapter(this.activeForm);
    if (form.isDialog()) {
      // find ui selectable part
      selectable = this._findActiveSelectablePart(form);
    } else if (form.isView()) {
      selectable = form;
    }
  }
  if (!selectable) {
    this.bringOutlineToFront();
  } else {
    this.viewTabsController.selectViewTab(this.viewTabsController.viewTab(selectable));
  }
  this.initialFormRendering = false;
};

scout.Desktop.prototype._renderActiveForm = function() {
  // NOP -> is handled in _setFormActivated when ui changes active form or if model changes form in _onModelFormShow/_onModelFormActivate
};

scout.Desktop.prototype._renderBench = function() {
  if (this.bench) {
    return;
  }
  this.bench = scout.create('DesktopBench', {
    parent: this
  });
  this.bench.render(this.$container);
  this.bench.$container.insertBefore(this.$overlaySeparator);
};

scout.Desktop.prototype._removeBench = function() {
  if (!this.bench) {
    return;
  }
  this.bench.remove();
  this.bench = null;
};

scout.Desktop.prototype._renderBenchVisible = function() {
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
    outline: this.outline
  });
  this.navigation.render(this.$container);
  this.navigation.$container.insertBefore(this.$overlaySeparator);
};

scout.Desktop.prototype._removeNavigation = function() {
  if (!this.navigation) {
    return;
  }
  this.navigation.remove();
  this.navigation = null;
};

scout.Desktop.prototype._renderNavigationVisible = function() {
  if (this.navigationVisible) {
    this._renderNavigation();
  } else {
    this._removeNavigation();
  }
};

scout.Desktop.prototype._renderHeader = function() {
  if (this.header) {
    return;
  }
  this.header = scout.create('DesktopHeader', {
    parent: this
  });
  this.header.render(this.$container);
  this.header.$container.insertBefore(this.$overlaySeparator);
};

scout.Desktop.prototype._removeHeader = function() {
  if (!this.header) {
    return;
  }
  this.header.remove();
  this.header = null;
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

scout.Desktop.prototype._renderSplitter = function() {
  if (!this.navigation) {
    return;
  }
  this.splitter = scout.create('Splitter', {
    parent: this,
    $anchor: this.navigation.$container,
    $root: this.$container,
    maxRatio: 0.5
  });
  this.splitter.render(this.$container);
  this.splitter.$container.insertBefore(this.$overlaySeparator);
  this.splitter.on('resize', this._onSplitterResize.bind(this));
  this.splitter.on('resizeend', this._onSplitterResizeEnd.bind(this));
};

scout.Desktop.prototype._setSplitterPosition = function() {
  if (!this.navigation) {
    return;
  }
  // FIXME awe: (user-prefs) Use user-preferences instead of sessionStorage
  var storedSplitterPosition = this.cacheSplitterPosition && sessionStorage.getItem('scout:desktopSplitterPosition');
  if (storedSplitterPosition) {
    // Restore splitter position
    var splitterPosition = parseInt(storedSplitterPosition, 10);
    this.splitter.updatePosition(splitterPosition);
    this.invalidateLayoutTree();
  } else {
    // Set initial splitter position (default defined by css)
    this.splitter.updatePosition();
    this.invalidateLayoutTree();
  }
};

scout.Desktop.prototype.onResize = function(event) {
  this.revalidateLayout();
};

scout.Desktop.prototype.revalidateHeaderLayout = function() {
  if (this.header) {
    this.header.revalidateLayout();
  }
};

scout.Desktop.prototype._onSplitterResize = function(event) {
  this.resizing = true;
  this.revalidateLayout();
};

scout.Desktop.prototype._onSplitterResizeEnd = function(event) {
  var splitterPosition = event.data;

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
        this.splitter.updatePosition();
        this.revalidateLayout();
        this.resizing = false; // progress seems to be called after complete again -> layout requires flag to be properly set
      }.bind(this),
      complete: function() {
        this.resizing = true;
        this.splitter.updatePosition();
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

scout.Desktop.prototype.setOutline = function(outline) {
  this.outline = outline;
  if (this.navigation) {
    this.navigation.setOutline(this.outline);
  }
  this.trigger('outlineChanged');
};

scout.Desktop.prototype.setNavigationVisible = function(visible) {
  this.navigationVisible = visible;
  if (this.rendered) {
    this._renderNavigationVisible();
  }
};

scout.Desktop.prototype.setBenchVisible = function(visible) {
  this.benchVisible = visible;
  if (this.rendered) {
    this._renderBenchVisible();
  }
};

scout.Desktop.prototype._onModelFormShow = function(event) {
  var form, displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    form = this.session.getOrCreateModelAdapter(event.form, displayParent.formController.displayParent);
    this._setFormActivated(form, true);
    // register listener to recover active form when child dialog is removed
    displayParent.formController.registerAndRender(event.form, event.position, true);
  }
};

scout.Desktop.prototype._onModelFormHide = function(event) {
  var displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    displayParent.formController.unregisterAndRemove(event.form);
  }
};

scout.Desktop.prototype._onModelFormActivate = function(event) {
  var displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    displayParent.formController.activateForm(event.form);
    this._setFormActivated(this.session.getOrCreateModelAdapter(event.form, displayParent.formController.displayParent), true);
  }
};

scout.Desktop.prototype._onModelMessageBoxShow = function(event) {
  var displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    displayParent.messageBoxController.registerAndRender(event.messageBox);
  }
};

scout.Desktop.prototype._onModelMessageBoxHide = function(event) {
  var displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    displayParent.messageBoxController.unregisterAndRemove(event.messageBox);
  }
};

scout.Desktop.prototype._onModelFileChooserShow = function(event) {
  var displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    displayParent.fileChooserController.registerAndRender(event.fileChooser);
  }
};

scout.Desktop.prototype._onModelFileChooserHide = function(event) {
  var displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    displayParent.fileChooserController.unregisterAndRemove(event.fileChooser);
  }
};

scout.Desktop.prototype._onModelOpenUri = function(event) {
  $.log.debug('(Desktop#_onModelOpenUri) uri=' + event.uri + ' action=' + event.action);
  if (!event.uri) {
    return;
  }

  if (event.action === 'download') {
    this._openUriInIFrame(event.uri);
  } else if (event.action === 'open') {
    // TODO [5.2] bsh: Does that really work on all platforms?
    this._openUriInIFrame(event.uri);
  } else if (event.action === 'new-window') {
    this._openUriAsNewWindow(event.uri);
  }
};

scout.Desktop.prototype._onModelOutlineChanged = function(event) {
  if (scout.DesktopStyle.DEFAULT === this.desktopStyle) {
    this.setOutline(this.session.getOrCreateModelAdapter(event.outline, this));
  }
};

scout.Desktop.prototype._onModelOutlineContentActivate = function(event) {
  if (scout.DesktopStyle.DEFAULT === this.desktopStyle) {
    this.bringOutlineToFront();
  }
};

scout.Desktop.prototype._onModelAddNotification = function(event) {
  scout.create('DesktopNotification', {
    parent: this,
    id: event.id,
    duration: event.duration,
    status: event.status,
    closeable: event.closeable
  }).show();
};

scout.Desktop.prototype._onModelRemoveNotification = function(event) {
  this.removeNotification(event.id);
};

scout.Desktop.prototype.onModelAction = function(event) {
  if (event.type === 'formShow') {
    this._onModelFormShow(event);
  } else if (event.type === 'formHide') {
    this._onModelFormHide(event);
  } else if (event.type === 'formActivate') {
    this._onModelFormActivate(event);
  } else if (event.type === 'messageBoxShow') {
    this._onModelMessageBoxShow(event);
  } else if (event.type === 'messageBoxHide') {
    this._onModelMessageBoxHide(event);
  } else if (event.type === 'fileChooserShow') {
    this._onModelFileChooserShow(event);
  } else if (event.type === 'fileChooserHide') {
    this._onModelFileChooserHide(event);
  } else if (event.type === 'openUri') {
    this._onModelOpenUri(event);
  } else if (event.type === 'outlineChanged') {
    this._onModelOutlineChanged(event);
  } else if (event.type === 'outlineContentActivate') {
    this._onModelOutlineContentActivate(event);
  } else if (event.type === 'addNotification') {
    this._onModelAddNotification(event);
  } else if (event.type === 'removeNotification') {
    this._onModelRemoveNotification(event);
  } else {
    scout.Desktop.parent.prototype.onModelAction.call(this, event);
  }
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
  this.viewTabsController.deselectViewTab();

  if (this.outline.inBackground) {
    if (this.navigation) {
      this.navigation.bringToFront();
    }
    if (this.bench) {
      this.bench.bringToFront();
    }
  }

  this._renderBenchDropShadow();
  // Set active form to null because outline is active form.
  this._setOutlineActivated();
};

scout.Desktop.prototype.sendOutlineToBack = function() {
  if (this.outline.inBackground) {
    return;
  }

  if (this.navigation) {
    this.navigation.sendToBack();
  }
  if (this.bench) {
    this.bench.sendToBack();
  }
  this._renderBenchDropShadow();
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns 'true' if the Desktop is currently accessible to the user.
 */
scout.Desktop.prototype.inFront = function() {
  return true; // Desktop is always available to the user.
};

scout.Desktop.prototype._renderBenchDropShadow = function() {
  if (this.navigation && this.bench) {
    this.bench.$container.toggleClass('drop-shadow', this.outline.inBackground);
  }
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

/**
 * Creates a local "null-outline" and an OutlineViewButton which is used, when no outline is available.
 * This avoids a lot of if/else code. The OVB adds a property 'visibleInMenu' which is only used in
 * the UI to decide whether or not the OVB will be shown in the ViewMenuPopup.js.
 */
scout.Desktop.prototype._addNullOutline = function(outline) {
  if (outline) {
    return;
  }
  var nullOutline = scout.create('Outline', {
      parent: this
    }),
    ovb = scout.create('OutlineViewButton', {
      parent: this,
      displayStyle: 'MENU',
      selected: true,
      text: this.session.text('ui.Outlines'),
      desktop: this,
      visibleInMenu: false
    });

  ovb.outline = nullOutline;
  this.outline = nullOutline;
  this.viewButtons.push(ovb);
};

scout.Desktop.prototype._setOutlineActivated = function() {
  this._setFormActivated();
};

scout.Desktop.prototype._setFormActivated = function(form, suppressSend) {
  // If desktop is in rendering process the can not set a new active for. instead the active form from the model is set selected.
  if (!this.rendered || this.initialFormRendering) {
    return;
  }

  if ((form && this.activeForm !== form.id) || (!form && this.activeForm)) {
    this.activeForm = form ? form.id : null;
    if (!suppressSend) {
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
