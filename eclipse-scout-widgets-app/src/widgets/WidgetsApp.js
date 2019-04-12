import { Scout, App, Desktop, Models, OutlineViewButton } from 'eclipse-scout';

import desktopModel from './Desktop.json'; // FIXME [awe] was passiert mit den extensions wenn wir das JSON direkt im JS Code haben?

export default class WidgetsApp extends App {

  _createDesktop(parent) {
    let desktop = Scout.create(Desktop, Models.getModel(desktopModel, parent));
    // FIXME [awe] add example wiht custom OutlineViewButton in app-project
    let dataButton = Scout.create(OutlineViewButton, {
      parent: desktop,
      text: 'Data',
      displayStyle: 'TAB'
    });
    let searchButton = Scout.create(OutlineViewButton, {
      parent: desktop,
      text: 'Search',
      displayStyle: 'TAB'
    });
    desktop._setViewButtons([dataButton, searchButton]);
    return desktop;
  }

}
