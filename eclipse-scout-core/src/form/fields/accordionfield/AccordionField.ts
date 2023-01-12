/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Accordion, AccordionFieldEventMap, AccordionFieldModel, EventDelegator, FormField, InitModelOf, LoadingSupport, ObjectOrChildModel} from '../../../index';

export class AccordionField extends FormField implements AccordionFieldModel {
  declare model: AccordionFieldModel;
  declare eventMap: AccordionFieldEventMap;
  declare self: AccordionField;

  accordion: Accordion;
  eventDelegator: EventDelegator;

  constructor() {
    super();
    this.accordion = null;
    this.eventDelegator = null;
    this._addWidgetProperties(['accordion']);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this._setAccordion(this.accordion);
  }

  protected override _createLoadingSupport(): LoadingSupport {
    // Loading is delegated to accordion
    return null;
  }

  protected override _render() {
    this.addContainer(this.$parent, 'accordion-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    if (this.accordion) {
      this._renderAccordion();
    }
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderDropType();
  }

  setAccordion(accordion: ObjectOrChildModel<Accordion>) {
    this.setProperty('accordion', accordion);
  }

  protected _setAccordion(accordion: Accordion) {
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

  protected _renderAccordion() {
    if (!this.accordion) {
      return;
    }
    this.accordion.render();
    this.addField(this.accordion.$container);
    this.invalidateLayoutTree();
  }

  protected _removeAccordion() {
    if (!this.accordion) {
      return;
    }
    this.accordion.remove();
    this._removeField();
    this.invalidateLayoutTree();
  }

  override getFocusableElement(): HTMLElement | JQuery {
    if (this.accordion) {
      return this.accordion.getFocusableElement();
    }
    return null;
  }

  override getDelegateScrollable(): Accordion {
    return this.accordion;
  }
}
