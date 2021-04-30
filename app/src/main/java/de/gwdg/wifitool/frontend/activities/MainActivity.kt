package de.gwdg.wifitool.frontend.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import de.gwdg.wifitool.R
import de.gwdg.wifitool.backend.ProfileApi
import de.gwdg.wifitool.backend.WifiConfig
import de.gwdg.wifitool.databinding.ActivityMainBinding
import de.gwdg.wifitool.frontend.adapters.MainPagerAdapter


class MainActivity : AppCompatActivity() {
    private val logTag = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var pagerAdapter: MainPagerAdapter
    private lateinit var dotImageViews: Array<ImageView>
    lateinit var profileApi: ProfileApi

    var wifiConfigResults: List<WifiConfig.WifiConfigResult> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initPager()
        bindDots()
        bindNavigationButtons()
        profileApi = ProfileApi(this)
    }

    private fun bindNavigationButtons() {
        binding.nextButton.setOnClickListener { goNext() }
        binding.backButton.setOnClickListener { goBack() }
    }

    private fun initPager() {
        pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.isUserInputEnabled = false
    }


    private fun bindDots() {
        with(binding) {
            this@MainActivity.dotImageViews = arrayOf(
                dot1ImageView,
                dot2ImageView,
                dot3ImageView,
                dot4ImageView,
                dot5ImageView
            )
            highlightDot(viewPager.currentItem)
            viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    highlightDot(position)
                }
            })
        }
    }

    private fun highlightDot(position: Int) {
        for (index in dotImageViews.indices) {
            if (index == position) {
                dotImageViews[index].setImageResource(R.drawable.dot)
                dotImageViews[index].animate().alpha(0.7f).scaleX(1.2f).scaleY(1.2f)
            } else {
                dotImageViews[index].setImageResource(R.drawable.dot_outline)
                dotImageViews[index].animate().alpha(0.5f).scaleX(1f).scaleY(1f)
            }
        }
    }

    fun allowNext() {
        binding.nextButton.visibility = View.VISIBLE
    }

    fun blockNext() {
        binding.nextButton.visibility = View.GONE
    }

    fun allowBack() {
        binding.backButton.visibility = View.VISIBLE
    }

    fun blockBack() {
        binding.backButton.visibility = View.GONE
    }

    fun backAllowed(): Boolean = binding.backButton.visibility == View.VISIBLE
    fun nextAllowed(): Boolean = binding.nextButton.visibility == View.VISIBLE
    fun getCurrentPage(): Int = binding.viewPager.currentItem

    private fun goNext() {
        binding.viewPager.currentItem += 1
        allowBack()
        blockNext()
        resetNextButton()
    }

    private fun goBack() {
        if (getCurrentPage() == 0) {
            return
        } else if (getCurrentPage() == 1) {
            blockBack()
        }
        binding.viewPager.currentItem -= 1
        allowNext()
        resetNextButton()
    }

    fun addNextButtonAction(action: () -> Unit) {
        binding.nextButton.setOnClickListener { action(); goNext() }
    }

    fun changeNextButtonText(newText: String) {
        binding.nextButton.text = newText
    }

    fun resetNextButton() {
        binding.nextButton.setOnClickListener { goNext() }
        binding.nextButton.text = getString(R.string.next_button)
    }

    override fun onBackPressed() {
        if (getCurrentPage() == 0)
        // close app with back on first page
            super.onBackPressed()
        else if (backAllowed()) {
            // back on all other pages if allowed
            goBack()
        }

    }
}


