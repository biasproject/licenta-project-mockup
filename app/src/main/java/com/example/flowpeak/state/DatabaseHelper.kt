package com.example.flowpeak.state

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "flowpeak.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TASKS = "tasks"
        private const val KEY_TASK_ID = "id"
        private const val KEY_TASK_USER = "username"
        private const val KEY_TASK_NAME = "name"
        private const val KEY_TASK_CATEGORY = "category"
        private const val KEY_TASK_XP = "xp_reward"
        private const val KEY_TASK_COMPLETED = "is_completed"

        private const val TABLE_JOURNAL = "journal"
        private const val KEY_JOURNAL_ID = "id"
        private const val KEY_JOURNAL_USER = "username"
        private const val KEY_JOURNAL_DATE = "date"
        private const val KEY_JOURNAL_TEXT = "text"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTasksTable = ("CREATE TABLE " + TABLE_TASKS + "("
                + KEY_TASK_ID + " TEXT PRIMARY KEY,"
                + KEY_TASK_USER + " TEXT,"
                + KEY_TASK_NAME + " TEXT,"
                + KEY_TASK_CATEGORY + " TEXT,"
                + KEY_TASK_XP + " INTEGER,"
                + KEY_TASK_COMPLETED + " INTEGER" + ")")
        db.execSQL(createTasksTable)

        val createJournalTable = ("CREATE TABLE " + TABLE_JOURNAL + "("
                + KEY_JOURNAL_ID + " TEXT PRIMARY KEY,"
                + KEY_JOURNAL_USER + " TEXT,"
                + KEY_JOURNAL_DATE + " TEXT,"
                + KEY_JOURNAL_TEXT + " TEXT" + ")")
        db.execSQL(createJournalTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_JOURNAL")
        onCreate(db)
    }

    // Tasks operations
    fun getTasks(username: String): List<TaskItem> {
        val list = mutableListOf<TaskItem>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_TASKS, null, "$KEY_TASK_USER = ?", arrayOf(username), null, null, null)
        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(KEY_TASK_ID)
            val nameIndex = cursor.getColumnIndex(KEY_TASK_NAME)
            val catIndex = cursor.getColumnIndex(KEY_TASK_CATEGORY)
            val xpIndex = cursor.getColumnIndex(KEY_TASK_XP)
            val compIndex = cursor.getColumnIndex(KEY_TASK_COMPLETED)
            
            // Check if column indices are valid to prevent crashes
            if (idIndex >= 0 && nameIndex >= 0 && catIndex >= 0 && xpIndex >= 0 && compIndex >= 0) {
                do {
                    val id = cursor.getString(idIndex)
                    val name = cursor.getString(nameIndex)
                    val category = cursor.getString(catIndex)
                    val xp = cursor.getInt(xpIndex)
                    val isCompleted = cursor.getInt(compIndex) == 1
                    list.add(TaskItem(id, name, category, xp, isCompleted))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return list
    }

    fun addTask(username: String, task: TaskItem) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TASK_ID, task.id)
            put(KEY_TASK_USER, username)
            put(KEY_TASK_NAME, task.name)
            put(KEY_TASK_CATEGORY, task.category)
            put(KEY_TASK_XP, task.xpReward)
            put(KEY_TASK_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_TASKS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateTask(task: TaskItem) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TASK_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        db.update(TABLE_TASKS, values, "$KEY_TASK_ID = ?", arrayOf(task.id))
    }

    // Journal operations
    fun getJournalEntries(username: String): List<JournalEntryItem> {
        val list = mutableListOf<JournalEntryItem>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_JOURNAL, null, "$KEY_JOURNAL_USER = ?", arrayOf(username), null, null, null)
        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(KEY_JOURNAL_ID)
            val dateIndex = cursor.getColumnIndex(KEY_JOURNAL_DATE)
            val textIndex = cursor.getColumnIndex(KEY_JOURNAL_TEXT)
            
            if (idIndex >= 0 && dateIndex >= 0 && textIndex >= 0) {
                do {
                    val id = cursor.getString(idIndex)
                    val date = cursor.getString(dateIndex)
                    val text = cursor.getString(textIndex)
                    list.add(JournalEntryItem(id, date, text))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return list
    }

    fun addJournalEntry(username: String, entry: JournalEntryItem) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_JOURNAL_ID, entry.id)
            put(KEY_JOURNAL_USER, username)
            put(KEY_JOURNAL_DATE, entry.date)
            put(KEY_JOURNAL_TEXT, entry.text)
        }
        db.insertWithOnConflict(TABLE_JOURNAL, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
