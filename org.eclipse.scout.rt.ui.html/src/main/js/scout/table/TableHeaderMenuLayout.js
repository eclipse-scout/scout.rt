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
  otherGroupsHeight += this.popup.$filtering.find('.header-text').outerHeight(true);

  popupSize = popupSize
    .subtract(htmlComp.getInsets())
    .subtract(scout.graphics.getInsets(this.popup.$filtering));
  filteringContainerHeight = popupSize.height - otherGroupsHeight;

  // If there are only some filter items make container smaller, otherwise use given height
  filteringContainerHeight = Math.min(filteringContainerHeight, filteringContainerScrollHeight);
  $filteringContainer.cssHeight(filteringContainerHeight);

  scout.scrollbars.update($filteringContainer);
};
