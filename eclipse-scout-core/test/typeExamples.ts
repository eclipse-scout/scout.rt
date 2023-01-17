/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// noinspection JSUnusedLocalSymbols

import {Accordion, FormModel, Group, GroupBox, Image, Menu, PropertyChangeEvent, scout, SmartField, StringField} from '../src';
import $ from 'jquery';

function createTyped() {
  let img = scout.create(Image, {
    parent: this,
    autoFit: true
  });
  img.setAutoFit(false);

  // @ts-expect-error (parent is missing)
  scout.create(Image, {
    autoFit: true
  });

  let img1 = scout.create(Image, {
    parent: this,
    autoFit: true
  }, {
    ensureUniqueId: false
  });
  img1.setAutoFit(false);

  let img2 = scout.create({
    objectType: Image,
    parent: this,
    autoFit: true
  });
  img2.setAutoFit(false);

  let img3 = scout.create({
    objectType: Image,
    parent: this
  });
  img3.setAutoFit(false);

  let img4 = scout.create({
    objectType: Image,
    parent: this
  }, {
    ensureUniqueId: false
  });
  img4.setAutoFit(false);
}

function createUntyped() {
  let img = scout.create('Image', {
    parent: this,
    autoFit: true
  });
  img.setAutoFit(false);

  let img1 = scout.create('Image', {
    parent: this,
    autoFit: true
  }, {
    ensureUniqueId: false
  });
  img1.setAutoFit(false);

  let img2 = scout.create({
    objectType: Image,
    parent: this,
    autoFit: true
  });
  img2.setAutoFit(false);

  let img3 = scout.create({
    objectType: Image,
    parent: this
  });
  img3.setAutoFit(false);

  let img4 = scout.create({
    objectType: Image,
    parent: this
  }, {
    ensureUniqueId: false
  });
  img4.setAutoFit(false);
}

function refTypes() {
  let accordion = new Accordion();
  accordion.setGroups([{
    objectType: Group
  }]);
  accordion.setGroups([{
    // @ts-expect-error
    objectType: Accordion
  }]);
  accordion.setGroups([{
    objectType: Group
  }, new Group()
  ]);
  accordion.setGroups([
    new Group()
  ]);

  let form: FormModel = {
    rootGroupBox: {
      // @ts-expect-error
      objectType: Menu
    }
  };
  let form2: FormModel = {
    rootGroupBox: {
      objectType: GroupBox
    }
  };
}

function events() {
  let img = new Image();
  img.trigger('init');
  img.on('init', event => {
    console.log(event.source.autoFit);
  });
  img.one('hierarchyChange', event => {
    console.log(event.oldParent);
    console.log(event.source.autoFit);
  });

  img.on('propertyChange:autoFit', event => {
    console.log(event.source.autoFit);
  });

  img.on('propertyChange', onPropertyChange);

  function onPropertyChange(event: PropertyChangeEvent<any, Image>) {
    console.log(event.newValue);
    console.log(event.source.autoFit);
  }

  img.one('propertyChange:enabled', onEnabledChange);

  function onEnabledChange(event: PropertyChangeEvent<boolean, Image>) {
    let bool: boolean = event.newValue;
    console.log(event.newValue);
    console.log(event.source.autoFit);
  }

  img.off('propertyChange', onPropertyChange);

  img.when('propertyChange:enabled').then(event => {
    let bool: boolean = event.newValue;
    console.log(bool);
    console.log(event.source.autoFit);
  });

  // Without generic
  scout.create(StringField).on('propertyChange:value', event => {
    let value: string = event.newValue;
    console.log(value);
    console.log(event.source.value);
  });

  // With generic
  scout.create(SmartField<string>).on('propertyChange:value', event => {
    let value: string = event.newValue;
    console.log(value);
    console.log(event.source.value);
  });

  // Native
  let $comp = $('<div>');
  $comp[0].addEventListener('focus', event => {
    console.log(event.relatedTarget);
  });
  let event = new FocusEvent('focus');
  $comp[0].dispatchEvent(event);

  // JQuery
  $comp.on('mousedown', event => {
    console.log(event.button);
  });
  $comp.trigger('mousedown', {
    hi: 'there'
  });
  let jqEvent = new $.Event('dblclick', {
    originalEvent: new $.Event('dummy', {
      detail: 2
    })
  });
  $comp.trigger(jqEvent);
}
