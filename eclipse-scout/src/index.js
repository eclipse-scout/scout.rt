/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
// Eclipse Scout module: re-exports
// The modules exported here will be available when someone imports from 'eclipse-scout'
import * as scout from './scout/scout';
import * as models from './scout/util/models';
import * as strings from './scout/util/strings';
import * as self from './index.js';

export default scout;
export { models };
export { strings };

export { default as App } from './scout/App';
export { default as Session } from './scout/session/Session';
export { default as Desktop } from './scout/desktop/Desktop';
export { default as NullWidget } from './scout/widget/NullWidget';
export { default as ViewButton } from './scout/desktop/ViewButton';
export { default as OutlineViewButton } from './scout/outline/OutlineViewButton';
export { default as Device } from './scout/util/Device';
export { default as Menu } from './scout/menu/Menu';

window.scout = Object.assign(window.scout || {}, self);
