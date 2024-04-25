/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {styles} from '../../src/index';

describe('styles', () => {
  let $sandbox: JQuery;

  beforeEach(() => {
    setFixtures(sandbox());
    $sandbox = $('#sandbox');
    $('<style>' +
      '.inner-class {' +
      '  background-color: #FF0000;' +
      '  border-color: #FFFFFF !important;' +
      '}' +
      '.outer-class .middle-class .inner-class {' +
      '  background-color: #00FF00;' +
      '}' +
      '.outer-class .middle-class.variant-b .inner-class {' +
      '  background-color: #0000FF;' +
      '  border-color: #000000 !important;' +
      '}' +
      '</style>').appendTo($sandbox);
  });

  it('can get styles', () => {
    // @ts-expect-error
    expect(styles.get()).toEqual({});
    expect(styles.get('inner-class', 'backgroundColor').backgroundColor).toBe('rgb(255, 0, 0)');
    expect(styles.get(['inner-class'], 'backgroundColor').backgroundColor).toBe('rgb(255, 0, 0)');
    expect(styles.get(['inner-class'], 'borderColor').borderColor).toBe('rgb(255, 255, 255)');
    expect(styles.get(['outer-class', 'inner-class'], 'backgroundColor').backgroundColor).toBe('rgb(255, 0, 0)');
    expect(styles.get(['middle-class', 'inner-class'], 'backgroundColor').backgroundColor).toBe('rgb(255, 0, 0)');
    expect(styles.get(['outer-class', 'middle-class', 'inner-class'], 'backgroundColor').backgroundColor).toBe('rgb(0, 255, 0)');
    expect(styles.get(['outer-class', 'middle-class', 'inner-class'], 'borderColor').borderColor).toBe('rgb(255, 255, 255)');
    expect(styles.get(['middle-class variant-b', 'inner-class'], 'backgroundColor').backgroundColor).toBe('rgb(255, 0, 0)');
    expect(styles.get(['outer-class', 'middle-class variant-b', 'inner-class'], 'backgroundColor').backgroundColor).toBe('rgb(0, 0, 255)');
    expect(styles.get(['outer-class', 'middle-class variant-b', 'inner-class'], 'borderColor').borderColor).toBe('rgb(0, 0, 0)');

    expect(styles.get('inner-class', 'display').display).toBe('none');
    expect(styles.get('inner-class', 'display', {display: 'flex'}).display).toBe('none');
    expect(styles.get('inner-class', 'backgroundColor', {backgroundColor: '#000000'}).backgroundColor).toBe('rgb(0, 0, 0)');
    expect(styles.get('inner-class', 'borderColor', {borderColor: '#000000'}).borderColor).toBe('rgb(255, 255, 255)');
  });

  it('can get first opaque background color', () => {
    let $elem = $sandbox.appendDiv(),
      $childElemBlue = $elem.appendDiv(),
      $childElemTransparentBlue = $elem.appendDiv(),
      $childElemRed = $elem.appendDiv(),
      $childElemTransparentRed = $elem.appendDiv(),
      $grandChildElemBlue = $childElemBlue.appendDiv(),
      $grandChildElemTransparentBlue = $childElemTransparentBlue.appendDiv(),
      $grandChildElemRed = $childElemRed.appendDiv(),
      $grandChildElemTransparentRed = $childElemTransparentRed.appendDiv();

    $childElemBlue.css('background-color', 'rgb(0, 0, 255)');
    $childElemTransparentBlue.css('background-color', 'rgba(0, 0, 255, 0.5)');
    $childElemRed.css('background-color', 'rgb(255, 0, 0)');
    $childElemTransparentRed.css('background-color', 'rgba(255, 0, 0, 0.5)');

    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemBlue)).toBe('rgb(0, 0, 255)');
    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemTransparentBlue)).toBe(undefined);
    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemRed)).toBe('rgb(255, 0, 0)');
    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemTransparentRed)).toBe(undefined);

    $elem.css('background-color', 'rgb(0, 0, 0)');

    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemBlue)).toBe('rgb(0, 0, 255)');
    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemTransparentBlue)).toBe('rgb(0, 0, 0)');
    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemRed)).toBe('rgb(255, 0, 0)');
    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemTransparentRed)).toBe('rgb(0, 0, 0)');

    $grandChildElemBlue.css('background-color', 'rgb(0, 255, 0)');
    $grandChildElemTransparentBlue.css('background-color', 'rgb(255, 255, 0)');
    $grandChildElemRed.css('background-color', 'rgb(255, 0, 255)');
    $grandChildElemTransparentRed.css('background-color', 'rgb(0, 255, 255)');

    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemBlue)).toBe('rgb(0, 255, 0)');
    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemTransparentBlue)).toBe('rgb(255, 255, 0)');
    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemRed)).toBe('rgb(255, 0, 255)');
    expect(styles.getFirstOpaqueBackgroundColor($grandChildElemTransparentRed)).toBe('rgb(0, 255, 255)');
  });

  it('can merge colors', () => {
    // @ts-expect-error
    expect(styles.mergeRgbColors()).toBe(undefined);
    expect(styles.mergeRgbColors('rgb(1,2,3)')).toBe('rgb(0,0,0)'); // no ratio
    expect(styles.mergeRgbColors('#fff', 1)).toBe(undefined); // invalid format
    expect(styles.mergeRgbColors('rgb(1,2,3)', 1)).toBe('rgb(1,2,3)');
    expect(styles.mergeRgbColors('rgb(10,10,10)', 0.5, 'rgb(20,20,20)', 0.5)).toBe('rgb(15,15,15)');
    expect(styles.mergeRgbColors('rgba(10,10,10,0.3)', 0.5, 'rgba(20,20,20,0.7)', 0.5)).toBe('rgb(15,15,15)'); // alpha is ignored
  });

  it('can lighten and darken colors', () => {
    // @ts-expect-error
    expect(styles.darkerColor()).toBe(undefined);
    expect(styles.darkerColor('#fff')).toBe(undefined);
    expect(styles.darkerColor('rgb(10,10,10)')).toBe('rgb(8,8,8)');

    // @ts-expect-error
    expect(styles.lighterColor()).toBe(undefined);
    expect(styles.lighterColor('#000')).toBe(undefined);
    expect(styles.lighterColor('rgb(10,10,10)')).toBe('rgb(59,59,59)');
  });

  it('can calculate and apply legacy styles', () => {
    // @ts-expect-error
    expect(styles.legacyStyle()).toBe('');

    let $el = $sandbox.appendDiv();
    let obj: any = {};
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

  describe('hex to rgb', () => {
    it('converts a hex string to a rgb string', () => {
      expect(styles.hexToRgb('#000')).toEqual('rgba(0,0,0,1)');
      expect(styles.hexToRgb('#123')).toEqual('rgba(17,34,51,1)');
      expect(styles.hexToRgb('#12a')).toEqual('rgba(17,34,170,1)');
      expect(styles.hexToRgb('#abc')).toEqual('rgba(170,187,204,1)');
      expect(styles.hexToRgb('#fff')).toEqual('rgba(255,255,255,1)');

      expect(styles.hexToRgb('#000000')).toEqual('rgba(0,0,0,1)');
      expect(styles.hexToRgb('#123456')).toEqual('rgba(18,52,86,1)');
      expect(styles.hexToRgb('#123abc')).toEqual('rgba(18,58,188,1)');
      expect(styles.hexToRgb('#abcdef')).toEqual('rgba(171,205,239,1)');
      expect(styles.hexToRgb('#ffffff')).toEqual('rgba(255,255,255,1)');
    });

    it('supports alpha', () => {
      expect(styles.hexToRgb('#0000')).toEqual('rgba(0,0,0,0)');
      expect(styles.hexToRgb('#1234')).toEqual('rgba(17,34,51,0.267)'); // 68/255=0.26666666...
      expect(styles.hexToRgb('#12ab')).toEqual('rgba(17,34,170,0.733)'); // 187/255=0.73333333...
      expect(styles.hexToRgb('#abcd')).toEqual('rgba(170,187,204,0.867)'); // 221/255=0.86666666...
      expect(styles.hexToRgb('#ffff')).toEqual('rgba(255,255,255,1)');

      expect(styles.hexToRgb('#00000000')).toEqual('rgba(0,0,0,0)');
      expect(styles.hexToRgb('#12345678')).toEqual('rgba(18,52,86,0.471)'); // 120/255=0.47058823...
      expect(styles.hexToRgb('#1234abcd')).toEqual('rgba(18,52,171,0.804)'); // 205/255=0.80392156...
      expect(styles.hexToRgb('#abcdefab')).toEqual('rgba(171,205,239,0.671)'); // 171/255=0.67058823...
      expect(styles.hexToRgb('#ffffffff')).toEqual('rgba(255,255,255,1)');
    });
  });

  describe('rgb/rgba to hex', () => {
    it('converts a rgb string to a hex string', () => {
      expect(styles.rgbToHex('rgb(0,0,0)')).toEqual('#000000');
      expect(styles.rgbToHex('rgb(17,34,51)')).toEqual('#112233');
      expect(styles.rgbToHex('rgb(17,34,170)')).toEqual('#1122aa');
      expect(styles.rgbToHex('rgb(170,187,204)')).toEqual('#aabbcc');
      expect(styles.rgbToHex('rgb(255,255,255)')).toEqual('#ffffff');

      expect(styles.rgbToHex('rgb(0,0,0)', true)).toEqual('#000000');
      expect(styles.rgbToHex('rgb(18,52,86)', true)).toEqual('#123456');
      expect(styles.rgbToHex('rgb(18,58,188)', true)).toEqual('#123abc');
      expect(styles.rgbToHex('rgb(171,205,239)', true)).toEqual('#abcdef');
      expect(styles.rgbToHex('rgb(255,255,255)', true)).toEqual('#ffffff');
    });

    it('converts a rgba string with no alpha to a hex string', () => {
      expect(styles.rgbToHex('rgba(0,0,0,1)')).toEqual('#000000ff');
      expect(styles.rgbToHex('rgba(17,34,51,1)')).toEqual('#112233ff');
      expect(styles.rgbToHex('rgba(17,34,170,1)')).toEqual('#1122aaff');
      expect(styles.rgbToHex('rgba(170,187,204,1)')).toEqual('#aabbccff');
      expect(styles.rgbToHex('rgba(255,255,255,1)')).toEqual('#ffffffff');

      expect(styles.rgbToHex('rgba(0,0,0,1)', true)).toEqual('#000000');
      expect(styles.rgbToHex('rgba(18,52,86,1)', true)).toEqual('#123456');
      expect(styles.rgbToHex('rgba(18,58,188,1)', true)).toEqual('#123abc');
      expect(styles.rgbToHex('rgba(171,205,239,1)', true)).toEqual('#abcdef');
      expect(styles.rgbToHex('rgba(255,255,255,1)', true)).toEqual('#ffffff');
    });

    it('converts a rgba string with full alpha to a hex string', () => {
      expect(styles.rgbToHex('rgba(0,0,0,0)')).toEqual('#00000000');
      expect(styles.rgbToHex('rgba(17,34,51,0)')).toEqual('#11223300');
      expect(styles.rgbToHex('rgba(17,34,170,0)')).toEqual('#1122aa00');
      expect(styles.rgbToHex('rgba(170,187,204,0)')).toEqual('#aabbcc00');
      expect(styles.rgbToHex('rgba(255,255,255,0)')).toEqual('#ffffff00');

      expect(styles.rgbToHex('rgba(0,0,0,0)', true)).toEqual('#000000');
      expect(styles.rgbToHex('rgba(18,52,86,0)', true)).toEqual('#123456');
      expect(styles.rgbToHex('rgba(18,58,188,0)', true)).toEqual('#123abc');
      expect(styles.rgbToHex('rgba(171,205,239,0)', true)).toEqual('#abcdef');
      expect(styles.rgbToHex('rgba(255,255,255,0)', true)).toEqual('#ffffff');
    });

    it('converts a rgba string with alpha to a hex string', () => {
      expect(styles.rgbToHex('rgba(0,0,0,0.1)')).toEqual('#0000001a');
      expect(styles.rgbToHex('rgba(17,34,51,0.3)')).toEqual('#1122334d');
      expect(styles.rgbToHex('rgba(17,34,170,0.5)')).toEqual('#1122aa80');
      expect(styles.rgbToHex('rgba(170,187,204,0.7)')).toEqual('#aabbccb3');
      expect(styles.rgbToHex('rgba(255,255,255,0.9)')).toEqual('#ffffffe6');

      expect(styles.rgbToHex('rgba(0,0,0,0.9)', true)).toEqual('#000000');
      expect(styles.rgbToHex('rgba(18,52,86,0.7)', true)).toEqual('#123456');
      expect(styles.rgbToHex('rgba(18,58,188,0.5)', true)).toEqual('#123abc');
      expect(styles.rgbToHex('rgba(171,205,239,0.3)', true)).toEqual('#abcdef');
      expect(styles.rgbToHex('rgba(255,255,255,0.1)', true)).toEqual('#ffffff');
    });
  });
});
