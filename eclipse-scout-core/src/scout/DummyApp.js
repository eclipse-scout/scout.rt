export default class DummyApp {

  constructor() {
    console.log('Constructing dummy app');
  }

  init(options) {
    console.log('Initializing dummy app');
    return true;
  }
}
