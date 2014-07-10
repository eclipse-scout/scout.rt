// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function() {
  scout.Desktop.parent.call(this);
  this.menubar;
  this.taskbar;
  this.tree;
  this.bench;
  this.layout;
  this._addAdapterProperties('viewButtons');
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

  // create all 4 containers
  if (this.viewButtons) {
    viewbar = new scout.DesktopViewButtonBar(this, $parent, this.viewButtons);
    marginTop = viewbar.$div.outerHeight();
  }

  this.menubar = new scout.DesktopMenubar($parent, this.session);
  marginTop += this.menubar.$container.outerHeight();

  this.layout = new scout.BorderLayout(marginTop, marginRight, 'desktop-area');
  if (this.outline) {
    this.tree = new scout.DesktopTreeContainer(this, $parent, this.outline);
    this.layout.register(this.tree.$div, 'W');
    this.showOrHideDesktopTree(); //FIXME CGU maybe refactor, don't create desktoptree container if not necessary
  }

  this.bench = new scout.DesktopBench($parent, this.session);
  this.layout.register(this.bench.$container, 'C');

  if (this.toolButtons) {
    this.taskbar = new scout.DesktopTaskbar(this);
    this.taskbar.render($parent);
    this.layout.register(this.taskbar.$div, 'E');
  }

  if (this.tree) {
    this.tree.renderTree();
  }

  this.layout.layout();

  if (viewbar || this.taskbar || this.tree) {
    $parent.attr('tabIndex', 0);
    $parent.css('outline', 'none');
    scout.keystrokeManager.installAdapter($parent, new scout.DesktopKeystrokeAdapter(viewbar, this.taskbar, this.tree));
    // Input focus is initially outside $parent, therefore keystrokes would not  work until
    // the user clicks $parent with the mouse. Therefore we set the focus manually.
    // FIXME BSH portlets?
    // FIXME BSH When closing a form, the focus gets lost - why? And how to fix that?
    $parent.focus();
  }

  //FIXME CGU remove, just simulating offline
  $('#ViewLogo').on('click', function(){
    if(this.session.url==='json') {
      this.session.url='http://localhost:123';
    }
    else {
      this.session.url='json';
    }
  }.bind(this));

  scout.Desktop.parent.prototype._render.call(this, $parent);
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

/**
 * If the outline contains no nodes, the desktop tree area will be detached.
 */
scout.Desktop.prototype.showOrHideDesktopTree = function() {
  if (!this.tree) {
    return;
  }

  if (this.tree.detached) {
    if (this.tree.desktopTree.nodes.length > 0) {
      this.tree.$div.insertBefore(this.bench.$container);
      this.tree.detached = false;
      this.layout.unregister(this.tree.$div);
      this.layout.layout();
    }
  } else {
    if (this.tree.desktopTree.nodes.length === 0) {
      this.tree.$div.detach();
      this.tree.detached = true;
      this.layout.register(this.tree.$div, 'W');
      this.layout.layout();
    }
  }
};

scout.Desktop.prototype.linkOutlineAndViewButton = function() {
  //Link button with outline (same done in desktopViewButton.js). Redundancy necessary because event order is not reliable (of button selection and outlineChanged events)
  //Only necessary due to separation of view buttons and outlines in scout model...
  //FXME CGU find better way for scout model
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
  this.tree.onOutlineChanged(this.outline);
  this.showOrHideDesktopTree();
};

/**
 * @override
 */
scout.Desktop.prototype.onModelAction = function(event) {
  if (event.type_ == 'outlineChanged') {
    this.changeOutline(this.session.getOrCreateModelAdapter(event.outline, this));
  } else {
    scout.Desktop.parent.prototype.onModelAction.call(this, event);
  }
};
