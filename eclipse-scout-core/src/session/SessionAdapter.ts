/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ModelAdapter} from './ModelAdapter';
import {SessionModel} from './SessionModel';

export class SessionAdapter extends ModelAdapter {

  override _initProperties(model: SessionModel) {
    super._initProperties(model);
    this.session.sharedVariableMap = model.sharedVariableMap || {};
  }

  protected override _writeProperty(propertyName: string, value: any) {
    // directly write property here as no widget.callSetter is available.
    this.session[propertyName] = value;
  }
}
