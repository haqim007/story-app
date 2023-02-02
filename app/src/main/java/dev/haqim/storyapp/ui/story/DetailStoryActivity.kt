package dev.haqim.storyapp.ui.story

import android.os.Build
import android.os.Bundle
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import dev.haqim.storyapp.R
import dev.haqim.storyapp.databinding.ActivityDetailStoryBinding
import dev.haqim.storyapp.model.Story
import dev.haqim.storyapp.ui.base.BaseActivity

class DetailStoryActivity : BaseActivity() {
    private lateinit var binding : ActivityDetailStoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.detail_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras
        val story: Story? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelable(DETAIL_STORY, Story::class.java)
        }else{
            bundle?.getParcelable(DETAIL_STORY)
        }

        story?.let{
            binding.tvFullName.text = story.name
            binding.tvCreatedAt.text = story.createdAt
            binding.tvDescription.text = story.description
            Glide.with(this).load(story.photoUrl).placeholder(R.drawable.outline_image_search_24)
                .error(R.drawable.outline_broken_image_24).centerCrop().into(binding.imgPhoto)
        } ?: kotlin.run {
            binding.tvError.isVisible = true
            binding.clContent.isVisible = false
            binding.tvError.text = getString(R.string.story_not_found)
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    companion object{
        const val DETAIL_STORY = "detail_Story"
    }
}