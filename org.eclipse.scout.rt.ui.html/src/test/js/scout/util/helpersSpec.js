describe("scout.helpers", function() {

  describe("nvl", function() {

    it("can return alternative value", function() {
      expect(scout.helpers.nvl()).toBe(undefined);
      expect(scout.helpers.nvl('X')).toBe('X');
      expect(scout.helpers.nvl('X', 'Y')).toBe('X');
      expect(scout.helpers.nvl(undefined)).toBe(undefined);
      expect(scout.helpers.nvl(undefined, undefined)).toBe(undefined);
      expect(scout.helpers.nvl(undefined, null)).toBe(null);
      expect(scout.helpers.nvl(undefined, '')).toBe('');
      expect(scout.helpers.nvl(undefined, 'X')).toBe('X');
      expect(scout.helpers.nvl(null, 'X')).toBe('X');
      expect(scout.helpers.nvl(null, '')).toBe('');
      expect(scout.helpers.nvl(null, undefined)).toBe(undefined);
      expect(scout.helpers.nvl(null, null)).toBe(null);
      expect(scout.helpers.nvl(null)).toBe(undefined);
      expect(scout.helpers.nvl(0, '123')).toBe(0);
      expect(scout.helpers.nvl(1, '123')).toBe(1);
      expect(scout.helpers.nvl(undefined, '123')).toBe('123');
      expect(scout.helpers.nvl(undefined, 123)).toBe(123);
      expect(scout.helpers.nvl(0.000000000000000000000001, -1)).toBe(0.000000000000000000000001);
      expect(scout.helpers.nvl({}, {x: 2})).toEqual({});
      expect(scout.helpers.nvl({y: undefined}, {x: 2})).toEqual({y: undefined});
      expect(scout.helpers.nvl(null, {x: 2})).toEqual({x: 2});
    });

  });

});
