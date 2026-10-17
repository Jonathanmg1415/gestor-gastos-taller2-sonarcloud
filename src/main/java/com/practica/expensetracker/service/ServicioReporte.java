package com.practica.expensetracker.service;

import com.practica.expensetracker.model.Categoria;
import com.practica.expensetracker.model.Gasto;
import com.practica.expensetracker.repository.RepositorioGastos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;

public class ServicioReporte {

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
        BigDecimal total = BigDecimal.ZERO;
        List<Gasto> gastos = repositorioGastos.listarTodos();
        if (gastos != null) {
            for (Gasto gasto : gastos) {
                if (gasto.getFecha().getYear() == anio && gasto.getFecha().getMonthValue() == mes) {
                    if (gasto.getCategoria() == Categoria.SALUD || gasto.getCategoria() == Categoria.EDUCACION) {
                        total = total.add(gasto.getMonto().multiply(new BigDecimal("0.5")));
                    } else if (gasto.getCategoria() == Categoria.ENTRETENIMIENTO) {
                        if (gasto.getMonto().compareTo(new BigDecimal("100000")) > 0) {
                            total = total.add(gasto.getMonto().multiply(new BigDecimal("1.2")));
                        } else {
                            total = total.add(gasto.getMonto());
                        }
                    } else if (gasto.getCategoria() == Categoria.TRANSPORTE
                            && gasto.getMonto().compareTo(new BigDecimal("50000")) > 0) {
                        total = total.add(gasto.getMonto().multiply(new BigDecimal("0.8")));
                    } else {
                        total = total.add(gasto.getMonto());
                    }
                }
            }
        }
        return total;
    }
}
