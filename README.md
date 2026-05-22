Touch Typer: Material You Dynamic Colors
A comprehensive Android application for touch typing training, developed using Jetpack Compose. The app combines interactive trainers, game mechanics, an achievement system, and competitive elements, functioning offline thanks to a local database. All interface elements and content are implemented in English.

🚀 Core Features
Learning & Training
Level Progression: Structured stages for gradual layout learning (Home Row, Top Row, Bottom Row, Numbers, Symbols).

Endless Mode: Customized sessions to practice weak spots (lowercase/uppercase letters only, numbers, symbols, or custom word lists).

Virtual Keyboard: An interactive UI component that highlights active keys in real-time and demonstrates the correct typing position.

Gaming Center (Mini-Games)
Falling Keys: An arcade mode with vertical scrolling where you must type letters falling at dynamic speeds.

Word Sprint: A speed-run mode with a timer to test reflexes and whole-word typing speed.

Profiles & Social Interaction
Profile Customization: Ability to set a nickname, choose an avatar, fill out the "About me" (bio) section, and add links to social networks (GitHub, LinkedIn, Twitter/X).

Global Leaderboard: A ranking system with sorting by accumulated experience (XP/Level), speed (Average WPM), and accuracy (Accuracy %).

Player Inspection: Ability to view the profiles of other leaderboard participants to analyze their statistics and achievements.

Statistics & Achievements
Detailed Analytics: Tracking of WPM (Words Per Minute), accuracy percentage, and accumulated XP after each session.

Badge System: Dynamic unlocking of achievements based on fulfillment of conditions (e.g., Home Row Graduate for Level 5+, Swift Keys for 50+ WPM, Precision Sniper for 96%+ accuracy).

🎨 Design & Interface (UI/UX)
Material You & Dynamic Colors: The interface automatically adapts to the color palette of the user's wallpaper (on supported devices).

Theming: Support for Elegant Dark (Cosmic Blue/Indigo) and light themes with the ability to force switch or synchronize with the system.

Audio-Visual & Tactile Feedback: Settings for vibration on mistypes and acoustic keyboard clicks to build muscle memory.

🛠 Tech Stack
UI Framework: Jetpack Compose, Material Design 3.

Database: Room Database (Offline-first architecture with relational links to save sessions, users, and metrics).

Architecture & Asynchrony: MVVM, Kotlin Coroutines, Flow (for reactive UI updates and combined leaderboard sorting).
