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
scout.ViewArea = function() {
  scout.ViewArea.parent.call(this);
  this.$body;
  this.$title;
  this.htmlComp;
  this.viewTabArea;
  this.viewStack = [];
  this.viewTabMap = {}; // [key=viewId, value=ViewTab instance]

  this.currentView;

  this.viewTabListener = function(viewTab) {
    this.activateView(viewTab.view);
  }.bind(this);

  this._addEventSupport();
};
scout.inherits(scout.ViewArea, scout.Widget);

scout.ViewArea.prototype._init = function(model) {
  scout.ViewArea.parent.prototype._init.call(this, model);
  //  this.menuBar = scout.create('MenuBar', {
  //    parent: this,
  //    menuOrder: new scout.GroupBoxMenuItemsOrder()
  //  });
  // create view tabs
  this.viewTabArea = scout.create('ViewTabArea', {
    parent: this
  });
};

/**
 * @override ModelAdapter.js
 */
scout.ViewArea.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.ViewArea.parent.prototype._initKeyStrokeContext.call(this, this.keyStrokeContext);
  //  this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  //  this.keyStrokeContext.$bindTarget = this._keyStrokeBindTarget.bind(this);
};

/**
 * Returns a $container used as a bind target for the key-stroke context of the group-box.
 * By default this function returns the container of the form, or when group-box is has no
 * form as a parent the container of the group-box.
 */
scout.ViewArea.prototype._keyStrokeBindTarget = function() {
  return this.$container;
};

scout.ViewArea.prototype._render = function($parent) {
  var htmlBody, i,
    env = scout.HtmlEnvironment;

  this.$container = $parent.appendDiv('view-box');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.validateRoot = true;
  this.htmlComp.setLayout(new scout.ViewAreaLayout(this));
  // render viewTabArea
  this._renderviewTabArea();
  // render content
  this.$viewContent = this.$container.appendDiv('tab-content');
  this.viewContent = new scout.HtmlComponent(this.$viewContent, this.session);
};

scout.ViewArea.prototype._renderviewTabArea = function() {
  if (this.viewTabArea.rendered) {
    return;
  }
  this.viewTabArea.render(this.$container);
  this.$viewTabArea = this.viewTabArea.$container;
};

scout.ViewArea.prototype._remove = function() {
  scout.ViewArea.parent.prototype._remove.call(this);
  if (this.scrollable) {
    scout.scrollbars.uninstall(this.$body);
  }
};

scout.ViewArea.prototype._renderProperties = function() {
  scout.ViewArea.parent.prototype._renderProperties.call(this);

  //  this._renderBorderVisible();
  //  this._renderExpandable();
  //  this._renderExpanded();
  //  this._renderMenuBarVisible();
};

scout.ViewArea.prototype.activateView = function(view) {
  if(view === this.currentView){
    return;
  }
  if (!view) {
    return;
  }
  // render
  if (!this.rendered) {
    this.render(this.parent.$container);
  }

  if (this.currentView) {
    this.currentView.detach();
    this.trigger('viewDeactivated', this.currentView);
    this.currentView = null;
  }
  // ensure rendered
  this._renderView(view);
  if (!view.attached) {
    view.attach();
  }
  this.currentView = view;
  // select view tab
  var viewTab = this.viewTabMap[view.id];
  this.viewTabArea.selectViewTab(viewTab);
  this.trigger('viewActivated', view);

  this.viewContent.invalidateLayoutTree();
  // Layout immediate to prevent 'laggy' form visualization,
  // but not initially while desktop gets rendered because it will be done at the end anyway
  this.viewContent.validateLayoutTree();
};

scout.ViewArea.prototype.showView = function(view) {
  // render
  if (!this.rendered) {
    this.render(this.parent.$container);
  }
  if (!view) {
    return;
  }
  if (this.viewStack.indexOf(view) > -1) {
    this.activateView(view);
    return;
  }
  // add to view stack
  var siblingView = this._addToViewStack(view);
  var siblingViewTab = (siblingView) ? (this.viewTabMap[siblingView.id]) : undefined;

  this._createViewTab(view, siblingViewTab);

  this.activateView(view);
};

/**
 *
 * @param view
 * @return the view which is gonna be the sibling to insert the new view tab after.
 */
scout.ViewArea.prototype._addToViewStack = function(view) {
  if (!scout.objects.someProperties(view, ['title', 'subTitle', 'iconId'])) {
    // first
    this.viewStack.unshift(view);
    return;
  }
  var selectedTab = this.viewTabArea.getSelectedViewTab();
  if (!selectedTab) {
    // end
    this.viewStack.push(view);
    if (this.viewStack.lengh > 1) {
      return this.viewStack[this.viewStack.length - 2];
    }
    return;
  }
  var selectedIndex = this.viewStack.indexOf(selectedTab.view);
  // it does not matter when index is -1 will be inserted at first position
  this.viewStack.splice(selectedIndex + 1, 0, view);
  if (selectedIndex > -1) {
    return this.viewStack[selectedIndex];
  }

};

scout.ViewArea.prototype._renderView = function(view) {
  if (view.rendered) {
    return;
  }
  view.render(this.$viewContent);
  view.setParent(this);
  view.$container.addClass('view');
  view.validateRoot = true;
};

scout.ViewArea.prototype._createViewTab = function(view, siblingViewTab) {
  if (!scout.objects.someProperties(view, ['title', 'subTitle', 'iconId'])) {
    return;
  }
  var viewTab = scout.create('DesktopViewTab', {
    parent: this.viewTabArea,
    view: view
  });
  viewTab.on('tabClicked', this.viewTabListener);
  this.viewTabMap[view.id] = viewTab;
  this.viewTabArea.addTab(viewTab, siblingViewTab);
  return viewTab;
};

scout.ViewArea.prototype.removeView = function(view) {
  if (!view) {
    return;
  }
  var index = this.viewStack.indexOf(view);
  if (index > -1) {
    // activate previous
    if (index - 1 >= 0) {
      this.activateView(this.viewStack[index - 1]);
    } else if (index + 1 < this.viewStack.length) {
      this.activateView(this.viewStack[index + 1]);
    }

    // remove
    this.viewStack.splice(index, 1);
    if (view.rendered) {
      view.remove();
    }
    this._removeViewTab(view);

    // remove if empty
    if (!this.hasViews() && this.rendered) {
      this.remove();
    }

    this.viewContent.invalidateLayoutTree();
    this.viewContent.validateLayoutTree();
  }
};

scout.ViewArea.prototype._removeViewTab = function(view) {
  var viewTab = this.viewTabMap[view.id];
  if (viewTab) {
    viewTab.off('tabClicked', this.viewTabListener);
    this.viewTabArea.removeTab(viewTab);
    delete this.viewTabMap[view.id];
  }
};

scout.ViewArea.prototype.viewCount = function() {
  return this.viewStack.length;
};

scout.ViewArea.prototype.hasViews = function() {
  return this.viewStack.length > 0;
};

scout.ViewArea.prototype.getViews = function(displayViewId) {
  return this.viewStack.filter(function(view) {
    if (!displayViewId) {
      return true;
    }
    return displayViewId === view.displayViewId;
  });
};
