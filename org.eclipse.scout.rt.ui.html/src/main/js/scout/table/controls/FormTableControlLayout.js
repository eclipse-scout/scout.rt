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
scout.FormTableControlLayout = function(control) {
  scout.FormTableControlLayout.parent.call(this);
  this.control = control;
};
scout.inherits(scout.FormTableControlLayout, scout.AbstractLayout);

scout.FormTableControlLayout.prototype.layout = function($container) {
  if (!this.control.contentRendered) {
    return;
  }

  var formSize,
    controlContentSize = scout.graphics.getSize(this.control.tableFooter.$controlContent);

  if (this.control.form) {
    var htmlForm = this.control.form.htmlComp;
    formSize = controlContentSize.subtract(htmlForm.getMargins());
    htmlForm.setSize(formSize);
  }
};
