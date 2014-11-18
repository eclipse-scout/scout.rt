/* TODO cru
 *  enable / disable and modal handling? auch beim tab löschen
 */

scout.Desktop = function() {
  scout.Desktop.parent.call(this);

  this.navigation;
  this.$bar;
  this.$tabbar;
  this.$toolbar;
  this.$bench;
  this.$toolContainer;
  this.$glasspane;

  this._allTabs = [];
  this._selectedTab;

  /**
   * TODO AWE/CGU: (forms) wird nun auch als 'activeForm' verwendet (siehe TableKeystrokeAdapter.js)
   * Wahrscheinlich müssen wir das refactoren und eine activeForm property verwenden
   * Diese Property muss mit dem Server synchronisiert werden, damit auch das server-seitige desktop.getActiveForm() stimmt.
   * Auch im zusammenhang mit focus-handling nochmals überdenken.
   */
  this.selectedTool;

  this._addAdapterProperties(['viewButtons', 'actions', 'forms', 'outline', 'searchOutline', 'messageBoxes', 'addOns']);
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
  var i, form, messageBox, action;

  this.$parent = $parent;

  this.navigation = new scout.DesktopNavigation(this);
  this.navigation.render($parent);

  this.$bar = $parent.appendDIV('desktop-taskbar');
  this.$bar.appendDIV('taskbar-logo')
    .delay(1000)
    .animateAVCSD('width', 40, null, null, 1000)
    .on('click', function() {
      if (this.session.url === 'json') {
        this.session.url = 'http://localhost:123';
      } else {
        this.session.url = 'json';
      }
    }.bind(this));

  this.$tabbar = this.$bar.appendDIV('taskbar-tabs');
  this.$toolbar = this.$bar.appendDIV('taskbar-tools');
  this.$bench = this.$parent.appendDIV('desktop-bench');
  new scout.HtmlComponent(this.$bench, this.session);
  this.$toolContainer = this.$parent.appendDIV('desktop-tool-container').hide();

  this._outlineTab = new scout.Desktop.TabAndContent();

  for (i = 0; i < this.addOns.length; i++) {
    this.addOns[i].render($parent);
  }

  // we set the menuStyle property to render a menu with a different style
  // depending on where the menu is located (taskbar VS menubar).
  for (i = 0; i < this.actions.length; i++) {
    action = this.actions[i];
    action.menuStyle = 'taskbar';
    action.render(this.$toolbar);
  }
  if(action){
    action.$container.addClass('last');
  }

  this.navigation.onOutlineChanged(this.outline);

  for (i = 0; i < this.forms.length; i++) {
    form = this.forms[i];
    this._addForm(form);
  }

  for (i = 0; i < this.messageBoxes.length; i++) {
    messageBox = this.messageBoxes[i];
    this.addMessageBox(messageBox);
  }

  $(window).on('resize', this.onResize.bind(this));

  // TODO CRU: split and move
  // scout.keystrokeManager.installAdapter($parent, new scout.DesktopKeystrokeAdapter(this.navigation, this.taskbar));
};

scout.Desktop.prototype.onResize = function() {
  if (this.forms.length > 0) {
    var i, form;
    for (i = 0; i < this.forms.length; i++) {
      form = this.forms[i];
      if (form.rendered) {
        form.onResize();
      }
    }
  }

  if (this._outlineTab.content) {
    this._outlineTab.content.onResize();
  }
};

scout.Desktop.TabAndContent = function(content, title, subtitle) {
  this.content = content;
  this.title = title;
  this.subtitle = subtitle;
  this.$container;
  this.$storage;
};

scout.Desktop.TabAndContent.prototype._update = function(content, title, subtitle) {
  this.content = content;
  this.title = title;
  this.subtitle = subtitle;
};

/* Tab-handling */

// TODO AWE/CGU: (dialoge) Über modale Dialoge sprechen, v.a. über folgenden Fall:
// - Outline-Tab > Autrag Xyz > Auftrag bearbeiten
// - Tab geht auf, nun wieder auf Outline-Tab wechseln und nochmals Auftrag bearbeiten
// - ein weiterer Tab geht auf, kann man beliebig wiederholen
// Lösungen besprechen. Eine Möglichkeit wäre, bei Klick auf Auftrag bearbeiten in den
// bereits geöffneten Tab/Dialog zu wecheseln. Oder das Menü-Item disablen.

scout.Desktop.prototype._addTab = function(tab, prepend) {
  tab.$container = $.makeDIV('taskbar-tab-item').
     append($.makeDIV('title', tab.title).attr('title', tab.title)).
     append($.makeDIV('subtitle', 'Bearbeiten')); // TODO AWE: (desktop) sub-titel für forms
     // müsste abhängig von Handler gesetzt werden.

  if (prepend) {
    tab.$container.prependTo(this.$tabbar);
  } else {
    tab.$container.appendTo(this.$tabbar);
  }

  tab.$container.on('click', function onTabClicked() {
    if (tab !== this._selectedTab) {
      this._selectTab(tab);
    }
  }.bind(this));

  this._allTabs.push(tab);
  this._selectTab(tab);
};

scout.Desktop.prototype._isTabVisible = function(tab) {
  return this._allTabs.indexOf(tab) >= 0;
};

