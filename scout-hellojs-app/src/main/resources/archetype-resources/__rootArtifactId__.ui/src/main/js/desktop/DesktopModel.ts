import {Menu, DesktopModel} from '@eclipse-scout/core';
import {DataOutline, Desktop} from '../index';

export default (): DesktopModel => ({
  id: '${simpleArtifactName}.Desktop',
  title: '${symbol_dollar}{textKey:ApplicationTitle}',
  objectType: Desktop,
  logoUrl: 'img/eclipse_scout_logo.svg',
  nativeNotificationDefaults: {
    iconId: 'img/eclipse_scout_logo.png'
  },
  outline: {
    objectType: DataOutline
  },
  menus: [
    {
      id: 'ThemeMenu',
      objectType: Menu,
      text: '${symbol_dollar}{textKey:Theme}',
      childActions: [
        {
          id: 'DefaultThemeMenu',
          objectType: Menu,
          text: 'Default'
        },
        {
          id: 'DarkThemeMenu',
          objectType: Menu,
          text: 'Dark'
        }
      ]
    },
    {
      id: 'AboutMenu',
      objectType: Menu,
      text: '${symbol_dollar}{textKey:About}',
      cssClass: 'about-menu'
    }
  ]
});

export type DesktopWidgetMap = {
  '${simpleArtifactName}.Desktop': Desktop;
  'ThemeMenu': Menu;
  'DefaultThemeMenu': Menu;
  'DarkThemeMenu': Menu;
  'AboutMenu': Menu;
};
