package com.example.roomie.frontend

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roomie.R
import com.example.roomie.backend.domain.Chunk
import com.example.roomie.backend.domain.Room
import com.example.roomie.backend.utils.Pair
import com.example.roomie.frontend.Adapters.RoomsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/** Search Screen Activity class
 *
 * @author Maria Schoinaki, Eleni Kechrioti, Christos Stamoulos
 * @Details This project is being carried out in the course Distributed Systems @ Spring AUEB 2024.
 *
 * This class is implemented to visualize an implemented search screen.
 */
class SearchActivity : AppCompatActivity(), RoomsAdapter.onRoomClickListener {

    var clearbtn: Button? = null
    var searchbtn: Button? = null
    var searchResultsView: RecyclerView? = null
    var miniSearchbtn: TextView? = null
    var viewFlipper: ViewFlipper? = null
    var bottomNavigationView: BottomNavigationView? = null
    var whenbtn: LinearLayout? = null
    var whobtn: LinearLayout? = null
    var wherebtn: LinearLayout? = null
    var pricebtn: LinearLayout? = null
    var starsbtn: LinearLayout? = null
    var when_expanded: LinearLayout? = null
    var who_expanded: LinearLayout? = null
    var where_expanded: LinearLayout? = null
    var stars_expanded: LinearLayout? = null
    var price_expanded: LinearLayout? = null
    var calendarView: MaterialCalendarView? = null
    var maxPricetxt: TextView? = null
    var minPricetxt: TextView? = null
    var maxPriceSeekBar: SeekBar? = null
    var minPriceSeekBar: SeekBar? = null
    var location: String? = null
    var people = 0
    private var startDate: CalendarDay? = null
    private var finishDate: CalendarDay? = null
    var minPrice: Int = 0
    var maxPrice: Int = 0
    val minP = 0
    val maxP = 1000
    var star: Int = 0
    var userId: Int = 0

    /**
     * Initializes the class's attributes
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        userId = intent.getIntExtra("userId", -1)

        bottomNavigationView = findViewById<View>(R.id.bottomNav) as BottomNavigationView?

        bottomNavigationView?.setSelectedItemId(R.id.searchbtn)
        bottomNavigationView!!.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item: MenuItem ->
            val itemId = item.itemId
            if (item.itemId == R.id.accountbtn) {
                val intent: Intent = Intent(this, MyAccountActivity::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                        this,
                        R.anim.slide_in,  // Animation for the entering activity
                        R.anim.slide_out  // Animation for the exiting activity
                )
                intent.putExtra("userId", userId)
                startActivity(intent, options.toBundle())
            } else if (itemId == R.id.reservbtn) {
                val intent: Intent = Intent(this, ReservationsActivity::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                        this,
                        R.anim.slide_in,  // Animation for the entering activity
                        R.anim.slide_out  // Animation for the exiting activity
                )
                intent.putExtra("userId", userId)
                startActivity(intent, options.toBundle())
            } else if (itemId == R.id.homeBtn) {
                val intent: Intent = Intent(this, HomeScreenActivity::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                        this,
                        R.anim.slide_out ,  // Animation for the entering activity
                        R.anim.slide_in// Animation for the exiting activity
                )
                intent.putExtra("userId", userId)
                startActivity(intent, options.toBundle())
            }
            item.setChecked(true)
            true
        })

        viewFlipper = findViewById(R.id.viewFlipper)
        viewFlipper!!.displayedChild = 0;

        whoButton()
        whenButton()
        whereButton()
        priceButton()
        starsButton()
        clearButton()
        searchButton()
        miniSearchButton()
    }

    /**
     * Implements the clear button, clears all filters
     */
    fun clearButton(){
       clearbtn = findViewById(R.id.clearBtn)

        clearbtn!!.setOnClickListener {
            location = ""
            var placetxt = findViewById<TextView>(R.id.wheretxt)
            placetxt.text = "Place"

            people = 0
            var whotxt = findViewById<TextView>(R.id.whotxt)
            whotxt.text = "People"

            startDate = null
            finishDate = null
            var whentxt = findViewById<TextView>(R.id.whentxt)
            whentxt.text = "Date"

            minPrice = 0
            maxPrice = 0
            val pricetxt = findViewById<TextView>(R.id.pricetxt)
            pricetxt.text = "Value"
        }
    }

