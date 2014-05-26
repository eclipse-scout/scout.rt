// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function(model, session) {
  scout.Desktop.parent.call(this, model, session);
};
scout.inherits(scout.Desktop, scout.BaseDesktop);

/**
 * @override
 */
scout.Desktop.prototype._render = function($parent) {
  var views, tools, tree,
    marginTop = 0,
    marginRight = 0;

  this.$parent = $parent;

  // create all 4 containers
  if (this.model.viewButtons) {
    views = new scout.DesktopViewButtonBar($parent, this.model.viewButtons, this.session);
    marginTop = views.$div.outerHeight();
  }
  if (this.model.toolButtons) {
    tools = new scout.DesktopToolButton(this.model.toolButtons, this.session);
    tools.render($parent);
    marginRight = tools.$div.outerWidth();
  }

  this.layout = new scout.BorderLayout(marginTop, marginRight, 'desktop-area');
  if (this.model.outline) {
    tree = new scout.DesktopTreeContainer($parent, this.model.outline, this.session);
    this.tree = tree;
    this.layout.register(tree.$div, 'W');
    this.showOrHideDesktopTree(); //FIXME CGU maybe refactor, don't create desktoptree container if not necessary
  }

  var bench = new scout.DesktopBench($parent, this.session);
  this.layout.register(bench.$container, 'C');

  this.layout.layout();

  this._bench = bench;

  if (views || tools || tree) {
    scout.keystrokeManager.addAdapter(new scout.DesktopKeystrokeAdapter(views, tools, tree));
  }

  if (tree) {
    this.tree.attachModel();
  }

  scout.Desktop.parent.prototype._render.call(this, $parent);
};

/**
 * @override
 */
scout.Desktop.prototype._resolveViewContainer = function(form) {
  return this._bench.$container;
};

/**
 * If the outline contains no nodes, the desktop tree area will be detached.
 */
scout.Desktop.prototype.showOrHideDesktopTree = function() {
  if (!this.tree) {
    return;
  }

  if (this.tree.detached) {
    if (this.tree.desktopTree.model.nodes.length > 0) {
      this.tree.$div.insertBefore(this._bench.$container);
      this.tree.detached = false;
      this.layout.unregister(this.tree.$div);
      this.layout.layout();
    }
  } else {
    if (this.tree.desktopTree.model.nodes.length === 0) {
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
scout.Desktop.prototype.onModelCreate = function(event) {
  if (event.objectType == "Outline") {
    this.tree.onOutlineCreated(event);
    this.showOrHideDesktopTree();
  } else {
    scout.Desktop.parent.prototype.onModelCreate.call(this, event);
  }
};

/**
 * @override
 */
scout.Desktop.prototype.onModelAction = function(event) {
  if (event.type_ == 'outlineChanged') {
    this.tree.onOutlineChanged(event.outlineId);
    this.showOrHideDesktopTree();
  } else {
    scout.Desktop.parent.prototype.onModelAction.call(this, event);
  }
};
