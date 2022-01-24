import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ipcalink.R
import com.example.ipcalink.models.Notification
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.databinding.FragmentShowNotificacoesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ShowNotifications : Fragment() {

    private var _binding: FragmentShowNotificacoesBinding? = null
    private val binding get() = _binding!!

    private var list : ArrayList<Notification> = ArrayList()
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    private val dbFirebase = Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowNotificacoesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Hides top bar
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

        //generateAndSaveRsaKey(requireContext())
        //generateAndSaveAESKey()

        mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = mLayoutManager
        mAdapter = Adapter()
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.adapter = mAdapter

        GlobalScope.launch(Dispatchers.IO) {
            getNotifications(requireContext())
        }
    }

    private fun getNotifications(context: Context) {

        dbFirebase.collection("users").document("EJ1NUwpOoziRyiWWzNej").collection("notifications").addSnapshotListener { value, error ->

            if (error != null) {
                Log.w("ShowNotificationsFragment", "Listen failed.", error)
                return@addSnapshotListener
            }

            for(query in value!!){

                val notification = Notification.fromHash(query, context)

                val date = notification.sendDate!!.removeRange(5, 19)

                list.add(Notification(notification.id, notification.title, notification.body, notification.secretKey, notification.iv, date, notification.senderId))
            }

            binding.recyclerView.adapter!!.notifyItemInserted(list.size - 1)
        }
    }



    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_view_notification, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {
                val textViewTitle = findViewById<TextView>(R.id.textViewTitle)
                textViewTitle.text = list[position].title
                val textViewDescription = findViewById<TextView>(R.id.textViewDescription)
                textViewDescription.text = list[position].body
                val textViewDate = findViewById<TextView>(R.id.textViewDate)
                textViewDate.text = list[position].sendDate
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }
}