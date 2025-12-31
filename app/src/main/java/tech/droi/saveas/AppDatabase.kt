package tech.droi.saveas

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ContactRoom::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
