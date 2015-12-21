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
  }

};
