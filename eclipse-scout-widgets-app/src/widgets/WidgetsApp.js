import { Scout, App, Desktop, Models, OutlineViewButton } from 'eclipse-scout';

import desktopModel from './Desktop.json';

export default class WidgetsApp extends App {

  _createDesktop(parent) {
    let desktop = Scout.create(Desktop, Models.getModel(desktopModel, parent));
    let button1Model = {
      parent: desktop,
      text: 'Data',
      displayStyle: 'TAB'
    };
    let button1 = Scout.create(OutlineViewButton, button1Model);

    let button2Model = {
      parent: desktop,
      text: 'Data2',
      displayStyle: 'TAB'
    };
    let button2 = Scout.create(OutlineViewButton, button2Model);

    desktop._setViewButtons([button1, button2]);
    return desktop;
  }

}
