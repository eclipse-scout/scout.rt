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
import {FormFieldModel, Widget} from '../../../index';
import {ButtonDisplayStyle, ButtonSystemType} from './Button';

export default interface ButtonModel extends FormFieldModel {
  defaultButton?: boolean;
  displayStyle?: ButtonDisplayStyle;
  iconId?: string;
  keyStroke?: string;
  keyStrokeScope?: Widget | string;
  processButton?: boolean;
  selected?: boolean;
  systemType?: ButtonSystemType;
  preventDoubleClick?: boolean;
  stackable?: boolean;
  shrinkable?: boolean;
}
