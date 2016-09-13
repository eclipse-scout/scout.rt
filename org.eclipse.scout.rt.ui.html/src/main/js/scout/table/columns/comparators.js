scout.comparators = {

  /**
   * Text comparator, used to compare strings with support for internationalization (i18n).
   * The collator object is only installed once.
   */
  TEXT: {
    collator: null,
    installed: false,
    install: function(session) {
      if (this.installed) {
        return !!this.collator;
      }

      // set static collator variable once
      if (scout.device.supportsInternationalization()) {
        this.collator = new window.Intl.Collator(session.locale.languageTag);
        $.log.info('(comparators.TEXT#install) Browser supports i18n - installed Intl.Collator, can sort in Browser');
      } else {
        $.log.info('(comparators.TEXT#install) Browser doesn\'t support i18n. Must sort on server');
      }

      this.installed = true;
      return !!this.collator;
    },
    compare: function(valueA, valueB) {
      if (!this.collator) {
        // Fallback for browsers that don't support internationalization. This is only necessary
        // for callers that call this method without check for internationalization support
        // first (e.g. TableMatrix).
        valueA = scout.nvl(valueA, '');
        valueB = scout.nvl(valueB, '');
        return (valueA < valueB ? -1 : ((valueA > valueB) ? 1 : 0));
      }
      // We don't check the installed flag here. It's a program error when we come here
      // and the collator is not set. Either we forgot to call install() or we've called
      // install but the browser does not support i18n.
      return this.collator.compare(valueA, valueB);
    }
  },

  /**
   * Numeric comparator, used to compare numeric values. Used for numbers, dates, etc.
   */
  NUMERIC: {
    install: function(session) {
      // NOP
      return true;
    },
    compare: function(valueA, valueB) {
      if (valueA < valueB) {
        return -1;
      } else if (valueA > valueB) {
        return 1;
      }
      return 0;
    }
  },

  /**
   * Alphanumeric comparator.
   */
  ALPHANUMERIC: {
    collator: null,
    installed: false,
    install: function(session) {
      scout.comparators.TEXT.install(session);
      this.collator = scout.comparators.TEXT.collator;
      return !!this.collator && scout.comparators.NUMERIC.install(session);
    },
    compare: function(valueA, valueB) {
      if (!valueA && !valueB) {
        return 0;
      }
      if (!valueA) {
        return -1;
      }
      if (!valueB) {
        return 1;
      }

      var pattern = '(([0-9]+)|([^0-9]+))';
      var regexp1 = new RegExp(pattern, 'g');
      var regexp2 = new RegExp(pattern, 'g');
      var found1 = regexp1.exec(valueA);
      var found2 = regexp2.exec(valueB);
      while (found1 && found2) {
        var n1 = parseInt(found1[1], 0);
        var n2 = parseInt(found2[1], 0);
        if (!isNaN(n1) && !isNaN(n2)) {
          var numericResult = scout.comparators.NUMERIC.compare(n1, n2);
          if (numericResult !== 0) {
            return numericResult;
          }
        } else {
          var textResult = scout.comparators.TEXT.compare(found1[1], found2[1]);
          if (textResult !== 0) {
            return textResult;
          }
        }
        found1 = regexp1.exec(valueA);
        found2 = regexp2.exec(valueB);
      }

      if (!found1 && !found2) {
        return 0;
      }
      if (!found1) {
        return -1;
      }
      return 1;
    }
  }

};
