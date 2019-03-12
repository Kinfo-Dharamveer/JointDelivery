package com.jointdelivery.fragments.homeFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jointdelivery.MyApplication
import com.jointdelivery.R
import com.jointdelivery.adapters.MessagesAdapter
import com.jointdelivery.auth.AuthManager
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannelListQuery
import com.sendbird.android.SendBirdException
import kotlinx.android.synthetic.main.fragment_messages_fragment.view.*
import javax.inject.Inject

open class MessagesFragmenrt : Fragment() {


    @Inject
    lateinit var authManager: AuthManager

    lateinit var messagesAdapter: MessagesAdapter
    private val CHANNEL_LIST_LIMIT = 15
    private var mSwipeRefresh: SwipeRefreshLayout? = null
    var mChannelListQuery: GroupChannelListQuery? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_messages_fragment, container, false)

        initView(view)

        return view
    }

    fun initView(view: View) {
        (activity!!.application as MyApplication).getAppComponent()?.inject(this)

        view.rv_messages_list.layoutManager = LinearLayoutManager(activity)
        mSwipeRefresh = view.findViewById(R.id.swipe_layout_group_channel_list)

      /*  mSwipeRefresh?.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {*/

                refresh()
       /*     }
        })
*/
    }


    private fun refresh() {
        refreshChannelList(CHANNEL_LIST_LIMIT)
    }

    private fun refreshChannelList(numChannels: Int) {
        mChannelListQuery = GroupChannel.createMyGroupChannelListQuery()
        mChannelListQuery?.setLimit(numChannels)

        mChannelListQuery?.next(GroupChannelListQuery.GroupChannelListQueryResultHandler { list, e ->
            if (e != null) {
                // Error!
                e.printStackTrace()
                return@GroupChannelListQueryResultHandler
            }

            messagesAdapter = MessagesAdapter(
                activity!!,
                list
            )
            view?.rv_messages_list?.adapter = messagesAdapter
        })


    }
}