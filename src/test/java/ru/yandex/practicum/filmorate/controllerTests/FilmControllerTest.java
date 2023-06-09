package ru.yandex.practicum.filmorate.controllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.ErrorResponse;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test_positive_film_adding() throws Exception {
        Film film = new Film();
        MPA mpa = new MPA();
        String filmAsString = objectMapper.writeValueAsString(film);
        String responseAsString = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmAsString)).andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ErrorResponse apiErrorResponse = objectMapper.readValue(responseAsString, ErrorResponse.class);
        assertEquals("У фильма нет названия", apiErrorResponse.getError());

        film.setName("title");
        film.setDescription("description");
        responseAsString = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film))).andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        apiErrorResponse = objectMapper.readValue(responseAsString, ErrorResponse.class);
        assertEquals("Дата выхода фильма отсутствует", apiErrorResponse.getError());

        film.setReleaseDate(LocalDate.of(2023, 12, 28));
        responseAsString = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film))).andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        apiErrorResponse = objectMapper.readValue(responseAsString, ErrorResponse.class);
        assertEquals("Продолжительность фильма должна быть больше нуля", apiErrorResponse.getError());

        film.setDuration(3000);
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film))).andExpect(status().is2xxSuccessful());

    }

    @Test
    public void test_positive_film_update() throws Exception {
        Film film = new Film();
        MPA mpa = new MPA();
        film.setId(125123);
        film.setName("title");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2023, 12, 28));
        film.setDuration(3000);
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);
        final String responseAsString = mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film))).andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(responseAsString, ErrorResponse.class);
        assertEquals("Нет такого фильма", errorResponse.getError());
        String newFilmAsString = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film))).andExpect(status().isOk()).andReturn()
                .getResponse()
                .getContentAsString();

        Film newFilm = objectMapper.readValue(newFilmAsString, Film.class);
        newFilm.setDescription("New Description");
        mockMvc.perform(put("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newFilm))).andExpect(status().isOk());
    }
}