/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, Desktop, HtmlComponent, ViewButtonActionKeyStroke} from '../../index';

export default class ViewButton extends Action {

  constructor() {
    super();
    this.showTooltipWhenSelected = false;
    this.displayStyle = 'TAB';
    /**
     * Indicates if this view button is currently the "selected button" in the ViewMenuTab widget,
     * i.e. if it was the last view button of type MENU to have been selected. Note that the
     * "selected" property does not necessarily have to be true as well, since an other button of
     * type TAB might currently be selected. This information is used when restoring the "selected
     * button" when the ViewMenuTab widget is removed and restored again, e.g. when toggling the
     * desktop's 'navigationVisible' property.
     * @type {boolean}
     */
    this.selectedAsMenu = false;
    this._desktopInBackgroundHandler = this._onDesktopInBackgroundChange.bind(this);
  }

  _init(model) {
    super._init(model);
    this.session.desktop.on('propertyChange:inBackground', this._desktopInBackgroundHandler);
  }

  _destroy() {
    this.session.desktop.off('propertyChange:inBackground', this._desktopInBackgroundHandler);
    super._destroy();
  }

  renderAsTab($parent) {
    let $wrapper = $parent.appendDiv('view-tab-wrapper');
    this.render($wrapper);
    this.$container.addClass('view-tab view-button-tab');
  }

  _render() {
    this.$container = this.$parent.appendDiv('view-button')
      .on('mousedown', this._onMouseEvent.bind(this));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.$container.prependDiv('edge left');
    this.$container.appendDiv('edge right');
  }

  _remove() {
    let $wrapper = this.$container.parent();
    if ($wrapper.hasClass('view-tab-wrapper')) {
      $wrapper.remove();
    }
    super._remove();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderInBackground();
  }

  _renderInBackground() {
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

  /**
   * @override
   */
  _renderText() {
    // No text
  }

  setDisplayStyle(displayStyle) {
    this.setProperty('displayStyle', displayStyle);
  }

  _onMouseEvent(event) {
    this.doAction();
  }

  /**
   * @override Action.js
   */
  _createActionKeyStroke() {
    return new ViewButtonActionKeyStroke(this);
  }

  setSelectedAsMenu(selectedAsMenu) {
    this.selectedAsMenu = selectedAsMenu;
  }

  _onDesktopInBackgroundChange() {
    if (this.rendered) {
      this._renderInBackground();
    }
  }
}
