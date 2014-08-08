// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function() {
  scout.Desktop.parent.call(this);

  this.navigation;
  this.$bar;
  this.$bench;

  this.$tabs;
  this.tabs = {};
  this.tabStack = [];
  this.selectedTab;
  this.selectedTool;

  this.modalDialogStack = [];

  this._addAdapterProperties(['viewButtons', 'toolButtons', 'forms', 'outline']);
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

scout.Desktop.prototype.onChildAdapterCreated = function(propertyName, adapter) {
  if (propertyName === 'viewButtons') {
    adapter.desktop = this;
  }
};

/**
 * @override
 */
scout.Desktop.prototype._render = function($parent) {
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

  this.$tabs = this.$bar.appendDIV('taskbar-tabs');
  this.$tools = this.$bar.appendDIV('taskbar-tools');

  this._outlineTab = {
    title: 'Tabelle',
    id: this.id,
  };

  this.addTab(this._outlineTab);
  this.selectTab(this._outlineTab);

  // warum kommen 7 toolbuttons?
  for (var i = 0; i < this.toolButtons.length; i++) {
    this.toolButtons[i].desktopTaskbar = this;
    this.toolButtons[i].render(this.$tools);
  }

  this.$bench = this.$parent.appendDIV('desktop-bench');

  this.navigation.onOutlineChanged(this.outline);

  // TODO cru: split and move
  // scout.keystrokeManager.installAdapter($parent, new scout.DesktopKeystrokeAdapter(this.navigation, this.taskbar));

  var i, form;
  for (i = 0; i < this.forms.length; i++) {
    form = this.forms[i];
    if (!form.minimized) {
      this.addForm(form);
    }
  }
};

scout.Desktop.prototype._resolveViewContainer = function(form) {
  return this.$parent;
};

scout.Desktop.prototype.linkOutlineAndViewButton = function() {
  // Link button with outline (same done in desktopViewButton.js). Redundancy necessary because event order is not reliable (of button selection and outlineChanged events)
  // Only necessary due to separation of view buttons and outlines in scout model...
  // FIXME CGU find better way for scout model
  for (var i = 0; i < this.viewButtons.length; i++) {
    if (this.viewButtons[i].selected) {
      this.viewButtons[i].outline = this.outline;
    }
  }
};

scout.Desktop.prototype.changeOutline = function(outline) {
  if (this.outline === outline) {
    return;
  }
  this.outline = outline;
  this.navigation.onOutlineChanged(this.outline);
};

<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout
/**
 * @override
 */
=======
scout.Desktop.prototype._onSearchPerformed = function(event) {
  this.navigation.onSearchPerformed(event);
};

scout.Desktop.prototype.onTabSelected = function(tab, previousTab) {
  if (!tab.content) {
    return;
  }

  if (previousTab) {
    previousTab.content.$container.hide();
  }

  if (tab.type == 'form') {
    this.desktop.activateForm(tab.content);
  }
  else {
    if (!tab.content.rendered) {
      tab.content.render(this.$container);
    }
    tab.content.$container.show();
  }
};

scout.Desktop.prototype.activateForm = function(form) {
  if (!form.rendered) {
    this.renderForm(form);
  }
  form.$container.show();
};

scout.Desktop.prototype.renderForm = function(form) {
  if (this.taskbar.getToolButtonForForm(form)) {
    form.render(this.$parent);
  }
  else {
    form.render(this.$container);
  }
};




scout.Desktop.prototype.addForm = function(form) {
  if (form.displayHint == 'view') {
    //FIXME CGU make views work like dialogs
    form.render(this._resolveViewContainer(form));
  } else if (form.displayHint == 'dialog') {
    var previousModalForm;
    if (form.modal) {
      if (this.modalDialogStack.length > 0) {
        previousModalForm = this.modalDialogStack[this.modalDialogStack.length - 1];
        previousModalForm.disable();//FIXME CGU implement enable/disable handling (disable desktop, tab switch must be possible)
      }
      this.modalDialogStack.push(form);
    }

    if (this.bench) {
      this.bench.renderForm(form);
    }

    if (this.taskbar) {
      if (previousModalForm) {
        this.taskbar.formDisabled(previousModalForm);
      }
      this.taskbar.formAdded(form);
    }
  } else {
    $.log('Form displayHint not handled: ' + form.displayHint + '.');
  }
};

scout.Desktop.prototype.removeForm = function(form) {
  if (!form) {
    return;
  }

  form.remove();

  if (form.displayHint === 'dialog') {
    var previousModalForm;
    if (form.modal) {
      scout.arrays.remove(this.modalDialogStack, form);
      previousModalForm = this.modalDialogStack[this.modalDialogStack.length - 1];
      if (previousModalForm) {
        previousModalForm.enable();
        this.activateForm(previousModalForm);
      }
    }

    if (this.taskbar) {
      if (previousModalForm) {
        this.taskbar.formEnabled(previousModalForm);
      }
      this.taskbar.formRemoved(form);
    }
  }
};

scout.Desktop.prototype.activateForm = function(form) {
  //FIXME CGU send form activated
  if (!form) {
    return;
  }

  if (this.bench) {
    this.bench.activateForm(form);
  }
};

scout.Desktop.prototype.minimizeForm = function(form) {
  //FIXME CGU minimize maximize sind properties auf form, können auch vom modell gesteuert werden -> Steuerung eher über form.setMaximized
  if (form.displayHint !== 'dialog') {
    return;
  }

  form.minized = true;
  form.remove();
};

scout.Desktop.prototype.maximizeForm = function(form) {
  if (form.displayHint !== 'dialog') {
    return;
  }

  form.minized = false;
  if (this.bench) {
    this.bench.renderForm(form);
  }
};

