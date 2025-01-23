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

      const conversationsSnapshot = await admin.database()
          .ref(`/conversations`)
          .orderByChild(`members/${userId}`)
          .equalTo(true)
          .once("value");

      const conversationsData = conversationsSnapshot.val();

      let isInConversation = false;
      let otherUserId = null;


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
      const userData = usersData[userId];

      const userAge = parseInt(userData.info.age, 10); // wiek użytkownika
      const otherUserAge = parseInt(otherUserData.info.age, 10);

      // Sprawdzamy, czy użytkownicy pasują do siebie po płci
      const genderMatch =
      // eslint-disable-next-line max-len
        (userData.info.gender === "male" && (otherUserData.pref.gender_pref === "FM" || otherUserData.pref.gender_pref === "M")) ||
        // eslint-disable-next-line max-len
        (userData.info.gender === "female" && (otherUserData.pref.gender_pref === "FM" || otherUserData.pref.gender_pref === "F"));

      const reverseGenderMatch =
      // eslint-disable-next-line max-len
        (otherUserData.info.gender === "male" && (userData.pref.gender_pref === "FM" || userData.pref.gender_pref === "M")) ||
        // eslint-disable-next-line max-len
        (otherUserData.info.gender === "female" && (userData.pref.gender_pref === "FM" || userData.pref.gender_pref === "F"));

      const ageMatch =
      // eslint-disable-next-line max-len
        (otherUserAge >= userData.pref.age_min_pref &&
        // eslint-disable-next-line max-len
        (userData.pref.age_max_pref === 50 || otherUserAge <= userData.pref.age_max_pref)) &&

        (userAge >= otherUserData.pref.age_min_pref &&
        // eslint-disable-next-line max-len
        (otherUserData.pref.age_max_pref === 50 || userAge <= otherUserData.pref.age_max_pref));

      // eslint-disable-next-line max-len
      const relationMatch = userData.pref.relation_pref === otherUserData.pref.relation_pref;

      // Obliczamy odległość, jeśli obaj użytkownicy podali współrzędne
      const userLat = userData.info.latitude;
      const userLon = userData.info.longitude;
      const otherUserLat = otherUserData.info.latitude;
      const otherUserLon = otherUserData.info.longitude;

      // Obliczanie odległości w km
      // eslint-disable-next-line max-len
      const distance = calculateDistance(userLat, userLon, otherUserLat, otherUserLon);

      // Sprawdzamy, czy odległość mieści się w granicach preferencji
      // eslint-disable-next-line max-len
      const locationMatch = distance <= userData.pref.location_max_pref && distance <= otherUserData.pref.location_max_pref;
      // Sprawdzamy, czy wiek i płeć pasują
      // eslint-disable-next-line max-len
      if (genderMatch && reverseGenderMatch && ageMatch && relationMatch && locationMatch) {
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

// Funkcja do obliczania odległości między dwoma punktami geograficznymi
// eslint-disable-next-line require-jsdoc
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371; // Promień Ziemi w km
  const rad = Math.PI / 180; // Przemiana stopni na radiany

  const deltaLat = (lat2 - lat1) * rad; // Różnica szerokości w radianach
  const deltaLon = (lon2 - lon1) * rad; // Różnica długości w radianach

  const a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
            Math.cos(lat1 * rad) * Math.cos(lat2 * rad) *
            Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  // Odległość w kilometrach
  return R * c;
}
