// Service worker minimal : permet l'installation sur l'écran d'accueil.
// Pas de mode hors-ligne : les requêtes passent toujours par le réseau.
self.addEventListener("install", () => self.skipWaiting());
self.addEventListener("activate", e => e.waitUntil(self.clients.claim()));
self.addEventListener("fetch", () => {});
