package com.meharenterprises.originconnect.ui.communities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.meharenterprises.originconnect.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunitiesFragment : Fragment() {
    private val vm: CommunitiesViewModel by viewModels()
    override fun onCreateView(inf: LayoutInflater, cont: ViewGroup?, state: Bundle?): View =
        inf.inflate(R.layout.fragment_communities, cont, false)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnNewCommunity).setOnClickListener {
            Toast.makeText(requireContext(), "Create community", Toast.LENGTH_SHORT).show()
        }
    }
}
