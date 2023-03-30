import {App, Desktop as ScoutDesktop, DesktopModel as ScoutDesktopModel, Event, Form, GroupBox, LabelField, icons, InitModelOf, scout} from '@eclipse-scout/core';
import DesktopModel, {DesktopWidgetMap} from './DesktopModel';

export class Desktop extends ScoutDesktop {
  declare widgetMap: DesktopWidgetMap;

  protected override _jsonModel(): ScoutDesktopModel {
    return DesktopModel();
  }

  protected override _init(model: InitModelOf<this>) {
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

  protected _onDefaultThemeMenuAction() {
    this.setTheme('default');
  }

  protected _onDarkThemeMenuAction() {
    this.setTheme('dark');
  }

  protected _onAboutMenuAction() {
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

export interface DataChangeEvent<T = Desktop> extends Event<T> {
  dataType: string;
  data: any;
}