>>>>>>> 9a0c84e html ui : first step to new design
scout.Desktop.prototype.onModelAction = function(event) {
  var form;

  if (event.type === 'formAdded') {
    form = this.session.getOrCreateModelAdapter(event.form, this);
    this.forms.push(form);
    this.addForm(form);
  } else if (event.type === 'formRemoved') {
    form = this.session.getOrCreateModelAdapter(event.form, this);
    scout.arrays.remove(this.forms, form);
    this.removeForm(form);
  } else if (event.type === 'formEnsureVisible') {
    form = this.session.getOrCreateModelAdapter(event.form, this);
    this.activateForm(form);
  } else if (event.type === 'outlineChanged') {
    this.changeOutline(this.session.getOrCreateModelAdapter(event.outline, this));
  } else if (event.type === 'searchPerformed') {
    this.navigation.onSearchPerformed(event);
  } else {
    scout.parent.prototype.onModelAction.call(this, event);
  }
};


// from taskbar
scout.Desktop.prototype.selectTab = function(tab) {
  var previousTab = this.selectedTab;
  if (previousTab) {
    this.unselectTab(previousTab);
  }

  var $tab = tab.$tab;
  $tab.select(true);
  this.selectedTab = tab;

  this.onTabSelected(tab, previousTab);
};


scout.Desktop.prototype.selectTool = function(tool) {
  var previousTool = this.selectedTool;
  if (previousTool) {
    this.unselectTool(previousTool);
  }

  var $tool = tool.$tool;
  $tool.select(true);
  this.selectedTool = tool;
  this.onToolSelected(tool, previousTool);
};




scout.Desktop.prototype.getToolButtonForForm = function(form) {
  for (var i = 0; i < this.toolButtons.length; i++) {
    if (this.toolButtons[i].form === form) {
      return this.toolButtons[i];
    }
  }
};

scout.Desktop.prototype.formActivated = function(form) {
  var toolButton = this.getToolButtonForForm(form);
  if (toolButton) {
    toolButton._setSelected(true);
    return;
  }

  var tab = this.tabs[form.id];
  this.selectTab(tab);
};


scout.Desktop.prototype.unselectTab = function(tab) {
  var $tab = tab.$tab;
  $tab.select(false);
  tab.selected = false;
  this.selectedTab = null;
};

scout.Desktop.prototype.formAdded = function(form) {
  var tab;
  var toolButton = this.getToolButtonForForm(form);
  if (toolButton) {
    toolButton._setSelected(true);
    return;
  }

 tab = {id: form.id, title: form.title, content: form, type: 'form'};
 this.addTab(tab);
 this.selectTab(tab);
};

scout.Desktop.prototype.addTab = function(tab) {
  var $tab = this.$tabs.appendDiv(undefined, 'taskbar-tab-item', tab.title);
  tab.$tab = $tab;
  this.tabs[tab.id] = tab;
  this.tabStack.push(tab);

  $tab.on('click', onTabClicked.bind(this));

  function onTabClicked() {
    if ($tab.isSelected()) {
      return;
    }

    this.selectTab(tab);
  }
};

scout.Desktop.prototype.updateTab = function(tab) {
  tab.$tab.text = tab.title;
};

scout.Desktop.prototype.formRemoved = function(form) {
  var toolButton = this.getToolButtonForForm(form);
  if (toolButton) {
      toolButton._setSelected(false);
    return;
  }

  var tab = this.tabs[form.id];
  this.removeTab(tab);
};

scout.Desktop.prototype.removeTab = function(tab) {
  delete this.tabs[tab.id];
  scout.arrays.remove(this.tabStack, tab);
  tab.$tab.remove();

  if (tab.selected) {
    this.selectedTab = null;
    if (this.tabStack.length > 0) {
      this.selectTab(this.tabStack[this.tabStack.length-1]);
    }
  }
};

scout.Desktop.prototype.formDisabled = function(form) {
  var toolButton, $tab;
  toolButton = this.getToolButtonForForm(form);
  if (toolButton) {
    $tab = toolButton.$container;
  } else {
    $tab = this.tabs[form.id].$tab;
  }

  $tab.addClass('disabled');
};

scout.Desktop.prototype.formEnabled = function(form) {
  var toolButton, $tab;
  toolButton = this.getToolButtonForForm(form);
  if (toolButton) {
    $tab = toolButton.$container;
  } else {
    $tab = this.tabs[form.id].$tab;
  }

  $tab.removeClass('disabled');
};

scout.Desktop.prototype.toolButtonSelected = function(toolButton, selected) {
  var form = toolButton.form;
  if (!form) {
    return;
  }

  if (selected) {
    if (form.minimized) {
      this.desktop.maximizeForm(form);
    }
    this.desktop.activateForm(form);
  } else {
    //minimize the form if the already selected button is clicked again
    if (form === this.formOfClickedButton && !form.minimized) {
      this.desktop.minimizeForm(form);
    }
  }

  if (selected) {
    this.unselectToolButtons(toolButton);
  }
};

scout.Desktop.prototype.unselectToolButtons = function(toolButton) {
  for (var i = 0; i < this.toolButtons.length; i++) {
    var otherToolButton = this.toolButtons[i];
    if (otherToolButton !== toolButton) {
      otherToolButton._setSelected(false);
    }
  }
};

scout.Desktop.prototype.addPageDetailTable = function(page, table) {
  this._outlineTab.content = table;
  this._outlineTab.title = page.text;
  this.updateTab(this._outlineTab);

  if (this._outlineTab.selected || true) {
    table.render(this.$bench);
  }
};

scout.Desktop.prototype.removePageDetailTable = function(page, table) {
  this._outlineTab.title = '';
  this._outlineTab.content = null;
  this.updateTab(this._outlineTab);
  table.remove();
};
