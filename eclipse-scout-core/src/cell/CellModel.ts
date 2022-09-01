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
import {Status} from '../index';
import {HorizontalAlignment} from './Cell';

export default interface CellModel {
  cssClass?: string;
  editable?: boolean;
  errorStatus?: Status;
  horizontalAlignment?: HorizontalAlignment;
  htmlEnabled?: boolean;
  iconId?: string;
  mandatory?: boolean;
  text?: string;
  flowsLeft?: boolean;
  empty?: boolean;
  value?: any;
  tooltipText?: string;
  foregroundColor?: string;
  backgroundColor?: string;
  font?: string;
  sortCode?: number;
}
