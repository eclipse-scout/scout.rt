import Point from './Point';
import Dimension from './Dimension';

export default class Rectangle {
    
    constructor(vararg, y, width, height) {
        if (vararg instanceof Rectangle) {
            this.x = vararg.x;
            this.y = vararg.y;
            this.width = vararg.width;
            this.height = vararg.height;
        } else {
            this.x = vararg || 0;
            this.y = y || 0;
            this.width = width || 0;
            this.height = height || 0;
        }
    }
    
    toString() {
        return 'Rectangle[x=' + this.x + ' y=' + this.y + ' width=' + this.width + ' height=' + this.height + ']';
    };

    equals(o) {
        if (!o) {
            return false;
        }
        return (this.x === o.x && this.y === o.y && this.width === o.width && this.height === o.height);
    };

    clone(o) {
        return new Rectangle(this.x, this.y, this.width, this.height);
    };

    center() {
        return new Point(this.x + this.width / 2, this.y + this.height / 2);
    };

    contains(x, y) {
        return y >= this.y && y < this.y + this.height && x >= this.x && x < this.x + this.width;
    };

    subtract(insets) {
        return new Rectangle(
            this.x + insets.left,
            this.y + insets.top,
            this.width - insets.horizontal(),
            this.height - insets.vertical());
    };

    /**
     * Moves the rectangle the given distance.
     * <p>
     * @param dx the distance to move the rectangle along the x axis.
     * @param dy the distance to move the rectangle along the y axis.
     */
    translate(dx, dy) {
        return new Rectangle(
            this.x + dx,
            this.y + dy,
            this.width,
            this.height);
    };

    point() {
        return new Point(this.x, this.y);
    };

    dimension() {
        return new Dimension(this.width, this.height);
    };

    union(r) {
        var tx2 = this.width;
        var ty2 = this.height;
        if (tx2 < 0 || ty2 < 0) {
            // This rectangle has negative dimensions...
            // If r has non-negative dimensions then it is the answer.
            // If r is non-existant (has a negative dimension), then both
            // are non-existant and we can return any non-existant rectangle
            // as an answer.  Thus, returning r meets that criterion.
            // Either way, r is our answer.
            return new Rectangle(r.x, r.y, r.width, r.height);
        }
        var rx2 = r.width;
        var ry2 = r.height;
        if (rx2 < 0 || ry2 < 0) {
            return new Rectangle(this.x, this.y, this.width, this.height);
        }
        var tx1 = this.x;
        var ty1 = this.y;
        tx2 += tx1;
        ty2 += ty1;
        var rx1 = r.x;
        var ry1 = r.y;
        rx2 += rx1;
        ry2 += ry1;
        if (tx1 > rx1) {
            tx1 = rx1;
        }
        if (ty1 > ry1) {
            ty1 = ry1;
        }
        if (tx2 < rx2) {
            tx2 = rx2;
        }
        if (ty2 < ry2) {
            ty2 = ry2;
        }
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 will never underflow since both original rectangles
        // were already proven to be non-empty
        // they might overflow, though...
        if (tx2 > Number.MAX_VALUE) {
            tx2 = Number.MAX_VALUE;
        }
        if (ty2 > Number.MAX_VALUE) {
            ty2 = Number.MAX_VALUE;
        }
        return new Rectangle(tx1, ty1, tx2, ty2);
    };

    floor() {
        return new Rectangle(Math.floor(this.x), Math.floor(this.y), Math.floor(this.width), Math.floor(this.height));
    };

    ceil() {
        return new Rectangle(Math.ceil(this.x), Math.ceil(this.y), Math.ceil(this.width), Math.ceil(this.height));
    };
}