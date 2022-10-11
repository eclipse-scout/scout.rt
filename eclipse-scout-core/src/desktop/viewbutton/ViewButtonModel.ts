/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ActionModel} from '../../index';

export default interface ViewButtonModel extends ActionModel {
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
