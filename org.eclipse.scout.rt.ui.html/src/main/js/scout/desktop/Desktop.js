scout.Desktop = function() {
  scout.Desktop.parent.call(this);

  this.navigation;
  this.$taskBar;
  this.$tabbar;
  this.$toolbar;
  this.$bench;

  this._allTabs = [];
  this._selectedTab;

  /**
   * TODO AWE/CGU: (key-handling, forms) wird nun auch als 'activeForm' verwendet (siehe TableKeystrokeAdapter.js)
   * Wahrscheinlich müssen wir das refactoren und eine activeForm property verwenden.  Diese Property muss
   * mit dem Server synchronisiert werden, damit auch das server-seitige desktop.getActiveForm() stimmt.
   * Auch im zusammenhang mit focus-handling nochmals überdenken.
   */
  this.selectedTool;
  this._addAdapterProperties(['viewButtons', 'actions', 'views', 'dialogs', 'outline', 'messageBoxes', 'fileChoosers', 'addOns', 'keyStrokes']);
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

scout.Desktop.prototype.onChildAdapterCreated = function(propertyName, adapter) {
  if (propertyName === 'viewButtons') {
    adapter.desktop = this;
  }
  if (propertyName === 'actions') {
    adapter.desktop = this;
  }
};

scout.Desktop.prototype._render = function($parent) {
  var i, action;

  this.$container = $parent;
  this._renderUniqueId($parent);
  this._renderModelClass($parent);

  this.navigation = new scout.DesktopNavigation(this);
  this.navigation.render($parent);

  this.$taskBar = $parent.appendDiv('desktop-taskbar');
  var htmlTabbar = new scout.HtmlComponent(this.$taskBar, this.session);
  htmlTabbar.setLayout(new scout.DesktopTabBarLayout(this));

  this.$taskBar.appendDiv('taskbar-logo')
    .delay(1000)
    .animateAVCSD('width', 40, null, null, 1000);

  this.$tabbar = this.$taskBar.appendDiv('taskbar-tabs');

  // FIXME AWE: (menu) hier menu-bar verwenden?
  this.$toolbar = this.$taskBar.appendDiv('taskbar-tools');
  this.$bench = this.$container.appendDiv('desktop-bench');
  new scout.HtmlComponent(this.$bench, this.session);

  this.splitter = new scout.Splitter({
    $anchor: this.navigation.$navigation,
    $root: this.$container,
    maxRatio: 0.5
  });
  this.splitter.render($parent);
  this.splitter.on('resize', this.onSplitterResize.bind(this));
  this.splitter.on('resizeend', this.onSplitterResizeEnd.bind(this));

  this._outlineTab = new scout.Desktop.TabAndContent(this);

  this.addOns.forEach(function(addOn) {
    addOn.render($parent);
  });

  // we set the menuStyle property to render a menu with a different style
  // depending on where the menu is located (taskbar VS menubar).
  for (i = 0; i < this.actions.length; i++) {
    action = this.actions[i];
    action.actionStyle = scout.Action.ActionStyle.TASK_BAR;
    action.render(this.$toolbar);
  }
  if (action) {
    action.$container.addClass('last');
  }
  if (this.selectedTool) {
    this.selectedTool.popup.alignTo();
  }
  this.navigation.onOutlineChanged(this.outline);

  // FIXME AWE: (user-prefs) Use user-preferences instead of sessionStorage
  var storedSplitterPosition = sessionStorage.getItem('scout:desktopSplitterPosition');
  if (storedSplitterPosition) {
    var splitterPosition = parseInt(storedSplitterPosition, 10);
    this.splitter.updatePosition(splitterPosition);
    this._handleUpdateSplitterPosition(splitterPosition);
  }

  this.views.forEach(this._renderView.bind(this));
  this.dialogs.forEach(this._renderDialog.bind(this));
  // TODO BSH How to determine order of messageboxes and filechoosers?
  this.messageBoxes.forEach(this._renderMessageBox.bind(this));
  this.fileChoosers.forEach(this._renderFileChooser.bind(this));

  $(window).on('resize', this.onResize.bind(this));

  // Switch off browser's default context menu for the entire scout desktop (except input fields)
  $parent.bind('contextmenu', function(event) {
    if (event.target.nodeName !== 'INPUT' && event.target.nodeName !== 'TEXTAREA' && !event.target.isContentEditable) {
      event.preventDefault();
    }
  });
  scout.keyStrokeManager.installAdapter($parent, new scout.DesktopKeyStrokeAdapter(this));
};

scout.Desktop.prototype.onResize = function(event) {
  // FIXME AWE/CGU this is called by JQuery ui when the dialog gets resized, why?
  if (this._selectedTab && this._selectedTab.content) {
    this._selectedTab.content.onResize();
  }
  if (this.outline) {
    this.outline.onResize();
  }
  this._layoutTaskBar();
};

scout.Desktop.prototype._layoutTaskBar = function() {
  var htmlTaskBar = scout.HtmlComponent.get(this.$taskBar);
  htmlTaskBar.revalidateLayout();
};

scout.Desktop.prototype.onSplitterResize = function(event) {
  this._handleUpdateSplitterPosition(event.data);
};

scout.Desktop.prototype.onSplitterResizeEnd = function(event) {
  var splitterPosition = event.data;

  // Store size
  sessionStorage.setItem('scout:desktopSplitterPosition', splitterPosition);

  // Check if splitter is smaller than min size
  if (splitterPosition < this.navigation.breadcrumbSwitchWidth) {
    // Set width of navigation to breadcrumbSwitchWidth, using an animation.
    // While animating, update the desktop layout.
    // At the end of the animation, update the desktop layout, and store the splitter position.
    this.navigation.$navigation.animate({
      width: this.navigation.breadcrumbSwitchWidth
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

/* Tab-handling */

// TODO AWE/CGU: (dialoge) Über modale Dialoge sprechen, v.a. über folgenden Fall:
// - Outline-Tab > Autrag Xyz > Auftrag bearbeiten
// - Tab geht auf, nun wieder auf Outline-Tab wechseln und nochmals Auftrag bearbeiten
// - ein weiterer Tab geht auf, kann man beliebig wiederholen
// Lösungen besprechen. Eine Möglichkeit wäre, bei Klick auf Auftrag bearbeiten in den
// bereits geöffneten Tab/Dialog zu wecheseln. Oder das Menü-Item disablen.

// ist schon in AbstractForm mit execStartExclusive gelöst - muss von CRM überall implementiert werden.

scout.Desktop.prototype._addTab = function(tab, prepend) {
  tab.$container = $.makeDiv('taskbar-tab-item')
    .append($.makeDiv('title', tab.title))
    .append($.makeDiv('sub-title', tab.subTitle));
  if (prepend) {
    tab.$container.prependTo(this.$tabbar);
  } else {
    tab.$container.appendTo(this.$tabbar);
  }

  tab.$container.on('click', function() {
    if (tab !== this._selectedTab) {
      this._selectTab(tab);
    }
  }.bind(this));

  this._allTabs.push(tab);
  this._layoutTaskBar();
};

scout.Desktop.prototype._isTabVisible = function(tab) {
  return this._allTabs.indexOf(tab) >= 0;
};

scout.Desktop.prototype._updateTab = function(tab) {
  if (!tab.$container) {
    return;
  }

  var setTitle = function(selector, title) {
    var $e = tab.$container.children(selector);
    if (title) {
      $e.text(title).setVisible(true);
    } else {
      $e.setVisible(false);
    }
  };
  setTitle('.title', tab.title);
  setTitle('.sub-title', tab.subTitle);
};

scout.Desktop.prototype._removeTab = function(tab) {
  scout.arrays.remove(this._allTabs, tab);

  // If tab was already rendered, unrender it
  if (tab.$container) {
    if (tab.$container.isSelected() && this._allTabs.length > 0) {
      this._selectTab(this._allTabs[this._allTabs.length - 1]);
    }
    tab.$container.remove();
  }

  this._layoutTaskBar();
};

scout.Desktop.prototype._selectTab = function(tab) {
  if (this._selectedTab) {
    this._unselectTab(this._selectedTab);
  }
  if (tab.$container) {
    tab.$container.select(true);
  }
  this._selectedTab = tab;
  if (tab.content instanceof scout.Table) {
    // Install adapter on parent (no table focus required)
    if (!(tab.content.keyStrokeAdapter instanceof scout.DesktopTableKeyStrokeAdapter)) {
      tab.content.injectKeyStrokeAdapter(new scout.DesktopTableKeyStrokeAdapter(tab.content), this.$container);
    } else {
      scout.keyStrokeManager.installAdapter(this.$container, tab.content.keyStrokeAdapter);
    }
  }
  if (tab.$content && tab.$content.length > 0) {
    this.$bench.append(tab.$content);
    this.session.detachHelper.afterAttach(tab.$content);

    // If the parent has been resized while the content was not visible, the content has the wrong size -> update
    var htmlComp = scout.HtmlComponent.get(tab.$content);
    var htmlParent = htmlComp.getParent();
    htmlComp.setSize(htmlParent.getSize());
  }
  this._layoutTaskBar();
  scout.focusManager.validateFocus(this.session.uiSessionId, 'desktop');
};

scout.Desktop.prototype._unselectTab = function(tab) {
  //remove registered keyStrokeAdapters
  if (tab.content && scout.keyStrokeManager.isAdapterInstalled(tab.content.keyStrokeAdapter)) {
    scout.keyStrokeManager.uninstallAdapter(tab.content.keyStrokeAdapter);
  }

  tab.$content = this.$bench.children();
  if (tab.$content.length > 0) {
    this.session.detachHelper.beforeDetach(tab.$content);
    tab.$content.detach();
  }

  if (tab.$container) {
    tab.$container.select(false);
  }
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

scout.Desktop.prototype._renderView = function(view) {
  var tab = new scout.Desktop.TabAndContent(this, view, view.title, view.subTitle);
  this._addTab(tab);
  this._selectTab(tab);
  view.render(this.$bench);

  scout.focusManager.validateFocus(this.session.uiSessionId, 'desktop._renderView');

  // FIXME CGU: maybe include in render?
  view.htmlComp.validateLayout();
  view.htmlComp.validateRoot = true;
  view.tab = tab;
};

scout.Desktop.prototype._showForm = function(form) {
  if (this._isDialog(form)) {
    // FIXME AWE: (modal dialog) - show dialogs
  } else {
    this._selectTab(form.tab);
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

scout.Desktop.prototype.updateOutlineTab = function(content, title, subTitle) {
  if (this._outlineTab.content && this._outlineTab.content !== content) {
    if (scout.keyStrokeManager.isAdapterInstalled(this._outlineTab.content.keyStrokeAdapter)) {
      scout.keyStrokeManager.uninstallAdapter(this._outlineTab.content.keyStrokeAdapter);
    }
    this._outlineTab.content.remove();
    // Also remove storage to make sure selectTab does not restore the content
    this._outlineTab.$content = null;
    this._outlineTab.content.tab = null;
  }

  // Remove tab completely if no content is available (neither a table nor a form)
  if (!content) {
    this._removeTab(this._outlineTab);
    return;
  }

  this._outlineTab._update(content, title, subTitle);
  content.tab = this._outlineTab;
//  if (!this._isTabVisible(this._outlineTab)) {
//    this._addTab(this._outlineTab, true);
//  }
  this._updateTab(this._outlineTab);
  this._selectTab(this._outlineTab);
  // FIXME CGU: create DesktopTable or OutlineTable
  if (content instanceof scout.Table) {
    if (!scout.keyStrokeManager.isAdapterInstalled(content.keyStrokeAdapter)) {
      if (!(content.keyStrokeAdapter instanceof scout.DesktopTableKeyStrokeAdapter)) {
        content.injectKeyStrokeAdapter(new scout.DesktopTableKeyStrokeAdapter(content), this.$container);
      } else {
        scout.keyStrokeManager.installAdapter(this.$container, content.keyStrokeAdapter);
      }
    }
  }
  if (!content.rendered) {
    if (content instanceof scout.Table) {
      content.menuBar.top();
      content.menuBar.large();
    }
    content.render(this.$bench);

    //request focus on first element in new outlineTab.
    scout.focusManager.validateFocus(this.session.uiSessionId);

    // FIXME CGU: maybe include in render?
    content.htmlComp.validateLayout();
    content.htmlComp.validateRoot = true;
  }
  //request focus on first element in new outlineTab.
  scout.focusManager.validateFocus(this.session.uiSessionId, 'update');
};

scout.Desktop.prototype.changeOutline = function(outline) {
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

/* message boxes */

scout.Desktop.prototype._renderMessageBox = function(messageBox) {
  messageBox.render(this.$container);
};

scout.Desktop.prototype.onMessageBoxClosed = function(messageBox) {
  scout.arrays.remove(this.messageBoxes, messageBox);
};

/* file chooser */

scout.Desktop.prototype._renderFileChooser = function(fileChooser) {
  fileChooser.render(this.$container);
};

scout.Desktop.prototype.onFileChooserClosed = function(fileChooser) {
  scout.arrays.remove(this.fileChoosers, fileChooser);
};

/* event handling */

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

scout.Desktop.prototype._isDialog = function(form) {
  return form.displayHint === 'dialog';
};

scout.Desktop.prototype.onModelAction = function(event) {
  var form;
  if (event.type === 'formAdded') {
    this._onModelFormAdded(event);
  } else if (event.type === 'formRemoved') {
    this._onModelFormRemoved(event);
  } else if (event.type === 'formEnsureVisible') {
    form = this.session.getOrCreateModelAdapter(event.form, this);
    this._showForm(form);
  } else if (event.type === 'outlineChanged') {
    this.changeOutline(this.session.getOrCreateModelAdapter(event.outline, this));
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

/* --- INNER TYPES ---------------------------------------------------------------- */

scout.Desktop.TabAndContent = function(desktop, content, title, subTitle) {
  this._desktop = desktop;
  this.$container;
  this.$content;

  this._contentPropertyChangeListener = function(event) {
    if (event.properties.title !== undefined || event.properties.subTitle !== undefined) {
      this.title = scout.helpers.nvl(event.properties.title, this.title);
      this.subTitle = scout.helpers.nvl(event.properties.subTitle, this.subTitle);
      this._desktop._updateTab(this);
    }
  }.bind(this);

  this._update(content, title, subTitle);
};

scout.Desktop.TabAndContent.prototype._update = function(content, title, subTitle) {
  this._uninstallPropertyChangeListener();
  this.content = content;
  this.title = title;
  this.subTitle = subTitle;
  this._installPropertyChangeListener();
};

scout.Desktop.TabAndContent.prototype._installPropertyChangeListener = function() {
  if (this.content instanceof scout.ModelAdapter) {
    this.content.on('propertyChange', this._contentPropertyChangeListener);
  }
};

scout.Desktop.TabAndContent.prototype._uninstallPropertyChangeListener = function() {
  if (this.content instanceof scout.ModelAdapter) {
    this.content.off('propertyChange', this._contentPropertyChangeListener);
  }
};
