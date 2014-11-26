describe("scout.strings", function() {

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
