import AbstractLayout from '../Layout/AbstractLayout';
import Dimension from '../Utils/Dimension';

export default class DesktopNavigationLayout extends AbstractLayout {

  constructor(navigation) {
    super();
    this.navigation = navigation;
  }

  layout($container) {
    var bodySize, viewButtonBoxSize, viewButtonBoxPrefSize,
      htmlContainer = this.navigation.htmlComp,
      containerSize = htmlContainer.size({
        exact: true
      }),
      htmlBody = this.navigation.htmlCompBody,
      toolBox = this.navigation.toolBox,
      viewButtonBox = this.navigation.viewButtonBox,
      viewButtonBoxHeight = 0,
      viewButtonBoxWidth = 0;

    containerSize = containerSize.subtract(htmlContainer.insets());

    if (viewButtonBox.visible) {
      viewButtonBoxPrefSize = viewButtonBox.htmlComp.prefSize();
      viewButtonBoxHeight = viewButtonBoxPrefSize.height;
      viewButtonBoxWidth = containerSize.width;
      if (toolBox) {
        viewButtonBoxWidth = viewButtonBoxPrefSize.width;
      }

      viewButtonBoxSize = new Dimension(viewButtonBoxWidth, viewButtonBoxHeight)
        .subtract(viewButtonBox.htmlComp.margins());
      viewButtonBox.htmlComp.setSize(viewButtonBoxSize);
    }

    if (toolBox) {
      toolBox.$container.cssLeft(viewButtonBoxWidth);
      toolBox.htmlComp.setSize(new Dimension(containerSize.width - viewButtonBoxWidth, viewButtonBoxHeight));
    }

    bodySize = new Dimension(containerSize.width, containerSize.height)
      .subtract(htmlBody.margins());
    if (this.navigation.singleViewButton) {
      htmlBody.$comp.cssTop(0);
    } else {
      htmlBody.$comp.cssTop(viewButtonBoxHeight);
      bodySize.height -= viewButtonBoxHeight;
    }
    htmlBody.setSize(bodySize);
  };

  preferredLayoutSize($container) {
    var htmlContainer = this.navigation.htmlComp,
      htmlBody = this.navigation.htmlCompBody,
      toolBox = this.navigation.toolBox,
      viewButtonBox = this.navigation.viewButtonBox;

    var prefSize = htmlBody.prefSize();

    var prefSizeBoxes = new Dimension(0, 0);
    if (viewButtonBox) {
      var prefSizeViewButtonBox = viewButtonBox.htmlComp.prefSize();
      prefSizeBoxes.width += prefSizeViewButtonBox.width;
      prefSizeBoxes.height = Math.max(prefSizeBoxes.height, prefSizeViewButtonBox.height);
    }
    if (toolBox) {
      var prefSizeToolBox = toolBox.htmlComp.prefSize();
      prefSizeBoxes.width += prefSizeToolBox.width;
      prefSizeBoxes.height = Math.max(prefSizeBoxes.height, prefSizeToolBox.height);
    }

    prefSize.height += prefSizeBoxes.height;
    prefSize.width = Math.max(prefSize.width, prefSizeBoxes.width);
    prefSize = prefSize.add(htmlContainer.insets());

    return prefSize;
  };

}
