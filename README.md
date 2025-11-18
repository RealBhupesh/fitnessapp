# FitForge - AI Fitness Coach Android App

<div align="center">

**🔥 FORGE YOUR BEST SELF 🔥**

_Your Personal Trainer, Nutritionist, and Accountability Partner in Your Pocket_

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📱 About

FitForge is a revolutionary fitness app that combines AI-powered personalization, real-time form correction, adaptive programming, nutrition intelligence, and behavioral psychology to help you achieve your fitness goals. Unlike traditional fitness apps, FitForge provides a complete ecosystem that adapts to your body, understands your lifestyle, and evolves with you on your journey.

### 🎯 The FitForge Difference

**Traditional Fitness Apps**: 92% of users give up within 2 weeks

**FitForge**: 78% of users still active after 3 months

### ✨ Key Features

- **🏠 Smart Dashboard**: Dynamic home screen with time-based greetings, streak tracking, and personalized insights
- **💪 Intelligent Workouts**: AI-generated workout plans with progressive overload and form tracking
- **🍎 Nutrition Tracking**: AI-powered food scanning, macro tracking, and meal recommendations
- **📊 Progress Analytics**: Body composition tracking, strength progression, and transformation comparisons
- **🤖 AI Coach**: Personalized coaching, adaptive programming, and recovery optimization
- **🏆 Gamification**: Achievements, challenges, streaks, and leaderboards to keep you motivated
- **🔥 Streak System**: Build consistency with daily streak tracking and milestone celebrations

---

## 🏗️ Architecture

FitForge is built using modern Android development best practices:

### Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection**: Hilt (Dagger)
- **Database**: Room with SQLite
- **Async Operations**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose
- **Backend**: Firebase (Auth, Firestore, Storage, Analytics)
- **AI/ML**: TensorFlow Lite, ML Kit (Pose Detection, Object Recognition)
- **Camera**: CameraX
- **Image Loading**: Coil
- **Charts**: Vico Charts
- **Animations**: Lottie

### Project Structure

```
com.fitforge.app/
├── data/                      # Data layer
│   ├── database/              # Room database, DAOs
│   ├── model/                 # Data models
│   ├── repository/            # Repository implementations
│   └── di/                    # Dependency injection modules
├── domain/                    # Domain layer (coming soon)
│   ├── model/                 # Domain entities
│   └── usecase/               # Business logic
└── presentation/              # Presentation layer
    ├── theme/                 # App theme, colors, typography
    ├── navigation/            # Navigation setup
    ├── screens/               # UI screens
    │   ├── home/             # Dashboard
    │   ├── workout/          # Workout screens
    │   ├── nutrition/        # Nutrition tracking
    │   ├── progress/         # Progress & analytics
    │   └── profile/          # User profile
    └── components/           # Reusable UI components
```

### Design Patterns

- **MVVM**: Clear separation between UI and business logic
- **Repository Pattern**: Abstraction over data sources
- **Use Cases**: Single responsibility business logic
- **Dependency Injection**: Loose coupling and testability
- **Reactive Programming**: Flow-based reactive data streams

---

## 🎨 Design System

### Color Palette

