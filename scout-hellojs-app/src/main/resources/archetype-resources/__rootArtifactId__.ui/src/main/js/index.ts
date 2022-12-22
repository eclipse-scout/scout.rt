import {ObjectFactory} from '@eclipse-scout/core';

export * from './App';
export * from './objectFactories';
export * from './repository/Repository';
export * from './desktop/Desktop';
export * from './desktop/DesktopModel';
export * from './desktop/DataOutline';
export * from './desktop/DataOutlineModel';
export * from './person/Person';
export * from './person/PersonForm';
export * from './person/PersonFormModel';
export * from './person/PersonRepository';
export * from './person/PersonRestriction';
export * from './person/PersonSearchForm';
export * from './person/PersonSearchFormModel';
export * from './person/PersonTablePage';
export * from './person/PersonTablePageModel';

import * as self from './index';
export default self;
ObjectFactory.get().registerNamespace('${simpleArtifactName}', self);
