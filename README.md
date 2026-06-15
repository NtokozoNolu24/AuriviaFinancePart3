
# 💰 AuriviaFinance - Personal Finance Tracker

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white)

A comprehensive Android application built with Kotlin that helps users take control of their finances by tracking expenses, managing budgets, and gaining valuable spending insights through gamification and analytics.

## 📱 Overview

AuriviaFinance is a feature-rich personal finance management app designed to help users monitor their spending habits, set budgets, and achieve financial goals. The app combines essential expense tracking with gamification elements to make financial management engaging and rewarding.

**GitHub Repository:** [AuriviaFinance1](https://github.com/NtokozoNolu24/AuriviaFinance1.git)  
**Demo Video:** [Watch on YouTube](https://youtu.be/MMKG_7ckF8k?feature=shared)

## 👥 Team Structure

This project was developed collaboratively using a role-based approach:

| Team Member | Role | Responsibilities |
|------------|------|-----------------|
| **Thato Menetje (ST10221273)** | UI & Navigation | • Login & Registration screens<br>• Navigation between fragments<br>• Dashboard, Expenses, Categories, Reports UI<br>• UI design and layout consistency |
| **Ntokozo Mashiane (ST10455454)** | Database & Data Management | • Implemented SQLite Database<br>• Created data models and helpers<br>• Managed data persistence for users, categories, expenses, and income<br>• Implemented CRUD operations |
| **Thobeka Sithole (ST10456076)** | Reports & Analytics | • Spending analysis and insights<br>• Budget tracking implementation<br>• Reports generation and visualization<br>• Category-based summaries |

## 🚀 Key Features

### Core Functionality
- ✅ **User Authentication** - Secure registration and login system
- ✅ **Expense Tracking** - Add, edit, and delete expenses with category selection
- ✅ **Income Management** - Track multiple income sources
- ✅ **Category Management** - Customize expense categories with color coding
- ✅ **Budget Setting** - Set monthly budgets and track progress

### Analytics & Insights
- 📊 **Real-time Balance Calculation** - Income vs Expenses
- 📈 **Category-based Spending Analysis** - Visual breakdown of expenses
- 💰 **Budget Tracking** - Monitor spending against budget limits
- 📅 **Period-based Reports** - Filter expenses by date ranges

### Gamification Features
- 🏆 **Points System** - Earn points for financial activities
- 📈 **Level Progression** - Advance through 6 levels from "Budget Beginner" to "Budget Legend"
- 🔥 **Daily Streaks** - Maintain consecutive days of activity
- 🎖️ **Achievements** - Unlock milestones for financial goals:
  - "Big Spender" - R1000+ total expenses
  - "High Earner" - R5000+ total income
  - "On Fire!" - Reach Level 3
  - And more!

## 🛠️ Technologies Used

### Core Technologies
- **Kotlin** - Primary programming language
- **Android SDK** - Android development framework
- **SQLite** - Local database for data persistence
- **Material Design Components** - Modern UI components

### Architecture & Libraries
- **View Binding** - Type-safe view access
- **Navigation Component** - Fragment navigation
- **RecyclerView** - Efficient list displays
- **Coroutines** - Background operations
- **AndroidX** - Modern Android libraries

## 📁 Project Structure

```
app/src/main/java/com/example/open_sourcepart2/
├── activities/
│   ├── LoginActivity.kt
│   └── MainActivity.kt
├── fragments/
│   ├── DashboardFragment.kt
│   ├── ExpensesFragment.kt
│   ├── CategoriesFragment.kt
│   ├── ReportsFragment.kt
│   ├── InsightsFragment.kt
│   ├── GamificationFragment.kt
│   └── ProfileFragment.kt
├── adapters/
│   ├── ExpenseAdapter.kt
│   ├── CategoryAdapter.kt
│   └── RecentExpenseAdapter.kt
├── database/
│   ├── DatabaseHelper.kt
│   └── models/
│       ├── User.kt
│       ├── Category.kt
│       ├── Expense.kt
│       ├── Income.kt
│       └── Budget.kt
├── managers/
│   ├── SessionManager.kt
│   └── GamificationManager.kt
└── utils/
    └── DateFormatter.kt
```

## 🗄️ Database Schema

### Tables Structure

#### Users Table
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    email TEXT UNIQUE,
    password TEXT
);
```

#### Categories Table
```sql
CREATE TABLE categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    color TEXT,
    budget REAL,
    user_id INTEGER,
    FOREIGN KEY(user_id) REFERENCES users(id)
);
```

#### Expenses Table
```sql
CREATE TABLE expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount REAL,
    description TEXT,
    date TEXT,
    category_id INTEGER,
    user_id INTEGER,
    image_path TEXT,
    FOREIGN KEY(category_id) REFERENCES categories(id),
    FOREIGN KEY(user_id) REFERENCES users(id)
);
```

#### Income Table
```sql
CREATE TABLE income (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount REAL NOT NULL,
    source TEXT NOT NULL,
    note TEXT,
    date TEXT NOT NULL,
    user_id INTEGER NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id)
);
```

#### Budgets Table
```sql
CREATE TABLE budgets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount REAL,
    period TEXT,
    start_date TEXT,
    end_date TEXT,
    user_id INTEGER,
    FOREIGN KEY(user_id) REFERENCES users(id)
);
```

## 🎮 Gamification System

The app includes a comprehensive gamification system to encourage consistent financial tracking:

| Level | Title | Points Required |
|-------|-------|----------------|
| 1 | Budget Beginner | 0-99 |
| 2 | Saving Scout | 100-249 |
| 3 | Finance Fighter | 250-499 |
| 4 | Money Master | 500-999 |
| 5 | Wealth Warrior | 1000-1999 |
| 6 | Budget Legend | 2000+ |

### Points System
- **Add Expense:** +10 points
- **Add Income:** +5 points
- **Add Category:** +15 points
- **Daily Login:** +20 points
- **7-Day Streak:** +100 bonus points
- **30-Day Streak:** +500 bonus points

## 🚦 Setup Instructions

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- Android SDK API 24 or higher
- JDK 8 or later

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/NtokozoNolu24/AuriviaFinance1.git
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Click "Sync Now" when prompted
   - Wait for dependencies to download

4. **Run the application**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green triangle)

### Build Configuration
- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 12 (API 31)
- **Compile SDK:** API 31
- **Language:** Kotlin 1.8+

## 📱 Usage Guide

### First Time Setup
1. Launch the app
2. Click "Register" to create a new account
3. Enter your name, email, and password
4. Login with your credentials

### Adding Expenses
1. Navigate to the Expenses tab
2. Click the "Add Expense" button
3. Enter amount, description, and select category
4. Save the expense

### Managing Categories
1. Go to Categories tab
2. Add new categories with custom colors
3. Set budget limits for each category

### Tracking Income
1. From the Dashboard, click "Add Income"
2. Enter amount and source
3. Add optional notes

### Viewing Reports
1. Navigate to Reports/Insights tab
2. View spending breakdown by category
3. Check budget progress
4. Analyze income vs expenses

## 🎯 Features in Detail

### Authentication Flow
- User registers with email and password
- Credentials stored securely in local database
- Session management for persistent login
- Logout functionality with session cleanup

### Dashboard
- Real-time balance calculation (Income - Expenses)
- Recent expenses list with category colors
- Quick actions for adding expenses/income
- Gamification stats display (points, level, streak)

### Expense Management
- Add expenses with amount, description, and category
- Edit existing expenses
- Delete unwanted entries
- View expense history with date sorting
- Optional image attachment for receipts

### Budget Tracking
- Set monthly budgets
- Track spending against budget limits
- Visual progress indicators
- Budget alerts and notifications

### Analytics Engine
- Calculate total spending by category
- Identify spending patterns
- Track saving progress
- Generate spending reports

## 🔄 Future Improvements

### Planned Features
- 📊 **Advanced Charts** - Integration with MPAndroidChart for better visualizations
- ☁️ **Cloud Sync** - Firebase integration for multi-device support
- 👥 **Multi-user Support** - Family/shared accounts
- 📄 **Export Reports** - PDF and CSV export functionality
- 🔔 **Push Notifications** - Budget alerts and reminders
- 💳 **Bank Integration** - Automatic transaction imports
- 🌙 **Dark Mode** - Theme customization
- 🌍 **Multi-language** - Internationalization support

## 🤝 Contribution Guidelines

### Development Workflow
1. **Create a branch** for your feature
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Follow coding conventions**
   - Use meaningful variable names
   - Add comments for complex logic
   - Follow Kotlin coding standards

3. **Test thoroughly** before committing
4. **Write clear commit messages**
5. **Push to GitHub** and create a Pull Request

### Code Review Checklist
- [ ] Code follows project structure
- [ ] No breaking changes
- [ ] Database migrations tested
- [ ] UI works on different screen sizes
- [ ] No memory leaks
- [ ] Edge cases handled

## 📝 Important Notes

- The app uses **SQLite** for local data persistence
- All database operations are handled through DatabaseHelper
- Do NOT modify database schema without team consensus
- Test all changes on multiple API levels
- Keep UI consistent with Material Design guidelines

## 🐛 Troubleshooting

### Common Issues

**App won't compile**
- Ensure Android Studio is updated
- Clean and rebuild project: `Build > Clean Project`, then `Rebuild Project`
- Invalidate caches: `File > Invalidate Caches and Restart`

**Database errors**
- Clear app data: `Settings > Apps > AuriviaFinance > Clear Data`
- Uninstall and reinstall the app
- Check Logcat for specific error messages

**Login issues**
- Verify credentials
- Check if user exists in database
- Clear app cache and try again

## 📄 License

This project is developed for educational purposes as part of academic coursework.

## 🙏 Acknowledgments

- Material Design guidelines for UI inspiration
- Android documentation for best practices
- Open source community for libraries and tools

## 📧 Contact

For questions or support, please contact the team members:

- **Thato Menetje** - UI/Navigation Lead
- **Ntokozo Mashiane** - Database Lead
- **Thobeka Sithole** - Analytics Lead

---

**Made with ❤️ by Team AuriviaFinance**

*Empowering financial freedom through technology*
```
