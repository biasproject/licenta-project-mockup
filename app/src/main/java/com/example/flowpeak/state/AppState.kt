package com.example.flowpeak.state

import android.content.Context
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.UUID

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val xpReward: Int = 100,
    val isCompleted: Boolean = false
)

data class JournalEntryItem(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val text: String
)

data class DayStats(
    val dayName: String,
    val workedMinutes: Float,
    val completedTasks: Float,
    val focusAlerts: Float
)

class AppState(context: Context) {
    private val appContext = context.applicationContext
    val dbHelper = DatabaseHelper(appContext)
    val sharedPrefs = appContext.getSharedPreferences("flowpeak_prefs", Context.MODE_PRIVATE)

    // ── Autentificare ──────────────────────────────────────────
    var isLoggedIn by mutableStateOf(false)
    var username by mutableStateOf("")

    fun loginUser(enteredUsername: String) {
        username = enteredUsername
        isLoggedIn = true
        loadUserSettings(enteredUsername)
        loadUserDataFromDb(enteredUsername)
    }

    fun logoutUser() {
        if (username.isNotBlank()) {
            saveUserSettings(username)
        }
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoggedIn = false
        username = ""
        resetStateToDefaults()
    }

    fun updateSleepHours(hours: Float) {
        sleepHours = hours
        saveUserSettings(username)
    }

    fun updateFoodType(type: String) {
        foodType = type
        saveUserSettings(username)
    }

    fun updateHydrationCups(cups: Int) {
        hydrationCups = cups
        saveUserSettings(username)
    }

    fun toggleDarkMode(dark: Boolean) {
        isDarkMode = dark
        saveUserSettings(username)
    }

    fun toggleThemeAutomatic(auto: Boolean) {
        isThemeAutomatic = auto
        saveUserSettings(username)
    }

    fun updateMaxTimerSeconds(minutes: Int) {
        if (!timerIsRunning && minutes > 0) {
            maxTimerSeconds = minutes * 60
            timerSecondsLeft = maxTimerSeconds
            saveUserSettings(username)
        }
    }

    private fun loadUserSettings(user: String) {
        isDarkMode = sharedPrefs.getBoolean("${user}_isDarkMode", true)
        isThemeAutomatic = sharedPrefs.getBoolean("${user}_isThemeAutomatic", false)
        xp = sharedPrefs.getInt("${user}_xp", 350)
        avatarColorName = sharedPrefs.getString("${user}_avatarColorName", "Grey") ?: "Grey"
        avatarExpression = sharedPrefs.getString("${user}_avatarExpression", "Neutral") ?: "Neutral"
        avatarAccessory = sharedPrefs.getString("${user}_avatarAccessory", "None") ?: "None"
        
        unlockedColors.clear()
        val colorsSet = sharedPrefs.getStringSet("${user}_unlockedColors", setOf("Grey")) ?: setOf("Grey")
        unlockedColors.addAll(colorsSet)
        
        unlockedExpressions.clear()
        val exprsSet = sharedPrefs.getStringSet("${user}_unlockedExpressions", setOf("Neutral")) ?: setOf("Neutral")
        unlockedExpressions.addAll(exprsSet)
        
        unlockedAccessories.clear()
        val accsSet = sharedPrefs.getStringSet("${user}_unlockedAccessories", setOf("None")) ?: setOf("None")
        unlockedAccessories.addAll(accsSet)
        
        sleepHours = sharedPrefs.getFloat("${user}_sleepHours", 7f)
        foodType = sharedPrefs.getString("${user}_foodType", "Normal") ?: "Normal"
        hydrationCups = sharedPrefs.getInt("${user}_hydrationCups", 3)
        trackersCompletedToday = sharedPrefs.getBoolean("${user}_trackersCompletedToday", false)
        
        maxTimerSeconds = sharedPrefs.getInt("${user}_maxTimerSeconds", 25 * 60)
        timerSecondsLeft = maxTimerSeconds
    }

