﻿Voici la structure des fichiers/dossiers

Application/src/main/java/com/example/android/
|
--- mediasession/	                <> Tout ce qui concerne la musique. 
|   |
|   --- client/
|   |   |
|   |   ---	MediaBrowserHelper	    <-- Inchangé, MainActivity.MediaBrowserConnection en
|   |                               hérite (personnaliser notre connection au MediaBrowser).
|   |
|   --- service/
|   |   | 
|   |   --- contentcatalogs/        <> Gestion de nos musique
|   |   |   |
|   |   |   --- DownloadLibrary
|   |   |   --- MusicDataBase       <--- Gestion, initialisation et accesseurs sur
|   |   |                           notre base de données.
|   |   |   --- MusicLibrary        <--- Classe static, permet de charger nos musiques
|   |   |                           en mémoire en un format que notre application peut
|   |   |                           utiliser (MediaMetadataCompat).
|   |   |   --- MusicLibraryLoader  <--- Permet de charger des musiques depuis diverse sources
|   |   |                           (ex. la BD ou un fichier downloadé).
|   |   |
|   |   --- notifications/          <> Inchangé, Gestion de la notification.
|   |   |
|   |   --- players/                <>
|   |   |   |
|   |   |   --- MediaPlayerAdapter  <--- Encapsule le MediaPlayer. Modifié pour jouer une 
|   |   |                           musique à partir d'un Uri, plutot que d'être limité au
|   |   |                           dossier Assets.
|   |   |
|   |   --- MusicService            <--- Inchangé, notre service de musique
|   |   --- PlaybackInfoListener    <--- Inchangé, pas utilisé
|   |   --- PlayerAdapter           <--- Inchangé, classe abstraite pour le MediaPlayerAdapter.
|   |
|   --- ui/                         <> Nos Activitées et composants associés.
|   |   |
|   |   --- fragment/               <> Contient le fragment pour la barre de navigation
|   |   |
|   |   --- DownloadListActivity    <--- En charge d'afficher et gérer les intéractions avec la
|   |   |                           liste de musiques téléchargeable.
|   |   --- MainActivity            <--- L'interface pour le player de musique.
|   |   --- MediaSeekBar            <--- Gère la barre de progression/scroll du player de musique.
|   |   --- MusicPlaylistActivity   <--- En charge d'afficher et gérer les intéractions avec la
|
|
--- wifip2p/		                <> Tout ce qui concerne le WiFi Peer to Peer.
|   |
|   --- file_transfert/             <>
|   |   |
|   |   --- AudioFileClientService  <---
|   |
|   |   --- AudioFileServerService  <---
|   |
|   |   --- DownloadEntry           <---
|   |
|   --- fragment/                   <>
|   |
|   --- DeviceDetailFragment        <--- Inchange selon exemple provenant de Google 
|   |			    				https://android.googlesource.com/platform/development/+/master/samples/WiFiDirectDemo/
|   |
|   --- DeviceListFragment          <--- Inchange selon exemple provenant de Google 
|   |								https://android.googlesource.com/platform/development/+/master/samples/WiFiDirectDemo/
|   |
|   --- WiFiDirectActivity          <--- Ajout d'une demande de permission a l'utilisateur,
|			 							Ajout d'une barre de tache inferieure,
|										Ajout de Toasts lors de connexion et de deconnexion au Wifi P2P. 
|   --- WiFiDirectBroadcastReceiver <--- Inchange selon exemple provenant de Google 
|										https://android.googlesource.com/platform/development/+/master/samples/WiFiDirectDemo/
|
END