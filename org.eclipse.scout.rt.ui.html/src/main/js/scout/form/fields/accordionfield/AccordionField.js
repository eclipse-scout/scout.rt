/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.AccordionField = function() {
  scout.AccordionField.parent.call(this);
  this.eventDelegator = null;
  this._addWidgetProperties(['accordion']);
};
scout.inherits(scout.AccordionField, scout.FormField);

scout.AccordionField.prototype._init = function(model) {
  scout.AccordionField.parent.prototype._init.call(this, model);

  this._setAccordion(this.accordion);
};

/**
 * @override
 */
scout.AccordionField.prototype._createLoadingSupport = function() {
  // Loading is delegated to accordion
  return null;
};

scout.AccordionField.prototype._render = function() {
  this.addContainer(this.$parent, 'accordion-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  if (this.accordion) {
    this._renderAccordion();
  }
};

scout.AccordionField.prototype.setAccordion = function(accordion) {
  this.setProperty('accordion', accordion);
};

scout.AccordionField.prototype._setAccordion = function(accordion) {
  if (this.accordion) {
    if (this.eventDelegator) {
      this.eventDelegator.destroy();
      this.eventDelegator = null;
    }
  }
  this._setProperty('accordion', accordion);
  if (accordion) {
    this.eventDelegator = scout.EventDelegator.create(this, accordion, {
      delegateProperties: ['loading']
    });
    accordion.setLoading(this.loading);
  }
};

scout.AccordionField.prototype._renderAccordion = function() {
  if (!this.accordion) {
    return;
  }
  this.accordion.render();
  this.addField(this.accordion.$container);
  this.invalidateLayoutTree();
};

scout.AccordionField.prototype._removeAccordion = function() {
  if (!this.accordion) {
    return;
  }
  this.accordion.remove();
  this._removeField();
  this.invalidateLayoutTree();
};

/**
 * @override
 */
scout.AccordionField.prototype.getFocusableElement = function() {
  if (this.accordion) {
    return this.accordion.getFocusableElement();
  }
  return null;
};
