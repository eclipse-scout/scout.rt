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
import {Event, EventHandler, Form, FormField, FormModel, GroupBox, RefModel, WrappedFormFieldEventMap, WrappedFormFieldModel} from '../../../index';

export default class WrappedFormField extends FormField implements WrappedFormFieldModel {
  declare model: WrappedFormFieldModel;
  declare eventMap: WrappedFormFieldEventMap;
  declare self: WrappedFormField;

  innerForm: Form;
  initialFocusEnabled: boolean;

  protected _formDestroyHandler: EventHandler<Event<Form>>;

  constructor() {
    super();
    this._addWidgetProperties(['innerForm']);
    this.innerForm = null;
    this.initialFocusEnabled = false;
    this._formDestroyHandler = this._onInnerFormDestroy.bind(this);
  }

  protected override _init(model: WrappedFormFieldModel) {
    super._init(model);
    this._setInnerForm(this.innerForm);
  }

  protected _render() {
    this.addContainer(this.$parent, 'wrapped-form-field');
    this.addLabel();
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderInnerForm();
  }

  setInnerForm(innerForm: Form | RefModel<FormModel>) {
    this.setProperty('innerForm', innerForm);
  }

  protected _setInnerForm(innerForm: Form) {
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
  protected _renderInnerForm() {
    if (!this.innerForm) {
      return;
    }

    this.innerForm.setDisplayHint(Form.DisplayHint.VIEW); // by definition, an inner form is a view.
    this.innerForm.setModal(false); // by definition, an inner form is not modal.
    this.innerForm.setClosable(false); // Disable close key stroke
    this.innerForm.renderInitialFocusEnabled = this.initialFocusEnabled; // do not render initial focus of form if disabled.
    if (this.innerForm.rootGroupBox) {
      this.innerForm.rootGroupBox.setMenuBarPosition(GroupBox.MenuBarPosition.BOTTOM);
    }

    this.innerForm.render();

    this.addField(this.innerForm.$container);
    this.innerForm.invalidateLayoutTree();

    // required because active element is lost when 'addField' is called.
    this._renderInitialFocusEnabled();
  }

  protected _removeInnerForm() {
    if (this.innerForm) {
      this.innerForm.remove();
    }
    this._removeField();
  }

  protected _onInnerFormDestroy(event: Event<Form>) {
    this._removeInnerForm();
    this._setInnerForm(null);
  }

  protected _renderInitialFocusEnabled() {
    if (this.innerForm && this.initialFocusEnabled) {
      this.innerForm.renderInitialFocus();
    }
  }
}
