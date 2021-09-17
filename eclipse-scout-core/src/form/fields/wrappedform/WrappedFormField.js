/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Form, FormField} from '../../../index';

export default class WrappedFormField extends FormField {

  constructor() {
    super();
    this._addWidgetProperties(['innerForm']);
    this.innerForm = null;
    this.initialFocusEnabled = false;

    this._formDestroyHandler = this._onInnerFormDestroy.bind(this);
  }

  _init(model) {
    super._init(model);
    this._setInnerForm(this.innerForm);
  }

  _render() {
    this.addContainer(this.$parent, 'wrapped-form-field');
    this.addLabel();
    this.addStatus();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderInnerForm();
  }

  setInnerForm(innerForm) {
    this.setProperty('innerForm', innerForm);
  }

  _setInnerForm(innerForm) {
    if (this.innerForm) {
      this.innerForm.off('destroy', this._formDestroyHandler);
    }
    if (innerForm) {
      innerForm.on('destroy', this._formDestroyHandler);
    }
    this._setProperty('innerForm', innerForm);
  }

  /**
   * Will also be called by model adapter on property change event
   */
  _renderInnerForm() {
    if (!this.innerForm) {
      return;
    }

    this.innerForm.setDisplayHint(Form.DisplayHint.VIEW); // by definition, an inner form is a view.
    this.innerForm.setModal(false); // by definition, an inner form is not modal.
    this.innerForm.setClosable(false); // Disable close key stroke
    this.innerForm.renderInitialFocusEnabled = this.initialFocusEnabled; // do not render initial focus of form if disabled.

    this.innerForm.render();

    this.addField(this.innerForm.$container);
    this.innerForm.invalidateLayoutTree();

    // required because active element is lost when 'addField' is called.
    this._renderInitialFocusEnabled();
  }

  _removeInnerForm() {
    if (this.innerForm) {
      this.innerForm.remove();
    }
    this._removeField();
  }

  _onInnerFormDestroy(event) {
    this._removeInnerForm();
    this._setInnerForm(null);
  }

  _renderInitialFocusEnabled() {
    if (this.innerForm && this.initialFocusEnabled) {
      this.innerForm.renderInitialFocus();
    }
  }
}
