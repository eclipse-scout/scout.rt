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
import {Button, FormFieldTile, NullLayout, strings, tooltips} from '../../../index';

export default class TileButton extends Button {

  constructor() {
    super();
    this.processButton = false;
  }

  _render() {
    if (this.parent.displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      super._render();
      return;
    }
    let $button = this.$parent.makeDiv();
    this.$buttonLabel = $button.appendSpan('label');

    this.addContainer(this.$parent, 'tile-button');
    this.addField($button);

    // Disable inner form field layout, because the tile button should always occupy
    // the entire container area.
    this.htmlComp.setLayout(new NullLayout());

    this.$container
      .on('click', this._onClick.bind(this))
      .unfocusable();
  }

  _remove() {
    this.$iconContainer = null;
    super._remove();
  }

  _renderIconId() {
    if (this.parent.displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      super._renderIconId();
      return;
    }
    this.$field.removeClass('with-icon without-icon');
    if (this.iconId) {
      if (!this.$iconContainer) {
        this.$iconContainer = this.$field.prependDiv('icon-container');
      }
      this.$iconContainer.icon(this.iconId);
      this.$field.addClass('with-icon');
    } else {
      if (this.$iconContainer) {
        this.$iconContainer.remove();
        this.$iconContainer = null;
      }
      this.$field.addClass('without-icon');
    }
    // Invalidate layout because button may now be longer or shorter
    this.invalidateLayoutTree();
  }

  _renderTooltipText() {
    // Because tile buttons don't have a visible status, display the tooltip text as normal "hover" tooltip
    if (strings.hasText(this.tooltipText)) {
      tooltips.install(this.$container, {
        parent: this,
        text: this.tooltipText
      });
    } else {
      tooltips.uninstall(this.$container);
    }
  }
}
