package com.example.proyectofinal.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.R

class HistorialAdapter(private val historialList: List<Historial>) :
    RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val aulaTextView: TextView = itemView.findViewById(R.id.textViewAula)
        val categoriaTextView: TextView = itemView.findViewById(R.id.textViewCategoria)
        val componenteTextView: TextView = itemView.findViewById(R.id.textViewComponente)
        val fechaTextView: TextView = itemView.findViewById(R.id.textViewFecha)
        val horaTextView: TextView = itemView.findViewById(R.id.textViewHora)
        val statusTextView: TextView = itemView.findViewById(R.id.textViewStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.historial_item, parent, false)
        return HistorialViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val currentItem = historialList[position]
        holder.aulaTextView.text = currentItem.aula.toString()
        holder.categoriaTextView.text = currentItem.categoria.toString()
        holder.componenteTextView.text = currentItem.componente.toString()
        holder.fechaTextView.text = currentItem.fecha
        holder.horaTextView.text = currentItem.hora
        holder.statusTextView.text = currentItem.status.toString()
    }

    override fun getItemCount() = historialList.size
}