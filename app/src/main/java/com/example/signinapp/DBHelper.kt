package com.example.signinapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION)
{
    override fun onCreate(db: SQLiteDatabase)
    {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                NAME_COL + " TEXT," +
                EMAIL_COL + " TEXT," +
                USERNAME_COL + " TEXT," +
                PASSWORD_COL + " TEXT" + ")")
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int)
    {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addUser(name : String, emailID : String, username : String, password : String )
    {
        val values = ContentValues()

        values.put(NAME_COL, name)
        values.put(EMAIL_COL, emailID)
        values.put(USERNAME_COL, username)
        values.put(PASSWORD_COL, password)
        val db = this.writableDatabase

        db.insert(TABLE_NAME, null, values)
        getDBData()
        db.close()
    }

    fun checkUser(username: String, password: String): Boolean
    {
        val columns = arrayOf(ID_COL)
        val db = this.readableDatabase
        val selection = "$USERNAME_COL = ? AND $PASSWORD_COL = ?"
        val selectionArgs = arrayOf(username, password)

        val cursor = db.query(TABLE_NAME,
            columns, //columns to return
            selection, //columns for the WHERE clause
            selectionArgs, //The values for the WHERE clause
            null,  //group the rows
            null, //filter by row groups
            null) //The sort order

        val cursorCount = cursor.count
        cursor.close()
        db.close()
        return cursorCount > 0
    }

    private fun getDBData()
    {
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase

        db.rawQuery(selectQuery, null).use { cursor ->
            if (cursor.moveToFirst())
            {
                val columnNames = cursor.columnNames
                Log.d("DB Columns", columnNames.joinToString(", "))
                do
                {
                    val rowData = columnNames.joinToString(", ")
                    {
                        columnName ->
                        val columnIndex = cursor.getColumnIndex(columnName)
                        val value = when (cursor.getType(columnIndex))
                        {
                            Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(columnIndex)
                            Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(columnIndex)
                            Cursor.FIELD_TYPE_STRING -> cursor.getString(columnIndex)
                            Cursor.FIELD_TYPE_BLOB -> cursor.getBlob(columnIndex).toString()
                            else -> "NULL"
                        }
                        "$columnName: $value"
                    }
                    Log.d("DB Row Data", rowData)
                }
                while (cursor.moveToNext())
            }
            else
            {
                Log.d("DB", "No entries found.")
            }
        }
    }

    companion object
    {
        private const val DATABASE_NAME = "APP_DB"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "app_table"
        const val ID_COL = "id"
        const val NAME_COL = "name"
        const val EMAIL_COL = "email"
        const val USERNAME_COL = "username"
        const val PASSWORD_COL = "password"
    }
}