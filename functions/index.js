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

exports.sendRelationshipOperationsNotification = functions.database.ref('/connections/{key}')
    .onWrite((change, context) => {
	
	if (!change.after.exists()) {
        return null;
    }
	
	const connection = change.after.val();
	const prevConnection = change.before.val();
	const fromUid = connection.fromUid;
	const toUid = connection.toUid;
	const connectionBit = connection.connectionBit;
	
	let toTokensRef = admin.database().ref(`/users/${toUid}/tokensMap`);
	let fromTokensRef = admin.database().ref(`/users/${fromUid}/tokensMap`);
	let toNameRef = admin.database().ref(`/users/${toUid}/username`);
	let fromNameRef = admin.database().ref(`/users/${fromUid}/username`);
	let toPhotoUrl = admin.database().ref(`/users/${toUid}/photoUrl`);
	let fromPhotoUrl = admin.database().ref(`/users/${fromUid}/photoUrl`);

	if (!change.before.exists()) {
		console.log('Sending relationship request from ', fromUid, ' to ', toUid);
		
		const deviceTokensPromise = toTokensRef.once('value');
		const name = fromNameRef.once('value');
		const photoUrl = fromPhotoUrl.once('value');
		
		let tokensSnapshot;
		let tokens;
		
		return Promise.all([
			deviceTokensPromise,
			name,
			photoUrl]).then(results => {
			tokensSnapshot = results[0];
			const nameResult = results[1].val();
			const photoUrlResult = results[2].val();
			const type = "request";
			const payload = {
				data: {
					type: `${type}`,
					name: `${nameResult}`,
					photoUrl: `${photoUrlResult}`
				}
			};
			tokens = Object.keys(tokensSnapshot.val());
			return admin.messaging().sendToDevice(tokens, payload);
		});
	} else {
		const prevConnectionBit = prevConnection.connectionBit;
		if (prevConnectionBit === 0 && connectionBit === 1){
			console.log('Sending relationship approval from ', toUid, ' to ', fromUid);
			
			const deviceTokensPromise = fromTokensRef.once('value');
			const name = toNameRef.once('value');
			const photoUrl = toPhotoUrl.once('value');
			
			let tokensSnapshot;
			let tokens;
			
			return Promise.all([
				deviceTokensPromise,
				name,
				photoUrl]).then(results => {
				tokensSnapshot = results[0];
				const nameResult = results[1].val();
				const photoUrlResult = results[2].val();
				const type = "approval";
				const payload = {
					data: {
						type: `${type}`,
						name: `${nameResult}`,
						photoUrl: `${photoUrlResult}`
					}
				};
				tokens = Object.keys(tokensSnapshot.val());
				return admin.messaging().sendToDevice(tokens, payload);
			});
		}
		else {
			return null;
		}
	}
 });