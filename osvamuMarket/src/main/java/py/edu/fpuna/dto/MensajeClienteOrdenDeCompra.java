package py.edu.fpuna.dto;
import py.edu.fpuna.enums.Opciones;

public class MensajeClienteOrdenDeCompra {

        private Opciones opcion;
        private int idProducto;
        private int cantidad;

    public MensajeClienteOrdenDeCompra(Opciones opcion, int idProducto, int cantidad) {
        this.opcion = opcion;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
    }

    public Opciones getOpcion() {
        return opcion;
    }

    public void setOpcion(Opciones opcion) {
        this.opcion = opcion;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
