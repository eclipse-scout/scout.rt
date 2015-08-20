/**
 * When jquery's detach is used, not everything is restored when attaching the element again (using append or similar methods).
 * ScrollTop and ScrollLeft are such examples.
 * This detach helper provides methods to store and restore such data.
 */
scout.DetachHelper = function(session) {
  this._$scrollables = [];
  this.session = session;
  this._defaultOptions = {
    storeScrollPositions: true,
    storeTooltips: true,
    storeFocus: true
  };
};

scout.DetachHelper.prototype.beforeDetach = function($container, options) {
  options = $.extend(this._defaultOptions, options || {});
  if (options.storeScrollPositions) {
    this._storeScrollPositions($container);
  }
  if (options.storeTooltips) {
    this._storeTooltips($container);
  }

  this._storeFocusAndFocusContext($container, options);
};

scout.DetachHelper.prototype.afterAttach = function($container) {
  this._restoreScrollPositions($container);
  this._restoreTooltips($container);
  this._restoreFocusAndFocusContext($container);
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

      $.log.debug('Stored scroll position for ' + this._$scrollables[i].attr('class') + '. Top: ' + scrollTop + '. Left: ' + scrollLeft);
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
      // Also make sure that scroll bar is up to date
      // Introduced for use case: Open large table page, edit entry, press f5
      // -> outline tab gets rendered, scrollbar gets updated with set timeout, outline tab gets detached -> update event never had any effect because it executed after detaching (due to set timeout)
      scout.scrollbars.update(this._$scrollables[i]);
      $.log.debug('Restored scroll position for ' + this._$scrollables[i].attr('class') + '. Top: ' + scrollTop + '. Left: ' + scrollLeft);
    }
  }
};

scout.DetachHelper.prototype.pushScrollable = function($container) {
  if (this._$scrollables.indexOf($container) > -1) {
    // already pushed
    return;
  }
  this._$scrollables.push($container);
  $.log.debug('Scrollable added: ' + $container.attr('class') + '. New length: ' + this._$scrollables.length);
};

scout.DetachHelper.prototype.removeScrollable = function($container) {
  var initLength = this._$scrollables.length;
  scout.arrays.$remove(this._$scrollables, $container);
  $.log.debug('Scrollable removed: ' + $container.attr('class') + '. New length: ' + this._$scrollables.length);

  if (initLength === this._$scrollables.length) {
    throw new Error('scrollable could not be removed. Potential memory leak. ' + $container.attr('class'));
  }
};

scout.DetachHelper.prototype._storeTooltips = function($container) {
  var tooltips = scout.Tooltip.findTooltips($container);
  tooltips.forEach(function(tooltip) {
    tooltip.remove();
  });
  $container.data('tooltips', tooltips);
};

scout.DetachHelper.prototype._restoreTooltips = function($container) {
  var tooltips = $container.data('tooltips');
  tooltips.forEach(function(tooltip) {
    tooltip.render(tooltip.$parent);
  });
  $container.data('tooltips', null);
};

scout.DetachHelper.prototype._storeFocusAndFocusContext = function($container, options) {
  // Get the currently focused element, which is either the given $container, or one of its children. (debugging hint: ':focus' does not work if debugging with breakpoints).
  var focusedElement = ($container.is(':focus') ? $container : $container.find(':focus'))[0];

  if (options.storeFocus) {
    if (focusedElement) {
      $container.data('focus', focusedElement);
    } else {
      $container.removeData('focus');
    }
  }

  if (this.session.focusManager.isFocusContextInstalled($container)) {
    this.session.focusManager.uninstallFocusContext($container);
    $container.data('focusContext', true);
  } else {
    $container.removeData('focusContext');

    if (focusedElement) {
      // Currently, the focus is on an element which is about to be detached. Hence, it must be set onto another control, which will not removed. Otherwise, the HTML body would be focused, because the currently focused element is removed from the DOM.
      // JQuery implementation detail: the detach operation does not trigger a 'remove' event.
      this.session.focusManager.validateFocus(scout.filters.outsideFilter($container)); // exclude the container or any of its child elements to gain focus.
    }
  }
};

scout.DetachHelper.prototype._restoreFocusAndFocusContext = function($container) {
  var focusedElement = $container.data('focus');
  var focusContext = $container.data('focusContext');

  if (focusContext) {
    this.session.focusManager.installFocusContext($container, focusedElement || scout.focusRule.AUTO);
  } else if (focusedElement) {
    this.session.focusManager.requestFocus(focusedElement);
  } else {
    this.session.focusManager.validateFocus();
  }
};
