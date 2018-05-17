/**
 * HtmlEnvironment is used in place of org.eclipse.scout.rt.ui.swing.DefaultSwingEnvironment.
 */
scout.HtmlEnvironment = {
  // -------------------------------
  // The values for these properties are defined using CSS (sizes.less).
  // The following values are default values in case the CSS values are not available.
  // -------------------------------
  formRowHeight: 30,
  formRowGap: 10,
  formColumnWidth: 420,
  formColumnGap: 32, // 40 pixel actual form gap - fieldMandatoryIndicatorWidth
  smallColumnGap: 4,
  fieldLabelWidth: 140,
  fieldMandatoryIndicatorWidth: 8,
  fieldStatusWidth: 20,

  init: function() {
    this.formRowHeight = scout.styles.getSize('html-env-logical-grid-row', 'height', 'height', this.formRowHeight);
    this.formRowGap = scout.styles.getSize('html-env-logical-grid-row', 'margin-bottom', 'marginBottom', this.formRowGap);
    this.formColumnWidth = scout.styles.getSize('html-env-logical-grid-column', 'width', 'width', this.formColumnWidth);
    this.formColumnGap = scout.styles.getSize('html-env-logical-grid-column', 'margin-right', 'marginRight', this.formColumnGap);
    this.smallColumnGap = scout.styles.getSize('html-env-logical-grid-column', 'margin-left', 'marginLeft', this.smallColumnGap);
    this.fieldLabelWidth = scout.styles.getSize('html-env-field-label', 'width', 'width', this.fieldLabelWidth);
    this.fieldMandatoryIndicatorWidth = scout.styles.getSize('html-env-field-mandatory-indicator', 'width', 'width', this.fieldMandatoryIndicatorWidth);
    this.fieldStatusWidth = scout.styles.getSize('html-env-field-status', 'width', 'width', this.fieldStatusWidth);
  }
};

scout.addAppListener('prepare', function() {
  scout.HtmlEnvironment.init();
});
