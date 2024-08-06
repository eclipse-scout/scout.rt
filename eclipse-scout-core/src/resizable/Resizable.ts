/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, EnumObject, graphics, InitModelOf, Insets, keys, ObjectWithType, Point, Rectangle, ResizableModel, scout, SomeRequired} from '../index';
import $ from 'jquery';
import MouseDownEvent = JQuery.MouseDownEvent;
import MouseUpEvent = JQuery.MouseUpEvent;
import MouseMoveEvent = JQuery.MouseMoveEvent;

/**
 * Resizable makes a DOM element resizable by adding resize handlers to all edges of the given model.$container.
 * The following events are triggered on the DOM element:
 * - resizeStep: triggered during resizing.
 * - resizeEnd: triggered when resizing ends.
 */
export class Resizable implements ResizableModel, ObjectWithType {
  declare model: ResizableModel;
  declare initModel: SomeRequired<this['model'], '$container'>;

  objectType: string;
  modes: ResizableMode[];
  boundaries: Insets;
  useOverlay: boolean;
  $container: JQuery;
  $window: JQuery<Window>;
  $resizableS: JQuery;
  $resizableE: JQuery;
  $resizableSE: JQuery;
  $resizableW: JQuery;
  $resizableSW: JQuery;
  $resizableN: JQuery;
  $resizableNW: JQuery;
  $resizableNE: JQuery;
  $resizingOverlay: JQuery;

  protected _context: ResizableContext;
  protected _mouseDownHandler: (event: MouseDownEvent) => void;
  protected _mouseUpHandler: (event: MouseUpEvent) => void;
  protected _mouseMoveHandler: (event: MouseMoveEvent) => void;
  protected _keyDownHandler: (event: KeyboardEvent) => void;
  protected _resizeHandler: (newBounds: Rectangle) => void;

  constructor() {
    this._mouseDownHandler = this._onMouseDown.bind(this);
    this._mouseUpHandler = this._onMouseUp.bind(this);
    this._mouseMoveHandler = this._onMouseMove.bind(this);
    this._keyDownHandler = this._onKeyDown.bind(this);
    this._resizeHandler = this._resize.bind(this);
  }

  static MODES = {
    SOUTH: 's',
    EAST: 'e',
    WEST: 'w',
    NORTH: 'n'
  } as const;

  init(model: InitModelOf<Resizable>) {
    scout.assertParameter('model', model);
    scout.assertParameter('$container', model.$container);
    this.$container = model.$container;
    this.$window = model.$container.window();
    this.useOverlay = scout.nvl(model.useOverlay, false);
    this.$container.addClass('resizable');
    this._appendResizeHandles();
    this.setModes(model.modes);
    this.setBoundaries(model.boundaries);
    this._installRemoveHandler();
  }

  setModes(modes?: ResizableMode[]) {
    let ensuredModes = modes || [Resizable.MODES.SOUTH, Resizable.MODES.EAST, Resizable.MODES.WEST, Resizable.MODES.NORTH];
    if (arrays.equals(ensuredModes, this.modes)) {
      return;
    }
    this.modes = ensuredModes;
    this._calculateResizeHandlersVisibility();
  }

  setBoundaries(boundaries?: Insets) {
    this.boundaries = $.extend(new Insets(), boundaries);
    if (this._boundaryValueSet(this.boundaries.left)) {
      this.boundaries.left -= this.$container.cssMarginLeft();
    }
    if (this._boundaryValueSet(this.boundaries.right)) {
      this.boundaries.right -= this.$container.cssMarginRight();
    }
    if (this._boundaryValueSet(this.boundaries.top)) {
      this.boundaries.top -= this.$container.cssMarginTop();
    }
    if (this._boundaryValueSet(this.boundaries.bottom)) {
      this.boundaries.bottom -= this.$container.cssMarginBottom();
    }
  }

  protected _appendResizeHandles() {
    this.$resizableS = this._appendResizeHandle('s');
    this.$resizableE = this._appendResizeHandle('e');
    this.$resizableSE = this._appendResizeHandle('se');
    this.$resizableW = this._appendResizeHandle('w');
    this.$resizableSW = this._appendResizeHandle('sw');
    this.$resizableN = this._appendResizeHandle('n');
    this.$resizableNW = this._appendResizeHandle('nw');
    this.$resizableNE = this._appendResizeHandle('ne');
  }

  protected _appendResizeHandle(edge: ResizableEdge): JQuery {
    return this.$container.appendDiv(`resizable-handle resizable-${edge}`)
      .data('edge', edge)
      .on('mousedown', this._mouseDownHandler);
  }

