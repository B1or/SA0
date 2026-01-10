package tech.droi.saveas

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context): ContactDao {
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "contacts")
            .build()
        return db.contactDao()
    }
}
