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

  this._allTabs = [];
  this._selectedTab;
  this.selectedTool;

  this._addAdapterProperties(['viewButtons', 'toolButtons', 'forms', 'outline', 'messageBoxes']);
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

scout.Desktop.prototype.onChildAdapterCreated = function(propertyName, adapter) {
  if (propertyName === 'viewButtons') {
    adapter.desktop = this;
  }
  if (propertyName === 'toolButtons') {
    adapter.desktop = this;
  }};

scout.Desktop.prototype._render = function($parent) {
  var i, form, messageBox;

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
  this.$toolContainer = this.$parent.appendDIV('desktop-tool-container');

  this._outlineTab = new scout.Desktop.TabAndContent();

  this._addTab(this._outlineTab);

  for (i = 0; i < this.toolButtons.length; i++) {
    //this.toolButtons[i].desktop  = this;
    this.toolButtons[i].render(this.$toolbar);
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


  // TODO cru: split and move
  // scout.keystrokeManager.installAdapter($parent, new scout.DesktopKeystrokeAdapter(this.navigation, this.taskbar));
};

scout.Desktop.TabAndContent = function(title, content) {
  this.title = title;
  this.content = null;
  this.$div = null;
  this.$storage = null;
};


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

/* tab handling */

scout.Desktop.prototype._addTab = function(tab) {
  tab.$div = this.$tabbar.appendDIV('taskbar-tab-item', tab.title);

  tab.$div.on('click', function onTabClicked() {
      if (tab !== this._selectedTab) {
        this._selectTab(tab);
      }
    }.bind(this));


  this._allTabs.push(tab);
  this._selectTab(tab);

};

scout.Desktop.prototype._updateTab = function(tab) {
  tab.$div.text(tab.title);
};

scout.Desktop.prototype._removeTab = function(tab) {
  scout.arrays.remove(this._allTabs, tab);

  if (tab.$div.isSelected()) {
    this._selectTab(this._allTabs[this._allTabs.length - 1]);
  }

  tab.$div.remove();
};

scout.Desktop.prototype._selectTab = function(tab) {
  if (this._selectedTab) {
    this._unselectTab(this._selectedTab);
  }

  tab.$div.select(true);
  this._selectedTab = tab;
  if (tab.$storage) {
    this.$bench.append(tab.$storage);
  }
};

scout.Desktop.prototype._unselectTab = function(tab) {
  tab.$storage = this.$bench.children();
  this.$bench.children().detach();
  tab.$div.select(false);
};

/* handling of forms */

scout.Desktop.prototype._addForm = function(form) {
  if (form.title == "Telefon") return;

  var tab = new scout.Desktop.TabAndContent(form.title, form);
  this._addTab(tab);
  form.render(this.$bench);
  form.tab = tab;
};

scout.Desktop.prototype._removeForm = function(form) {
  this._removeTab(form.tab);
  form.remove();
};

scout.Desktop.prototype._showForm = function(form) {
  this._selectTab(form.tab);
};

/* communication with outline */

scout.Desktop.prototype.updateOutline = function(content, title) {
  this._outlineTab.title = title;
  this._outlineTab.content = content;

  this._updateTab(this._outlineTab);
  this._selectTab(this._outlineTab);
  content.render(this.$bench);
};

scout.Desktop.prototype.changeOutline = function(outline) {
  if (this.outline === outline) {
    return;
  }
  this.outline = outline;
  this.navigation.onOutlineChanged(this.outline);
};

/* message boxes */

scout.Desktop.prototype.addMessageBox = function(messageBox) {
  messageBox.render(this.$bench);
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
  } else {
    scout.parent.prototype.onModelAction.call(this, event);
  }
};

scout.Desktop.prototype.onMessageBoxClosed = function(messageBox) {
  scout.arrays.remove(this.messageBoxes, messageBox);
};