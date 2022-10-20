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
import {EventDelegator, FormField} from '../../../index';

export default class AccordionField extends FormField {

  constructor() {
    super();
    this.accordion = null;
    this.eventDelegator = null;
    this._addWidgetProperties(['accordion']);
  }

  _init(model) {
    super._init(model);

    this._setAccordion(this.accordion);
  }

  /**
   * @override
   */
  _createLoadingSupport() {
    // Loading is delegated to accordion
    return null;
  }

  _render() {
    this.addContainer(this.$parent, 'accordion-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    if (this.accordion) {
      this._renderAccordion();
    }
  }

  _renderProperties() {
    super._renderProperties();
    this._renderDropType();
  }

  setAccordion(accordion) {
    this.setProperty('accordion', accordion);
  }

  _setAccordion(accordion) {
    if (this.accordion) {
      if (this.eventDelegator) {
        this.eventDelegator.destroy();
        this.eventDelegator = null;
      }
    }
    this._setProperty('accordion', accordion);
    if (accordion) {
      this.eventDelegator = EventDelegator.create(this, accordion, {
        delegateProperties: ['loading']
      });
      accordion.setLoading(this.loading);
      accordion.setScrollTop(this.scrollTop);
    }
  }

  _renderAccordion() {
    if (!this.accordion) {
      return;
    }
    this.accordion.render();
    this.addField(this.accordion.$container);
    this.invalidateLayoutTree();
  }

  _removeAccordion() {
    if (!this.accordion) {
      return;
    }
    this.accordion.remove();
    this._removeField();
    this.invalidateLayoutTree();
  }

  /**
   * @override
   */
  getFocusableElement() {
    if (this.accordion) {
      return this.accordion.getFocusableElement();
    }
    return null;
  }

  /**
   * @override
   */
  getDelegateScrollable() {
    return this.accordion;
  }
}
