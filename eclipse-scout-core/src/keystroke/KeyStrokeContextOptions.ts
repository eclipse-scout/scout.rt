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
import {EventHandler, KeyStroke} from '../index';

export default interface KeyStrokeContextOptions {
  /**
   * Indicates whether to invoke 'acceptInput' on a currently focused value field prior handling the keystroke.
   */
  invokeAcceptInputOnActiveValueField: boolean;
  /**
   * Holds the keystrokes registered within this context.
   */
  keyStrokes: KeyStroke[];
  /**
   * Array of interceptors to participate in setting 'stop propagation' flags.
   */
  stopPropagationInterceptors: EventHandler[];
  /**
   * Holds the target where to bind this context as keydown listener.
   * This can either be a static value or a function to resolve the target.
   */
  $bindTarget: JQuery | (() => JQuery);
  /**
   * Holds the scope of this context and is used to determine the context's accessibility, meaning not covert by a glasspane.
   * This can either be a static value or a function to resolve the target.
   */
  $scopeTarget: JQuery | (() => JQuery);
}
