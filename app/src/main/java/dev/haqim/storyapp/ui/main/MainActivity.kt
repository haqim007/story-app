package dev.haqim.storyapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.haqim.storyapp.R
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.databinding.ActivityMainBinding
import dev.haqim.storyapp.di.Injection
import dev.haqim.storyapp.ui.add_story.AddStoryActivity
import dev.haqim.storyapp.ui.base.BaseActivity
import dev.haqim.storyapp.ui.login.LoginActivity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels{
        Injection.provideViewModelProvider(this)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.home)

        val uiState = viewModel.uiState
        val uiAction = {action: MainUiAction -> viewModel.processAction(action)}

        //setup recyclerview
        val adapter = setupAdapter()

        //bindUserData
        bindUserData(uiState, uiAction)

        // bindStories
        bindStories(uiState, adapter)

        // bindNavigateToAddStory
        bindNavigateToAddStory(uiAction, uiState)

        binding.srlMain.setOnRefreshListener {
            uiAction(MainUiAction.GetStories)
            binding.srlMain.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                viewModel.processAction(MainUiAction.Logout)
                true
            }
            R.id.action_language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun bindNavigateToAddStory(
        uiAction: (MainUiAction) -> Boolean,
        uiState: StateFlow<MainUiState>,
    ) {
        binding.btnAddStory.setOnClickListener {
            uiAction(MainUiAction.NavigateToAddStory)
        }
        val navigateToAddStoryFlow = uiState.map { it.navigateToAddStory }.distinctUntilChanged()
        lifecycleScope.launch {
            navigateToAddStoryFlow.collect {
                if (it) {
                    val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
                    startActivity(intent)
                    uiAction(MainUiAction.NavigateToAddStory)
                }
            }
        }
    }

    private fun setupAdapter(): StoryAdapter {
        val adapter = StoryAdapter()
        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvStories.layoutManager = layoutManager
        binding.rvStories.adapter = adapter
        binding.rvStories.addItemDecoration(
            DividerItemDecoration(
                baseContext,
                layoutManager.orientation
            )
        )
        return adapter
    }

    private fun bindStories(
        uiState: StateFlow<MainUiState>,
        adapter: StoryAdapter,
    ) {
        val storiesFlow = uiState.map { it.stories }.distinctUntilChanged()
        lifecycleScope.launch {
            storiesFlow.collectLatest {
                binding.pbLoader.isVisible = false
                binding.tvError.isVisible = false
                when (it) {
                    is Resource.Loading -> {
                        binding.rvStories.isVisible = false
                        binding.pbLoader.isVisible = true
                    }
                    is Resource.Success -> {
                        if (it.data.isNullOrEmpty()) {
                            binding.tvError.isVisible = true
                            binding.tvError.text = getString(R.string.empty)
                        } else {
                            binding.rvStories.isVisible = true
                            adapter.submitList(it.data)
                        }
                    }
                    is Resource.Error -> {
                        binding.tvError.isVisible = true
                        binding.tvError.text = it.message
                    }
                    else -> {}
                }
            }
        }
    }

    private fun bindUserData(
        uiState: StateFlow<MainUiState>,
        uiAction: (MainUiAction) -> Boolean,
    ) {
        val userDataFlow = uiState.map { it.userData }.distinctUntilChanged()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userDataFlow.collectLatest {
                    if (it != null) {
                        if (it.token.isEmpty()) {
                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            uiAction(MainUiAction.GetStories)
                        }
                    }
                }
            }
        }
    }
}