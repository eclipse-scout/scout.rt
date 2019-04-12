import CollapseHandle from './CollapseHandle';

//require('./DesktopNavigationHandle.less');

export default class DesktopNavigationHandle extends CollapseHandle {
    constructor() {
        super();
    }

    _initKeyStrokeContext() {
        super._initKeyStrokeContext();

        // Bound to desktop
        /*this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
        this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
        this.desktopKeyStrokeContext.$scopeTarget = this.session.desktop.$container;
        this.desktopKeyStrokeContext.registerKeyStroke([
            new scout.ShrinkNavigationKeyStroke(this),
            new scout.EnlargeNavigationKeyStroke(this)
        ]);*/
    };

    _render() {
        super._render();
        this.$container.addClass('desktop-navigation-handle');
        //this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
    };

    _remove() {
        super._remove();
        //this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
    };

}
