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
import {Form, FormTableControlLayout, GroupBox, TabBox, TableControl} from '../../index';

export default class FormTableControl extends TableControl {

  constructor() {
    super();
    this._addWidgetProperties('form');

    this._formDestroyedHandler = this._onFormDestroyed.bind(this);
  }

  _init(model) {
    super._init(model);
    this._setForm(this.form);
  }

  /**
   * @return {FormTableControlLayout}
   */
  _createLayout() {
    return new FormTableControlLayout(this);
  }

  _renderContent($parent) {
    this.form.renderInitialFocusEnabled = false;
    this.form.render($parent);

    // Tab box gets a special style if it is the first field in the root group box
    let rootGroupBox = this.form.rootGroupBox;
    if (rootGroupBox.controls[0] instanceof TabBox) {
      rootGroupBox.controls[0].$container.addClass('in-table-control');
    }

    this.form.$container.height($parent.height());
    this.form.$container.width($parent.width());
    this.form.htmlComp.validateRoot = true;
    this.form.htmlComp.validateLayout();
  }

  _removeContent() {
    if (this.form) {
      this.form.remove();
    }
  }

  _removeForm() {
    this.removeContent();
  }

  _renderForm(form) {
    this.renderContent();
  }

  /**
   * Returns true if the table control may be displayed (opened).
   */
  isContentAvailable() {
    return !!this.form;
  }

  _setForm(form) {
    if (this.form) {
      this.form.off('destroy', this._formDestroyedHandler);
    }
    if (form) {
      form.on('destroy', this._formDestroyedHandler);
      this._adaptForm(form);
    }
    this._setProperty('form', form);
  }

  _adaptForm(form) {
    form.rootGroupBox.setMenuBarPosition(GroupBox.MenuBarPosition.BOTTOM);
    form.setDisplayHint(Form.DisplayHint.VIEW);
    form.setModal(false);
    form.setAskIfNeedSave(false);
    form.setClosable(false);
  }

  onControlContainerOpened() {
    if (!this.form || !this.form.rendered) {
      return;
    }
    this.form.renderInitialFocus();
  }

  _onFormDestroyed(event) {
    // Called when the inner form is destroyed --> unlink it from this table control
    this._removeForm();
    this._setForm(null);
  }
}
