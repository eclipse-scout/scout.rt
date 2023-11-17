/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, EventEmitter, EventMap, events, graphics, Insets, Point, Rectangle, Session, Widget} from '../index';
import $ from 'jquery';

export class MoveSupport<TElem extends Widget> extends EventEmitter {
  declare eventMap: MoveSupportEventMap;
  /**
   * Minimal distance in pixels for a "mouse move" action to take effect.
   * Prevents "mini jumps" when simply clicking on an element.
   */
  mouseMoveThreshold: number;
  /**
   * The maximum size the clone should have. If it exceeds that size it will be scaled down.
   */
  maxCloneSize: number;

  /**
   * Widget containing the draggable elements
   */
  widget: Widget;

  protected _moveData: MoveData<TElem>;
  protected _animationDurationFactor: number;
  protected _mouseMoveHandler: (event: JQuery.MouseMoveEvent) => void;
  protected _mouseUpHandler: (event: JQuery.MouseUpEvent) => void;

  /**
   * @param widget the widget containing the draggable elements. Is used to automatically cancel the move operation when the widget is removed.
   */
  constructor(widget: Widget) {
    super();

    this.maxCloneSize = 200;
    this.mouseMoveThreshold = 7;
    this.widget = widget;

    this._moveData = null;
    this._animationDurationFactor = 1; // for debugging to slow down the animation

    this._mouseMoveHandler = this._onMouseMove.bind(this);
    this._mouseUpHandler = this._onMouseUp.bind(this);
  }

  start(event: JQuery.MouseDownEvent, elements: TElem[], draggedElement: TElem): boolean {
    if (this._moveData) {
      // Do nothing, when dragging is already in progress. This can happen when the user leaves
      // the browser window (e.g. using Alt-Tab) while holding the mouse button pressed and
      // then returns and presses the mouse button again.
      return;
    }
    if (draggedElement.$container.hasClass('dragged')) {
      // If MoveSupport is created again for an already dragged element, do nothing. This makes sure the placeholder element cannot be dragged if clone is released and drag started right again
      return;
    }
    if (!event || !elements || !elements.length || !draggedElement || !draggedElement.$container) {
      return;
    }

    events.fixTouchEvent(event);

    this._initMoveData(event, elements, draggedElement);
    $('iframe').addClass('dragging-in-progress');

    // Prevent scrolling on touch devices (like "touch-action: none" but with better browser support).
    // Theoretically, unwanted scrolling can be prevented by adding the CSS rule "touch-action: none"
    // to the element. Unfortunately, not all devices support this (e.g. Apple Safari on iOS).
    // Therefore, we always suppress the scrolling in JS. Because this also suppresses the 'click'
    // event, click actions have to be triggered manually in the 'mouseup' handler.
    event.preventDefault();

    // Cancel moving when widget is removed
    let handler = () => this.cancel();
    this.widget.one('remove', handler);
    this.one('cancel end', () => {
      this.widget.off('remove', handler);
    });
    return true;
  }

