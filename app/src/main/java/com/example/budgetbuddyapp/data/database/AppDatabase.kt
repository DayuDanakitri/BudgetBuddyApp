package com.example.budgetbuddyapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.budgetbuddyapp.data.model.Category
import com.example.budgetbuddyapp.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Category::class, Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetbuddy_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).categoryDao().apply {
                                    insert(Category(name = "Gaji",       type = "INCOME",  iconEmoji = "💰"))
                                    insert(Category(name = "Freelance",  type = "INCOME",  iconEmoji = "💻"))
                                    insert(Category(name = "Investasi",  type = "INCOME",  iconEmoji = "📈"))
                                    insert(Category(name = "Lainnya",    type = "INCOME",  iconEmoji = "✨"))
                                    insert(Category(name = "Makanan",    type = "EXPENSE", iconEmoji = "🍜"))
                                    insert(Category(name = "Transportasi", type = "EXPENSE", iconEmoji = "🚗"))
                                    insert(Category(name = "Belanja",    type = "EXPENSE", iconEmoji = "🛒"))
                                    insert(Category(name = "Hiburan",    type = "EXPENSE", iconEmoji = "🎮"))
                                    insert(Category(name = "Kesehatan",  type = "EXPENSE", iconEmoji = "💊"))
                                    insert(Category(name = "Tagihan",    type = "EXPENSE", iconEmoji = "📄"))
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}