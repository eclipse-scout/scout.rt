import {ObjectFactory} from '@eclipse-scout/core';

// import your custom JS files here


// Define namespace and put it onto window (necessary for model variants, e.g. @ModelVariant(${classPrefixLowerCase}.Example)
import * as self from './index.js';
export default self;
ObjectFactory.get().registerNamespace('${simpleArtifactName}', self);
