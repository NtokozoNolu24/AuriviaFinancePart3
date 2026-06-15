package com.example.open_sourcepart2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.databinding.FragmentGamificationBinding

class GamificationFragment : Fragment() {

    private var _binding: FragmentGamificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var gamificationManager: GamificationManager
    private lateinit var achievementAdapter: AchievementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gamificationManager = GamificationManager(requireContext())

        setupUI()
        loadGamificationData()
    }

    private fun setupUI() {
        binding.rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        achievementAdapter = AchievementAdapter(emptyList())
        binding.rvAchievements.adapter = achievementAdapter
    }

    private fun loadGamificationData() {
        // Update user stats
        val totalPoints = gamificationManager.getTotalPoints()
        val currentLevel = gamificationManager.getUserLevel()
        val pointsToNext = gamificationManager.getPointsToNextLevel()
        val currentStreak = gamificationManager.getCurrentStreak()

        binding.tvTotalPoints.text = "Total Points: $totalPoints"
        binding.tvCurrentLevel.text = "Level $currentLevel"
        binding.tvCurrentStreak.text = "Current Streak: $currentStreak days"

        if (pointsToNext > 0) {
            binding.tvPointsToNext.text = "$pointsToNext points to next level"
            binding.progressLevel.progress = ((totalPoints.toFloat() / (totalPoints + pointsToNext)) * 100).toInt()
        } else {
            binding.tvPointsToNext.text = "Max level reached!"
            binding.progressLevel.progress = 100
        }

        // Load achievements
        val achievements = gamificationManager.getAllAchievements()
        achievementAdapter.updateAchievements(achievements)

        // Update achievement stats
        val unlockedCount = achievements.count { it.isUnlocked }
        binding.tvAchievementProgress.text = "Achievements: $unlockedCount/${achievements.size}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
