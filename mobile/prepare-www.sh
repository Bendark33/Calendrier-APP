#!/bin/sh
# Copie les fichiers du site dans le dossier "www" lu par Capacitor.
rm -rf www && mkdir www
cp ../index.html ../manifest.webmanifest ../sw.js ../icon-192.png ../icon-512.png www/
