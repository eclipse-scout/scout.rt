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
import {AbstractLayout, Dimension, graphics, HtmlComponent} from '../index';
import $ from 'jquery';

/**
 * Resizes the child so it has the same size as the container.<br>
 * If no child is provided, the first child in the container is used.
 */
export default class SingleLayout extends AbstractLayout {

  constructor(htmlChild, options) {
    super();
    this._htmlChild = htmlChild;
    options = options || {};
    this.exact = scout.nvl(options.exact, false);
  }

  layout($container) {
    let htmlContainer = HtmlComponent.get($container);
    let childSize = htmlContainer.availableSize({exact: this.exact})
        .subtract(htmlContainer.insets()),
      htmlChild = this._htmlChild;

    if (!htmlChild) {
      htmlChild = this._getHtmlSingleChild($container);
    }
    if (htmlChild) {
      htmlChild.setSize(childSize);
    }
  }

  preferredLayoutSize($container, options) {
    let htmlChild = this._htmlChild;
    if (!htmlChild) {
      htmlChild = this._getHtmlSingleChild($container);
    }
    if (htmlChild) {
      return htmlChild.prefSize(options).add(graphics.insets($container));
    }
    return new Dimension(1, 1);
  }

  /**
   * @returns {HtmlComponent} the first child html component of the given container or null if the container has no child with a html component or no children at all.
   */
  _getHtmlSingleChild($container) {
    let htmlComp = null;
    $container.children().each((i, child) => {
      let htmlChild = HtmlComponent.optGet($(child));
      if (htmlChild) {
        htmlComp = htmlChild;
        return false;
      }
    });
    return htmlComp;
  }
}
