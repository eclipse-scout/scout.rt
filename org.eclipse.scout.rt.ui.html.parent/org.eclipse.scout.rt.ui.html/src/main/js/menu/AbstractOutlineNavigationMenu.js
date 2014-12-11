/**
 * The outline navigation works mostly browser-side. The navigation logic is implemented in JavaScript.
 * When a navigation button is clicked, we process that click browser-side first and send an event to
 * the server which nodes have been selected. We do that for better user experience. In a first attempt
 * the whole navigation logic was on the server, which caused a lag and flickering in the UI.
 */
scout.AbstractOutlineNavigationMenu = function() {
  scout.AbstractOutlineNavigationMenu.parent.call(this);
  this._addAdapterProperties('outline');
  this._clickBehavior = function() {};
};
scout.inherits(scout.AbstractOutlineNavigationMenu, scout.Menu);

scout.AbstractOutlineNavigationMenu.prototype.init = function(model, session) {
  scout.AbstractOutlineNavigationMenu.parent.prototype.init.call(this, model, session);
  this.outline.events.on('outlineUpdated nodesSelected', this._listener.bind(this));
};

// FIXME AWE/CGU: dieses event wird viel zu oft aufgerufen. Analysieren warum das so ist
// evtl. wäre es sinnvoller, wenn man einen listener auf einer node anhängen könnte anstatt
// auf der ganzen outline. Dann würde man gezielter notifiziert.
scout.AbstractOutlineNavigationMenu.prototype._listener = function(event) {
  var text, detailVisible, node = this._getNode(event);
  if (node) {
    this._setEnabled(this._menuEnabled(node));
    if (node.detailForm && this._isDetail(node)) {
      this._clickBehavior = this._setDetailVisible.bind(this, node);
      text = this.session.text(this._text1);
    } else {
      this._clickBehavior = this._drill.bind(this, node);
      text = this.session.text(this._text2);
    }
    $(this.$container).text(text);
  } else {
    this._setEnabled(false);
  }
};

scout.AbstractOutlineNavigationMenu.prototype._getNode = function(event) {
  if (event.node) { // outlineChanged event
    return event.node;
  } else if (event.nodeIds && event.nodeIds.length > 0) { // nodesSelected event
    return this.outline._nodeMap[event.nodeIds[0]];
  } else {
    return null;
  }
};

scout.AbstractOutlineNavigationMenu.prototype._setEnabled = function(enabled) {
  this.enabled = enabled;
  if (this.rendered) {
    this._renderEnabled(enabled);
  }
};

scout.AbstractOutlineNavigationMenu.prototype._onMenuClicked = function(event) {
  this._clickBehavior.call(this);
};

scout.AbstractOutlineNavigationMenu.prototype._setDetailVisible = function(node) {
  var detailVisible = this._toggleDetail();
  $.log.debug('show detail-' + detailVisible ? 'form' : 'table');
  node.detailFormVisible = detailVisible;
  this.outline._updateOutlineTab(node);
};

scout.AbstractOutlineNavigationMenu.prototype.dispose = function() {
  scout.AbstractOutlineNavigationMenu.parent.prototype.dispose.call(this);
  this.outline.events.off('outlineUpdated nodesSelected', this._listener);
};



