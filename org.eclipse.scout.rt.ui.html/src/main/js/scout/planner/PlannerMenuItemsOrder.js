scout.PlannerMenuItemsOrder = function(session, objectType) {
  scout.PlannerMenuItemsOrder.parent.call(this, session, objectType);
  this.selectionTypes = ['Resource', 'Activity', 'Range'];
};
scout.inherits(scout.PlannerMenuItemsOrder, scout.MenuItemsOrder);
