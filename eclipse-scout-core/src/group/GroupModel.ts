/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GroupCollapseStyle, ObjectOrChildModel, Widget, WidgetModel} from '../index';

export interface GroupModel<TBody extends Widget = Widget> extends WidgetModel {
  collapsed?: boolean;
  collapsible?: boolean;
  title?: string;
  titleHtmlEnabled?: boolean;
  titleSuffix?: string;
  /**
   * Specifies the widget used for the group header.
   * If the value is null, a default header is created that consists of the {@link iconId}, {@link title} and {@link titleSuffix}.
   * Default is null.
   */
  header?: ObjectOrChildModel<Widget>;
  /**
   * Specifies whether the header should receive the focused when being clicked.
   * If set to false, it can only receive the focus by keyboard (e.g. by pressing TAB). Default is false.
   */
  headerFocusable?: boolean;
  headerVisible?: boolean;
  /**
   * Specifies the widget used for the group body.
   * The body will be rendered when the group is expanded, so having a body is required.
   * Default is null.
   */
  body?: ObjectOrChildModel<TBody>;
  collapseStyle?: GroupCollapseStyle;
  /**
   * Icon that should be displayed beneath the title in the header of the group.
   *
   * The iconId may be an url pointing to an image, or a font icon specifier (e.g. an icon of {@link icons}).
   */
  iconId?: string;
}