    fun saveUserSettings(user: String) {
        if (user.isBlank()) return
        sharedPrefs.edit().apply {
            putBoolean("${user}_isDarkMode", isDarkMode)
            putBoolean("${user}_isThemeAutomatic", isThemeAutomatic)
            putInt("${user}_xp", xp)
            putString("${user}_avatarColorName", avatarColorName)
            putString("${user}_avatarExpression", avatarExpression)
            putString("${user}_avatarAccessory", avatarAccessory)
            putStringSet("${user}_unlockedColors", unlockedColors.toSet())
            putStringSet("${user}_unlockedExpressions", unlockedExpressions.toSet())
            putStringSet("${user}_unlockedAccessories", unlockedAccessories.toSet())
            putFloat("${user}_sleepHours", sleepHours)
            putString("${user}_foodType", foodType)
            putInt("${user}_hydrationCups", hydrationCups)
            putBoolean("${user}_trackersCompletedToday", trackersCompletedToday)
            putInt("${user}_maxTimerSeconds", maxTimerSeconds)
            apply()
        }
    }

    private fun loadUserDataFromDb(user: String) {
        tasks.clear()
        val dbTasks = dbHelper.getTasks(user)
        if (dbTasks.isEmpty()) {
            val defaultTasks = listOf(
                TaskItem(name = "Analiză cerințe licență", category = "Dev", isCompleted = true),
                TaskItem(name = "Configurare proiect în Android Studio", category = "Setup", isCompleted = true),
                TaskItem(name = "Implementare design sistem cu culori noi", category = "Design", isCompleted = false),
                TaskItem(name = "Testare audio player și timer", category = "QA", isCompleted = false),
                TaskItem(name = "Scriere secțiune metodologie în document", category = "Documentație", isCompleted = false)
            )
            defaultTasks.forEach {
                dbHelper.addTask(user, it)
                tasks.add(it)
            }
        } else {
            tasks.addAll(dbTasks)
        }

        journalEntries.clear()
        val dbJournals = dbHelper.getJournalEntries(user)
        if (dbJournals.isEmpty()) {
            val defaultJournals = listOf(
                JournalEntryItem(date = "Azi — 09:14", text = "Am început ziua cu o sesiune de focus. Mintea este limpede și gata de muncă."),
                JournalEntryItem(date = "Ieri — 17:30", text = "Dificil de menținut atenția din cauza notificărilor. Trebuie să folosesc DND data viitoare.")
            )
            defaultJournals.forEach {
                dbHelper.addJournalEntry(user, it)
                journalEntries.add(it)
            }
        } else {
            journalEntries.addAll(dbJournals.reversed())
        }
    }

    private fun resetStateToDefaults() {
        isDarkMode = true
        isThemeAutomatic = false
        xp = 350
        avatarColorName = "Grey"
        avatarExpression = "Neutral"
        avatarAccessory = "None"
        unlockedColors.clear()
        unlockedColors.add("Grey")
        unlockedExpressions.clear()
        unlockedExpressions.add("Neutral")
        unlockedAccessories.clear()
        unlockedAccessories.add("None")
        tasks.clear()
        journalEntries.clear()
        sleepHours = 7f
        foodType = "Normal"
        hydrationCups = 3
        trackersCompletedToday = false
        maxTimerSeconds = 25 * 60
        timerSecondsLeft = 25 * 60
    }


    // ── Setări Temă ────────────────────────────────────────────
    var isDarkMode by mutableStateOf(true)
    var isThemeAutomatic by mutableStateOf(false)

