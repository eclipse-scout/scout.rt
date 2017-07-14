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
  var htmlComp = scout.HtmlComponent.get($container),
    windowSize = $container.windowSize(),
    currentBounds = htmlComp.offsetBounds(true),
    messageBoxSize = htmlComp.size(),
    messageBoxMargins = htmlComp.margins();

  messageBoxSize = scout.DialogLayout.fitContainerInWindow(windowSize, currentBounds.point(), messageBoxSize, messageBoxMargins);

  // Add markers to be able to style the dialog in a different way when it uses the full width or height
  $container
    .toggleClass('full-width', (currentBounds.x === 0 && messageBoxMargins.horizontal() === 0 && windowSize.width === messageBoxSize.width))
    .toggleClass('full-height', (currentBounds.y === 0 && messageBoxMargins.vertical() === 0 && windowSize.height === messageBoxSize.height));

  scout.graphics.setSize($container, messageBoxSize);

  var buttonsSize = scout.graphics.size(this.messageBox.$buttons, {
    exact: true
  });
  this.messageBox.$content.css('height', 'calc(100% - ' + buttonsSize.height + 'px)');
  scout.scrollbars.update(this.messageBox.$content);

  $container.cssPosition(scout.DialogLayout.positionContainerInWindow($container));
};
