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
const nodemailer = require('nodemailer');
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
 
exports.sendNewWishNotification = functions.database.ref('/connections/{key}/wishes/{wish_key}')
    .onWrite((change, context) => {
	
	if (!change.after.exists()) {
        return null;
    }
	
	const wish = change.after.val();
	const prevWish = change.before.val();
	
	const fromUidValue = wish.fromUid;
	const connectionFromUidValue = wish.connectionFromUid;
	const connectionToUidValue = wish.connectionToUid;
	let fromUid = connectionFromUidValue;
	let toUid = connectionToUidValue;
	if (fromUidValue === connectionToUidValue) {
		fromUid = connectionToUidValue;
		toUid = connectionFromUidValue;
	}
	
	const hasArrived = wish.hasArrived;
	const message = wish.text;
	
	let toTokensRef = admin.database().ref(`/users/${toUid}/tokensMap`);
	let fromNameRef = admin.database().ref(`/users/${fromUid}/username`);
	let fromPhotoUrl = admin.database().ref(`/users/${fromUid}/photoUrl`);

	if ((prevWish === null && hasArrived === true) || (prevWish !== null && prevWish.hasArrived !== hasArrived && hasArrived === true)) {
		console.log('Sending wish notification from ', fromUid, ' to ', toUid);
		
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
			const type = "wish";
			const payload = {
				data: {
					type: `${type}`,
					name: `${nameResult}`,
					photoUrl: `${photoUrlResult}`,
					content: message
				}
			};
			tokens = Object.keys(tokensSnapshot.val());
			return admin.messaging().sendToDevice(tokens, payload);
		});
	}
	return null;
 });
 
exports.sendNewEventNotification = functions.database.ref('/connections/{key}/events/{event_key}')
    .onWrite((change, context) => {
	
	if (!change.after.exists()) {
        return null;
    }
	
	const object = change.after.val();
	const prevObject = change.before.val();
	
	const fromUidValue = object.fromUid;
	const connectionFromUidValue = object.connectionFromUid;
	const connectionToUidValue = object.connectionToUid;
	let fromUid = connectionFromUidValue;
	let toUid = connectionToUidValue;
	if (fromUidValue === connectionToUidValue) {
		fromUid = connectionToUidValue;
		toUid = connectionFromUidValue;
	}
	
	const hasArrived = object.hasArrived;
	const message = object.text;
	
	let toTokensRef = admin.database().ref(`/users/${toUid}/tokensMap`);
	let fromNameRef = admin.database().ref(`/users/${fromUid}/username`);
	let fromPhotoUrl = admin.database().ref(`/users/${fromUid}/photoUrl`);

	if ((prevObject === null && hasArrived === true) || (prevObject !== null && prevObject.hasArrived !== hasArrived && hasArrived === true)) {
		console.log('Sending event notification from ', fromUid, ' to ', toUid);
		
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
			const type = "event";
			const payload = {
				data: {
					type: `${type}`,
					name: `${nameResult}`,
					photoUrl: `${photoUrlResult}`,
					content: message
				}
			};
			tokens = Object.keys(tokensSnapshot.val());
			return admin.messaging().sendToDevice(tokens, payload);
		});
	}
	return null;
 });
 
exports.sendNewThoughtNotification = functions.database.ref('/connections/{key}/thoughts/{thought_key}')
    .onWrite((change, context) => {
	
	if (!change.after.exists()) {
        return null;
    }
	
	const object = change.after.val();
	const prevObject = change.before.val();

	const fromUidValue = object.fromUid;
	const connectionFromUidValue = object.connectionFromUid;
	const connectionToUidValue = object.connectionToUid;
	let fromUid = connectionFromUidValue;
	let toUid = connectionToUidValue;
	if (fromUidValue === connectionToUidValue) {
		fromUid = connectionToUidValue;
		toUid = connectionFromUidValue;
	}
	
	const hasArrived = object.hasArrived;
	const message = object.text;
	
	let toTokensRef = admin.database().ref(`/users/${toUid}/tokensMap`);
	let fromNameRef = admin.database().ref(`/users/${fromUid}/username`);
	let fromPhotoUrl = admin.database().ref(`/users/${fromUid}/photoUrl`);

	if ((prevObject === null && hasArrived === true) || (prevObject !== null && prevObject.hasArrived !== hasArrived && hasArrived === true)) {
		console.log('Sending thought notification from ', fromUid, ' to ', toUid);
		
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
			const type = "thought";
			const payload = {
				data: {
					type: `${type}`,
					name: `${nameResult}`,
					photoUrl: `${photoUrlResult}`,
					content: message
				}
			};
			tokens = Object.keys(tokensSnapshot.val());
			return admin.messaging().sendToDevice(tokens, payload);
		});
	}
	return null;
 });
