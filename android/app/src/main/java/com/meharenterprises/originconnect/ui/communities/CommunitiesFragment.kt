package com.meharenterprises.originconnect.ui.communities
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.meharenterprises.originconnect.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunitiesFragment : Fragment() {
    override fun onCreateView(inf: LayoutInflater, cont: ViewGroup?, state: Bundle?): View =
        inf.inflate(R.layout.fragment_communities, cont, false)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try { view.findViewById<View>(R.id.btnNewCommunity)?.setOnClickListener { Toast.makeText(requireContext(),"Community coming soon",Toast.LENGTH_SHORT).show() } } catch (_: Exception) {}
    }
}