  protected _calculateResizeHandlersVisibility() {
    this.$resizableS.setVisible(this._hasMode(Resizable.MODES.SOUTH));
    this.$resizableE.setVisible(this._hasMode(Resizable.MODES.EAST));
    this.$resizableSE.setVisible(this._hasMode(Resizable.MODES.SOUTH) && this._hasMode(Resizable.MODES.EAST));
    this.$resizableW.setVisible(this._hasMode(Resizable.MODES.WEST));
    this.$resizableSW.setVisible(this._hasMode(Resizable.MODES.SOUTH) && this._hasMode(Resizable.MODES.WEST));
    this.$resizableN.setVisible(this._hasMode(Resizable.MODES.NORTH));
    this.$resizableNW.setVisible(this._hasMode(Resizable.MODES.NORTH) && this._hasMode(Resizable.MODES.WEST));
    this.$resizableNE.setVisible(this._hasMode(Resizable.MODES.NORTH) && this._hasMode(Resizable.MODES.EAST));
  }

  protected _hasMode(mode: ResizableMode): boolean {
    return this.modes.some(m => m === mode);
  }

  protected _installRemoveHandler() {
    this.$container.on('remove', this.destroy.bind(this));
  }

  destroy() {
    this.$resizableS.remove();
    this.$resizableE.remove();
    this.$resizableSE.remove();
    this.$resizableW.remove();
    this.$resizableSW.remove();
    this.$resizableN.remove();
    this.$resizableNW.remove();
    this.$resizableNE.remove();
    this._cleanup();
  }

  protected _onMouseDown(event: MouseDownEvent) {
    let $resizable = this.$container;
    let $myWindow = $resizable.window();
    let $handle = $(event.target);
    let minWidth = $resizable.cssMinWidth();
    let minHeight = $resizable.cssMinHeight();
    let maxWidth = $resizable.cssMaxWidth();
    let maxHeight = $resizable.cssMaxHeight();
    let $offsetParent = $resizable.offsetParent();
    let initialBounds = graphics.bounds($resizable, {exact: true})
      .translate(new Point($offsetParent[0].scrollLeft, $offsetParent[0].scrollTop));

    this._context = {
      initialBounds: initialBounds,
      currentBounds: initialBounds.clone(),
      minBounds: new Rectangle(
        initialBounds.right() - minWidth,
        initialBounds.bottom() - minHeight,
        minWidth,
        minHeight
      ),
      maxBounds: new Rectangle(
        Math.max(-$resizable.cssMarginLeft(), initialBounds.right() - maxWidth),
        Math.max(-$resizable.cssMarginTop(), initialBounds.bottom() - maxHeight),
        Math.min($myWindow.width() - $resizable[0].offsetLeft, maxWidth),
        Math.min($myWindow.height() - $resizable[0].offsetTop, maxHeight)
      ),
      distance: [0, 0],
      edge: $handle.data('edge'),
      mousedownEvent: event
    };

    if (this.useOverlay) {
      this.$resizingOverlay = $resizable.parent().appendDiv('resizing-overlay');
      this.$resizingOverlay.css('border-radius', $resizable.css('border-radius'));
      this.$resizingOverlay.css('border-color', $resizable.css('--resizable-color'));
      graphics.setBounds(this.$resizingOverlay, initialBounds);
    }

    $resizable.addClass('resizing');
    this.$window
      .off('mouseup', this._mouseUpHandler)
      .off('mousemove', this._mouseMoveHandler)
      .on('mouseup', this._mouseUpHandler)
      .on('mousemove', this._mouseMoveHandler)
      .body().addClass(`${this._context.edge}-resize`);
    this.$window[0].removeEventListener('keydown', this._keyDownHandler, true);
    this.$window[0].addEventListener('keydown', this._keyDownHandler, true);
    $('iframe').addClass('dragging-in-progress');
  }

  protected _onMouseUp(event: MouseUpEvent) {
    this.finish();
  }

  /**
   * Finishes the resizing by cleaning up all temporary states and triggering the `resizeEnd` event with the initial and new bounds.
   */
  finish() {
    this._cleanup();
    this._resizeEnd();
    this._context = null;
  }

  /**
   * Cancels the resizing if it is in progress.
   */
  cancel() {
    if (!this._context) {
      return;
    }
    this._context.currentBounds = this._context.initialBounds;
    this.finish();
  }

  protected _onKeyDown(event: KeyboardEvent) {
    if (event.which === keys.ESC) {
      this.cancel();
      event.stopPropagation();
    }
  }

