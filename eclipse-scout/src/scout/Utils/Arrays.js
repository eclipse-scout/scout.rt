import Strings from './Strings';

export default class Arrays {
    static ensure(array) {
        if (array === undefined || array === null) {
            return [];
        }
        if (!Array.isArray(array)) {
            return [array];
        }
        return array;
    }

    static diff(arr1, arr2) {
        var diff = arr1.slice();
        Arrays.removeAll(diff, arr2);
        return diff;
    }

    static insert(arr, element, index) {
        Arrays.insertAll(arr, [element], index);
    }

    static insertAll(arr, elements, index) {
        elements = Arrays.ensure(elements);
        Array.prototype.splice.apply(arr, [index, 0].concat(elements));
    }

    static pushAll(arr, arr2) {
        arr.push.apply(arr, arr2);
    }

    static removeAll(arr, elements) {
        var modified = false;
        if (!elements || elements.length === 0) {
            return false;
        }
        for (var i = arr.length - 1; i >= 0; i--) {
            if (elements.indexOf(arr[i]) > -1) {
                arr.splice(i, 1);
                modified = true;
            }
        }
        return modified;
    }

    static find(arr, predicate, thisArg) {
        var index = Arrays.findIndex(arr, predicate, thisArg);
        if (index === -1) {
            return null;
        }
        return arr[index];
    }

    static findIndex(arr, predicate, thisArg) {
        if (!arr || !predicate) {
            return -1;
        }
        for (var i = 0; i < arr.length; i++) {
            if (predicate.call(thisArg, arr[i], i, arr)) {
                return i;
            }
        }
        return -1;
    }

    static last(arr) {
        if (Array.isArray(arr)) {
            return arr[arr.length - 1];
        }
        return arr;
    }

    static empty(arr) {
        if (Array.isArray(arr)) {
            return arr.length === 0;
        }
        return true;
    }

    static remove(arr, element) {
        if (arr) {
            var index = arr.indexOf(element);
            if (index !== -1) {
                arr.splice(index, 1);
                return true;
            }
        }
        return false;
    }

    static format(arr, delimiter, encodeHtml) {
        if (!arr || arr.length === 0) {
            return '';
        }

        var output = '';
        for (var i = 0; i < arr.length; i++) {
            var element = arr[i];
            if (delimiter && i > 0 && i < arr.length) {
                output += delimiter;
            }
            if (encodeHtml) {
                element = Strings.encode(element);
            }
            output += element;
        }
        return output;
    }
}
export {Arrays}