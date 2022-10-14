/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BeanField, Event, ValueFieldAdapter} from '../../../index';
import {AppLinkActionEvent} from './BeanFieldEventMap';

export default class BeanFieldAdapter extends ValueFieldAdapter {

  constructor() {
    super();
  }

  protected _onWidgetAppLinkAction(event: AppLinkActionEvent) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  protected override _onWidgetEvent(event: Event<BeanField<object>>) {
    if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event as AppLinkActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
