describe('FileChooser', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('open', function() {
    it('opens the chooser', function() {
      var fileChooser = scout.create('FileChooser', {
        parent: createNullParent(session)
      });
      fileChooser.open();
      expect(fileChooser.rendered).toBe(true);
      expect($('.file-chooser').length).toBe(1);
      fileChooser.close();
    });
  });

  describe('close', function() {
    it('closes the chooser', function() {
      var fileChooser = scout.create('FileChooser', {
        parent: createNullParent(session)
      });
      fileChooser.open();
      fileChooser.close();
      expect($('.file-chooser').length).toBe(0);
      expect(fileChooser.destroyed).toBe(true);
    });
  });

  describe('addFiles', function() {
    it('adds the files if multiSelect is true', function() {
      if (!scout.device.supportsFileConstructor()) {
        return;
      }
      var fileChooser = scout.create('FileChooser', {
        parent: createNullParent(session),
        multiSelect: true
      });
      fileChooser.open();
      var file1 = new File([''], 'file 1');
      var file2 = new File([''], 'file 2');
      var file3 = new File([''], 'file 3');
      fileChooser.addFiles([file1, file2]);
      fileChooser.addFiles(file3);
      expect(fileChooser.files).toEqual([file1, file2, file3]);
      expect(fileChooser.$files.children().eq(0).text().indexOf('file 1')).toBe(0);
      expect(fileChooser.$files.children().eq(1).text().indexOf('file 2')).toBe(0);
      expect(fileChooser.$files.children().eq(2).text().indexOf('file 3')).toBe(0);
      fileChooser.close();
    });

    it('does only add one file if multiSelect is false', function() {
      if (!scout.device.supportsFileConstructor()) {
        return;
      }
      var fileChooser = scout.create('FileChooser', {
        parent: createNullParent(session),
        multiSelect: false
      });
      fileChooser.open();
      var file1 = new File([''], 'file 1');
      var file2 = new File([''], 'file 2');
      var file3 = new File([''], 'file 3');
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

  describe('removeFile', function() {
    it('removes the file', function() {
      if (!scout.device.supportsFileConstructor()) {
        return;
      }
      var file1 = new File([''], 'file 1');
      var file2 = new File([''], 'file 2');
      var file3 = new File([''], 'file 3');
      var fileChooser = scout.create('FileChooser', {
        parent: createNullParent(session),
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
