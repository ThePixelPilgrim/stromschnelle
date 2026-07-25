package de.nereide.stromschnelle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Todo::class], version = 2, exportSchema = true)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        /**
         * Drops `sortIndex` in favour of `importance` / `effort`, mapping the old
         * manual ranking into thirds. SQLite cannot drop a column, hence the
         * recreate dance; the thirds are computed in the same pass.
         *
         * The statements live in [TodoMigrationSql] so the unit test can execute
         * exactly what ships here.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                TodoMigrationSql.V1_TO_V2.forEach(db::execSQL)
            }
        }

        fun getInstance(context: Context): TodoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "stromschnelle.db"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
