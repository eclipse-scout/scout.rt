describe("scout-jquery", function() {

  var $e;

  beforeEach(function() {
    $e = $('<div>');
  });

  describe("isEnabled", function() {

    it("is only false when data enabled is === false", function() {
      expect($e.isEnabled()).toBe(true);
      $e.data('enabled', false);
      expect($e.isEnabled()).toBe(false);
      $e.removeData('enabled');
      expect($e.isEnabled()).toBe(true);
    });

  });

  describe("setEnabled", function() {

    it("DIV does not have disabled attribute", function() {
      $e.setEnabled(false);
      expect($e.hasClass('disabled')).toBe(true);
      expect($e.attr('disabled')).toBeUndefined();
      $e.setEnabled(true);
      expect($e.hasClass('disabled')).toBe(false);
      expect($e.attr('disabled')).toBeUndefined();
    });

    it("INPUT must have disabled attribute", function() {
      $e = $('<input>');
      $e.setEnabled(false);
      expect($e.hasClass('disabled')).toBe(true);
      expect($e.attr('disabled')).toBe('disabled');
      $e.setEnabled(true);
      expect($e.hasClass('disabled')).toBe(false);
      expect($e.attr('disabled')).toBeUndefined();
    });

  });

 });
