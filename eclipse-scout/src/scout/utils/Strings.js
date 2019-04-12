import Scout from '../Scout';

let _encodeElement = null;

export default class Strings {

    static encode(string) {
        if (!string) {
            return string;
        }
        var elem = _encodeElement;
        if (!elem) {
            elem = window.document.createElement('div');
            // cache it to prevent creating an element every time
            _encodeElement = elem;
        }
        elem.textContent = string;
        return elem.innerHTML;
    }

    static hasText(text) {
        if (text === undefined || text === null) {
            return false;
        }
        text = Strings.asString(text);
        if (typeof text !== 'string' || text.length === 0) {
            return false;
        }
        return !/^\s*$/.test(text);
    }

    static box(prefix, string, suffix) {
        prefix = Strings.asString(prefix);
        string = Strings.asString(string);
        suffix = Strings.asString(suffix);
        var s = '';
        if (Strings.hasText(string)) {
            if (prefix) {
                s += prefix;
            }
            s += string;
            if (suffix) {
                s += suffix;
            }
        }
        return s;
    }

    static startsWith(fullString, startString) {
        if (fullString === undefined || fullString === null || startString === undefined || startString === null) {
            return false;
        }
        fullString = Strings.asString(fullString);
        startString = Strings.asString(startString);
        if (startString.length === 0) {
            return true; // every string starts with the empty string
        }
        if (fullString.length === 0) {
            return false; // empty string cannot start with non-empty string
        }
        return (fullString.substr(0, startString.length) === startString);
    }

    static quote(string) {
        if (string === undefined || string === null) {
            return string;
        }
        string = Strings.asString(string);
        // see 'escapeRegExp()' from https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions#Using_special_characters
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // $& = last match
    }

    static count(string, separator) {
        if (!string || separator === undefined || separator === null) {
            return 0;
        }
        string = Strings.asString(string);
        separator = Strings.asString(separator);
        return string.split(separator).length - 1;
    }

    static padZeroLeft(string, padding) {
        string = Strings.asString(string);
        if (string === undefined || string === null || typeof padding !== 'number' || padding < 1 || (string + '').length >= padding) {
            return string;
        }
        var z = Strings.repeat('0', padding) + string;
        return z.slice(-padding);
    }

    static repeat(pattern, count) {
        if (pattern === undefined || pattern === null) {
            return pattern;
        }
        if (typeof count !== 'number' || count < 1) {
            return '';
        }
        var result = '';
        for (var i = 0; i < count; i++) {
            result += pattern;
        }
        return result;
    }

    static asString(input) {
        if (input === undefined || input === null) {
            return input;
        }
        if (typeof input === 'string' || input instanceof String) {
            return input;
        }
        return String(input);
    }

    static join(separator, vararg) {
        var stringsToJoin;
        if (vararg && Array.isArray(vararg)) {
            stringsToJoin = vararg;
        } else {
            stringsToJoin = Scout.argumentsToArray(arguments).slice(1);
        }
        separator = Strings.asString(separator);
        var s = '';
        for (var i = 0; i < stringsToJoin.length; i++) {
            var arg = Strings.asString(stringsToJoin[i]);
            if (arg) {
                if (s && separator) {
                    s += separator;
                }
                s += arg;
            }
        }
        return s;
    }

    static contains(string, searchFor) {
        if (!string) {
            return false;
        }
        return string.indexOf(searchFor) > -1;
    }


    static toUpperCaseFirstLetter(string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
