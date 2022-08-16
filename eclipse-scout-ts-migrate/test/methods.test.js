import {mockPluginParams} from './test-utils.js';
import {crlfToLf, lfToCrlf} from '../src/common.js';
import methodsPlugin from '../src/methodsPlugin';

describe('methods plugin', () => {
  it('adds parameter types based on names in Classes', async () => {
    let text = `
class Class1 {
  func(menu, enabled) {
  }

  func2(menus, $abc, aaa) {
  }
}`;

    text = lfToCrlf(text);
    let result = await methodsPlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {}})
    );
    result = crlfToLf(result);
    expect(result).toBe(`
import {Menu} from '@eclipse-scout/core';
class Class1 {
  func(menu: Menu, enabled: boolean) {
  }

  func2(menus: Menu[], $abc: JQuery, aaa) {
  }
}`);
  });

  it('adds parameter types based on names in utils', async () => {
    let text = `
export function func(text, visible) {
}

export function func2(widgets, currentDate) {
}`;

    text = lfToCrlf(text);
    let result = await methodsPlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {}})
    );
    result = crlfToLf(result);
    expect(result).toBe(`
import {Widget} from '@eclipse-scout/core';
export function func(text: string, visible: boolean) {
}

export function func2(widgets: Widget[], currentDate: Date) {
}`);
  });

  it('adds missing import in utils', async () => {
    let text = `
import {Widget} from '@eclipse-scout/core';

export function func(menu, formFields) {
}

export function func2(widgets, currentDate) {
}`;

    text = lfToCrlf(text);
    let result = await methodsPlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {}})
    );
    result = crlfToLf(result);
    expect(result).toBe(`
import {FormField, Menu, Widget} from '@eclipse-scout/core';

export function func(menu: Menu, formFields: FormField[]) {
}

export function func2(widgets: Widget[], currentDate: Date) {
}`);
  });

  it('removes types from jsdoc', async () => {
    let text = `
class Class1 {
  /**
   * This method does something.
   *
   * @param {Menu} menu This is a menu
   *     with multiline
   * @param {boolean} [enabled] optional boolean
   * @param [asdf] optional without type
   * @param {object} [options] optional object
   * @param {boolean} [options.abc] options property, keep it because not a param
   */
  func(menu, enabled, asdf, visible, options: object) {
  }

  /**
   * @param [menus] {Menu[]} name and type other way round
   * @param $abc already ok
   * @return {boolean} true if else
   */
  func2(menus, $abc, aaa): boolean {
  }

  /**
   * @param [a] optional param
  */
  func3(a?: boolean) {
  }
}`;

    text = lfToCrlf(text);
    let result = await methodsPlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {}})
    );
    result = crlfToLf(result);
    expect(result).toBe(`
import {Menu} from '@eclipse-scout/core';
class Class1 {
  /**
   * This method does something.
   *
   * @param menu This is a menu
   *     with multiline
   * @param enabled optional boolean
   * @param [asdf] optional without type
   * @param options optional object
   * @param {boolean} [options.abc] options property, keep it because not a param
  */
  func(menu: Menu, enabled: boolean, asdf, visible: boolean, options: object) {
  }

  /**
   * @param menus name and type other way round
   * @param $abc already ok
   * @returns true if else
  */
  func2(menus: Menu[], $abc: JQuery, aaa): boolean {
  }

  /**
   * @param a optional param
  */
  func3(a?: boolean) {
  }
}`);
  });

  it('cleans empty jsdoc', async () => {
    let text = `
class Class1 {
   /**
   * Method desc.
   * @param {boolean} abc
   * @param {boolean} bbb with desc
   * @param {boolean} ccc
   * @return {boolean}
   */
  func(abc: boolean, bbb: boolean, ccc: boolean): boolean {
  }

   /**
   * Method desc.
   * @param {boolean} abc
   */
  func2(abc: boolean): boolean {
  }

  /** @param {boolean} abc single line doc */
  func3(abc: boolean): boolean {
  }

   /** @param abc */
  func4(abc: boolean): boolean {
  }
}`;

    text = lfToCrlf(text);
    let result = await methodsPlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {}})
    );
    result = crlfToLf(result);
    expect(result).toBe(`
class Class1 {
   /**
    * Method desc.
    * @param bbb with desc
   */
  func(abc: boolean, bbb: boolean, ccc: boolean): boolean {
  }

   /**
    * Method desc.
   */
  func2(abc: boolean): boolean {
  }

  /** @param abc single line doc */
  func3(abc: boolean): boolean {
  }

   func4(abc: boolean): boolean {
  }
}`);
  });
});
