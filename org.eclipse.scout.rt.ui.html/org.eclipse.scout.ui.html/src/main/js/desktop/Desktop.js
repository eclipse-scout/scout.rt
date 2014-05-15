// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function(session, model) {
  this.base(session, model);
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
    views = new scout.DesktopViewButtonBar(this.session, $parent, this.model.viewButtons);
    marginTop = views.$div.outerHeight();
  }
  if (this.model.toolButtons) {
    tools = new scout.DesktopToolButton(this.session, this.model.toolButtons);
    tools.render($parent);
    marginRight = tools.$div.outerWidth();
  }

  var layout = new scout.BorderLayout(marginTop, marginRight, 'desktop-area');
  if (this.model.outline) {
    tree = new scout.DesktopTreeContainer(this.session, $parent, this.model.outline);
    layout.register(tree.$div, 'W', true);
  }

  var bench = new scout.DesktopBench(this.session, $parent);
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
