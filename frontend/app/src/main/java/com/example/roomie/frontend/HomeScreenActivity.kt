package com.example.roomie.frontend

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roomie.R
import com.example.roomie.backend.domain.Chunk
import com.example.roomie.backend.domain.Room
import com.example.roomie.frontend.Adapters.RoomsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HomeScreenActivity : AppCompatActivity(), RoomsAdapter.onRoomClickListener {

    var bottomNavigationView: BottomNavigationView? = null
    var recyclerView: RecyclerView? = null
    var backendCommunicator: BackendCommunicator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)

        bottomNavigationView = findViewById<View>(R.id.bottomNav) as BottomNavigationView?

        bottomNavigationView?.setSelectedItemId(R.id.homeBtn)
        bottomNavigationView!!.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item: MenuItem ->
            val itemId = item.itemId
            if (item.itemId == R.id.accountbtn) {
                val intent: Intent = Intent(this, HomeScreenActivity::class.java)
                startActivity(intent)
            } else if (itemId == R.id.reservbtn) {
                val intent: Intent = Intent(this, HomeScreenActivity::class.java)
                startActivity(intent)
            } else if (itemId == R.id.searchbtn) {
                val intent: Intent = Intent(this, SearchActivity::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                        this,
                        R.anim.slide_in,  // Animation for the entering activity
                        R.anim.slide_out  // Animation for the exiting activity
                )
                startActivity(intent, options.toBundle())
                //startActivity(intent)
                //overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,R.anim.slide_in, R.anim.slide_out, 0)
            }
            item.setChecked(true)
            true
        })

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)!!
        val grid = GridLayoutManager(this, 2)
        recyclerView!!.layoutManager = grid
        var rooms = ArrayList<Room>()

        /*
        backendCommunicator = BackendCommunicator()
        backendCommunicator!!.attemptConnection()
        backendCommunicator!!.sendMasterInfo(Chunk("", 1, generateFilterAll()))

        var masterInput = backendCommunicator!!.sendClientInfo()
        val rooms = masterInput.data as ArrayList<Room>

        Log.d("HomeScreen", rooms.toString())
        val roomsAdapter = RoomsAdapter(rooms, this)
        recyclerView!!.adapter = roomsAdapter*/

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            val to = Chunk("", 1, generateFilterAll().toString())

            val backendCommunicator = BackendCommunicator()
            Log.d("HomeScreen1", rooms.toString())
            backendCommunicator.sendMasterInfo(to)
            Log.d("HomeScreen2", rooms.toString())
            val answer = backendCommunicator.sendClientInfo()

            rooms = answer.data as ArrayList<Room>

            Log.d("HomeScreen", rooms.toString())


        }
        Log.d("RRRRRRRRRRRRRRRRRR", rooms.toString())
        val roomsAdapter = RoomsAdapter(rooms, this)
        recyclerView!!.adapter = roomsAdapter
    }

    override fun onRoomClick(room: Room, view: View) {
        var intent = Intent(this, RoomDetailsActivity::class.java)
        val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.expand_trans,  // Animation for the entering activity
                R.anim.slide_out  // Animation for the exiting activity
        )
        startActivity(intent, options.toBundle())
        //startActivity(intent)
    }

    fun generateFilterAll(): JSONObject{
        var filter = JSONObject()
        filter.put("area", "default")
        filter.put("startDate", "01/01/0001")
        filter.put("finishDate", "01/01/0001")
        filter.put("noOfPeople", 0)
        filter.put("lowPrice", 0)
        filter.put("highPrice", 0)
        filter.put("stars", 0.0)

        var f = JSONObject()
        f.put("filters", filter)
        return f
    }
}