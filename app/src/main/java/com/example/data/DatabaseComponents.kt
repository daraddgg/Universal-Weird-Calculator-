package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "conversion_history")
data class ConversionHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val value: Double,
    val category: String,
    val fromUnit: String,
    val toUnit: String,
    val result: String, // Calulated primary scientific results (e.g. "1 km")
    val comparisonText: String, // Multi-line real-world comparisons
    val fact: String, // Target factual insight
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ConversionHistory>>

    @Query("SELECT * FROM conversion_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<ConversionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversion: ConversionHistory): Long

    @Update
    suspend fun update(conversion: ConversionHistory)

    @Delete
    suspend fun delete(conversion: ConversionHistory)

    @Query("DELETE FROM conversion_history")
    suspend fun clearAll()
}

@Database(entities = [ConversionHistory::class], version = 1, exportSchema = false)
abstract class WeirdDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}

class HistoryRepository(private val dao: HistoryDao) {
    val allHistory: Flow<List<ConversionHistory>> = dao.getAllHistory()
    val favorites: Flow<List<ConversionHistory>> = dao.getFavorites()

    suspend fun insert(conversion: ConversionHistory) = dao.insert(conversion)
    suspend fun update(conversion: ConversionHistory) = dao.update(conversion)
    suspend fun delete(conversion: ConversionHistory) = dao.delete(conversion)
    suspend fun clearAll() = dao.clearAll()
}

object DatabaseProvider {
    private var db: WeirdDatabase? = null
    fun getDatabase(context: android.content.Context): WeirdDatabase {
        return db ?: synchronized(this) {
            val instance = androidx.room.Room.databaseBuilder(
                context.applicationContext,
                WeirdDatabase::class.java,
                "weird_calculator_database"
            ).fallbackToDestructiveMigration().build()
            db = instance
            instance
        }
    }
}
