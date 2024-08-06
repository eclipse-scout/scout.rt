/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Action, aria, Event, graphics, icons, InitModelOf, Insets, ObjectOrChildModel, Point, Popup, PopupAlignment, PopupLayout, Resizable, ResizableMode, scout, Widget, WidgetPopupEventMap, WidgetPopupLayout, WidgetPopupModel
} from '../index';
import $ from 'jquery';

export class WidgetPopup<TContent extends Widget = Widget> extends Popup implements WidgetPopupModel<TContent> {
  declare model: WidgetPopupModel<TContent>;
  declare eventMap: WidgetPopupEventMap;
  declare self: WidgetPopup;

  closable: boolean;
  closeAction: Action;
  movable: boolean;
  resizable: boolean;
  resizeModes: ResizableMode[];
  $dragHandle: JQuery;
  content: TContent;
  /**
   * Action bar on the top right corner, is used to display the close action but may also be used by popups to add custom actions.
   * The bar will only be added automatically if the popup is closable.
   */
  $actions: JQuery;

  protected _moveHandler: (position: { top: number; left: number }) => void;
  protected _resizeHandler: (event: JQuery.ResizeEvent) => boolean;
  protected _autoPositionOrig: boolean;

  constructor() {
    super();
    this.animateOpening = true;
    this.animateResize = true;
    this.animateRemoval = true;
    this.closable = false;
    this.closeAction = null;
    this.movable = false;
    this.resizable = false;
    this.resizeModes = null;
    this.content = null;
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
    this.windowResizeType = 'layoutAndPosition';
    this.$dragHandle = null;
    this.$actions = null;
    this._moveHandler = this._onMove.bind(this);
    this._resizeHandler = this._onResize.bind(this);
    this._autoPositionOrig = null;
    this._addWidgetProperties(['content']);
  }

