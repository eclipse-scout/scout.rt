/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, ErrorHandler, Event, EventEmitter, EventMap, events, graphics, InitModelOf, Insets, keys, Point, Rectangle, scout, scrollbars, Session, ViewportScroller, ViewportScrollerModel, Widget} from '../index';
import $ from 'jquery';

export class MoveSupport<TElem extends DraggableElement> extends EventEmitter {
  declare self: MoveSupport<TElem>;
  declare eventMap: MoveSupportEventMap;

  /**
   * Minimal distance in pixels for a "mouse move" action to take effect.
   * Prevents "mini jumps" when simply clicking on an element.
   */
  mouseMoveThreshold = 7;
  /**
   * The maximum size the clone should have. If it exceeds that size it will be scaled down.
   */
  maxCloneSize = 200;
  /**
   * Widget containing the draggable elements
   */
  widget: Widget;

  /**
   * Temporary data structure to hold data while a move operation is in progress.
   */
  protected _moveData: MoveData<TElem>;
  /**
   * For debugging, to slow down the animation
   */
  protected _animationDurationFactor = 1;
  // FIXME bsh [dnd-table]: Remove when no longer needed
  protected _paused = false;

  protected _mouseMoveHandler = this._onMouseMove.bind(this);
  protected _mouseUpHandler = this._onMouseUp.bind(this);
  protected _keyDownHandler = this._onKeyDown.bind(this);
  protected _scrollHandler = this._onScroll.bind(this);

  /**
   * @param widget the widget containing the draggable elements. Is used to automatically cancel the move operation when the widget is removed.
   */
  constructor(widget: Widget) {
    super();
    this.widget = widget;
  }

