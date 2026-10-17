package com.practica.expensetracker.ui;

import com.practica.expensetracker.exception.ValidacionGastoException;
import com.practica.expensetracker.model.Categoria;
import com.practica.expensetracker.model.Gasto;
import com.practica.expensetracker.repository.RepositorioGastos;
import com.practica.expensetracker.service.RastreadorGastos;
import com.practica.expensetracker.service.ServicioReporte;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class VentanaPrincipal extends JFrame {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Bogota");

    private final RastreadorGastos rastreador;
    private final ServicioReporte servicioReporte;

    private final JTextField campoMonto = new JTextField(8);
    private final JComboBox<Categoria> comboCategoria = new JComboBox<>(Categoria.values());
    private final JTextField campoFecha = new JTextField(LocalDate.now(ZONA_HORARIA).toString(), 10);
    private final JTextField campoDescripcion = new JTextField(15);

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Categoria", "Monto", "Fecha", "Descripcion"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaGastos = new JTable(modeloTabla);

    private final JLabel labelTotal = new JLabel("Total: 0");
    private final JLabel labelPromedio = new JLabel("Promedio: 0");
    private final JComboBox<Categoria> comboFiltro = new JComboBox<>(Categoria.values());
    private final JLabel labelTotalCategoria = new JLabel("Total categoria: 0");

    public VentanaPrincipal(RepositorioGastos repositorio) {
        super("Gestor de Gastos");
        this.rastreador = new RastreadorGastos(repositorio);
        this.servicioReporte = new ServicioReporte(repositorio);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JScrollPane scrollTabla = new JScrollPane(tablaGastos);
        scrollTabla.setPreferredSize(new Dimension(720, 220));

        add(construirFormulario(), BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
        add(construirPanelReporte(), BorderLayout.SOUTH);

        tablaGastos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaGastos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaGastos.getColumnModel().getColumn(0).setWidth(0);

        actualizarTabla();
        actualizarReporte();

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private JPanel construirFormulario() {
        JPanel filaCampos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaCampos.add(new JLabel("Monto:"));
        filaCampos.add(campoMonto);
        filaCampos.add(new JLabel("Categoria:"));
        filaCampos.add(comboCategoria);
        filaCampos.add(new JLabel("Fecha (yyyy-MM-dd):"));
        filaCampos.add(campoFecha);
        filaCampos.add(new JLabel("Descripcion:"));
        filaCampos.add(campoDescripcion);

        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton botonAgregar = new JButton("Agregar gasto");
        botonAgregar.addActionListener(e -> agregarGasto());
        filaBotones.add(botonAgregar);

        JButton botonEliminar = new JButton("Eliminar seleccionado");
        botonEliminar.addActionListener(e -> eliminarSeleccionado());
        filaBotones.add(botonEliminar);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(filaCampos);
        panel.add(filaBotones);
        return panel;
    }

    private JPanel construirPanelReporte() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(labelTotal);
        panel.add(labelPromedio);
        panel.add(new JLabel("   |   Categoria:"));
        panel.add(comboFiltro);
        panel.add(labelTotalCategoria);
        comboFiltro.addActionListener(e -> actualizarTotalCategoria());
        return panel;
    }

    private void agregarGasto() {
        try {
            BigDecimal monto = new BigDecimal(campoMonto.getText().trim());
            Categoria categoria = (Categoria) comboCategoria.getSelectedItem();
            LocalDate fecha = LocalDate.parse(campoFecha.getText().trim());
            String descripcion = campoDescripcion.getText().trim();

            Gasto gasto = new Gasto(monto, categoria, fecha, descripcion);
            rastreador.agregarGasto(gasto);

            limpiarFormulario();
            actualizarTabla();
            actualizarReporte();
        } catch (NumberFormatException ex) {
            mostrarError("El monto debe ser un numero valido, ej: 25.50");
        } catch (DateTimeParseException ex) {
            mostrarError("La fecha debe tener el formato yyyy-MM-dd, ej: 2026-08-20");
        } catch (ValidacionGastoException | IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void eliminarSeleccionado() {
        int fila = tablaGastos.getSelectedRow();
        if (fila < 0) {
            mostrarError("Selecciona un gasto de la tabla para eliminar");
            return;
        }
        String id = (String) modeloTabla.getValueAt(fila, 0);
        rastreador.eliminarGasto(id);
        actualizarTabla();
        actualizarReporte();
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        for (Gasto gasto : rastreador.listarGastos()) {
            modeloTabla.addRow(new Object[]{
                    gasto.getId(),
                    gasto.getCategoria(),
                    gasto.getMonto(),
                    gasto.getFecha(),
                    gasto.getDescripcion()
            });
        }
    }

    private void actualizarReporte() {
        labelTotal.setText("Total: " + servicioReporte.calcularTotal());
        labelPromedio.setText("Promedio: " + servicioReporte.calcularPromedio());
        actualizarTotalCategoria();
    }

    private void actualizarTotalCategoria() {
        Categoria categoria = (Categoria) comboFiltro.getSelectedItem();
        if (categoria != null) {
            labelTotalCategoria.setText("Total categoria: " + servicioReporte.calcularTotalPorCategoria(categoria));
        }
    }

    private void limpiarFormulario() {
        campoMonto.setText("");
        campoFecha.setText(LocalDate.now(ZONA_HORARIA).toString());
        campoDescripcion.setText("");
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
