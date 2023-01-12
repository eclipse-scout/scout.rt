/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, Label, LabelAppLinkActionEvent, ModelAdapter} from '../index';

export class LabelAdapter extends ModelAdapter {

  protected _onWidgetAppLinkAction(event: LabelAppLinkActionEvent) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  protected override _onWidgetEvent(event: Event<Label>) {
    if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event as LabelAppLinkActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
