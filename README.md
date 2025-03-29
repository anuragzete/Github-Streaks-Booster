# GitHub Streak Booster 🚀

## 🌟 Introduction
GitHub Streak Booster is an automated script that ensures your GitHub contributions remain active by making periodic commits. The program:
- Writes timestamps in a text file.
- Executes Git commands (`add`, `commit`, `push`).
- Checks for internet connectivity and pushes changes when online.
- Runs every hour and stops after successfully pushing three times.
- Retries twice on failure before pausing for an hour.
- Runs as a Windows service for automation.
- Logs all activity in `logRecords.log`.

## 🔍 Documentation
👉 **[View Javadocs](https://anuragzete.github.io/Github-Streaks-Booster/StreakBooster.html)** 📚

## ✨ Features
- 🕒 **Automated GitHub Contributions**
- 🚀 **Retry Mechanism for Failures**
- 🌍 **Internet Connectivity Check**
- 🏗 **Runs as a Windows Service**
- 📜 **Detailed Logging System**

## 📸 Screenshots
### 1️⃣ Log Records
![Log Records](https://github.com/anuragzete/Github-Streaks-Booster/blob/main/Project_Assets/Screenshot%202025-02-21%20214539.png?raw=true)

### 2️⃣ Git Commit History
![Git Commit History](https://github.com/anuragzete/Github-Streaks-Booster/blob/main/Project_Assets/Screenshot%202025-02-21%20214605.png?raw=true)

## 🏗 Tech Stack
- **Language:** Java
- **Version Control:** Git
- **Automation:** Windows Service
- **Logging:** File-based logs (`records.txt` and `logRecords.log`)

## 📖 Installation & Usage Instructions
For detailed installation and usage instructions, check out my blog post:

[🔗 Read the full guide here](YOUR_BLOG_LINK_HERE)

## 🔧 Configuration
- Update Git credentials using `git config --global user.name "your-username"`.
- Modify retry logic and execution interval in `StreakBooster.java`.
- Ensure logs are written to `logRecords.log`.

## 🛠 Future Enhancements
- ✅ Cross-platform support (Linux/macOS)
- ✅ Improved scheduling options
- ✅ GUI for monitoring activity

## 📜 License
This project is **open-source** under the MIT License.

---

### 🔗 Connect with Me
[![LinkedIn](https://img.shields.io/static/v1?message=LinkedIn&logo=linkedin&label=&color=0077B5&logoColor=white&labelColor=&style=for-the-badge)](https://www.linkedin.com/in/anurag-zete-java-developer) [![Outlook](https://img.shields.io/static/v1?message=Outlook&logo=microsoft-outlook&label=&color=0078D4&logoColor=white&labelColor=&style=for-the-badge)](mailto:anuragzete27@outlook.com)


