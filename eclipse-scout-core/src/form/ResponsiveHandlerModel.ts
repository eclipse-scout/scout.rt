/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ObjectModel, ResponsiveHandler, ResponsiveState, Widget} from '../index';

export interface ResponsiveHandlerModel extends ObjectModel<ResponsiveHandler> {
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
