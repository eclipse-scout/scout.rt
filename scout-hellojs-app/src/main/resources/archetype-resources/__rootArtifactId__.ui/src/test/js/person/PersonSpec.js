import {scout} from '@eclipse-scout/core';

describe('Person', function() {

  it('stores values correctly', function() {
    var person = scout.create('${simpleArtifactName}.Person');
    var values = ['first', 'last', 'id'];
    person.setFirstName(values[0]);
    person.setLastName(values[1]);
    person.setPersonId(values[2]);

    expect(person.firstName).toBe(values[0]);
    expect(person.lastName).toBe(values[1]);
    expect(person.personId).toBe(values[2]);
    expect(person.resourceType).toBe('Person');
  });

  it('initializes correctly from model', function() {
    var values = ['first', 'last', 'id'];
    var model = {
      personId: values[2],
      firstName: values[0],
      lastName: values[1]
    };
    var person = scout.create('${simpleArtifactName}.Person', model);

    expect(person.firstName).toBe(values[0]);
    expect(person.lastName).toBe(values[1]);
    expect(person.personId).toBe(values[2]);
    expect(person.resourceType).toBe('Person');
  });

});
