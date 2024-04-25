/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {fields, Form, StringField, TabBox, TabItem} from '../../../src/index';
import {FormSpecHelper} from '../../../src/testing';

describe('fields', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;
  let form: Form;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    form = helper.createFormWithFieldsAndTabBoxes();
  });

  it('selectAllParentTabsOf', () => {
    let field2 = form.widget('Field2', StringField),
      tabBox = form.widget('TabBox', TabBox),
      tabA = form.widget('TabA', TabItem),
      fieldA1 = form.widget('FieldA1', StringField),
      tabBoxA = form.widget('TabBoxA', TabBox),
      tabAA = form.widget('TabAA', TabItem),
      fieldAA2 = form.widget('FieldAA2', StringField),
      tabAB = form.widget('TabAB', TabItem),
      fieldAB1 = form.widget('FieldAB1', StringField),
      tabAC = form.widget('TabAC', TabItem),
      fieldAC1 = form.widget('FieldAC1', StringField),
      tabB = form.widget('TabB', TabItem),
      fieldB3 = form.widget('FieldB3', StringField);

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
    let field2 = form.widget('Field2', StringField),
      tabBox = form.widget('TabBox', TabBox),
      tabA = form.widget('TabA', TabItem),
      tabBoxA = form.widget('TabBoxA', TabBox),
      tabAA = form.widget('TabAA', TabItem),
      fieldAA2 = form.widget('FieldAA2', StringField),
      tabAB = form.widget('TabAB', TabItem),
      tabAC = form.widget('TabAC', TabItem),
      tabB = form.widget('TabB', TabItem),
      fieldB3 = form.widget('FieldB3', StringField);

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
