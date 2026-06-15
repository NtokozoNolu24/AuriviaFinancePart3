package com.example.open_sourcepart2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AchievementAdapter(
    private var achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount(): Int = achievements.size

    fun updateAchievements(newAchievements: List<Achievement>) {
        achievements = newAchievements
        notifyDataSetChanged()
    }

    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAchievementIcon: TextView = itemView.findViewById(R.id.tvAchievementIcon)
        private val tvAchievementTitle: TextView = itemView.findViewById(R.id.tvAchievementTitle)
        private val tvAchievementDescription: TextView = itemView.findViewById(R.id.tvAchievementDescription)
        private val tvAchievementStatus: TextView = itemView.findViewById(R.id.tvAchievementStatus)
        private val progressAchievement: ProgressBar? = itemView.findViewById(R.id.progressAchievement)
        private val tvProgressText: TextView? = itemView.findViewById(R.id.tvProgressText)

        fun bind(achievement: Achievement) {
            // Set icon based on achievement key/title
            tvAchievementIcon.text = getAchievementIcon(achievement.key)
            tvAchievementTitle.text = achievement.title
            tvAchievementDescription.text = achievement.description

            when {
                achievement.isUnlocked -> {
                    // Unlocked state
                    tvAchievementIcon.alpha = 1.0f
                    tvAchievementTitle.setTextColor(Color.parseColor("#FFFFFF"))
                    tvAchievementDescription.setTextColor(Color.parseColor("#CE93D8"))
                    tvAchievementStatus.text = "✓ Unlocked"
                    tvAchievementStatus.setTextColor(Color.parseColor("#E040FB"))
                    progressAchievement?.visibility = View.GONE
                    tvProgressText?.visibility = View.GONE
                    itemView.alpha = 1.0f
                }
                else -> {
                    // Locked state
                    tvAchievementIcon.alpha = 0.6f
                    tvAchievementTitle.setTextColor(Color.parseColor("#757575"))
                    tvAchievementDescription.setTextColor(Color.parseColor("#757575"))
                    tvAchievementStatus.text = "🔒 Locked"
                    tvAchievementStatus.setTextColor(Color.parseColor("#757575"))
                    progressAchievement?.visibility = View.GONE
                    tvProgressText?.visibility = View.GONE
                    itemView.alpha = 0.6f
                }
            }
        }

        private fun getAchievementIcon(key: String): String {
            return when (key) {
                GamificationManager.ACHIEVEMENT_FIRST_EXPENSE -> "📝"
                GamificationManager.ACHIEVEMENT_BUDGET_KEEPER -> "💰"
                GamificationManager.ACHIEVEMENT_STREAK_7 -> "🔥"
                GamificationManager.ACHIEVEMENT_STREAK_30 -> "🏆"
                GamificationManager.ACHIEVEMENT_SAVER -> "⭐"
                GamificationManager.ACHIEVEMENT_CATEGORY_MASTER -> "📊"
                else -> "🏆"
            }
        }
    }
}