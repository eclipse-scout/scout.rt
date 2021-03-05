import {scout} from '@eclipse-scout/core';

describe('Person', () => {

  it('stores values correctly', () => {
    let person = scout.create('${simpleArtifactName}.Person');
    let values = ['first', 'last', 'id'];
    person.setFirstName(values[0]);
    person.setLastName(values[1]);
    person.setPersonId(values[2]);

    expect(person.firstName).toBe(values[0]);
    expect(person.lastName).toBe(values[1]);
    expect(person.personId).toBe(values[2]);
    expect(person.resourceType).toBe('Person');
  });

  it('initializes correctly from model', () => {
    let values = ['first', 'last', 'id'];
    let model = {
      personId: values[2],
      firstName: values[0],
      lastName: values[1]
    };
    let person = scout.create('${simpleArtifactName}.Person', model);

    expect(person.firstName).toBe(values[0]);
    expect(person.lastName).toBe(values[1]);
    expect(person.personId).toBe(values[2]);
    expect(person.resourceType).toBe('Person');
  });

});
