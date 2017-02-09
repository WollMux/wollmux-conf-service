/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module conf-service-js/conf_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JConfService = de.muenchen.wollmux.conf.service.ConfService;

/**
 Interface des Konfigurationsservice. Wird in conf-service implementiert
 und als Service registriert.
 
 @class
*/
var ConfService = function(j_val) {

  var j_confService = j_val;
  var that = this;

  /**
   Liefert einen String im WollMux-Conf-Format.

   @public
   @param file {string} 
   @param resultHandler {function} 
   */
  this.getFile = function(file, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_confService["getFile(java.lang.String,io.vertx.core.Handler)"](file, function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Liefert einen String im JSON-Format.

   @public
   @param file {string} 
   @param resultHandler {function} 
   */
  this.getJSON = function(file, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_confService["getJSON(java.lang.String,io.vertx.core.Handler)"](file, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_confService;
};

// We export the Constructor function
module.exports = ConfService;