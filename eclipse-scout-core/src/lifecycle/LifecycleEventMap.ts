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
import {Event, EventMap, Lifecycle} from '../index';

export default interface LifecycleEventMap<VALIDATION_RESULT> extends EventMap {
  'load': Event<Lifecycle<VALIDATION_RESULT>>;
  'postLoad': Event<Lifecycle<VALIDATION_RESULT>>;
  'save': Event<Lifecycle<VALIDATION_RESULT>>;
  'close': Event<Lifecycle<VALIDATION_RESULT>>;
  'reset': Event<Lifecycle<VALIDATION_RESULT>>;
}
