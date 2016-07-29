/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TreeAdapter = function() {
  scout.TreeAdapter.parent.call(this);
  this._addAdapterProperties(['menus', 'keyStrokes']);
};
scout.inherits(scout.TreeAdapter, scout.ModelAdapter);

scout.TreeAdapter.prototype._sendNodesSelected = function(nodeIds, debounceSend) {
  var eventData = {
    nodeIds: nodeIds
  };

  // send delayed to avoid a lot of requests while selecting
  // coalesce: only send the latest selection changed event for a field
  this._send('nodesSelected', eventData, debounceSend ? 250 : 0, function(previous) {
    return this.id === previous.id && this.type === previous.type;
  });
};

scout.TreeAdapter.prototype._onWidgetNodeClicked = function(event) {
  this._send({
    node: event.node.id
  });
};

scout.TreeAdapter.prototype._onWidgetNodesSelected = function(event) {
  var nodeIds = this.widget._nodesToIds(this.widget.selectedNodes);
  this._sendNodesSelected(nodeIds, event.debounce);
};

scout.TreeAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'nodesSelected') {
    this._onWidgetNodesSelected(event);
  } else if (event.type === 'nodeClicked') {
    this._onWidgetNodeClicked(event);
  } else {
    scout.TreeAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
