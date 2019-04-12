import MenuBox from '../Menu/MenuBox';
import Strings from '../Utils/Strings';

//require('./DesktopToolBox.less');

export default class DesktopToolBox extends MenuBox {
    constructor(menuBar) {
        super(menuBar);
    }

    _init(options) {
        options.uiMenuCssClass = Strings.join(' ', options.uiMenuCssClass, 'desktop-tool-box-item');
        super._init(options)
    };

    /**
     * @override
     */
    _initMenu(menu) {
        super._initMenu(menu);
        menu.popupOpeningDirectionX = 'left';
    };

    /**
     * @override
     */
    _render() {
        super._render();
        this.$container.addClass('desktop-tool-box');
    };

}
