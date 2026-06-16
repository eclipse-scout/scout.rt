/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Device, FileChooserField, scout, Status} from '../../../../src/index';
import {FormSpecHelper, JQueryTesting} from '../../../../src/testing/index';

describe('FileChooserField', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;

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
    let field: FileChooserField;

    beforeEach(() => {
      field = helper.createField(FileChooserField);
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

    // Test for ticket #288702. Value had wrong type 'string' after clear() has been called.
    // Thus an error status has been displayed for no reason.
    it('uses proper value type after clear() has been called', () => {
      field.render();
      field.setDisplayText('unicorn-website-design.zip');
      expect(field.errorStatus).toBe(null);
      field.clear();
      expect(field.errorStatus).toBe(null);
    });
  });

  describe('acceptTypes', () => {

    it('is propagated to fileInput', () => {
      const field = scout.create(FileChooserField, {
        parent: session.desktop,
        acceptTypes: '.png,.jpg'
      });
      field.render();

      expect(field.fileInput.acceptTypes).toBe('.png,.jpg');
      expect(field.fileInput.$fileInput.attr('accept')).toBe('.png,.jpg');

      field.setAcceptTypes('.pdf');
      expect(field.fileInput.acceptTypes).toBe('.pdf');
      expect(field.fileInput.$fileInput.attr('accept')).toBe('.pdf');

      field.setAcceptTypes(null);
      expect(field.fileInput.acceptTypes).toBeNull();
      expect(field.fileInput.$fileInput.attr('accept')).toBeUndefined();

      field.setAcceptTypes('.csv');
      expect(field.fileInput.acceptTypes).toBe('.csv');
      expect(field.fileInput.$fileInput.attr('accept')).toBe('.csv');
    });
  });

  describe('displayText', () => {

    it('is propagated to fileInput', () => {
      const field = scout.create(FileChooserField, {
        parent: session.desktop,
        displayText: 'Foo.png'
      });

      expect(field.fileInput.text).toBe('Foo.png');

      field.setDisplayText('Bar.pdf');
      expect(field.fileInput.text).toBe('Bar.pdf');

      field.setDisplayText(null);
      expect(field.fileInput.text).toBeNull();

      field.setDisplayText('test.csv');
      expect(field.fileInput.text).toBe('test.csv');
    });

    it('updates hasText', () => {
      const field = scout.create(FileChooserField, {
        parent: session.desktop,
        displayText: 'Foo.png'
      });
      field.render();

      expect(field.hasText).toBeTrue();

      field.setDisplayText('Bar.pdf');
      expect(field.hasText).toBeTrue();

      field.setDisplayText(null);
      expect(field.hasText).toBeFalse();

      field.setDisplayText('test.csv');
      expect(field.hasText).toBeTrue();

      field.remove();
      expect(field.hasText).toBeTrue();

      field.setDisplayText(null);
      expect(field.hasText).toBeTrue();

      field.render();
      expect(field.hasText).toBeFalse();

      field.remove();
      expect(field.hasText).toBeFalse();

      field.setDisplayText('test.csv');
      expect(field.hasText).toBeFalse();

      field.render();
      expect(field.hasText).toBeTrue();
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

      let field = helper.createField(FileChooserField);
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

      let field = helper.createField(FileChooserField);
      field.setValue(largeFile);
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(largeFile);

      field.setMaximumUploadSize(10);
      expect(field.errorStatus).toBe(null);
      expect(field.value).toBe(largeFile);
    });

    it('is propagated to fileInput', () => {
      const field = scout.create(FileChooserField, {
        parent: session.desktop,
        maximumUploadSize: 42
      });

      expect(field.fileInput.maximumUploadSize).toBe(42);

      field.setMaximumUploadSize(13);
      expect(field.fileInput.maximumUploadSize).toBe(13);

      field.setMaximumUploadSize(null);
      expect(field.fileInput.maximumUploadSize).toBeNull();

      field.setMaximumUploadSize(7);
      expect(field.fileInput.maximumUploadSize).toBe(7);
    });
  });

  describe('label', () => {

    it('focuses the field when clicked', () => {
      let field = scout.create(FileChooserField, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      JQueryTesting.triggerClick(field.$label);
      expect(field.$field).toBeFocused();
    });

  });

  describe('aria properties', () => {

    it('has aria-labelledby set', () => {
      let field = scout.create(FileChooserField, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      expect(field.$field.attr('aria-labelledby')).toBeTruthy();
      expect(field.$field.attr('aria-labelledby')).toBe(field.$label.attr('id'));
      expect(field.$field.attr('aria-label')).toBeFalsy();
    });
  });
});
