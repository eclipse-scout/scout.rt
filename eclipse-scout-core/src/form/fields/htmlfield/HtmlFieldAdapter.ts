/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, HtmlField, HtmlFieldAppLinkActionEvent, HtmlFieldModel, ValueFieldAdapter} from '../../../index';

export class HtmlFieldAdapter extends ValueFieldAdapter {

  protected override _initProperties(model: HtmlFieldModel & { scrollToEnd?: boolean }) {
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
