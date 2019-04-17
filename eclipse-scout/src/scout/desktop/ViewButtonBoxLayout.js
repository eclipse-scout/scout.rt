import AbstractLayout from '../layout/AbstractLayout';
import * as scout from '../scout';
import * as graphics from '../utils/graphics';

export default class ViewButtonBoxLayout extends AbstractLayout {

  constructor(viewButtonBox) {
    super();
    this.viewButtonBox = viewButtonBox;
  }

  layout($container) {
    var tabs = this.viewButtonBox.tabButtons.filter(function(tab) {
        return tab.visible;
      }),
      viewMenuTab = this.viewButtonBox.viewMenuTab,
      htmlComp = this.viewButtonBox.htmlComp,
      containerWidth = htmlComp.size().width,
      tabWidth = containerWidth / tabs.length;

    if (viewMenuTab.visible && viewMenuTab.selectedButton.rendered) {
      if (viewMenuTab.selectedButton) {
        tabWidth = (containerWidth - graphics.size(viewMenuTab.dropdown.$container, {
          exact: true
        }).width) / (tabs.length + 1);
        viewMenuTab.selectedButton.$container.cssPxValue('width', tabWidth);
      }

      containerWidth -= graphics.size(viewMenuTab.$container, {
        exact: true
      }).width;
    }

    tabs.forEach(function(tab, index) {
      if (tabs.length - 1 === index) {
        // to avoid pixel fault due to rounding issues calculate the rest for the last tab.
        // Round up to the second digit otherwise at least Chrome may still show the background of the view button box (at least in compact mode)
        tab.$container.cssWidth(Math.ceil(containerWidth * 100) / 100);
      } else {
        tab.$container.cssWidth(tabWidth);
        containerWidth -= tab.$container.cssWidth();
      }
    }, this);
  };

  preferredLayoutSize($container) {
    // View buttons have an absolute css height set -> useCssSize = true
    return graphics.prefSize($container, {
      useCssSize: true
    });
  };

}
