package com.example.android.click.main_package

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.android.click.Constants
import com.example.android.click.R
import com.example.android.click.databinding.FragmentRunTestBinding
import com.example.android.click.info
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.*

class the_game : Fragment() {


    private var mInterstitialAd: InterstitialAd? = null
    private var TAG = "Moha"

    var cancelIt = false
    lateinit var sharedPreferencee: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private lateinit var binding: FragmentRunTestBinding


    var GoldenLevel=5
    var MagnetLevel=5
    var slowLevel =5
    var moreMoneyLevel=5
    var bigHitLevel=5


    //Control the game from here
    var BreakLoop = false
    var score = 0
    var timer = Constants.time
    var time = Constants.time
    var timeBetweenMoney: Long = 90
    var minSpeed = 1000
    var maxSpeed = 2000
    var hitBox = 110
    var delayer : Long = 300


    var magn=false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRunTestBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        var adRequest = AdRequest.Builder().build()
        // remember to put yours
        InterstitialAd.load(requireActivity(), "ca-app-pub-3940256099942544/1033173712", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError?.toString().toString())
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded")
                    mInterstitialAd = interstitialAd
                }
            })

        binding.Timer.text = time.toString()
        sharedPreferencee = requireActivity().getSharedPreferences(
            getString(R.string.highscore),
            Context.MODE_PRIVATE
        )
        editor = sharedPreferencee.edit()
        lifecycleScope.launch {
            delay((Math.random() * 800 + 200).toLong())

            withContext(Dispatchers.Main) {
                lifecycleScope.launch {
                    repeat(time) {
                        delay(1000)
                        withContext(Dispatchers.Main) {
                            timer--
                            binding.Timer.text = timer.toString()
                            if (timer == 0) {
                                BreakLoop = true
                                GameEnd()
                                startActivity(Intent(activity, info::class.java))
                                findNavController().popBackStack()
                                cancelIt = true
                                this.cancel()
                            }
                        }
                    }
                }

                MakeItRain(view)
            }
        }

    }

    fun MakeItRain(view: View) {
        GlobalScope.launch {
            while (true) {
                val num = ((0..100).random()) // generated random from 0 to 10 included

                val isSpecialAbility = ((0..100).random()) % 5 == 0

                //Abilities
                var isGolden = false
                var isMagnet = false
                var isSlow = false
                var isMoreMoney = false
                var isBigHit = false

                if (timer < 2) continue
                delay(delayer)
                withContext(Dispatchers.Main) {
                    try {

                        val star: ImageView = view.findViewById(R.id.star)
                        val container = star.parent as ViewGroup
                        val containerW = container.width
                        val containerH = container.height
                        var starW: Float = star.width.toFloat()
                        var starH: Float = star.height.toFloat()





                        val newStar = AppCompatImageView(requireActivity())
                        if (isSpecialAbility) {
                            if (num<=40){
                                    newStar.setImageResource(R.drawable.ic_golden_dollar)
                                    isGolden = true
                                }else if(num == 60){
                                    newStar.setImageResource(R.drawable.ic_magnet)
                                    isMagnet=true
                                }else if (num<=70 && num >60){
                                    newStar.setImageResource(R.drawable.ic_slow)
                                    isSlow=true
                                }else if (num==80){
                                    newStar.setImageResource(R.drawable.ic_more_money)
                                    isMoreMoney=true
                                }else if (num==90 ){
                                    newStar.setImageResource(R.drawable.ic_best)
                                    isBigHit=true
                                }
                            } else {
                            newStar.setImageResource(R.drawable.ic_baseline_attach_money_24)
                        }

                        newStar.layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )
                        container.addView(newStar)
                        newStar.setPadding(hitBox, hitBox, hitBox, hitBox)

                        newStar.translationX = Math.random().toFloat() *
                                containerW - starW / 2

                        val mover = ObjectAnimator.ofFloat(
                            newStar, View.TRANSLATION_Y,
                            -starH, containerH + starH
                        )
                        mover.interpolator = AccelerateInterpolator(1f)
                        val rotator = ObjectAnimator.ofFloat(
                            newStar, View.ROTATION,
                            (Math.random() * 1080).toFloat()
                        )
                        rotator.interpolator = LinearInterpolator()

                        val set = AnimatorSet()
                        set.playTogether(mover, rotator)

                        set.duration = (Math.random() * maxSpeed + minSpeed).toLong()


                        set.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                if (magn){
                                    if (isGolden){
                                        score+=GoldenLevel
                                        isGolden=false
                                    }else
                                    score++

                                    binding.txt.setTextColor(resources.getColor(R.color.Green))
                                    binding.txt.text = score.toString()
                                }
                                container.removeView(newStar)
                            }
                        })
                        set.start()

                        newStar.setOnClickListener {
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) {
                                    if (timer == 0) { newStar.visibility = View.GONE }

                                    if (isSpecialAbility){
                                        if (isGolden){
                                            score+=GoldenLevel
                                            isGolden=false
                                        }else if (isSlow){
                                            minSpeed=2000
                                            maxSpeed=2900
                                            //todo : start timer for 5 seconds then restore them to 900-1900
                                            GlobalScope.launch(){
                                                var slowLeveltimer=slowLevel
                                                repeat(slowLeveltimer){
                                                    delay(1000)
                                                    slowLeveltimer--
                                                }
                                                minSpeed=1000
                                                maxSpeed=2000
                                                isSlow=false
                                            }
                                        }
                                        else if(isBigHit){
                                            hitBox=220
                                            GlobalScope.launch(){
                                                var BigHittimer=bigHitLevel
                                                repeat(BigHittimer){
                                                    delay(1000)
                                                    BigHittimer--
                                                }
                                                hitBox=110
                                                isBigHit = false
                                            }
                                        }else if (isMoreMoney){
                                            delayer=100
                                            GlobalScope.launch(){
                                                var BigHittimer=moreMoneyLevel
                                                repeat(BigHittimer){
                                                    delay(1000)
                                                    BigHittimer--
                                                }
                                                delayer=300
                                                isMoreMoney=false
                                            }                                           }
                                        else if(isMagnet){
                                            //todo: show all timers
                                            GlobalScope.launch(){
                                                magn=true
                                                var BigHittimer=MagnetLevel
                                                repeat(BigHittimer){
                                                    delay(1000)
                                                    BigHittimer--
                                                }
                                                isMagnet=false
                                                magn=false
                                            }
                                        }


                                    }else {
                                        score++
                                    }
                                    binding.txt.setTextColor(resources.getColor(R.color.Green))
                                    binding.txt.text = score.toString()
                                    newStar.visibility = View.GONE
                                }
                            }
                        }
                    } catch (e: Exception) {
                    }
                }


                if (BreakLoop) {
                    break

                }
                delay(timeBetweenMoney)


            }
        }
    }


    private fun GameEnd() {
        var i = sharedPreferencee.getInt("high", 0)
        Constants.HighScore = i
        Constants.scoree = score
        if (i < score) {
            editor.putInt("high", score).commit()
        }

        var money = sharedPreferencee.getLong("UserMoney", 0)
        editor.putLong("UserMoney", money + score).commit()
        try {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(requireActivity())
            }
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        GameEnd()
        super.onDestroy()
    }

}