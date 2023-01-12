/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ActionModel} from '../index';

export interface ModeModel<TRef = any> extends ActionModel {
  /**
   *  Arbitrary reference value, can be used to find and select modes (see {@link ModeSelector}).
   *  Default is null.
   **/
  ref?: TRef;
}
