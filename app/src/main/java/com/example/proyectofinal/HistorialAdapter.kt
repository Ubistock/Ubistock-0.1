package com.example.proyectofinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.database.Historial

class HistorialAdapter(private val historialList: List<Historial>) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val aula: TextView = itemView.findViewById(R.id.aula)
        val categoria: TextView = itemView.findViewById(R.id.categoria)
        val fecha: TextView = itemView.findViewById(R.id.fecha)
        val hora: TextView = itemView.findViewById(R.id.hora)
        val status: TextView = itemView.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.historial_item, parent, false)
        return HistorialViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val historial = historialList[position]
        holder.aula.text = historial.aula.toString()
        holder.categoria.text = historial.categoria.toString()
        holder.fecha.text = historial.fecha
        holder.hora.text = historial.hora
        holder.status.text = historial.status.toString()
    }

    override fun getItemCount() = historialList.size
}
