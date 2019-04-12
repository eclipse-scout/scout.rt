import AbstractLayout from '../Layout/AbstractLayout';
import Dimension from '../Utils/Dimension';
import HtmlComponent from '../Layout/HtmlComponent';

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

