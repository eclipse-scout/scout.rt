// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function(model, session) {
  this.base(model, session);
};
scout.Desktop.inheritsFrom(scout.BaseDesktop);

/**
 * @override
 */
scout.Desktop.prototype._render = function($parent) {
  var views, tools, tree,
    marginTop=0, marginRight=0;

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

  var layout = new scout.BorderLayout(marginTop, marginRight, 'desktop-area');
  if (this.model.outline) {
    tree = new scout.DesktopTreeContainer($parent, this.model.outline, this.session);
    layout.register(tree.$div, 'W', true);
  }

  var bench = new scout.DesktopBench($parent, this.session);
  layout.register(bench.$container, 'C');

  layout.layout();

  this._bench = bench;

  if (views || tools || tree) {
    scout.keystrokeManager.addAdapter(new scout.DesktopKeystrokeAdapter(views, tools, tree));
  }

  if (tree) {
    this.tree = tree;
    this.tree.attachModel();
  }

  this.base.prototype._render.call(this, $parent);
};

/**
 * @override
 */
scout.Desktop.prototype._resolveViewContainer = function(form) {
  return this._bench.$container;
};
