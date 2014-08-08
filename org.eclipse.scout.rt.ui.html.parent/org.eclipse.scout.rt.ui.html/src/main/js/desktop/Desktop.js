// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function() {
  scout.Desktop.parent.call(this);
  this.navigation;
  this.bench;
  this.taskbar;
  this.menubar;
  this.layout;
  this._addAdapterProperties(['viewButtons', 'toolButtons']);
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

scout.Desktop.prototype.init = function(model, session) {
  scout.Desktop.parent.prototype.init.call(this, model, session);
  this.outline = session.getOrCreateModelAdapter(model.outline, this);
};

scout.Desktop.prototype.onChildAdapterCreated = function(propertyName, adapter) {
  //Link with desktop
  if (propertyName === 'viewButtons') {
    adapter.desktop = this;
  }
};

/**
 * @override
 */
scout.Desktop.prototype._render = function($parent) {
  var viewbar,
    marginTop = 0,
    marginRight = 0;

  this.$parent = $parent;

  this.layout = new scout.BorderLayout(marginTop, marginRight, 'desktop-area');
  this.navigation = new scout.DesktopNavigation(this, $parent);
  this.layout.register(this.navigation.$div, 'W');

  this.bench = new scout.DesktopBench(this);
  this.bench.render($parent);
  this.layout.register(this.bench.$container, 'C');
  this.taskbar = this.bench.taskbar;
  this.menubar = this.bench.menubar;

  this.navigation.renderOutline();
  this.layout.layout();

  $parent.attr('tabIndex', 0);
  $parent.css('outline', 'none'); //FIXME CGU what is this for?
  scout.keystrokeManager.installAdapter($parent, new scout.DesktopKeystrokeAdapter(this.navigation, this.bench));
  // Input focus is initially outside $parent, therefore keystrokes would not  work until
  // the user clicks $parent with the mouse. Therefore we set the focus manually.
  // FIXME BSH portlets?
  // FIXME BSH When closing a form, the focus gets lost - why? And how to fix that?
  $parent.focus();

  scout.Desktop.parent.prototype._render.call(this, $parent);
};

scout.Desktop.prototype.addPageDetailTable = function(page, table) {
  this.bench.addPageDetailTable(page, table);
};

scout.Desktop.prototype.removePageDetailTable = function(page, table) {
  this.bench.removePageDetailTable(page, table);
};

/**
 * @override
 */
scout.Desktop.prototype.onMenusUpdated = function(group, menus) {
  this.menubar.updateItems(group, menus);
};

/**
 * @override
 */
scout.Desktop.prototype._resolveViewContainer = function(form) {
  return this.bench.$container;
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

scout.Desktop.prototype._onSearchPerformed = function(event) {
  this.navigation.onSearchPerformed(event);
};

/**
 * @override
 */
scout.Desktop.prototype.onModelAction = function(event) {
  if (event.type === 'outlineChanged') {
    this.changeOutline(this.session.getOrCreateModelAdapter(event.outline, this));
  } else if (event.type === 'searchPerformed') {
    this._onSearchPerformed(event);
  } else {
    scout.Desktop.parent.prototype.onModelAction.call(this, event);
  }
};
