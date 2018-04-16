/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('scout.styles', function() {
  var $sandbox;

  beforeEach(function() {
    setFixtures(sandbox());
    $sandbox = $('#sandbox');
  });

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

  it('can calculate and apply legacy styles', function() {
    expect(scout.styles.legacyStyle()).toBe('');

    var $el = $sandbox.appendDiv();
    var obj = {};
    expect(scout.styles.legacyStyle(null, $el)).toBe('');
    expect($el.attr('style')).toBe(undefined);
    expect(scout.styles.legacyStyle(obj, $el)).toBe('');
    expect($el.attr('style')).toBe(undefined);

    obj = {
      backgroundColor: 'red',
      foregroundColor: null
    };
    expect(scout.styles.legacyStyle(obj, $el)).toBe('background-color: red; ');
    expect($el.attr('style')).toBe('background-color: red;');
    obj = {
      backgroundColor: null,
      foregroundColor: 'yellow'
    };
    expect(scout.styles.legacyStyle(obj, $el)).toBe('color: yellow; ');
    expect($el.attr('style')).toBe('color: yellow;');
    obj = {
      font: 'Times New Roman-14-bold'
    };
    expect(scout.styles.legacyStyle(obj, $el)).toBe('font-weight: bold; font-size: 14pt; font-family: Times New Roman; ');
    expect($el.attr('style')).toMatch(/font-weight: bold; font-size: 14pt; font-family: ['"]?Times New Roman['"]?;/);

    // Test prefix
    obj = {
      foregroundColor: 'red',
      labelForegroundColor: 'green',
      xyzForegroundColor: null
    };
    expect(scout.styles.legacyStyle(obj, $el)).toBe('color: red; ');
    expect($el.attr('style')).toBe('color: red;');
    expect(scout.styles.legacyStyle(obj, $el, 'label')).toBe('color: green; ');
    expect($el.attr('style')).toBe('color: green;');
    expect(scout.styles.legacyStyle(obj, $el, 'doesnotexist')).toBe('');
    expect($el.attr('style')).toBe('');
    expect(scout.styles.legacyStyle(obj, $el, 'xyz')).toBe('');
    expect($el.attr('style')).toBe('');
    obj = {
      foregroundColor: 'red',
      labelForegroundColor: 'blue',
      backgroundColor: 'yellow',
      labelBackgroundColor: 'green'
    };
    expect(scout.styles.legacyStyle(obj, $el)).toBe('color: red; background-color: yellow; ');
    expect($el.attr('style')).toBe('color: red; background-color: yellow;');
    expect(scout.styles.legacyStyle(obj, $el, 'label')).toBe('color: blue; background-color: green; ');
    expect($el.attr('style')).toBe('color: blue; background-color: green;');
    expect(scout.styles.legacyStyle(obj, $el, 'doesnotexist')).toBe('');
    expect($el.attr('style')).toBe('');
    expect(scout.styles.legacyStyle(obj, $el, 'xyz')).toBe('');
    expect($el.attr('style')).toBe('');

    // Test properties
    obj = {
      foregroundColor: 'red',
      backgroundColor: 'yellow',
      labelBackgroundColor: 'purple',
      xyzBackgroundColor: null,
    };
    expect(scout.styles.legacyStyle(obj, $el)).toBe('color: red; background-color: yellow; ');
    expect($el.attr('style')).toBe('color: red; background-color: yellow;');
    obj.backgroundColor = 'blue';
    expect(scout.styles.legacyBackgroundColor(obj, $el)).toBe('background-color: blue; ');
    expect($el.attr('style')).toBe('color: red; background-color: blue;');
    expect(scout.styles.legacyBackgroundColor(obj, $el, 'label')).toBe('background-color: purple; ');
    expect($el.attr('style')).toBe('color: red; background-color: purple;');
    expect(scout.styles.legacyBackgroundColor(obj, $el, 'doesnotexist')).toBe('');
    expect($el.attr('style')).toBe('color: red;');
    expect(scout.styles.legacyBackgroundColor(obj, $el, 'xyz')).toBe('');
    expect($el.attr('style')).toBe('color: red;');
    expect(scout.styles.legacyForegroundColor(obj, $el, 'label')).toBe('');
    expect(scout.styles.legacyBackgroundColor(obj, $el, 'label')).toBe('background-color: purple; ');
    expect($el.attr('style')).toBe('background-color: purple;');
  });

});