scout.Desktop.prototype._updateTab = function(tab) {
  var setTitle = function(selector, title) {
    var $e = tab.$container.children(selector);
    if (title) {
      $e.text(title).attr('title', title).setVisible(true);
    } else {
      $e.setVisible(false);
    }
  };
  setTitle('.title', tab.title);
  setTitle('.subtitle', tab.subtitle);
};

scout.Desktop.prototype._removeTab = function(tab) {
  scout.arrays.remove(this._allTabs, tab);

  if (tab.$container.isSelected() && this._allTabs.length > 0) {
    this._selectTab(this._allTabs[this._allTabs.length - 1]);
  }

  tab.$container.remove();
};

scout.Desktop.prototype._selectTab = function(tab) {
  if (this._selectedTab) {
    this._unselectTab(this._selectedTab);
  }

  tab.$container.select(true);
  this._selectedTab = tab;
  if (tab.$storage && tab.$storage.length > 0) {
    this.$bench.append(tab.$storage);

    //If the parent has been resized while the content was not visible, the content has the wrong size -> update
    var htmlComp = scout.HtmlComponent.get(tab.$storage);
    var htmlParent = htmlComp.getParent();
    htmlComp.setSize(htmlParent.getSize());
  }
};

scout.Desktop.prototype._unselectTab = function(tab) {
  tab.$storage = this.$bench.children();
  var $children = this.$bench.children();
  scout.Tooltip.removeTooltips($children);
  $children.detach();
  tab.$container.select(false);
};

/* handling of forms */

scout.Desktop.prototype._addForm = function(form) {
  var tab = new scout.Desktop.TabAndContent(form, form.title, '%Modus%');
  this._addTab(tab);
  form.render(this.$bench);

  // FIXME CGU: maybe include in render?
  form.htmlComp.layout();
  form.htmlComp.validateRoot = true;
  form.tab = tab;
};

scout.Desktop.prototype._removeForm = function(form) {
  this._removeTab(form.tab);
  form.remove();
};

scout.Desktop.prototype._showForm = function(form) {
  this._selectTab(form.tab);
};

scout.Desktop.prototype._openUrlInBrowser = function(event) {
  $.log.debug('(Desktop#_openUrlInBrowser) path=' + event.path + ' targetUrl=' + event.targetUrl);
  if (event.path) {
    window.open(event.path);
  }
};

/* communication with outline */

scout.Desktop.prototype.updateOutlineTab = function(content, title, subtitle) {
  if (this._outlineTab.content && this._outlineTab.content !== content) {
    this._outlineTab.content.remove();
    // Also remove storage to make sure selectTab does not restore the content
    this._outlineTab.$storage = null;
  }

  // Remove tab completely if no content is available (neither a table nor a form)
  if (!content) {
    this._removeTab(this._outlineTab);
    return;
  }

  if (!this._isTabVisible(this._outlineTab)) {
    this._addTab(this._outlineTab, true);
  }

  this._outlineTab._update(content, title, subtitle);
  this._updateTab(this._outlineTab);
  this._selectTab(this._outlineTab);

  if (!content.rendered) {
    var selectedNodes = this.outline.getSelectedModelNodes();
    if (selectedNodes.length > 0) {
      content.staticMenus = [new scout.OutlineNavigateUpMenu(this.outline, selectedNodes[0])];
    }
    content.render(this.$bench);

    // FIXME CGU: maybe include in render?
    content.htmlComp.layout();
    content.htmlComp.validateRoot = true;
  }
};

scout.Desktop.prototype.changeOutline = function(outline) {
  this.outline = outline;
  this.navigation.onOutlineChanged(this.outline);
};

/* message boxes */

scout.Desktop.prototype.addMessageBox = function(messageBox) {
  if (!this.$glasspane) {
    this.$glasspane = this.$parent.appendDIV('glasspane');
  }
  messageBox.render(this.$glasspane);
};

scout.Desktop.prototype.onMessageBoxClosed = function(messageBox) {
  scout.arrays.remove(this.messageBoxes, messageBox);
  if (this.messageBoxes.length === 0) {
    this.$glasspane.remove();
    this.$glasspane = null;
  }
};

/* event handling */

scout.Desktop.prototype.onModelAction = function(event) {
  var form;
  if (event.type === 'formAdded') {
    form = this.session.getOrCreateModelAdapter(event.form, this);
    this.forms.push(form);
    this._addForm(form);
  } else if (event.type === 'formRemoved') {
    form = this.session.getOrCreateModelAdapter(event.form, this);
    scout.arrays.remove(this.forms, form);
    this._removeForm(form);
  } else if (event.type === 'formEnsureVisible') {
    form = this.session.getOrCreateModelAdapter(event.form, this);
    this._showForm(form);
  } else if (event.type === 'outlineChanged') {
    this.changeOutline(this.session.getOrCreateModelAdapter(event.outline, this));
  } else if (event.type === 'searchPerformed') {
    this.navigation.onSearchPerformed(event);
  } else if (event.type === 'messageBoxAdded') {
    this.addMessageBox(this.session.getOrCreateModelAdapter(event.messageBox, this));
  } else if (event.type === 'openUrlInBrowser') {
    this._openUrlInBrowser(event);
  } else {
    scout.Desktop.parent.prototype.onModelAction.call(this, event);
  }
};
