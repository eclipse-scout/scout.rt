describe("scout.texts", function() {

  describe("get", function() {

    it("check if correct text is returned", function() {
      expect(scout.texts.get('noOptions')).toBe('Keine Übereinstimmung');
    });

    it("check if arguments are replaced in text", function() {
      expect(scout.texts.get('options', 3)).toBe('3 Optionen');
    });

  });

});
