export default class Water {

  constructor() {
    this.salinityLevel = 0;
  }

  flow() {
    return 'Flowing...';
  }

  freeze() {
    return 'Freezing...';
  }

  vaporize() {
    return 'Vaporizing...';
  }

  tasteSalinityLevel() {
    return 'Tastes like salinity-level ' + this.salinityLevel;
  }

}
