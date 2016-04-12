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
scout.ActionBoxLayout = function(actionBox) {
  scout.ActionBoxLayout.parent.call(this);
  this.actionBox = actionBox;
};
scout.inherits(scout.ActionBoxLayout, scout.AbstractLayout);

/**
 * @override AbstractLayout.js
 */
scout.ActionBoxLayout.prototype.layout = function($container) {
  this._layout($container);

  this.visibleActions().forEach(function(action) {
    // Make sure open popups are at the correct position after layouting
    if (action.popup) {
      action.popup.position();
    }
  });
};

scout.ActionBoxLayout.prototype._layout = function($container) {
  var htmlContainer = this.actionBox.htmlComp,
    containerSize = htmlContainer.getSize(),
    actions = this.visibleActions(),
    actionsWidth = 0;

  this.undoCollapse(actions);
  this.undoCompact(actions);
  this.undoShrink(actions);
  actionsWidth = this.actualSize(actions).width;
  if (actionsWidth <= containerSize.width) {
    // OK, every action fits into container
    return;
  }

  // Actions don't fit

  // First approach: Set actionBox into compact mode
  this.compact(actions);
  actionsWidth = this.actualSize(actions).width;
  if (actionsWidth <= containerSize.width) {
    // OK, every action fits into container
    return;
  }

  // Second approach: Make text invisible and only show the icon (if available)
  this.shrink(actions);
  actionsWidth = this.actualSize(actions).width;
  if (actionsWidth <= containerSize.width) {
    // OK, every action fits into container
    return;
  }

  // Third approach: Create ellipsis and move overflown actions into it
  this.collapse(actions, containerSize, actionsWidth);
};

scout.ActionBoxLayout.prototype.preferredLayoutSize = function($container) {
  var actions = this.visibleActions();

  this.undoCollapse(actions);
  this.undoCompact(actions);
  this.undoShrink(actions);

  return this.actualSize();
};

scout.ActionBoxLayout.prototype.compact = function(actions) {
  if (this.actionBox.compactOrig === undefined) {
    this.actionBox.compactOrig = this.compact;
    this.actionBox.htmlComp.suppressInvalidate = true;
    this.actionBox.setCompact(true);
    this.actionBox.htmlComp.suppressInvalidate = false;
  }

  this.compactActions(actions);
};

scout.ActionBoxLayout.prototype.undoCompact = function(actions) {
  if (this.actionBox.compactOrig !== undefined) {
    this.actionBox.htmlComp.suppressInvalidate = true;
    this.actionBox.setCompact(this.compactOrig);
    this.actionBox.htmlComp.suppressInvalidate = false;
    this.actionBox.compactOrig = undefined;
  }

  this.undoCompactActions(actions);
};

/**
 * Sets all actions into compact mode.
 */
scout.ActionBoxLayout.prototype.compactActions = function(actions) {
  actions = actions || this.visibleActions();
  actions.forEach(function(action) {
    if (action.compactOrig !== undefined) {
      // already done
      return;
    }
    action.compactOrig = action.compact;
    action.htmlComp.suppressInvalidate = true;
    action.setCompact(true);
    action.htmlComp.suppressInvalidate = false;
  }, this);

  if (this._ellipsis) {
    this._ellipsis.setCompact(true);
  }
};

/**
 * Restores to the previous state of the compact property.
 */
scout.ActionBoxLayout.prototype.undoCompactActions = function(actions) {
  actions = actions || this.visibleActions();
  actions.forEach(function(action) {
    if (action.compactOrig === undefined) {
      return;
    }
    // Restore old compact state
    action.htmlComp.suppressInvalidate = true;
    action.setCompact(action.compactOrig);
    action.htmlComp.suppressInvalidate = false;
    action.compactOrig = undefined;
  }, this);

  if (this._ellipsis) {
    this._ellipsis.setCompact(false);
  }
};

scout.ActionBoxLayout.prototype.shrink = function(actions) {
  this.shrinkActions(actions);
};

/**
 * Makes the text invisible of all actions with an icon.
 */
scout.ActionBoxLayout.prototype.shrinkActions = function(actions) {
  actions = actions || this.visibleActions();
  actions.forEach(function(action) {
    if (action.textVisibleOrig !== undefined) {
      // already done
      return;
    }
    if (action.iconId) {
      action.textVisibleOrig = action.textVisible;
      action.htmlComp.suppressInvalidate = true;
      action.setTextVisible(false);
      action.htmlComp.suppressInvalidate = false;
    }
  }, this);
};

