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
import {Device, FileChooser, scout} from '../../src/index';

describe('FileChooser', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('open', () => {
    it('opens the chooser', () => {
      let fileChooser = scout.create(FileChooser, {
        parent: session.desktop
      });
      fileChooser.open();
      expect(fileChooser.rendered).toBe(true);
      expect($('.file-chooser').length).toBe(1);
      fileChooser.close();
    });
  });

  describe('close', () => {
    it('closes the chooser', () => {
      let fileChooser = scout.create(FileChooser, {
        parent: session.desktop
      });
      fileChooser.open();
      fileChooser.close();
      expect($('.file-chooser').length).toBe(0);
      expect(fileChooser.destroyed).toBe(true);
    });
  });

  describe('addFiles', () => {
    it('adds the files if multiSelect is true', () => {
      if (!Device.get().supportsFileConstructor()) {
        return;
      }
      let fileChooser = scout.create(FileChooser, {
        parent: session.desktop,
        multiSelect: true
      });
      fileChooser.open();
      let file1 = new File([''], 'file 1');
      let file2 = new File([''], 'file 2');
      let file3 = new File([''], 'file 3');
      fileChooser.addFiles([file1, file2]);
      fileChooser.addFiles(file3);
      expect(fileChooser.files).toEqual([file1, file2, file3]);
      expect(fileChooser.$files.children().eq(0).text().indexOf('file 1')).toBe(0);
      expect(fileChooser.$files.children().eq(1).text().indexOf('file 2')).toBe(0);
      expect(fileChooser.$files.children().eq(2).text().indexOf('file 3')).toBe(0);
      fileChooser.close();
    });

    it('does only add one file if multiSelect is false', () => {
      if (!Device.get().supportsFileConstructor()) {
        return;
      }
      let fileChooser = scout.create(FileChooser, {
        parent: session.desktop,
        multiSelect: false
      });
      fileChooser.open();
      let file1 = new File([''], 'file 1');
      let file2 = new File([''], 'file 2');
      let file3 = new File([''], 'file 3');
      fileChooser.addFiles([file1, file2]);
      expect(fileChooser.files).toEqual([file1]);
      expect(fileChooser.$files.children().length).toBe(1);
      expect(fileChooser.$files.children().eq(0).text().indexOf('file 1')).toBe(0);

      fileChooser.addFiles(file3);
      expect(fileChooser.files).toEqual([file3]);
      expect(fileChooser.$files.children().length).toBe(1);
      expect(fileChooser.$files.children().eq(0).text().indexOf('file 3')).toBe(0);
      fileChooser.close();
    });
  });

  describe('removeFile', () => {
    it('removes the file', () => {
      if (!Device.get().supportsFileConstructor()) {
        return;
      }
      let file1 = new File([''], 'file 1');
      let file2 = new File([''], 'file 2');
      let file3 = new File([''], 'file 3');
      let fileChooser = scout.create(FileChooser, {
        parent: session.desktop,
        multiSelect: true,
        files: [file1, file2, file3]
      });
      fileChooser.open();
      expect(fileChooser.files).toEqual([file1, file2, file3]);
      expect(fileChooser.$files.children().eq(0).text().indexOf('file 1')).toBe(0);
      expect(fileChooser.$files.children().eq(1).text().indexOf('file 2')).toBe(0);
      expect(fileChooser.$files.children().eq(2).text().indexOf('file 3')).toBe(0);

      fileChooser.removeFile(file2);
      expect(fileChooser.files).toEqual([file1, file3]);
      expect(fileChooser.$files.children().eq(0).text().indexOf('file 1')).toBe(0);
      expect(fileChooser.$files.children().eq(1).text().indexOf('file 3')).toBe(0);

      fileChooser.close();
    });
  });

});
