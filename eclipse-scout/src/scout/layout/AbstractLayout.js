import * as $ from 'jquery';
import HtmlComponent from './HtmlComponent';
import * as graphics from '../utils/graphics';

export default class AbstractLayout {

    constructor(){
        this.animateClasses = [];
    }

    /**
     * Called when layout is invalidated. An implementation should delete cached layout-information
     * when it is invalidated.
     *
     * May be implemented by sub-class.
     */
    invalidate() { //
    };

    /**
     * Layouts children of the given $container, according to the implemented layout algorithm.
     * The implementation should call setSize or setBounds on its children.
     *
     * Must be implemented by sub-class.
     */
    layout($container) { //
    };

    _revertSizeHintsAdjustments($container, options) {
        var htmlContainer = HtmlComponent.get($container);
        if (options.widthHint) {
            options.widthHint += htmlContainer.insets().horizontal();
        }
        if (options.heightHint) {
            options.heightHint += htmlContainer.insets().vertical();
        }
    };

    /**
     * Returns the preferred size of the given $container.
     *
     * @return scout.Dimension preferred size
     */
    preferredLayoutSize($container, options) {
        options = $.extend({}, options);
        if (this.animateClasses.length > 0) {
            options.animateClasses = this.animateClasses;
        }
        // Insets have been removed automatically by the html component with the assumption that the layout will pass it to its child elements.
        // Since this is not the case in this generic layout the insets have to be added again, otherwise the sizes used to measure would too small.
        this._revertSizeHintsAdjustments($container, options);
        return graphics.prefSize($container, options);
    };

}

