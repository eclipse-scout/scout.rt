/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AppLinkKeyStroke, BeanFieldModel, ValueField} from '../../../index';
import $ from 'jquery';

/**
 * Base class for fields where the value should be visualized.
 */
export class BeanField<TValue extends object> extends ValueField<TValue> implements BeanFieldModel<TValue> {

  constructor() {
    super();

    this.preventInitialFocus = true;
  }

  protected override _render() {
    this.addContainer(this.$parent, 'bean-field');
    this.addLabel();
    this.addField(this.$parent.makeDiv());
    this.addStatus();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderValue();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(new AppLinkKeyStroke(this, this._onAppLinkAction));
  }

  protected override _formatValue(value: TValue): string {
    // The value cannot be changed by the user, therefore we always return the initial displayText property.
    //
    // Strange things happen, if another value is returned... Example:
    // 1. The value is set asynchronously on the field using setValue().
    // 2. This causes the display text to be updated (using _formatValue).
    // 3. When acceptInput() is called (via aboutToBlurByMouseDown), the "current" displayText
    //    is read using _readDisplayText(). The default ValueField.js implementation returns
    //    an empty string, which is different from this.displayText (which is equal to the value,
    //    because of step 2).
    // 4. Because the displayText has changed, parseAndSetValue() is called, which
    //    causes the value to be set to the empty string. The _renderValue() method
    //    will then most likely clear the bean field's content.
    //
    // Test case:
    //   bf.setValue({...}) --> should not update displayText property
    //   bf.acceptInput() --> should not do anything
    return this.displayText;
  }

  protected override _parseValue(displayText: string): TValue {
    // DisplayText cannot be converted to value, use original value (see comment in _formatValue).
    return this.value;
  }

  protected override _readDisplayText(): string {
    // DisplayText cannot be changed, therefore it must be equal to the current value (see comment in _formatValue)
    return this.displayText;
  }

  protected override _renderDisplayText() {
    // nop
  }

  protected _renderValue() {
    // to be implemented by the subclass
  }

  triggerAppLinkAction(ref: string) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }

  protected _onAppLinkAction(event: JQuery.TriggeredEvent) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref');
    this.triggerAppLinkAction(ref);
  }
}
