import {App} from '@${simpleArtifactName}/ui';

new App().init({
  bootstrap: {
    textsUrl: 'texts.json',
    permissionsUrl: 'api/permissions'
  }
});

