/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BusyIndicator, BusyIndicatorModel, BusyIndicatorOptions, BusySupport, ModelOf, ObjectModel, Widget} from '../index';

export interface BusySupportModel extends ObjectModel<BusySupport> {
  /**
   * The default parent {@link Widget} to use for the {@link BusyIndicator}.
   * This property is mandatory.
   * The default parent specified by this property may be overwritten using {@link BusyIndicatorOptions.busyIndicatorModel} in {@link BusySupport.setBusy}.
   */
  parent?: Widget;
  /**
   * The default model to use for the {@link BusyIndicator}.
   * It may be overwritten using {@link BusyIndicatorOptions.busyIndicatorModel} in {@link BusySupport.setBusy}.
   * By default it sets {@link busyIndicatorModel.cancellable} to false.
   */
  busyIndicatorModel?: ModelOf<BusyIndicator>;
  /**
   * The default timeout in milliseconds between the call to {@link BusyIndicator.setBusy} and the rendering of the {@link BusyIndicator}.
   * This is not the timeout until the busy indicator popup is shown (for this use {@link BusyIndicatorModel.showTimeout}) but the timeout until the glasspanes are rendered.
   * The default is 50ms. It is not recommended to set this value to zero because this may lead to flickering of the mouse cursor.
   */
  renderDelay?: number;
}
