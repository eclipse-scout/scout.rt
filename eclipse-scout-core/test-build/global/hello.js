/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// noinspection JSUnresolvedVariable

class Desktop extends scout.Desktop {

  constructor() {
    super();
  }

  _jsonModel() {
    return {
      objectType: 'Desktop',
      navigationHandleVisible: false,
      navigationVisible: false,
      headerVisible: false,
      views: [
        {
          objectType: 'Form',
          displayHint: 'view',
          modal: false,
          rootGroupBox: {
            objectType: 'GroupBox',
            borderDecoration: scout.GroupBox.BorderDecoration.EMPTY,
            fields: [
              {
                id: 'NameField',
                objectType: 'StringField',
                label: 'Name'
              },
              {
                id: 'GreetButton',
                objectType: 'Button',
                label: 'Say Hello',
                keyStroke: 'enter',
                processButton: false
              }
            ]
          }
        }
      ]
    };
  }

  _init(model) {
    super._init(model);
    this.widget('GreetButton').on('click', event => {
      let name = this.widget('NameField').value || 'stranger';
      scout.MessageBoxes.openOk(this.session.desktop, `Hello ${name}!`);
    });
  }
}

scout.addObjectFactories({
  'Desktop': () => new Desktop()
});

new scout.App().init({
  bootstrap: {
    textsUrl: 'https://unpkg.com/@eclipse-scout/core@22.0.37/dist/texts.json',
    localesUrl: 'https://unpkg.com/@eclipse-scout/core@22.0.37/dist/locales.json'
  }
});
