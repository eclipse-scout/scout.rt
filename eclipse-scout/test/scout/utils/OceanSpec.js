import Ocean from '../../src/scout/Ocean.js';

describe('Ocean', () => {

  it('should taste salty', () => {
	 var ocean = new Ocean();
	 expect(ocean.salinityLevel).toBe(1.0);
  });

});