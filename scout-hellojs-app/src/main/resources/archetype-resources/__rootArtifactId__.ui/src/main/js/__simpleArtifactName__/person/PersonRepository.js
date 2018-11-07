#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${simpleArtifactName}.PersonRepository = function() {
  ${simpleArtifactName}.PersonRepository.parent.call(this);
  this.entityType = 'Person';
  this.targetUrl = scout.app.apiUrl + 'persons/';
};
scout.inherits(${simpleArtifactName}.PersonRepository, ${simpleArtifactName}.Repository);

/**
 * Loads a single person
 * @param personId The id of the person to fetch. Must not be null.
 * @returns Person
 */
${simpleArtifactName}.PersonRepository.prototype.load = function(personId) {
  return this.getJson(this.targetUrl + personId)
    .then(this._first.bind(this));
};

/**
 * get all persons
 * @param restrictions list restrictions object of type PersonRestriction
 * @returns promise with person array
 */
${simpleArtifactName}.PersonRepository.prototype.list = function(restrictions) {
  return this.postJson(this.targetUrl + 'list', JSON.stringify(restrictions));
};

/**
 * Update existing Person
 * @param person The person to update
 * @returns The updated person
 */
${simpleArtifactName}.PersonRepository.prototype.store = function(person) {
  return this.putJson(this.targetUrl + person.personId, JSON.stringify(person))
    .then(this._first.bind(this));
};

/**
 * Delete person
 * @param personId The id of the person to delete.
 * @returns nothing
 */
${simpleArtifactName}.PersonRepository.prototype.remove = function(personId) {
  return this.removeJson(this.targetUrl + personId);
};

/**
 * Create new person
 * @param person The person to create
 * @returns the created person
 */
${simpleArtifactName}.PersonRepository.prototype.create = function(person) {
  return this.postJson(this.targetUrl, JSON.stringify(person));
};

scout.addAppListener('bootstrap', function() {
  ${simpleArtifactName}.persons = ${simpleArtifactName}.Repository.register('${simpleArtifactName}.PersonRepository');
});
