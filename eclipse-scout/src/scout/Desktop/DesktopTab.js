import SimpleTab from '../TabBox/SimpleTab';

//require('./DesktopTab.less');

export default class DesktopTab extends SimpleTab {

    constructor() {
        super();
    }

    _render() {
        super._render();
        this.$container.addClass('desktop-tab');
    };

}


