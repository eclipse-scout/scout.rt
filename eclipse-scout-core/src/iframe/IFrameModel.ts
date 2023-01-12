/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {WidgetModel} from '../index';

export interface IFrameModel extends WidgetModel {
  location?: string;
  /**
   * Configures whether the sandbox mode is enabled. Default is true.
   */
  sandboxEnabled?: boolean;
  /**
   * Sandbox permissions separated by space.
   * This property is only relevant when sandbox is enabled ({@link sandboxEnabled}).
   * For a list of available permissions see the <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/iframe#attr-sandbox">MDN</a>.
   */
  sandboxPermissions?: string;
  scrollBarEnabled?: boolean;
  /**
   * If true, the location property is updated whenever the location of the iframe changes. Default is false.
   *
   * Note: This does only work if the iframe and the iframe's parent document have the same origin (protocol, port and host are the same).
   */
  trackLocation?: boolean;
}
