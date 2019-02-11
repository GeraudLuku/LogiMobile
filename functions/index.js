const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const firestore = functions.firestore;

exports.onUserStatusChange = functions.database
	.ref('/status/{userId}')
	.onUpdate(event => {
		
		var db = admin.firestore();
		
		
		//const usersRef = firestore.document('/users/' + event.params.userId);
		const usersRef = db.collection("USERS");
		var snapShot = event.data;
		
		return event.data.ref.once('value')
			.then(statusSnap => snapShot.val())
			.then(status => {
				if (status === 'offline'){
					usersRef
						.doc(event.params.userId)
						.set({
							online: false
						}, {merge: true});
				}
			})
});