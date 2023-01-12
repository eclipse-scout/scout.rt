/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {mockPluginParams} from './test-utils.js';
import {crlfToLf} from '../src/common.js';
import typedObjectTypePlugin from '../src/typedObjectTypePlugin.js';

describe('typed-object-type plugin', () => {
  it('converts objectTypes in models', async () => {
    let text = `\
export default () => ({
  id: 'jswidgets.AccordionForm',
  displayHint: 'view',
  rootGroupBox: {
    id: 'MainBox',
    objectType: 'GroupBox',
    logicalGrid: 'scout.HorizontalGrid',
    fields: [
      {
        id: 'DetailBox',
        objectType: 'StringField',
        gridColumnCount: 2
      },
      {
        objectType: SmartField,
        lookupCall: 'StaticLookupCall'
      }
    ]
  }
});`;
    let result = await typedObjectTypePlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {}})
    );

    result = crlfToLf(result);
    expect(result).toBe(`\
import {GroupBox, HorizontalGrid, StaticLookupCall, StringField} from '@eclipse-scout/core';
export default () => ({
  id: 'jswidgets.AccordionForm',
  displayHint: 'view',
  rootGroupBox: {
    id: 'MainBox',
    objectType: GroupBox,
    logicalGrid: HorizontalGrid,
    fields: [
      {
        id: 'DetailBox',
        objectType: StringField,
        gridColumnCount: 2
      },
      {
        objectType: SmartField,
        lookupCall: StaticLookupCall
      }
    ]
  }
});`);
  });

  it('converts objectTypes in scout.create calls', async () => {
    let text = `\
export function func() {
  let group = scout.create('Group', {
    parent: this.accordion,
    title: title,
    body: {
      objectType: 'TileGrid',
      gridColumnCount: 6
    }
  });
}`;
    let result = await typedObjectTypePlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {}})
    );

    result = crlfToLf(result);
    expect(result).toBe(`\
import {Group, TileGrid} from '@eclipse-scout/core';
export function func() {
  let group = scout.create(Group, {
    parent: this.accordion,
    title: title,
    body: {
      objectType: TileGrid,
      gridColumnCount: 6
    }
  });
}`);
  });

  it('puts file imports after module imports', async () => {
    let text = `\
export function func() {
  let group = scout.create('Group', {
    parent: this.accordion,
    body: {
      objectType: 'jswidgets.TileGrid'
    }
  });
}`;
    let result = await typedObjectTypePlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {moduleMap: {jswidgets: '../index'}}})
    );

    result = crlfToLf(result);
    expect(result).toBe(`\
import {Group} from '@eclipse-scout/core';
import {TileGrid} from '../index';
export function func() {
  let group = scout.create(Group, {
    parent: this.accordion,
    body: {
      objectType: TileGrid
    }
  });
}`);
  });

  it('puts file imports after existing module imports', async () => {
    let text = `\
import {Group} from '@eclipse-scout/core';
export function func() {
  let group = scout.create(Group, {
    parent: this.accordion,
    body: {
      objectType: 'jswidgets.TileGrid'
    }
  });
}`;
    let result = await typedObjectTypePlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {moduleMap: {jswidgets: '../index'}}})
    );

    result = crlfToLf(result);
    expect(result).toBe(`\
import {Group} from '@eclipse-scout/core';
import {TileGrid} from '../index';
export function func() {
  let group = scout.create(Group, {
    parent: this.accordion,
    body: {
      objectType: TileGrid
    }
  });
}`);
  });

  it('puts module imports before existing file imports', async () => {
    let text = `\
import {TileGrid} from '../index';
export function func() {
  let group = scout.create('Group', {
    parent: this.accordion,
    body: {
      objectType: TileGrid
    }
  });
}`;
    let result = await typedObjectTypePlugin.run(
      mockPluginParams({text, fileName: 'file.ts', options: {moduleMap: {jswidgets: '../index'}}})
    );

    result = crlfToLf(result);
    expect(result).toBe(`\
import {Group} from '@eclipse-scout/core';
import {TileGrid} from '../index';
export function func() {
  let group = scout.create(Group, {
    parent: this.accordion,
    body: {
      objectType: TileGrid
    }
  });
}`);
  });
});
