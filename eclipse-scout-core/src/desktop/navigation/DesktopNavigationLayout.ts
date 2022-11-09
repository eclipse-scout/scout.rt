/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, DesktopNavigation, Dimension, HtmlCompPrefSizeOptions} from '../../index';

export class DesktopNavigationLayout extends AbstractLayout {
  navigation: DesktopNavigation;

  constructor(navigation: DesktopNavigation) {
    super();
    this.navigation = navigation;
  }

  override layout($container: JQuery) {
    let htmlContainer = this.navigation.htmlComp;
    let containerSize = htmlContainer.size({exact: true})
      .subtract(htmlContainer.insets());

    let toolBox = this.navigation.toolBox;
    let viewButtonBoxHeight = 0;
    let viewButtonBoxWidth = 0;
    let viewButtonBox = this.navigation.viewButtonBox;
    if (viewButtonBox.visible) {
      let viewButtonBoxPrefSize = viewButtonBox.htmlComp.prefSize(true);
      viewButtonBoxHeight = viewButtonBoxPrefSize.height;
      viewButtonBoxWidth = containerSize.width;
      if (toolBox) {
        viewButtonBoxWidth = viewButtonBoxPrefSize.width;
      }

      let viewButtonBoxSize = new Dimension(viewButtonBoxWidth, viewButtonBoxHeight)
        .subtract(viewButtonBox.htmlComp.margins());
      viewButtonBox.htmlComp.setSize(viewButtonBoxSize);
    }

    if (toolBox) {
      toolBox.$container.cssLeft(viewButtonBoxWidth);
      let toolBoxSize = new Dimension(containerSize.width - viewButtonBoxWidth, viewButtonBoxHeight)
        .subtract(toolBox.htmlComp.margins());
      toolBox.htmlComp.setSize(toolBoxSize);
    }

    let htmlBody = this.navigation.htmlCompBody;
    let bodySize = new Dimension(containerSize.width, containerSize.height)
      .subtract(htmlBody.margins());
    htmlBody.$comp.cssTop(viewButtonBoxHeight);
    bodySize.height -= viewButtonBoxHeight;
    htmlBody.setSize(bodySize);
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let htmlBody = this.navigation.htmlCompBody;
    let prefSize = htmlBody.prefSize(options)
      .add(htmlBody.margins());

    let prefSizeBoxes = new Dimension(0, 0);
    let viewButtonBox = this.navigation.viewButtonBox;
    if (viewButtonBox) {
      let prefSizeViewButtonBox = viewButtonBox.htmlComp.prefSize(true);
      prefSizeBoxes.width += prefSizeViewButtonBox.width;
      prefSizeBoxes.height = Math.max(prefSizeBoxes.height, prefSizeViewButtonBox.height);
    }
    let toolBox = this.navigation.toolBox;
    if (toolBox) {
      let prefSizeToolBox = toolBox.htmlComp.prefSize(true);
      prefSizeBoxes.width += prefSizeToolBox.width;
      prefSizeBoxes.height = Math.max(prefSizeBoxes.height, prefSizeToolBox.height);
    }

    let htmlContainer = this.navigation.htmlComp;
    prefSize.height += prefSizeBoxes.height;
    prefSize.width = Math.max(prefSize.width, prefSizeBoxes.width);
    prefSize = prefSize.add(htmlContainer.insets());

    return prefSize;
  }
}
