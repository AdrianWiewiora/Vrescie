const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// eslint-disable-next-line max-len
// Globalna zmienna do przechowywania informacji o ostatnich użytkownikach w ciągu ostatnich 3 sekund
const recentUsers = {};

exports.assignUsersToConversation = functions.database
    .ref("/users/{userId}")
    .onWrite(async (change, context) => {
      const userId = context.params.userId;

      if (recentUsers[userId]) {
        // eslint-disable-next-line max-len
        console.log("Obaj użytkownicy są już w informacjach o ostatnich użytkownikach, nie tworzę nowej konwersacji.");
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
          // eslint-disable-next-line max-len
          otherUserId = Object.keys(conversation.members).find((id) => id !== userId);
          break;
        }
      }

      if (isInConversation) {
        // eslint-disable-next-line max-len
        // Jeżeli użytkownik jest już w konwersacji, sprawdź parametr canConnected
        // eslint-disable-next-line max-len
        const conversationRef = admin.database().ref(`/conversations/${getConversationId(userId, otherUserId)}`);
        const conversationSnapshot = await conversationRef.once("value");
        const conversationData = conversationSnapshot.val();

        if (conversationData.canConnected) {
          recentUsers[userId] = false;


          // eslint-disable-next-line max-len
          // Możesz zmienić konwersację z tym użytkownikiem, ponieważ canConnected jest true
          // Tutaj możesz dodać odpowiednią logikę lub inne działania
          return null;
        } else {
          // Nie możesz zmienić konwersacji, ponieważ canConnected jest false
          // Tutaj możesz dodać odpowiednią logikę lub inne działania

          // eslint-disable-next-line max-len
          const usersSnapshot = await admin.database().ref("/users").once("value");
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
          }, 8000);

          return null;
        }
      } else {
        // eslint-disable-next-line max-len
        // Użytkownik nie jest w żadnej konwersacji, więc wylosuj drugiego użytkownika
        // eslint-disable-next-line max-len
        const usersSnapshot = await admin.database().ref("/users").once("value");
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
}


exports.checkAndRemoveConversations = functions.database
    .ref("/conversations/{conversationId}")
    .onWrite(async (change, context) => {
      const conversationId = context.params.conversationId;
      const conversationSnapshot = change.after;

      if (conversationSnapshot.exists()) {
        const conversationData = conversationSnapshot.val();
        const members = conversationData.members;
        const canConnected = conversationData.canConnected;

        // Sprawdza, czy istnieje canConnected i czy jest równy false
        if (canConnected === false) {
        // Sprawdza, czy oba members są ustawione na false
          if (members) {
            // eslint-disable-next-line max-len
            const allMembersFalse = Object.values(members).every((value) => value === false);

            if (allMembersFalse) {
            // Usuwa konwersację
              // eslint-disable-next-line max-len
              await admin.database().ref(`/conversations/${conversationId}`).remove();
              console.log(`Usunięto konwersację: ${conversationId}`);
            }
          }
        }
      }

      return null;
    });


