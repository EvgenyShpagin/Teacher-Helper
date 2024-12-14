package com.tusur.teacherhelper.presentation.core

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.ActivityMainBinding
import com.tusur.teacherhelper.presentation.core.util.AppBarConfigurationProvider
import com.tusur.teacherhelper.presentation.core.util.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AppBarConfigurationProvider {

    private lateinit var binding: ActivityMainBinding

    override lateinit var appBarConfiguration: AppBarConfiguration
        private set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            drawerLayout = binding.root,
            topLevelDestinationIds = setOf(
                R.id.subjectsFragment,
                R.id.allGroupsFragment,
                R.id.globalTopicListFragment,
                R.id.topicTypeListFragment
            )
        )

        binding.navView.setupWithNavController(navController)
    }
}