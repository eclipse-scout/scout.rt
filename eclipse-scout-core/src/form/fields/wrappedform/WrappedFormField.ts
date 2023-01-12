/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, EventHandler, Form, FormField, GroupBox, InitModelOf, ObjectOrChildModel, WrappedFormFieldEventMap, WrappedFormFieldModel} from '../../../index';

export class WrappedFormField extends FormField implements WrappedFormFieldModel {
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

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setInnerForm(this.innerForm);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'wrapped-form-field');
    this.addLabel();
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderInnerForm();
  }

  setInnerForm(innerForm: ObjectOrChildModel<Form>) {
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
