/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {graphics} from '../index';
import {Rectangle} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';

/**
 * Resizable makes a DOM element resizable by adding resize handlers to all edges of the given $container. This is primarily used for (modal) dialogs.
 */
export default class Resizable {

constructor($container) {
  scout.assertParameter('$container', $container);
  this.$container = $container;
  this.$window = $container.window();
  this.$resizableS = null;
  this.$resizableE = null;
  this.$resizableSE = null;
  this._context = null;

  this._mouseDownHandler = this._onMouseDown.bind(this);
  this._mouseUpHandler = this._onMouseUp.bind(this);
  this._mousemoveHandler = this._onMousemove.bind(this);
  this._resizeHandler = this._resize.bind(this);
}

/**
 * 15 fps seems to be a good value for slower browsers like firefox,
 * where it takes longer to render.
 */
static FPS = 1000 / 15;

_appendResizeHandles() {
  this.$resizableS = this.$container.appendDiv('resizable-handle resizable-s')
    .data('edge', 's')
    .on('mousedown.resizable', this._mouseDownHandler);
  this.$resizableE = this.$container.appendDiv('resizable-handle resizable-e')
    .data('edge', 'e')
    .on('mousedown.resizable', this._mouseDownHandler);
  this.$resizableSE = this.$container.appendDiv('resizable-handle resizable-se')
    .data('edge', 'se')
    .on('mousedown.resizable', this._mouseDownHandler);
  this.$resizableW = this.$container.appendDiv('resizable-handle resizable-w')
    .data('edge', 'w')
    .on('mousedown.resizable', this._mouseDownHandler);
  this.$resizableSW = this.$container.appendDiv('resizable-handle resizable-sw')
    .data('edge', 'sw')
    .on('mousedown.resizable', this._mouseDownHandler);
  this.$resizableN = this.$container.appendDiv('resizable-handle resizable-n')
    .data('edge', 'n')
    .on('mousedown.resizable', this._mouseDownHandler);
  this.$resizableNW = this.$container.appendDiv('resizable-handle resizable-nw')
    .data('edge', 'nw')
    .on('mousedown.resizable', this._mouseDownHandler);
  this.$resizableN = this.$container.appendDiv('resizable-handle resizable-ne')
    .data('edge', 'ne')
    .on('mousedown.resizable', this._mouseDownHandler);
}

init() {
  this.$container.addClass('resizable');
  this._appendResizeHandles();
  this._installRemoveHandler();
}

_installRemoveHandler() {
  this.$container.on('remove', this.destroy.bind(this));
}

destroy() {
  if (this.$resizableS) {
    this.$resizableS.remove();
    this.$resizableS = null;
  }
  if (this.$resizableE) {
    this.$resizableE.remove();
    this.$resizableE = null;
  }
  if (this.$resizableSE) {
    this.$resizableSE.remove();
    this.$resizableSE = null;
  }
  if (this.$container) {
    this.$container.removeClass('resizable');
  }
  this.$window
    .off('mouseup.resizable', this._mouseUpHandler)
    .off('mousemove.resizable', this._mousemoveHandler);
}

_onMouseDown(event) {
  var $resizable = this.$container,
    $myWindow = $resizable.window(),
    $handle = $(event.target),
    minWidth = $resizable.cssPxValue('min-width'),
    minHeight = $resizable.cssPxValue('min-height'),
    initialBounds = graphics.bounds($resizable);

  this._context = {
    initialBounds: initialBounds,
    minBounds: new Rectangle(
      initialBounds.right() - minWidth,
      initialBounds.bottom() - minHeight,
      minWidth,
      minHeight
    ),
    maxBounds: new Rectangle(
      -$resizable.cssMarginLeft(),
      -$resizable.cssMarginTop(),
      $myWindow.width() - $resizable[0].offsetLeft,
      $myWindow.height() - $resizable[0].offsetTop
    ),
    distance: [0, 0],
    edge: $handle.data('edge'),
    mousedownEvent: event
  };

  $resizable.addClass('resizable-resizing');
  this.$window
    .off('mouseup.resizable', this._mouseUpHandler)
    .off('mousemove.resizable', this._mousemoveHandler)
    .on('mouseup.resizable', this._mouseUpHandler)
    .on('mousemove.resizable', this._mousemoveHandler);
  $('iframe').addClass('dragging-in-progress');
}

_onMouseUp(event) {
  this.$container.removeClass('resizable-resizing');
  this.$window
    .off('mouseup.resizable', this._mouseUpHandler)
    .off('mousemove.resizable', this._mousemoveHandler);
  $('iframe').removeClass('dragging-in-progress');
  this._context = null;
}

_onMousemove(event) {
  var ctx = this._context,
    newBounds = ctx.initialBounds.clone(),
    distance = this._calcDistance(ctx.mousedownEvent, event);

  if (scout.isOneOf(ctx.edge, 'ne', 'e', 'se')) {
    newBounds.width = Math.max(ctx.minBounds.width,
      Math.min(ctx.maxBounds.width, ctx.initialBounds.width + distance[0]));
  } else if (scout.isOneOf(ctx.edge, 'nw', 'w', 'sw')) {
    // Resize to the left
    newBounds.x = Math.min(ctx.minBounds.x,
      Math.max(ctx.maxBounds.x, ctx.initialBounds.x + distance[0]));
    newBounds.width += ctx.initialBounds.x - newBounds.x;
  }
  if (scout.isOneOf(ctx.edge, 'sw', 's', 'se')) {
    newBounds.height = Math.max(ctx.minBounds.height,
      Math.min(ctx.maxBounds.height, ctx.initialBounds.height + distance[1]));
  } else if (scout.isOneOf(ctx.edge, 'nw', 'n', 'ne')) {
    // Resize to the bottom
    newBounds.y = Math.min(ctx.minBounds.y,
      Math.max(ctx.maxBounds.y, ctx.initialBounds.y + distance[1]));
    newBounds.height += ctx.initialBounds.y - newBounds.y;
  }
  $.throttle(this._resizeHandler, Resizable.FPS)(newBounds);
}

_resize(newBounds) {
  graphics.setBounds(this.$container, newBounds);
  this.$container.trigger('resize', {
    newBounds: newBounds,
    initialBounds: this._context.initialBounds
  });
}

_calcDistance(eventA, eventB) {
  var
    distX = eventB.pageX - eventA.pageX,
    distY = eventB.pageY - eventA.pageY;
  return [distX, distY];
}
}
