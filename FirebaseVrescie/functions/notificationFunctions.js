const functions = require("firebase-functions");
const admin = require("firebase-admin");
exports.notifyNewMessage = functions.database
    .ref("/explicit_conversations/{conversationId}/messages/{messageId}")
    .onCreate(async (snapshot, context) => {
      const messageData = snapshot.val();
      const {conversationId} = context.params;

      // Pobierz ID nadawcy
      const senderId = messageData.senderId;
      const messageText = messageData.text;

      // Pobierz członków konwersacji
      // eslint-disable-next-line max-len
      const conversationRef = admin.database().ref(`/explicit_conversations/${conversationId}/members`);
      const membersSnapshot = await conversationRef.once("value");
      const members = membersSnapshot.val();

      // Znajdź ID drugiego użytkownika
      const receiverId = Object.keys(members).find((id) => id !== senderId);
      const senderName = members[senderId];

      // Pobierz token FCM odbiorcy
      const userRef = admin.database().ref(`/user/${receiverId}/fcmToken`);
      const userTokenSnapshot = await userRef.once("value");
      const userToken = userTokenSnapshot.val();

      if (!userToken) return;

      const message = {
        token: userToken,
        notification: {
          title: senderName,
          body: messageText,
        },
      };

      try {
        await admin.messaging().send(message);
        console.log("Powiadomienie wysłane pomyślnie");
      } catch (error) {
        console.error("Błąd podczas wysyłania powiadomienia:", error);
      }
    });
