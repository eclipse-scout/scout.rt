import SimpleTabBoxController from '../tabbox/SimpleTabBoxController';
import DesktopTab from './DesktopTab';
import Scout from '../Scout';

export default class DesktopTabBoxController extends SimpleTabBoxController {

  constructor() {
    super();
  }

  _createTab(view) {
    return Scout.create(DesktopTab, {
      parent: this.tabArea,
      view: view
    });
  };

}
