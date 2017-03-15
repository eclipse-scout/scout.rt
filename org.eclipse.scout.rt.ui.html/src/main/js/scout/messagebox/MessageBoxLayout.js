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
scout.MessageBoxLayout = function(messageBox) {
  scout.MessageBoxLayout.parent.call(this, messageBox);
  this.messageBox = messageBox;
};
scout.inherits(scout.MessageBoxLayout, scout.AbstractLayout);

scout.MessageBoxLayout.prototype.layout = function($container) {
  var messageBoxSize,
    htmlComp = scout.HtmlComponent.get($container),
    bounds = htmlComp.getBounds();

  messageBoxSize = scout.DialogLayout.fitContainerInWindow($container.windowSize(), bounds, bounds.dimension(), htmlComp.getMargins());
  scout.graphics.setSize($container, messageBoxSize);

  var buttonsSize = scout.graphics.getSize(this.messageBox.$buttons);
  this.messageBox.$content.css('height', 'calc(100% - ' + buttonsSize.height + 'px)');
  scout.scrollbars.update(this.messageBox.$content);

  $container.cssPosition(scout.DialogLayout.positionContainerInWindow($container));
};
