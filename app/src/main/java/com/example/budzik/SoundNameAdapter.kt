package com.example.budzik

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

/** Klasa odpowiedzialna za poprawną prezentację dostępnych dźwięków budzika
 *  z wykorzystaniem RecyclerView oraz obsługę zdarzeń z interfejsu graficznego
 */
class SoundNameAdapter(
    private val list: ArrayList<String>, private val model: SoundViewModel, private val file: File
) : RecyclerView.Adapter<SoundNameAdapter.MyViewHolder>() {
    private val NUMBER_OF_DEFAULT_SOUNDS = 5

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row = layoutInflater.inflate(R.layout.sound_row, parent, false)
        return MyViewHolder(row)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.soundName.text = list[position]
        holder.soundName.setOnClickListener {
            model.setString(holder.soundName.text.toString())
        }

        holder.soundName.setOnLongClickListener {
            if (holder.adapterPosition >= NUMBER_OF_DEFAULT_SOUNDS) {
                val string = holder.soundName.text.toString()
                val file_remove = File(file, string)
                file_remove.delete()
                list.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                model.setString("buzzer")
            }
            true
        }
    }

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val soundName: TextView = v.findViewById(R.id.sound_name_row)
    }
}