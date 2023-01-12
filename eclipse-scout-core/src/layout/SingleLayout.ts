/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, graphics, HtmlComponent, HtmlCompPrefSizeOptions, scout} from '../index';
import $ from 'jquery';

export interface SingleLayoutOptions {
  /** True to use the exact size including fractional digits of the container. See also {@link HtmlComponent.availableSize}. Default is false. */
  exact: boolean;
}

/**
 * Resizes the child so it has the same size as the container.<br>
 * If no child is provided, the first child in the container is used.
 */
export class SingleLayout extends AbstractLayout implements SingleLayoutOptions {
  exact: boolean;
  protected _htmlChild: HtmlComponent;

  constructor(htmlChild?: HtmlComponent, options?: SingleLayoutOptions) {
    super();
    this._htmlChild = htmlChild;
    options = options || {} as SingleLayoutOptions;
    this.exact = scout.nvl(options.exact, false);
  }

  override layout($container: JQuery) {
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

  override preferredLayoutSize($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
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
   * @returns the first child html component of the given container or null if the container has no child with a html component or no children at all.
   */
  protected _getHtmlSingleChild($container: JQuery): HtmlComponent {
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
