/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, DesktopTab, keys, RangeKeyStroke} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

/**
 * Composite keystroke to provide a numeric keystroke to select view tabs.
 */
export class DesktopTabSelectKeyStroke extends RangeKeyStroke {
  declare field: Desktop;

  constructor(desktop: Desktop) {
    super();
    this.field = desktop;

    // modifier
    this.parseAndSetKeyStroke(desktop.selectViewTabsKeyStrokeModifier);

    // range [1..9]
    this.registerRange(
      keys['1'], // range from
      () => {
        return keys[Math.min(this._viewTabs().length, 9)]; // range to
      }
    );

    // rendering hints
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let viewIndex = event.which - keys['1'];
      return this._viewTabs()[viewIndex].$container;
    };
    this.inheritAccessibility = false;
  }

  protected override _isEnabled(): boolean {
    let enabled = super._isEnabled();
    return enabled && this.field.selectViewTabsKeyStrokesEnabled && this._viewTabs().length > 0;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    let viewIndex = event.which - keys['1'];

    let tabs = this._viewTabs();
    if (tabs.length && viewIndex < tabs.length) {
      let viewTab = tabs[viewIndex];
      if (this.field.bench) {
        this.field.bench.activateView(viewTab.view);
      }
    }
  }

  protected _viewTabs(): DesktopTab[] {
    if (this.field.bench) {
      return this.field.bench.getTabs();
    }
    return [];
  }
}
