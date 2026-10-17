package com.practica.expensetracker.service;

import com.practica.expensetracker.model.Categoria;
import com.practica.expensetracker.model.Gasto;
import com.practica.expensetracker.repository.RepositorioGastos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;

public class ServicioReporte {

    private static final BigDecimal FACTOR_DEDUCIBLE = new BigDecimal("0.5");
    private static final BigDecimal FACTOR_RECARGO_ENTRETENIMIENTO = new BigDecimal("1.2");
    private static final BigDecimal FACTOR_TRANSPORTE = new BigDecimal("0.8");
    private static final BigDecimal LIMITE_ENTRETENIMIENTO = new BigDecimal("100000");
    private static final BigDecimal LIMITE_TRANSPORTE = new BigDecimal("50000");

    private final RepositorioGastos repositorioGastos;

    public ServicioReporte(RepositorioGastos repositorioGastos) {
        this.repositorioGastos = Objects.requireNonNull(repositorioGastos, "El repositorio de gastos no puede ser nulo");
    }

    public BigDecimal calcularTotal() {
        return repositorioGastos.listarTodos().stream()
                .map(Gasto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularTotalPorCategoria(Categoria categoria) {
        return repositorioGastos.listarTodos().stream()
                .filter(gasto -> gasto.getCategoria() == categoria)
                .map(Gasto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularPromedio() {
        List<Gasto> gastos = repositorioGastos.listarTodos();
        if (gastos.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = calcularTotal();
        return total.divide(BigDecimal.valueOf(gastos.size()), MathContext.DECIMAL64);
    }

    // Total del mes ajustado: salud y educación cuentan al 50 %, entretenimiento mayor a 100.000
    // tiene recargo del 20 % y transporte mayor a 50.000 cuenta al 80 %.
    public BigDecimal calcularTotalAjustadoDelMes(int anio, int mes) {
        return repositorioGastos.listarTodos().stream()
                .filter(gasto -> esDelMes(gasto, anio, mes))
                .map(ServicioReporte::montoAjustado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean esDelMes(Gasto gasto, int anio, int mes) {
        return gasto.getFecha().getYear() == anio && gasto.getFecha().getMonthValue() == mes;
    }

    private static BigDecimal montoAjustado(Gasto gasto) {
        BigDecimal monto = gasto.getMonto();
        return switch (gasto.getCategoria()) {
            case SALUD, EDUCACION -> monto.multiply(FACTOR_DEDUCIBLE);
            case ENTRETENIMIENTO -> ajustarSiSupera(monto, LIMITE_ENTRETENIMIENTO, FACTOR_RECARGO_ENTRETENIMIENTO);
            case TRANSPORTE -> ajustarSiSupera(monto, LIMITE_TRANSPORTE, FACTOR_TRANSPORTE);
            default -> monto;
        };
    }

    private static BigDecimal ajustarSiSupera(BigDecimal monto, BigDecimal limite, BigDecimal factor) {
        if (monto.compareTo(limite) <= 0) {
            return monto;
        }
        return monto.multiply(factor);
    }
}
