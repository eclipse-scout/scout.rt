// noinspection JSUnresolvedVariable

/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
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
