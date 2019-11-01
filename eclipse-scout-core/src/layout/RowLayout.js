/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout} from '../index';
import {Dimension} from '../index';
import {HtmlComponent} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';

export default class RowLayout extends AbstractLayout {

constructor(options) {
  super();
  options = options || {};
  this.pixelBasedSizing = scout.nvl(options.pixelBasedSizing, true);
  this.stretch = scout.nvl(options.stretch, true);

  // RowLayout = each child element represents a row
  // +-----------------+
  // |                 |
  // +-----------------+
  // |                 |
  // |                 |
  // +-----------------+
  // |                 |
  // +-----------------+
}


layout($container) {
  var htmlComp = HtmlComponent.get($container);
  var containerSize = htmlComp.availableSize()
    .subtract(htmlComp.insets());

  $container.children().each(function(index, elem) {
    var $elem = $(elem);
    var htmlChild = HtmlComponent.optGet($elem);
    if (!htmlChild || !$elem.isVisible()) {
      return;
    }
    if (!this.pixelBasedSizing) {
      htmlChild.validateLayout();
      return;
    }
    var prefSize = htmlChild.prefSize({
      widthHint: containerSize.width
    });

    if (this.stretch) {
      // All elements in a row layout have the same width which is the width of the container
      prefSize.width = containerSize.width - htmlChild.margins().horizontal();
    }

    htmlChild.setSize(prefSize);
  }.bind(this));
}

preferredLayoutSize($container, options) {
  var prefSize = new Dimension(),
    htmlContainer = HtmlComponent.get($container),
    maxWidth = 0;

  $container.children().each(function(index, elem) {
    var $elem = $(elem);
    var htmlChild = HtmlComponent.optGet($elem);
    if (!htmlChild || !$elem.isVisible()) {
      return;
    }
    var htmlChildPrefSize = htmlChild.prefSize(options)
      .add(htmlChild.margins());
    maxWidth = Math.max(htmlChildPrefSize.width, maxWidth);
    prefSize.height += htmlChildPrefSize.height;
    prefSize.width = maxWidth;
  });

  prefSize = prefSize.add(htmlContainer.insets());
  return prefSize;
}
}
