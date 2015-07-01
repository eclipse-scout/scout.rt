scout.Desktop = function() {
  scout.Desktop.parent.call(this);

  this._$viewTabBar;
  this._$taskBar;
  this._$toolBar;
  this.$bench;

  this.navigation;
  this._allTabs = [];
  /**
   * outline-content = outline form or table
   */
  this._outlineContent;
  this._selectedTab;

  /**
   * FIXME DWI: (activeForm): selectedTool wird nun auch als 'activeForm' verwendet (siehe TableKeystrokeAdapter.js)
   * Wahrscheinlich müssen wir das refactoren und eine activeForm property verwenden.  Diese Property muss
   * mit dem Server synchronisiert werden, damit auch das server-seitige desktop.getActiveForm() stimmt.
   * Auch im zusammenhang mit focus-handling nochmals überdenken.
   */
  this.selectedTool;
  this._addAdapterProperties(['viewButtons', 'actions', 'views', 'dialogs', 'outline', 'messageBoxes', 'fileChoosers', 'addOns', 'keyStrokes']);
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

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

  this.views.forEach(this._renderView.bind(this));
  this.dialogs.forEach(this._renderDialog.bind(this));
  // TODO BSH: How to determine order of messageboxes and filechoosers?
  this.messageBoxes.forEach(this._renderMessageBox.bind(this));
  this.fileChoosers.forEach(this._renderFileChooser.bind(this));

  $(window).on('resize', this.onResize.bind(this));

  // prevent general drag and drop, dropping a file anywhere in the application must not open this file in browser
  $parent.on('dragstart', function (event) {
    event.stopPropagation();
    event.preventDefault();
    // change cursor to forbidden (no dropping allowed)
    event.originalEvent.dataTransfer.dropEffect = "none";
  });
  $parent.on('dragover', function (event) {
    event.stopPropagation();
    event.preventDefault();
    // change cursor to forbidden (no dropping allowed)
    event.originalEvent.dataTransfer.dropEffect = "none";
  });
  $parent.on('drop', function (event) {
    event.stopPropagation();
    event.preventDefault();
  });

  // Switch off browser's default context menu for the entire scout desktop (except input fields)
  $parent.bind('contextmenu', function(event) {
    if (event.target.nodeName !== 'INPUT' && event.target.nodeName !== 'TEXTAREA' && !event.target.isContentEditable) {
      event.preventDefault();
    }
  });
  scout.keyStrokeManager.installAdapter($parent, new scout.DesktopKeyStrokeAdapter(this));
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

scout.Desktop.prototype._createSplitter = function($parent) {
  if (!this._hasNavigation()) {
    return;
  }
  this.splitter = new scout.Splitter({
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
  }
  else {
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

// FIXME AWE/CGU this is called by JQuery UI when a dialog gets resized, why?
scout.Desktop.prototype.onResize = function(event) {
  if (this._selectedTab) {
    this._selectedTab.onResize();
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
  if (splitterPosition < this.navigation.BREADCRUMB_SWITCH_WIDTH) {
    // Set width of navigation to BREADCRUMB_SWITCH_WIDTH, using an animation.
    // While animating, update the desktop layout.
    // At the end of the animation, update the desktop layout, and store the splitter position.
    this.navigation.$navigation.animate({
      width: this.navigation.BREADCRUMB_SWITCH_WIDTH
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
  this.navigation.onResize({data: newPosition});
  this.onResize({data: newPosition});
};

scout.Desktop.prototype._addTab = function(tab) {
  this._allTabs.push(tab);
  this._setSelectedTab(tab);
};

scout.Desktop.prototype._removeTab = function(tab) {
  scout.arrays.remove(this._allTabs, tab);
  tab.removeTab();

  // FIXME DWI: (activeForm) use activeForm here or when no form is active, show outline again (from A.WE)
  if (this._allTabs.length > 0) {
    this._setSelectedTab(this._allTabs[this._allTabs.length - 1]);
  } else {
    this._attachOutlineContent();
    this._bringNavigationToFront();
    this._selectedTab = null;
  }

  this._layoutTaskBar();
};

scout.Desktop.prototype._setSelectedTab = function(tab) {
  if (this._selectedTab !== tab) {
    this._sendNavigationToBack();
    this._detachOutlineContent();
    this._deselectTab();
    this._selectTab(tab);
    this._layoutTaskBar();
    scout.focusManager.validateFocus(this.session.uiSessionId, 'desktop');
  }
};

scout.Desktop.prototype._detachOutlineContent = function() {
  if (this._outlineContent) {
    var $outlineContent = this._outlineContent.$container;
    this.session.detachHelper.beforeDetach($outlineContent);
    $outlineContent.detach();
  }
};

scout.Desktop.prototype._attachOutlineContent = function() {
  if (this._outlineContent) {
    var $outlineContent = this._outlineContent.$container;
    this.$bench.append($outlineContent);
    this.session.detachHelper.afterAttach($outlineContent);

    // If the parent has been resized while the content was not visible, the content has the wrong size -> update
    var htmlComp = scout.HtmlComponent.get($outlineContent);
    var htmlParent = htmlComp.getParent();
    htmlComp.setSize(htmlParent.getSize());
  }
};

/**
 * De-selects the currently selected tab.
 */
scout.Desktop.prototype._deselectTab = function() {
 if (this._selectedTab) {
   this._selectedTab.deselect();
   this._selectedTab = null;
 }
};

scout.Desktop.prototype._selectTab = function(tab) {
  tab.select();
  this._selectedTab = tab;
};

/* handling of forms */

scout.Desktop.prototype._renderDialog = function(dialog) {
  dialog.render(this.$container);
  dialog.htmlComp.pixelBasedSizing = true;

  var prefSize = dialog.htmlComp.getPreferredSize(),
    dialogMargins = dialog.htmlComp.getMargins(),
    documentSize = new scout.Dimension($(document).width(), $(document).height()),
    dialogSize = new scout.Dimension();

  // class .dialog may specify a margin
  var maxWidth = (documentSize.width - dialogMargins.left - dialogMargins.right);
  var maxHeight = (documentSize.height - dialogMargins.top - dialogMargins.bottom);

  // Ensure the dialog is not larger than viewport
  dialogSize.width = Math.min(maxWidth, prefSize.width);
  dialogSize.height = Math.min(maxHeight, prefSize.height);

  var marginLeft = (documentSize.width - dialogSize.width) / 2;
  var marginTop = (documentSize.height - dialogSize.height) / 2;

  // optical middle
  var opticalMiddleOffset = Math.min(marginTop / 5, 10);
  marginTop -= opticalMiddleOffset;

  dialog.htmlComp.setSize(dialogSize);

  dialog.$container
    .cssMarginLeft(marginLeft)
    .cssMarginTop(marginTop);
};

/**
 * We only render the tab here. Because only the view of the currently selected tab is visible.
 * Thus it makes no sense to render all forms now.
 */
scout.Desktop.prototype._renderView = function(view) {
  var tab = new scout.DesktopViewTab(view, this.$bench);
  tab.events.on('tabClicked', this._setSelectedTab.bind(this));
  if (this._hasTaskBar()) {
    tab.renderTab(this._$viewTabBar);
  }
  this._addTab(tab);
  scout.focusManager.validateFocus(this.session.uiSessionId, 'desktop._renderView');
};

scout.Desktop.prototype._showForm = function(form) {
  if (this._isDialog(form)) {
    // FIXME AWE: (modal dialog) - show dialogs
  } else {
    this._setSelectedTab(form.tab);
  }
};

scout.Desktop.TargetWindow = {
  AUTO: 'AUTO',
  SELF: 'SELF',
  BLANK: 'BLANK'
};

scout.Desktop.prototype._openUri = function(event) {
  if (!event.uri) {
    return;
  }
  var newWindow = false;
  if (scout.Desktop.TargetWindow.BLANK === event.uriTarget) {
    newWindow = true;
  } else if (scout.Desktop.TargetWindow.SELF === event.uriTarget) {
    newWindow = false;
  } else if (scout.Desktop.TargetWindow.AUTO === event.uriTarget) {
    // this is important for download resources with Firefox. Firefox cancels all running
    // requests (also the background polling job) when a resource is opened in the same
    // windows as the Scout application. This would lead to a connection failure, thus
    // we always want to open the resource in a new window (Firefox automatically closes
    // this window as soon as the download is started).
    newWindow = !scout.device.supportsDownloadInSameWindow();
  }

  $.log.debug('(Desktop#_openUri) uri=' + event.uri + ' target=' + event.uriTarget + ' newWindow=' + newWindow);
  if (newWindow) {
    window.open(event.uri);
  } else {
    window.location.href = event.uri;
  }
};

/* communication with outline */

scout.Desktop.prototype.setOutlineContent = function(content) {
  if (this._outlineContent && this._outlineContent !== content) {
    if (scout.keyStrokeManager.isAdapterInstalled(this._outlineContent.keyStrokeAdapter)) {
      scout.keyStrokeManager.uninstallAdapter(this._outlineContent.keyStrokeAdapter);
    }
    this._outlineContent.remove();
    this._outlineContent = null;
  }

  if (!content) {
    return;
  }

  this._outlineContent = content;
  this._deselectTab();
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
  scout.focusManager.validateFocus(this.session.uiSessionId, 'update');
};

scout.Desktop.prototype.setOutline = function(outline) {
  this.outline = outline;
  this.navigation.onOutlineChanged(this.outline);
};

scout.Desktop.prototype.removeForm = function(id) {
  var form = this.session.getOrCreateModelAdapter(id, this);
  if (!form.rendered) {
    // Form has already been removed (either by form close or form removed event) -> no need to do it twice
    return;
  }
  if (this._isDialog(form)) {
    scout.arrays.remove(this.dialogs, form);
  } else {
    scout.arrays.remove(this.views, form);
  }
  if (form.tab) {
    this._removeTab(form.tab);
  }
  form.remove();
};

scout.Desktop.prototype._renderMessageBox = function(messageBox) {
  messageBox.render(this.$container);
};

scout.Desktop.prototype.onMessageBoxClosed = function(messageBox) {
  scout.arrays.remove(this.messageBoxes, messageBox);
};

scout.Desktop.prototype._renderFileChooser = function(fileChooser) {
  fileChooser.render(this.$container);
};

scout.Desktop.prototype.onFileChooserClosed = function(fileChooser) {
  scout.arrays.remove(this.fileChoosers, fileChooser);
};

scout.Desktop.prototype._onModelFormAdded = function(event) {
  var form = this.session.getOrCreateModelAdapter(event.form, this);
  if (this._isDialog(form)) {
    this.dialogs.push(form);
    this._renderDialog(form);
  } else {
    this.views.push(form);
    this._renderView(form);
  }
};

scout.Desktop.prototype._onModelFormRemoved = function(event) {
  this.removeForm(event.form);
};

scout.Desktop.prototype._onModelOutlineChanged = function(event) {
  if (scout.DesktopStyle.DEFAULT === this.desktopStyle) {
    this.setOutline(this.session.getOrCreateModelAdapter(event.outline, this));
  }
};

scout.Desktop.prototype._isDialog = function(form) {
  return form.displayHint === 'dialog';
};

scout.Desktop.prototype.onModelAction = function(event) {
  if (event.type === 'formAdded') {
    this._onModelFormAdded(event);
  } else if (event.type === 'formRemoved') {
    this._onModelFormRemoved(event);
  } else if (event.type === 'formEnsureVisible') {
    this._showForm(this.session.getOrCreateModelAdapter(event.form, this));
  } else if (event.type === 'outlineChanged') {
    this._onModelOutlineChanged(event);
  } else if (event.type === 'messageBoxAdded') {
    this._renderMessageBox(this.session.getOrCreateModelAdapter(event.messageBox, this));
  } else if (event.type === 'fileChooserAdded') {
    this._renderFileChooser(this.session.getOrCreateModelAdapter(event.fileChooser, this));
  } else if (event.type === 'openUri') {
    this._openUri(event);
  } else {
    scout.Desktop.parent.prototype.onModelAction.call(this, event);
  }
  scout.focusManager.validateFocus(this.session.uiSessionId);
};

scout.Desktop.prototype.tabCount = function() {
  return this._allTabs.length;
};

scout.Desktop.prototype.bringOutlineToFront = function(outline) {
  this._deselectTab();
  if (this.outline === outline) {
    if (this.outline.inBackground) {
      this._attachOutlineContent();
    }
  } else {
    this.setOutline(outline);
  }
  this._bringNavigationToFront();
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

scout.Desktop.prototype._bringNavigationToFront = function () {
  this.navigation.bringToFront();
  this._renderBenchDropShadow(false);
};

scout.Desktop.prototype._sendNavigationToBack = function () {
  this.navigation.sendToBack();
  this._renderBenchDropShadow(true);
};

scout.Desktop.prototype._renderBenchDropShadow = function(showShadow) {
  if (this._hasNavigation()) {
    this.$bench.toggleClass('drop-shadow', showShadow);
  }
};
