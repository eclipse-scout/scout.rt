/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Event, EventHandler, Form, FormTableControl, FormTableControlEventMap, icons, TabBox} from '../../index';

export class SearchFormTableControl extends FormTableControl {
  declare eventMap: SearchFormTableControlEventMap;
  declare self: SearchFormTableControl;

  protected _formSearchHandler: EventHandler<Event<Form>> = this._onFormSearch.bind(this);
  protected _formResetHandler: EventHandler<Event<Form>> = this._onFormReset.bind(this);

  constructor() {
    super();

    this.iconId = icons.SEARCH;
    this.tooltipText = '${textKey:Search}';
    this.enabled = false;
    this.keyStroke = 'ctrl-shift-f';
  }

  protected _onFormSearch(event: Event<Form>) {
    this.trigger('search');
  }

  protected _onFormReset(event: Event<Form>) {
    this.trigger('reset');
  }

  protected override _setForm(form: Form) {
    // disable search/reset listeners on old form
    this.form?.off('search', this._formSearchHandler);
    this.form?.off('reset', this._formResetHandler);

    // enable search/reset listeners on new form
    form?.on('search', this._formSearchHandler);
    form?.on('reset', this._formResetHandler);

    super._setForm(form);

    // set enabled iff form is set
    this.setEnabled(!!form);
  }

  protected override _adaptForm(form: Form) {
    super._adaptForm(form);

    form.visitFields(field => {
      if (field instanceof TabBox && field.markStrategy) { // Use save_needed strategy unless marking was explicitly disabled (e.g. by TabBoxAdapter.ts)
        field.setMarkStrategy(TabBox.MarkStrategy.SAVE_NEEDED);
      }
    });
  }
}

export interface SearchFormTableControlEventMap extends FormTableControlEventMap {
  'search': Event<SearchFormTableControl>;
  'reset': Event<SearchFormTableControl>;
}
