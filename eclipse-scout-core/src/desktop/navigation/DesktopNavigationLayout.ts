/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, DesktopNavigation, Dimension, graphics, HtmlCompPrefSizeOptions} from '../../index';

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
      let toolBoxSize;
      let outline = this.navigation.outline;
      if (viewButtonBoxWidth === 0 && outline && outline.$title) {
        // If there is no view button box, the outline title will be moved up.
        // If there is no outline title, the tool box will take the whole width (else case)
        let outlineTitleWidth = graphics.prefSize(outline.$title).width;
        toolBoxSize = new Dimension(containerSize.width - outlineTitleWidth, 0) // height is set by css
          .subtract(toolBox.htmlComp.margins());
      } else {
        toolBoxSize = new Dimension(containerSize.width - viewButtonBoxWidth, viewButtonBoxHeight)
          .subtract(toolBox.htmlComp.margins());
      }
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
