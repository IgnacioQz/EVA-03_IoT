package com.example.eva_02_ignacioquiero

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eva_02_ignacioquiero.adapters.NoticiasAdapter
import com.example.eva_02_ignacioquiero.firebase.FirebaseHelper
import com.example.eva_02_ignacioquiero.models.Noticia
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addNoticiaCard: com.google.android.material.card.MaterialCardView
    private lateinit var userIconImageView: ImageView
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: NoticiasAdapter

    private val firebaseHelper = FirebaseHelper()
    private var noticiasList = mutableListOf<Noticia>()

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        initializeViews()
        setupRecyclerView()
        setupListeners()

        // Cargar noticias desde Firebase
        loadNoticiasFromFirebase()
    }

    override fun onResume() {
        super.onResume()
        // Recargar noticias cada vez que volvemos a esta pantalla
        Log.d(TAG, "onResume - Recargando noticias...")
        loadNoticiasFromFirebase()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.noticiasRecyclerView)
        addNoticiaCard = findViewById(R.id.addNoticiaCard)
        userIconImageView = findViewById(R.id.userIconImageView)

        // Crear TextView para estado vacío si no existe en el layout
        emptyTextView = TextView(this).apply {
            text = "📰 No hay noticias aún\n\nPresiona el botón + para agregar la primera noticia"
            textSize = 16f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(32, 32, 32, 32)
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            visibility = View.GONE
        }

        // Agregar el TextView al layout principal si no existe
        val mainLayout = findViewById<View>(android.R.id.content).parent as? android.view.ViewGroup
        mainLayout?.addView(emptyTextView)
    }

    private fun setupRecyclerView() {
        adapter = NoticiasAdapter(noticiasList) { noticia ->
            val intent = Intent(this, DetalleNoticiaActivity::class.java).apply {
                putExtra("TITULO", noticia.titulo)
                putExtra("BAJADA", noticia.bajada)
                putExtra("CUERPO", noticia.cuerpo)
                putExtra("FECHA", noticia.fecha)
                putExtra("IMAGEN_URL", noticia.imagenUrl)
                putExtra("ID", noticia.id)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true) // Optimización
    }

    private fun setupListeners() {
        addNoticiaCard.setOnClickListener {
            val intent = Intent(this, AgregarNoticiaActivity::class.java)
            startActivity(intent)
        }

        userIconImageView.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadNoticiasFromFirebase() {
        Log.d(TAG, "Iniciando carga de noticias...")

        // Mostrar loading
        setLoading(true)

        firebaseHelper.getNoticias(
            onSuccess = { noticias ->
                setLoading(false)

                Log.d(TAG, "✅ Noticias cargadas exitosamente: ${noticias.size} noticias")

                // Actualizar la lista
                noticiasList.clear()
                noticiasList.addAll(noticias)

                // Ordenar por fecha (más recientes primero) manualmente
                noticiasList.sortByDescending { it.fecha }

                // Notificar al adapter
                adapter.notifyDataSetChanged()

                // Mostrar mensaje si no hay noticias
                if (noticias.isEmpty()) {
                    showEmptyState()
                    Toast.makeText(
                        this,
                        "No hay noticias. ¡Agrega la primera!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    hideEmptyState()
                    Toast.makeText(
                        this,
                        "✅ ${noticias.size} noticia(s) cargada(s)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onFailure = { errorMessage ->
                setLoading(false)
                Log.e(TAG, "❌ Error al cargar noticias: $errorMessage")

                Toast.makeText(
                    this,
                    "Error al cargar noticias: $errorMessage",
                    Toast.LENGTH_LONG
                ).show()

                showEmptyState()
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            recyclerView.visibility = View.GONE
            emptyTextView.text = "⏳ Cargando noticias..."
            emptyTextView.visibility = View.VISIBLE
        } else {
            // La visibilidad se maneja en showEmptyState/hideEmptyState
        }
    }

    private fun showEmptyState() {
        emptyTextView.text = "📰 No hay noticias aún\n\nPresiona el botón + para agregar la primera noticia"
        emptyTextView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        emptyTextView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí, cerrar sesión") { dialog, _ ->
                dialog.dismiss()
                logout()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logout() {
        // Cerrar sesión en Firebase
        firebaseHelper.logout()

        Toast.makeText(this, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Salir")
            .setMessage("¿Deseas salir de la aplicación?")
            .setPositiveButton("Sí") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}