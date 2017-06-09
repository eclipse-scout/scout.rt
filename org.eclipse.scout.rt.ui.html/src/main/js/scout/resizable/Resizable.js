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
 * Resizable makes a DOM element resizable by adding resize handlers to the
 * E, SE and S of the given $container. This is primarily used for (modal) dialogs.
 */
scout.Resizable = function($container) {
  scout.assertParameter('$container', $container);
  this.$container = $container;
  this._context = null;

  this._mouseDownHandler = this._onMouseDown.bind(this);
  this._mouseUpHandler = this._onMouseUp.bind(this);
  this._mousemoveHandler = this._onMousemove.bind(this);
  this._resizeHandler = this._resize.bind(this);
};

/**
 * 15 fps seems to be a good value for slower browsers like firefox,
 * where it takes longer to render.
 */
scout.Resizable.FPS = 1000 / 15;

scout.Resizable.prototype._appendResizeHandles = function() {
  this.$container.appendDiv('resizable-handle resizable-s')
    .data('axis', 'y')
    .on('mousedown.resizable', this._mouseDownHandler);
  this.$container.appendDiv('resizable-handle resizable-e')
    .data('axis', 'x')
    .on('mousedown.resizable', this._mouseDownHandler);
  this.$container.appendDiv('resizable-handle resizable-se resizable-gripsmall-se')
    .data('axis', 'xy')
    .on('mousedown.resizable', this._mouseDownHandler);
};

scout.Resizable.prototype.init = function(event) {
  this.$container.addClass('resizable');
  this._appendResizeHandles();
};

scout.Resizable.prototype._onMouseDown = function(event) {
  var $resizable = this.$container,
    $myWindow = $resizable.window(),
    $handle = $(event.target),
    minWidth = $resizable.cssPxValue('min-width'),
    minHeight = $resizable.cssPxValue('min-height');

  this._context = {
    initialSize: [
      $resizable.outerWidth(),
      $resizable.outerHeight()
    ],
    minSize: [
      minWidth,
      minHeight
    ],
    maxSize: [
      $myWindow.width() - $resizable[0].offsetLeft,
      $myWindow.height() - $resizable[0].offsetTop
    ],
    distance: [0, 0],
    axis: $handle.data('axis'),
    mousedownEvent: event
  };

  $resizable.addClass('resizable-resizing');
  $resizable.document()
    .on('mouseup.resizable', this._mouseUpHandler)
    .on('mousemove.resizable', this._mousemoveHandler);
};

scout.Resizable.prototype._onMouseUp = function(event) {
  this.$container.removeClass('resizable-resizing');
  this.$container.document()
    .off('mouseup.resizable', this._mouseUpHandler)
    .off('mousemove.resizable', this._mousemoveHandler);
  this._context = null;
};

scout.Resizable.prototype._onMousemove = function(event) {
  var newSize = [0, 0],
    ctx = this._context,
    distance = this._calcDistance(ctx.mousedownEvent, event);

  if (ctx.axis.indexOf('x') > -1) {
    newSize[0] = Math.max(ctx.minSize[0],
        Math.min(ctx.maxSize[0], ctx.initialSize[0] + distance[0]));

  }
  if (ctx.axis.indexOf('y') > -1) {
    newSize[1] = Math.max(ctx.minSize[1],
        Math.min(ctx.maxSize[1], ctx.initialSize[1] + distance[1]));
  }
  $.throttle(this._resizeHandler, scout.Resizable.FPS)(newSize);
};

scout.Resizable.prototype._resize = function(newSize) {
  if (newSize[0] > 0) {
    this.$container.cssWidth(newSize[0]);
  }
  if (newSize[1] > 0) {
    this.$container.cssHeight(newSize[1]);
  }
  this.$container.trigger('resize', {
    newSize: newSize,
    initialSize: this._context.initialSize
  });
};

scout.Resizable.prototype._calcDistance = function(eventA, eventB) {
  var
    distX = eventB.pageX - eventA.pageX,
    distY = eventB.pageY - eventA.pageY;
  return [distX, distY];
};

