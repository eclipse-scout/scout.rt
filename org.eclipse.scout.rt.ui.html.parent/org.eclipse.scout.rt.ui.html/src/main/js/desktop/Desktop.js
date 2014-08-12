// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function() {
  scout.Desktop.parent.call(this);

  this.navigation;
  this.$bar;
  this.$tabbar;
  this.$toolbar
  this.$bench;

  this.allTabs = [];
  this.selectedTab;

  this._addAdapterProperties(['viewButtons', 'toolButtons', 'forms', 'outline', 'messageBoxes']);
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

scout.Desktop.prototype.onChildAdapterCreated = function(propertyName, adapter) {
  if (propertyName === 'viewButtons') {
    adapter.desktop = this;
  }
};


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

  this.$tabbar = this.$bar.appendDIV('taskbar-tabs');
  this.$toolbar = this.$bar.appendDIV('taskbar-tools');

  this._outlineTab = new scout.Desktop.TabAndContent();

  this.addTab(this._outlineTab);

  for (var i = 0; i < this.toolButtons.length; i++) {
    this.toolButtons[i].desktopTaskbar = this;
    this.toolButtons[i].render(this.$toolbar);
  }

  this.$bench = this.$parent.appendDIV('desktop-bench');

  this.navigation.onOutlineChanged(this.outline);

  // TODO cru: split and move
  // scout.keystrokeManager.installAdapter($parent, new scout.DesktopKeystrokeAdapter(this.navigation, this.taskbar));

  var i, form, messageBox;
  for (i = 0; i < this.forms.length; i++) {
    form = this.forms[i];
    if (!form.minimized) {
      this.addForm(form);
    }
  }

  for (i = 0; i < this.messageBoxes.length; i++) {
    messageBox = this.messageBoxes[i];
    this.addMessageBox(messageBox);
  }
};

scout.Desktop.TabAndContent = function(title, content) {
  this.title = title;
  this.content = null;
  this.$div = null;
  this.$storage = null;
};

scout.Desktop.prototype.changeOutline = function(outline) {
  if (this.outline === outline) {
    return;
  }
  this.outline = outline;
  this.navigation.onOutlineChanged(this.outline);
};

scout.Desktop.prototype._onSearchPerformed = function(event) {
  this.navigation.onSearchPerformed(event);
};

scout.Desktop.prototype.activateForm = function(form) {
  if (!form.rendered) {
    form.render(this.$bench);
  }
  form.$container.show();
};


<<<<<<< Upstream, based on branch 'develop' of ssh://cru@git.bsiag.com:29418/tools/eclipse.scout



scout.Desktop.prototype.addForm = function(form) {
  if (form.displayHint == 'view') {
    //FIXME CGU make views work like dialogs
    form.render(this._resolveViewContainer(form));
    this._outlineTab.title = form.title;
    this.updateTab(this._outlineTab);
  } else if (form.displayHint == 'dialog') {
    var previousModalForm;
    if (form.modal) {
      if (this.modalDialogStack.length > 0) {
        previousModalForm = this.modalDialogStack[this.modalDialogStack.length - 1];
        previousModalForm.disable();//FIXME CGU implement enable/disable handling (disable desktop, tab switch must be possible)
      }
      this.modalDialogStack.push(form);
    }

    form.render(this.$bench);

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

scout.Desktop.prototype.onModelAction = function(event) {
  var form, messageBox;

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
  } else if (event.type === 'messageBoxAdded') {
    messageBox = this.session.getOrCreateModelAdapter(event.messageBox, this);
    this.forms.push(form);
    this.addMessageBox(messageBox);
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

=======
// check, delete
>>>>>>> ee88fb8 html ui: tabbar

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

/* tab handling */

scout.Desktop.prototype.addTab = function(tab) {
  tab.$div = this.$tabbar.appendDIV('taskbar-tab-item', tab.title);

  tab.$div.on('click', function onTabClicked() {
      if (!tab.$div.isSelected()) {
        this.selectTab(tab);
      }
    }.bind(this));


  this.allTabs.push(tab);
  this.selectTab(tab);

};

scout.Desktop.prototype.updateTab = function(tab) {
  tab.$div.text(tab.title);
};

scout.Desktop.prototype.removeTab = function(tab) {
  scout.arrays.remove(this.allTabs, tab);

  if (tab.$div.isSelected()) {
    this.selectTab(this.allTabs[this.allTabs.length - 1]);
  }

  tab.$div.remove();
};

scout.Desktop.prototype.selectTab = function(tab) {
  if (this.selectedTab) {
    this.unselectTab(this.selectedTab);
  }

  tab.$div.select(true);
  this.selectedTab = tab;
  if (tab.$storage) {
    this.$bench.append(tab.$storage);
  }
};

scout.Desktop.prototype.unselectTab = function(tab) {
  tab.$storage = this.$bench.children();
  this.$bench.children().detach();
  tab.$div.select(false);
};

/* handling of forms */

// activate form
// enable / disable and modal handling auch beim tab löschen

/* handling of outline (form and table) and forms */

scout.Desktop.prototype.addForm = function(form) {
  var tab = new scout.Desktop.TabAndContent(form.title, form);
  this.addTab(tab);
  form.render(this.$bench);
};

scout.Desktop.prototype.removeForm = function(form) {
  // muss nicht der selektierte sein, muss man in allenTabs suchen.. zusammen mit modal, letzzte machen
  this.removeTab(this.selectedTab);
  form.remove();
};

scout.Desktop.prototype.updateOutline = function(content, title) {
  this._outlineTab.title = title;
  this._outlineTab.content = content;

  this.updateTab(this._outlineTab);
  this.selectTab(this._outlineTab);
  content.render(this.$bench);
};


/* event handling */

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

scout.Desktop.prototype.addMessageBox = function(messageBox) {
  messageBox.render(this.$bench);
};

scout.Desktop.prototype.onMessageBoxClosed = function(messageBox) {
  scout.arrays.remove(this.messageBoxes, messageBox);
};
