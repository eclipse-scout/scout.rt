/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormMenu, FormMenuModel, icons, Table} from '../../index';

export default (): FormMenuModel => ({
  objectType: FormMenu,
  iconId: icons.GEAR,
  tooltipText: '${textKey:TableOrganize}',
  menuTypes: [Table.MenuType.Header],
  inheritAccessibility: false,
  popupResizable: true,
  popupMovable: true
});
