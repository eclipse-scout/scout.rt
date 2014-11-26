scout.numbers =  {

    /**
     * Converts the given decimal number to base-62 (i.e. the same value, but
     * represented by [a-zA-Z0-9] instead of only [0-9].
     */
    toBase62: function(number) {
      if (typeof number === 'undefined') {
        return undefined;
      }
      var symbols = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'.split('');
      var base = 62;
      var s = '';
      var n;
      while (number >= 1) {
        n = Math.floor(number / base);
        s = symbols[(number - (base * n))] + s;
        number = n;
      }
      return s;
    },

    /**
     * Returns a random sequence of characters out of the set [a-zA-Z0-9] with the
     * given length. The default length is 8.
     */
    randomId: function(length) {
      if (typeof length === 'undefined') {
        length = 8;
      }
      var charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
      var s = '';
      for (var i = 0; i < length; i++) {
        s += charset[Math.floor(Math.random() * charset.length)];
      }
      return s;
    }

};
