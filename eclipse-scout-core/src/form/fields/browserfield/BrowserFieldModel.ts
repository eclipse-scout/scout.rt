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
import {FormFieldModel} from '../../../index';

export default interface BrowserFieldModel extends FormFieldModel {
  /**
   * Configures whether the external window should be auto closed when this field gets removed.
   *
   * NOTE: Auto closing only makes sense, if content is shown in an external window see (@link showInExternalWindow).
   */
  autoCloseExternalWindow?: boolean;
  /**
   * Text of the button that is used to reopen the external window, if {@link showInExternalWindow} is true.
   */
  externalWindowButtonText?: string;
  /**
   * Text that is shown in the browser field if the content is opened in an external window, see {@link showInExternalWindow}.
   */
  externalWindowFieldText?: string;
  /**
   * The location (URI) of the content to be displayed.
   */
  location?: string;
  /** @see IFrameModel.sandboxEnabled */
  sandboxEnabled?: boolean;
  scrollBarEnabled?: boolean;
  /** @see IFrameModel.sandboxPermissions */
  sandboxPermissions?: string;
  /**
   * Configures whether a separate browser window should be opened to display the content of the browser field.
   *
   * Default is false, to the content of the browser field will be shown inline in an <iframe>.
   */
  showInExternalWindow?: boolean;
  /** @see IFrameModel.trackLocation */
  trackLocation?: boolean;
  /**
   * A list of origin URIs from which this field will receive messages posted via <i>postMessage</i>.
   * If this is `null` or empty, messages from all origins are accepted.
   *
   * Default is [].
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">window.postMessage (MDN)</a>
   */
  trustedMessageOrigins?: string[];
}
