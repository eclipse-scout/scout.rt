// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopBench = function(desktop) {
  this.session = desktop.session;
  this.desktop = desktop;
};

scout.DesktopBench.prototype.render = function($parent) {
  this.$container = $parent.appendDiv('DesktopBench');
  this.$parent = $parent;

  this.taskbar = new scout.DesktopTaskbar(this.desktop);
  this.taskbar.render(this.$container);

  this.menubar = new scout.DesktopMenubar(this.$container, this.session);

  //FIXME CGU remove, just simulating offline
  $('#ViewLogo').on('click', function() {
    if (this.session.url === 'json') {
      this.session.url = 'http://localhost:123';
    } else {
      this.session.url = 'json';
    }
  }.bind(this));

  var that = this;
  this._pageTab = {
    id: that.desktop.id,
  };
  this.taskbar.addTab(this._pageTab);
  this.taskbar.selectTab(this._pageTab);
};

scout.DesktopBench.prototype.onTabSelected = function(tab, previousTab) {
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

scout.DesktopBench.prototype.activateForm = function(form) {
  if (!form.rendered) {
    this.renderForm(form);
  }
  form.$container.show();
};

scout.DesktopBench.prototype.renderForm = function(form) {
  if (this.taskbar.getToolButtonForForm(form)) {
    form.render(this.$parent);
  }
  else {
    form.render(this.$container);
  }
};

scout.DesktopBench.prototype.addPageDetailTable = function(page, table) {
  this._pageTab.content = table;
  this._pageTab.title = page.text;
  this.taskbar.updateTab(this._pageTab);

  if (this._pageTab.selected) {
    table.render(this.$container);
  }
};

scout.DesktopBench.prototype.removePageDetailTable = function(page, table) {
  this._pageTab.title = '';
  this._pageTab.content = null;
  this.taskbar.updateTab(this._pageTab);
  table.remove();
};
