# AquaTrace - Water Leak Detection

A software-only water leak detection app that finds possible leaks
using only monthly water bill data. No smart meters or hardware needed.

## Features
- Login screen (demo user: demo / aquatrace123)
- Bill entry: add, edit and delete monthly bills, plus bulk entry for a full year
- Year-over-year flow chart comparing current and previous year
- History view of all years with annual totals
- Leak detection: compares each month with the same month last year
  and the trailing 3-month average
- Risk levels: LOW (under 15%), MEDIUM (15-30%), HIGH (over 30%)
- Impact card: extra liters, estimated extra cost and CO2
- Clarifying questions (new occupants, appliances, away) that
  downgrade the risk level
- Pre-loaded demo data with a visible leak

## Tech Stack
- Java
- JavaFX
- SQLite (JDBC)
- Maven

## How to Run
1. Install JDK 21 (Temurin) and IntelliJ IDEA
2. git clone https://github.com/Tharanigasri/AquaTrace-LeakDetection.git
3. Open the folder in IntelliJ and wait for Maven to load
4. Run the `Launcher` class (com.aquatrace.ui.Launcher)
   - Or, if Maven is installed: mvn javafx:run
5. Log in with: demo / aquatrace123

The database (aquatrace.db) is created and filled with demo data automatically on first run.

## Author
Tharanigasri
