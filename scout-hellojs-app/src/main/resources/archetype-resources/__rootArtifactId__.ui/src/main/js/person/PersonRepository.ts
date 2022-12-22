import {App, Person, PersonRestriction, Repository} from '../index';
import {App as ScoutApp, scout} from '@eclipse-scout/core';

export class PersonRepository extends Repository {
  targetUrl: string;

  constructor() {
    super();

    let app = ScoutApp.get() as App;
    this.targetUrl = app.apiUrl + 'persons/';
  }

  /**
   * Loads a single person
   * @param personId The id of the person to fetch. Must not be null.
   */
  load(personId: string): JQuery.Promise<Person> {
    return this.getJson(this.targetUrl + personId)
      .then(items => this._first(items));
  }

  /**
   * get all persons
   * @param restrictions Restrictions which persons to fetch
   * @return The persons matching the restrictions
   */
  list(restrictions: PersonRestriction): JQuery.Promise<Person[]> {
    return this.postJson(this.targetUrl + 'list', JSON.stringify(restrictions));
  }

  /**
   * Update existing Person
   * @param person The person to update
   * @returns The updated person
   */
  store(person: Person): JQuery.Promise<Person> {
    return this.putJson(this.targetUrl + person.personId, JSON.stringify(person))
      .then(items => this._first(items) as Person)
      .then(person => this._triggerPersonChanged(person));
  }

  /**
   * Delete person
   * @param personId The id of the person to delete.
   */
  remove(personId: string): JQuery.Promise<void> {
    return this.removeJson(this.targetUrl + personId)
      .then(() => {
        this._triggerPersonChanged();
      });
  }

  /**
   * Create new person
   * @param person The person to create
   * @returns the created person
   */
  create(person: Person): JQuery.Promise<Person> {
    return this.postJson(this.targetUrl, JSON.stringify(person))
      .then(person => this._triggerPersonChanged(person));
  }

  protected _triggerPersonChanged(person?: Person): Person {
    scout.getSession().desktop.dataChange({
      dataType: Person.EVENT_TYPE,
      data: person
    });
    return person;
  }

  static get(): PersonRepository {
    if (!personRepo) {
      personRepo = scout.create(PersonRepository);
    }
    return personRepo;
  }
}

let personRepo: PersonRepository;