  /**
   * @return `true` if the dragging was started successfully, false otherwise.
   */
  start(event: JQuery.MouseDownEvent, elements: TElem[], draggedElement: TElem): boolean {
    if (this._paused) {
      return false;
    }
    if (this._moveData) {
      // Do nothing, when dragging is already in progress. This can happen when the user leaves
      // the browser window (e.g. using Alt-Tab) while holding the mouse button pressed and
      // then returns and presses the mouse button again.
      return false;
    }
    if (!event || !elements || !draggedElement || !elements.includes(draggedElement) || !draggedElement.$container) {
      return false;
    }
    if (draggedElement.$container.hasClass('dragged')) {
      // If MoveSupport is created again for an already dragged element, do nothing. This ensures
      // the placeholder element cannot be dragged if clone is released and drag started right again
      return false;
    }
    if (event.which !== 1) {
      // Only accept left mouse button clicks (right one is reserved for context menu)
      return false;
    }
    events.fixTouchEvent(event);

    this._initMoveData(event, elements, draggedElement);

    // TODO CGU on touch devices it must be possible to scroll but also to drag the element -> drag should start not when pointer is moved but when touch is pressed down for some time

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
    let $container = draggedElement.$container.parent();
    this._moveData = {} as MoveData<TElem>;
    this._moveData.session = draggedElement.session;
    this._moveData.$window = $window;
    this._moveData.$container = $container;
    this._moveData.containerBounds = graphics.offsetBounds($container, {
      includeMargin: true
    });

    this._initViewportScroller(this._moveData.$container.scrollParent());

    this._moveData.elements = elements;
    this._moveData.elementInfos = this._createElementInfos(elements, draggedElement);

    this._moveData.startCursorPosition = new Point(
      event.pageX - this._moveData.containerBounds.x,
      event.pageY - this._moveData.containerBounds.y
    );
    this._moveData.currentCursorPosition = this._moveData.startCursorPosition;

    // Compute distances from the cursor to the edges of the dragged element
    let bounds = this._moveData.draggedElementInfo.bounds;
    this._moveData.cursorDistance = new Insets(
      event.pageY - bounds.y,
      bounds.x + bounds.width - event.pageX,
      bounds.y + bounds.height - event.pageY,
      event.pageX - bounds.x
    );

    // Install global listeners, will be removed again in _cleanup()
    this._moveData.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler)
      .on('mousemove touchmove', this._mouseMoveHandler)
      .on('mouseup touchend touchcancel', this._mouseUpHandler);
    this._moveData.$window[0].removeEventListener('keydown', this._keyDownHandler, true);
    this._moveData.$window[0].addEventListener('keydown', this._keyDownHandler, true);
    $('iframe').addClass('dragging-in-progress');
  }

  protected _initViewportScroller($viewport: JQuery, options?: ViewportScrollerModel) {
    if (!$viewport?.length) {
      return;
    }
    this._moveData.$viewport = $viewport;
    this._moveData.scrollPosition = new Point($viewport.scrollLeft(), $viewport.scrollTop());
    this._moveData.maxScrollPosition = new Point(
      Math.max(0, $viewport[0].scrollWidth - $viewport[0].clientWidth),
      Math.max(0, $viewport[0].scrollHeight - $viewport[0].clientHeight)
    );
    $viewport.on('scroll', this._scrollHandler);
    this._moveData.viewportScroller = scout.create(ViewportScroller, this._viewportScrollerModel());
  }

  protected _viewportScrollerModel(): InitModelOf<ViewportScroller> {
    let viewportSize = graphics.size(this._moveData.$viewport);
    return {
      viewportWidth: viewportSize.width,
      viewportHeight: viewportSize.height,
      active: () => !!this._moveData,
      scroll: (dx: number, dy: number) => this._scrollViewport(dx, dy)
    };
  }

  protected _scrollViewport(dx: number, dy: number) {
    let oldScrollPosition = this._moveData.scrollPosition;
    let newScrollPosition = new Point(
      Math.min(Math.max(0, oldScrollPosition.x + dx), this._moveData.maxScrollPosition.x),
      Math.min(Math.max(0, oldScrollPosition.y + dy), this._moveData.maxScrollPosition.y)
    );

    if (newScrollPosition.x !== oldScrollPosition.x) {
      scrollbars.scrollLeft(this._moveData.$viewport, newScrollPosition.x);
    }
    if (newScrollPosition.y !== oldScrollPosition.y) {
      scrollbars.scrollTop(this._moveData.$viewport, newScrollPosition.y);
    }
  }

  protected _onScroll(event: JQuery.ScrollEvent) {
    this._moveData.scrollPosition = new Point(
      event.target.scrollLeft,
      event.target.scrollTop
    );
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
          this._moveData.draggedElement = element;
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
    this._moveData.$clone?.remove();

    // A done class makes it possible to disable transitions that must not be active while the clone
    // will be swapped with the dragged element
    this._moveData.$draggedElement.removeClass('dragged releasing');
    this._moveData.$container.removeClass('dragging-element');
  }

  protected _onMouseMove(event: JQuery.MouseMoveEvent) {
    if (this._paused) {
      return;
    }
    events.fixTouchEvent(event);
    this._updateOffsets();

    this._moveData.currentCursorPosition = new Point(
      event.pageX - this._moveData.containerBounds.x,
      event.pageY - this._moveData.containerBounds.y
    );
    let distance = this._moveData.currentCursorPosition.subtract(this._moveData.startCursorPosition);

    if (!this._moveData.moving) {
      // Ignore small mouse movements
      if (Math.abs(distance.x) < this.mouseMoveThreshold && Math.abs(distance.y) < this.mouseMoveThreshold) {
        return;
      }

      this._moveData.moving = true;
      this._onFirstMouseMove();
      this._startMove(event);
    }

    this._whileMove(event, distance);
  }

  /**
   * Optional hook for subclasses, called just before {@link _startMove}. The default implementation does nothing.
   */
  protected _onFirstMouseMove() {
  }

  protected _startMove(event: JQuery.MouseEventBase) {
    // Create a clone of the dragged element that is positioned 'fixed', i.e. with document-absolute coordinates
    this._moveData.cloneBounds = graphics.offsetBounds(this._moveData.$draggedElement);
    this._moveData.cloneStartOffset = this._moveData.cloneBounds.point();
    this._append$Clone();

    // Change style of dragged element, but only after the clone has been created
    this._moveData.$container.addClass('dragging-element');
    this._moveData.$draggedElement.addClass('dragged');
  }

  protected _whileMove(event: JQuery.MouseMoveEvent, distance: Point) {
    // Automatically scroll the viewport when the cursor is moved towards the edges while dragging
    if (this._moveData.$viewport) {
      let viewportOffset = this._moveData.$viewport.offset();
      let viewportMousePosition = new Point(
        event.pageX - viewportOffset.left,
        event.pageY - viewportOffset.top
      );
      this._moveData.viewportScroller.update(viewportMousePosition);
    }

    // Update clone position
    this._moveData.cloneBounds = this._moveData.cloneBounds.moveTo(this._moveData.cloneStartOffset.add(distance));

    // Scale down clone if necessary
    let scale = this._calculateScale();
    this._update$Clone(scale);

    // Check if the (scaled) clone is outside the container area
    let scaledCloneBounds = new Rectangle(
      this._moveData.cloneBounds.x + this._moveData.cursorDistance.left - Math.round(scale * this._moveData.cursorDistance.left),
      this._moveData.cloneBounds.y + this._moveData.cursorDistance.top - Math.round(scale * this._moveData.cursorDistance.top),
      Math.round(scale * this._moveData.cloneBounds.width),
      Math.round(scale * this._moveData.cloneBounds.height)
    );
    let scaledCloneInside = this._moveData.containerBounds.intersects(scaledCloneBounds);
    let cursorInside = this._moveData.containerBounds.contains(new Point(event.pageX, event.pageY)); // check needed because clone might have no size
    if (scaledCloneInside || cursorInside) {
      this._drag(event);
    } else {
      this._dragOutside(event);
    }
  }

  protected _calculateScale(): number {
    let scale = 1;
    if (this._moveData.cloneBounds.width > this.maxCloneSize) {
      scale = this.maxCloneSize / this._moveData.cloneBounds.width;
    }
    if (this._moveData.cloneBounds.height > this.maxCloneSize) {
      scale = Math.min(this.maxCloneSize / this._moveData.cloneBounds.height, scale);
    }
    return scale;
  }

  /**
   * Adjusts relative values if the panel has been scrolled while dragging (e.g. using the mouse wheel)
   */
  protected _updateOffsets() {
    let containerOffset = graphics.offset(this._moveData.$container);
    if (!containerOffset.equals(this._moveData.containerBounds.point())) {
      let diff = containerOffset.subtract(this._moveData.containerBounds.point());
      this._moveData.containerBounds = this._moveData.containerBounds.translate(diff);
      if (this._moveData.cloneStartOffset) {
        this._moveData.cloneStartOffset = this._moveData.cloneStartOffset.add(diff);
      }
      this._moveData.elementInfos.forEach(info => {
        info.bounds = info.bounds.translate(diff);
      });
    }
  }

  protected _drag(event: JQuery.MouseMoveEvent) {
    this.trigger('drag');
  }

  protected _dragOutside(event: JQuery.MouseMoveEvent) {
    this.trigger('dragOutside');
  }

  protected _append$Clone() {
    let $clone = this._moveData.$draggedElement.clone()
      .addClass('dragged-clone')
      .removeAttr('data-id')
      .css('position', 'fixed')
      .appendTo(this._moveData.session.$entryPoint);

    // Because the clone is added to the $entryPoint (to ensure it is drawn above everything else),
    // the wheel events won't bubble to the container. To make the mouse work while dragging,
    // we delegate the event manually.
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

  protected _update$Clone(scale: number) {
    this._moveData.$clone.css({
      'top': this._moveData.cloneBounds.y,
      'left': this._moveData.cloneBounds.x,
      '--dragging-scale': scale,
      'transform-origin': `${this._moveData.cursorDistance.left}px ${this._moveData.cursorDistance.top}px`
    });
  }

  protected _onMouseUp(event: JQuery.MouseUpEvent) {
    if (this._paused) {
      return;
    }
    events.fixTouchEvent(event);
    this._updateOffsets();
    this._cleanup();

    if (this._moveData.moving) {
      this._dragEnd(event)
        .then(async targetBounds => {
          if (targetBounds) {
            await this._moveToTarget(targetBounds);
          }
          this._restoreStyles();
          if (targetBounds && !targetBounds.equals(this._moveData.draggedElementInfo.bounds)) {
            this._moveEnd();
          }
          this._moveData = null;
          this._end();
        }, error => {
          scout.create(ErrorHandler, {displayError: false}).handle(error);
          this.cancel();
        })
        .catch(error => App.get().errorHandler.handle(error));
    } else {
      this._restoreStyles();
      this._moveData = null;
      this._end();
    }
  }

  protected _onKeyDown(event: KeyboardEvent) {
    if (event.which === keys.ESC) {
      this.cancel();
      event.preventDefault();
      event.stopPropagation();
    } else if (event.which === keys.PAUSE) {
      this._paused = !this._paused;
      event.preventDefault();
      event.stopPropagation();
    }
  }

  protected _cleanup() {
    this._moveData.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler);
    this._moveData.$window[0].removeEventListener('keydown', this._keyDownHandler, true);
    this._moveData.$viewport?.off('scroll', this._scrollHandler);
    $('iframe').removeClass('dragging-in-progress');
  }

  protected _moveToTarget(targetBounds: Rectangle): JQuery.Promise<void> {
    if (!this._moveData.$clone) {
      return $.resolvedPromise();
    }

    // stop all animations in case of scroll (e.g. by mousewheel, page down etc.)
    let $scrollParents = this._moveData.$draggedElement.scrollParents();
    let releasingScrollHandler = (event: JQuery.ScrollEvent) => {
      this._moveData.elementInfos.forEach(info => info.$element.stop(true, true));
      this._moveData.$clone.stop(true, true);
      this._moveData.$cloneShadow?.stop(true, true);
    };
    $scrollParents.on('scroll', releasingScrollHandler);

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
    this._moveData.$cloneShadow && promises.push(this._moveData.$cloneShadow
      .stop(true)
      .animate({
        opacity: 0
      }, {
        duration: 500 * this._animationDurationFactor
      })
      .promise());

    return $.promiseAll(promises).then(() => {
      $scrollParents.off('scroll', releasingScrollHandler);
    });
  }

  /**
   * Called when the mouse button is released.
   *
   * After the promise is resolved, the clone is moved to the returned target bounds. The default
   * implementation returns the dragged element's original location. When the animation has been
   * completed, the clone is destroyed and the operation ends.
   *
   * @returns the target offset bounds to where the element should be moved, or null if the
   *          operation should be ended without animation.
   */
  protected _dragEnd(event: JQuery.MouseUpEvent): JQuery.Promise<Rectangle> {
    let info = this._moveData.draggedElementInfo;
    return $.resolvedPromise(new Rectangle(info.bounds.x, info.bounds.y, info.bounds.width, info.bounds.height));
  }

  /**
   * Called when the move operation has finished and the element was actually moved.
   * All animations are done, the clone and all listeners have been removed.
   */
  protected _moveEnd() {
    this.trigger('moveEnd');
  }

  /**
   * Called when the move operation has finished normally. The element may or may not have been
   * moved. All animations are done, the clone and all listeners have been removed.
   */
  protected _end() {
    this.trigger('end');
  }

  /**
   * Called when the move operation has been cancelled (e.g. user pressed ESC or widget was removed).
   */
  protected _cancel() {
    this.trigger('cancel');
  }
}

