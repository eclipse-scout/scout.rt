scout.Graph = function() {
  scout.Graph.parent.call(this);

  this.$container;
  this.graphRenderer;
  this._updateGraphTimeoutId;
};
scout.inherits(scout.Graph, scout.ModelAdapter);

scout.Graph.prototype._init = function(model) {
  scout.Graph.parent.prototype._init.call(this, model);
  this._updateGraphRenderer();
};

scout.Graph.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('graph');

  // Install layout to update graph when parent size changes
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.GraphLayout(this));
  this.htmlComp.pixelBasedSizing = false;

  this.updateGraph({
    debounce: 200
  });
};

scout.Graph.prototype._renderProperties = function() {
  scout.Graph.parent.prototype._renderProperties.call(this);
  // No properties here, because all properties are already handled by the updateGraph() call in _render()
};

scout.Graph.prototype._remove = function() {
  if (this.graphRenderer) {
    this.graphRenderer.remove();
  }
  this.$container.remove();
  this.$container = null;
};

scout.Graph.prototype._renderAutoColor = function() {
  this.updateGraph();
};

scout.Graph.prototype._renderGraphModel = function() {
  this.updateGraph({
    debounce: 200
  });
};

scout.Graph.prototype._renderEnabled = function() {
  this.updateGraph();
};

scout.Graph.prototype._renderVisible = function() {
  this.$container.setVisible(this.visible);
  if (this.rendered) {
    var htmlCompParent = this.htmlComp.getParent();
    if (htmlCompParent) { // may be null if $container is detached
      htmlCompParent.invalidateLayoutTree();
    }
  }
};

scout.Graph.prototype._renderClickable = function() {
  this.$container.toggleClass('clickable', this.clickable);
  this.updateGraph();
};

scout.Graph.prototype._renderModelHandlesClick = function() {
  // nothing to render, property is only used in handleValueClicked()
};

scout.Graph.prototype._renderAnimated = function() {
  // nothing to render, property is only used in the graphs rendering methods
};

/**
 * @param opts
 *   [rebuild] default true
 *   [debounce] default 0
 */
scout.Graph.prototype.updateGraph = function(opts) {
  opts = opts || {};

  clearTimeout(this._updateGraphTimeoutId);

  var updateGraphImplFn = updateGraphImpl.bind(this);

  var doDebounce = (opts.debounce === true || typeof opts.debounce === 'number');
  if (doDebounce) {
    if (typeof opts.debounce === 'number') {
      this._updateGraphTimeoutId = setTimeout(updateGraphImplFn, opts.debounce);
    } else {
      this._updateGraphTimeoutId = setTimeout(updateGraphImplFn);
    }
  } else {
    updateGraphImplFn();
  }

  // ---- Helper functions -----

  function updateGraphImpl() {
    if (this.graphRenderer) {
      if (scout.helpers.nvl(opts.rebuild, true) || !this.graphRenderer.rendered) {
        this.graphRenderer.remove();
        this.graphRenderer.render();
      } else {
        this.graphRenderer.touch();
      }
    }
  }
};

scout.Graph.prototype._updateGraphRenderer = function() {
  this.graphRenderer && this.graphRenderer.remove();
  this.graphRenderer = new scout.NetworkGraphRenderer(this);
};

// TODO BSH Implement
scout.Graph.prototype.handleNodeAction = function(event) {
  if (this.modelHandlesClick) {
    var data = event.data || {};
    this._send('nodeAction', {
      node: event.nodeId
    });
  }
  this.trigger('nodeAction', event);
};

// TODO BSH Implement
scout.Graph.prototype.handleAppLinkAction = function(event) {
  var $target = $(event.target);
  var ref = $target.data('ref');
  this._send('appLinkAction', {
    ref: ref
  });
};
