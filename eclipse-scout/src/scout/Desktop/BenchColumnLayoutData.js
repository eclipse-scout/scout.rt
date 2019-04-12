import * as $ from 'jquery';
import BenchRowLayoutData from './BenchRowLayoutData';

export default class BenchColumnLayoutData {

    constructor(model) {
        this.columns = [null, null, null];
        $.extend(this, model);

        this._ensureColumns();
    }

    _ensureColumns() {
        this.columns = this.columns.map(function(col, i) {
            return new BenchRowLayoutData(col).withOrder(i*2);
        });
    };

    getColumns() {
        return this.columns;
    };



    static ensure(layoutData) {
        if (!layoutData) {
            layoutData = new BenchColumnLayoutData();
            return layoutData;
        }
        if (layoutData instanceof BenchColumnLayoutData) {
            return layoutData;
        }
        return new BenchColumnLayoutData(layoutData);
    };
}

