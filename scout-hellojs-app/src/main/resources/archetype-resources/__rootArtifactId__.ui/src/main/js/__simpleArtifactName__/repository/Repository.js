#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/**
 * Subclasses of Repository must set the resourceType property.
 *
 * @abstract
 * @class
 */
${simpleArtifactName}.Repository = function() {
  this.entityType = null;
};

${simpleArtifactName}.Repository.prototype.getJson = function(url, opts) {
  return ${simpleArtifactName}.Repository.map(scout.ajax.getJson(url, this._ensureConverter(opts)));
};

${simpleArtifactName}.Repository.prototype.postJson = function(url, data, opts) {
  return ${simpleArtifactName}.Repository.map(scout.ajax.postJson(url, data, this._ensureConverter(opts)));
};

${simpleArtifactName}.Repository.prototype.removeJson = function(url, opts) {
  return ${simpleArtifactName}.Repository.map(scout.ajax.removeJson(url, this._ensureConverter(opts)));
};

${simpleArtifactName}.Repository.prototype.putJson = function(url, data, opts) {
  return ${simpleArtifactName}.Repository.map(scout.ajax.putJson(url, data, this._ensureConverter(opts)));
};

${simpleArtifactName}.Repository.prototype._ensureConverter = function(opts) {
  return ${symbol_dollar}.extend({}, {
    converters: {
      "text json": this._mapJson
    }
  }, opts);
};

${simpleArtifactName}.Repository.prototype._mapJson = function(data) {
  return JSON.parse(data, function(key, value) {
    if (key === '_type') {
      this.objectType = scout.app.appPrefix + value;
    }
    return value;
  });
};

${simpleArtifactName}.Repository.prototype._first = function(items) {
  return items[0];
};

// ---- Static Objects ---- //

${simpleArtifactName}.repositories = {};

/**
 * @static
 * @param {string} objectName
 * @returns {${simpleArtifactName}.Repository}
 */
${simpleArtifactName}.Repository.register = function(objectName) {
  var repository = scout.create(objectName);
  ${simpleArtifactName}.repositories[repository.entityType] = repository;
  return repository;
};

${simpleArtifactName}.Repository.map = function(promise) {
  return promise
    .then(function(response) {
      if (!response || !response.items) {
        return response;
      }

      return response.items
        .map(function(item) {
          return scout.create(item, {
            ensureUniqueId: false
          });
        });
    });
};

/**
 * @static
 * @param {string} resourceType
 * @returns {${simpleArtifactName}.Repository} a repository for the given resourceType
 */
${simpleArtifactName}.Repository.get = function(resourceType) {
  var repository = ${simpleArtifactName}.repositories[resourceType];
  if (!repository) {
    throw new Error('no repository found for resourceType ' + resourceType);
  }
  return repository;
};
