describe("scout.strings", function() {

  describe("nl2br", function() {

    it("can convert newlines to br tags", function() {
      expect(scout.strings.nl2br()).toBe(undefined);
      expect(scout.strings.nl2br('')).toBe('');
      expect(scout.strings.nl2br('Hello')).toBe('Hello');
      expect(scout.strings.nl2br('Hello\nGoodbye')).toBe('Hello<br>Goodbye');
      expect(scout.strings.nl2br('Hello\nGoodbye\n')).toBe('Hello<br>Goodbye<br>');
      expect(scout.strings.nl2br('Hello\n\nGoodbye')).toBe('Hello<br><br>Goodbye');
      expect(scout.strings.nl2br('Hello\n<br>\nGoodbye')).toBe('Hello<br><br><br>Goodbye');
      expect(scout.strings.nl2br('Hello\n\r\nGoodbye')).toBe('Hello<br>\r<br>Goodbye');
    });

  });

  describe("removeAmpersand", function() {

    it("can remove ampersands", function() {
      expect(scout.strings.removeAmpersand()).toBe(undefined);
      expect(scout.strings.removeAmpersand('')).toBe('');
      expect(scout.strings.removeAmpersand(' ')).toBe(' ');
      expect(scout.strings.removeAmpersand('Hello')).toBe('Hello');
      expect(scout.strings.removeAmpersand('Hello & Co')).toBe('Hello  Co');
      expect(scout.strings.removeAmpersand('&Menu')).toBe('Menu');
      expect(scout.strings.removeAmpersand('&')).toBe('');
      expect(scout.strings.removeAmpersand('&One &Two &Three&')).toBe('One Two Three');
      expect(scout.strings.removeAmpersand('You&&Me')).toBe('You&Me');
      expect(scout.strings.removeAmpersand('You&&&Me')).toBe('You&Me');
      expect(scout.strings.removeAmpersand('You&&&&Me')).toBe('You&&Me');
      expect(scout.strings.removeAmpersand('You&&&&&Me')).toBe('You&&Me');
    });

  });

  describe("hasText", function() {

    it("can check if string has text", function() {
      expect(scout.strings.hasText()).toBe(false);
      expect(scout.strings.hasText('')).toBe(false);
      expect(scout.strings.hasText(' ')).toBe(false);
      expect(scout.strings.hasText('Hello')).toBe(true);
      expect(scout.strings.hasText('       .      ')).toBe(true);
      expect(scout.strings.hasText('       \n      ')).toBe(false);
      expect(scout.strings.hasText('       \n      \nn')).toBe(true);
    });

  });

  describe("repeat", function() {

    it("can repeat strings", function() {
      expect(scout.strings.repeat()).toBe(undefined);
      expect(scout.strings.repeat('')).toBe('');
      expect(scout.strings.repeat('X')).toBe('');
      expect(scout.strings.repeat('X', 1)).toBe('X');
      expect(scout.strings.repeat('X', 7)).toBe('XXXXXXX');
      expect(scout.strings.repeat('X', -7)).toBe('');
    });

  });

  describe("padZeroLeft", function() {

    it("can pad strings with 0", function() {
      expect(scout.strings.padZeroLeft()).toBe(undefined);
      expect(scout.strings.padZeroLeft('')).toBe('');
      expect(scout.strings.padZeroLeft('X')).toBe('X');
      expect(scout.strings.padZeroLeft('X', 1)).toBe('X');
      expect(scout.strings.padZeroLeft('X', 7)).toBe('000000X');
      expect(scout.strings.padZeroLeft('X', -7)).toBe('X');
    });

  });

  describe("nvl", function() {

    it("can return alternative string", function() {
      expect(scout.strings.nvl()).toBe(undefined);
      expect(scout.strings.nvl('X')).toBe('X');
      expect(scout.strings.nvl(undefined)).toBe(undefined);
      expect(scout.strings.nvl(undefined, undefined)).toBe(undefined);
      expect(scout.strings.nvl(undefined, null)).toBe(null);
      expect(scout.strings.nvl(undefined, '')).toBe('');
      expect(scout.strings.nvl(undefined, 'X')).toBe('X');
      expect(scout.strings.nvl(null, 'X')).toBe('X');
      expect(scout.strings.nvl(null, '')).toBe('');
      expect(scout.strings.nvl(null, undefined)).toBe(undefined);
      expect(scout.strings.nvl(null, null)).toBe(null);
      expect(scout.strings.nvl(null)).toBe(undefined);
    });

  });

  describe("startsWith", function() {

    it("can check if a string starts with another", function() {
      expect(scout.strings.startsWith('abc', 'a')).toBe(true);
      expect(scout.strings.startsWith('abc', 'b')).toBe(false);
      expect(scout.strings.startsWith('äabc', 'ä')).toBe(true);
      expect(scout.strings.startsWith('äabc', 'Ä')).toBe(false);
      expect(scout.strings.startsWith('abc', '')).toBe(true);
      expect(scout.strings.startsWith('', '')).toBe(true);
      expect(scout.strings.startsWith()).toBe(false);
      expect(scout.strings.startsWith(undefined, 'hello')).toBe(false);
      expect(scout.strings.startsWith('Der Himmel ist blau!', 'Der')).toBe(true);
      expect(scout.strings.startsWith('¿Vive usted en España?', 'Vive')).toBe(false);
      expect(scout.strings.startsWith('¿Vive usted en España?', '¿Vive')).toBe(true);
    });

  });

  describe("endsWith", function() {

    it("can check if a string ends with another", function() {
      expect(scout.strings.endsWith('abc', 'c')).toBe(true);
      expect(scout.strings.endsWith('abc', 'b')).toBe(false);
      expect(scout.strings.endsWith('abcä', 'ä')).toBe(true);
      expect(scout.strings.endsWith('abcä', 'Ä')).toBe(false);
      expect(scout.strings.endsWith('abc', '')).toBe(true);
      expect(scout.strings.endsWith('', '')).toBe(true);
      expect(scout.strings.endsWith()).toBe(false);
      expect(scout.strings.endsWith(undefined, 'hello')).toBe(false);
      expect(scout.strings.endsWith('Der Himmel ist blau!', 'blau')).toBe(false);
      expect(scout.strings.endsWith('Der Himmel ist blau!', 'blau!')).toBe(true);
    });

  });

});
