package eu.tutorials.roomdemo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee-table")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int =0,
    val name: String ="",
    @ColumnInfo(name="email-id")
    val email: String = "",
    val phoneNumber: String = "",
    val jobPosition : String = ""
)
