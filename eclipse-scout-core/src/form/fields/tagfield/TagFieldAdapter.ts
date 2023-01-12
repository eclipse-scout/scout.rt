/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormFieldModel, LookupFieldAdapter, RemoteLookupCall, scout, TagField, TagFieldAcceptInputEvent} from '../../../index';

export class TagFieldAdapter extends LookupFieldAdapter {
  declare widget: TagField;

  protected override _initProperties(model: FormFieldModel & { insertText?: boolean }) {
    if (model.insertText !== undefined) {
      // ignore pseudo property initially (to prevent the function StringField#insertText() to be replaced)
      delete model.insertText;
    }
  }

  /** @internal */
  override _postCreateWidget() {
    super._postCreateWidget();
    let lookupCallType = RemoteLookupCall<string>;
    this.widget.lookupCall = scout.create(lookupCallType, this);
  }

  protected _syncResult(result) {
    let currentLookupCall = this.widget._currentLookupCall as RemoteLookupCall<string>;
    if (currentLookupCall) {
      currentLookupCall.resolveLookup(result);
    }
  }

  protected override _onWidgetAcceptInput(event: TagFieldAcceptInputEvent) {
    this._send('acceptInput', {
      displayText: event.displayText,
      value: event.value
    });
  }
}