exports.sendNewMessageNotification = functions.database.ref('/connections/{key}/messages/{message_key}')
    .onWrite((change, context) => {
	
	if (!change.after.exists()) {
        return null;
    }
	
	const object = change.after.val();
	const fromUid = object.from;
	const toUid = object.to;
	const extraPhotoUrl = object.photoUrl;
	const message = object.text;
	const connectionFromUid = object.connectionFromUid;
	const connectionToUid = object.connectionToUid;
	const key = context.params.key;
	
	let isChatVisibleRef = admin.database().ref(`/chat_visibility/${connectionFromUid}_${connectionToUid}/${toUid}`);
	let toTokensRef = admin.database().ref(`/users/${toUid}/tokensMap`);
	let fromNameRef = admin.database().ref(`/users/${fromUid}/username`);
	let fromPhotoUrl = admin.database().ref(`/users/${fromUid}/photoUrl`);
	let fromGenderRef = admin.database().ref(`/users/${fromUid}/gender`);
	let fromBirthdayRef = admin.database().ref(`/users/${fromUid}/birthday`);
	let toNameRef = admin.database().ref(`/users/${toUid}/username`);
	let toPhotoUrlRef = admin.database().ref(`/users/${toUid}/photoUrl`);
	let toGenderRef = admin.database().ref(`/users/${toUid}/gender`);
	let toBirthdayRef = admin.database().ref(`/users/${toUid}/birthday`);
	let connTypeRef = admin.database().ref(`/connections/${key}/type`);
	let timestampRef = admin.database().ref(`/connections/${key}/timestamp`);
	
	const isChatVisible = isChatVisibleRef.once('value');
	const deviceTokensPromise = toTokensRef.once('value');
	const name = fromNameRef.once('value');
	const photoUrl = fromPhotoUrl.once('value');
	const fromGender = fromGenderRef.once('value');
	const fromBirthday = fromBirthdayRef.once('value');
	const toName = toNameRef.once('value');
	const toPhotoUrl = toPhotoUrlRef.once('value');
	const toGender = toGenderRef.once('value');
	const toBirthday = toBirthdayRef.once('value');
	const connType = connTypeRef.once('value');
	const timestamp = timestampRef.once('value');
		
	let tokensSnapshot;
	let tokens;
		
	return Promise.all([
		deviceTokensPromise,
		name,
		photoUrl,
		fromGender,
		fromBirthday,
		toName,
		toPhotoUrl,
		toGender,
		toBirthday,
		connType,
		timestamp,
		isChatVisible]).then(results => {
		tokensSnapshot = results[0];
		const nameResult = results[1].val();
		const photoUrlResult = results[2].val();
		const fromGenderResult = results[3].val();
		const fromBirthdayResult = results[4].val();
		const toNameResult = results[5].val();
		const toPhotoUrlResult = results[6].val();
		const toGenderResult = results[7].val();
		const toBirthdayResult = results[8].val();
		const connTypeResult = results[9].val();
		const timestampResult = results[10].val();
		const isChatVisibleResult = results[11].val();
		if (isChatVisibleResult === true) {
			return null;
		}
		const type = "message";
		const payload = {
			data: {
				type: `${type}`,
				name: `${nameResult}`,
				photoUrl: `${photoUrlResult}`,
				content: `${message}`,
				extraPhotoUrl: `${extraPhotoUrl}`,
				fromUid: connectionFromUid,
				fromGender: `${fromGenderResult}`,
				fromBirthday: `${fromBirthdayResult}`,
				toUid: connectionToUid,
				toName: `${toNameResult}`,
				toPhotoUrl: `${toPhotoUrlResult}`,
				toGender: `${toGenderResult}`,
				toBirthday: `${toBirthdayResult}`,
				connectionType: `${connTypeResult}`,
				timestamp: `${timestampResult}`
			}
		};
		tokens = Object.keys(tokensSnapshot.val());
		console.log('Sending message notification from ', fromUid, ' to ', toUid);
		return admin.messaging().sendToDevice(tokens, payload);
	});
 });
exports.sendEmail = functions.database.ref('/problems')
    .onWrite((change, context) => {
	
	if (!change.after.exists()) {
        return null;
    }
	
	const object = change.after.val();
	const fromUid = object.from;
	const message = object.message;
	const time = object.utc_time;
	const tag = object.tag;
	
	const gmailEmail = functions.config().gmail.email;
	const gmailPassword = functions.config().gmail.password;
	console.log('email:', gmailEmail, 'pw:', gmailPassword);
	const mailTransport = nodemailer.createTransport({
		service: 'gmail',
		auth: {
			user: gmailEmail,
			pass: gmailPassword,
		},
	});
	
	const email = `karsai1993@gmail.com`;
	const mailOptions = {
		//from: `<noreply.alert@bringcloser.com>`,
		from: email,
		to: email,
	};

	
	mailOptions.subject = `BringCloser problem in ${tag}!`;
	mailOptions.text = `Problem from ${fromUid} with message: ${message} at UTC ${time}`;
	return mailTransport.sendMail(mailOptions).then(() => {
		return console.log('New welcome email sent to:', email);
	});
 });