exports.removeInactiveUsers = functions.database
    .ref("/users/{userId}")
    .onWrite(async (change, context) => {
      const userId = context.params.userId;

      // Sprawdź, czy to dodanie nowego użytkownika
      if (!change.before.exists()) {
        // console.log("Nowy użytkownik został dodany:", userId);
        // eslint-disable-next-line max-len
        return null; // Jeśli to dodanie nowego użytkownika, nie wykonuj reszty funkcji
      }

      // Sprawdź, czy to usunięcie użytkownika
      if (!change.after.exists()) {
        // console.log("Użytkownik został usunięty:", userId);
        // eslint-disable-next-line max-len
        return null; // Jeśli to usunięcie użytkownika, nie wykonuj reszty funkcji
      }

      const lastSeen3 = change.after.val().lastSeen;
      if (!lastSeen3) {
        // console.log("Brak informacji o lastSeen");
        return null;
      }

      const currentTimeS = Date.now();
      // console.log(`currentTime1: ${currentTimeS} ms`);
      const lastSeen1 = change.after.val().lastSeen;
      // console.log(`lastSeen1: ${lastSeen1} ms`);
      const timeDifference1 = currentTimeS - lastSeen1;
      // console.log(`Różnica czasu1: ${timeDifference1} ms`);

      // Ustawienie opóźnienia 8 sekund (8000 milisekund)
      const delay = 8000 - timeDifference1;

      try {
        // Poczekaj przez 8 sekund
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Pobierz aktualne dane użytkownika
        // eslint-disable-next-line max-len
        const userSnapshot = await admin.database().ref(`/users/${userId}`).once("value");
        const currentUserData = userSnapshot.val();

        // Sprawdź, czy użytkownik nadal istnieje
        if (!currentUserData) {
          // console.log("Użytkownik został usunięty:", userId);
          return null;
        }

        // Odczytaj aktualny czas
        const currentTime = Date.now();
        // console.log(`currentTime2: ${currentTime} ms`);

        // Odczytaj lastSeen użytkownika
        const lastSeen = currentUserData.lastSeen;
        // console.log(`lastSeen2: ${lastSeen} ms`);

        // Oblicz różnicę czasu
        const timeDifference = currentTime - lastSeen;

        // console.log(`Różnica czasu2: ${timeDifference} ms`);
        // eslint-disable-next-line max-len
        // console.log(`Różnica czasu3: ${timeDifference - timeDifference1} ms`);

        // Sprawdź, czy minęło 7 sekund od ostatniego lastSeen
        if (timeDifference >= 7000) {
          // Usuń użytkownika
          await admin.database().ref(`/users/${userId}`).remove();
        }

        return null;
      } catch (error) {
        console.error("Błąd podczas przetwarzania funkcji:", error);
        return null;
      }
    });

exports.checkLikesAndCopyConversation = functions.database
    .ref("/conversations/{conversationId}/likes")
    .onUpdate((change, context) => {
      const conversationId = context.params.conversationId;
      const likesSnapshot = change.after.val();
      let conversationData;

      // Sprawdź, czy są co najmniej dwa lajki (true) w polu "likes"
      // eslint-disable-next-line max-len
      const likesCount = Object.values(likesSnapshot).filter((value) => value === true).length;

      if (likesCount >= 2) {
      // Pobierz konwersację z bieżącego "conversations"
        // eslint-disable-next-line max-len
        return admin.database().ref(`/conversations/${conversationId}`).once("value")
            .then((snapshot) => {
              conversationData = snapshot.val();

              console.log("Members in conversationData:");
              const promises = [];
              // eslint-disable-next-line guard-for-in
              for (const memberId in conversationData.members) {
                console.log(memberId);

                // Pobierz dane użytkownika z tabeli "user" dla danego memberId
                // eslint-disable-next-line max-len
                const promise = admin.database().ref(`/user/${memberId}`).once("value")
                    .then((userSnapshot) => {
                      const userData = userSnapshot.val();

                      // Zaktualizuj wartość w members na imię użytkownika
                      conversationData.members[memberId] = userData.name;
                    })
                    .catch((error) => {
                      // eslint-disable-next-line max-len
                      console.error("Error fetching user data for member:", error);
                    });

                promises.push(promise);
              }

              // Czekaj na zakończenie wszystkich operacji asynchronicznych
              return Promise.all(promises)
                  .then(() => {
                    // eslint-disable-next-line max-len
                  // Skopiuj konwersację do "explicit_conversations" (bez pola "canConnected")
                    // eslint-disable-next-line max-len
                    return admin.database().ref(`/explicit_conversations/${conversationId}`).set({
                      members: conversationData.members,
                      messages: conversationData.messages,
                    });
                  })
                  .catch((error) => {
                    console.error("Error copying conversation:", error);
                  });
            })
            .catch((error) => {
              console.error("Error copying conversation:", error);
            });
      }

      return null;
    });
