package py.edu.fpuna.dto;

import py.edu.fpuna.entities.Producto;
import py.edu.fpuna.enums.TipoDeMensaje;

import java.util.List;

public class MensajeServidor {
    private TipoDeMensaje tipo;
    private String mensaje;
    private List<Producto> productos;

    public MensajeServidor(TipoDeMensaje tipo, String mensaje, List<Producto> productos) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.productos = productos;
    }
}