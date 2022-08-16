import {mockDiagnostic, mockPluginParams} from './test-utils.js';
import declareMissingClassPropertiesPlugin from '../src/declareMissingClassPropertiesPlugin.js';
import {crlfToLf, lfToCrlf} from '../src/common';

describe('declare-missing-class-properties plugin', () => {
  it.each([2339, 2551])(
    'declares missing class properties with diagnostic code %i',
    async diagnosticCode => {
      let text = `
class Class1 {
  constructor() {
    this.myProp = null;
  }

  method1() {
    console.log(this.property1a);
  }
}`;

      text = lfToCrlf(text);
      const diagnosticFor = str => mockDiagnostic(text, str, {code: diagnosticCode});
      let result = await declareMissingClassPropertiesPlugin.run(
        mockPluginParams({
          text,
          semanticDiagnostics: [
            diagnosticFor('myProp')
          ]
        })
      );
      result = crlfToLf(result);
      expect(result).toBe(`
class Class1 {
  myProp: any;
  constructor() {
    this.myProp = null;
  }

  method1() {
    console.log(this.property1a);
  }
}`);
    }
  );

  it('creates types based on assignment', async () => {
    let text = `
class Class1 {
  constructor() {
    this.ref = new RefObj();
    this.date = new Date();
    this.bool = false;
    this.num = 3;
    this.string = 'asdf';
    this.arr = [];
    this.strArr = ['asdf'];
  }
}`;

    text = lfToCrlf(text);
    const diagnosticFor = str => mockDiagnostic(text, str, {code: 2339});
    let result = await declareMissingClassPropertiesPlugin.run(
      mockPluginParams({
        text,
        semanticDiagnostics: [
          diagnosticFor('ref'),
          diagnosticFor('date'),
          diagnosticFor('bool'),
          diagnosticFor('num'),
          diagnosticFor('string'),
          diagnosticFor('arr'),
          diagnosticFor('strArr')
        ]
      })
    );
    result = crlfToLf(result);
    expect(result).toBe(`
class Class1 {
  ref: RefObj;
  date: Date;
  bool: boolean;
  num: number;
  string: string;
  arr: any[];
  strArr: string[];
  constructor() {
    this.ref = new RefObj();
    this.date = new Date();
    this.bool = false;
    this.num = 3;
    this.string = 'asdf';
    this.arr = [];
    this.strArr = ['asdf'];
  }
}`);
  });

  it('creates types based on names', async () => {
    let text = `
class Class1 {
  constructor() {
    this.menu = null;
    this.menus = [];
    this.$jq = null;
    this._$jq = null;
  }
}`;

    text = lfToCrlf(text);
    const diagnosticFor = str => mockDiagnostic(text, str, {code: 2339});
    let result = await declareMissingClassPropertiesPlugin.run(
      mockPluginParams({
        text,
        semanticDiagnostics: [
          diagnosticFor('menu'),
          diagnosticFor('menus'),
          diagnosticFor('$jq'),
          diagnosticFor('_$jq')
        ]
      })
    );
    result = crlfToLf(result);
    expect(result).toBe(`
import {Menu} from '@eclipse-scout/core';
class Class1 {
  menu: Menu;
  menus: Menu[];
  $jq: JQuery;
  _$jq: JQuery;
  constructor() {
    this.menu = null;
    this.menus = [];
    this.$jq = null;
    this._$jq = null;
  }
}`);
  });


  it('adds missing imports for types based on names', async () => {
    let text = `
class Class1 {
  constructor() {
    this.menu = null;
    this.htmlComp = null;
  }
}`;

    text = lfToCrlf(text);
    const diagnosticFor = str => mockDiagnostic(text, str, {code: 2339});
    let result = await declareMissingClassPropertiesPlugin.run(
      mockPluginParams({
        text,
        semanticDiagnostics: [
          diagnosticFor('menu'),
          diagnosticFor('htmlComp')
        ]
      })
    );
    result = crlfToLf(result);
    expect(result).toBe(`
import {HtmlComponent, Menu} from '@eclipse-scout/core';
class Class1 {
  menu: Menu;
  htmlComp: HtmlComponent;
  constructor() {
    this.menu = null;
    this.htmlComp = null;
  }
}`);
  });
});
