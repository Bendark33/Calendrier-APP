# Widget Calendy « Aujourd'hui » (Android)

Résumé des événements de la journée sur l'écran d'accueil : date, nombre d'événements, liste (heure, icône du type,
titre, couleurs des personnes concernées, étoile si important) et bouton « + » pour créer un événement.

## Comment ça marche
- L'appli (index.html) envoie au widget un résumé des **14 prochains jours** (répétitions déjà calculées) à chaque ouverture,
  à chaque changement d'événement détecté, et au retour dans l'appli.
- Le widget **filtre lui-même sur la date du jour** : il passe au jour suivant à minuit sans que l'appli soit ouverte.
- Il n'est mis à jour qu'à ces moments-là : un événement ajouté par un proche n'apparaît qu'à la prochaine ouverture
  (ou quand l'appli reçoit le changement en direct).
- À la déconnexion, les données du widget sont effacées.

## Installation (projet Capacitor : dossier `mobile`)

1. **Copie les fichiers** de ce dossier dans `mobile/android/app/src/main/` en gardant l'arborescence :
   - `java/com/bendark/calendy/` : `CalendyWidgetData.java`, `CalendyWidgetProvider.java`, `CalendyWidgetService.java`, `WidgetBridgePlugin.java`
   - `res/` : tout le contenu (layout, drawable, values, values-night, xml)
   - `MainActivity.java` : **seulement si le tien est celui par défaut** (vide). Sinon, ajoute uniquement la ligne
     `registerPlugin(WidgetBridgePlugin.class);` dans `onCreate`, **avant** `super.onCreate(...)`.
2. **Manifeste** : ouvre `mobile/android/app/src/main/AndroidManifest.xml` et colle le contenu de `AndroidManifest-AJOUT.xml`
   à l'intérieur de `<application> ... </application>` (avant `</application>`).
   Attention : le fichier `AndroidManifest.xml` que tu m'as envoyé est presque vide, ce n'est pas celui de l'appli.
3. **Appli web** : copie le nouveau `index.html` dans `mobile/www/` (ton appli embarque ses fichiers, elle ne charge pas le site),
   puis dans le dossier `mobile` : `npx cap sync android`.
4. **Android Studio** : ouvre `mobile/android`, Build > Make Project, puis lance l'appli sur le téléphone.
5. **Ajoute le widget** : appui long sur l'écran d'accueil > Widgets > Calendy > « Calendy · Aujourd'hui ».
6. Ouvre l'appli une fois (connecté) : le widget se remplit.

## Si le widget reste vide
- « Ouvre Calendy et connecte-toi… » : l'appli n'a encore rien envoyé. Ouvre-la, attends quelques secondes.
- Dans Android Studio, Logcat, filtre `Capacitor` ou `Widget` : une erreur `WidgetBridge ... not implemented`
  signifie que le plugin n'est pas enregistré (étape 1, `MainActivity`) ou que `npx cap sync` / la recompilation manque.
- Appui long sur le widget > supprimer, puis le rajouter, après une mise à jour du code.
