/**
 * When jquery's detach is used, not everything is restored when attaching the element again (using append or similar methods).
 * ScrollTop and ScrollLeft are such examples.
 * This detach helper provides methods to store and restore such data.
 */
scout.DetachHelper = function() {
  this._$scrollables = [];
};

scout.DetachHelper.prototype.beforeDetach = function($container) {
  this._storeScrollPositions($container);
};

scout.DetachHelper.prototype.afterAttach = function($container) {
  this._restoreScrollPositions($container);
};

scout.DetachHelper.prototype._storeScrollPositions = function($container) {
  var scrollTop, scrollLeft;
  if (!this._$scrollables) {
    return;
  }

  for (var i = 0; i < this._$scrollables.length; i++) {
    if ($.contains($container[0], this._$scrollables[i][0])) {
      scrollTop = this._$scrollables[i].scrollTop();
      this._$scrollables[i].data('scrollTop', scrollTop);
      scrollLeft = this._$scrollables[i].scrollLeft();
      this._$scrollables[i].data('scrollLeft', this._$scrollables[i].scrollLeft());

      $.log.debug('Stored scroll position for ' + this._$scrollables[i].attr('class') + '. Top: ' + scrollTop +'. Left: ' + scrollLeft);
    }
  }
};

scout.DetachHelper.prototype._restoreScrollPositions = function($container) {
  var scrollTop, scrollLeft;
  if (!this._$scrollables) {
    return;
  }

  for (var i = 0; i < this._$scrollables.length; i++) {
    if ($.contains($container[0], this._$scrollables[i][0])) {
      scrollTop = this._$scrollables[i].data('scrollTop');
      if (scrollTop) {
        this._$scrollables[i].scrollTop(scrollTop);
        this._$scrollables[i].removeData('scrollTop');
      }
      scrollLeft = this._$scrollables[i].data('scrollLeft');
      if (scrollLeft) {
        this._$scrollables[i].scrollLeft(scrollLeft);
        this._$scrollables[i].removeData('scrollLeft');
      }
      $.log.debug('Restored scroll position for ' + this._$scrollables[i].attr('class') + '. Top: ' + scrollTop +'. Left: ' + scrollLeft);
    }
  }
};

scout.DetachHelper.prototype.pushScrollable = function($container) {
  if ($container.hasClass('scrollable')) {
    // In case of non native scrollbars (-> Scrollbar.js), use the parent container since this is the one with the scrolling properties
    $container = $container.parent();
  }
  this._$scrollables.push($container);
  $.log.debug('Scrollable added: ' + $container.attr('class') + '. New length: ' + this._$scrollables.length);
};

scout.DetachHelper.prototype.removeScrollable = function($container) {
  var initLength = this._$scrollables.length;
  if ($container.hasClass('scrollable')) {
    $container = $container.parent();
  }
  scout.arrays.$remove(this._$scrollables, $container);
  $.log.debug('Scrollable removed: ' + $container.attr('class') + '. New length: ' + this._$scrollables.length);

  if (initLength === this._$scrollables.length) {
    throw new Error('scrollable could not be removed. Potential memory leak. ' + $container.attr('class'));
  }
};