/**
 * Temporary data structure to store data while mouse actions are handled.
 */
export interface MoveData<TElem extends DraggableElement> {
  session: Session;
  $window: JQuery<Window>;
  /**
   * The container containing the draggable elements
   */
  $container: JQuery;
  /**
   * The offset bounds of the container relative to the document, including margins.
   */
  containerBounds: Rectangle;
  /**
   * The element representing the scrollable viewport. Is either the same as $container or its scroll parent.
   * The viewport is scrolled automatically when the mouse cursor is moved near its edges while dragging.
   */
  $viewport: JQuery;
  /**
   * The current scroll position of $viewport.
   */
  scrollPosition: Point;
  /**
   * The maximum scroll position of $viewport.
   */
  maxScrollPosition: Point;
  /**
   * Helper object to automatically scroll the $viewport when the mouse is moved towards its edges while dragging.
   */
  viewportScroller: ViewportScroller;
  /**
   * The draggable elements.
   */
  elements: TElem[];
  /**
   * Contains various information about each element.
   */
  elementInfos: DraggableElementInfo<TElem>[];
  /**
   * The dragged element. Same as to `draggedElementInfo.element`.
   */
  draggedElement: TElem;
  /**
   * The dragged DOM element. Same as `draggedElementInfo.$element`.
   */
  $draggedElement: JQuery;
  /**
   * Contains various information about the dragged element.
   */
  draggedElementInfo: DraggableElementInfo<TElem>;
  /**
   * Distance from cursor to the edges of the dragged element when the dragging started.
   */
  cursorDistance: Insets;
  /**
   * The position of the cursor relative to the container when the dragging started.
   */
  startCursorPosition: Point;
  /**
   * The current position of the cursor relative to the container.
   */
  currentCursorPosition: Point;
  /**
   * Whether the dragged element is being moved.
   */
  moving: boolean;
  /**
   * A clone of the dragged element that follows the cursor. The dragged element itself stays
   * at its original position until it should be moved to a new location. This element is only
   * created once the dragging has started (i.e. mouseMoveThreshold has been exceeded).
   */
  $clone: JQuery;
  /**
   * A dedicated shadow element (inside the $clone) so it can be animated.
   */
  $cloneShadow: JQuery;
  cloneStartOffset: Point;
  cloneBounds: Rectangle;
}

// FIXME bsh [dnd-table]: rename to MovableElement?
export interface DraggableElement {
  $container: JQuery;
  /**
   * Used to populate {@link MoveData.session}.
   */
  session: Session;
}

// FIXME bsh [dnd-table]: rename to MovableElementInfo?
export interface DraggableElementInfo<TElem extends DraggableElement> {
  element: TElem;
  /**
   * Same as `element.$container`
   */
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
  'dragOutside': Event;
  'moveEnd': Event;
  'end': Event;
  'cancel': Event;
}
