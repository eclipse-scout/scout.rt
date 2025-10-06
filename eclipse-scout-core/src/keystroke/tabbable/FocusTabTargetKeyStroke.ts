/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {KeyStroke, ScoutKeyboardEvent, TabbableCoordinator, Widget} from '../..';

export class FocusTabTargetKeyStroke extends KeyStroke {
  tabbableCoordinator: TabbableCoordinator;

  constructor(widget: Widget, tabbableCoordinator: TabbableCoordinator) {
    super();
    this.field = widget;
    this.which = [];
    this.renderingHints.render = false;
    this.stopPropagation = true;
    this.keyStrokeMode = KeyStroke.Mode.DOWN;
    this.inheritAccessibility = false;
    this.repeatable = true;
    this.tabbableCoordinator = tabbableCoordinator;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    return super._accept(event) && !!this.tabbableCoordinator;
  }

  protected _$getFocusedItem(event: JQuery.KeyboardEventBase) {
    // Keystrokes may either be registered on the container of the tabbableCoordinator.parent, or on a completely different context
    // -> Use currentTarget to find the focusable element instead of parent.$container
    return $(event.currentTarget).find(':focus');
  }
}
