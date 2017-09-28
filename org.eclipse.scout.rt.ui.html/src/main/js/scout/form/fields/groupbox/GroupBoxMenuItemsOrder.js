/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.GroupBoxMenuItemsOrder = function() {};

scout.GroupBoxMenuItemsOrder.prototype.order = function(items) {
  var leftButtons = [],
    leftMenus = [],
    rightButtons = [],
    rightMenus = [];

  items.forEach(function(item) {
    if (item.isButton()) {
      var horizontalAlignment = item.horizontalAlignment;
      if (horizontalAlignment === undefined) {
        // Real buttons have no property 'horizontalAlignment' but a corresponding field on the gridData
        horizontalAlignment = (item.gridData && item.gridData.horizontalAlignment);
      }
      if (horizontalAlignment === 1) {
        rightButtons.push(item);
      } else { // also 0
        leftButtons.push(item);
      }
    } else {
      if (item.horizontalAlignment === 1) {
        rightMenus.push(item);
      } else { // also 0
        leftMenus.push(item);
      }
    }
  });

  return {
    left: leftButtons.concat(leftMenus),
    right: rightButtons.concat(rightMenus)
  };
};
