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
  this.viewTabAreaVisible = true;
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
  // link
  this.localViewTabAreaController = new scout.ViewTabAreaController(this, this.viewTabArea);
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

  this._renderView(this.currentView);
};

scout.ViewArea.prototype._renderviewTabArea = function() {
  this.viewTabArea.render(this.$container);
  this.$viewTabArea = this.viewTabArea.$container;
};

scout.ViewArea.prototype._renderView = function(view) {
  if (!view) {
    return;
  }
  if (view.rendered) {
    return;
  }
  view.render(this.$viewContent);
  view.setParent(this);
  view.$container.addClass('view');
  if (view.uiCssClasses) {
    scout.arrays.ensure(view.uiCssClasses).forEach(function(cssClass) {
      view.$container.addClass(cssClass);
    }.bind(this));
  }
  view.validateRoot = true;
};

scout.ViewArea.prototype.postRender = function() {
  if(this.viewStack.length > 0 && !this.currentView){
    this.activateView(this.viewStack[this.viewStack.length -1]);
  }
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
  if (view === this.currentView) {
    return;
  }

  if (this.currentView) {
    this.currentView.detach();
    this.trigger('viewDeactivated', {
      view: this.currentView
    });
    this.currentView = null;
  }
  // ensure rendered
  if (this.rendered) {
    this._renderView(view);
  }
  if (!view.attached) {
    view.attach();
  }
  this.currentView = view;

  this.trigger('viewActivated', {
    view: view
  });

  if (this.rendered) {
    this.viewContent.invalidateLayoutTree();
    // Layout immediate to prevent 'laggy' form visualization,
    // but not initially while desktop gets rendered because it will be done at the end anyway
    this.viewContent.validateLayoutTree();
  }
};

scout.ViewArea.prototype.addView = function(view, activate) {
  activate = scout.nvl(activate, true);
  // add to view stack
  var siblingView = this._addToViewStack(view);
  this.trigger('viewAdded', {
    view: view,
    siblingView: siblingView
  });

  if(activate){
    this.activateView(view);
  }
};

/**
 *
 * @param view
 * @return the view which is gonna be the sibling to insert the new view tab after.
 */
scout.ViewArea.prototype._addToViewStack = function(view) {
  var sibling;
  var index = this.viewStack.indexOf(view);
  if(index > -1){
    return this.viewStack[index-1];
  }

  if (!scout.ViewTabAreaController.hasViewTab(view)) {
    // first
    this.viewStack.unshift(view);
    return sibling;
  }
  if (!this.currentView) {
    // end
    sibling = this.viewStack[this.viewStack.length - 1];
    this.viewStack.push(view);
    return;
  }
  var currentIndex = this.viewStack.indexOf(this.currentView);
  sibling = this.viewStack[currentIndex];
  // it does not matter when index is -1 will be inserted at first position
  this.viewStack.splice(currentIndex + 1, 0, view);
  return sibling;
};

scout.ViewArea.prototype.removeView = function(view, showSiblingView) {

  if (!view) {
    return;
  }
  showSiblingView = scout.nvl(showSiblingView, true);
  var index = this.viewStack.indexOf(view);
  if (index > -1) {
    // activate previous
    if (showSiblingView) {
      if (index - 1 >= 0) {
        this.activateView(this.viewStack[index - 1]);
      } else if (index + 1 < this.viewStack.length) {
        this.activateView(this.viewStack[index + 1]);
      }
    }

    // remove
    this.viewStack.splice(index, 1);
    if (view.rendered) {
      view.remove();
    }
    this.trigger('viewRemoved', {
      view: view
    });

    if (this.rendered) {

      this.viewContent.invalidateLayoutTree();
      this.viewContent.validateLayoutTree();
    }
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
