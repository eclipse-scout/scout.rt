import {Repository} from '../index';
import {scout, App} from '@eclipse-scout/core';

export default class PersonRepository extends Repository {

  constructor() {
    super();
    this.entityType = 'Person';
    this.targetUrl = App.get().apiUrl + 'persons/';
  }


  /**
   * Loads a single person
   * @param personId The id of the person to fetch. Must not be null.
   * @returns Person
   */
  load(personId) {
    return this.getJson(this.targetUrl + personId)
      .then(this._first.bind(this));
  }

  /**
   * get all persons
   * @param restrictions list restrictions object of type PersonRestriction
   * @returns promise with person array
   */
  list(restrictions) {
    return this.postJson(this.targetUrl + 'list', JSON.stringify(restrictions));
  }

  /**
   * Update existing Person
   * @param person The person to update
   * @returns The updated person
   */
  store(person) {
    return this.putJson(this.targetUrl + person.personId, JSON.stringify(person))
      .then(this._first.bind(this));
  }

  /**
   * Delete person
   * @param personId The id of the person to delete.
   * @returns nothing
   */
  remove(personId) {
    return this.removeJson(this.targetUrl + personId);
  }

  /**
   * Create new person
   * @param person The person to create
   * @returns the created person
   */
  create(person) {
    return this.postJson(this.targetUrl, JSON.stringify(person));
  }
}

App.addListener('bootstrap', function() {
  ${simpleArtifactName}
.
  persons = Repository.register('${simpleArtifactName}.PersonRepository');
});
