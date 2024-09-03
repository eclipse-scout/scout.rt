/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, Button, FormFieldTile, NullLayout, strings, tooltips} from '../../../index';

export class TileButton extends Button {
  $iconContainer: JQuery;

  constructor() {
    super();
    this.processButton = false;
    this.$iconContainer = null;
  }

  protected override _render() {
    if ((this.parent as FormFieldTile).displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      super._render();
      return;
    }
    let $button = this.$parent.makeDiv();
    aria.role($button, 'button');
    this.$buttonLabel = $button.appendSpan('label');

    this.addContainer(this.$parent, 'tile-button');
    this.addField($button);
    this.addStatus();

    // Disable inner form field layout, because the tile button should always occupy
    // the entire container area.
    this.htmlComp.setLayout(new NullLayout());

    this.$container
      .on('click', (event: JQuery.ClickEvent) => {
        if (this.fieldStatus.$container.isOrHas(event.target)) {
          return;
        }
        this._onClick(event);
      })
      .unfocusable();
  }

  protected override _remove() {
    this.$iconContainer = null;
    super._remove();
  }

  protected override _renderIconId() {
    if ((this.parent as FormFieldTile).displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
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

  /** @internal */
  override _renderTooltipText() {
    // Because tile buttons normally don't have a visible status, display the tooltip text as normal "hover" tooltip
    if (strings.hasText(this.tooltipText)) {
      tooltips.install(this.$container, {
        parent: this,
        text: this.tooltipText
      });
    } else {
      tooltips.uninstall(this.$container);
    }
  }

  protected override _renderLabelVisible() {
    super._renderLabelVisible();
    this._renderChildVisible(this.$buttonLabel, this.labelVisible);
  }
}
