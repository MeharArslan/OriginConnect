package com.meharenterprises.originconnect.ui.auth
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meharenterprises.originconnect.R
import com.meharenterprises.originconnect.data.model.Country
import com.meharenterprises.originconnect.data.model.CountryList

class CountryActivity : AppCompatActivity() {
    private lateinit var adapter: CountryAdapter
    private val allCountries = CountryList.all

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country)
        val recycler = findViewById<RecyclerView>(R.id.recyclerCountries)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
        adapter = CountryAdapter(allCountries) { country ->
            setResult(RESULT_OK, intent.putExtra("country_name", country.name))
            finish()
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().lowercase()
                adapter.filter(if (q.isEmpty()) allCountries else allCountries.filter {
                    it.name.lowercase().contains(q) || it.dialCode.contains(q)
                })
            }
        })
    }

    inner class CountryAdapter(private var list: List<Country>, private val onClick: (Country) -> Unit) :
        RecyclerView.Adapter<CountryAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val flag: TextView = v.findViewById(R.id.tvFlag)
            val name: TextView = v.findViewById(R.id.tvCountryName)
            val code: TextView = v.findViewById(R.id.tvDialCode)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false))
        override fun getItemCount() = list.size
        override fun onBindViewHolder(h: VH, pos: Int) {
            val c = list[pos]
            h.flag.text = c.flag; h.name.text = c.name; h.code.text = c.dialCode
            h.itemView.setOnClickListener { onClick(c) }
        }
        fun filter(newList: List<Country>) { list = newList; notifyDataSetChanged() }
    }
}
