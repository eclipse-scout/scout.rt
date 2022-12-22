import {ObjectFactory} from '@eclipse-scout/core';

// export your custom JS files here. Example:
// export * from './yourFolder/YourClass';

// Define namespace and put it onto window (necessary for model variants, e.g. @ModelVariant(${classPrefixLowerCase}.Example)
import * as self from './index';

export default self;
ObjectFactory.get().registerNamespace('${simpleArtifactName}', self);
