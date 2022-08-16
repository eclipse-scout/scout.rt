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
});
