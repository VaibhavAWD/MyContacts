package app.practice.mycontacts.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * [Contact] entity which determines "contacts" table
 * in the database.
 */
@Entity(tableName = "contacts")
class Contact(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "mobile")
    var mobile: String,
    @PrimaryKey
    @ColumnInfo(name = "entryId")
    val id: String = UUID.randomUUID().toString()
)