[Figma Prototype 2](https://www.figma.com/design/ApJM9mCpNewdokuCuSq0y2/LOST-Coding-Helper---Mobile-UI?node-id=0-1&t=2Q4GYt9FjX6UUCa2-1)

**Presentation video:** [LOST — CSCE 247 group presentation](https://www.youtube.com/watch?v=pn7DI63_Z9E)

---

# LOST (Lord of the Strings) Coding Helper

A simplified LeetCode-style app for practicing coding problems, tracking progress, and interacting with solutions. Built for **CSCE 247-002 – Software Engineering**.

The goal was to design and model a structured software system using object-oriented principles and design patterns from the course.

---

## What you can do

- Create an account and log in
- Browse and search coding questions
- Submit solutions
- Comment on solutions
- Track completed problems and progress
- Earn achievements
- View the leaderboard

---

## Design & architecture

The system is modeled in UML with core classes such as **User**, **Question**, **Solution**, **Comment**, and **ProgressTracker**. A **Facade** (`ProblemApplication`) coordinates the main functionality.

Concepts we focused on:

- Object-oriented design
- Separation of concerns
- Facade design pattern
- UML modeling
- System architecture planning

LOST is meant to be a lightweight coding-practice platform that demonstrates clear software design and organization.

---

## Data persistence (JSON)

Data is consolidated into two files, aligned with the UML and **DataLoader** / **DataWriter** channels:

- **`json/users.json`** – User accounts, achievement definitions, progress trackers, and leaderboard. In each user, `achievements` and `progressTracker` are stored as reference IDs (`achievementIds`, `progressTrackerId`); `favoriteProblems` remains a list of question IDs.
- **`json/questions.json`** – All questions with their solutions, comments, and attachments embedded, plus supported languages. One load provides full question data for the app.
