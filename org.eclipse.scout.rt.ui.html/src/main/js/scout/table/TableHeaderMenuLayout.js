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
scout.TableHeaderMenuLayout = function(popup) {
  scout.TableHeaderMenuLayout.parent.call(this, popup);
  this.popup = popup;
};
scout.inherits(scout.TableHeaderMenuLayout, scout.PopupLayout);

scout.TableHeaderMenuLayout.prototype.layout = function($container) {
  scout.TableHeaderMenuLayout.parent.prototype.layout.call(this, $container);
  var htmlComp = this.popup.htmlComp,
    popupSize = htmlComp.getSize(),
    $filteringContainer = this.popup.$filteringContainer,
    otherGroupsHeight = 0,
    filteringContainerHeight = 0,
    filteringContainerScrollHeight = $filteringContainer.get(0).scrollHeight,
    groups = [
      this.popup.$moving,
      this.popup.$sorting,
      this.popup.$grouping,
      this.popup.$coloring
    ];

  // Get size of other groups
  groups.forEach(function($group) {
    if ($group) {
      otherGroupsHeight += $group.outerHeight(true);
    }
  });
  otherGroupsHeight += this.popup.$filtering.find('.table-header-menu-group-text').outerHeight(true);

  popupSize = popupSize
    .subtract(htmlComp.getInsets())
    .subtract(scout.graphics.getInsets(this.popup.$filtering));
  filteringContainerHeight = popupSize.height - otherGroupsHeight - 26 - 39; // FIXME AWE: (filter) read height of filter field (30)

  // If there are only some filter items make container smaller, otherwise use given height
  filteringContainerHeight = Math.min(filteringContainerHeight, filteringContainerScrollHeight);
  $filteringContainer.cssHeight(filteringContainerHeight);

  scout.scrollbars.update($filteringContainer);

  // Layout filter fields
  var fieldHtmlComp = scout.HtmlComponent.get(this.popup.$filteringField.find('.form-field'));
  fieldHtmlComp.setSize(new scout.Dimension(popupSize.width - 10, 26)); // FIXME AWE: (filter) dynamic layout for field

};
