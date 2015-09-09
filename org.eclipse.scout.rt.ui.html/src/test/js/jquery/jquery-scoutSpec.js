describe('scout-jquery', function() {

  var $e;

  /**
   * We must append $e to the DOM, because otherwise test would fail in some browsers (Chrome, PhantomJS).
   */
  beforeEach(function() {
    setFixtures(sandbox());
    $e = $('<div>');
    $e.appendTo($('#sandbox'));
  });

  describe('isEnabled', function() {

    it('is only false when class disabled is set', function() {
      expect($e.isEnabled()).toBe(true);
      $e.addClass('disabled');
      expect($e.isEnabled()).toBe(false);
      $e.removeClass('disabled');
      expect($e.isEnabled()).toBe(true);
    });

  });

  describe('isVisible', function() {

    it('returns true when display != none and visiblity != hidden', function() {
      expect($e.isVisible()).toBe(true);
      $e.css('display', 'none');
      expect($e.isVisible()).toBe(false);
      $e.css('display', '');
      expect($e.isVisible()).toBe(true);
      $e.css('visibility', 'hidden');
      expect($e.isVisible()).toBe(false);
      $e.css('visibility', '');
      expect($e.isVisible()).toBe(true);
    });

  });

  describe('setEnabled', function() {

    it('DIV does not have disabled attribute', function() {
      $e.setEnabled(false);
      expect($e.hasClass('disabled')).toBe(true);
      expect($e.attr('disabled')).toBeUndefined();
      $e.setEnabled(true);
      expect($e.hasClass('disabled')).toBe(false);
      expect($e.attr('disabled')).toBeUndefined();
    });

    it('INPUT must have disabled attribute', function() {
      $e = $('<input>');
      $e.setEnabled(false);
      expect($e.hasClass('disabled')).toBe(true);
      expect($e.attr('disabled')).toBe('disabled');
      $e.setEnabled(true);
      expect($e.hasClass('disabled')).toBe(false);
      expect($e.attr('disabled')).toBeUndefined();
    });

  });

  describe('toggleAttr', function() {

    it('is toggles attribute', function() {
      expect($e.attr('test')).toBeUndefined();
      $e.toggleAttr('test');
      expect($e.attr('test')).toBe('test');
      $e.toggleAttr('test');
      expect($e.attr('test')).toBeUndefined();
      $e.toggleAttr('test', false);
      expect($e.attr('test')).toBeUndefined();
      $e.toggleAttr('test', true);
      expect($e.attr('test')).toBe('test');
      $e.toggleAttr('test', true);
      expect($e.attr('test')).toBe('test');
      $e.toggleAttr('test');
      expect($e.attr('test')).toBeUndefined();
      $e.toggleAttr('test', true, 1);
      expect($e.attr('test')).toBe('1');
      $e.toggleAttr('test', true, 'one');
      expect($e.attr('test')).toBe('one');
      $e.toggleAttr('test', false, 'bla');
      expect($e.attr('test')).toBeUndefined();
    });

  });

 });
