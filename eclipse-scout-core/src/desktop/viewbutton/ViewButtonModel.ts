/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ActionModel} from '../../index';

export interface ViewButtonModel extends ActionModel {
  /**
   * Indicates if this view button is currently the "selected button" in the ViewMenuTab widget,
   * i.e. if it was the last view button of type MENU to have been selected. Note that the
   * "selected" property does not necessarily have to be true as well, since an other button of
   * type TAB might currently be selected. This information is used when restoring the "selected
   * button" when the ViewMenuTab widget is removed and restored again, e.g. when toggling the
   * desktop's 'navigationVisible' property.
   */
  selectedAsMenu?: boolean;
  displayStyle?: ViewButtonDisplayStyle;
}

export type ViewButtonDisplayStyle = 'MENU' | 'TAB';
