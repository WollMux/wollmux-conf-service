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
 @class
*/
var ConfService = function(j_val) {

  var j_confService = j_val;
  var that = this;

  /**

   @public
   @param arg0 {function} 
   */
  this.getConf = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_confService["getConf(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        arg0(ar.result(), null);
      } else {
        arg0(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param arg0 {function} 
   */
  this.getJSON = function(arg0) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_confService["getJSON(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        arg0(utils.convReturnJson(ar.result()), null);
      } else {
        arg0(null, ar.cause());
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