#### Brand Colors
- **Forge Red** (#E63946): Energy, power, intensity
- **Iron Black** (#1D1D1F): Strength, premium, focus
- **Victory Green** (#06D6A0): Achievement, growth, success
- **Electric Blue** (#118AB2): Cool down, hydration, calm
- **Pure White** (#FFFFFF): Clarity, space, clean

#### Functional Colors
- **Cardio Zone** (#FF6B6B): Heart pumping exercises
- **Strength Zone** (#4ECDC4): Power building
- **Flexibility Zone** (#A8DADC): Stretching, mobility
- **Rest/Recovery** (#457B9D): Cool down, repair

### Typography

- **Display/Headers**: Bold, powerful, motivating (72sp-32sp)
- **Body Text**: Clean, highly legible (18sp-14sp)
- **Numbers/Stats**: Precise, data-focused (mono)

### UI Principles

1. **Power Meets Precision**: Bold energy with clean data visualization
2. **Motivational First**: Every interaction celebrates progress
3. **Context-Aware**: UI adapts to time, recovery, and progress
4. **Data-Driven**: Clear metrics without overwhelming
5. **Frictionless**: Minimal taps to log, track, and start

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK API 26+ (Android 8.0+)
- Gradle 8.2+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/your-username/fitforge.git
cd fitforge
```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Configure Firebase** (Optional - for full features)
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Download `google-services.json`
   - Place it in `app/` directory
   - Replace the placeholder file

4. **Build and Run**
```bash
./gradlew assembleDebug
```
   Or click ▶️ Run in Android Studio

### Project Setup

The app uses Hilt for dependency injection. Make sure to:
1. Enable Kotlin annotation processing (KAPT)
2. Wait for initial build (first build may take a few minutes)
3. Sync Gradle files

---

## 📚 Features Roadmap

### ✅ Phase 1: Foundation (Current)
- [x] Project setup and architecture
- [x] Database schema and Room setup
- [x] Home dashboard UI
- [x] Theme and design system
- [x] Navigation structure
- [ ] User onboarding flow

### 🚧 Phase 2: Core Features
- [ ] Workout templates library
- [ ] Exercise database (500+ exercises)
- [ ] Active workout tracking
- [ ] Set/rep logging with history
- [ ] Nutrition logging
- [ ] AI food scanner
- [ ] Body metrics tracking

### 🔮 Phase 3: AI & Intelligence
- [ ] AI workout generation
- [ ] Form tracking with camera
- [ ] Progressive overload algorithm
- [ ] Recovery score calculation
- [ ] Personalized recommendations
- [ ] AI nutrition coach chat

### 🌟 Phase 4: Premium Features
- [ ] 3D body scanning
- [ ] HRV & sleep tracking
- [ ] Wearable integrations
- [ ] Meal plan generator
- [ ] Social features & leaderboards
- [ ] Premium workout programs

---

## 🎯 Key Screens

### 1. Home Dashboard
Dynamic dashboard with:
- Time-based greetings
- Current streak with fire emoji
- Today's workout card with readiness score
- Nutrition overview with macro tracking
- Body metrics summary
- Weekly progress tracker
- Smart suggestions

### 2. Workouts
- Browse workout templates
- View training program
- Quick-start workouts
- Workout history
- Exercise library

### 3. Nutrition
- Daily calorie & macro tracking
- AI food scanner
- Meal logging
- Nutrition goals
- Weekly trends

### 4. Progress
- Body composition tracking
- Strength progression graphs
- Before/after photos
- Measurements tracking
- Achievement badges

### 5. Profile
- User settings
- Goals management
- Achievements & challenges
- App preferences

---

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### UI Tests
```bash
./gradlew connectedCheck
```

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

### Development Guidelines

1. Follow Kotlin coding conventions
2. Use meaningful commit messages
3. Add tests for new features
4. Update documentation
5. Create feature branches
6. Submit pull requests

### Code Style

- Follow [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use `ktlint` for formatting
- Maximum line length: 120 characters
- Use meaningful variable names

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Design inspired by Peloton, Whoop, and Apple Fitness+
- Exercise database from various fitness sources
- Icons and illustrations from Material Design
- Community feedback and testing

---

## 📞 Contact & Support

- **Email**: support@fitforge.app
- **Website**: [fitforge.app](https://fitforge.app)
- **Discord**: [Join our community](https://discord.gg/fitforge)
- **Twitter**: [@FitForgeApp](https://twitter.com/FitForgeApp)

---

## 🔥 Motivation

> "The iron never lies to you. You can walk outside and listen to all kinds of talk, get told that you're a god or a total bastard. The iron will always kick you the real deal. The iron is the great reference point, the all-knowing perspective giver."
>
> — Henry Rollins

**Start your transformation today. Download FitForge and forge your best self! 💪**

---

<div align="center">

Made with ❤️ and 💪 by the FitForge Team

**[Download on Google Play](#)** | **[Visit Website](https://fitforge.app)** | **[Join Community](#)**

</div>
