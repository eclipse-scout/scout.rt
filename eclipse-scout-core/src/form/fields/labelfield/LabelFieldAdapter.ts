/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, LabelField, LabelFieldAppLinkActionEvent, ValueFieldAdapter} from '../../../index';

export class LabelFieldAdapter extends ValueFieldAdapter {

  protected _onWidgetAppLinkAction(event: LabelFieldAppLinkActionEvent) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  protected override _onWidgetEvent(event: Event<LabelField>) {
    if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event as LabelFieldAppLinkActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
