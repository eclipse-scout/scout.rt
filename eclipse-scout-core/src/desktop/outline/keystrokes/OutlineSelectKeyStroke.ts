/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {KeyStroke, Outline, ScoutKeyboardEvent, TreeEventCurrentNode, TreeSelectKeyStroke} from '../../../index';

export class OutlineSelectKeyStroke extends TreeSelectKeyStroke {
  declare field: Outline;

  protected override _accept(event: ScoutKeyboardEvent & TreeEventCurrentNode): boolean {
    if (this.field.focusedNode && this.field.session.desktop.inBackground) {
      return KeyStroke.acceptEvent(this, event);
    }
    return super._accept(event);
  }

  override handle(event: JQuery.KeyboardEventBase & TreeEventCurrentNode) {
    this.field.session.desktop.bringOutlineToFront();
    super.handle(event);
  }
}
