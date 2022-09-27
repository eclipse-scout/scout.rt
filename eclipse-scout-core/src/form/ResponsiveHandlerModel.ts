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
import {Widget} from '../index';
import {ResponsiveState} from './ResponsiveManager';

export default interface ResponsiveHandlerModel {
  widget?: Widget;
  /**
   * Default is -1.
   */
  compactThreshold?: number;
  /**
   * Default is -1.
   */
  condensedThreshold?: number;
  /**
   * Default is ResponsiveManager.ResponsiveState.NORMAL.
   */
  oldState?: ResponsiveState;
  /**
   * Default is ResponsiveManager.ResponsiveState.NORMAL.
   */
  state?: ResponsiveState;
  /**
   * Default is [ResponsiveManager.ResponsiveState.NORMAL, ResponsiveManager.ResponsiveState.COMPACT].
   */
  allowedStates?: ResponsiveState[];
  /**
   * Default is {}.
   */
  transformations?: Record<string, (Widget, boolean) => void>;
  /**
   * Default is {}.
   */
  enabledTransformations?: Record<ResponsiveState, string[]>;
}
