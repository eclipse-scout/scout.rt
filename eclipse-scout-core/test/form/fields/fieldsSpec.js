/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {fields} from '../../../src/index';
import {FormSpecHelper} from '../../../src/testing';

describe('scout.fields', () => {
  let session;
  let helper;
  let form;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    form = helper.createFormWithFieldsAndTabBoxes();
  });

  it('selectAllParentTabsOf', () => {
    let field2 = form.widget('Field2'),
      tabBox = form.widget('TabBox'),
      tabA = form.widget('TabA'),
      fieldA1 = form.widget('FieldA1'),
      tabBoxA = form.widget('TabBoxA'),
      tabAA = form.widget('TabAA'),
      fieldAA2 = form.widget('FieldAA2'),
      tabAB = form.widget('TabAB'),
      fieldAB1 = form.widget('FieldAB1'),
      tabAC = form.widget('TabAC'),
      fieldAC1 = form.widget('FieldAC1'),
      tabB = form.widget('TabB'),
      fieldB3 = form.widget('FieldB3');

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAA);

    fields.selectAllParentTabsOf(field2);

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAA);

    fields.selectAllParentTabsOf(fieldAC1);

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAC);

    fields.selectAllParentTabsOf(fieldB3);

    expect(tabBox.selectedTab).toBe(tabB);
    expect(tabBoxA.selectedTab).toBe(tabAC);

    fields.selectAllParentTabsOf(fieldA1);

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAC);

    fields.selectAllParentTabsOf(fieldAA2);

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAA);

    fields.selectAllParentTabsOf(fieldAB1);

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAB);
  });

  it('selectIfIsTab', () => {
    let field2 = form.widget('Field2'),
      tabBox = form.widget('TabBox'),
      tabA = form.widget('TabA'),
      tabBoxA = form.widget('TabBoxA'),
      tabAA = form.widget('TabAA'),
      fieldAA2 = form.widget('FieldAA2'),
      tabAB = form.widget('TabAB'),
      tabAC = form.widget('TabAC'),
      tabB = form.widget('TabB'),
      fieldB3 = form.widget('FieldB3');

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAA);

    fields.selectIfIsTab(field2);

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAA);

    fields.selectIfIsTab(fieldB3);

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAA);

    fields.selectIfIsTab(tabB);

    expect(tabBox.selectedTab).toBe(tabB);
    expect(tabBoxA.selectedTab).toBe(tabAA);

    fields.selectIfIsTab(tabAC);

    expect(tabBox.selectedTab).toBe(tabB);
    expect(tabBoxA.selectedTab).toBe(tabAC);

    fields.selectIfIsTab(fieldAA2);

    expect(tabBox.selectedTab).toBe(tabB);
    expect(tabBoxA.selectedTab).toBe(tabAC);

    fields.selectIfIsTab(tabA);

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAC);

    fields.selectIfIsTab(tabAB);

    expect(tabBox.selectedTab).toBe(tabA);
    expect(tabBoxA.selectedTab).toBe(tabAB);
  });
});
