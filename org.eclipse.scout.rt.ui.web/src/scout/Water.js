export default class Water {

  constructor() {
	this.salinityLevel = 0;
  }

  flow() {
    console.log('flowing...');
  }

  freeze() {
	console.log('freezing...');
  }

  vaporize() {
	console.log('vaporizing...');
  }

  tasteSalinityLevel() {
	console.log('tastes like salinity-level ', this.salinityLevel);
  }

}