    /**
     * Implements the search button, sends the filters to master
     * and displays the search results to the screen
     */
    fun searchButton(){
        searchbtn = findViewById(R.id.searchBtn)
        searchResultsView = findViewById(R.id.searchResults)
        val grid = GridLayoutManager(this, 2)

        searchResultsView!!.layoutManager = grid


        searchbtn!!.setOnClickListener {

            var filters = createJson()
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {

                val chunk = Chunk(userId.toString(), 1, filters.toString())

                val backendCommunicator = BackendCommunicator()
                backendCommunicator.sendMasterInfo(chunk)
                val answer = backendCommunicator.sendClientInfo()
                var searchResults = answer.data as ArrayList<Pair<Room, ArrayList<ByteArray>>>

                // Switch to the main thread to update the UI
                withContext(Dispatchers.Main) {

                    searchResultsView!!.animate()
                            .translationY(-searchResultsView!!.height.toFloat())
                            .alpha(0.0f)
                            .setDuration(300)
                            .withEndAction {
                                // Once the filter layout is hidden, make it gone
                                viewFlipper!!.showNext()

                                // Slide in the results layout
                                searchResultsView!!.translationY = searchResultsView!!.height.toFloat()
                                searchResultsView!!.alpha = 0.0f
                                searchResultsView!!.animate()
                                        .translationY(0f)
                                        .alpha(1.0f)
                                        .setDuration(300)
                                        .start()
                            }
                            .start()

                    val roomsAdapter = RoomsAdapter(searchResults, this@SearchActivity)
                    searchResultsView!!.adapter = roomsAdapter
                }

            }
        }
    }

    /**
     * mini search button, for when the search results are displayed
     * to be able to return to search
     */
    fun miniSearchButton(){
        miniSearchbtn = findViewById(R.id.minisearchbtn)
        var searchResultsV = findViewById<ConstraintLayout>(R.id.searchResultsView)
        miniSearchbtn!!.setOnClickListener {

            searchResultsV.animate()
                    .translationY(searchResultsV.height.toFloat())  // Slide down
                    .alpha(0.0f)
                    .setDuration(300)
                    .withEndAction {
                        viewFlipper!!.showPrevious()
                        // Slide in the results layout
                        searchResultsV.translationY = -searchResultsV.height.toFloat()  // Start above the view
                        searchResultsV.alpha = 0.0f
                        searchResultsV.animate()
                                .translationY(0f)  // Slide down to original position
                                .alpha(1.0f)
                                .setDuration(300)
                                .start()
                    }
                    .start()
        }
    }

    /**
     * Implements the where filter, lets user write the location
     * he wants,
     */
    fun whereButton(){
        wherebtn = findViewById(R.id.wherebtn)
        where_expanded = findViewById(R.id.where_expanded)
        wherebtn!!.setOnClickListener {
            if (where_expanded!!.visibility == View.VISIBLE){
                where_expanded!!.visibility = View.GONE
                wherebtn!!.visibility = View.VISIBLE
            }else{
                where_expanded!!.visibility = View.VISIBLE
                wherebtn!!.visibility = View.GONE
            }
        }
        var skipWhere = findViewById<Button>(R.id.skipWhere)
        skipWhere.setOnClickListener{
            where_expanded!!.visibility = View.GONE
            wherebtn!!.visibility = View.VISIBLE
            when_expanded!!.visibility = View.VISIBLE
            whenbtn!!.visibility = View.GONE
        }

        var nextWhere = findViewById<Button>(R.id.nextWhere)
        var placetxt = findViewById<TextView>(R.id.wheretxt)
        var placeQuery = findViewById<EditText>(R.id.placeQuery)
        nextWhere.setOnClickListener {
            if (!placeQuery.equals("")){
                placetxt.text = placeQuery.text.toString()
                location = placeQuery.text.toString()
            }
            //TO DO: add message that place needs to be filled
            where_expanded!!.visibility = View.GONE
            wherebtn!!.visibility = View.VISIBLE
            when_expanded!!.visibility = View.VISIBLE
            whenbtn!!.visibility = View.GONE
        }

    }

