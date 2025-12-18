/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  CheckBoxField, DateField, dates, Form, FormField, GroupBox, ListBox, NumberField, ObjectOrChildModel, RadioButton, RadioButtonGroup, scout, SearchFilterTextBuilder, SequenceBox, SmartField, StringField, TreeBox
} from '../../../../src';
import {DummyLookupCall} from '../../../../src/testing/lookup/DummyLookupCall';

describe('SearchFilterTextBuilder', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();

    session.textMap.add('Yes', 'Yes');
    session.textMap.add('No', 'No');
  });

  async function openForm(fields: ObjectOrChildModel<FormField>[]): Promise<Form> {
    let form = scout.create(Form, {
      parent: session.desktop,
      rootGroupBox: {
        objectType: GroupBox,
        fields
      }
    });
    await form.open();
    return form;
  }

  it('supports StringField', async () => {
    let form = await openForm([{objectType: StringField}]);
    let field = form.findChild(StringField);
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('');

    field.setValue('value');
    expect(await builder.build(form)).toEqual('value');

    field.setLabel('label');
    expect(await builder.build(form)).toEqual('label: value');

    field.setValue(null);
    expect(await builder.build(form)).toEqual('');
  });

  it('supports NumberField', async () => {
    let form = await openForm([{objectType: NumberField}]);
    let field = form.findChild(NumberField);
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('');

    field.setValue(3);
    expect(await builder.build(form)).toEqual('3');

    field.setLabel('label');
    expect(await builder.build(form)).toEqual('label: 3');

    field.setValue(null);
    expect(await builder.build(form)).toEqual('');
  });

  it('supports CheckBoxField', async () => {
    let form = await openForm([{objectType: CheckBoxField}]);

    let field = form.findChild(CheckBoxField);
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('No');

    field.setValue(true);
    expect(await builder.build(form)).toEqual('Yes');

    field.setLabel('label');
    expect(await builder.build(form)).toEqual('label: Yes');

    field.setValue(null);
    expect(await builder.build(form)).toEqual('label: No');
  });

  it('supports DateField', async () => {
    let form = await openForm([{objectType: DateField}]);
    let field = form.findChild(DateField);
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('');

    field.setValue(dates.create('2025-12-24 12:00'));
    expect(await builder.build(form)).toEqual('24.12.2025');

    field.setLabel('label');
    expect(await builder.build(form)).toEqual('label: 24.12.2025');

    field.setHasTime(true);
    field.setValue(dates.create('2025-12-24 12:00'));
    expect(await builder.build(form)).toEqual('label: 24.12.2025 12:00');

    field.setHasDate(false);
    expect(await builder.build(form)).toEqual('label: 12:00');

    field.setValue(null);
    expect(await builder.build(form)).toEqual('');
  });

  it('supports SmartField', async () => {
    let form = await openForm([{
      objectType: SmartField,
      lookupCall: DummyLookupCall
    }]);
    let field = form.findChild(SmartField);
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('');

    field.setValue(1);
    await field.when('propertyChange:displayText');
    expect(await builder.build(form)).toEqual('Foo');

    field.setLabel('label');
    expect(await builder.build(form)).toEqual('label: Foo');

    field.setValue(null);
    expect(await builder.build(form)).toEqual('');
  });

  it('supports RadioButtonGroup', async () => {
    let form = await openForm([{
      objectType: RadioButtonGroup,
      lookupCall: DummyLookupCall,
      fields: [{
        objectType: RadioButton
      }, {
        objectType: RadioButton
      }]
    }
    ]);

    let field = form.findChild(RadioButtonGroup);
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('');

    field.setValue(1);
    await field.when('propertyChange:displayText');
    expect(await builder.build(form)).toEqual('Foo');

    field.setLabel('label');
    expect(await builder.build(form)).toEqual('label: Foo');

    field.setValue(null);
    expect(await builder.build(form)).toEqual('');
  });

  it('supports ListBox', async () => {
    let form = await openForm([{
      objectType: ListBox,
      lookupCall: DummyLookupCall
    }]);
    let field = form.findChild(ListBox);
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('');

    field.setValue(1);
    await field.when('propertyChange:displayText');
    expect(await builder.build(form)).toEqual('Foo');

    field.setLabel('label');
    expect(await builder.build(form)).toEqual('label: Foo');

    field.setValue([1, 3]);
    expect(await builder.build(form)).toEqual('label: Foo, Baz');

    field.setValue(null);
    expect(await builder.build(form)).toEqual('');
  });

  it('supports TreeBox', async () => {
    let form = await openForm([{
      objectType: TreeBox,
      lookupCall: DummyLookupCall
    }]);
    let field = form.findChild(TreeBox);
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('');

    field.setValue(1);
    await field.when('propertyChange:displayText');
    expect(await builder.build(form)).toEqual('Foo');

    field.setLabel('label');
    expect(await builder.build(form)).toEqual('label: Foo');

    field.setValue([1, 3]);
    expect(await builder.build(form)).toEqual('label: Foo, Baz');

    field.setValue(null);
    expect(await builder.build(form)).toEqual('');
  });

  it('separates search texts with linebreak', async () => {
    let form = await openForm([{objectType: NumberField}, {objectType: StringField}]);
    let numberField = form.findChild(NumberField);
    let stringField = form.findChild(StringField);
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('');

    numberField.setValue(100);
    expect(await builder.build(form)).toEqual('100');

    stringField.setValue('string');
    expect(await builder.build(form)).toEqual('100\nstring');

    stringField.setLabel('str label');
    expect(await builder.build(form)).toEqual('100\nstr label: string');

    numberField.setLabel('num label');
    expect(await builder.build(form)).toEqual('num label: 100\nstr label: string');

    numberField.setValue(null);
    expect(await builder.build(form)).toEqual('str label: string');
  });

  it('supports SequenceBox', async () => {
    let form = await openForm([{
      objectType: SequenceBox,
      fields: [{
        objectType: DateField
      }, {
        objectType: DateField
      }]
    }
    ]);
    let field = form.findChild(SequenceBox);
    let dateField1 = field.fields[0] as DateField;
    let dateField2 = field.fields[1] as DateField;
    let builder = scout.create(SearchFilterTextBuilder);
    expect(await builder.build(form)).toEqual('');

    dateField1.setValue(dates.create('2025-12-24 12:00'));
    expect(await builder.build(form)).toEqual('24.12.2025');

    dateField1.setLabel('label1');
    expect(await builder.build(form)).toEqual('label1: 24.12.2025');

    field.setLabel('seq label');
    expect(await builder.build(form)).toEqual('seq label label1: 24.12.2025');

    dateField1.setLabel(null);
    expect(await builder.build(form)).toEqual('seq label: 24.12.2025');

    dateField1.setValue(null);
    expect(await builder.build(form)).toEqual('');

    field.setLabel(null);
    expect(await builder.build(form)).toEqual('');

    dateField2.setValue(dates.create('2026-01-10 12:00'));
    expect(await builder.build(form)).toEqual('10.01.2026');

    dateField2.setLabel('label2');
    expect(await builder.build(form)).toEqual('label2: 10.01.2026');

    field.setLabel('seq label');
    expect(await builder.build(form)).toEqual('seq label label2: 10.01.2026');

    dateField2.setLabel(null);
    expect(await builder.build(form)).toEqual('seq label: 10.01.2026');

    dateField2.setValue(null);
    expect(await builder.build(form)).toEqual('');

    field.setLabel('seq label');
    dateField1.setValue(dates.create('2025-12-24 12:00'));
    dateField2.setValue(dates.create('2026-01-10 12:00'));
    expect(await builder.build(form)).toEqual('seq label: 24.12.2025\nseq label: 10.01.2026');

    dateField1.setLabel('label1');
    expect(await builder.build(form)).toEqual('seq label label1: 24.12.2025\nseq label: 10.01.2026');

    dateField2.setLabel('label2');
    expect(await builder.build(form)).toEqual('seq label label1: 24.12.2025\nseq label label2: 10.01.2026');

    dateField1.setValue(null);
    expect(await builder.build(form)).toEqual('seq label label2: 10.01.2026');

    dateField2.setValue(null);
    expect(await builder.build(form)).toEqual('');
  });
});
