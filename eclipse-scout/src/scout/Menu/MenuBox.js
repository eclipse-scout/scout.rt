import Widget from '../Widget/Widget';
import HtmlComponent from '../Layout/HtmlComponent';
import MenuBoxLayout from './MenuBoxLayout';

export default class MenuBox extends Widget {
    constructor(menuBar){
        super();
        this.compact = false;
        this.menus = [];
        this._addWidgetProperties('menus');
    }

    _init(options) {
        super._init(options);
        this.menus = options.menus || [];
        this.uiMenuCssClass = options.uiMenuCssClass || '';
        this.uiMenuCssClass += ' ' + 'menu-box-item';
        this._initMenus(this.menus);
    };

    _initMenus(menus) {
        menus.forEach(this._initMenu.bind(this));
    };

    _initMenu(menu) {
        menu.uiCssClass = this.uiMenuCssClass;
    };

    /**
     * @override Widget.js
     */
    _render() {
        this.$container = this.$parent.appendDiv('menu-box');

        this.htmlComp = HtmlComponent.install(this.$container, this.session);
        this.htmlComp.setLayout(new MenuBoxLayout(this));
    };

    _renderProperties() {
        super._renderProperties();
        this._renderMenus();
        this._renderCompact();
    };

    _renderMenus() {
        this.menus.forEach(function(menu) {
            menu.render();
        }, this);
        this.invalidateLayoutTree();
    };

    _removeMenus() {
        this.menus.forEach(function(menu) {
            menu.remove();
        });
        this.invalidateLayoutTree();
    };

    _renderCompact() {
        this.$container.toggleClass('compact', this.compact);
        this.invalidateLayoutTree();
    };

    setCompact(compact) {
        this.setProperty('compact', compact);
    };

    setMenus(menus) {
        this.setProperty('menus', menus);
    };

}
