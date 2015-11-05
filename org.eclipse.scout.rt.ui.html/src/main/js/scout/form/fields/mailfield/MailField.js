/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.MailField = function() {
  scout.MailField.parent.call(this);
};
scout.inherits(scout.MailField, scout.ValueField);

scout.MailField.prototype._render = function($parent) {
  this.addContainer($parent, 'mail-field');
  this.addLabel();
  this.addField($parent.makeDiv('not-implemented').text('not implemented yet'));
  this.addMandatoryIndicator();
  this.addStatus();
};
