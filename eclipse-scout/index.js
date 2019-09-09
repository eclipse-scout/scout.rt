/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import * as self from './src/index.js';

import * as scout from 'src/scout/scout';
import * as models from 'src/scout/util/models';
import * as strings from 'src/scout/util/strings';

export default scout;
export { models };
export { strings };

export { default as App } from 'src/scout/App';
export { default as Session } from 'src/scout/session/Session';
export { default as Desktop } from 'src/scout/desktop/Desktop';
export { default as NullWidget } from 'src/scout/widget/NullWidget';
export { default as ViewButton } from 'src/scout/desktop/ViewButton';
export { default as OutlineViewButton } from 'src/scout/outline/OutlineViewButton';
export { default as Device } from 'src/scout/util/Device';
export { default as Menu } from 'src/scout/menu/Menu';

window.scout = Object.assign({}, self);

// This is used for the ES6 build stack, where this module is exported and
// consumed by other modules. When this piece of code is executed in a browser
// it will fail, because 'exports' is a read-only property.
// try {
//   // module.exports = scout;
// } catch (e) {
//   // NOP - silently fail
// }

// This is used for our ES5 eclipse-scout.js export, which is used to create
// simple Scout apps that depend only on the built JS file and don't require
// the whole build stack with modules, Webpack, etc.
// if (window) {
//
// }
// window.scout = Object.assign({}, self);

// FIXME [awe] ES6: review, improve this index.js. Maybe we should have two separate files?
// one is the main, as referenced by package.json and the other is the build-entry as used
// by Webpack? How can we do the same with import/export instead of require and module.exports
