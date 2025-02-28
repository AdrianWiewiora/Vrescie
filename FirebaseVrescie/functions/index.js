const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
const {assignUsersToConversation} = require("./conversationFunctions");
const {notifyNewMessage} = require("./notificationFunctions");
admin.initializeApp();

exports.assignUsersToConversation = assignUsersToConversation;
exports.notifyNewMessage = notifyNewMessage;

exports.checkAndRemoveConversations = functions.database
    .ref("/conversations/{conversationId}/members")
    .onUpdate(async (change, context) => {
      const conversationId = context.params.conversationId;

      // eslint-disable-next-line max-len
      const conversationSnapshot = await admin.database().ref(`/conversations/${conversationId}`).once("value");

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
    .ref("/vChatUsers/{userId}")
    .onUpdate(async (change, context) => {
      const userId = context.params.userId;

      // Sprawdź, czy to dodanie nowego użytkownika
      if (!change.before.exists()) {
        return null;
      }

      // Sprawdź, czy to usunięcie użytkownika
      if (!change.after.exists()) {
        return null;
      }

      const lastSeen3 = change.after.val().info.lastSeen;
      if (!lastSeen3) {
        // console.log("Brak informacji o lastSeen");
        return null;
      }

      const currentTimeS = Date.now();
      // console.log(`currentTime1: ${currentTimeS} ms`);
      const lastSeen1 = change.after.val().info.lastSeen;
      // console.log(`lastSeen1: ${lastSeen1} ms`);
      const timeDifference1 = currentTimeS - lastSeen1;
      // console.log(`Różnica czasu1: ${timeDifference1} ms`);

      // Ustawienie opóźnienia 8 sekund (8000 milisekund)
      const delay = 13000 - timeDifference1;

      try {
        // Poczekaj przez 13 sekund
        await new Promise((resolve) => setTimeout(resolve, delay));

        // Pobierz aktualne dane użytkownika
        // eslint-disable-next-line max-len
        const userSnapshot = await admin.database().ref(`/vChatUsers/${userId}`).once("value");
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
        const lastSeen = currentUserData.info.lastSeen;
        // console.log(`lastSeen2: ${lastSeen} ms`);

        // Oblicz różnicę czasu
        const timeDifference = currentTime - lastSeen;

        // console.log(`Różnica czasu2: ${timeDifference} ms`);
        // eslint-disable-next-line max-len
        // console.log(`Różnica czasu3: ${timeDifference - timeDifference1} ms`);

        // Sprawdź, czy minęło 7 sekund od ostatniego lastSeen
        if (timeDifference >= 12000) {
          // Usuń użytkownika
          await admin.database().ref(`/vChatUsers/${userId}`).remove();
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