  protected _initMoveData(event: JQuery.MouseDownEvent, elements: TElem[], draggedElement: TElem) {
    let $window = draggedElement.$container.window();
    let $elements = draggedElement.$container.parent();
    this._moveData = {} as MoveData<TElem>;
    this._moveData.session = draggedElement.session;
    this._moveData.$window = $window;
    this._moveData.$container = $elements;
    this._moveData.containerBounds = graphics.offsetBounds($elements, {
      includeMargin: true
    });

    this._moveData.elements = elements;
    this._moveData.elementInfos = this._createElementInfos(elements, draggedElement);

    this._moveData.startCursorPosition = new Point(
      event.pageX - this._moveData.containerBounds.x,
      event.pageY - this._moveData.containerBounds.y
    );
    this._moveData.currentCursorPosition = this._moveData.startCursorPosition;

    // Compute distances from the cursor to the edges of the dragged element
    let draggedElementInfo = this._moveData.draggedElementInfo;
    this._moveData.cursorDistance = new Insets(
      event.pageY - draggedElementInfo.bounds.y,
      draggedElementInfo.bounds.x + draggedElementInfo.bounds.width - event.pageX,
      draggedElementInfo.bounds.y + this._moveData.draggedElementInfo.bounds.height - event.pageY,
      event.pageX - draggedElementInfo.bounds.x
    );

    this._moveData.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler)
      .on('mousemove touchmove', this._mouseMoveHandler)
      .on('mouseup touchend touchcancel', this._mouseUpHandler);
  }

  protected _createElementInfos(elements: TElem[], draggedElement: TElem): DraggableElementInfo<TElem>[] {
    return elements
      .filter(element => !!element.$container)
      .map((element, index) => {
        // Collect various information about each element. This allows us to retrieve positions later on without
        // needing to measure them each time the mouse cursor moves. We can also skip null checks for $element.
        let $element = element.$container;
        let info = {
          element: element,
          $element: $element
        } as DraggableElementInfo<TElem>;
        this._updateElementInfo(info);
        if (element === draggedElement) {
          this._moveData.draggedElementInfo = info;
          this._moveData.$draggedElement = $element;
        }
        return info;
      });
  }

  protected _updateElementInfo(elementInfo: DraggableElementInfo<TElem>) {
    let $element = elementInfo.$element;
    let bounds = graphics.offsetBounds($element);
    let position = new Point(
      bounds.x - this._moveData.containerBounds.x,
      bounds.y - this._moveData.containerBounds.y
    );
    $.extend(elementInfo, {
      position: position,
      bounds: bounds
    });
  }

  protected _updateElementInfos() {
    this._moveData.elementInfos.forEach(info => this._updateElementInfo(info));
  }

  cancel() {
    if (!this._moveData) {
      return;
    }

    this._cleanup();
    this._restoreStyles();
    this._moveData = null;
    this._cancel();
  }

  protected _restoreStyles() {
    // Remove clone
    this._moveData.$clone && this._moveData.$clone.remove();

    // A done class makes it possible to disable transitions that must not be active while the clone will be swapped with the dragged element
    let $draggedElement = this._moveData.$draggedElement;
    $draggedElement.addClass('drag-done');
    setTimeout(() => {
      $draggedElement.removeClass('drag-done');
    }, 100);
    this._moveData.$draggedElement.removeClass('dragged releasing');

    this._moveData.$container.removeClass('dragging-element');
  }

  protected _onMouseMove(event: JQuery.MouseMoveEvent) {
    events.fixTouchEvent(event);

    // Adjust relative values if the panel has been scrolled while dragging (e.g. using the mouse wheel)
    // TODO CGU does this also need to be done when scroll pos of scrollable parent change?
    let containerOffset = graphics.offset(this._moveData.$container);
    if (!containerOffset.equals(this._moveData.containerBounds.point())) {
      let diff = containerOffset.subtract(this._moveData.containerBounds.point());
      this._moveData.containerBounds = this._moveData.containerBounds.translate(diff);
      this._moveData.cloneStartOffset = this._moveData.cloneStartOffset.add(diff);
      this._moveData.elementInfos.forEach(info => {
        info.bounds = info.bounds.translate(diff);
      });
    }

    this._moveData.currentCursorPosition = new Point(
      event.pageX - this._moveData.containerBounds.x,
      event.pageY - this._moveData.containerBounds.y
    );
    let distance = this._moveData.currentCursorPosition.subtract(this._moveData.startCursorPosition);

    // Ignore small mouse movements
    if (!this._moveData.moving) {
      if (Math.abs(distance.x) < this.mouseMoveThreshold && Math.abs(distance.y) < this.mouseMoveThreshold) {
        return;
      }
      this._moveData.moving = true;
      this._onFirstMouseMove();
    }

    // Create a clone of the dragged element that is positioned 'fixed', i.e. with document-absolute coordinates
    if (!this._moveData.$clone) {
      this._moveData.cloneBounds = graphics.offsetBounds(this._moveData.$draggedElement);
      this._moveData.cloneStartOffset = this._moveData.cloneBounds.point();
      this._append$Clone();

      // Change style of dragged element
      this._moveData.$draggedElement.addClass('dragged');
    }

    // Update clone position
    this._moveData.cloneBounds = this._moveData.cloneBounds.moveTo(this._moveData.cloneStartOffset.add(distance));

    // Scale down clone if necessary
    let scale = 1;
    if (this._moveData.cloneBounds.width > this.maxCloneSize) {
      scale = this.maxCloneSize / this._moveData.cloneBounds.width;
    }
    if (this._moveData.cloneBounds.height > this.maxCloneSize) {
      scale = Math.min(this.maxCloneSize / this._moveData.cloneBounds.height, scale);
    }
    this._moveData.$clone.css({
      'top': this._moveData.cloneBounds.y,
      'left': this._moveData.cloneBounds.x,
      '--dragging-scale': scale,
      'transform-origin': this._moveData.cursorDistance.left + 'px ' + this._moveData.cursorDistance.top + 'px'
    });

    // Don't change element order if the clone is outside the container area
    if (!this._moveData.containerBounds.intersects(this._moveData.cloneBounds)) {
      return;
    }

    this._drag(event);
  }

  protected _drag(event: JQuery.MouseMoveEvent) {
    this.trigger('drag');
  }

  protected _onFirstMouseMove() {
    this._moveData.$container.addClass('dragging-element');
  }

  protected _append$Clone() {
    let $clone = this._moveData.$draggedElement.clone()
      .addClass('dragging clone')
      .removeAttr('data-id')
      .css('position', 'fixed')
      .appendTo(this._moveData.session.$entryPoint);

    // Because the clone is added to the $entryPoint (to ensure it is drawn above everything else),
    // the mouse wheel events won't bubble to the container. To make the mouse while work while
    // dragging, we delegate the event manually.
    $clone.on('wheel', event => this._moveData.$container.trigger(event));

    // Clone canvas contents manually
    let origCanvases = this._moveData.$draggedElement.find('canvas:visible') as JQuery<HTMLCanvasElement>;
    $clone.find('canvas:visible').each((index, canvas: HTMLCanvasElement) => {
      try {
        canvas.getContext('2d').drawImage(origCanvases.get(index), 0, 0);
      } catch (err) {
        // Drawing on the canvas can throw unexpected errors, for example:
        // "DOMException: Failed to execute 'drawImage' on 'CanvasRenderingContext2D':
        // The image argument is a canvas element with a width or height of 0."
        $.log.isWarnEnabled() && $.log.warn('Unable to clone canvas. Reason: ', err);
      }
    });
    this._moveData.$clone = $clone;
    this._moveData.$cloneShadow = this._moveData.$clone.prependDiv('shadow')
      .animate({
        opacity: 1
      }, {
        duration: 250 * this._animationDurationFactor
      });
  }

  protected _onMouseUp(event: JQuery.MouseUpEvent) {
    events.fixTouchEvent(event);
    this._cleanup();
    this._dragEnd(event)
      .then(targetBounds => this._moveToTarget(targetBounds).then(() => targetBounds))
      .then(targetBounds => {
        this._restoreStyles();

        if (!targetBounds.equals(this._moveData.draggedElementInfo.bounds)) {
          this._moveEnd();
        }
        this._moveData = null;
        this._end();
      });
  }

  protected _cleanup() {
    this._moveData.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler);
    $('iframe').removeClass('dragging-in-progress');
  }

  protected _moveToTarget(targetBounds: Rectangle): JQuery.Promise<void> {
    if (!this._moveData.$clone) {
      return $.resolvedPromise();
    }
    let promises = [];
    this._moveData.$clone.addClass('releasing');
    this._moveData.$draggedElement.addClass('releasing');

    // Move clone to target position and restore original size
    promises.push(this._moveData.$clone
      .css('pointer-events', 'none')
      .css('--dragging-scale', '1')
      .animate({
        top: targetBounds.y,
        left: targetBounds.x,
        width: targetBounds.width,
        height: targetBounds.height
      }, {
        easing: 'easeOutQuart',
        duration: 500 * this._animationDurationFactor
      })
      .promise());

    // Fade out shadow
    promises.push(this._moveData.$cloneShadow
      .stop(true)
      .animate({
        opacity: 0
      }, {
        duration: 500 * this._animationDurationFactor
      })
      .promise());

    return $.promiseAll(promises);
  }

  /**
   * @returns the target offset bounds to where the element should be moved
   */
  protected _dragEnd(event: JQuery.MouseUpEvent): JQuery.Promise<Rectangle> {
    let info = this._moveData.draggedElementInfo;
    return $.resolvedPromise(new Rectangle(info.bounds.x, info.bounds.y, info.bounds.width, info.bounds.height));
  }

  protected _moveEnd() {
    this.trigger('moveEnd');
  }

  protected _end() {
    this.trigger('end');
  }

  protected _cancel() {
    this.trigger('cancel');
  }
}

