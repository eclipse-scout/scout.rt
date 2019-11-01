export { default as SvgField } from './svg/SvgField';
export { default as SvgFieldAdapter } from './svg/SvgFieldAdapter';

import * as self from './index.js';
export default self;
window.scout = Object.assign(window.scout || {}, self);
