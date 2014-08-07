// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopTaskbar = function(desktop) {
  this.desktop = desktop;
  this.toolButtons = desktop.toolButtons;
  this.$div;
  this.$tabs;
  this.tabs = {};
  this.tabStack = [];
  this.selectedTab;
};

scout.DesktopTaskbar.prototype.render = function($desktop) {
  this.$parent = $desktop;
  this.$div = $desktop.appendDiv(undefined, 'desktop-taskbar');

  this.$tabs = this.$div.appendDiv(undefined, 'tabs');

  this.$tools = this.$div.appendDiv(undefined, 'tools');
  for (var i = 0; i < this.toolButtons.length; i++) {
    this.toolButtons[i].desktopTaskBar = this;
    this.toolButtons[i].render(this.$tools);
  }

  this.$div.appendDiv('ViewLogo').delay(1000).animateAVCSD('width', 40, null, null, 1000);
};

scout.DesktopTaskbar.prototype.getToolButtonForForm = function(form) {
  for (var i = 0; i < this.toolButtons.length; i++) {
    if (this.toolButtons[i].form === form) {
      return this.toolButtons[i];
    }
  }
};

scout.DesktopTaskbar.prototype.formActivated = function(form) {
  var toolButton = this.getToolButtonForForm(form);
  if (toolButton) {
    toolButton._setSelected(true);
    return;
  }

  var tab = this.tabs[form.id];
  this.selectTab(tab);
};

scout.DesktopTaskbar.prototype.selectTab = function(tab) {
  var previousTab = this.selectedTab;
  if (previousTab) {
    this.unselectTab(previousTab);
  }

  var $tab = tab.$tab;
  $tab.select(true);
  tab.selected = true;
  this.selectedTab = tab;
  this.desktop.bench.onTabSelected(tab, previousTab);
};

scout.DesktopTaskbar.prototype.unselectTab = function(tab) {
  var $tab = tab.$tab;
  $tab.select(false);
  tab.selected = false;
  this.selectedTab = null;
};

scout.DesktopTaskbar.prototype.formAdded = function(form) {
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

scout.DesktopTaskbar.prototype.addTab = function(tab) {
  var $tab = this.$tabs.appendDiv(undefined, 'tab taskbar-item', tab.title);
  tab.$tab = $tab;
  this.tabs[tab.id] = tab;
  this.tabStack.push(tab);

  $tab.attr('data-icon', '\uf096');
  $tab.on('click', onTabClicked.bind(this));

  function onTabClicked() {
    if ($tab.isSelected()) {
      return;
    }

    this.selectTab(tab);
  }
};

scout.DesktopTaskbar.prototype.updateTab = function(tab) {
  tab.$tab.text = tab.title;
};

scout.DesktopTaskbar.prototype.formRemoved = function(form) {
  var toolButton = this.getToolButtonForForm(form);
  if (toolButton) {
      toolButton._setSelected(false);
    return;
  }

  var tab = this.tabs[form.id];
  this.removeTab(tab);
};

scout.DesktopTaskbar.prototype.removeTab = function(tab) {
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

scout.DesktopTaskbar.prototype.formDisabled = function(form) {
  var toolButton, $tab;
  toolButton = this.getToolButtonForForm(form);
  if (toolButton) {
    $tab = toolButton.$container;
  } else {
    $tab = this.tabs[form.id].$tab;
  }

  $tab.addClass('disabled');
};

scout.DesktopTaskbar.prototype.formEnabled = function(form) {
  var toolButton, $tab;
  toolButton = this.getToolButtonForForm(form);
  if (toolButton) {
    $tab = toolButton.$container;
  } else {
    $tab = this.tabs[form.id].$tab;
  }

  $tab.removeClass('disabled');
};

scout.DesktopTaskbar.prototype.toolButtonSelected = function(toolButton, selected) {
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

scout.DesktopTaskbar.prototype.unselectToolButtons = function(toolButton) {
  for (var i = 0; i < this.toolButtons.length; i++) {
    var otherToolButton = this.toolButtons[i];
    if (otherToolButton !== toolButton) {
      otherToolButton._setSelected(false);
    }
  }
};
