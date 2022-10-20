/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Dimension, HtmlComponent, scout} from '../index';
import $ from 'jquery';

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

  _getChildren($container) {
    return $container.children();
  }

  layout($container) {
    let htmlComp = HtmlComponent.get($container);
    let containerSize = htmlComp.availableSize()
      .subtract(htmlComp.insets());

    this._getChildren($container).each((index, elem) => {
      let $elem = $(elem);
      let htmlChild = HtmlComponent.optGet($elem);
      if (!htmlChild || !$elem.isVisible()) {
        return;
      }
      if (!this.pixelBasedSizing) {
        htmlChild.validateLayout();
        return;
      }
      let prefSize = htmlChild.prefSize({
        widthHint: containerSize.width
      });

      if (this.stretch) {
        // All elements in a row layout have the same width which is the width of the container
        prefSize.width = containerSize.width - htmlChild.margins().horizontal();
      }

      htmlChild.setSize(prefSize);
    });
  }

  preferredLayoutSize($container, options) {
    let prefSize = new Dimension(),
      htmlContainer = HtmlComponent.get($container),
      maxWidth = 0;

    this._getChildren($container).each((index, elem) => {
      let $elem = $(elem);
      let htmlChild = HtmlComponent.optGet($elem);
      if (!htmlChild || !$elem.isVisible()) {
        return;
      }
      let htmlChildPrefSize = htmlChild.prefSize(options)
        .add(htmlChild.margins());
      maxWidth = Math.max(htmlChildPrefSize.width, maxWidth);
      prefSize.height += htmlChildPrefSize.height;
      prefSize.width = maxWidth;
    });

    prefSize = prefSize.add(htmlContainer.insets());
    return prefSize;
  }
}
