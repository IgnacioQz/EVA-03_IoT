package com.example.eva_02_ignacioquiero.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.eva_02_ignacioquiero.R
import com.example.eva_02_ignacioquiero.models.Noticia

class NoticiasAdapter(
    private val noticias: MutableList<Noticia>,
    private val onNoticiaClick: (Noticia) -> Unit
) : RecyclerView.Adapter<NoticiasAdapter.NoticiaViewHolder>() {

    class NoticiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imagenNoticia)
        val titulo: TextView = itemView.findViewById(R.id.tituloNoticia)
        val bajada: TextView = itemView.findViewById(R.id.bajadaNoticia)
        val fecha: TextView = itemView.findViewById(R.id.fechaNoticia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_noticia, parent, false)
        return NoticiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticiaViewHolder, position: Int) {
        val noticia = noticias[position]

        holder.titulo.text = noticia.titulo
        holder.bajada.text = noticia.bajada
        holder.fecha.text = noticia.fecha

        // Cargar imagen desde URL usando Coil
        if (noticia.imagenUrl.isNotEmpty()) {
            holder.imagen.load(noticia.imagenUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground) // Mientras carga
                error(android.R.drawable.ic_menu_gallery) // Si hay error
                transformations(RoundedCornersTransformation(8f))
            }
        } else {
            // Imagen por defecto si no hay URL
            holder.imagen.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onNoticiaClick(noticia)
        }
    }

    override fun getItemCount(): Int = noticias.size

    // Método para actualizar toda la lista
    fun updateNoticias(newNoticias: List<Noticia>) {
        noticias.clear()
        noticias.addAll(newNoticias)
        notifyDataSetChanged()
    }

    // Método para agregar una noticia al inicio
    fun addNoticia(noticia: Noticia) {
        noticias.add(0, noticia)
        notifyItemInserted(0)
    }

    // Método para limpiar la lista
    fun clear() {
        val size = noticias.size
        noticias.clear()
        notifyItemRangeRemoved(0, size)
    }
}