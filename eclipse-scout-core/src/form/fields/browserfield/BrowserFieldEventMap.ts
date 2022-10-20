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
import {BrowserField, Event, FormFieldEventMap, PropertyChangeEvent} from '../../../index';
import {BrowserFieldWindowStates} from './BrowserField';

export interface BrowserFieldExternalWindowStateChangeEvent<T = BrowserField> extends Event<T> {
  windowState: BrowserFieldWindowStates;
}

/**
 * This event is triggered when the field has received a message from the embedded page (`iframe`) or external
 * window.
 *
 * Possible reasons why this method is not called:
 * <ul>
 * <li>The embedded page use the wrong target `window`.
 * <li>The browser blocked the message for some unknown reason (check the F12 developer console).
 * <li>The sandbox is enabled and does not allow sending messages.
 * <li>The embedded page specified the wrong `targetOrigin` when calling <i>postMessage</i>.
 * <li>The sender origin does not match the list {@link BrowserField.trustedMessageOrigins}.
 * <li>The browser field is disabled.
 * </ul>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">window.postMessage (MDN)</a>
 */
export interface BrowserFieldMessageEvent<T = BrowserField> extends Event<T> {
  /**
   * Message received from the `iframe`.
   */
  data: any;
  /**
   * The origin of the window that sent the message.
   */
  origin: string;
}

export default interface BrowserFieldEventMap extends FormFieldEventMap {
  'externalWindowStateChange': BrowserFieldExternalWindowStateChangeEvent;
  'message': BrowserFieldMessageEvent;
  'propertyChange:autoCloseExternalWindow': PropertyChangeEvent<boolean>;
  'propertyChange:externalWindowButtonText': PropertyChangeEvent<string>;
  'propertyChange:externalWindowFieldText': PropertyChangeEvent<string>;
  'propertyChange:location': PropertyChangeEvent<any>;
  'propertyChange:sandboxEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:sandboxPermissions': PropertyChangeEvent<any>;
  'propertyChange:scrollBarEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:trackLocation': PropertyChangeEvent<boolean>;
}
