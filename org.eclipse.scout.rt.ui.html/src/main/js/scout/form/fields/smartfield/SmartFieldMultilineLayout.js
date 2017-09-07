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
/**
 * This layout only layouts the INPUT and DIV part of the multi-line smart-field, not the entire form-field.
 */
scout.SmartFieldMultilineLayout = function() {
  scout.SmartFieldMultilineLayout.parent.call(this);
};
scout.inherits(scout.SmartFieldMultilineLayout, scout.AbstractLayout);

scout.SmartFieldMultilineLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    $input = $container.children('.multiline-input'),
    $lines = $container.children('.multiline-lines'),
    innerSize = htmlContainer.availableSize()
      .subtract(htmlContainer.insets());

  $input.cssHeight(scout.HtmlEnvironment.formRowHeight);
  $lines.cssHeight(innerSize.height - scout.HtmlEnvironment.formRowHeight);
};
