/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, aria, KeyStrokeContext, Menu, NavigateButtonModel, Outline, Page, SomeRequired} from '../../../index';
import $ from 'jquery';

/**
 * The outline navigation works mostly browser-side. The navigation logic is implemented in JavaScript.
 * When a navigation button is clicked, we process that click browser-side first and send an event to
 * the server which nodes have been selected. We do that for better user experience. In a first attempt
 * the whole navigation logic was on the server, which caused a lag and flickering in the UI.
 */
export abstract class NavigateButton extends Menu implements NavigateButtonModel {
  declare model: NavigateButtonModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'node' | 'outline'>;

  node: Page;
  outline: Outline;
  altKeyStrokeContext: KeyStrokeContext;
  overflow: boolean;

  protected _defaultText: string;
  protected _defaultIconId: string;

  constructor() {
    super();

    this.node = null;
    this.outline = null;
    this.actionStyle = Action.ActionStyle.BUTTON;
    this._addCloneProperties(['node', 'outline', 'altKeyStrokeContext']);
    this.inheritAccessibility = false;
  }

  protected override _render() {
    if (this.overflow) {
      this.text = this.session.text(this._defaultText);
      this.iconId = null;
    } else {
      this.text = null;
      this.iconId = this._defaultIconId;
    }
    this.updateEnabled();
    super._render();
    this.$container.addClass('navigate-button small');
    this.altKeyStrokeContext.registerKeyStroke(this);
  }

  protected override _renderProperties() {
    super._renderProperties();
    aria.label(this.$container, this.session.text(this._defaultText));
  }

  protected override _remove() {
    super._remove();
    this.altKeyStrokeContext.unregisterKeyStroke(this);
  }

  protected _setDetailVisible() {
    let detailVisible = this._toggleDetail();
    $.log.isDebugEnabled() && $.log.debug('show detail-' + (detailVisible ? 'form' : 'table'));
    this.outline.setDetailFormVisibleByUi(this.node, detailVisible);
  }

  protected override _doAction() {
    super._doAction();
    if (this._isDetail()) {
      this._setDetailVisible();
    } else {
      this._drill();
    }
  }

  protected abstract _isDetail(): boolean;

  protected abstract _drill();

  protected abstract _toggleDetail(): boolean;

  protected abstract _buttonEnabled(): boolean;

  /**
   * Called when enabled state must be re-calculated and probably rendered.
   */
  updateEnabled() {
    this.setEnabled(this._buttonEnabled());
  }
}
