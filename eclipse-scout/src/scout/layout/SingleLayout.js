import AbstractLayout from './AbstractLayout';
import HtmlComponent from './HtmlComponent';
import Dimension from '../Utils/Dimension';
import Graphics from '../Utils/Graphics';

export default class SingleLayout extends AbstractLayout {

    constructor(htmlChild) {
        super();
        this._htmlChild = htmlChild;
    }

    layout($container) {
        var htmlContainer = HtmlComponent.get($container);
        var childSize = htmlContainer.availableSize()
                .subtract(htmlContainer.insets()),
            htmlChild = this._htmlChild;

        if (!htmlChild) {
            htmlChild = this._getHtmlSingleChild($container);
        }
        if (htmlChild) {
            htmlChild.setSize(childSize);
        }
    };

    preferredLayoutSize($container, options) {
        var htmlChild = this._htmlChild;
        if (!htmlChild) {
            htmlChild = this._getHtmlSingleChild($container);
        }
        if (htmlChild) {
            return htmlChild.prefSize(options).add(Graphics.insets($container));
        } else {
            return new Dimension(1, 1);
        }
    };

    /**
     * @returns a HtmlComponent instance for the first child of the given container or null if the container has no children.
     */
    _getHtmlSingleChild($container) {
        var $firstChild = $container.children().first();
        if ($firstChild.length) {
            return HtmlComponent.get($firstChild);
        } else {
            return null;
        }
    };

}

