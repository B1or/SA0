package tech.droi.saveas

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts")
    suspend fun getAll(): List<ContactRoom>

    @Query("SELECT * FROM contacts WHERE cid = :id")
    suspend fun getContact(id: Long): ContactRoom?

    @Insert
    suspend fun insert(contact: ContactRoom)

    @Delete
    suspend fun delete(contact: ContactRoom)

    @Update
    suspend fun update(contact: ContactRoom)
}
