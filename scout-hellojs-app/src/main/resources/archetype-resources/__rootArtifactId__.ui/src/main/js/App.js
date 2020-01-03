import {App as ScoutApp} from '@eclipse-scout/core';

export default class App extends ScoutApp {

  constructor() {
    super();
    this.apiUrl = '../api/';
    this.appPrefix = '${simpleArtifactName}.';
  }

}
