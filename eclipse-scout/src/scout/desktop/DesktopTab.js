import SimpleTab from '../TabBox/SimpleTab';

export default class DesktopTab extends SimpleTab {

  constructor() {
    super();
  }

  _render() {
    super._render();
    this.$container.addClass('desktop-tab');
  };

}
