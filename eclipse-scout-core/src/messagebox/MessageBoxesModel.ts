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
import {MessageBoxes, Widget} from '../index';
import {StatusSeverity} from '../status/Status';
import {ObjectModel} from '../scout';

export default interface MessageBoxesModel extends ObjectModel<MessageBoxes> {
  parent?: Widget;
  yesText?: string;
  noText?: string;
  cancelText?: string;
  bodyText?: string;
  severity?: StatusSeverity;
  headerText?: string;
  iconId?: string;
  closeOnClick?: boolean;
  html?: boolean;
}
