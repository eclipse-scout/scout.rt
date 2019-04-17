import SimpleTabBoxController from '../tabbox/SimpleTabBoxController';
import DesktopTab from './DesktopTab';
import * as scout from '../scout';

export default class DesktopTabBoxController extends SimpleTabBoxController {

  constructor() {
    super();
  }

  _createTab(view) {
    return scout.create(DesktopTab, {
      parent: this.tabArea,
      view: view
    });
  };

}
