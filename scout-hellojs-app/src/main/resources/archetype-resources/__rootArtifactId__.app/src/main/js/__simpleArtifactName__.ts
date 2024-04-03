import {App} from '@${simpleArtifactName}/ui';
import {access} from '@eclipse-scout/core';

App.addBootstrapper(() => access.bootstrapSystem());

new App().init({
  bootstrap: {
    textsUrl: 'texts.json'
  }
});

