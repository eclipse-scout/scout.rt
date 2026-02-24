/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, ObjectOrChildModel, StatusSeverity, WidgetModel} from '../../index';

export interface StatusMenuMappingModel extends WidgetModel {
  /**
   * Configures the codes the status should have to display the menu.
   *
   * If no codes are defined, the menu is shown for every code.
   */
  codes?: number[];
  /**
   * Configures the severities the status should have to display the menu.
   *
   * If no severities are defined, the menu is shown for every severity.
   */
  severities?: StatusSeverity[];
  /**
   * Configures the menu that should be shown.
   *
   * If an id is provided, the menu is resolved automatically using the {@link WidgetModel.parent}.
   */
  menu?: ObjectOrChildModel<Menu> | string;
}
