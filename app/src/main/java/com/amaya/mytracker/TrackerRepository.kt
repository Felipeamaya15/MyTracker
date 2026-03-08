package com.amaya.mytracker

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TrackerRepository {
    private val db = Firebase.firestore
    private val apiService = JikanApiService.create()

    // Firestore: Listen to real-time updates
    fun getTrackItems(): Flow<List<TrackItem>> = callbackFlow {
        val subscription = db.collection("trackers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        TrackItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            chapter = doc.getLong("chapter")?.toInt() ?: 0,
                            status = doc.getString("status") ?: "Reading",
                            lastUpdated = doc.getLong("lastUpdated") ?: 0L,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            totalChapters = doc.getLong("totalChapters")?.toInt() ?: 0,
                            genres = doc.get("genres") as? List<String> ?: emptyList()
                        )
                    }
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }

    // API: Search manga
    suspend fun searchManga(query: String): List<MangaData> {
        return try {
            val response = apiService.searchManga(query)
            response.data
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Firestore: Add item
    fun addTrackItem(manga: MangaData) {
        val nuevo = hashMapOf(
            "title" to manga.title,
            "chapter" to 0,
            "status" to "Reading",
            "lastUpdated" to System.currentTimeMillis(),
            "imageUrl" to manga.images.jpg.image_url,
            "totalChapters" to (manga.chapters ?: 0),
            "genres" to (manga.genres?.map { it.name } ?: emptyList<String>())
        )
        db.collection("trackers").add(nuevo)
    }

    // Firestore: Update item
    fun updateTrackItem(id: String, updates: Map<String, Any>) {
        val data = updates.toMutableMap()
        data["lastUpdated"] = System.currentTimeMillis()
        db.collection("trackers").document(id).update(data)
    }

    // Firestore: Delete item
    fun deleteTrackItem(id: String) {
        db.collection("trackers").document(id).delete()
    }
}