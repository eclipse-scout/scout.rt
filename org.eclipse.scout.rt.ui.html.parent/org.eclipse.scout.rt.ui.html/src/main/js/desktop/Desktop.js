// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function() {
  scout.Desktop.parent.call(this);
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

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
    viewbar = new scout.DesktopViewButtonBar($parent, this.viewButtons, this.session);
    marginTop = viewbar.$div.outerHeight();
  }

  this.menu = new scout.DesktopMenu($parent, this.session);
  marginTop += this.menu.$container.outerHeight();


  this.layout = new scout.BorderLayout(marginTop, marginRight, 'desktop-area');
  if (this.outline) {
    this.tree = new scout.DesktopTreeContainer($parent, this.outline, this.session);
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

  scout.Desktop.parent.prototype._render.call(this, $parent);
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

/**
 * @override
 */
scout.Desktop.prototype.onModelAction = function(event) {
  if (event.type_ == 'outlineChanged') {
    this.tree.onOutlineChanged(event.outline);
    this.showOrHideDesktopTree();
  } else {
    scout.Desktop.parent.prototype.onModelAction.call(this, event);
  }
};
