/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, EnumObject, graphics, InitModelOf, Insets, ObjectWithType, Rectangle, ResizableModel, scout, SomeRequired} from '../index';
import $ from 'jquery';
import MouseDownEvent = JQuery.MouseDownEvent;
import MouseUpEvent = JQuery.MouseUpEvent;
import MouseMoveEvent = JQuery.MouseMoveEvent;

/**
 * Resizable makes a DOM element resizable by adding resize handlers to all edges of the given model.$container. This is primarily used for (modal) dialogs.
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
  protected _mousemoveHandler: (event: MouseMoveEvent) => void;
  protected _resizeHandler: (newBounds: Rectangle) => void;

  constructor(model: InitModelOf<Resizable>) {
    scout.assertParameter('model', model);
    scout.assertParameter('$container', model.$container);
    this.$container = model.$container;
    this.$window = model.$container.window();
    this.setModes(model.modes);
    this.setBoundaries(model.boundaries);
    this.useOverlay = scout.nvl(model.useOverlay, false);
    this.$resizableS = null;
    this.$resizableE = null;
    this.$resizableSE = null;
    this.$resizableW = null;
    this.$resizableSW = null;
    this.$resizableN = null;
    this.$resizableNW = null;
    this.$resizableNE = null;
    this._context = null;

    this._mouseDownHandler = this._onMouseDown.bind(this);
    this._mouseUpHandler = this._onMouseUp.bind(this);
    this._mousemoveHandler = this._onMousemove.bind(this);
    this._resizeHandler = this._resize.bind(this);
  }

  static MODES = {
    SOUTH: 's',
    EAST: 'e',
    WEST: 'w',
    NORTH: 'n'
  } as const;

  /**
   * 15 fps seems to be a good value for slower browsers like firefox,
   * where it takes longer to render.
   */
  static FPS = 1000 / 15;

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
    this.$resizableNE = this.$container.appendDiv('resizable-handle resizable-ne')
      .data('edge', 'ne')
      .on('mousedown.resizable', this._mouseDownHandler);
    this._calculateResizeHandlersVisibility();
  }

  protected _calculateResizeHandlersVisibility() {
    if (this.$resizableS) {
      this.$resizableS.setVisible(this._hasMode(Resizable.MODES.SOUTH));
    }
    if (this.$resizableE) {
      this.$resizableE.setVisible(this._hasMode(Resizable.MODES.EAST));
    }
    if (this.$resizableSE) {
      this.$resizableSE.setVisible(this._hasMode(Resizable.MODES.SOUTH) && this._hasMode(Resizable.MODES.EAST));
    }
    if (this.$resizableW) {
      this.$resizableW.setVisible(this._hasMode(Resizable.MODES.WEST));
    }
    if (this.$resizableSW) {
      this.$resizableSW.setVisible(this._hasMode(Resizable.MODES.SOUTH) && this._hasMode(Resizable.MODES.WEST));
    }
    if (this.$resizableN) {
      this.$resizableN.setVisible(this._hasMode(Resizable.MODES.NORTH));
    }
    if (this.$resizableNW) {
      this.$resizableNW.setVisible(this._hasMode(Resizable.MODES.NORTH) && this._hasMode(Resizable.MODES.WEST));
    }
    if (this.$resizableNE) {
      this.$resizableNE.setVisible(this._hasMode(Resizable.MODES.NORTH) && this._hasMode(Resizable.MODES.EAST));
    }
  }

  protected _hasMode(mode: ResizableMode): boolean {
    return this.modes.some(m => m === mode);
  }

  init() {
    this.$container.addClass('resizable');
    this._appendResizeHandles();
    this._installRemoveHandler();
  }

  protected _installRemoveHandler() {
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
    if (this.$resizableW) {
      this.$resizableW.remove();
      this.$resizableW = null;
    }
    if (this.$resizableSW) {
      this.$resizableSW.remove();
      this.$resizableSW = null;
    }
    if (this.$resizableN) {
      this.$resizableN.remove();
      this.$resizableN = null;
    }
    if (this.$resizableNW) {
      this.$resizableNW.remove();
      this.$resizableNW = null;
    }
    if (this.$resizableNE) {
      this.$resizableNE.remove();
      this.$resizableNE = null;
    }
    if (this.$container) {
      this.$container.removeClass('resizable');
    }
    this.$window
      .off('mouseup.resizable', this._mouseUpHandler)
      .off('mousemove.resizable', this._mousemoveHandler);
  }

  protected _onMouseDown(event: MouseDownEvent) {
    let $resizable = this.$container,
      $myWindow = $resizable.window(),
      $handle = $(event.target),
      minWidth = $resizable.cssMinWidth(),
      minHeight = $resizable.cssMinHeight(),
      maxWidth = $resizable.cssMaxWidth(),
      maxHeight = $resizable.cssMaxHeight(),
      initialBounds = graphics.bounds($resizable, {exact: true});

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
      graphics.setBounds(this.$resizingOverlay, initialBounds);
    }

    $resizable.addClass('resizing');
    this.$window
      .off('mouseup.resizable', this._mouseUpHandler)
      .off('mousemove.resizable', this._mousemoveHandler)
      .on('mouseup.resizable', this._mouseUpHandler)
      .on('mousemove.resizable', this._mousemoveHandler);
    $('iframe').addClass('dragging-in-progress');
  }

  protected _onMouseUp(event: MouseUpEvent) {
    this.$container.removeClass('resizing');
    if (this.$resizingOverlay) {
      this.$resizingOverlay.remove();
      this.$resizingOverlay = null;
    }
    this.$window
      .off('mouseup.resizable', this._mouseUpHandler)
      .off('mousemove.resizable', this._mousemoveHandler);
    $('iframe').removeClass('dragging-in-progress');
    this._resizeEnd();
    this._context = null;
  }

  protected _onMousemove(event: MouseMoveEvent) {
    let newBounds = this._computeBounds(event);
    if (newBounds) {
      $.throttle(this._resizeHandler, Resizable.FPS)(newBounds);
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
    this.$container.trigger('resize', {
      newBounds: newBounds,
      initialBounds: this._context.initialBounds
    });
  }

  protected _resizeEnd() {
    if (this._context.currentBounds.equals(this._context.initialBounds)) {
      return;
    }
    this.$container.trigger('resizeend', {
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
  edge: string;
  mousedownEvent: MouseDownEvent;
}

export type ResizableMode = EnumObject<typeof Resizable.MODES>;
