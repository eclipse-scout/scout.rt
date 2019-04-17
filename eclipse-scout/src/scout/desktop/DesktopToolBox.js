import MenuBox from '../menu/MenuBox';
import * as strings from '../utils/strings';

export default class DesktopToolBox extends MenuBox {
  constructor(menuBar) {
    super(menuBar);
  }

  _init(options) {
    options.uiMenuCssClass = strings.join(' ', options.uiMenuCssClass, 'desktop-tool-box-item');
    super._init(options);
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
