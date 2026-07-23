package com.TracoCultural.TracoCultural.config;

import com.TracoCultural.TracoCultural.model.Repository.CategoriaRepository;
import com.TracoCultural.TracoCultural.model.Repository.EventoRepository;
import com.TracoCultural.TracoCultural.model.entity.Categoria;
import com.TracoCultural.TracoCultural.model.entity.Evento;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class EventoSeeder implements CommandLineRunner {

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;

    public EventoSeeder(EventoRepository eventoRepository,
                        CategoriaRepository categoriaRepository) {
        this.eventoRepository = eventoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Categoria musica = criarCategoria("Música");
        Categoria gastronomia = criarCategoria("Gastronomia");
        Categoria cultura = criarCategoria("Cultura");
        Categoria tecnologia = criarCategoria("Tecnologia");
        Categoria educacao = criarCategoria("Educação");
        Categoria esporte = criarCategoria("Esporte");
        Categoria profissional = criarCategoria("Profissional");
        Categoria social = criarCategoria("Social");


        eventoRepository.save(
                criarEvento(
                        "São Paulo Oktoberfest",
                        "Festival com gastronomia, música e cultura alemã.",
                        sdf.parse("19/09/2026 12:00"),
                        sdf.parse("20/09/2026 22:00"),
                        "São Paulo",
                        gastronomia,
                        true,
                        false,
                        "https://saopaulooktoberfest.com.br"
                )
        );

        eventoRepository.save(
                criarEvento(
                        "Mostra Internacional de Cinema de São Paulo",
                        "Festival com exibição de filmes nacionais e internacionais.",
                        sdf.parse("22/10/2026 10:00"),
                        sdf.parse("04/11/2026 22:00"),
                        "São Paulo",
                        cultura,
                        true,
                        false,
                        "https://mostra.org"
                )
        );

        eventoRepository.save(
                criarEvento(
                        "Feira do Empreendedor Sebrae",
                        "Evento voltado ao empreendedorismo, inovação e negócios.",
                        sdf.parse("15/10/2026 09:00"),
                        sdf.parse("18/10/2026 20:00"),
                        "São Paulo",
                        profissional,
                        true,
                        false,
                        "https://feiradoempreendedor.sebraesp.com.br"
                )
        );
    }


    private Categoria criarCategoria(String nome) {

        return categoriaRepository.findByNome(nome)
                .orElseGet(() -> {
                    Categoria categoria = new Categoria();
                    categoria.setNome(nome);
                    return categoriaRepository.save(categoria);
                });
    }


    private Evento criarEvento(
            String nome,
            String descricao,
            Date dataInicio,
            Date dataFim,
            String cidade,
            Categoria categoria,
            boolean destacado,
            boolean patrocinado,
            String linkExterno) {

        Evento evento = new Evento();

        evento.setNome(nome);
        evento.setDescricao(descricao);
        evento.setDataInicio(dataInicio);
        evento.setDataFim(dataFim);
        evento.setCidade(cidade);
        evento.setCategoria(categoria);
        evento.setDestacado(destacado);
        evento.setPatrocinado(patrocinado);
        evento.setLinkExterno(linkExterno);

        return evento;
    }
}