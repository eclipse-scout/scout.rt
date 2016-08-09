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
scout.SimpleTabViewContentLayout = function(tabBox) {
  scout.SimpleTabViewContentLayout.parent.call(this);
  this.tabBox = tabBox;
};
scout.inherits(scout.SimpleTabViewContentLayout, scout.AbstractLayout);

// FIXME [6.1] BSH/CGU Improve this
scout.SimpleTabViewContentLayout.prototype.layout = function($container) {
  var currentView = this.tabBox.currentView;
  if (!currentView || !currentView.rendered || !currentView.htmlComp) {
    return;
  }

  var htmlContainer = scout.HtmlComponent.get($container);
  var size = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets())
    .subtract(currentView.htmlComp.getMargins());

  currentView.htmlComp.setSize(size);
};

scout.SimpleTabViewContentLayout.prototype.preferredLayoutSize = function($container, options) {
  options = options || {};
  var htmlContainer = this.tabBox.viewContent,
    containerInsets = htmlContainer.getInsets(),
    prefSize = new scout.Dimension();

  var currentView = this.tabBox.currentView;
  if (currentView && currentView.rendered && currentView.htmlComp) { // XXX Is this check necessary?
    var htmlInnerComp = currentView.htmlComp;
    if (htmlInnerComp && htmlInnerComp.isVisible()) {
      var innerMargins = htmlInnerComp.getMargins();
      var innerOptions = {};
      if (options.widthHint) {
        innerOptions.widthHint = options.widthHint - containerInsets.horizontal() - innerMargins.horizontal();
      }
      if (options.heightHint) {
        innerOptions.heightHint = options.heightHint - containerInsets.vertical() - innerMargins.vertical();
      }

      var innerPrefSize = currentView.htmlComp.getPreferredSize(innerOptions);
      prefSize.width += innerPrefSize.width + innerMargins.horizontal();
      prefSize.height += innerPrefSize.height + innerMargins.vertical();
    }
  }

  return prefSize.add(containerInsets);

//  options = options || {};
//  var htmlContainer = this.tabBox.htmlComp,
//    containerInsets = htmlContainer.getInsets(),
//    prefSize = new scout.Dimension();
//
//  var currentView = this.tabBox.currentView;
//  if (currentView && currentView.rendered && currentView.htmlComp) { // XXX Is this check necessary?
//    var innerMargins = currentView.htmlComp.getMargins();
//    var innerOptions = {};
//    if (options.widthHint) {
//      innerOptions.widthHint = options.widthHint - containerInsets.horizontal() - innerMargins.horizontal();
//    }
//    if (options.heightHint) {
//      innerOptions.heightHint = options.heightHint - containerInsets.vertical() - innerMargins.vertical();
//    }
//
//    var innerPrefSize = currentView.htmlComp.getPreferredSize(innerOptions);
//    prefSize.width += innerPrefSize.width + innerMargins.horizontal();
//    prefSize.height += innerPrefSize.height + innerMargins.vertical();
//  }
//
//  return prefSize.add(containerInsets);

//  var currentView = this.tabBox.currentView;
//  if (!currentView || !currentView.rendered || !currentView.htmlComp) {
//    return new scout.Dimension();
//  }
//
//  var htmlContainer = scout.HtmlComponent.get($container);
//  var prefSize = currentView.htmlComp.getPreferredSize()
//    .add(htmlContainer.getInsets())
//    .add(currentView.htmlComp.getMargins());
//
//  return prefSize;
};
