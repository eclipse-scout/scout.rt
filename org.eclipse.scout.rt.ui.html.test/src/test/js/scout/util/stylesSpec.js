/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('scout.styles', function() {

  it('can merge colors', function() {
    expect(scout.styles.mergeRgbColors()).toBe(undefined);
    expect(scout.styles.mergeRgbColors('rgb(1,2,3)')).toBe('rgb(0,0,0)'); // no ratio
    expect(scout.styles.mergeRgbColors('#fff', 1)).toBe(undefined); // invalid format
    expect(scout.styles.mergeRgbColors('rgb(1,2,3)', 1)).toBe('rgb(1,2,3)');
    expect(scout.styles.mergeRgbColors('rgb(10,10,10)', 0.5, 'rgb(20,20,20)', 0.5)).toBe('rgb(15,15,15)');
    expect(scout.styles.mergeRgbColors('rgba(10,10,10,0.3)', 0.5, 'rgba(20,20,20,0.7)', 0.5)).toBe('rgb(15,15,15)'); // alpha is ignored
  });

  it('can lighten and darken colors', function() {
    expect(scout.styles.darkerColor()).toBe(undefined);
    expect(scout.styles.darkerColor('#fff')).toBe(undefined);
    expect(scout.styles.darkerColor('rgb(10,10,10)')).toBe('rgb(8,8,8)');

    expect(scout.styles.lighterColor()).toBe(undefined);
    expect(scout.styles.lighterColor('#000')).toBe(undefined);
    expect(scout.styles.lighterColor('rgb(10,10,10)')).toBe('rgb(59,59,59)');
  });

});
