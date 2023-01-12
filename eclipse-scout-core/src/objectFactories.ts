/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Action, ActionAdapter, scout, TreeField, TreeFieldAdapter} from './index';

scout.addObjectFactories({
  'KeyStroke': () => {
    // A model keystroke is represented as an Action
    return new Action();
  },
  'KeyStrokeAdapter': () => new ActionAdapter(),
  'ComposerField': () => {
    // Composer is just a tree field, there is currently no need to duplicate the JS/CSS code
    return new TreeField();
  },
  'ComposerFieldAdapter': () => new TreeFieldAdapter()
});
