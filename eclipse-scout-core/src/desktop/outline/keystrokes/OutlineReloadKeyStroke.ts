/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HybridActionContextElements, HybridManager, keys, KeyStroke, Outline, scout, ScoutKeyboardEvent} from '../../../index';

/**
 * Keystroke to reload all nodes of an outline on the UI server via hybrid action. Only active when no node is selected.
 */
export class OutlineReloadKeyStroke extends KeyStroke {
  declare field: Outline;

  protected _reloading = false;

  constructor(outline: Outline) {
    super();
    this.field = outline;
    this.which = [keys.F5];

    this.renderingHints.offset = 8; // increase distance from left edge
    this.renderingHints.$drawingArea = ($drawingArea, event) => this.field.$title || this.field.$data; // same as OutlineNavigateToTopKeyStroke
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    if (this._reloading) {
      return false; // prevent parallel hybrid actions
    }
    if (this.field.selectedNodes.length) {
      return false; // keystroke not available when a node is selected
    }
    return super._accept(event);
  }

  override handle(event: JQuery.KeyboardEventBase) {
    // noinspection JSIgnoredPromiseFromCall
    this._handleAsync(event);
  }

  protected async _handleAsync(event: JQuery.KeyboardEventBase): Promise<void> {
    this._reloading = true;
    try {
      let contextElements = scout.create(HybridActionContextElements)
        .withElement('outline', this.field);
      await HybridManager.get(this.field.session).callActionAndWait('scout.ReloadOutline', undefined, contextElements);
    } finally {
      this._reloading = false;
    }
  }
}
