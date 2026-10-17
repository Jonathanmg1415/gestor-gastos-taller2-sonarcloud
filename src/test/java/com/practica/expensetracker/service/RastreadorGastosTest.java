package com.practica.expensetracker.service;

import com.practica.expensetracker.model.Categoria;
import com.practica.expensetracker.model.Gasto;
import com.practica.expensetracker.repository.RepositorioGastos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RastreadorGastosTest {

  @Mock
  private RepositorioGastos repo;

  @InjectMocks
  private RastreadorGastos rastreadorGastos;

  private Gasto gastoComida1;
  private Gasto gastoComida2;
  private Gasto gastoEntretenimiento;
  private List<Gasto> lista;
  private void construirGastos() {
    this.gastoComida1 = new Gasto(
            BigDecimal.valueOf(25000L),
            Categoria.COMIDA,
            LocalDate.now(),
            "Gasto de comida para el almuerzo"
    );
    this.gastoComida2 = new Gasto(
            BigDecimal.valueOf(10000L),
            Categoria.COMIDA,
            LocalDate.now(),
            "Gasto de comida para la cena"
    );
    this.gastoEntretenimiento = new Gasto(
            BigDecimal.valueOf(40000L),
            Categoria.ENTRETENIMIENTO,
            LocalDate.now(),
            "Película en el cine"
    );
    this.lista = List.of(this.gastoComida1, this.gastoComida2, this.gastoEntretenimiento);
  }

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void when_agregar_gasto_then_guardar_gasto_en_repository() {
    construirGastos();
    var gasto = this.gastoComida1;

    rastreadorGastos.agregarGasto(gasto);

    verify(repo, times(1)).agregar(gasto);
  }

  @Test
  void when_agregar_gasto_and_repository_lanza_excepcion_then_lanzar_excepcion() {
    construirGastos();

    doThrow(new RuntimeException("message")).when(repo).agregar(any());

    var exception = assertThrows(RuntimeException.class, () -> {
      rastreadorGastos.agregarGasto(gastoComida1);
    });

    assertEquals("message", exception.getMessage());
  }

  @Test
  void when_eliminar_gasto_then_eliminar_gasto_en_repository() {
    var id = "id-of-expese";
    rastreadorGastos.eliminarGasto(id);

    verify(repo, times(1)).eliminar(id);
  }

  @Test
  void when_argumento_nulo_then_nada_sucede() {
    assertDoesNotThrow(() -> rastreadorGastos.agregarGasto(null));

    verify(repo, times(1)).agregar(null);
  }

  @Test
  void when_eliminar_gasto_and_repositorio_lanza_excepcion_then_lanzar_excepcion() {
    var id = "id-of-expese";
    doThrow(new RuntimeException("message")).when(repo).eliminar(id);

    var ex = assertThrows(RuntimeException.class, () -> {
      rastreadorGastos.eliminarGasto(id);
    });
    assertEquals("message", ex.getMessage());

  }

  @Test
  void when_listar_gasto_then_mostrar_gastos_de_repositorio() {
    construirGastos();
    when(repo.listarTodos()).thenReturn(lista);

    var actual =  rastreadorGastos.listarGastos();

    assertEquals(lista, actual);
    verify(repo, times(1)).listarTodos();
  }

  @Test
  void when_listar_gasto_and_repositorio_lanza_excepcion_then_lanzar_excepcion() {
    when(repo.listarTodos()).thenThrow(new RuntimeException("message"));

    var ex = assertThrows(RuntimeException.class, () -> {
      rastreadorGastos.listarGastos();
    });
    assertEquals("message", ex.getMessage());
  }


  @Test
  void when_listar_por_categorias_then_mostrar_gastos_de_repositorio() {
    construirGastos();

    when(repo.listarTodos()).thenReturn(lista);

    var actual =  rastreadorGastos.listarPorCategoria(Categoria.COMIDA);
    assertEquals(List.of(gastoComida1, gastoComida2), actual);
  }

  @Test
  void when_listar_por_categoria_and_no_coincidencias_then_retornar_lista_vacia() {
    construirGastos();

    when(repo.listarTodos()).thenReturn(lista);

    var actual = rastreadorGastos.listarPorCategoria(Categoria.SALUD);
    assertEquals(Collections.emptyList(), actual);
  }

  @Test
  void when_listar_por_categorias_and_argumento_nulo_then_lista_vacia() {
    construirGastos();

    when(repo.listarTodos()).thenReturn(lista);

    var actual = rastreadorGastos.listarPorCategoria(null);
    assertEquals(Collections.emptyList(), actual);
  }

  @Test
  void when_listarPorCategoria_and_repositorio_lista_vacia_then_lista_vacia() {
    when(repo.listarTodos()).thenReturn(Collections.emptyList());

    var actual = rastreadorGastos.listarPorCategoria(Categoria.SALUD);
    assertEquals(Collections.emptyList(), actual);
  }

  @Test
  void when_listarPorCategoria_and_repositorio_lanzar_excepcion_then_lanzar_excepcion() {
    when(repo.listarTodos()).thenThrow(new RuntimeException("message"));

    var ex = assertThrows(RuntimeException.class, () -> {
      rastreadorGastos.listarPorCategoria(Categoria.TRANSPORTE);
    });
    assertEquals("message", ex.getMessage());
  }
}