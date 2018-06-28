${rootArtifactId}.PersonRepository = function() {
  ${rootArtifactId}.PersonRepository.parent.call(this);
  this.entityType = 'Person';
  this.targetUrl = scout.app.apiUrl + 'persons/';
};
scout.inherits(${rootArtifactId}.PersonRepository, ${rootArtifactId}.Repository);

/**
 * Loads a single person
 * @param personId The id of the person to fetch. Must not be null.
 * @returns Person
 */
${rootArtifactId}.PersonRepository.prototype.load = function(personId) {
  return this.getJson(this.targetUrl + personId)
    .then(this._first.bind(this));
};

/**
 * get all persons
 * @returns promise with person array
 */
${rootArtifactId}.PersonRepository.prototype.list = function() {
  return this.getJson(this.targetUrl);
};

/**
 * Update existing Person
 * @param person The person to update
 * @returns The updated person
 */
${rootArtifactId}.PersonRepository.prototype.store = function(person) {
  return this.putJson(this.targetUrl + person.personId, JSON.stringify(person))
    .then(this._first.bind(this));
};

/**
 * Delete person
 * @param personId The id of the person to delete.
 * @returns nothing
 */
${rootArtifactId}.PersonRepository.prototype.remove = function(personId) {
  return this.removeJson(this.targetUrl + personId);
};

/**
 * Create new person
 * @param person The person to create
 * @returns the created person
 */
${rootArtifactId}.PersonRepository.prototype.create = function(person) {
  return this.postJson(this.targetUrl, JSON.stringify(person));
};

scout.addAppListener('bootstrap', function() {
  ${rootArtifactId}.persons = ${rootArtifactId}.Repository.register('${rootArtifactId}.PersonRepository');
});