scout.ActionBoxLayout.prototype.undoShrink = function(actions) {
  this.undoShrinkActions(actions);
};

scout.ActionBoxLayout.prototype.undoShrinkActions = function(actions) {
  actions = actions || this.visibleActions();
  actions.forEach(function(action) {
    if (action.textVisibleOrig === undefined) {
      return;
    }
    // Restore old text visible state
    action.htmlComp.suppressInvalidate = true;
    action.setTextVisible(action.textVisibleOrig);
    action.htmlComp.suppressInvalidate = false;
    action.textVisibleOrig = undefined;
  }, this);
};

scout.ActionBoxLayout.prototype.collapse = function(actions, containerSize, actionsWidth) {
  this._createAndRenderEllipsis(this.actionBox.$container);
  this._moveOverflowActionsIntoEllipsis(containerSize, actionsWidth);
};

/**
 * Undoes the collapsing by removing ellipsis and rendering non rendered actions.
 */
scout.ActionBoxLayout.prototype.undoCollapse = function(actions) {
  actions = actions || this.visibleActions();
  this._destroyEllipsis();
  this._removeActionsFromEllipsis(actions, this.actionBox.$container);
};

scout.ActionBoxLayout.prototype._createAndRenderEllipsis = function($parent) {
  var ellipsis = scout.create('Menu', {
    parent: this.actionBox,
    horizontalAlignment: 1,
    iconId: scout.icons.ELLIPSIS_V,
    tabbable: false,
    compact: this.actionBox.compact
  });
  ellipsis._customCssClasses = this.actionBox.customActionCssClasses;
  ellipsis.render($parent);
  this._ellipsis = ellipsis;
};

scout.ActionBoxLayout.prototype._destroyEllipsis = function() {
  if (this._ellipsis) {
    this._ellipsis.destroy();
    this._ellipsis = null;
  }
};

/**
 * Moves every action which doesn't fit into the container into the ellipsis menu
 */
scout.ActionBoxLayout.prototype._moveOverflowActionsIntoEllipsis = function(containerSize, actionsWidth) {
  var ellipsisSize = scout.graphics.getSize(this._ellipsis.$container, true);
  actionsWidth += ellipsisSize.width;
  this.visibleActions().slice().reverse().forEach(function(action) {
    var actionSize;
    if (actionsWidth > containerSize.width) {
      // Action does not fit -> move to ellipsis action
      actionSize = scout.graphics.getSize(action.$container, true);
      actionsWidth -= actionSize.width;
      this._moveActionIntoEllipsis(action);
    }
  }, this);
};

scout.ActionBoxLayout.prototype._moveActionIntoEllipsis = function(action) {
  action.remove();
  action.overflow = true;
  this._ellipsis.childActions.push(action);
};

scout.ActionBoxLayout.prototype._removeActionsFromEllipsis = function(actions) {
  actions = actions || this.visibleActions();
  actions.forEach(function(action) {
    if (!action.rendered) {
      action.render(this.actionBox.$container);
    }
  }, this);
};

scout.ActionBoxLayout.prototype.actualSize = function(actions) {
  var actionsWidth, prefSize;

  actions = actions || this.visibleActions();
  actionsWidth = this._actionsWidth(actions);
  prefSize = scout.graphics.prefSize(this.actionBox.$container, true, true);
  prefSize.width = actionsWidth + this.actionBox.htmlComp.getInsets().horizontal();

  return prefSize;
};

/**
 * @return the current width of all actions incl. the ellipsis
 */
scout.ActionBoxLayout.prototype._actionsWidth = function(actions) {
  var actionsWidth = 0;
  actions = actions || this.visibleActions();
  actions.forEach(function(action) {
    if (action.rendered) {
      actionsWidth += action.$container.outerWidth(true);
    }
  }, this);
  if (this._ellipsis) {
    actionsWidth += this._ellipsis.$container.outerWidth(true);
  }
  return actionsWidth;
};

scout.ActionBoxLayout.prototype.compactPrefSize = function(actions) {
  actions = actions || this.visibleActions();

  this.undoCollapse(actions);
  this.undoShrink(actions);
  this.compact(actions);

  return this.actualSize();
};

scout.ActionBoxLayout.prototype.smallPrefSize = function(actions) {
  actions = actions || this.visibleActions();

  this.undoCollapse(actions);
  this.compact(actions);
  this.shrink(actions);

  return this.actualSize();
};

scout.ActionBoxLayout.prototype.visibleActions = function() {
  return this.actionBox.actions.filter(function(action) {
    return action.visible;
  }, this);
};
