package com.example.data

import android.content.Context
import androidx.room.*
import com.example.model.ChatMessage
import com.example.model.MatchRequest
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 0 ORDER BY registeredTimestamp DESC")
    fun getAllOtherProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun getProfileByIdFlow(id: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<UserProfile>)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("UPDATE user_profiles SET isCurrentUser = 0")
    suspend fun clearCurrentUserFlag()

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)

    @Query("UPDATE user_profiles SET isShortlisted = :shortlisted WHERE id = :id")
    suspend fun updateShortlistStatus(id: String, shortlisted: Boolean)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM match_requests")
    fun getAllRequestsFlow(): Flow<List<MatchRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: MatchRequest)

    @Query("DELETE FROM match_requests WHERE id = :id")
    suspend fun deleteRequestById(id: String)

    @Query("UPDATE match_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: String, status: String)

    @Query("SELECT * FROM match_requests WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) LIMIT 1")
    suspend fun getRequestBetweenUsers(user1: String, user2: String): MatchRequest?
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) ORDER BY timestamp ASC")
    fun getMessagesForChat(user1: String, user2: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1)")
    suspend fun deleteChatHistory(user1: String, user2: String)
}

@Database(entities = [UserProfile::class, MatchRequest::class, ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun matchDao(): MatchDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vvs_matrimony_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object DatabaseInitializer {
    suspend fun seedDatabaseIfEmpty(database: AppDatabase) {
        val profileDao = database.profileDao()
        val existingCurrentUser = profileDao.getCurrentUser()
        val allOtherProfiles = database.profileDao().getCurrentUser() // quick check

        // Seed 6 profiles for matching
        val profiles = listOf(
            UserProfile(
                id = "p1",
                fullName = "Ananya Sastry",
                email = "ananya.s@outlook.com",
                mobileNumber = "9876543210",
                gender = "Female",
                dob = "1997-08-16",
                community = "Smartha",
                subSect = "Mulukanadu",
                education = "M.S. Computer Science",
                occupation = "Software Developer (Amazon)",
                location = "Bengaluru",
                height = "5'4\"",
                maritalStatus = "Never Married",
                aboutMe = "Traditional yet progressive Smartha Mulukanadu Brahmin girl. Loves Indian classical music (Veena), traveling, and reading. Born and brought up in Bangalore, working for a top tech firm.",
                avatarUrl = "avatar_f1",
                additionalPhotosJson = "[\"avatar_f1_slide1\", \"avatar_f1_slide2\"]"
            ),
            UserProfile(
                id = "p2",
                fullName = "Vikram Acharya",
                email = "vikram.acharya@gmail.com",
                mobileNumber = "9885544332",
                gender = "Male",
                dob = "1994-05-12",
                community = "Madhwa",
                subSect = "Shivalli",
                education = "B.E. & MBA (IIMB)",
                occupation = "Investment Banker",
                location = "Mumbai",
                height = "5'11\"",
                maritalStatus = "Never Married",
                aboutMe = "Madhwa Shivalli Brahmin groom settled in Mumbai. Honest, active lifestyle, practices daily Sandhyavandanam, loves tennis and trekking. Looking for a family-oriented, educated Brahmin companion.",
                avatarUrl = "avatar_m1",
                additionalPhotosJson = "[\"avatar_m1_slide1\"]"
            ),
            UserProfile(
                id = "p3",
                fullName = "Sujata Iyengar",
                email = "sujata.iyengar@gmail.com",
                mobileNumber = "9900223344",
                gender = "Female",
                dob = "1996-11-22",
                community = "Iyengar",
                subSect = "Vadakalai",
                education = "Ph.D. in Linguistics",
                occupation = "Assistant Professor",
                location = "Chennai",
                height = "5'5\"",
                maritalStatus = "Never Married",
                aboutMe = "Brought up in a devout Srivaishnava Iyengar family. Passionate about Sanskrit literature, Carnatic vocals, and teaching. Seeking a progressive groom who values family culture.",
                avatarUrl = "avatar_f2",
                additionalPhotosJson = "[]"
            ),
            UserProfile(
                id = "p4",
                fullName = "Rahul Bhat",
                email = "rahul.bhat@yahoo.com",
                mobileNumber = "9448855221",
                gender = "Male",
                dob = "1993-02-10",
                community = "Havyaka",
                subSect = "Yajurvedi",
                education = "M.S. in Agronomy",
                occupation = "Organic Agri-Business Owner",
                location = "Sirsi",
                height = "5'9\"",
                maritalStatus = "Never Married",
                aboutMe = "Havyaka Brahmin groom running a sustainable food processing start-up in Western Ghats. Grounded, fond of nature, organic farming, and Sanskrit culture.",
                avatarUrl = "avatar_m2",
                additionalPhotosJson = "[\"avatar_m2_slide1\"]"
            ),
            UserProfile(
                id = "p5",
                fullName = "Deepa Laxmi",
                email = "deepalaxmi@gmail.com",
                mobileNumber = "9112233445",
                gender = "Female",
                dob = "1995-12-04",
                community = "Iyer",
                subSect = "Vadama",
                education = "Chartered Accountant (CA)",
                occupation = "Finance Manager (EY)",
                location = "Bengaluru",
                height = "5'2\"",
                maritalStatus = "Never Married",
                aboutMe = "Professional CA Iyer girl. Warm, cheerful, values traditions, fond of cooking and painting. Seeking a smart, respectful Iyer/Brahmin professional.",
                avatarUrl = "avatar_f3",
                additionalPhotosJson = "[\"avatar_f3_slide1\"]"
            ),
            UserProfile(
                id = "p6",
                fullName = "Prasanna Joshi",
                email = "prasanna.joshi@gmail.com",
                mobileNumber = "9776655443",
                gender = "Male",
                dob = "1992-09-18",
                community = "Smartha",
                subSect = "Deshastha",
                education = "M.Tech in VLSI",
                occupation = "Senior hardware Engineer",
                location = "Bengaluru",
                height = "5'10\"",
                maritalStatus = "Never Married",
                aboutMe = "A simple Smartha Deshastha Brahmin. Intellectually curious, nature enthusiast, values family relationships and rituals. Looking for a compatible Brahmin girl.",
                avatarUrl = "avatar_m3",
                additionalPhotosJson = "[]"
            )
        )

        // Only insert if database doesn't have these
        for (p in profiles) {
            val exist = profileDao.getProfileById(p.id)
            if (exist == null) {
                profileDao.insertProfile(p)
            }
        }
    }
}
