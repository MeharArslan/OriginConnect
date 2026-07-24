package com.meharenterprises.originconnect.ui.calls
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.meharenterprises.originconnect.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallsFragment : Fragment() {
    override fun onCreateView(inf: LayoutInflater, cont: ViewGroup?, state: Bundle?): View =
        inf.inflate(R.layout.fragment_calls, cont, false)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }
}
