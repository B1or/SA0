package tech.droi.saveas

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactRoom(
    @PrimaryKey @ColumnInfo(name = "cid") val contactId: Long,
    @ColumnInfo(name = "save_as") val saveAs: Int
)
