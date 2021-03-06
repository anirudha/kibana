/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This is will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

Cypress.Commands.add('stubSecurityApi', function (dataFileName) {
  cy.on('window:before:load', (win) => {
    win.fetch = null;
  });
  cy.server();
  cy.fixture(dataFileName).as(`${dataFileName}JSON`);
  cy.route('POST', 'api/solutions/security/graphql', `@${dataFileName}JSON`);
});

Cypress.Commands.add('stubSearchStrategyApi', function (dataFileName) {
  cy.on('window:before:load', (win) => {
    win.fetch = null;
  });
  cy.server();
  cy.fixture(dataFileName).as(`${dataFileName}JSON`);
  cy.route('POST', 'internal/search/securitySolutionSearchStrategy', `@${dataFileName}JSON`);
});

Cypress.Commands.add(
  'attachFile',
  {
    prevSubject: 'element',
  },
  (input, fileName, fileType = 'text/plain') => {
    cy.fixture(fileName).then((content) => {
      const blob = Cypress.Blob.base64StringToBlob(content, fileType);
      const testFile = new File([blob], fileName, { type: fileType });
      const dataTransfer = new DataTransfer();

      dataTransfer.items.add(testFile);
      input[0].files = dataTransfer.files;
      return input;
    });
  }
);