/**
 * Temporary data structure to store data while mouse actions are handled.
 */
export interface MoveData<TElem extends Widget> {
  /**
   * Distance from cursor to the edges of the dragged element.
   */
  cursorDistance: Insets;
  session: Session;
  $window: JQuery<Window>;
  /**
   * The container containing the draggable elements
   */
  $container: JQuery;
  /**
   * The offset bounds of the container;
   */
  containerBounds: Rectangle;
  /**
   * The draggable elements.
   */
  elements: TElem[];
  /**
   * Contains various information about each element.
   */
  elementInfos: DraggableElementInfo<TElem>[];
  /**
   * Contains various information about the dragged element.
   */
  draggedElementInfo: DraggableElementInfo<TElem>;
  /**
   * Points to draggedElementInfo.$element.
   */
  $draggedElement: JQuery;
  /**
   * The position of the cursor when the dragging started.
   */
  startCursorPosition: Point;
  /**
   * The current position of the cursor.
   */
  currentCursorPosition: Point;
  /**
   * Whether an element is being moved.
   */
  moving: boolean;
  /**
   * A clone of the dragged element that follows the cursor. The dragged element itself stays at its original position until it should be moved to a new location.
   */
  $clone: JQuery;
  /**
   * A dedicated shadow element so it can be animated.
   */
  $cloneShadow: JQuery;
  cloneStartOffset: Point;
  cloneBounds: Rectangle;
}

export interface DraggableElementInfo<TElem extends Widget> {
  element: TElem;
  $element: JQuery;
  /**
   * The relative position to the container.
   */
  position: Point;
  /**
   * The size and absolute position (relative to the window).
   */
  bounds: Rectangle;
}

export interface MoveSupportEventMap extends EventMap {
  'drag': Event;
  'moveEnd': Event;
  'end': Event;
  'cancel': Event;
}
