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
// Eclipse Scout module: re-exports
// The modules exported here will be available when someone imports from 'eclipse-scout'
import * as scout from './scout';
import * as models from './util/models';

export { scout as default };
export { models };
export { default as App } from './App';
export { default as Session } from './session/Session';
export { default as Desktop } from './desktop/Desktop';
export { default as NullWidget } from './widget/NullWidget';
export { default as ViewButton } from './desktop/ViewButton';
export { default as OutlineViewButton } from './outline/OutlineViewButton';

// FIXME [awe] ES6: its important to think about how we want to organize the eclipse-scout module
// as this decides how a Scout developer will import stuff from Eclipse Scout.
// take a look at WidgetsApp.js
// Idea: instead of exporting a single module, it could be more appropriate to define an "alias"
// in Webpack that accesses the /src/ directory of Eclipse Scout. So instead of doing
//   import { models } from 'eclipse-scout';
// The developer would write:
//   import * as models from 'eclipse-scout/util/models';
// Don't know if that will work, but this way we don't have to deal with building a namespace
// structure for the eclipse-scout module.