    /**
     * Implements the when filter, lets the user pick a range of dates
     */
    fun whenButton(){
        whenbtn = findViewById(R.id.whenbtn)
        when_expanded = findViewById(R.id.when_expanded)
        whenbtn!!.setOnClickListener {
            if (when_expanded!!.visibility == View.VISIBLE){
                when_expanded!!.visibility = View.GONE
                whenbtn!!.visibility = View.VISIBLE
            }else{
                when_expanded!!.visibility = View.VISIBLE
                whenbtn!!.visibility = View.GONE
            }
        }
        var skipWhen = findViewById<Button>(R.id.skipWhen)
        skipWhen.setOnClickListener{
            when_expanded!!.visibility = View.GONE
            whenbtn!!.visibility = View.VISIBLE
            who_expanded!!.visibility = View.VISIBLE
            whobtn!!.visibility = View.GONE
        }

        var nextWhen = findViewById<Button>(R.id.nextWhen)
        var whentxt = findViewById<TextView>(R.id.whentxt)
        nextWhen.setOnClickListener {
            //TO DO: add clickable dates
            when_expanded!!.visibility = View.GONE
            whenbtn!!.visibility = View.VISIBLE
            who_expanded!!.visibility = View.VISIBLE
            whobtn!!.visibility = View.GONE
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
            if (startDate!=null && finishDate!=null){
                whentxt.text = startDate!!.date.toLocalDate().format(formatter)+ " - " + finishDate!!.date.toLocalDate().format(formatter)
            }else if(startDate==null && finishDate!=null){
                whentxt.text = " - " + finishDate!!.date.toLocalDate().format(formatter)
            }else if(startDate!=null && finishDate==null){
                whentxt.text = startDate!!.date.toLocalDate().format(formatter)+ " - "
            }else if(startDate!!.equals(finishDate)){
                whentxt.text = startDate!!.date.toLocalDate().format(formatter)
            }
        }

        calendarView = findViewById(R.id.when_calendar)


        calendarView!!.setOnDateChangedListener(object : OnDateSelectedListener {
            override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
                if (startDate == null) {
                    startDate = date
                } else if (finishDate == null) {
                    finishDate = date
                    // Optionally sort dates if needed
                    if (startDate!!.isAfter(finishDate!!)) {
                        val temp = startDate
                        startDate = finishDate
                        startDate = temp
                    }
                    highlightSelectedRange()
                } else {
                    // Reset the selection
                    startDate = date
                    finishDate = null
                    calendarView!!.clearSelection()
                    calendarView!!.setDateSelected(date, true)
                }
            }
        })

    }

    /**
     * Implements the who button, lets the user pick how
     * many people will stay on the accomodation
     */
    fun whoButton(){

        whobtn = findViewById(R.id.whobtn)
        who_expanded = findViewById(R.id.who_expanded)
        whobtn!!.setOnClickListener {
            if (who_expanded!!.visibility == View.VISIBLE){
                who_expanded!!.visibility = View.GONE
                whobtn!!.visibility = View.VISIBLE
            }else{
                who_expanded!!.visibility = View.VISIBLE
                whobtn!!.visibility = View.GONE
            }
        }

        var skipWho = findViewById<Button>(R.id.skipWho)
        skipWho.setOnClickListener{
            who_expanded!!.visibility = View.GONE
            whobtn!!.visibility = View.VISIBLE
            stars_expanded!!.visibility = View.VISIBLE
            starsbtn!!.visibility = View.GONE
        }

        var nextWho = findViewById<Button>(R.id.nextWho)
        var ppltxt = findViewById<TextView>(R.id.ppltxt)
        var whotxt = findViewById<TextView>(R.id.whotxt)
        nextWho.setOnClickListener {
            who_expanded!!.visibility = View.GONE
            whobtn!!.visibility = View.VISIBLE
            stars_expanded!!.visibility = View.VISIBLE
            ppltxt.text = people.toString()
            whotxt.text = people.toString()
            stars_expanded!!.visibility = View.VISIBLE
            starsbtn!!.visibility = View.GONE
        }

        var plusbtn = findViewById<Button>(R.id.plusppl)
        var ppl = findViewById<TextView>(R.id.minppl)
        plusbtn.setOnClickListener {
            people +=1
            ppl.text = people.toString()
        }

        var minusppl = findViewById<Button>(R.id.minusppl)
        minusppl.setOnClickListener {
            if(people > 0){
                people -=1
                ppl.text = people.toString()
            }
        }
    }

    /**
     * Implements the price filter, lets the user pick a range
     * of prices for a night in the object
     */
    fun priceButton(){

        pricebtn = findViewById(R.id.pricebtn)
        price_expanded = findViewById(R.id.price_expanded)
        pricebtn!!.setOnClickListener {
            if (price_expanded!!.visibility == View.VISIBLE){
                price_expanded!!.visibility = View.GONE
                pricebtn!!.visibility = View.VISIBLE
            }else{
                price_expanded!!.visibility = View.VISIBLE
                pricebtn!!.visibility = View.GONE
            }
        }

        var skipPrice = findViewById<Button>(R.id.skipPrice)
        skipPrice.setOnClickListener{
            pricebtn!!.visibility = View.VISIBLE
            price_expanded!!.visibility = View.GONE
        }

        var nextPrice = findViewById<Button>(R.id.nextPrice)
        nextPrice.setOnClickListener {
            //TO DO:
            who_expanded!!.visibility = View.GONE
            whobtn!!.visibility = View.VISIBLE
            pricebtn!!.visibility = View.VISIBLE
            price_expanded!!.visibility = View.GONE
            val ptxt = findViewById<TextView>(R.id.pricetxt)
            ptxt.text = "$minPrice - $maxPrice"

        }

        //TO DO: price range for max

        minPriceSeekBar = findViewById(R.id.minPriceBar)
        minPricetxt = findViewById(R.id.minPricetxt)


        // Set the minimum and maximum values for the SeekBar (e.g., price range)
        minPriceSeekBar!!.min = minP
        minPriceSeekBar!!.max = maxP
        var minprice = 0

        // Set up a listener to handle changes in the SeekBar progress
        minPriceSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress > maxPriceSeekBar!!.progress) {
                    minPriceSeekBar!!.progress = maxPriceSeekBar!!.progress
                } else {
                    updateMinPriceText(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Handle when the user starts dragging the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                minPrice = minprice
            }
        })

        maxPriceSeekBar = findViewById(R.id.maxPriceBar)
        maxPricetxt = findViewById(R.id.maxPricetxt)

        maxPriceSeekBar!!.min = minP
        maxPriceSeekBar!!.max = maxP
        var maxprice = 0

        // Set up a listener to handle changes in the SeekBar progress
        maxPriceSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < minPriceSeekBar!!.progress) {
                    maxPriceSeekBar!!.progress = minPriceSeekBar!!.progress
                } else {
                    updateMaxPriceText(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Handle when the user starts dragging the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                maxPrice = maxprice
            }
        })
    }

    /**
     * Updates the min price text, calculates the price in the
     * seek bar
     */
    fun updateMinPriceText(progress: Int) {
        val minprice = (minP + (maxP - minP) * (progress.toFloat() / minPriceSeekBar!!.max)).toInt()
        minPricetxt!!.text = minprice.toString()
        minPrice = minprice
    }

    /**
     * Updates the max price text, calculates the price in the
     * seek bar
     */
    fun updateMaxPriceText(progress: Int) {
        val maxprice = (minP + (maxP - minP) * (progress.toFloat() / maxPriceSeekBar!!.max)).toInt()
        maxPricetxt!!.text = maxprice.toString()
        maxPrice = maxprice
    }

    /**
     * Implements the stars filter, lets the user pick the minimum
     * stars he wants the accomodation to have
     */
    fun starsButton(){
        starsbtn = findViewById(R.id.starsbtn)
        stars_expanded = findViewById(R.id.stars_expanded)

        starsbtn!!.setOnClickListener(){
            if (stars_expanded!!.visibility == View.VISIBLE){
                stars_expanded!!.visibility = View.GONE
                starsbtn!!.visibility = View.VISIBLE
            }else{
                stars_expanded!!.visibility = View.VISIBLE
                starsbtn!!.visibility = View.GONE
            }
        }

        var skipStars = findViewById<Button>(R.id.skipStars)
        skipStars.setOnClickListener{
            stars_expanded!!.visibility = View.GONE
            starsbtn!!.visibility = View.VISIBLE
            price_expanded!!.visibility = View.VISIBLE
            pricebtn!!.visibility = View.GONE
        }

        var nextStars = findViewById<Button>(R.id.nextStars)
        var starstxt = findViewById<TextView>(R.id.starstxt)

        nextStars.setOnClickListener {
            stars_expanded!!.visibility = View.GONE
            starsbtn!!.visibility = View.VISIBLE
            price_expanded!!.visibility = View.VISIBLE
            pricebtn!!.visibility = View.GONE
            starstxt!!.text = star.toString()
        }

        val stars1 = findViewById<CheckBox>(R.id.star1)
        val stars2 = findViewById<CheckBox>(R.id.star2)
        val stars3 = findViewById<CheckBox>(R.id.star3)
        val stars4 = findViewById<CheckBox>(R.id.star4)
        val stars5 = findViewById<CheckBox>(R.id.star5)

// Set the OnCheckedChangeListener for each CheckBox
        stars1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // If this CheckBox is checked, uncheck all other CheckBoxes
                stars2.isChecked = false
                stars3.isChecked = false
                stars4.isChecked = false
                stars5.isChecked = false
                star = 1
            }
        }

        stars2.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // If this CheckBox is checked, uncheck all other CheckBoxes
                stars1.isChecked = false
                stars3.isChecked = false
                stars4.isChecked = false
                stars5.isChecked = false
                star = 2
            }
        }

        stars3.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // If this CheckBox is checked, uncheck all other CheckBoxes
                stars1.isChecked = false
                stars2.isChecked = false
                stars4.isChecked = false
                stars5.isChecked = false
                star = 3
            }
        }

        stars4.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // If this CheckBox is checked, uncheck all other CheckBoxes
                stars1.isChecked = false
                stars2.isChecked = false
                stars3.isChecked = false
                stars5.isChecked = false
                star = 4
            }
        }

        stars5.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // If this CheckBox is checked, uncheck all other CheckBoxes
                stars1.isChecked = false
                stars2.isChecked = false
                stars3.isChecked = false
                stars4.isChecked = false
                star = 5
            }
        }
    }

    /**
     * Creates a JSONObject with the filters the user picked
     *
     * @return the filters as JSONObject
     */
    fun createJson() : JSONObject {
        var filters = JSONObject()
        var filter = JSONObject()
        if (location.equals("") || location == null){
            filter.put("area", "default")
        }else{
            filter.put("area", location)
        }

        if(startDate!=null){
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
            val d = startDate!!.date.toLocalDate().format(formatter)
            filter.put("startDate", d)
        }else{
            filter.put("startDate", "01/01/0001")
        }

        if(finishDate!=null){
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
            val d = finishDate!!.date.toLocalDate().format(formatter)
            filter.put("finishDate", d)
        }else{
            filter.put("finishDate", "01/01/0001")
        }

        filter.put("noOfPeople", people)

        filter.put("lowPrice", minPrice)
        filter.put("highPrice", maxPrice)
        filter.put("stars", star)

        filters.put("filters", filter)
        return filters
    }

    /**
     * Highlights the range of dates the user selected
     */
    private fun highlightSelectedRange() {
        if (startDate != null && finishDate != null) {
            val startdate = startDate!!.date.toLocalDate()
            val enddate = finishDate!!.date.toLocalDate()
            val datesInRange = generateDatesInRange(startdate, enddate)

            datesInRange.forEach { date ->
                val calendarDay = CalendarDay.from(date.year, date.monthValue - 1, date.dayOfMonth)
                calendarView!!.setDateSelected(calendarDay, true)
            }
        }
    }

    /**
     * Turns a Date object into LocalDate
     * @return a LocalDate Object
     */
    private fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }


    /**
     * Generates a list of dates between the start and end date the
     * user picked
     *
     * @param startDate the start date the user picked
     * @param endDate   the end date the user picked
     * @return a List of LocalDate Objects
     */
    private fun generateDatesInRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val datesInRange = mutableListOf<LocalDate>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            datesInRange.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }
        return datesInRange
    }

    /**
     * Starts RoomDetailsActivity, when a user clicks a room
     *
     * @param room  the Room the user clicked
     * @param view the view
     */
    override fun onRoomClick(room: Room, view: View) {
        var intent = Intent(this, RoomDetailsActivity::class.java)
        val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.expand_trans,  // Animation for the entering activity
                R.anim.slide_out  // Animation for the exiting activity
        )
        intent.putExtra("roomId", room.id)
        intent.putExtra("userId", userId)
        startActivity(intent, options.toBundle())
    }
}