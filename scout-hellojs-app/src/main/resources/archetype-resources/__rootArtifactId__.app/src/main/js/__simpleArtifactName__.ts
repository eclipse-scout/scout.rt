import {App} from '@${simpleArtifactName}/ui';
import {access, System, systems} from '@eclipse-scout/core';

systems.getOrCreate(System.MAIN_SYSTEM, '../api/');
App.addBootstrapper(() => access.bootstrapSystem());

new App().init({
  bootstrap: {
    textsUrl: 'texts.json'
  }
});
