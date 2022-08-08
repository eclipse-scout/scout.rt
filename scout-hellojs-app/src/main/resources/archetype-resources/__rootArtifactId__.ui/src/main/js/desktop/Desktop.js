import {App, Desktop as ScoutDesktop, icons, models, scout} from '@eclipse-scout/core';
import DesktopModel from './DesktopModel';

export default class Desktop extends ScoutDesktop {

  constructor() {
    super();
  }

  _jsonModel() {
    return models.get(DesktopModel);
  }

  _init(model) {
    super._init(model);

    let aboutMenu = this.widget('AboutMenu');
    aboutMenu.on('action', this._onAboutMenuAction.bind(this));

    let defaultThemeMenu = this.widget('DefaultThemeMenu');
    defaultThemeMenu.on('action', this._onDefaultThemeMenuAction.bind(this));

    let darkThemeMenu = this.widget('DarkThemeMenu');
    darkThemeMenu.on('action', this._onDarkThemeMenuAction.bind(this));

    if (this.theme === 'dark') {
      darkThemeMenu.setIconId(icons.CHECKED_BOLD);
    } else {
      defaultThemeMenu.setIconId(icons.CHECKED_BOLD);
    }
  }

  _onDefaultThemeMenuAction(event) {
    this.setTheme('default');
  }

  _onDarkThemeMenuAction(event) {
    this.setTheme('dark');
  }

  _onAboutMenuAction(event) {
    let form = scout.create(Form, {
      parent: this,
      resizable: false,
      title: this.session.text('ApplicationTitle'),
      rootGroupBox: {
        objectType: GroupBox,
        borderDecoration: 'empty',
        fields: [{
          objectType: LabelField,
          value: this.session.text('AboutText', this.session.text('ApplicationTitle'), App.get().version),
          labelVisible: false,
          wrapText: true,
          htmlEnabled: true,
          cssClass: 'about-info',
          statusVisible: false,
          gridDataHints: {
            h: 3
          }
        }]
      }
    });
    form.open();
  }
}
