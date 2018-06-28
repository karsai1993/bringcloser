/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendRelationshipRequestNotification = functions.database.ref('/requests/{requestedUid}/{requestorUid}')
    .onWrite((change, context) => {
      const requestorUid = context.params.requestorUid;
      const requestedUid = context.params.requestedUid;
      // If un-follow we exit the function.
      if (!change.after.val()) {
        return console.log('User ', requestorUid, 'un-followed user', requestedUid);
      }
      console.log('We have a new requestor UID:', requestorUid, 'for user:', requestedUid);

      // Get the list of device notification tokens.
      const getDeviceTokensPromise = admin.database()
          .ref(`/users/${requestedUid}/tokensMap`).once('value');

      // Get the requestor profile.
      const getRequestorProfilePromise = admin.auth().getUser(requestorUid);

      // The snapshot to the user's tokens.
      let tokensSnapshot;

      // The array containing all the user's tokens.
      let tokens;

      return Promise.all([getDeviceTokensPromise, getRequestorProfilePromise]).then(results => {
        tokensSnapshot = results[0];
        const requestor = results[1];

        // Check if there are any device tokens.
        if (!tokensSnapshot.hasChildren()) {
          return console.log('There are no notification tokens to send to.');
        }
        console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
        console.log('Fetched requestor profile', requestor);

        // Notification details.
        const payload = {
          notification: {
            title: 'New contact request!',
            body: `${requestor.displayName} wants to connect you.`,
            icon: `${requestor.photoURL}`
          }
        };

        // Listing all tokens as an array.
        tokens = Object.keys(tokensSnapshot.val());
        // Send notifications to all tokens.
        return admin.messaging().sendToDevice(tokens, payload);
      }).then((response) => {
        // For each message check if there was an error.
        const tokensToRemove = [];
        response.results.forEach((result, index) => {
          const error = result.error;
          if (error) {
            console.error('Failure sending notification to', tokens[index], error);
            // Cleanup the tokens who are not registered anymore.
            if (error.code === 'messaging/invalid-registration-token' ||
                error.code === 'messaging/registration-token-not-registered') {
              tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
            }
          }
        });
        return Promise.all(tokensToRemove);
      });
    });