import {RemoteApp} from '@eclipse-scout/core';
import * as ${simpleArtifactName} from './index';

Object.assign({}, ${simpleArtifactName}); // Use import so that it is not marked as unused

new RemoteApp().init();
