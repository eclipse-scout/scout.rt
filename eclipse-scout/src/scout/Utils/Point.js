export default class Point {
    constructor(vararg, y) {
        if (vararg instanceof Point) {
            this.x = vararg.x;
            this.y = vararg.y;
        } else {
            this.x = vararg || 0;
            this.y = y || 0;
        }
    }

    toString() {
        return 'Point[x=' + this.x + ' y=' + this.y + ']';
    };

    equals(o) {
        if (!o) {
            return false;
        }
        return (this.x === o.x && this.y === o.y);
    };

    clone(o) {
        return new Point(this.x, this.y);
    };

    add(point) {
        return new Point(this.x + point.x, this.y + point.y);
    };

    subtract(point) {
        return new Point(this.x - point.x, this.y - point.y);
    };

    floor() {
        return new Point(Math.floor(this.x), Math.floor(this.y));
    };

    ceil() {
        return new Point(Math.ceil(this.x), Math.ceil(this.y));
    };
}