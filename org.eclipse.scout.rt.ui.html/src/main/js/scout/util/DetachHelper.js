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
/**
 * When jquery's detach is used, not everything is restored when attaching the element again (using append or similar methods).
 * ScrollTop and ScrollLeft are such examples.
 * This detach helper provides methods to store and restore such data.
 */
scout.DetachHelper = function(session) {
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

/**
 * Stores the position of all scrollables that belong to this session.
 */
scout.DetachHelper.prototype._storeScrollPositions = function($container) {
  scout.scrollbars.storeScrollPositions($container, this.session);
};

/**
 * Restores the position of all scrollables that belong to this session.
 */
scout.DetachHelper.prototype._restoreScrollPositions = function($container) {
  scout.scrollbars.restoreScrollPositions($container, this.session);
};

scout.DetachHelper.prototype._storeTooltips = function($container) {
  var tooltips = scout.tooltips.find($container),
    tooltipDestroyHandler, tooltipRenderHandler;
  tooltipDestroyHandler = function(event) {
    // If tooltip will be destroyed, remove it from the list so that restore won't try to render it
    scout.arrays.remove(tooltips, event.source);
    event.source.off('render', tooltipRenderHandler);
  };
  tooltipRenderHandler = function(event) {
    // If tooltip will be rendered, destroy listener is obsolete
    event.source.off('destroy', tooltipDestroyHandler);
  };
  tooltips.forEach(function(tooltip) {
    if (!tooltip.rendered) {
      return;
    }
    tooltip.remove();
    tooltip.one('render', tooltipRenderHandler);
    tooltip.one('destroy', tooltipDestroyHandler);
  });
  $container.data('tooltips', tooltips);
};

scout.DetachHelper.prototype._restoreTooltips = function($container) {
  var tooltips = $container.data('tooltips');
  if (!tooltips) {
    return;
  }
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
