# Calendy

Calendrier partagé pour la famille (web, adaptable en application Android avec Capacitor).
Données hébergées sur Supabase (projet `famille-calendrier`).

## Utilisation
Ouvrir `index.html` dans un navigateur, créer un compte, puis créer ou rejoindre une famille avec un code d'invitation.

## Sécurité
- La clé `sb_publishable_…` dans `index.html` est publique par conception ; l'accès aux données est protégé par les règles RLS de la base.
- Ne jamais ajouter au dépôt : clé `service_role`, mot de passe de la base, jeton d'accès Supabase, fichier keystore Android.
