/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, BenchColumnLayoutData, DesktopDisplayStyle, DesktopNotification, DisplayParentModel, Menu, NativeNotificationDefaults, ObjectOrChildModel, Outline, ViewButton, Widget, WidgetModel} from '../index';

export interface DesktopModel extends WidgetModel, DisplayParentModel {
  /**
   * Configures the style which defines the basic layout of the application.
   *
   * Default is {@link DesktopDisplayStyle.DEFAULT}.
   */
  displayStyle?: DesktopDisplayStyle;
  /**
   * Configures the title of the desktop shown in the browser window.
   */
  title?: string;
  /**
   * Configures the url pointing to the image that should be displayed on the top right of the application in the {@link DesktopHeader}.
   *
   * @see DesktopLogo
   */
  logoId?: string;
  /**
   * Configures whether the {@link logo} should be clickable and trigger a {@link DesktopEventMap.logoAction} when clicked.
   *
   * Default is false.
   */
  logoActionEnabled?: boolean;
  /**
   * Defines whether the {@link DesktopNavigation} should be visible.
   *
   * Default is true.
   */
  navigationVisible?: boolean;
  /**
   * Defines whether the {@link DesktopNavigationHandle}, which enables the user to hide and show the navigation, should be visible.
   *
   * Default is true.
   */
  navigationHandleVisible?: boolean;
  /**
   * Defines whether the {@link DesktopBench} should be visible.
   *
   * Default is true.
   */
  benchVisible?: boolean;
  /**
   * Configures the layout data used by the {@link FlexboxLayout} to arrange the view areas in the {@link DesktopBench}.
   *
   * @see BenchColumn
   */
  benchLayoutData?: BenchColumnLayoutData;
  /**
   * Defines whether the {@link DesktopHeader} should be visible.
   *
   * Default is true.
   */
  headerVisible?: boolean;
  /**
   * Configures the menus to be displayed in the {@link DesktopToolBox} in the {@link DesktopHeader}.
   */
  menus?: ObjectOrChildModel<Menu>[];
  /**
   * Defines the (toast) notifications that should be shown on the top right of the desktop.
   */
  notifications?: ObjectOrChildModel<DesktopNotification>[];
  /**
   * Configures the default settings for native notifications. A specific notification can override these settings if
   * desired.
   *
   * The default object is initialized with the desktop's title and logo id. If these values change the defaults won't
   * be adjusted automatically. If you want them to be aligned you need to update the native notification defaults
   * whenever the title or logo changes.
   *
   * *Note*: SVG icons may not work with every browser, you may have to use a bitmap icon.
   */
  nativeNotificationDefaults?: NativeNotificationDefaults;
  /**
   * AddOns are custom widgets that are rendered with the desktop and may or may not have a visual representation.
   */
  addOns?: ObjectOrChildModel<Widget>[];
  /**
   * Configures the keystrokes that should be registered in the current {@link keyStrokeContext}.
   *
   * Use the {@link ActionModel.keyStroke} to assign the keys that need to be pressed.
   *
   * @see KeyStrokeContext
   */
  keyStrokes?: ObjectOrChildModel<Action>[];
  /**
   * View buttons are displayed on the top of the {@link DesktopNavigation} and are mostly used to so switch between {@link Outline}s by using {@link OutlineViewButton}.
   */
  viewButtons?: ObjectOrChildModel<ViewButton>[];
  /**
   * The currently active {@link Outline}.
   */
  outline?: ObjectOrChildModel<Outline> | string;
  /**
   * The name of the CSS theme to be used for the application.
   * @see Desktop.setTheme
   */
  theme?: string;
  /**
   * Defines whether the application should be in dense mode.
   *
   * Dense means that certain UI elements are displayed smaller,
   * like smaller table rows or smaller logical grid rows, so the user can see more information at once on the screen.
   *
   * Default is false.
   */
  dense?: boolean;
  /**
   * Configures whether the keystrokes to select {@link DesktopTab}s should be enabled.
   *
   * Default is true.
   *
   * @see DesktopTabSelectKeyStroke
   */
  selectViewTabsKeyStrokesEnabled?: boolean;
  /**
   * Configures the modifier used by {@link DesktopTabSelectKeyStroke}.
   *
   * Only has an effect is {@link selectViewTabsKeyStrokesEnabled} is set to true.
   */
  selectViewTabsKeyStrokeModifier?: string;
  /**
   * Configures whether the position of the splitter between the navigation and the bench should be stored in the session
   * storage, so that the position may be restored after a page reload. If set to false, the default position is used.
   *
   * Default is true.
   */
  cacheSplitterPosition?: boolean;
}
