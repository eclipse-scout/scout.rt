/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {styles} from '../../src/index';

describe('scout.styles', () => {
  let $sandbox;

  beforeEach(() => {
    setFixtures(sandbox());
    $sandbox = $('#sandbox');
  });

  it('can merge colors', () => {
    expect(styles.mergeRgbColors()).toBe(undefined);
    expect(styles.mergeRgbColors('rgb(1,2,3)')).toBe('rgb(0,0,0)'); // no ratio
    expect(styles.mergeRgbColors('#fff', 1)).toBe(undefined); // invalid format
    expect(styles.mergeRgbColors('rgb(1,2,3)', 1)).toBe('rgb(1,2,3)');
    expect(styles.mergeRgbColors('rgb(10,10,10)', 0.5, 'rgb(20,20,20)', 0.5)).toBe('rgb(15,15,15)');
    expect(styles.mergeRgbColors('rgba(10,10,10,0.3)', 0.5, 'rgba(20,20,20,0.7)', 0.5)).toBe('rgb(15,15,15)'); // alpha is ignored
  });

  it('can lighten and darken colors', () => {
    expect(styles.darkerColor()).toBe(undefined);
    expect(styles.darkerColor('#fff')).toBe(undefined);
    expect(styles.darkerColor('rgb(10,10,10)')).toBe('rgb(8,8,8)');

    expect(styles.lighterColor()).toBe(undefined);
    expect(styles.lighterColor('#000')).toBe(undefined);
    expect(styles.lighterColor('rgb(10,10,10)')).toBe('rgb(59,59,59)');
  });

  it('can calculate and apply legacy styles', () => {
    expect(styles.legacyStyle()).toBe('');

    let $el = $sandbox.appendDiv();
    let obj = {};
    expect(styles.legacyStyle(null, $el)).toBe('');
    expect($el.attr('style')).toBe(undefined);
    expect(styles.legacyStyle(obj, $el)).toBe('');
    expect($el.attr('style')).toBe(undefined);

    obj = {
      backgroundColor: 'red',
      foregroundColor: null
    };
    expect(styles.legacyStyle(obj, $el)).toBe('background-color: red; ');
    expect($el.attr('style')).toBe('background-color: red;');
    obj = {
      backgroundColor: null,
      foregroundColor: 'yellow'
    };
    expect(styles.legacyStyle(obj, $el)).toBe('color: yellow; ');
    expect($el.attr('style')).toBe('color: yellow;');
    obj = {
      font: 'Times New Roman-14-bold'
    };
    expect(styles.legacyStyle(obj, $el)).toBe('font-weight: bold; font-size: 14pt; font-family: Times New Roman; ');
    expect($el.attr('style')).toMatch(/font-weight: bold; font-size: 14pt; font-family: ['"]?Times New Roman['"]?;/);

    // Test prefix
    obj = {
      foregroundColor: 'red',
      labelForegroundColor: 'green',
      xyzForegroundColor: null
    };
    expect(styles.legacyStyle(obj, $el)).toBe('color: red; ');
    expect($el.attr('style')).toBe('color: red;');
    expect(styles.legacyStyle(obj, $el, 'label')).toBe('color: green; ');
    expect($el.attr('style')).toBe('color: green;');
    expect(styles.legacyStyle(obj, $el, 'doesnotexist')).toBe('');
    expect($el.attr('style')).toBe('');
    expect(styles.legacyStyle(obj, $el, 'xyz')).toBe('');
    expect($el.attr('style')).toBe('');
    obj = {
      foregroundColor: 'red',
      labelForegroundColor: 'blue',
      backgroundColor: 'yellow',
      labelBackgroundColor: 'green'
    };
    expect(styles.legacyStyle(obj, $el)).toBe('color: red; background-color: yellow; ');
    expect($el.attr('style')).toBe('color: red; background-color: yellow;');
    expect(styles.legacyStyle(obj, $el, 'label')).toBe('color: blue; background-color: green; ');
    expect($el.attr('style')).toBe('color: blue; background-color: green;');
    expect(styles.legacyStyle(obj, $el, 'doesnotexist')).toBe('');
    expect($el.attr('style')).toBe('');
    expect(styles.legacyStyle(obj, $el, 'xyz')).toBe('');
    expect($el.attr('style')).toBe('');

    // Test properties
    obj = {
      foregroundColor: 'red',
      backgroundColor: 'yellow',
      labelBackgroundColor: 'purple',
      xyzBackgroundColor: null
    };
    expect(styles.legacyStyle(obj, $el)).toBe('color: red; background-color: yellow; ');
    expect($el.attr('style')).toBe('color: red; background-color: yellow;');
    obj.backgroundColor = 'blue';
    expect(styles.legacyBackgroundColor(obj, $el)).toBe('background-color: blue; ');
    expect($el.attr('style')).toBe('color: red; background-color: blue;');
    expect(styles.legacyBackgroundColor(obj, $el, 'label')).toBe('background-color: purple; ');
    expect($el.attr('style')).toBe('color: red; background-color: purple;');
    expect(styles.legacyBackgroundColor(obj, $el, 'doesnotexist')).toBe('');
    expect($el.attr('style')).toBe('color: red;');
    expect(styles.legacyBackgroundColor(obj, $el, 'xyz')).toBe('');
    expect($el.attr('style')).toBe('color: red;');
    expect(styles.legacyForegroundColor(obj, $el, 'label')).toBe('');
    expect(styles.legacyBackgroundColor(obj, $el, 'label')).toBe('background-color: purple; ');
    expect($el.attr('style')).toBe('background-color: purple;');
  });

  describe('rgb', () => {
    it('parses an rgb string', () => {
      expect(styles.rgb('rgb(255,100,200)')).toEqual({
        red: 255,
        green: 100,
        blue: 200,
        alpha: 1
      });
    });

    it('supports alpha', () => {
      expect(styles.rgb('rgba(255,100,200,0.5)')).toEqual({
        red: 255,
        green: 100,
        blue: 200,
        alpha: 0.5
      });
      expect(styles.rgb('rgba(20,150,50,0.25)')).toEqual({
        red: 20,
        green: 150,
        blue: 50,
        alpha: 0.25
      });
      expect(styles.rgb('rgba(20,150,50,0)')).toEqual({
        red: 20,
        green: 150,
        blue: 50,
        alpha: 0
      });
    });
  });

});
