/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {WidgetSupportOptions} from '../index';

export interface LoadingSupportOptions extends WidgetSupportOptions {
  /**
   * If not set: 250 ms
   */
  loadingIndicatorDelay?: number;
  /**
   * Specifies whether the loading support should draw a glass pane to prevent clicks and other interaction.
   * The loading indicator will be put onto that glass pane.
   *
   * Default is false.
   */
  withGlassPane?: boolean;
  /**
   * Whether the loading support is abortable.
   *
   * If the loading support is abortable a cancel button will be rendered together with the loading indicator.
   *
   * Default is `false`.
   */
  abortable?: boolean;
  /**
   * Handler that is called when loading is aborted (see {@link abortable}).
   */
  abortHandler?: () => void;
}
