package com.jihoheo.todolist

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jihoheo.todolist.databinding.ActivityMainBinding
import com.jihoheo.todolist.databinding.ItemTodoBinding
import kotlinx.android.synthetic.main.activity_memo_main.*

var dayN: Int = 0

class MainActivity : AppCompatActivity() {
    val RC_SIGN_IN = 1000


    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        if (intent.hasExtra("dayQ")) {
            binding.dayQ.text = intent.getStringExtra("dayQ")
            /* "dayQ"라는 이름의 key에 저장된 값이 있다면
               textView의 내용을 "sayQ" key에서 꺼내온 값으로 바꾼다 */

        } else {
            binding.dayQ.text = "오늘의 질문을 확인해주세요:)"
            /* "dayQ"라는 이름의 key에 저장된 값이 없다면
               textView의 내용을 "질문오류"라고 띄운다*/
        }


        dayN = intent.getIntExtra("dayNum", 0)
        /* "dayNum"라는 이름의 key에 저장된 값이 있다면
           textView의 내용을 "dayNum" key에서 꺼내온 값으로 바꾼다
           넘겨준 값이 없다면 defaultValue인 0으로 설정*/
        if (dayN == 0) {
            //메인화면에서는 작성할 수 없도록
            binding.editText.isEnabled = false
        } else {
            //각 요일의 질문 화면에서는 작성가능
            binding.editText.isEnabled = true
            //메인화면에서는 추가 버튼 눌러도 변화없고 각 요일의 질문 화면에서 추가 버튼 누르면 답 추가됨.
            binding.addButton.setOnClickListener {
                val todo = Todo(binding.editText.text.toString())
                viewModel.addTodo(todo)
                binding.recyclerView.adapter?.notifyDataSetChanged()
                binding.editText.text.clear() //작성한 답을 추가한 후 에딧텍스트의 텍스트 지우기
            }
        }
        //로그인 안됨
        if (FirebaseAuth.getInstance().currentUser == null) {
            login()
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = TodoAdapter(
                emptyList(),
                onClickDeleteIcon = {
                    viewModel.deleteTodo(it)
//                    binding.recyclerView.adapter?.notifyDataSetChanged()

                },
                onClickItem = {
                    viewModel.toggleTodo(it)
//                    binding.recyclerView.adapter?.notifyDataSetChanged()
                }

            )
        }



        // 관찰 UI업데이트 -  화면 갱신 코드 모아두기
        viewModel.todoLiveData.observe(this, Observer {
            (binding.recyclerView.adapter as TodoAdapter).setData(it)
        })
    }

    //로그인 응답
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) { //로그인이 된 상태
                // Successfully signed in
                viewModel.fetchData()
            } else {//로그인 실패했을 때
                // Sign in failed. If response is null the user canceled the
                finish()
            }
        }
    }

    fun login() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    fun logout() {
        binding.dayQ.text = "오늘의 질문을 확인해주세요:)"
        dayN = 0
        binding.editText.text.clear() //답을 작성하다가 로그아웃이 되면 에딧텍스트의 텍스트 지우기
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                login()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.backToMain -> {
                val intent = Intent(this, MemoMainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_log_out -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

// Todo 객체 만들기
data class Todo(
    val text: String,
    var isDone: Boolean = false,
    var dayNum: Int = dayN
) //이거를 가지고 리사이클뷰에 표시를 해야되니까 어뎁터도 만들고 해야된다.

// Todo 객체를 상속받을 어뎁터
class TodoAdapter(
    private var myDataset: List<DocumentSnapshot>,
    val onClickDeleteIcon: (todo: DocumentSnapshot) -> Unit, //함수 정의
    val onClickItem: (todo: DocumentSnapshot) -> Unit //함수 정의
) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    class TodoViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TodoAdapter.TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)

        return TodoViewHolder(ItemTodoBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = myDataset[position]
        holder.binding.todoTextView.text = todo.getString("text") ?: ""

        if ((todo.getBoolean("isDone") ?: false) == true) {
            holder.binding.todoTextView.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                setTypeface(null, Typeface.ITALIC)
            }
        } else {
            holder.binding.todoTextView.apply {
                paintFlags = 0
                setTypeface(null, Typeface.NORMAL)
            }
        }

        holder.binding.deleteImageView.setOnClickListener {
            onClickDeleteIcon.invoke(todo)
        }

        holder.binding.root.setOnClickListener {
            onClickItem.invoke(todo)
        }
    }

    override fun getItemCount() = myDataset.size

    fun setData(newData: List<DocumentSnapshot>) {
        myDataset = newData
        notifyDataSetChanged()
    }
}

class MainViewModel : ViewModel() {
    val db = Firebase.firestore

    val todoLiveData = MutableLiveData<List<DocumentSnapshot>>()

    init {
        fetchData()
    }

    fun fetchData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            db.collection(user.uid)
                .whereEqualTo("dayNum", dayN)
                .addSnapshotListener { value, error ->
                    if (0 == null) {
                        return@addSnapshotListener
                    }
                    if (value != null) {
                        todoLiveData.value = value.documents
                    }
                }
        }
    }

    fun toggleTodo(todo: DocumentSnapshot) {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val isDone = todo.getBoolean("isDone") ?: false
            db.collection(user.uid).document(todo.id).update("isDone", !isDone)
        }
    }

    fun addTodo(todo: Todo) {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            db.collection(user.uid).add(todo)
        }
    }

    fun deleteTodo(todo: DocumentSnapshot) {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            db.collection(user.uid).document(todo.id).delete()
        }
    }

}