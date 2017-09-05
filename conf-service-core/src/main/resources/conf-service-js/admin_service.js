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

/** @module conf-service-js/admin_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JAdminService = de.muenchen.wollmux.conf.service.AdminService;

/**
 Interface des Administrationssservices. Wird in admin-service implementiert
 und als Service registriert.

 @class
*/
var AdminService = function(j_val) {

  var j_adminService = j_val;
  var that = this;

  /**
   Liefert eine Datei der Konfiguration.

   @public
   @param file {string} 
   @param resultHandler {function} 
   */
  this.getFile = function(file, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_adminService["getFile(java.lang.String,io.vertx.core.Handler)"](file, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Schreibt einen Datei der Konfiguration auf die Platte.

   @public
   @param file {string} Der Dateiname relativ zur PATH Variable. 
   @param content {string} Der Dateiinhalt. 
   @param resultHandler {function} Der Actionhandler, der das Ergebnis entgegen nimmt. 
   */
  this.writeFile = function(file, content, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'function') {
      j_adminService["writeFile(java.lang.String,java.lang.String,io.vertx.core.Handler)"](file, content, function(ar) {
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
  this._jdel = j_adminService;
};

// We export the Constructor function
module.exports = AdminService;