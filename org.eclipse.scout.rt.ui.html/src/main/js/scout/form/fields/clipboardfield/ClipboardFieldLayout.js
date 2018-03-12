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
scout.ClipboardFieldLayout = function(field) {
  this.clipboardField = field;
};
scout.inherits(scout.ClipboardFieldLayout, scout.AbstractLayout);

scout.ClipboardFieldLayout.prototype.layout = function($container) {
  var htmlContainer = this.clipboardField.htmlField;
  var containerSize = htmlContainer.availableSize().subtract(htmlContainer.insets());
  var copyButton = this.clipboardField.copyButton;
  var buttonPrefSize = copyButton.htmlComp.prefSize(true);
  var inputWidth = containerSize.width - buttonPrefSize.width;

  this.clipboardField.$field.cssWidth(inputWidth);
  copyButton.htmlComp.setSize(buttonPrefSize);
};
