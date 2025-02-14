/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Event, EventHandler, Form, FormTableControlEventMap, FormTableControlLayout, FormTableControlModel, GroupBox, InitModelOf, ObjectOrChildModel, TabBox, TableControl} from '../../index';

export class FormTableControl extends TableControl implements FormTableControlModel {
  declare model: FormTableControlModel;
  declare eventMap: FormTableControlEventMap;
  declare self: FormTableControl;

  form: Form;
  protected _formDestroyHandler: EventHandler<Event<Form>>;

  constructor() {
    super();
    this._addWidgetProperties('form');
    this._formDestroyHandler = this._onFormDestroy.bind(this);
    this.childDestroyer = Form.destroyChildForm.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setForm(this.form);
  }

  protected override _createLayout(): AbstractLayout {
    return new FormTableControlLayout(this);
  }

  protected override _renderContent($parent: JQuery) {
    this.form.renderInitialFocusEnabled = false;
    this.form.render($parent);

    // Tab box gets a special style if it is the first field in the root group box
    let rootGroupBox = this.form.rootGroupBox;
    if (rootGroupBox && rootGroupBox.controls[0] instanceof TabBox) {
      rootGroupBox.controls[0].$container.addClass('in-table-control');
    }

    this.form.$container.height($parent.height());
    this.form.$container.width($parent.width());
    this.form.htmlComp.validateRoot = true;
    this.form.htmlComp.validateLayout();
  }

  protected override _removeContent() {
    if (this.form) {
      this.form.remove();
    }
  }

  protected _removeForm() {
    this.removeContent();
  }

  protected _renderForm(form: Form) {
    this.renderContent();
  }

  /**
   * Returns true if the table control may be displayed (opened).
   */
  override isContentAvailable(): boolean {
    return !!this.form;
  }

  setForm(form: ObjectOrChildModel<Form>) {
    this.setProperty('form', form);
  }

  protected _setForm(form: Form) {
    if (this.form) {
      this.form.off('destroy', this._formDestroyHandler);
    }
    if (form) {
      form.one('destroy', this._formDestroyHandler);
      this._adaptForm(form);
    }
    this._setProperty('form', form);
  }

  protected _adaptForm(form: Form) {
    form.setShowOnOpen(false);
    form.setDisplayHint(Form.DisplayHint.VIEW);
    form.setModal(false);
    form.setClosable(false);
    form.setAskIfNeedSave(false);
    form.rootGroupBox?.setMenuBarPosition(GroupBox.MenuBarPosition.BOTTOM);
    form.setOwner(this); // TODO CGU See comment in Page.ts
  }

  override onControlContainerOpened() {
    if (!this.form || !this.form.rendered) {
      return;
    }
    this.form.renderInitialFocus();
  }

  protected _onFormDestroy(event: Event<Form>) {
    // Called when the inner form is destroyed --> unlink it from this table control
    this._removeForm();
    this._setForm(null);
  }
}
