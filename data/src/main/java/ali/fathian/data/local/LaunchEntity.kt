package ali.fathian.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launches")
data class LaunchEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val image: String? = null,
    val name: String? = null,
    val date: String? = null,
    val time: String? = null,
    val details: String? = null,
    val upcoming: Boolean = false,
    val bookmarked: Boolean = false,
    val success: Boolean = false,
)
