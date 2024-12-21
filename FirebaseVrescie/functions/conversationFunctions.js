const admin = require("firebase-admin");
const functions = require("firebase-functions/v1");

// eslint-disable-next-line max-len
// Globalna zmienna do przechowywania informacji o ostatnich użytkownikach w ciągu ostatnich 3 sekund
const recentUsers = {};

exports.assignUsersToConversation = functions.database
    .ref("/vChatUsers/{userId}")
    .onCreate(async (change, context) => {
      const userId = context.params.userId;

      if (recentUsers[userId]) {
        // Użytkownik dopiero został przydzielony do konwersacji
        return null;
      }

      recentUsers[userId] = true;

      // Sprawdź, czy użytkownik jest już w jakiejkolwiek konwersacji
      // eslint-disable-next-line max-len
      const conversationsSnapshot = await admin.database()
          .ref(`/conversations`)
          .orderByChild(`members/${userId}`)
          .equalTo(true)
          .once("value");

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
          await matchUsers(userId);
        }
      } else {
        await matchUsers(userId);
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


// Funkcja do szukania partnera wedle preferencji
// eslint-disable-next-line require-jsdoc
async function matchUsers(userId) {
  const usersSnapshot = await admin.database().ref("/vChatUsers").once("value");
  const usersData = usersSnapshot.val();

  let randomIndex = Math.floor(Math.random() * Object.keys(usersData).length);
  let otherUserId = null;
  let attempts = 0; // Licznik prób

  while (!otherUserId && attempts < Object.keys(usersData).length) {
    randomIndex = Math.floor(Math.random() * Object.keys(usersData).length);
    const otherUserKey = Object.keys(usersData)[randomIndex];
    if (otherUserKey !== userId) {
      const otherUserData = usersData[otherUserKey];
      if (
      // eslint-disable-next-line max-len
        ((usersData[userId].info.gender === "male" && (otherUserData.pref.gender_pref === "FM" || otherUserData.pref.gender_pref === "M")) ||
        // eslint-disable-next-line max-len
        (usersData[userId].info.gender === "female" && (otherUserData.pref.gender_pref === "FM" || otherUserData.pref.gender_pref === "F"))) &&
        // eslint-disable-next-line max-len
        ((otherUserData.info.gender === "male" && (usersData[userId].pref.gender_pref === "FM" || usersData[userId].pref.gender_pref === "M")) ||
        // eslint-disable-next-line max-len
        (otherUserData.info.gender === "female" && (usersData[userId].pref.gender_pref === "FM" || usersData[userId].pref.gender_pref === "F")))
      ) {
        otherUserId = otherUserKey;
      }
    }
    attempts++; // Zwiększ licznik prób
  }

  if (!otherUserId) {
    setTimeout(() => {
      delete recentUsers[userId];
      delete recentUsers[otherUserId];
    }, 3000);
    return null;
  }

  await createConversation(userId, otherUserId);

  recentUsers[userId] = true;
  recentUsers[otherUserId] = true;

  setTimeout(() => {
    delete recentUsers[userId];
    delete recentUsers[otherUserId];
  }, 3000);

  return null;
}

