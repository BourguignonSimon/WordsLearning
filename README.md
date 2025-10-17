🎯 Contexte du projet

Fonction principale : application Android native 100 % hors‑ligne pour enregistrer, transcrire (moteur Vosk par défaut), résumer et exporter des conversations audio.

Flux actuel : bouton d’enregistrement → audio PCM/WAV chiffré → WorkManager lance la transcription (Vosk) → un résumé JSON structuré est généré → possibilité d’exporter en Markdown/JSON.

Architecture : Kotlin + Jetpack (Room/WorkManager) + SQLCipher pour la DB chiffrée ; fichiers audio et exports chiffrés AES‑GCM (Keystore)
github.com
.

Modèles : import Vosk (zip) et option Whisper JNI (gguf) via menu ; modèle Whisper sélectionné via BuildConfig.USE_WHISPER
github.com
.

Docs : un ensemble de fichiers docs/ fixe les exigences MVP, la roadmap d’amélioration et décrit en détail la chaîne d’enregistrement audio (sélection dynamique du micro, normalisation RMS, réduction de bruit RNNoise/ONNX, VAD, gestion des erreurs)
github.com
github.com
.

✅ Priorités MVP (à implémenter pour un test Android Studio)

1. Build & compilation :

Finaliser les implémentations manquantes dans RecordService.start(context) et RecordService.stop(context) et corriger l’import java.io.RandomAccessFile (ENHANCEMENT_PLAN)
github.com
.

S’assurer que la configuration gradle (minSdk=26, target=35) compile sans erreurs et que l’option Vosk fonctionne par défaut.

2. Enregistrement audio fiable :

Implémenter la sélection dynamique du micro selon la priorité définie (filaires > Bluetooth SCO > micro interne) en utilisant AudioManager et AudioRecord.Builder#setPreferredDevice
github.com
.

Ajouter la normalisation RMS et la réduction de bruit RNNoise (modèle ONNX ou noise‑gate fallback) avant l’écriture WAV
github.com
.

Gérer les erreurs ERROR_INVALID_OPERATION, ERROR_BAD_VALUE, ERROR_DEAD_OBJECT et SecurityException en envoyant un ACTION_RECORDING_ERROR et en affichant une bannière 
github.com
.

3. Transcription & résumé :

Maintenir l’option Vosk comme moteur par défaut ; segmentation automatique pour les longs enregistrements (fenêtrage et reprise mémoire)
github.com
.

Générer un résumé global (JSON structuré : titre, résumé, actions, décisions, sentiments, participants, tags, topics, mots‑clés, timings) sans correction manuelle
github.com
.

Respecter l’objectif de traitement hors‑ligne ≤ 30 min pour 60 min d’audio
github.com
.

4. Organisation & UX :

Implémenter une liste des sessions avec filtres date/durée et recherche FTS.

Ajouter un écran Détail avec lecteur audio, transcription segmentée et résumé JSON ; permettre l’export Markdown/JSON (déjà présent dans v0.6.0).

Intégrer l’import de modèle via SAF pour Vosk/Whisper et gérer les permissions (persist URI, collisions)
github.com
.

Assurer la protection par PIN/biométrie et le chiffrement AES‑GCM (audio, exports) et SQLCipher (DB)
github.com
.

5. Tests & validation :

Préparer des scénarios manuels : perte d’autorisation micro, interruption d’appel, batterie faible, conflit avec une autre app, déconnexion du micro
github.com
.

Mettre en place des tests instrumentés sur appareil réel/émulateur et définir les métriques : WER pour l’ASR, ROUGE/BLEU pour les résumés et satisfaction utilisateur
github.com
.

Inclure un outil interne pour vérifier la qualité audio (SNR, bruit)
github.com
.

🔜 Améliorations post‑MVP (phase 2 et suivantes)

Mode arrière‑plan complet (enregistrement/tâches en tâche de fond) : requiert gestion fine du service et des permissions.

Amélioration UX : découpage par thèmes, visualisation chronologique des segments, dossiers/projets, personnalisation UI.

Intégration Whisper JNI : finaliser le wrapper JNI, autoriser la sélection dynamique Vosk/WhisperEngine, et prévoir import de modèles gguf (écran déjà présent)
github.com
.

LLM local avancé : remplacer le résumé naif par un petit modèle (LLama‑3 ou Phi‑3) pour actions/décisions plus précises.

Support multilingue (EN/FR) et traduction ; entités et relations métiers (CRM/dossiers patients) prévues pour V2.

Audit trail avancé : journal signé (hash SHA‑256 + horodatage) garantissant l’intégrité des données
github.com
.

Partage local (AirDrop/USB), intégration Nextcloud offline ou export vers outils métiers.

✉️ Prompt proposé

Objet : Définition des actions pour un MVP testable d’OfflineHQASR
Contexte : Le repo OfflineHQASR (v0.6.0) est une appli Android (Kotlin) 100 % hors‑ligne pour enregistrer, transcrire (Vosk par défaut, Whisper optionnel), résumer et organiser des conversations audio. La documentation (docs/Product Requirements, Enhancement Plan, Recording Pipeline) fixe des exigences claires pour le MVP : enregistrement WAV 48 kHz stéréo, normalisation et réduction de bruit RNNoise, transcriptions segmentées, résumé JSON global, stockage chiffré AES‑GCM/SQLCipher, UI simple (liste des sessions, détail avec export), import de modèles via SAF, et test couvrant les cas d’erreurs (perte de micro, conflits, batterie).
Demande : Peux‑tu :

Revoir l’ensemble du code et des docs pour confirmer quelles fonctionnalités sont déjà opérationnelles (enregistrement, VoskEngine, WorkManager, Résumé naïf, UI de base, import modèle, export).

Identifier les tâches prioritaires pour un MVP testable dans Android Studio en suivant les exigences ci‑dessus : finaliser RecordService.start/stop, implémenter la sélection dynamique du micro et la chaîne de traitement audio (normalisation, RNNoise, VAD), fiabiliser la transcription Vosk + segmentation, générer le résumé structuré, sécuriser l’encryption et le stockage, compléter l’UI (écran Détail, filtres/recherche FTS), gérer l’import de modèles et la gestion des erreurs.

Lister les améliorations à planifier ensuite (mode arrière‑plan, intégration Whisper JNI, LLM local avancé, multilingue, entités métiers, audit trail étendu, partage local, UI avancée).

Prioriser ces actions en indiquant ce qui est indispensable pour la release MVP et ce qui peut être reporté en phase 2+.

Cette formulation devrait permettre au tech‑lead de cibler rapidement les éléments clés du dépôt et de structurer un plan d’actions clair pour livrer un MVP opérationnel, puis de planifier les évolutions ultérieures.
