import {ObjectFactory} from '@eclipse-scout/core';

export * from './App';
export * from './objectFactories';
export * from './desktop/Desktop';
export * from './desktop/DataOutline';
export * from './rest/AbstractItemResponse';
export * from './rest/AbstractRestClient';
export * from './person/PersonDo';
export * from './person/PersonForm';
export * from './person/PersonResponse';
export * from './person/PersonRestClient';
export * from './person/PersonRestrictionDo';
export * from './person/PersonSearchForm';
export * from './person/PersonTablePage';

import * as self from './index';
export default self;
ObjectFactory.get().registerNamespace('${simpleArtifactName}', self);
