# Cashwind Bug & Improvement Tracker

**Last Updated**: Dec 31, 2025  
**Version**: 1.0.0 (stable on phone)

---

## Format
- **[BUG]** - Issue/crash that needs fixing
- **[SUGGESTION]** - Feature improvement or enhancement
- **[UI/UX]** - Visual or usability improvement
- **Status**: `New` | `In Progress` | `Fixed` | `Closed`

---

## Active Issues

### [SUGGESTION] Search Feature - Re-implement with proper debugging
- **Description**: Search feature was implemented but crashes when typing in EditText. Disabled in v1.0.0.
- **Severity**: Medium
- **Status**: `New`
- **Notes**: Attempted multiple approaches (Flow, suspend functions, debouncing). Needs logcat debugging to identify root cause.
- **Version Introduced**: During development (pre-1.0.0)

### [SUGGESTION] Calendar View - Fix black screen issue
- **Description**: Calendar feature was implemented but displays only a black screen. Disabled in v1.0.0.
- **Severity**: Medium
- **Status**: `New`
- **Notes**: Dark mode theme/color resolution issues despite multiple layout and color fixes. Calendar grid not rendering.
- **Version Introduced**: During development (pre-1.0.0)

### [SUGGESTION] CSV Export Feature - Fix crashes
- **Description**: Export feature crashes on button press despite button visibility fixes. Disabled in v1.0.0.
- **Severity**: Medium
- **Status**: `New`
- **Notes**: Changed to MaterialButton, used suspend DAO functions, but still crashes. Needs logcat debugging.
- **Version Introduced**: During development (pre-1.0.0)

---

## Suggestions & Improvements

### [UI/UX] Dark Mode Visual Issues
- **Description**: Several features show visibility/contrast issues in dark mode. Text colors and theme attributes not resolving properly.
- **Severity**: High
- **Status**: `New`
- **Affects**: Search, Calendar, Export, possibly other screens
- **Notes**: Consider implementing explicit color overrides instead of relying on theme attributes.

### [SUGGESTION] Bill Payment Link Navigation
- **Description**: Bills support web payment links. Consider adding link preview or confirmation dialog before opening in browser.
- **Severity**: Low
- **Status**: `New`
- **Notes**: Current behavior: tap webLink → opens browser directly. Could improve UX with confirmation.

### [SUGGESTION] Transaction Tagging System
- **Description**: Add ability to tag transactions (e.g., "business", "personal", "tax-deductible").
- **Severity**: Low
- **Status**: `New`
- **Notes**: Would help with filtering and categorization.

### [UI/UX] Notification Customization
- **Description**: Add UI to customize bill reminder notification settings (days before due date, frequency).
- **Severity**: Medium
- **Status**: `New`
- **Current**: Hard-coded 24-hour check interval with 1/3/5/7/14 day options
- **Notes**: Could be improved with per-bill reminder customization screen.

### [SUGGESTION] Account Transaction History
- **Description**: Show transaction history for each account (tapping account → recent transactions for that account).
- **Severity**: Medium
- **Status**: `New`
- **Notes**: Currently accounts show balance but no transaction details.

### [SUGGESTION] Recurring Bill Improvements
- **Description**: Auto-generate recurring bills, skip/reschedule recurring instances, handle bill sequence history.
- **Severity**: Medium
- **Status**: `New`
- **Notes**: Currently supports marking bills as recurring but no auto-generation or sequencing.

### [SUGGESTION] Budget Alerts
- **Description**: Show warning when spending approaches budget limit (e.g., at 75%, 90%, 100%).
- **Severity**: Medium
- **Status**: `New`
- **Notes**: Budgets exist but no spending alerts or progress notifications.

### [SUGGESTION] Goal Projections
- **Description**: Show projected completion date based on current savings rate.
- **Severity**: Low
- **Status**: `New`
- **Notes**: Goals show progress but no AI-based completion projections.

### [SUGGESTION] Offline Data Sync
- **Description**: When backend integration happens, implement offline-first sync (cache local, sync when connected).
- **Severity**: High (for future)
- **Status**: `New`
- **Notes**: Currently local-only. Important for reliability when network is unavailable.

### [UI/UX] Mobile-Optimized Layouts
- **Description**: Review layouts on various phone sizes (small, medium, large screens).
- **Severity**: Medium
- **Status**: `New`
- **Notes**: Currently designed for standard phones. Test on different screen sizes.

### [SUGGESTION] Undo/Redo Functionality
- **Description**: Add undo/redo for bill/account/transaction deletions and edits.
- **Severity**: Low
- **Status**: `New`
- **Notes**: Could prevent accidental data loss.

### [SUGGESTION] Batch Operations
- **Description**: Mark multiple bills as paid, delete multiple transactions, etc.
- **Severity**: Low
- **Status**: `New`
- **Notes**: Currently must handle items one-at-a-time.

### [UI/UX] Loading States
- **Description**: Add loading indicators/spinners for database operations on slower phones.
- **Severity**: Low
- **Status**: `New`
- **Notes**: Currently operations are instantaneous but could improve perceived responsiveness.

### [SUGGESTION] Data Export (Non-CSV)
- **Description**: Export as PDF reports, JSON backup, or other formats.
- **Severity**: Low
- **Status**: `New`
- **Notes**: CSV export attempted but currently disabled. Other formats would be valuable.

### [SUGGESTION] Multi-User Support
- **Description**: Allow multiple users per device with separate profiles.
- **Severity**: Low
- **Status**: `New`
- **Notes**: Currently hard-coded userId=1. Multi-user would support family/shared phones.

---

## Fixed Issues (v1.0.0)

### [BUG] Delete Button Smooshed on Bill Edit Page
- **Status**: `Fixed` ✅
- **Fix**: Changed layout_width from wrap_content to 0dp with layout_weight=1 for proper spacing
- **Version Fixed**: 1.0.1
- **Affected File**: activity_bill_detail.xml

### [BUG] Bill Status Toggle Crash
- **Status**: `Fixed` ✅
- **Fix**: Ensured callbacks run on main thread
- **Version Fixed**: 1.0.0

### [BUG] Date Parsing in Calendar
- **Status**: `Fixed` ✅
- **Fix**: Handled String format "MM/dd/yyyy" instead of assuming Long timestamp
- **Version Fixed**: Pre-1.0.0

---

## Testing Checklist

Use this when testing on your phone:

- [ ] Bills: Create, edit, delete, mark paid/unpaid
- [ ] Accounts: Create, edit, delete, filter by type
- [ ] Transactions: View, verify amounts and dates
- [ ] Budgets: Create, view spending vs. budget
- [ ] Goals: Create, track progress
- [ ] Paychecks: Create, allocate to goals
- [ ] Analytics: View summary, category breakdown
- [ ] Dashboard: Navigate between screens
- [ ] Notifications: Receive bill reminders (check notification panel)
- [ ] Dark mode: Test visibility of all text/buttons
- [ ] Landscape mode: Test layout rotation
- [ ] Small/large screens: Test layout scaling

---

## How to Report Issues

When testing, note:
1. **Steps to reproduce** - Exact actions that trigger the issue
2. **Expected behavior** - What should happen
3. **Actual behavior** - What actually happens
4. **Device info** - Phone model, Android version
5. **Screenshots/video** - If applicable

Add findings to this file in the "Active Issues" section with `[BUG]` prefix.

---

## Priority Queue for Next Version (v1.1.0)

1. Fix dark mode visibility issues (theme attributes)
2. Re-implement search with logcat debugging
3. Fix calendar black screen
4. Fix CSV export crashes
5. Add account transaction history
6. Improve recurring bill handling
7. Add budget alerts

