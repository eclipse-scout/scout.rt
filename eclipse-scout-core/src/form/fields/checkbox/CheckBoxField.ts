/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AddCellEditorFieldCssClassesOptions, aria, CheckBoxFieldEventMap, CheckBoxFieldModel, CheckBoxToggleKeyStroke, Device, InitModelOf, KeyStrokeContext, objects, styles, tooltips, ValueField} from '../../../index';

export class CheckBoxField extends ValueField<boolean> implements CheckBoxFieldModel {
  declare model: CheckBoxFieldModel;
  declare eventMap: CheckBoxFieldEventMap;
  declare self: CheckBoxField;

  triStateEnabled: boolean;
  wrapText: boolean;
  keyStroke: string;
  checkBoxKeyStroke: CheckBoxToggleKeyStroke;
  formKeyStrokeContext: KeyStrokeContext;
  $checkBox: JQuery;
  $checkBoxLabel: JQuery;

  constructor() {
    super();

    this.triStateEnabled = false;
    this.wrapText = false;
    this.keyStroke = null;
    this.checkBoxKeyStroke = new CheckBoxToggleKeyStroke(this);
    this.$checkBox = null;
    this.$checkBoxLabel = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setKeyStroke(this.keyStroke);
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(new CheckBoxToggleKeyStroke(this));

    // The key stroke configured by this.keyStroke has form scope
    this.formKeyStrokeContext = new KeyStrokeContext();
    this.formKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.formKeyStrokeContext.registerKeyStroke(this.checkBoxKeyStroke);
    this.formKeyStrokeContext.$bindTarget = () => {
      // use form if available
      let form = this.getForm();
      if (form) {
        return form.$container;
      }
      // use desktop otherwise
      return this.session.desktop.$container;
    };
  }

  protected override _render() {
    this.addContainer(this.$parent, 'check-box-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addFieldContainer(this.$parent.makeDiv());

    this.$checkBox = this.$fieldContainer
      .appendDiv('check-box')
      .on('mousedown', this._onMouseDown.bind(this))
      .data('valuefield', this);
    aria.role(this.$checkBox, 'checkbox');
    this.addField(this.$checkBox);

    this.$checkBoxLabel = this.$fieldContainer
      .appendDiv('label')
      .on('mousedown', this._onMouseDown.bind(this));

    aria.linkElementWithLabel(this.$checkBox, this.$checkBoxLabel);
    tooltips.installForEllipsis(this.$checkBoxLabel, {
      parent: this
    });
    this.addStatus();
    this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderValue();
    this._renderWrapText();
  }

  protected override _remove() {
    tooltips.uninstall(this.$checkBoxLabel);
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
    super._remove();
  }

  protected override _renderDisplayText() {
    // NOP
  }

  override setValue(value: boolean) {
    this.setProperty('value', value);
  }

  protected override _ensureValue(value: boolean): boolean {
    if (!this.triStateEnabled && objects.isNullOrUndefined(value)) {
      return false;
    }
    return super._ensureValue(value);
  }

  /**
   * The value may be false, true (and null in tri-state mode)
   */
  protected _renderValue() {
    this.$fieldContainer.toggleClass('checked', this.value === true);
    this.$checkBox.toggleClass('checked', this.value === true);
    this.$checkBox.toggleClass('undefined', this.triStateEnabled && this.value !== true && this.value !== false);
    aria.checked(this.$checkBox, this.value, this.triStateEnabled);
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this.$checkBox
      .setTabbable(this.enabledComputed && !Device.get().supportsOnlyTouch())
      .setEnabled(this.enabledComputed);
  }

  setTriStateEnabled(triStateEnabled: boolean) {
    if (this.setProperty('triStateEnabled', triStateEnabled)) {
      this.setValue(this._ensureValue(this.value));
    }
  }

  protected override _renderLabel() {
    this.$checkBoxLabel.contentOrNbsp(this.labelHtmlEnabled, this.label, 'empty');
    this._renderEmptyLabel();
  }

  protected override _renderFont() {
    styles.legacyFont(this, this.$fieldContainer);
    // Changing the font may enlarge or shrink the field (e.g. set the style to bold makes the text bigger) -> invalidate layout
    this.invalidateLayoutTree();
  }

  protected override _renderForegroundColor() {
    styles.legacyForegroundColor(this, this.$fieldContainer);
  }

  protected override _renderBackgroundColor() {
    styles.legacyBackgroundColor(this, this.$fieldContainer);
  }

  protected override _renderGridData() {
    super._renderGridData();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  protected override _renderGridDataHints() {
    super._renderGridDataHints();
    this.updateInnerAlignment({
      useHorizontalAlignment: true
    });
  }

  setKeyStroke(keyStroke: string) {
    this.setProperty('keyStroke', keyStroke);
  }

  protected _setKeyStroke(keyStroke: string) {
    this._setProperty('keyStroke', keyStroke);
    this.checkBoxKeyStroke.parseAndSetKeyStroke(this.keyStroke);
  }

  setWrapText(wrapText: boolean) {
    this.setProperty('wrapText', wrapText);
  }

  protected _renderWrapText() {
    this.$checkBoxLabel.toggleClass('white-space-nowrap', !this.wrapText);
    this.invalidateLayoutTree();
  }

  override acceptInput(whileTyping?: boolean) {
    // NOP
  }

  toggleChecked() {
    if (!this.enabledComputed) {
      return;
    }
    if (this.triStateEnabled) {
      if (this.value === false) {
        this.setValue(true);
      } else if (this.value === true) {
        this.setValue(null);
      } else {
        this.setValue(false);
      }
    } else {
      this.setValue(!this.value);
    }
  }

  override prepareForCellEdit(opts?: AddCellEditorFieldCssClassesOptions) {
    super.prepareForCellEdit(opts);
    this.$checkBoxLabel.hide();
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent) {
    if (!this.enabledComputed) {
      return;
    }
    this.toggleChecked();
    // Also focus when check box is clicked otherwise firefox would loose the focus (see device.loosesFocusIfPseudoElementIsRemoved)
    let onCheckBox = this.$checkBox.is(event.currentTarget),
      onLabel = this.$checkBoxLabel.isOrHas(event.currentTarget); // isOrHas is required for HTML enabled labels with nested elements
    if (onCheckBox || onLabel) {
      this.focusAndPreventDefault(event);
    }
  }
}
