/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, FormFieldAdapter, WizardProgressField, WizardProgressFieldStepActionEvent} from '../../../index';

export class WizardProgressFieldAdapter extends FormFieldAdapter {

  protected _onWidgetStepAction(event: WizardProgressFieldStepActionEvent) {
    this._send('doStepAction', {
      stepIndex: event.stepIndex
    });
  }

  protected override _onWidgetEvent(event: Event<WizardProgressField>) {
    if (event.type === 'stepAction') {
      this._onWidgetStepAction(event as WizardProgressFieldStepActionEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
