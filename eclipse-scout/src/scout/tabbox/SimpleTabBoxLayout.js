import AbstractLayout from '../layout/AbstractLayout';
import HtmlComponent from '../layout/HtmlComponent';
import Dimension from '../util/Dimension';

export default class SimpleTabBoxLayout extends AbstractLayout {

  constructor(tabBox) {
    super();
    this.tabBox = tabBox;
  }

  layout($container) {
    var containerSize, viewContentSize,
      htmlContainer = HtmlComponent.get($container),
      htmlViewContent = HtmlComponent.get(this.tabBox.$viewContent),
      tabAreaSize;

    containerSize = htmlContainer.availableSize({
      exact: true
    })
      .subtract(htmlContainer.insets());

    tabAreaSize = this._layoutTabArea(containerSize);

    viewContentSize = containerSize.subtract(htmlViewContent.margins());
    viewContentSize.height -= tabAreaSize.height;
    htmlViewContent.setSize(viewContentSize);
  };

  _layoutTabArea(containerSize) {
    if (!this.tabBox.rendered) {
      return new Dimension(0, 0);
    }
    // expected the tab area is layouted dynamically only
    var htmlViewTabs = HtmlComponent.get(this.tabBox.$tabArea),
      prefSize = htmlViewTabs.prefSize(),
      margins = htmlViewTabs.margins();
    var size = new Dimension(containerSize.width, prefSize.height + margins.top + margins.bottom);
    htmlViewTabs.setSize(size);
    return size;
  };

  /**
   * Preferred size of the tab-box aligns every tab-item in a single line, so that each item is visible.
   */
  preferredLayoutSize($container, options) {
    options = options || {};
    var htmlContainer = HtmlComponent.get($container),
      htmlViewContent = HtmlComponent.get(this.tabBox.$viewContent),
      htmlViewTabs = HtmlComponent.get(this.tabBox.$tabArea),
      viewTabsSize = new Dimension(),
      viewContentSize = new Dimension();

    // HeightHint not supported
    options.heightHint = null;

    if (htmlViewTabs.isVisible()) {
      viewTabsSize = htmlViewTabs.prefSize()
        .add(htmlViewTabs.margins());
    }

    viewContentSize = htmlViewContent.prefSize(options)
      .add(htmlContainer.insets())
      .add(htmlViewContent.margins());

    return new Dimension(
      Math.max(viewTabsSize.width, viewContentSize.width),
      viewContentSize.height + viewTabsSize.height);
  };

}
