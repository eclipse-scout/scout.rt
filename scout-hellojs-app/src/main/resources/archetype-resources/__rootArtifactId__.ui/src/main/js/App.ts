import {App as ScoutApp} from '@eclipse-scout/core';

export class App extends ScoutApp {
  // @ts-expect-error
  static get(): App {
    return ScoutApp.get() as App;
  }
}
