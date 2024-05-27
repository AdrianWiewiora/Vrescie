const admin = require("firebase-admin");
const functions = require("firebase-functions");

// eslint-disable-next-line max-len
// Globalna zmienna do przechowywania informacji o ostatnich użytkownikach w ciągu ostatnich 3 sekund
const recentUsers = {};

exports.assignUsersToConversation = functions.database
    .ref("/vChatUsers/{userId}")
    .onCreate(async (change, context) => {
      const userId = context.params.userId;

      if (recentUsers[userId]) {
        // eslint-disable-next-line max-len
        console.log("Użytkownik dopiero został przydzielony do konwersacji");
        return null;
      }

      recentUsers[userId] = true;

      // Sprawdź, czy użytkownik jest już w jakiejkolwiek konwersacji
      // eslint-disable-next-line max-len
      const conversationsSnapshot = await admin.database().ref(`/conversations`).once("value");
      const conversationsData = conversationsSnapshot.val();

      let isInConversation = false;
      let otherUserId = null;


      // eslint-disable-next-line max-len
      // Iteruj przez konwersacje, aby sprawdzić, czy użytkownik jest już w jakiejkolwiek konwersacji
      // eslint-disable-next-line guard-for-in
      for (const conversationId in conversationsData) {
        const conversation = conversationsData[conversationId];
        if (userId in conversation.members) {
          isInConversation = true;
          // eslint-disable-next-line max-len,no-unused-vars
          otherUserId = Object.keys(conversation.members).find((id) => id !== userId);
          break;
        }
      }
      if (isInConversation) {
        // eslint-disable-next-line max-len
        const conversationRef = admin.database().ref(`/conversations/${getConversationId(userId, otherUserId)}`);
        const conversationSnapshot = await conversationRef.once("value");
        const conversationData = conversationSnapshot.val();
        if (conversationData.canConnected) {
          recentUsers[userId] = false;
          return null;
        } else {
          await recreateConversation(userId);
        }
      } else {
        await recreateConversation(userId);
      }
    });

// Funkcja do tworzenia unikalnego identyfikatora konwersacji
// eslint-disable-next-line require-jsdoc
function getConversationId(userId1, userId2) {
  const sortedIds = [userId1, userId2].sort();
  return `${sortedIds[0]}_${sortedIds[1]}`;
}

// Funkcja do tworzenia konwersacji
// eslint-disable-next-line require-jsdoc
async function createConversation(userId1, userId2) {
  const conversationId = getConversationId(userId1, userId2);

  // eslint-disable-next-line max-len
  const conversationRef = admin.database().ref(`/conversations/${conversationId}`);

  const conversationData = {
    canConnected: true,
    members: {
      [userId1]: true,
      [userId2]: true,
    },
    messages: [],
  };

  await conversationRef.set(conversationData);

  // Usuń rekordy użytkowników z vChatUsers
  await admin.database().ref(`/vChatUsers/${userId1}`).remove();
  await admin.database().ref(`/vChatUsers/${userId2}`).remove();
}


// Funkcja do tworzenia unikalnego identyfikatora konwersacji
// eslint-disable-next-line require-jsdoc
async function recreateConversation(userId) {
  // eslint-disable-next-line max-len,indent
    // Użytkownik nie jest w żadnej konwersacji, więc wylosuj drugiego użytkownika
  // eslint-disable-next-line max-len
  const usersSnapshot = await admin.database().ref("/vChatUsers").once("value");
  const usersList = Object.keys(usersSnapshot.val());

  // Wylosuj innego użytkownika
  let randomIndex = Math.floor(Math.random() * usersList.length);
  while (usersList[randomIndex] === userId) {
    randomIndex = Math.floor(Math.random() * usersList.length);
  }

  const otherUserId = usersList[randomIndex];

  // Utwórz nową konwersację
  await createConversation(userId, otherUserId);

  // Dodaj informacje o użytkownikach do ostatnich użytkowników
  recentUsers[userId] = true;
  recentUsers[otherUserId] = true;

  // Ustaw timer do usunięcia informacji po 3 sekundach
  setTimeout(() => {
    delete recentUsers[userId];
    delete recentUsers[otherUserId];
  }, 3000);

  return null;
}
