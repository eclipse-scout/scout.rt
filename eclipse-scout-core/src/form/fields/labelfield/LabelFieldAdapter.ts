/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, LabelField, ValueFieldAdapter} from '../../../index';
import {LabelFieldAppLinkActionEvent} from './LabelFieldEventMap';

export default class LabelFieldAdapter extends ValueFieldAdapter {

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
