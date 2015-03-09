scout.TreeKeyStrokeAdapter = function(field) {
  scout.TreeKeyStrokeAdapter.parent.call(this, field);
  var that = this;

  this._field = field;
  this.ctrl= false;
  this.alt= false;
  this.shift= false;
  this.meta= false;

  this.keyStrokes.push({
    accept: function(event) {
      return event &&
        $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.LEFT, scout.keys.RIGHT]) >= 0 &&
        event.ctrlKey === that.ctrl && event.altKey === that.alt && event.shiftKey === that.shift && event.metaKey === that.meta;
    },
    handle: function(event) {
      var $targetNode, targetNode,
        keycode = event.which,
        $currentNode = that._field.$selectedNodes().eq(0),
        currentNode = $currentNode.data('node');

      if (keycode === scout.keys.UP) {
        if ($currentNode.length === 0) {
          $targetNode = that._field.$nodes().last();
          targetNode = $targetNode.data('node');
        } else {
          $targetNode = $currentNode.prev('.tree-item');
          targetNode = $targetNode.data('node');
        }
        if (targetNode) {
          that._field.setNodesSelected(targetNode);
        }
      } else if (keycode === scout.keys.DOWN) {
        if ($currentNode.length === 0) {
          $targetNode = that._field.$nodes().first();
          targetNode = $targetNode.data('node');
        } else {
          $targetNode = $currentNode.next('.tree-item');
          targetNode = $targetNode.data('node');
        }
        if (targetNode) {
          that._field.setNodesSelected(targetNode);
        }
      } else if (currentNode && keycode === scout.keys.LEFT) {
        if (currentNode.expanded) {
          that._field.setNodeExpanded(currentNode, false);
        } else if (currentNode.parentNode) {
          that._field.setNodesSelected(currentNode.parentNode);
        }
      } else if (currentNode && keycode === scout.keys.RIGHT) {
        if (!currentNode.expanded && !currentNode.leaf) {
          that._field.setNodeExpanded(currentNode, true);
        } else if (currentNode.childNodes.length > 0) {
          that._field.setNodesSelected(currentNode.childNodes[0]);
        }
      }
    },
    bubbleUp: false
  });
};
scout.inherits(scout.TreeKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.TreeKeyStrokeAdapter.prototype.drawKeyBox = function() {
  if (this.keyBoxDrawn) {
    return;
  }
  //TODO nbu draw.
  this.keyBoxDrawn = true;
};
