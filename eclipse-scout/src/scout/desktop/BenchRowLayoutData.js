import FlexboxLayoutData from '../layout/FlexboxLayoutData';

export default class BenchRowLayoutData extends FlexboxLayoutData {

  constructor(model) {

    super(model);
    this.rows = [null, null, null];
    this._ensureRows();
  }

  getRows() {
    return this.rows;
  };

  _ensureRows() {
    this.rows = this.rows.map(function(row, i) {
      return new FlexboxLayoutData(row).withOrder(i * 2);
    });
  };

  updateVisibilities(rows) {
    rows.forEach(function(row, index) {
      this.rows[index].visible = row.rendered;

    }.bind(this));
  };

}
