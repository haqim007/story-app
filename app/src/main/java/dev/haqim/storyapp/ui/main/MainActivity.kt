package dev.haqim.storyapp.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import dev.haqim.storyapp.R
import dev.haqim.storyapp.databinding.ActivityMainBinding
import dev.haqim.storyapp.di.Injection
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.ui.add_story.AddStoryActivity
import dev.haqim.storyapp.ui.base.BaseActivity
import dev.haqim.storyapp.ui.login.LoginActivity
import dev.haqim.storyapp.ui.map.StoryMapsActivity
import dev.haqim.storyapp.ui.story.DetailStoryActivity
import kotlinx.coroutines.flow.*
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
        val adapter = setupAdapter(uiAction)
        
        val openStoryFlow = uiState.map { it.storyToBeOpened }.distinctUntilChanged()
        lifecycleScope.launch { 
            openStoryFlow.collectLatest { 
                if (it.story != null){
                    val intent = Intent(
                        this@MainActivity,
                        DetailStoryActivity::class.java
                    )
                    val bundle = Bundle()
                    bundle.putParcelable(DetailStoryActivity.DETAIL_STORY, it.story)
                    intent.putExtras(bundle)
                    startActivity(intent, it.optionsCompat?.toBundle())
                    uiAction(MainUiAction.FinishNavigateToDetailStory)
                }
            }
        }

        //bindUserData
        bindUserData(uiState)

        // bindStories
        bindStories(viewModel.pagingDataFlow, adapter)

        // bindNavigateToAddStory
        bindNavigateToAddStory(uiAction, uiState)

        binding.srlMain.setOnRefreshListener {
            adapter.refresh()
            binding.srlMain.isRefreshing = false
        }

        binding.btnShowMap.setOnClickListener {
            val intent = Intent(this, StoryMapsActivity::class.java)
            startActivity(intent)
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

    private fun setupAdapter(uiAction: (MainUiAction) -> Boolean): StoryAdapter {
        val adapter = StoryAdapter(object : StoryAdapterListener{

            override fun onClickStory(
                context: Context,
                story: Story?,
                optionsCompat: ActivityOptionsCompat?,
            ) {
                story?.let {
                    uiAction(MainUiAction.NavigateToDetailStory(story, optionsCompat))
                }
            }

        })
        val layoutManager = GridLayoutManager(
            this,
            2
        )
        binding.rvStories.layoutManager = layoutManager
        binding.rvStories.adapter = adapter
        return adapter
    }

    private fun bindStories(
        pagingData: Flow<PagingData<Story>>,
        adapter: StoryAdapter,
    ) {
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter{
                adapter.retry()
            }
        )
        
        lifecycleScope.launch { 
            pagingData.collect(adapter::submitData)
        }
    }

    private fun bindUserData(
        uiState: StateFlow<MainUiState>,
    ) {
        val userDataFlow = uiState.map { it.userData }.distinctUntilChanged()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userDataFlow.collectLatest {
                    if (it != null && it.token.isEmpty()) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}