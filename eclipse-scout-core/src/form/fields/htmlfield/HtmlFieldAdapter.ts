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
import {Event, HtmlField, HtmlFieldModel, ValueFieldAdapter} from '../../../index';
import {HtmlFieldAppLinkActionEvent} from './HtmlFieldEventMap';

export default class HtmlFieldAdapter extends ValueFieldAdapter {

  protected override _initProperties(model: HtmlFieldModel) {
    if (model.scrollToEnd !== undefined) {
      // ignore pseudo property initially (to prevent the function StringField#scrollToEnd() to be replaced)
      delete model.scrollToEnd;
    }
  }

  protected _syncScrollToEnd() {
    this.widget.scrollToBottom();
  }

  protected _onWidgetAppLinkAction(event: HtmlFieldAppLinkActionEvent) {
    this._send('appLinkAction', {
      ref: event.ref
    });
  }

  protected override _onWidgetEvent(event: Event<HtmlField>) {
    if (event.type === 'appLinkAction') {
      this._onWidgetAppLinkAction(event as HtmlFieldAppLinkActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
