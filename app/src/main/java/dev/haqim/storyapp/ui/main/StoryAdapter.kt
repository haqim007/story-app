package dev.haqim.storyapp.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import dev.haqim.storyapp.R
import dev.haqim.storyapp.databinding.ItemMainBinding
import dev.haqim.storyapp.model.Story
import dev.haqim.storyapp.ui.story.DetailStoryActivity

class StoryAdapter:
    ListAdapter<Story, RecyclerView.ViewHolder>(DIFF_CALLBACK){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.onCreate(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val story = getItem(position)
        (holder as ViewHolder).onBind(story)
    }

    class ViewHolder(private val binding: ItemMainBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(story: Story) {

            Glide.with(itemView.context).load(story.photoUrl)
                .placeholder(R.drawable.outline_image_search_24)
                .transform(CenterCrop(), RoundedCorners(24))
                .into(binding.imgPhoto)
            binding.tvFullName.text = story.name
            binding.root.setOnClickListener {
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.tvFullName, "full_name"),
                        Pair(binding.tvCreatedAt, "created_at"),
                        Pair(binding.imgPhoto, "photo"),
                        Pair(binding.tvDescription, "description"),
                    )

                val intent = Intent(
                    itemView.context,
                    DetailStoryActivity::class.java
                )
                val bundle = Bundle()
                bundle.putParcelable(DetailStoryActivity.DETAIL_STORY, story)
                intent.putExtras(bundle)
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
            binding.tvDescription.text = story.description
        }

        companion object{
            fun onCreate(parent: ViewGroup): ViewHolder{
                val itemView =
                    ItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(itemView)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {

            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }
        }
    }
}