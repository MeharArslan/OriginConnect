package com.meharenterprises.originconnect.ui.updates
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.meharenterprises.originconnect.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdatesFragment : Fragment() {
    private val vm: UpdatesViewModel by viewModels()

    override fun onCreateView(inf: LayoutInflater, cont: ViewGroup?, state: Bundle?): View =
        inf.inflate(R.layout.fragment_updates, cont, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.myStatusRow).setOnClickListener {
            Toast.makeText(requireContext(), "Status feature coming in next update", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btnAddStatus).setOnClickListener {
            Toast.makeText(requireContext(), "Add status", Toast.LENGTH_SHORT).show()
        }
    }
}
