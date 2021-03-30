package com.jihoheo.todolist

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jihoheo.todolist.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_memo_main.*


class MemoMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_main)

        val intent = Intent(this, MainActivity::class.java)

        mondayBtn.setOnClickListener {
            intent.putExtra("dayQ", monQ)
            intent.putExtra("dayNum", 1)
            startActivity(intent)
        }
        tuesdayBtn.setOnClickListener {
            intent.putExtra("dayQ", tueQ)
            intent.putExtra("dayNum", 2)
            startActivity(intent)
        }
        wednesdayBtn.setOnClickListener {
            intent.putExtra("dayQ", wedQ)
            intent.putExtra("dayNum", 3)
            startActivity(intent)
        }
        thursdayBtn.setOnClickListener {
            intent.putExtra("dayQ", thuQ)
            intent.putExtra("dayNum", 4)
            startActivity(intent)
        }
        fridayBtn.setOnClickListener {
            intent.putExtra("dayQ", friQ)
            intent.putExtra("dayNum", 5)
            startActivity(intent)
        }
        saturdayBtn.setOnClickListener {
            intent.putExtra("dayQ", satQ)
            intent.putExtra("dayNum", 6)
            startActivity(intent)
        }
        sundayBtn.setOnClickListener {
            intent.putExtra("dayQ", sunQ)
            intent.putExtra("dayNum", 7)
            startActivity(intent)
        }

    }

    var time3: Long = 0
    override fun onBackPressed() {
        val time1 = System.currentTimeMillis()
        val time2 = time1 - time3
        if (time2 in 0..2000) {
            ActivityCompat.finishAffinity(this) //해당 앱의 루트 액티비티를 종료시킨다.

            System.runFinalization() //현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어이다.

            System.exit(0) // 현재 액티비티를 종료시킨다.
        } else {
            time3 = time1
            Toast.makeText(applicationContext, "한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val monQ: String = "오늘이 당신의 마지막 날이라면 어떤 하루를 보내고 싶나요?"
    val tueQ: String = "당신이 가장 행복했던 순간은?"
    val wedQ: String = "오늘 같은 날 생각나는 영화는? 이유도 궁금해요."
    val thuQ: String = "당신의 하루를 가장 잘 표현해주는 노래는? 이유도 궁금해요."
    val friQ: String = "당신을 너무 힘들게 한 일은?"
    val satQ: String = "이번 주에 당신이 가장 당신다웠던 순간은?"
    val sunQ: String = "이번 주도 수고했어요 :) 어떤 일주일이었는지 알고싶어요."


}
