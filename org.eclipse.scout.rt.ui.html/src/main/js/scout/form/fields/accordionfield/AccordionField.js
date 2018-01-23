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
  this._addWidgetProperties(['accordion']);
};
scout.inherits(scout.AccordionField, scout.FormField);

scout.AccordionField.prototype._render = function() {
  this.addContainer(this.$parent, 'accordion-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  if (this.accordion) {
    this._renderAccordion();
  }
};

scout.AccordionField.prototype.setTileGrid = function(accordion) {
  this.setProperty('accordion', accordion);
};

scout.AccordionField.prototype._renderAccordion = function() {
  this.accordion.render();
  this.addField(this.accordion.$container);
};

scout.AccordionField.prototype._removeAccordion = function() {
  this.accordion.remove();
  this._removeField();
};
