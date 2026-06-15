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

        _binding = FragmentGamificationBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        gamificationManager =
            GamificationManager(requireContext())


        setupRecyclerView()

        loadGamificationData()

    }



    private fun setupRecyclerView(){

        binding.rvAchievements.layoutManager =
            LinearLayoutManager(requireContext())


        achievementAdapter =
            AchievementAdapter(emptyList())


        binding.rvAchievements.adapter =
            achievementAdapter

    }





    private fun loadGamificationData(){


        val totalPoints =
            gamificationManager.getTotalPoints()


        val currentLevel =
            gamificationManager.getUserLevel()


        val pointsToNext =
            gamificationManager.getPointsToNextLevel()


        val currentStreak =
            gamificationManager.getCurrentStreak()



        // XP POINTS

        binding.tvTotalPoints.text =
            "$totalPoints XP"



        // LEVEL TITLE

        binding.tvCurrentLevel.text =
            when {

                currentLevel >= 10 ->
                    "🏆 Finance Master"


                currentLevel >= 5 ->
                    "⭐ Money Champion"


                else ->
                    "🌱 Finance Beginner"

            }




        // STREAK

        binding.tvCurrentStreak.text =
            "🔥 $currentStreak Day Streak"





        // LEVEL PROGRESS


        if(pointsToNext > 0){


            val progress =
                (
                        totalPoints.toFloat()
                                /
                                (totalPoints + pointsToNext)
                        ) * 100



            binding.progressLevel.progress =
                progress.toInt()



            binding.tvPointsToNext.text =
                "${progress.toInt()}% completed • $pointsToNext XP to next level"


        }
        else{


            binding.progressLevel.progress =
                100


            binding.tvPointsToNext.text =
                "🎉 Maximum level reached!"

        }







        // ACHIEVEMENTS


        val achievements =
            gamificationManager.getAllAchievements()



        achievementAdapter.updateAchievements(
            achievements
        )



        val unlocked =
            achievements.count {
                it.isUnlocked
            }



        binding.tvAchievementProgress.text =
            "🏅 $unlocked/${achievements.size} Achievements unlocked"



    }





    override fun onResume(){

        super.onResume()


        if(::gamificationManager.isInitialized){

            loadGamificationData()

        }

    }





    override fun onDestroyView(){

        super.onDestroyView()

        _binding = null

    }


}