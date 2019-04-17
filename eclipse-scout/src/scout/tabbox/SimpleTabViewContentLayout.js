import AbstractLayout from '../layout/AbstractLayout';
import Dimension from '../util/Dimension';
import HtmlComponent from '../layout/HtmlComponent';

export default class SimpleTabViewContentLayout extends AbstractLayout {

  constructor(tabBox) {
    super();
    this.tabBox = tabBox;
  }

  layout($container) {
    var currentView = this.tabBox.currentView;
    if (!currentView || !currentView.rendered || !currentView.htmlComp) {
      return;
    }

    var htmlContainer = HtmlComponent.get($container);
    var size = htmlContainer.availableSize()
      .subtract(htmlContainer.insets())
      .subtract(currentView.htmlComp.margins());

    currentView.htmlComp.setSize(size);
  };

  preferredLayoutSize($container) {
    var currentView = this.tabBox.currentView;
    if (!currentView || !currentView.rendered || !currentView.htmlComp) {
      return new Dimension();
    }

    var htmlContainer = HtmlComponent.get($container);
    var prefSize = currentView.htmlComp.prefSize()
      .add(htmlContainer.insets())
      .add(currentView.htmlComp.margins());

    return prefSize;
  };

}
