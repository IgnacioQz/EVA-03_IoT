package com.example.eva_02_ignacioquiero

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eva_02_ignacioquiero.adapters.NoticiasAdapter
import com.example.eva_02_ignacioquiero.models.Noticia
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: FloatingActionButton
    private lateinit var adapter: NoticiasAdapter

    // Lista temporal de noticias (datos hardcodeados)
    private val noticiasList = listOf(
        Noticia(
            id = "1",
            titulo = "Debate Presidencial 2025",
            bajada = "Los candidatos presidenciales se enfrentaron en un acalorado debate sobre economía y seguridad.",
            imagenUrl = "",
            cuerpo = "El debate presidencial de anoche marcó un punto crucial en la campaña electoral. Los candidatos discutieron temas como la inflación, el empleo y las políticas de seguridad ciudadana. El encuentro, que duró dos horas, fue moderado por reconocidos periodistas y contó con la participación de los tres principales candidatos.",
            fecha = "15 Nov 2025"
        ),
        Noticia(
            id = "2",
            titulo = "Nueva Ley de Educación",
            bajada = "El congreso aprobó una reforma educativa que beneficiará a millones de estudiantes.",
            imagenUrl = "",
            cuerpo = "La nueva ley de educación busca modernizar el sistema educativo nacional, incorporando tecnología en las aulas y mejorando los salarios de los docentes. La reforma fue aprobada con amplio consenso y entrará en vigencia el próximo año académico.",
            fecha = "14 Nov 2025"
        ),
        Noticia(
            id = "3",
            titulo = "Avances en Tecnología Verde",
            bajada = "Empresas locales presentan innovaciones en energías renovables que prometen reducir emisiones.",
            imagenUrl = "",
            cuerpo = "Un grupo de empresas tecnológicas del país presentó sus últimos desarrollos en paneles solares de alta eficiencia y sistemas de almacenamiento de energía. Los expertos señalan que estas innovaciones podrían reducir significativamente la dependencia de combustibles fósiles en los próximos años.",
            fecha = "13 Nov 2025"
        ),
        Noticia(
            id = "4",
            titulo = "Campeonato Nacional de Fútbol",
            bajada = "El equipo local se coronó campeón tras una emocionante final ante su rival histórico.",
            imagenUrl = "",
            cuerpo = "En un partido lleno de emociones, el equipo local logró vencer 2-1 a su rival en la final del campeonato nacional. Los goles fueron anotados en los últimos minutos del partido, desatando la celebración de miles de aficionados que se dieron cita en el estadio.",
            fecha = "12 Nov 2025"
        ),
        Noticia(
            id = "5",
            titulo = "Descubrimiento Arqueológico",
            bajada = "Arqueólogos encuentran restos de una civilización antigua en el norte del país.",
            imagenUrl = "",
            cuerpo = "Un equipo de arqueólogos nacionales e internacionales descubrió restos de una civilización que habitó la región hace más de 3000 años. Entre los hallazgos se encuentran cerámicas, herramientas y estructuras arquitectónicas que podrían cambiar la comprensión de la historia precolombina de la región.",
            fecha = "11 Nov 2025"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ocultar ActionBar
        supportActionBar?.hide()

        // Inicializar vistas
        initializeViews()

        // Configurar RecyclerView
        setupRecyclerView()

        // Configurar listeners
        setupListeners()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.noticiasRecyclerView)
        addButton = findViewById(R.id.addButton)
    }

    private fun setupRecyclerView() {
        // Configurar el adapter
        adapter = NoticiasAdapter(noticiasList) { noticia ->
            // Acción al hacer click en una noticia
            // Navegar a DetalleNoticiaActivity pasando los datos
            val intent = Intent(this, DetalleNoticiaActivity::class.java).apply {
                putExtra("TITULO", noticia.titulo)
                putExtra("BAJADA", noticia.bajada)
                putExtra("CUERPO", noticia.cuerpo)
                putExtra("FECHA", noticia.fecha)
                putExtra("IMAGEN_URL", noticia.imagenUrl)
                putExtra("ID", noticia.id) // Por si lo necesitas después
            }
            startActivity(intent)
        }

        // Configurar el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        addButton.setOnClickListener {
            // Navegar a AgregarNoticiaActivity
            val intent = Intent(this, AgregarNoticiaActivity::class.java)
            startActivity(intent)
        }
    }
}