export function constructorName(constructor) {
  if (constructor.name) {
    console.log('using constructor name: ' + constructor.name);
    return constructor.name;
  }
  return extractConstructorName(constructor.toString());
}

export function extractConstructorName(constructorStr) {
  // Internet Explorer 11
  // matches "function ClassName("
  var name = constructorStr.match(/^function\s+(\S+)\s?\(/);
  if (name[1]) {
    return name[1];
  }
}

export default {constructorName, extractConstructorName }
