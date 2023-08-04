/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, ActionKeyStroke, aria, Desktop, EventHandler, HtmlComponent, InitModelOf, PropertyChangeEvent, ViewButtonActionKeyStroke, ViewButtonDisplayStyle, ViewButtonEventMap, ViewButtonModel} from '../../index';

export class ViewButton extends Action implements ViewButtonModel {
  declare model: ViewButtonModel;
  declare eventMap: ViewButtonEventMap;
  declare self: ViewButton;

  displayStyle: ViewButtonDisplayStyle;
  selectedAsMenu: boolean;

  protected _desktopInBackgroundHandler: EventHandler<PropertyChangeEvent<boolean, Desktop>>;

  constructor() {
    super();
    this.showTooltipWhenSelected = false;
    this.displayStyle = 'TAB';
    this.selectedAsMenu = false;
    this._desktopInBackgroundHandler = this._onDesktopInBackgroundChange.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.session.desktop.on('propertyChange:inBackground', this._desktopInBackgroundHandler);
  }

  protected override _destroy() {
    this.session.desktop.off('propertyChange:inBackground', this._desktopInBackgroundHandler);
    super._destroy();
  }

  renderAsTab($parent: JQuery) {
    let $wrapper = $parent.appendDiv('view-tab-wrapper');
    this.render($wrapper);
    this.$container.addClass('view-tab view-button-tab');
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('view-button')
      .on('mousedown', this._onMouseEvent.bind(this));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.$container.prependDiv('edge left');
    this.$container.appendDiv('edge right');
  }

  protected override _remove() {
    let $wrapper = this.$container.parent();
    if ($wrapper.hasClass('view-tab-wrapper')) {
      $wrapper.remove();
    }
    super._remove();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderInBackground();
  }

  protected _renderInBackground() {
    if (this.session.desktop.displayStyle === Desktop.DisplayStyle.COMPACT) {
      return;
    }
    if (!this.rendering) {
      if (this.session.desktop.inBackground) {
        this.$container.addClassForAnimation('animate-bring-to-back');
      } else {
        this.$container.addClassForAnimation('animate-bring-to-front');
      }
    }
    this.$container.toggleClass('in-background', this.session.desktop.inBackground);
  }

  protected override _renderText() {
    // render text as invisible label for screen readers
    aria.label(this.$container, this.text);
  }

  setDisplayStyle(displayStyle: ViewButtonDisplayStyle) {
    this.setProperty('displayStyle', displayStyle);
  }

  protected _onMouseEvent(event: JQuery.MouseDownEvent) {
    this.doAction();
  }

  protected override _createActionKeyStroke(): ActionKeyStroke {
    return new ViewButtonActionKeyStroke(this);
  }

  setSelectedAsMenu(selectedAsMenu: boolean) {
    this.selectedAsMenu = selectedAsMenu;
  }

  protected _onDesktopInBackgroundChange(event: PropertyChangeEvent<boolean, Desktop>) {
    if (this.rendered) {
      this._renderInBackground();
    }
  }
}