  protected override _createLayout(): PopupLayout {
    return new WidgetPopupLayout(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setClosable(this.closable);
  }

  protected override _render() {
    super._render();
    this.$container.addClass('widget-popup');
    // ignore this container for screen readers, the rendered content will have the relevant role
    aria.role(this.$container, 'none');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderContent();
    this._renderClosable();
    this._renderMovable();
    this._renderResizable();
  }

  protected override _remove() {
    this.$dragHandle = null;
    this.$actions = null;
    super._remove();
  }

  setContent(content: ObjectOrChildModel<TContent>) {
    this.setProperty('content', content);
  }

  protected _renderContent() {
    if (!this.content) {
      return;
    }
    this.content.render();
    this.content.$container.addClass('popup-widget');
    this.invalidateLayoutTree();
  }

  setClosable(closable: boolean) {
    this.setProperty('closable', closable);
  }

  protected _setClosable(closable: boolean) {
    this._setProperty('closable', closable);
    if (this.closable) {
      if (this.closeAction) {
        return;
      }
      this.closeAction = this._createCloseAction();
      this.closeAction.on('action', this._onCloseAction.bind(this));
    } else {
      if (!this.closeAction) {
        return;
      }
      this.closeAction.destroy();
      this.closeAction = null;
    }
  }

  protected _createCloseAction(): Action {
    return scout.create(Action, {
      parent: this,
      cssClass: 'close-action menu-item',
      iconId: icons.REMOVE,
      text: this.session.text('ui.Close'),
      textVisible: false
    });
  }

  protected _renderClosable() {
    if (this.closeAction) {
      if (!this.$actions) {
        this.$actions = this.$container.appendDiv('actions');
      }
      this.closeAction.render(this.$actions);
    } else {
      if (this.$actions && this.$actions.children().length === 0) {
        this.$actions.remove();
        this.$actions = null;
      }
    }
  }

  protected _onCloseAction(event: Event<Action>) {
    this.close();
  }

  setResizable(resizable: boolean) {
    this.setProperty('resizable', resizable);
  }

  protected _renderResizable() {
    if (this.resizable) {
      this.$container
        .resizable({modes: this._determineResizeModes(), $container: this.$container, boundaries: this._calculateResizeBoundaries()})
        .on('resizeStep', this._resizeHandler);
    } else {
      this.$container
        .unresizable()
        .off('resizeStep', this._resizeHandler);
      this.invalidateLayoutTree(); // Resize popup to preferred size
    }
  }

  protected _calculateResizeBoundaries(): Insets {
    let resizeBoundaries = new Insets();
    if (!this.$arrow) {
      return resizeBoundaries;
    }
    let anchorMiddle = this._getAnchorMiddlePoint();
    if (!anchorMiddle) {
      return resizeBoundaries;
    }
    let arrowSize = graphics.size(this.$arrow);
    if (this._isVerticallyAligned()) {
      resizeBoundaries.top = anchorMiddle.y - arrowSize.height;
      resizeBoundaries.bottom = anchorMiddle.y + arrowSize.height;
    } else {
      resizeBoundaries.left = anchorMiddle.x - arrowSize.width;
      resizeBoundaries.right = anchorMiddle.x + arrowSize.width;
    }
    return resizeBoundaries;
  }

  protected _determineResizeModes(): ResizableMode[] {
    if (!this.$arrow || this.resizeModes) {
      return this.resizeModes;
    }
    if (this.calculatedVerticalAlignment === Popup.Alignment.TOP) {
      return [Resizable.MODES.WEST, Resizable.MODES.EAST, Resizable.MODES.NORTH];
    }
    if (this.calculatedVerticalAlignment === Popup.Alignment.BOTTOM) {
      return [Resizable.MODES.WEST, Resizable.MODES.EAST, Resizable.MODES.SOUTH];
    }
    if (this.calculatedHorizontalAlignment === Popup.Alignment.LEFT) {
      return [Resizable.MODES.NORTH, Resizable.MODES.SOUTH, Resizable.MODES.WEST];
    }
    if (this.calculatedHorizontalAlignment === Popup.Alignment.RIGHT) {
      return [Resizable.MODES.NORTH, Resizable.MODES.SOUTH, Resizable.MODES.EAST];
    }
    return this.resizeModes;
  }

  protected _onResize(event: JQuery.ResizeEvent): boolean {
    let layout = this.htmlComp.layout as PopupLayout;
    let autoSizeOrig = layout.autoSize;
    layout.autoSize = false;
    this.htmlComp.revalidateLayoutTree(false);
    layout.autoSize = autoSizeOrig;
    this._updateArrowPosition();
    return false;
  }

  override position(switchIfNecessary?: boolean) {
    if (!this.rendered) {
      return;
    }
    super.position(switchIfNecessary);
    if (this.resizable) {
      let resizable = this.$container.data('resizable');
      resizable.setBoundaries(this._calculateResizeBoundaries());
      resizable.setModes(this._determineResizeModes());
      this._updateArrowPosition();
    }
  }

  protected override _updateArrowClass(verticalAlignment?: PopupAlignment, horizontalAlignment?: PopupAlignment) {
    super._updateArrowClass(verticalAlignment, horizontalAlignment);
    if (this.$arrow) {
      // make sure to remove css positioning which could have been applied in _updateArrowPosition()
      // otherwise the original positioning functions of popup.js may not work correctly anymore
      this.$arrow.cssLeft(null);
      this.$arrow.cssTop(null);
    }
  }

  protected _updateArrowPosition() {
    if (!this.$arrow) {
      return;
    }
    let anchorMiddlePoint = this._getAnchorMiddlePoint();
    if (!anchorMiddlePoint) {
      return;
    }
    this.$arrow.removeClass('leftedge rightedge topedge bottomedge');
    let arrowSize = graphics.size(this.$arrow);
    let arrowMargins = graphics.margins(this.$arrow);
    if (this._isVerticallyAligned()) {
      let verticalMarginShift = arrowMargins.top + arrowMargins.bottom;
      this.$arrow.cssTop(anchorMiddlePoint.y - (verticalMarginShift + this.$container.position().top + (arrowSize.height / 2)));
    } else {
      let horizontalMarginShift = arrowMargins.left + arrowMargins.right;
      this.$arrow.cssLeft(anchorMiddlePoint.x - (horizontalMarginShift + this.$container.position().left + (arrowSize.width / 2)));
    }
  }

  protected _getAnchorMiddlePoint(): Point {
    let anchorBounds = this.getAnchorBounds();
    return anchorBounds ? anchorBounds.center() : null;
  }

  protected _isVerticallyAligned(): boolean {
    return scout.isOneOf(this.calculatedHorizontalAlignment, Popup.Alignment.LEFT, Popup.Alignment.RIGHT);
  }

  setMovable(movable: boolean) {
    this.setProperty('movable', movable);
  }

  protected _renderMovable() {
    if (this.movable) {
      if (this.$dragHandle) {
        return;
      }
      this.$dragHandle = this.$container.prependDiv('drag-handle');
      this.$container.draggable(this.$dragHandle, $.throttle(this._moveHandler, 1000 / 60)); // 60fps
    } else {
      if (!this.$dragHandle) {
        return;
      }
      this.$container.undraggable(this.$dragHandle);
      this.$dragHandle.remove();
      this.$dragHandle = null;
      let layout = this.htmlComp.layout as PopupLayout;
      layout.autoPosition = this._autoPositionOrig;
      this._autoPositionOrig = null;
      this.invalidateLayoutTree(); // move popup to preferred position
    }
  }

  protected _onMove(newOffset: { top: number; left: number }) {
    // Disable automatic positioning as soon as the user drags the popup
    if (this._autoPositionOrig === null) {
      let layout = this.htmlComp.layout as PopupLayout;
      this._autoPositionOrig = layout.autoPosition;
      layout.autoPosition = false;
    }
    this.trigger('move', newOffset);
  }

  override set$Anchor($anchor: JQuery) {
    if (this._autoPositionOrig && $anchor && this.$anchor !== $anchor) {
      // If a new anchor is set, the popup is positioned automatically -> reset flag to make animation work
      let layout = this.htmlComp.layout as PopupLayout;
      layout.autoPosition = this._autoPositionOrig;
      this._autoPositionOrig = null;
    }
    super.set$Anchor($anchor);
  }
}
