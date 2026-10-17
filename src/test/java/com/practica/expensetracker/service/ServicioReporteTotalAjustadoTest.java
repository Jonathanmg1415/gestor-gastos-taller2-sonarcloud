package com.practica.expensetracker.service;

import com.practica.expensetracker.model.Categoria;
import com.practica.expensetracker.model.Gasto;
import com.practica.expensetracker.repository.RepositorioGastos;
import com.practica.expensetracker.support.GastoBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link ServicioReporte#calcularTotalAjustadoDelMes(int, int)}.
 * Cada caso del {@code @CsvSource} cubre una rama de las reglas de ajuste por categoria.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioReporte - Total ajustado del mes")
class ServicioReporteTotalAjustadoTest {

    private static final LocalDate FECHA_DEL_MES = LocalDate.of(2026, 10, 15);

    @Mock
    private RepositorioGastos repositorioGastos;

    @InjectMocks
    private ServicioReporte servicioReporte;

    @ParameterizedTest(name = "{0} de {1} se ajusta a {2}")
    @CsvSource({
            "SALUD,           40000,  20000",
            "EDUCACION,       80000,  40000",
            "ENTRETENIMIENTO, 150000, 180000",
            "ENTRETENIMIENTO, 100000, 100000",
            "TRANSPORTE,      60000,  48000",
            "TRANSPORTE,      50000,  50000",
            "COMIDA,          25000,  25000",
            "OTROS,           5000,   5000"
    })
    @DisplayName("Aplica el ajuste correspondiente a cada categoria")
    void dadoUnGastoDelMes_cuandoSeCalculaElTotalAjustado_entoncesAplicaElAjusteDeSuCategoria(
            Categoria categoria, String monto, String totalEsperado) {
        // Arrange
        Gasto gasto = GastoBuilder.unGasto()
                .conCategoria(categoria)
                .conMonto(monto)
                .conFecha(FECHA_DEL_MES)
                .construir();
        when(repositorioGastos.listarTodos()).thenReturn(List.of(gasto));

        // Act
        BigDecimal total = servicioReporte.calcularTotalAjustadoDelMes(2026, 10);

        // Assert
        assertEquals(0, new BigDecimal(totalEsperado).compareTo(total),
                () -> "Total esperado " + totalEsperado + " pero fue " + total);
    }

    @Test
    @DisplayName("Ignora los gastos de otro mes o de otro anio")
    void dadoGastosDeOtrosPeriodos_cuandoSeCalculaElTotalAjustado_entoncesSoloSumaLosDelMes() {
        // Arrange
        Gasto delMes = GastoBuilder.unGasto().conMonto("10000").conFecha(FECHA_DEL_MES).construir();
        Gasto otroMes = GastoBuilder.unGasto().conMonto("20000").conFecha(LocalDate.of(2026, 9, 15)).construir();
        Gasto otroAnio = GastoBuilder.unGasto().conMonto("30000").conFecha(LocalDate.of(2025, 10, 15)).construir();
        when(repositorioGastos.listarTodos()).thenReturn(List.of(delMes, otroMes, otroAnio));

        // Act
        BigDecimal total = servicioReporte.calcularTotalAjustadoDelMes(2026, 10);

        // Assert
        assertEquals(0, new BigDecimal("10000").compareTo(total),
                () -> "Total esperado 10000 pero fue " + total);
    }

    @Test
    @DisplayName("Suma los montos ajustados de varias categorias")
    void dadoGastosDeVariasCategorias_cuandoSeCalculaElTotalAjustado_entoncesSumaLosMontosAjustados() {
        // Arrange: 20.000 (SALUD al 50 %) + 180.000 (ENTRETENIMIENTO +20 %) + 25.000 (COMIDA) = 225.000
        Gasto medicina = GastoBuilder.unGasto().conCategoria(Categoria.SALUD).conMonto("40000")
                .conFecha(FECHA_DEL_MES).construir();
        Gasto concierto = GastoBuilder.unGasto().conCategoria(Categoria.ENTRETENIMIENTO).conMonto("150000")
                .conFecha(FECHA_DEL_MES).construir();
        Gasto almuerzo = GastoBuilder.unGasto().conCategoria(Categoria.COMIDA).conMonto("25000")
                .conFecha(FECHA_DEL_MES).construir();
        when(repositorioGastos.listarTodos()).thenReturn(List.of(medicina, concierto, almuerzo));

        // Act
        BigDecimal total = servicioReporte.calcularTotalAjustadoDelMes(2026, 10);

        // Assert
        assertEquals(0, new BigDecimal("225000").compareTo(total),
                () -> "Total esperado 225000 pero fue " + total);
    }

    @Test
    @DisplayName("Sin gastos el total ajustado es cero")
    void dadoRepositorioVacio_cuandoSeCalculaElTotalAjustado_entoncesEsCero() {
        // Arrange
        when(repositorioGastos.listarTodos()).thenReturn(Collections.emptyList());

        // Act
        BigDecimal total = servicioReporte.calcularTotalAjustadoDelMes(2026, 10);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(total),
                () -> "Total esperado 0 pero fue " + total);
    }
}
