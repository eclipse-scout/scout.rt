scout.Desktop = function() {
  scout.Desktop.parent.call(this);

  this._$viewTabBar;
  this._$taskBar; // FIXME awe: uniform naming
  this._$toolBar; // FIXME awe: uniform naming
  this.$bench;

  this.navigation;
  /**
   * outline-content = outline form or table
   */
  this._outlineContent;

  /**
   * FIXME DWI: (activeForm): selectedTool wird nun auch als 'activeForm' verwendet (siehe TableKeystrokeAdapter.js)
   * Wahrscheinlich müssen wir das refactoren und eine activeForm property verwenden.  Diese Property muss
   * mit dem Server synchronisiert werden, damit auch das server-seitige desktop.getActiveForm() stimmt.
   * Auch im zusammenhang mit focus-handling nochmals überdenken.
   */
  this.selectedTool;
  this._addAdapterProperties(['viewButtons', 'actions', 'views', 'dialogs', 'outline', 'messageBoxes', 'fileChoosers', 'addOns', 'keyStrokes']);

  this.viewTabsController;
  this.formController;
  this.messageBoxController;
  this.fileChooserController;
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

scout.Desktop.prototype._init = function(model, session) {
  scout.Desktop.parent.prototype._init.call(this, model, session);

  this.viewTabsController = new scout.ViewTabsController(this);

  this.formController = new scout.FormController(this, session);
  this.messageBoxController = new scout.MessageBoxController(this, session);
  this.fileChooserController = new scout.FileChooserController(this, session);
};

scout.DesktopStyle = {
  DEFAULT: 'DEFAULT',
  BENCH: 'BENCH'
};

scout.Desktop.prototype.onChildAdapterCreated = function(propertyName, adapter) {
  if (propertyName === 'viewButtons') {
    adapter.desktop = this;
  } else if (propertyName === 'actions') {
    adapter.desktop = this;
  }
};

scout.Desktop.prototype._render = function($parent) {
  var hasNavigation = this._hasNavigation(),
    hasTaskBar = this._hasTaskBar();

  this.$container = $parent;
  this.$container.toggleClass('has-navigation', hasNavigation);
  this._renderUniqueId($parent);
  this._renderModelClass($parent);

  this.navigation = hasNavigation ? new scout.DesktopNavigation(this) : scout.NullDesktopNavigation;
  this.navigation.render($parent);
  this._renderTaskBar($parent);

  this.$bench = this.$container.appendDiv('desktop-bench');
  this.$bench.toggleClass('has-taskbar', hasTaskBar);
  new scout.HtmlComponent(this.$bench, this.session);
  this._createSplitter($parent);
  this.addOns.forEach(function(addOn) {
    addOn.render($parent);
  });

  this._renderToolMenus();
  this.navigation.onOutlineChanged(this.outline);
  this._setSplitterPosition();

  $(window).on('resize', this.onResize.bind(this));

  // prevent general drag and drop, dropping a file anywhere in the application must not open this file in browser
  this._setupDragAndDrop();

  // Switch off browser's default context menu for the entire scout desktop (except input fields)
  $parent.bind('contextmenu', function(event) {
    if (event.target.nodeName !== 'INPUT' && event.target.nodeName !== 'TEXTAREA' && !event.target.isContentEditable) {
      event.preventDefault();
    }
  });

  // Keystrokes referring to the desktop bench, and are keystrokes declared on desktop (desktop.keyStrokes).
  scout.KeyStrokeUtil.installAdapter(this.session, new scout.DesktopBenchKeyStrokeAdapter(this));
  // Keystrokes referring to the desktop view button area, and are keystrokes to switch between outlines (desktop.viewButtons).
  scout.KeyStrokeUtil.installAdapter(this.session, new scout.DesktopViewButtonKeyStrokeAdapter(this));
  // Keystrokes referring to the desktop task bar, and are keystrokes associated with FormToolButtons (desktop.actions).
  scout.KeyStrokeUtil.installAdapter(this.session, new scout.DesktopTaskBarKeyStrokeAdapter(this));
};

scout.Desktop.prototype._postRender = function() {
  // Render attached forms, message boxes and file choosers.
  this.formController.render();
  this.messageBoxController.render();
  this.fileChooserController.render();
};

scout.Desktop.prototype._renderToolMenus = function() {
  if (!this._hasTaskBar()) {
    return;
  }
  // we set the menuStyle property to render a menu with a different style
  // depending on where the menu is located (taskbar VS menubar).
  var i, action;
  for (i = 0; i < this.actions.length; i++) {
    action = this.actions[i];
    action.actionStyle = scout.Action.ActionStyle.TASK_BAR;
    action.render(this._$toolBar);
  }
  if (action) {
    action.$container.addClass('last');
  }
  if (this.selectedTool) {
    this.selectedTool.popup.alignTo();
  }
};

scout.Desktop.prototype._renderTaskBar = function($parent) {
  if (!this._hasTaskBar()) {
    return;
  }
  this._$taskBar = $parent.appendDiv('desktop-taskbar');
  var htmlTabbar = new scout.HtmlComponent(this._$taskBar, this.session);
  htmlTabbar.setLayout(new scout.DesktopTabBarLayout(this));
  this._$taskBar.appendDiv('taskbar-logo')
    .delay(1000)
    .animateAVCSD('width', 40, null, null, 1000);
  this._$viewTabBar = this._$taskBar.appendDiv('desktop-view-tabs');
  this._$toolBar = this._$taskBar.appendDiv('taskbar-tools');
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
},

scout.Desktop.prototype._createSplitter = function($parent) {
  if (!this._hasNavigation()) {
    return;
  }
  this.splitter = new scout.Splitter(this.session, {
    $anchor: this.navigation.$navigation,
    $root: this.$container,
    maxRatio: 0.5
  });
  this.splitter.render($parent);
  this.splitter.on('resize', this._onSplitterResize.bind(this));
  this.splitter.on('resizeend', this._onSplitterResizeEnd.bind(this));
};

scout.Desktop.prototype._setSplitterPosition = function() {
  if (!this._hasNavigation()) {
    return;
  }
  // FIXME AWE: (user-prefs) Use user-preferences instead of sessionStorage
  var storedSplitterPosition = sessionStorage.getItem('scout:desktopSplitterPosition');
  if (storedSplitterPosition) {
    // Restore splitter position
    var splitterPosition = parseInt(storedSplitterPosition, 10);
    this.splitter.updatePosition(splitterPosition);
    this._handleUpdateSplitterPosition(splitterPosition);
  } else {
    // Set initial splitter position
    this.splitter.updatePosition();
    this._handleUpdateSplitterPosition(this.splitter.positoin);
  }
};

scout.Desktop.prototype._hasNavigation = function() {
  return this.desktopStyle === scout.DesktopStyle.DEFAULT;
};

scout.Desktop.prototype._hasTaskBar = function() {
  return this.desktopStyle === scout.DesktopStyle.DEFAULT;
};

scout.Desktop.prototype.onResize = function(event) {
  var selectedViewTab = this.viewTabsController.selectedViewTab();
  if (selectedViewTab) {
    selectedViewTab.onResize();
  }
  if (this.outline) {
    this.outline.onResize();
  }
  if (this._outlineContent) {
    this._outlineContent.onResize();
  }
  this._layoutTaskBar();
};

scout.Desktop.prototype._layoutTaskBar = function() {
  if (this._hasTaskBar()) {
    var htmlTaskBar = scout.HtmlComponent.get(this._$taskBar);
    htmlTaskBar.revalidateLayout();
  }
};

scout.Desktop.prototype._onSplitterResize = function(event) {
  this._handleUpdateSplitterPosition(event.data);
};

scout.Desktop.prototype._onSplitterResizeEnd = function(event) {
  var splitterPosition = event.data;

  // Store size
  sessionStorage.setItem('scout:desktopSplitterPosition', splitterPosition);

  // Check if splitter is smaller than min size
  if (splitterPosition < scout.DesktopNavigation.BREADCRUMB_SWITCH_WIDTH) {
    // Set width of navigation to BREADCRUMB_SWITCH_WIDTH, using an animation.
    // While animating, update the desktop layout.
    // At the end of the animation, update the desktop layout, and store the splitter position.
    this.navigation.$navigation.animate({
      width: scout.DesktopNavigation.BREADCRUMB_SWITCH_WIDTH
    }, {
      progress: function() {
        this.splitter.updatePosition();
        this._handleUpdateSplitterPosition(this.splitter.position);
      }.bind(this),
      complete: function() {
        this.splitter.updatePosition();
        // Store size
        sessionStorage.setItem('scout:desktopSplitterPosition', this.splitter.position);
        this._handleUpdateSplitterPosition(this.splitter.position);
      }.bind(this)
    });
  }
};

scout.Desktop.prototype._handleUpdateSplitterPosition = function(newPosition) {
  this.navigation.onResize({
    data: newPosition
  });
  this.onResize({
    data: newPosition
  });
};

scout.Desktop.prototype._attachOutlineContent = function() {
  if (this._outlineContent) {
    this._outlineContent.attach();
  }
};

scout.Desktop.prototype._detachOutlineContent = function() {
  if (this._outlineContent) {
    this._outlineContent.detach();
  }
};

/* communication with outline */

scout.Desktop.prototype.setOutlineContent = function(content) {
  if (this._outlineContent && this._outlineContent !== content) {
    scout.KeyStrokeUtil.uninstallAdapter(this._outlineContent.keyStrokeAdapter);
    this._outlineContent.remove();
    this._outlineContent = null;
  }

  if (!content) {
    return;
  }

  this._outlineContent = content;
  this.viewTabsController.deselectViewTab();
  this._bringNavigationToFront();

  if (!content.rendered) {
    if (content instanceof scout.Table) {
      content.menuBar.top();
      content.menuBar.large();
    }
    content.render(this.$bench);

    // Request focus on first element in new outlineTab.
    scout.focusManager.validateFocus(this.session.uiSessionId);

    // FIXME CGU: maybe include in render?
    content.htmlComp.validateLayout();
    content.htmlComp.validateRoot = true;
  }

  // Request focus on first element in new outlineTab.
  scout.focusManager.validateFocus(this.session.uiSessionId);
};

scout.Desktop.prototype.setOutline = function(outline) {
  this.outline = outline;
  this.navigation.onOutlineChanged(this.outline);
};

scout.Desktop.prototype._onModelFormShow = function(event) {
  var displayParent = this.session.getModelAdapter(event.displayParent);
  if (displayParent) {
    displayParent.formController.registerAndRender(event.form);
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
  $.log.debug('(Desktop#_onModelOpenUri) uri=' + event.uri + ' openUriHint=' + event.openUriHint);
  if (!event.uri) {
    return;
  }

  if (!event.hint && event.uri.match(/^(callto|facetime|fax|geo|mailto|maps|notes|sip|skype|tel):/)) {
    event.hint = 'open-application';
  }

  if (event.hint === 'download') {
    this._openUriInIFrame(event.uri);
  } else if (event.hint === 'open-application') {
      // TODO BSH Does that really work on all platforms?
    this._openUriInIFrame(event.uri);
  } else {
    // 'new-window' -> Open popup
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
    this.bringOutlineToFront(this.outline);
  }
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
  } else {
    scout.Desktop.parent.prototype.onModelAction.call(this, event);
  }
};

scout.Desktop.prototype._openUriInIFrame = function(uri) {
  // Create a hidden iframe and set the URI as src attribute value
  var $iframe = $('<iframe>')
    .addClass('download-frame')
    .attr('tabindex', -1)
    .appendTo(this.session.$entryPoint)
    .attr('src', uri);

  // Remove the iframe again after 10s (should be enough to get the download started)
  setTimeout(function() {
    $iframe.remove();
  }, 10 * 1000);
};

scout.Desktop.prototype._openUriAsNewWindow = function(uri) {
  var popup = window.open(uri);

  // Chrome returns undefined, FF && IE null when popup is blocked
  // FIXME BSH IE also returns null when "protected mode" is on, even when the popup was successful! How to resolve this?
  if (!popup) {
    // Popup blocker detected
    var $notification = $.makeDiv('notification');
    var $notificationContent = $notification
      .appendDiv('notification-content notification-closable');
    $.makeDiv('close')
      .on('click', function() {
        this.removeNotification($notification);
      }.bind(this))
      .appendTo($notificationContent);
    $.makeDiv('popup-blocked-title')
      .text(this.session.text('ui.PopupBlockerDetected'))
      .appendTo($notificationContent);
    $('<a href="' + scout.strings.encode(uri) + '" target="_blank">')
      .addClass('popup-blocked-link')
      .text(this.session.text('ui.OpenManually'))
      .on('click', function() {
        this.removeNotification($notification);
      }.bind(this))
      .appendTo($notificationContent);
    this.addNotification($notification);
  }
};

scout.Desktop.prototype.bringOutlineToFront = function(outline) {
  this.viewTabsController.deselectViewTab();

  if (this.outline === outline) {
    if (this.outline.inBackground) {
      this._attachOutlineContent();
      this._bringNavigationToFront();
    }
  } else {
    this.setOutline(outline);
  }
};

/**
 * Called after width of navigation has been updated.
 */
scout.Desktop.prototype.navigationWidthUpdated = function(navigationWidth) {
  if (this._hasNavigation()) {
    this._$taskBar.css('left', navigationWidth);
    this.$bench.css('left', navigationWidth);
  }
};

scout.Desktop.prototype._bringNavigationToFront = function() {
  this.navigation.bringToFront();
  this._renderBenchDropShadow(false);
};

scout.Desktop.prototype._sendNavigationToBack = function() {
  this.navigation.sendToBack();
  this._renderBenchDropShadow(true);
};

scout.Desktop.prototype._renderBenchDropShadow = function(showShadow) {
  if (this._hasNavigation()) {
    this.$bench.toggleClass('drop-shadow', showShadow);
  }
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box, file-chooser or wait-dialog is showed with the Desktop as its 'displayParent'.
 */
scout.Desktop.prototype.glassPaneTargets = function() {
  // Do not return $container, because this is the parent of all forms and message boxes. Otherwise, no form could gain focus, even the form requested desktop modality.
  return $.makeArray(this.$container
      .children()
      .not('.splitter') // exclude splitter to be locked
      .not('.notifications')); // exclude notification box like 'connection interrupted' to be locked
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns 'true' if the Desktop is currently accessible to the user.
 */
scout.Desktop.prototype.inFront = function() {
  return true; // Desktop is always available to the user.
};
