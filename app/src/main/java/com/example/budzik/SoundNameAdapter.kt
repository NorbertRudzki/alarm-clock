package com.example.budzik

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView

class SoundNameAdapter(
    private val list: ArrayList<String>,private val model: SoundViewModel ): RecyclerView.Adapter<SoundNameAdapter.MyViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.sound_row, parent, false)
        return MyViewHolder(row)
    }

    override fun getItemCount(): Int {
        return  list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.soundName.text = list[position]
        holder.soundName.setOnClickListener {
            Log.d("tak","tak")
            model.setString(holder.soundName.text.toString())
        }
    }

    inner class MyViewHolder(v: View):RecyclerView.ViewHolder(v){
        val soundName: TextView = v.findViewById(R.id.sound_name_row)

    }
}