    fun updateThemeAutomatically() {
        if (isThemeAutomatic) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            isDarkMode = hour >= 18 || hour < 6
        }
    }

    // ── Gamificare & XP ─────────────────────────────────────────
    var xp by mutableIntStateOf(350) // Începem cu 350 XP
    val level: Int get() = (xp / 1000) + 1
    val xpInCurrentLevel: Int get() = xp % 1000

    var avatarColorName by mutableStateOf("Grey")
    var avatarExpression by mutableStateOf("Neutral")
    var avatarAccessory by mutableStateOf("None")

    val unlockedColors = mutableStateListOf("Grey")
    val unlockedExpressions = mutableStateListOf("Neutral")
    val unlockedAccessories = mutableStateListOf("None")

    // ── Task-uri To-Do ──────────────────────────────────────────
    val tasks = mutableStateListOf<TaskItem>()

    fun toggleTask(taskId: String) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val oldTask = tasks[index]
            val newCompleted = !oldTask.isCompleted
            val newTask = oldTask.copy(isCompleted = newCompleted)
            tasks[index] = newTask
            
            // Save to database
            dbHelper.updateTask(newTask)
            
            // Adăugăm/Scădem XP
            if (newCompleted) {
                xp += oldTask.xpReward
            } else {
                xp = (xp - oldTask.xpReward).coerceAtLeast(0)
            }
            saveUserSettings(username)
        }
    }

    fun addTask(name: String, category: String) {
        if (name.isNotBlank()) {
            val newTask = TaskItem(name = name, category = category)
            tasks.add(newTask)
            dbHelper.addTask(username, newTask)
        }
    }

    // ── Jurnal & Tracker ───────────────────────────────────────
    val journalEntries = mutableStateListOf<JournalEntryItem>()

    var sleepHours by mutableFloatStateOf(7f)
    var foodType by mutableStateOf("Normal") // Healthy, Normal, Junk Food
    var hydrationCups by mutableIntStateOf(3)
    var trackersCompletedToday by mutableStateOf(false)

    fun addJournalEntry(text: String) {
        if (text.isNotBlank()) {
            val dateStr = "Azi — %02d:%02d".format(
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE)
            )
            val newEntry = JournalEntryItem(date = dateStr, text = text)
            journalEntries.add(0, newEntry)
            dbHelper.addJournalEntry(username, newEntry)
        }
    }

    fun completeTrackers() {
        if (!trackersCompletedToday) {
            trackersCompletedToday = true
            xp += 150 // Recompensă tracker complet
            saveUserSettings(username)
        }
    }

    // ── Cronometru & Audio ──────────────────────────────────────
    var timerSecondsLeft by mutableIntStateOf(25 * 60)
    var timerIsRunning by mutableStateOf(false)
    var maxTimerSeconds by mutableIntStateOf(25 * 60)

    fun adjustTimer(minutesChange: Int) {
        if (!timerIsRunning) {
            val currentMinutes = maxTimerSeconds / 60
            val newMinutes = (currentMinutes + minutesChange).coerceIn(1, 9999) // No rigid limit, set between 1m and 9999m
            maxTimerSeconds = newMinutes * 60
            timerSecondsLeft = maxTimerSeconds
            saveUserSettings(username)
        }
    }

    var selectedTrackIndex by mutableIntStateOf(0) // 0 = Rock, 1 = Lofi, 2 = White Noise
    var isMusicPlaying by mutableStateOf(false)

    // Focus Check Popup
    var showFocusCheckDialog by mutableStateOf(false)
    var focusCheckSecondsLeft by mutableIntStateOf(60)

    // ── Insights & Date Grafice ────────────────────────────────
    val weeklyStats = listOf(
        DayStats("Lun", workedMinutes = 120f, completedTasks = 4f, focusAlerts = 1f),
        DayStats("Mar", workedMinutes = 180f, completedTasks = 6f, focusAlerts = 0f),
        DayStats("Mie", workedMinutes = 90f, completedTasks = 3f, focusAlerts = 2f),
        DayStats("Joi", workedMinutes = 240f, completedTasks = 8f, focusAlerts = 1f),
        DayStats("Azi", workedMinutes = 150f, completedTasks = 5f, focusAlerts = 3f), // Azi crește dinamic
        DayStats("Sâm", workedMinutes = 0f, completedTasks = 0f, focusAlerts = 0f),
        DayStats("Dum", workedMinutes = 0f, completedTasks = 0f, focusAlerts = 0f)
    )

    var focusAlertsCount by mutableIntStateOf(3)

    fun triggerFocusAlert() {
        focusAlertsCount++
    }

    // Este declarat la final: astfel toate stările Compose sunt inițializate
    // înainte să încărcăm setările unui utilizator autentificat la relansare.
    init {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            loginUser(currentUser.email ?: "default_user")
        }
    }
}
