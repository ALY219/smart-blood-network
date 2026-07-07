# Smart Blood & Emergency Donor Network

A production-ready, location-based mobile application designed to bridge the gap between blood donors, patients, and healthcare facilities during critical medical emergencies. Built with React Native, TypeScript, and Firebase, the platform leverages real-time geospatial filtering and instant synchronization to locate and mobilize eligible blood donors within seconds.

---

## 🚀 Core Features

### 1. Secure Authentication & Role Management
* **Dual-Role Onboarding:** Seamless registration workflows for **Donors** and **Patients/Hospitals** with distinct data profiles.
* **Profile Control:** Global state tracking for real-time donor availability toggles and last-donation tracking.

### 2. Live Geospatial Donor Discovery & Mapping
* **Real-Time Map Overlays:** Seamless integration of `react-native-maps` and `expo-location` to map live coordinates.
* **Geospatial Distance Filtering:** Patients can instantly visualize nearby available blood donors and medical facilities filtered by precise radial distance.
* **Custom Map Visuals:** Features custom, high-contrast markers separating urgent emergency blood requests (pulsing indicators) from blood banks and verified hospitals.

### 3. Emergency Broadcasting System
* **Instant Requests:** High-urgency form allowing patients to broadcast immediate requests specifying blood group, target hospital, required units, and coordinates.
* **Real-Time Distribution:** Utilizes Firestore snapshot listeners to instantly project active emergency cards onto nearby donor dashboards without requiring manual pull-to-refresh actions.

### 4. Interactive Medical Eligibility Checker
* **Smart Screening:** A modular, step-by-step screening form evaluating user eligibility based on clinical health criteria, recent procedures, and time elapsed since the last donation.
* **Visual Status Engine:** Dynamically calculates eligibility state, displaying an intuitive medical pass/fail clearance indicator.

### 5. Secure QR Code Identification
* **Digital Donor Passes:** Generates dynamic, high-fidelity secure QR codes embedded directly into the user’s profile using `react-native-qrcode-svg`.
* **Instant Verification:** Enables immediate terminal/hospital scanning for rapid authentication and verification of a donor’s active status and medical history.

---

## 🛠️ Tech Stack & Architecture

### Frontend
* **Framework:** React Native (Expo Workflow)
* **Language:** TypeScript (Strictly Typed)
* **Navigation:** React Navigation v6 (Nested Stack & Bottom Tabs)
* **State Management:** Zustand (Highly optimized, atomic global state)

### Backend & Infrastructure
* **Database & Auth:** Firebase Firestore (Real-time NoSQL) & Firebase Authentication
* **Cloud Storage:** Firebase Storage (For digital asset management)
* **Geospatial Engine:** Real-time latitude/longitude bounding-box queries

---

## 📂 System Architecture & Folder Structure

The project follows a clean, modular architecture decoupled by layer responsibility to ensure extreme scalability and testing readiness:

```text
src/
├── components/     # Reusable UI Atoms (Custom High-Contrast Inputs, AppLogo)
├── navigation/     # Centralized Navigation Hub & Route Configuration
├── screens/        # Feature Modules (Auth, Dashboard, Maps, Medical, Profile)
├── services/       # Firebase Initialization & API Management Layer
├── store/          # Zustand State Engines (Global Auth & Emergency State)
└── utils/          # Location Helpers, Mock-Data Seeders & Validation Schemas
