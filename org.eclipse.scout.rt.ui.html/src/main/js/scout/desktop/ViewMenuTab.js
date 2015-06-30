/**
 * Shows a list of view buttons with displayStyle=MENU
 * and shows the title of the active outline, if the outline is one
 * of the outline-view-buttons contained in the menu.
 */
scout.ViewMenuTab = function(viewMenus, session) {
  this.viewMenus = viewMenus;
  this.session = session;

  this.$container;
  this.$title;
  this.$menuButton;

  this.outlineViewButton = null;
  this.selected = false;
  this.text;
  this.iconId;
  this._inBackground = false;

  this._update();
};

/**
 * 1. look for a selected outline-view-button
 * 2. look for any outline-view-button
 * 3. in rare cases there will be no outline-view-button at all
 */
scout.ViewMenuTab.prototype._update = function() {
  var ovb = this._findOutlineViewButton(true);
  if (ovb) {
    this.selected = true;
  } else {
    ovb = this._findOutlineViewButton(false);
    this.selected = false;
  }
  this.outlineViewButton = ovb;

  if (this.outlineViewButton) {
    this.text = this.outlineViewButton.text;
    this.iconId = this.outlineViewButton.getIconId();
  } else {
    this.text = '%Sichten'; // FIXME AWE: i18n
    this.iconId = 'font:\uE030'; // XXX AWE: icons.js - constant
  }
};

scout.ViewMenuTab.prototype.render = function($parent) {
  this.$container = $parent.appendDiv('view-button-tab')
    .on('click', this._onClickTab.bind(this));
  this.$title = this.$container.appendSpan('view-button-tab-title has-menu')
    .icon(this.iconId);
  this.$menuButton = this.$container.appendSpan('view-menu-button')
    .on('click', this._onClickMenuButton.bind(this));
  this._renderProperties();
};

scout.ViewMenuTab.prototype._renderProperties = function() {
  this._renderText();
  this._renderIconId();
  this._renderSelected();
};

scout.ViewMenuTab.prototype._renderText = function() {
  this.$title.text(this.selected ? this.text : '');
};

scout.ViewMenuTab.prototype._renderSelected = function() {
  this.$container.select(this.selected);
  this.$menuButton.setVisible(this.selected && !this._inBackground);
};

scout.ViewMenuTab.prototype._renderIconId = function() {
  this.$container.icon(this.iconId);
};

/**
 * @param onlySelected when false -> function returns the first viewMenu which is an OutlineViewButton
 *                     when true  -> function returns the first viewMenu which is an OutlineViewButton AND also selected
 */
scout.ViewMenuTab.prototype._findOutlineViewButton = function(onlySelected) {
  var viewMenu;
  for (var i = 0; i < this.viewMenus.length; i++) {
    viewMenu = this.viewMenus[i];
    if (viewMenu instanceof scout.OutlineViewButton) {
      if (!onlySelected ||
           onlySelected && viewMenu.selected) {
        return viewMenu;
      }
    }
  }
  return null;
};

scout.ViewMenuTab.prototype._onClickTab = function(event) {
  if (this._inBackground) {
    this.session.desktop.bringOutlineToFront();
  }
  else if (this.selected) {
    this._openMenu();
  }
  else if (this.outlineViewButton) {
    this.outlineViewButton.doAction();
  }
};

scout.ViewMenuTab.prototype._onClickMenuButton = function(event) {
  this._openMenu();
  event.stopPropagation();
  event.stopImmediatePropagation();
};

scout.ViewMenuTab.prototype._openMenu = function() {
  var naviBounds = scout.graphics.getBounds(this.$container.parent());
  this.popup = new scout.ViewMenuPopup(this.$container, this.viewMenus, naviBounds, this.session);
  this.popup.headText = this.text;
  this.popup.render();
};

scout.ViewMenuTab.prototype.onOutlineChanged = function(outline) {
  var i, viewMenu, ovb = null;
  for (i = 0; i < this.viewMenus.length; i++) {
    viewMenu = this.viewMenus[i];
    if (viewMenu instanceof scout.OutlineViewButton && viewMenu.outline === outline) {
      ovb = viewMenu;
      break;
    }
  }

  if (ovb) {
    this.selected = true;
    this.text = ovb.text;
    this.iconId = ovb.getIconId();
    this.outlineViewButton = ovb;
  } else {
    this.selected = false;
  }

  this._renderProperties();
};

scout.ViewMenuTab.prototype.sendToBack = function() {
  this._inBackground = true;
  this._renderSelected();
};

scout.ViewMenuTab.prototype.bringToFront = function() {
  this._inBackground = false;
  this._renderSelected();
};