  protected _cleanup() {
    this.$container.removeClass('resizing');
    if (this.$resizingOverlay) {
      this.$resizingOverlay.remove();
      this.$resizingOverlay = null;
    }
    this.$window
      .off('mouseup', this._mouseUpHandler)
      .off('mousemove', this._mouseMoveHandler);
    this.$window[0].removeEventListener('keydown', this._keyDownHandler, true);
    if (this._context) {
      this.$window.body().removeClass(`${this._context.edge}-resize`);
    }
    $('iframe').removeClass('dragging-in-progress');
  }

  protected _onMouseMove(event: MouseMoveEvent) {
    let newBounds = this._computeBounds(event);
    if (newBounds) {
      this._resizeHandler(newBounds);
    }
  }

  protected _computeBounds(event: MouseMoveEvent) {
    let ctx = this._context;
    let newBounds = ctx.initialBounds.clone();
    let distance = this._calcDistance(ctx.mousedownEvent, event);
    if (scout.isOneOf(ctx.edge, 'ne', 'e', 'se')) {
      newBounds.width = Math.max(ctx.minBounds.width, Math.min(ctx.maxBounds.width, ctx.initialBounds.width + distance[0]));
    } else if (scout.isOneOf(ctx.edge, 'nw', 'w', 'sw')) {
      // Resize to the left
      newBounds.x = Math.min(ctx.minBounds.x, Math.max(ctx.maxBounds.x, ctx.initialBounds.x + distance[0]));
      newBounds.width += ctx.initialBounds.x - newBounds.x;
    }
    if (scout.isOneOf(ctx.edge, 'sw', 's', 'se')) {
      newBounds.height = Math.max(ctx.minBounds.height, Math.min(ctx.maxBounds.height, ctx.initialBounds.height + distance[1]));
    } else if (scout.isOneOf(ctx.edge, 'nw', 'n', 'ne')) {
      // Resize to the bottom
      newBounds.y = Math.min(ctx.minBounds.y, Math.max(ctx.maxBounds.y, ctx.initialBounds.y + distance[1]));
      newBounds.height += ctx.initialBounds.y - newBounds.y;
    }
    return newBounds;
  }

  protected _resize(newBounds: Rectangle) {
    this._cropToBoundaries(newBounds);
    if (this._context.currentBounds.equals(newBounds)) {
      return;
    }
    this._context.currentBounds = newBounds;
    if (this.useOverlay) {
      graphics.setBounds(this.$resizingOverlay, newBounds);
    } else {
      graphics.setBounds(this.$container, newBounds);
    }
    // 'resize' would be a better name, but it is a native event triggered by the browser when the window is resized
    // To not accidentally trigger event handlers listening for window resizes, another name is used.
    this.$container.trigger('resizeStep', {
      newBounds: newBounds,
      initialBounds: this._context.initialBounds
    });
  }

  protected _resizeEnd() {
    if (this._context.currentBounds.equals(this._context.initialBounds)) {
      return;
    }
    this.$container.trigger('resizeEnd', {
      newBounds: this._context.currentBounds,
      initialBounds: this._context.initialBounds
    });
  }

  protected _cropToBoundaries(newBounds: Rectangle) {
    if (this._boundaryValueSet(this.boundaries.left) && newBounds.x > this.boundaries.left) {
      newBounds.width -= (this.boundaries.left - newBounds.x);
      newBounds.x = this.boundaries.left;
    }
    if (this._boundaryValueSet(this.boundaries.right) && (newBounds.x + newBounds.width) < this.boundaries.right) {
      newBounds.width = this.boundaries.right - newBounds.x;
    }
    if (this._boundaryValueSet(this.boundaries.top) && newBounds.y > this.boundaries.top) {
      newBounds.height -= (this.boundaries.top - newBounds.y);
      newBounds.y = this.boundaries.top;
    }
    if (this._boundaryValueSet(this.boundaries.bottom) && (newBounds.y + newBounds.height) < this.boundaries.bottom) {
      newBounds.height = this.boundaries.bottom - newBounds.y;
    }
  }

  protected _boundaryValueSet(value: number): boolean {
    return value > 0;
  }

  protected _calcDistance(eventA: MouseDownEvent, eventB: MouseMoveEvent): number[] {
    let
      distX = eventB.pageX - eventA.pageX,
      distY = eventB.pageY - eventA.pageY;
    return [distX, distY];
  }
}

export interface ResizableContext {
  initialBounds: Rectangle;
  currentBounds: Rectangle;
  minBounds: Rectangle;
  maxBounds: Rectangle;
  distance: number[];
  edge: ResizableEdge;
  mousedownEvent: MouseDownEvent;
}

export type ResizableMode = EnumObject<typeof Resizable.MODES>;
export type ResizableEdge = 'n' | 'ne' | 'e' | 'se' | 's' | 'sw' | 'w' | 'nw';
