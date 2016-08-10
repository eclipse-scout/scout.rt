/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.objectFactories = {
  'KeyStroke': function() {
    // A model keystroke is represented as an Action
    return new scout.Action();
  },
  'ComposerField': function() {
    // Composer is just a tree field, there is currently no need to duplicate the JS/CSS code
    return new scout.TreeField();
  },
  'ComposerFieldAdapter': function() {
    return new scout.TreeFieldAdapter();
  }
};
