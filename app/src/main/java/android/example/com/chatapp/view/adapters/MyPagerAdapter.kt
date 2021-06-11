package android.example.com.chatapp.view.adapters

import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.ArrayList

class MyPagerAdapter(list: ArrayList<Fragment>, fm : FragmentActivity): FragmentStateAdapter(fm) {

    val fragmentList: ArrayList<Fragment> = list


    override fun getItemCount(): Int = fragmentList.size


    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }



}