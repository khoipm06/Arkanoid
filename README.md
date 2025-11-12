# Arkanoid Game - Object-Oriented Programming Project

## Author
Group 18 - Class 2526I_INT2204_6
1. Phạm Minh Khởi - 24020186
2. Trần Văn Thạo - 24020312
3. Phạm Đức Mạnh - 24020222

**Instructor**: [Kiều Văn Tuyên]  
**Semester**: [HK1 - 2025/2026]

---

## Description
This is a classic Arkanoid game developed in Java as a final project for Object-Oriented Programming course. The project demonstrates the implementation of OOP principles and design patterns.

**Key features:**
1. The game is developed using Java 21 with JavaFX for GUI.
2. Implements core OOP principles: Encapsulation, Inheritance, Polymorphism, and Abstraction.
3. Includes sound effects, animations, and power-up systems.
. Supports save/load game functionality and leaderboard system.

**Game mechanics:**
- Control a paddle to bounce a ball and destroy bricks
- Collect power-ups for special abilities
- Progress through multiple levels with increasing difficulty
- Score points and compete on the leaderboard

---

## UML Diagram

### Class Diagram
![Class Diagram](docs/uml/class-diagram.png)

---

## Design Patterns Implementation

### 1. Singleton Pattern
**Used in:** `GameManager`, `AudioManager`, `ResourceLoader`

**Purpose:** Ensure only one instance exists throughout the application.

---

## Multithreading Implementation

The game uses multiple threads to ensure smooth performance:

1. **Game Loop Thread**: Updates game logic at 60 FPS
2. **Rendering Thread**: Handles graphics rendering (EDT for JavaFX Application Thread)
3. **Audio Thread Pool**: Plays sound effects asynchronously
4. **I/O Thread**: Handles save/load operations without blocking UI

---

## Installation

1. Clone the project from the repository.
2. Open the project in the IDE.
3. Run the project.

## Usage

### Controls
| Key | Action |
|-----|--------|
| `←` | Move paddle left |
| `→` | Move paddle right |
| `SPACE` | Launch ball |
| `ESC` | Pause game |

### How to Play
1. **Start the game**: Click "New Game" from the main menu.
2. **Control the paddle**: Use arrow keys to move left and right.
3. **Launch the ball**: Press SPACE to launch the ball from the paddle.
4. **Destroy bricks**: Bounce the ball to hit and destroy bricks.
5. **Collect power-ups**: Catch falling power-ups for special abilities.
6. **Avoid losing the ball**: Keep the ball from falling below the paddle.
7. **Complete the level**: Destroy all destructible bricks to advance.

### Power-ups
| Icon | Name | Effect |
|------|------|--------|
| 🟦 | Expand Paddle | Increases paddle |
| ⚡ | Thunder | A lightning bolt destroys bricks |
| 🔫 |  Gun | Shoot lasers to destroy bricks |
| 💥 | Explosion Ball | Causes a small explosion |

### Brick
- Normal Brick: Breaks after one hit.
- Strong Brick: Breaks after three hit.
- Moving Brick: Brick can move.
- Unbreakable Brick: Cannot be destroyed by normal means.

---

## Demo

### Screenshots

**Main Menu**  
![mainmenu](https://github.com/user-attachments/assets/03062118-272d-4e09-8642-eb284551d9d5)

**Gameplay**  
![gameplay](https://github.com/user-attachments/assets/ebfb0e84-b8e4-4d04-8fbd-5341bfca6724)

**Leaderboard**  
![leader board](https://github.com/user-attachments/assets/b6d1eefe-98a4-44b8-9315-3f969e8ef201)

---

## Future Improvements

### Planned Features
1. **Additional game modes**
   - Time attack mode
   - Survival mode with endless levels

2. **Enhanced gameplay**
   - Boss battles at end of worlds
   - More power-up varieties (freeze time, shield wall, etc.)
   - Achievements system

3. **Technical improvements**
   - Migrate to LibGDX or JavaFX for better graphics
   - Add particle effects and advanced animations
   - Implement AI opponent mode
   - Add online leaderboard with database backend

---

## Technologies Used

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| JavaFX | 19.0.2 | GUI framework |
| Gradle | 9.0.1 | Build tool |

---

## Notes

- The game was developed as part of the Object-Oriented Programming with Java course curriculum.
- All code is written by group members with guidance from the instructor.
- Some assets (images, sounds) may be used for educational purposes under fair use.
- The project demonstrates practical application of OOP concepts and design patterns.

---
