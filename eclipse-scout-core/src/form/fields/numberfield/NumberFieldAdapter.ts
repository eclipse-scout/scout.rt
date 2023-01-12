/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, BasicFieldAdapter, Event, NumberField, objects, ValueFieldParseErrorEvent} from '../../../index';

export class NumberFieldAdapter extends BasicFieldAdapter {

  protected _onWidgetParseError(event: ValueFieldParseErrorEvent<number, NumberField>) {
    // The parsing might fail on JS side, but it might succeed on server side -> Don't show an error status, instead let the server decide
    event.preventDefault();
  }

  protected override _onWidgetEvent(event: Event<NumberField>) {
    if (event.type === 'parseError') {
      this._onWidgetParseError(event as ValueFieldParseErrorEvent<number, NumberField>);
    } else {
      super._onWidgetEvent(event);
    }
  }

  static modifyPrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(NumberField, 'clearErrorStatus', function() {
      if (this.modelAdapter) {
        // Don't do anything -> let server handle it

      } else {
        return this.clearErrorStatusOrig();
      }
    }, true);
  }
}

App.addListener('bootstrap', NumberFieldAdapter.modifyPrototype);
