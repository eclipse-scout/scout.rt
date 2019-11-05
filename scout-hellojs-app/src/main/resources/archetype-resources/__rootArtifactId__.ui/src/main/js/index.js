export {default as App} from './App';
export * from './objectFactories';
export {default as Repository} from './repository/Repository';
export {default as Desktop} from './desktop/Desktop';
export {default as DataOutline} from './desktop/DataOutline';
export {default as Person} from './person/Person';
export {default as PersonForm} from './person/PersonForm';
export {default as PersonRepository} from './person/PersonRepository';
export {default as PersonRestriction} from './person/PersonRestriction';
export {default as PersonSearchForm} from './person/PersonSearchForm';
export {default as PersonTablePage} from './person/PersonTablePage';

import * as self from './index.js';

export default self;
window.${simpleArtifactName} = Object.assign(window.${simpleArtifactName} || {}, self);
