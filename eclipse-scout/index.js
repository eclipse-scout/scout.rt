/*******************************************************************************
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
let scout = require('./src/scout/index');

// This is used for our ES5 eclipse-scout.js export, which is used to create
// simply Scout apps that depend only on the built JS file and don't require
// the whole build stack with modules, Webpack, etc.
if (window && !window.scout) {
  window.scout = Object.assign({}, scout.default, scout);
}

try {
  module.exports = scout;
} catch (e) {
  // NOP - silently fail in browser because .exports is read-only
}

// FIXME [awe] ES6: review, improve this index.js. Maybe we should have two separate files?
// one is the main, as referenced by package.json and the other is the build-entry as used
// by Webpack? How can we do the same with import/export instead of require and module.exports
