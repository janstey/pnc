/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function () {
  'use strict';

  angular.module('pnc.build-configs').component('pncCreateBuildConfigWizard', {
    templateUrl: 'build-configs/directives/pnc-create-build-config-wizard/pnc-create-build-config-wizard.html',
    controller: ['$log', '$scope', '$timeout', 'eventTypes', 'RepositoryConfiguration', 'BuildConfiguration', Controller],
    bindings: {
      initialValues: '<',
      project: '<',
      resolve: '<',
      onClose: '&close'
    }
  });

  function Controller($log, $scope, $timeout, eventTypes, RepositoryConfiguration, BuildConfiguration) {
    var $ctrl = this,
        emptyWizardData = {
          general: {},
          buildParameters: {},
          dependencies: {},
          repoConfig: {}
        };

    // -- Controller API --
    
    // If we don't init these here the FormController isn't available in the template for "next-enabled" property.
    $ctrl.generalForm = {};
    $ctrl.repoForm = {};

    $ctrl.createStatusMessages = [];
    $ctrl.createComplete = false;
    $ctrl.createError = false;
    $ctrl.reviewPageShown = false;

    $ctrl.onShowReviewSummary = onShowReviewSummary;
    $ctrl.create = create;

    // --------------------

    $ctrl.$onInit = function () {
      $ctrl.wizardData = angular.extend({}, emptyWizardData, $ctrl.initialValues);
      $ctrl.wizardData.project = $ctrl.resolve.project;
    };

    function onShowReviewSummary() {
      $ctrl.reviewPageShown = true;
      $timeout(function () {
        $ctrl.reviewPageShown = false;  // done so the next time the page is shown it updates
      });
    }

    function parseBuildConfig(wizardData) {
      var bc = angular.copy(wizardData.general);
      bc.genericParameters = angular.copy(wizardData.buildParameters);
      bc.dependencyIds = wizardData.dependencies.map(function (d) { return d.id; });
      bc.scmRevision = wizardData.repoConfig.revision;
      bc.project = $ctrl.wizardData.project;
      bc.buildConfigurationSetIds = [];
      return bc;
    }

    function create() {
      var bc = new BuildConfiguration(parseBuildConfig($ctrl.wizardData));

      if ($ctrl.wizardData.repoConfig.useExistingRepoConfig) {
        bc.repositoryConfiguration = { id: $ctrl.wizardData.repoConfig.repoConfig.id };
        bc.$save().finally(function () {
          $ctrl.createComplete = true;
        });
      } else {
        $scope.$on(eventTypes.RC_BPM_NOTIFICATION, function (event, payload) {

          switch (payload.eventType) {
            case 'RC_REPO_CREATION_SUCCESS':
              $ctrl.createStatusMessages.push(payload.data.message);
              break;
            case 'RC_REPO_CLONE_SUCCESS':
              $ctrl.createStatusMessages.push('Repository successfully cloned.');
              break;
            case 'RC_CREATION_SUCCESS':
              $ctrl.createStatusMessages.push('Build Config successfully created.');
              $ctrl.createdBuildConfigId = payload.buildConfigurationId;
              $ctrl.createdRepoConfigId = payload.repositoryId;              
              $ctrl.createComplete = true;
              break;
            case 'RC_CREATION_ERROR':
              $ctrl.createComplete = true;
              $ctrl.createError = true;
              break;
          }

        });
        RepositoryConfiguration.autoCreateRepoConfig({
          url: $ctrl.wizardData.repoConfig.scmUrl,
          preBuildSyncEnabled: $ctrl.wizardData.repoConfig.preBuildSyncEnabled,
          buildConfiguration: bc
        }).catch(function () {
          $ctrl.createComplete = true;
          $ctrl.createError = true;
        });
      }
    }
  }
})();