/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Device, scout, Status} from '../../../../src/index';
import {FormSpecHelper} from '@eclipse-scout/testing';

describe('FileChooserField', () => {
  let session;
  let helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  describe('setValue', () => {
    let field;

    beforeEach(() => {
      field = helper.createField('FileChooserField');
    });

    it('sets the file as value', () => {
      if (!Device.get().supportsFileConstructor()) {
        return;
      }
      let file = new File(['lorem'], 'ipsum.txt');
      field.render();
      field.setValue(file);
      expect(field.value).toBe(file);
      expect(field.displayText).toBe('ipsum.txt');
    });

    it('sets no file as value', () => {
      field.render();
      field.setValue(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
    });
  });

  describe('maximumUploadSize', () => {

    it('is validated when setting new value', () => {
      if (!Device.get().supportsFileConstructor()) {
        return;
      }
      let smallFile = new File(['a'], 'small file.txt');
      let largeFile = new File(['abcdefghijklmnopqrstuvwxyz'], 'large file.txt');
      let largerFile = new File(['abcdefghijklmnopqrstuvwxyz0123456789'], 'larger file.txt');

      let field = helper.createField('FileChooserField');
      field.setMaximumUploadSize(5); // 5 bytes

      field.setValue(largeFile);
      expect(field.errorStatus instanceof Status).toBe(true);
      expect(field.value).toBe(null);

      field.setValue(smallFile);
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(smallFile);

      field.setValue(largerFile);
      expect(field.errorStatus instanceof Status).toBe(true);
      expect(field.value).toBe(smallFile);
    });

    it('is not validated when changing maximumUploadSize', () => {
      if (!Device.get().supportsFileConstructor()) {
        return;
      }
      let largeFile = new File(['abcdefghijklmnopqrstuvwxyz'], 'large file.txt');

      let field = helper.createField('FileChooserField');
      field.setValue(largeFile);
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(largeFile);

      field.setMaximumUploadSize(10);
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(largeFile);
    });
  });

  describe('label', () => {

    it('is linked with the field', () => {
      let field = scout.create('FileChooserField', {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      expect(field.$field.attr('aria-labelledby')).toBeTruthy();
      expect(field.$field.attr('aria-labelledby')).toBe(field.$label.attr('id'));
    });

    it('focuses the field when clicked', () => {
      let field = scout.create('FileChooserField', {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      field.$label.triggerClick();
      expect(field.$field).toBeFocused();
    });

  });

});
