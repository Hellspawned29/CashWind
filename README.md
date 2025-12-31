# Cashwind

An Android native app (Kotlin + Room database) to manage bills and accounts locally.

## Overview
- **Language**: Kotlin
- **UI**: Android Views + RecyclerView, DataBinding
- **Database**: Room ORM (SQLite) with LiveData
- **Architecture**: MVVM (ViewModel + LiveData)
- **Features**: Bills CRUD, recurrence, status toggle, sort/filter, totals; Accounts CRUD with balances

## Build & Run

### Prerequisites
- Android Studio (2022+) with Android SDK & Gradle
- JDK 11+
- Emulator or physical Android device

### Build
```bash
cd android
./gradlew build          # Build release & debug APK
./gradlew installDebug   # Install on connected device/emulator
```

The debug APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Features Implemented
- **Bills**: Add/edit/delete, due date tracking, paid/unpaid toggle, recurring options (weekly, biweekly, monthly, etc.), sort by due date/amount/name, filter by status, totals dashboard
- **Accounts**: Add/edit/delete, balance tracking, account type selector (Checking, Savings, Credit Card, Cash, Loan, Investment), credit limit, APR, minimum payment, due day meta fields, type filter, total balance display
- **Local-Only**: All data stored locally in Room database; no backend sync yet

## Project Structure
```
android/app/src/main/
├── java/com/cashwind/app/
│   ├── MainActivity.kt                 # Bills list (launcher)
│   ├── BillDetailActivity.kt           # Bill detail & recurrence display
│   ├── AddBillActivity.kt              # Bill add/edit
│   ├── AccountsActivity.kt             # Accounts list with filter
│   ├── AddAccountActivity.kt           # Account add/edit
│   ├── database/
│   │   ├── CashwindDatabase.kt         # Room database
│   │   ├── entity/Entities.kt          # UserEntity, BillEntity, AccountEntity, etc.
│   │   └── dao/DAOs.kt                 # DAOs for all entities
│   ├── model/
│   │   ├── Financials.kt               # Bill, Account, Transaction models
│   │   ├── Auth.kt                     # User model
│   │   └── Goals.kt                    # Goal model
│   └── ui/
│       ├── MainViewModel.kt            # Bill list logic (sort, filter, totals)
│       ├── AddBillViewModel.kt         # Bill save/update
│       ├── BillDetailViewModel.kt      # Bill detail UI
│       ├── BillAdapter.kt              # Bill RecyclerView adapter
│       ├── AccountsViewModel.kt        # Accounts list logic
│       ├── AddAccountViewModel.kt      # Account save/update
│       └── AccountAdapter.kt           # Account RecyclerView adapter
└── res/
    ├── layout/
    │   ├── activity_main.xml           # Bills list UI
    │   ├── activity_bill_detail.xml    # Bill detail form
    │   ├── activity_add_bill.xml       # Bill add/edit form
    │   ├── activity_accounts.xml       # Accounts list UI
    │   ├── activity_add_account.xml    # Account add/edit form
    │   ├── item_bill.xml               # Bill list row
    │   └── item_account.xml            # Account list row
    └── values/
        ├── arrays.xml                  # Spinner options (sort, status filters, frequencies, account types)
        └── themes.xml
```

## Navigation
- **MainActivity** (launcher): Bills list with totals, sort, status filter, "Add Bill" button
  - "+ Add Bill" → AddBillActivity
  - "Accounts" button → AccountsActivity
  - Tap bill row → BillDetailActivity
  - Click status → toggle paid/unpaid
  - Long-press row → delete
- **AccountsActivity**: Accounts list with type filter and total balance
  - "+ Add" button → AddAccountActivity
  - "Back" button → MainActivity
  - Tap account row → AddAccountActivity (edit mode)
  - Long-press row → delete

## Database
- **Version**: 3 (with `fallbackToDestructiveMigration`)
- **Tables**: users, bills, accounts, transactions, budgets, goals
- **Default User**: id=1 (no authentication yet)

## Recent Changes
- Added Accounts feature: CRUD, filter by type, total balance calculation
- Added recurrence to bills: frequency options stored in DB and UI
- Implemented sort/filter/totals on main screen
- Fixed paid/unpaid toggle crash by ensuring callbacks run on main thread
- Database version bumped to 3 (data reset on migration)

## Next Steps
- Account detail screen (balance history, recent transactions)
- Transactions per account
- Budget management and goals tracking
- Reminders/notifications
- Sync to backend API (Prosper)
- Authentication
